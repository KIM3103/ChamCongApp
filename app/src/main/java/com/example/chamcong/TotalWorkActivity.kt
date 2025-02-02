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

class TotalWorkActivity : AppCompatActivity() {

    private lateinit var spinnerMonth: Spinner
    private lateinit var btnTotalWork: Button
    private lateinit var btnTotalLeave: Button
    private lateinit var listViewDetails: ListView
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private val dataList = mutableListOf<String>()
    private val calendar = Calendar.getInstance()
    private var targetEmail: String? = null // Email của user hoặc nhân viên được chọn

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_total_work)

        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottomNavigationView)

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.TrangChu -> {
                    startActivity(Intent(this, CheckInActivity::class.java))
                    true
                }
                R.id.DonNghi -> {
                    startActivity(Intent(this, LeaveRequestActivity::class.java))
                    true
                }
                R.id.LichSu -> {
                    startActivity(Intent(this, LeaveHistoryActivity::class.java))
                    true
                }
                R.id.TongCong -> {
                    startActivity(Intent(this, TotalWorkActivity::class.java))
                    true
                }
                R.id.TaiKhoan -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                else -> false
            }
        }

        //tong cong user

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

                for (document in result) {
                    val date = document.getString("date") ?: continue
                    if (date.startsWith("$year-$selectedMonth")) {
                        workDays.add(date) // Thêm ngày làm việc vào danh sách
                    }
                }

                if (workDays.isNotEmpty()) {
                    dataList.clear()
                    dataList.add("Tổng công làm tháng $selectedMonth: ${workDays.size} ngày")
                    dataList.addAll(workDays) // Thêm danh sách các ngày làm việc chi tiết
                } else {
                    dataList.clear()
                    dataList.add("Không có ngày làm việc trong tháng $selectedMonth")
                }

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
                var leaveRequestCount = 0 // Biến để đếm số lần gửi đơn nghỉ phép trong tháng
                val leaveRequestDetails = mutableListOf<String>() // Danh sách lưu các chi tiết đơn nghỉ phép

                for (document in result) {
                    val startDate = document.getString("startDate") ?: continue
                    val endDate = document.getString("endDate") ?: continue

                    // Phân tích ngày bắt đầu và kết thúc từ startDate và endDate
                    val startDateParts = startDate.split("/")
                    val endDateParts = endDate.split("/")

                    if (startDateParts.size == 3 && endDateParts.size == 3) {
                        val startYear = startDateParts[2].toIntOrNull()
                        val startMonth = startDateParts[1].toIntOrNull()
                        val endYear = endDateParts[2].toIntOrNull()
                        val endMonth = endDateParts[1].toIntOrNull()

                        // Kiểm tra nếu năm và tháng của startDate và endDate trùng với năm và tháng đã chọn
                        if (startYear == year && startMonth == selectedMonth.toInt() &&
                            endYear == year && endMonth == selectedMonth.toInt()) {

                            leaveRequestCount++ // Tăng biến đếm mỗi khi tìm thấy đơn nghỉ phép trong tháng đã chọn
                            leaveRequestDetails.add("Đơn nghỉ phép từ ngày $startDate đến ngày $endDate") // Thêm chi tiết vào danh sách
                        }
                    }
                }

                // Cập nhật giao diện với số lượng đơn nghỉ phép và các chi tiết đơn
                dataList.clear()
                if (leaveRequestCount > 0) {
                    dataList.add("Tổng số đơn nghỉ phép tháng $selectedMonth: $leaveRequestCount đơn")
                    dataList.addAll(leaveRequestDetails) // Thêm các chi tiết đơn nghỉ phép vào danh sách
                } else {
                    dataList.add("Không có đơn nghỉ phép trong tháng $selectedMonth")
                }

                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to fetch leave data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


}




