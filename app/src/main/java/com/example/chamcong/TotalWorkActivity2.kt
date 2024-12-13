package com.example.chamcong

import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TotalWorkActivity2 : AppCompatActivity() {

    private lateinit var spinnerMonth: Spinner
    private lateinit var btnTotalWork: Button
    private lateinit var btnTotalLeave: Button
    private lateinit var listViewDetails: ListView
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private val dataList = mutableListOf<String>()
    private val leaveRequestList = mutableListOf<LeaveRequest>()
    private val calendar = Calendar.getInstance()
    private var targetEmail: String? = null // Email của user hoặc nhân viên được chọn

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_total_work2)

        // Khởi tạo BottomNavigationView và xử lý sự kiện chọn mục
        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.DSNhanVien -> {
                    startActivity(Intent(this, EmployeeListActivity::class.java))
                    true
                }
                R.id.DSTongCong -> {
                    startActivity(Intent(this, AllTimeActivity::class.java))
                    true
                }
                R.id.DSTanCa -> {
                    startActivity(Intent(this, CheckoutListActivity::class.java))
                    true
                }
                R.id.TaiKhoanAd -> {
                    startActivity(Intent(this, MainActivityAd::class.java))
                    true
                }
                else -> false
            }
        }

        // Initialize Firebase
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Initialize views
        spinnerMonth = findViewById(R.id.spinnerMonth)
        btnTotalWork = findViewById(R.id.btnTotalWork)
        btnTotalLeave = findViewById(R.id.btnTotalLeave)
        listViewDetails = findViewById(R.id.listViewDetails)

        // Initialize adapter for ListView
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, dataList)
        listViewDetails.adapter = adapter

        // Setup spinner with months
        setupMonthSpinner()

        // Determine target email
        handleTargetEmail()

        // Button listeners
        btnTotalWork.setOnClickListener {
            calculateTotalWork()
        }

        btnTotalLeave.setOnClickListener {
            calculateTotalLeave()
        }

        // Set click listener for each item in the list view to toggle leave request status
        listViewDetails.setOnItemClickListener { _, _, position, _ ->
            if (position > 0) { // To avoid clicking the total count row
                toggleLeaveRequestStatus(position - 1)  // Corrected here: position - 1 because we are not modifying the first row.
            }
        }
    }

    private fun handleTargetEmail() {
        // Lấy email từ Intent, nếu không có thì dùng email của chính người dùng
        targetEmail = intent.getStringExtra("EMPLOYEE_EMAIL") ?: auth.currentUser?.email

        if (targetEmail == null) {
            Toast.makeText(this, "Không xác định được email", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupMonthSpinner() {
        val months = (1..12).map { it.toString().padStart(2, '0') }
        val currentMonth = calendar.get(Calendar.MONTH) + 1 // Months are 0-based

        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, months)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerMonth.adapter = spinnerAdapter
        spinnerMonth.setSelection(currentMonth - 1)
    }

    private fun calculateTotalWork() {
        val selectedMonth = spinnerMonth.selectedItem.toString()

        if (targetEmail == null) {
            Toast.makeText(this, "Không xác định được email", Toast.LENGTH_SHORT).show()
            return
        }

        val year = calendar.get(Calendar.YEAR)

        firestore.collection("CheckIn")
            .whereEqualTo("email", targetEmail)
            .get()
            .addOnSuccessListener { result ->
                val workDays = mutableListOf<String>()

                // Duyệt qua từng tài liệu CheckIn và kiểm tra tháng
                for (document in result) {
                    val date = document.getString("date") ?: continue
                    if (date.startsWith("$year-$selectedMonth")) {
                        workDays.add(date) // Thêm ngày làm việc vào danh sách
                    }
                }

                // Tính tổng số ngày làm việc
                val totalWorkDays = workDays.size
                if (totalWorkDays > 0) {
                    dataList.clear()
                    dataList.add("Tổng công làm tháng $selectedMonth: $totalWorkDays ngày")
                    dataList.addAll(workDays) // Hiển thị thêm các ngày làm việc chi tiết
                } else {
                    dataList.clear()
                    dataList.add("Không có ngày làm việc trong tháng $selectedMonth")
                }

                // Hiển thị tổng số ngày làm việc và cập nhật giao diện
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to fetch data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun calculateTotalLeave() {
        val selectedMonth = spinnerMonth.selectedItem.toString()

        if (targetEmail == null) {
            Toast.makeText(this, "Không xác định được email", Toast.LENGTH_SHORT).show()
            return
        }

        val year = calendar.get(Calendar.YEAR)

        firestore.collection("leaveRequests")
            .whereEqualTo("employeeId", targetEmail)
            .get()
            .addOnSuccessListener { result ->
                leaveRequestList.clear() // Reset list before adding new data
                dataList.clear() // Reset dataList before updating

                var leaveRequestCount = 0 // Biến để đếm số lần gửi đơn nghỉ phép trong tháng
                val startYearMonth = "$year-$selectedMonth" // Dùng để kiểm tra năm-tháng

                // Duyệt qua các đơn nghỉ phép và kiểm tra tháng
                for (document in result) {
                    val leaveRequest = document.toObject(LeaveRequest::class.java)
                    val startDateParts = leaveRequest.startDate?.split("/")
                    val endDateParts = leaveRequest.endDate?.split("/")

                    if (startDateParts?.size == 3 && endDateParts?.size == 3) {
                        val startYear = startDateParts[2].toIntOrNull()
                        val startMonth = startDateParts[1].toIntOrNull()
                        val endYear = endDateParts[2].toIntOrNull()
                        val endMonth = endDateParts[1].toIntOrNull()

                        // Kiểm tra nếu tháng bắt đầu và tháng kết thúc trong tháng đã chọn
                        if (startYear == year && startMonth == selectedMonth.toInt() &&
                            endYear == year && endMonth == selectedMonth.toInt()) {
                            leaveRequestCount++
                            // Thêm thông tin chi tiết vào dataList
                            dataList.add("Đơn nghỉ phép từ ngày ${leaveRequest.startDate} đến ngày ${leaveRequest.endDate} - Trạng thái: ${leaveRequest.status}")
                        }
                    }
                }

                // Tính tổng số đơn nghỉ phép trong tháng và hiển thị ngay
                if (leaveRequestCount > 0) {
                    dataList.add("Tổng số đơn nghỉ phép trong tháng $selectedMonth: $leaveRequestCount đơn")
                } else {
                    dataList.add("Không có đơn nghỉ phép trong tháng $selectedMonth")
                }

                // Cập nhật giao diện
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to fetch leave data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun toggleLeaveRequestStatus(position: Int) {
        // Lấy đơn nghỉ phép từ danh sách
        val leaveRequest = leaveRequestList[position]

        // Thay đổi trạng thái
        val newStatus = when (leaveRequest.status) {
            "pending" -> "accepted"
            "accepted" -> "disaccepted"
            else -> "pending"  // Nếu là "disaccepted" thì quay lại "pending"
        }

        // Cập nhật trạng thái trong Firestore
        firestore.collection("leaveRequests")
            .whereEqualTo("employeeId", leaveRequest.employeeId)
            .whereEqualTo("startDate", leaveRequest.startDate)
            .whereEqualTo("endDate", leaveRequest.endDate)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    // Cập nhật trạng thái của đơn nghỉ phép trong Firestore
                    document.reference.update("status", newStatus)
                        .addOnSuccessListener {
                            // Cập nhật trạng thái trong đối tượng LeaveRequest
                            leaveRequest.status = newStatus

                            // Cập nhật trực tiếp vào dataList (dùng cho ListView)
                            dataList[position + 1] = "Đơn nghỉ phép từ ngày ${leaveRequest.startDate} đến ngày ${leaveRequest.endDate} - Trạng thái: ${newStatus}"

                            // Notify adapter để làm mới giao diện
                            adapter.notifyDataSetChanged()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Failed to update status: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}







