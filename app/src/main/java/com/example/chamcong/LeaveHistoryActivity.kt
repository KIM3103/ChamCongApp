package com.example.chamcong

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CalendarView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot

class LeaveHistoryActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var calendarView: CalendarView
    private lateinit var detailLayout: LinearLayout
    private lateinit var btnLeaveHistory: Button
    private lateinit var btnAttendanceHistory: Button

    private val dateWithData: MutableSet<String> = mutableSetOf() // Ngày có dữ liệu
    private var selectedDate: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leave_history)

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
                R.id.TongCong -> {
                    startActivity(Intent(this, TotalWorkActivity::class.java))
                    true
                }
                R.id.LichSu -> {
                    startActivity(Intent(this, LeaveHistoryActivity::class.java))
                    true
                }
                R.id.TaiKhoan -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                else -> false
            }
        }

        // Firebase instances
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // UI elements
        calendarView = findViewById(R.id.calendarView)
        detailLayout = findViewById(R.id.detailLayout)
        btnLeaveHistory = findViewById(R.id.btnLeaveHistory)
        btnAttendanceHistory = findViewById(R.id.btnAttendanceHistory)

        // Tải các ngày có dữ liệu
        fetchDatesWithData()

        // Xử lý khi chọn ngày từ CalendarView
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year)
            detailLayout.removeAllViews()

            if (dateWithData.contains(selectedDate)) {
                fetchHistoryForSelectedDate(selectedDate)
            } else {
                Toast.makeText(this, "Không có dữ liệu cho ngày $selectedDate", Toast.LENGTH_SHORT).show()
            }
        }

        // Hiển thị toàn bộ lịch sử nghỉ phép
        btnLeaveHistory.setOnClickListener {
            detailLayout.removeAllViews()
            fetchLeaveHistory()
        }

        // Hiển thị toàn bộ lịch sử chấm công
        btnAttendanceHistory.setOnClickListener {
            detailLayout.removeAllViews()
            fetchAttendanceHistory()
        }
    }

    private fun fetchDatesWithData() {
        val currentUserEmail = auth.currentUser?.email
        if (currentUserEmail != null) {
            // Tải dữ liệu nghỉ phép
            firestore.collection("leaveRequests")
                .whereEqualTo("employeeId", currentUserEmail)
                .get()
                .addOnSuccessListener { leaveSnapshot ->
                    for (document in leaveSnapshot) {
                        val date = document.getString("startDate") ?: continue
                        dateWithData.add(date)
                    }

                    // Tải dữ liệu chấm công
                    firestore.collection("CheckIn")
                        .whereEqualTo("email", currentUserEmail)
                        .get()
                        .addOnSuccessListener { attendanceSnapshot ->
                            for (document in attendanceSnapshot) {
                                val date = document.getString("date") ?: continue
                                dateWithData.add(date)
                            }
                            Toast.makeText(this, "Tải dữ liệu hoàn tất", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(this, "Lỗi tải chấm công: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Lỗi tải nghỉ phép: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Người dùng chưa đăng nhập", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchHistoryForSelectedDate(date: String) {
        val currentUserEmail = auth.currentUser?.email
        if (currentUserEmail != null) {
            var hasData = false

            // Lịch sử nghỉ phép cho ngày đã chọn
            firestore.collection("leaveRequests")
                .whereEqualTo("employeeId", currentUserEmail)
                .whereEqualTo("startDate", date)
                .get()
                .addOnSuccessListener { leaveSnapshot ->
                    if (!leaveSnapshot.isEmpty) {
                        hasData = true
                        for (document in leaveSnapshot) {
                            displayDetails(document, "Nghỉ Phép")
                        }
                    }

                    // Lịch sử chấm công cho ngày đã chọn
                    firestore.collection("CheckIn")
                        .whereEqualTo("email", currentUserEmail)
                        .whereEqualTo("date", date)
                        .get()
                        .addOnSuccessListener { attendanceSnapshot ->
                            if (!attendanceSnapshot.isEmpty) {
                                hasData = true
                                for (document in attendanceSnapshot) {
                                    displayDetails(document, "Chấm Công")
                                }
                            }
                            if (!hasData) {
                                Toast.makeText(this, "Không có lịch sử ngày $date", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(this, "Lỗi khi tải lịch sử chấm công: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Lỗi khi tải lịch sử nghỉ phép: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun fetchLeaveHistory() {
        val currentUserEmail = auth.currentUser?.email
        if (currentUserEmail != null) {
            firestore.collection("leaveRequests")
                .whereEqualTo("employeeId", currentUserEmail)
                .get()
                .addOnSuccessListener { leaveSnapshot ->
                    if (!leaveSnapshot.isEmpty) {
                        for (document in leaveSnapshot) {
                            displayDetails(document, "Nghỉ Phép")
                        }
                    } else {
                        Toast.makeText(this, "Không có lịch sử nghỉ phép", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Lỗi khi tải lịch sử nghỉ phép: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun fetchAttendanceHistory() {
        val currentUserEmail = auth.currentUser?.email
        if (currentUserEmail != null) {
            firestore.collection("CheckIn")
                .whereEqualTo("email", currentUserEmail)
                .get()
                .addOnSuccessListener { attendanceSnapshot ->
                    if (!attendanceSnapshot.isEmpty) {
                        for (document in attendanceSnapshot) {
                            displayDetails(document, "Chấm Công")
                        }
                    } else {
                        Toast.makeText(this, "Không có lịch sử chấm công", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Lỗi khi tải lịch sử chấm công: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun displayDetails(document: QueryDocumentSnapshot, type: String) {
        val details = StringBuilder().apply {
            append("Loại: $type\n")
            document.data.forEach { (key, value) ->
                append("$key: $value\n")
            }
        }.toString()

        val detailTextView = TextView(this).apply {
            text = details
            textSize = 16f
            setPadding(16, 16, 16, 16)
        }

        detailLayout.addView(detailTextView)
    }
}








