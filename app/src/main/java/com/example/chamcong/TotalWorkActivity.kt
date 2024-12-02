package com.example.chamcong

import android.icu.util.Calendar
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

        //tongcong user

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
                val leaveDays = mutableListOf<String>()

                for (document in result) {
                    val startDate = document.getString("startDate") ?: continue
                    if (startDate.startsWith("$year-$selectedMonth")) {
                        leaveDays.add(startDate) // Thêm ngày nghỉ phép vào danh sách
                    }
                }

                if (leaveDays.isNotEmpty()) {
                    dataList.clear()
                    dataList.add("Tổng ngày nghỉ phép tháng $selectedMonth: ${leaveDays.size} ngày")
                    dataList.addAll(leaveDays) // Thêm danh sách các ngày nghỉ chi tiết
                } else {
                    dataList.clear()
                    dataList.add("Không có ngày nghỉ phép trong tháng $selectedMonth")
                }

                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to fetch leave data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
