package com.example.chamcong

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

class WorkingDaysActivity : AppCompatActivity() {

    private lateinit var pieChart: PieChart
    private lateinit var backButton: Button
    private lateinit var monthSpinner: Spinner
    private lateinit var email: String
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_working_days)

        pieChart = findViewById(R.id.pieChart)
        backButton = findViewById(R.id.btnBack)
        monthSpinner = findViewById(R.id.spinnerMonth)

        // Nhận thông tin nhân viên từ Intent
        email = intent.getStringExtra("EMPLOYEE_EMAIL") ?: "Unknown"

        // Cấu hình Spinner
        setupMonthSpinner()

        // Lấy dữ liệu chấm công tháng hiện tại
        val currentMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Calendar.getInstance().time)
        fetchEmployeeCheckInData(currentMonth)

        // Button quay lại
        backButton.setOnClickListener {
            onBackPressed()
        }
    }

    private fun setupMonthSpinner() {
        val months = (1..12).map { "Tháng $it" }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, months)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        monthSpinner.adapter = adapter

        // Chọn tháng hiện tại mặc định
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
        monthSpinner.setSelection(currentMonth)

        monthSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedMonth = position + 1
                val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                val formattedMonth = String.format("%04d-%02d", currentYear, selectedMonth)
                fetchEmployeeCheckInData(formattedMonth)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Không làm gì nếu không có gì được chọn
            }
        }
    }

    private fun fetchEmployeeCheckInData(selectedMonth: String) {
        // Tính tổng số ngày trong tháng
        val calendar = Calendar.getInstance()
        val daysInMonth = calendar.apply {
            time = SimpleDateFormat("yyyy-MM", Locale.getDefault()).parse(selectedMonth)!!
        }.getActualMaximum(Calendar.DAY_OF_MONTH)

        // Cấu trúc ngày bắt đầu và kết thúc trong tháng
        val startDate = "$selectedMonth-01" // Ngày đầu tháng
        val endDate = "$selectedMonth-${String.format("%02d", daysInMonth)}" // Ngày cuối tháng

        Log.d("DEBUG", "Start Date: $startDate, End Date: $endDate")

        // Truy vấn dữ liệu từ Firestore với kiểu dữ liệu String
        firestore.collection("CheckIn")
            .whereEqualTo("email", email)
            .whereGreaterThanOrEqualTo("date", startDate) // So sánh theo định dạng chuỗi
            .whereLessThanOrEqualTo("date", endDate) // So sánh theo định dạng chuỗi
            .get()
            .addOnSuccessListener { result ->
                Log.d("DEBUG", "Query result: ${result.size()} documents found.")
                if (result.isEmpty) {
                    Log.d("DEBUG", "Không có dữ liệu cho tháng này.")
                    updatePieChart(0, daysInMonth)
                } else {
                    // Kiểm tra xem có tài liệu nào không
                    for (document in result) {
                        Log.d("DEBUG", "Document: ${document.data}")
                    }
                    val daysWorked = result.documents.count { document ->
                        val checkInTime = document.getString("checkInTime") // Kiểm tra thời gian check-in
                        Log.d("DEBUG", "checkInTime: $checkInTime")
                        checkInTime != null && checkInTime.isNotEmpty() // Kiểm tra checkInTime hợp lệ
                    }
                    val daysNotWorked = daysInMonth - daysWorked
                    updatePieChart(daysWorked, daysNotWorked)
                }
            }
            .addOnFailureListener { e ->
                Log.e("DEBUG", "Lỗi khi lấy dữ liệu: ${e.message}")
                Toast.makeText(this, "Lỗi khi lấy dữ liệu chấm công: ${e.message}", Toast.LENGTH_SHORT).show()
                updatePieChart(0, daysInMonth)
            }
    }



    private fun updatePieChart(daysWorked: Int, daysNotWorked: Int) {
        try {
            val totalDays = daysWorked + daysNotWorked
            val workedPercentage = if (totalDays > 0) (daysWorked.toFloat() / totalDays * 100).toInt() else 0
            val notWorkedPercentage = 100 - workedPercentage

            val entries = arrayListOf<PieEntry>()
            entries.add(PieEntry(workedPercentage.toFloat(), "Làm việc"))
            entries.add(PieEntry(notWorkedPercentage.toFloat(), "Không làm việc"))

            val dataSet = PieDataSet(entries, "")
            dataSet.colors = listOf(Color.GREEN, Color.RED)
            val data = PieData(dataSet)

            pieChart.data = data
            pieChart.invalidate()
        } catch (e: Exception) {
            Log.e("DEBUG", "Error updating PieChart: ${e.message}")
            Toast.makeText(this, "Lỗi khi hiển thị biểu đồ.", Toast.LENGTH_SHORT).show()
        }
    }
}
