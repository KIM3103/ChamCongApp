package com.example.chamcong

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class AttendanceListActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: ArrayAdapter<AttendanceRecord>
    private val historyList = mutableListOf<AttendanceRecord>()
    private lateinit var listView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.listcheckin)  // Đảm bảo sử dụng layout listcheckin

        listView = findViewById(R.id.listView)

        // Thiết lập adapter
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, historyList)
        listView.adapter = adapter

        db = FirebaseFirestore.getInstance()
        loadCheckInRecords()
       // Gọi phương thức để tải tất cả bản ghi check-in
    }

    private fun loadCheckInRecords() {
        val attendanceRef = db.collection("CheckIn")  // Thay đổi collection thành CheckIn

        // Truy xuất tất cả các tài liệu trong collection CheckIn
        attendanceRef.get().addOnCompleteListener { attendanceTask ->
            if (attendanceTask.isSuccessful) {
                for (record in attendanceTask.result) {
                    val checkInTime = record.getString("checkInTime") ?: ""
                    val date = record.getString("date") ?: ""  // Lấy ngày từ document
                    val email = record.getString("email") ?: ""
                    val status = record.getString("lateStatus") ?: "" // Sử dụng "lateStatus" để lấy trạng thái

                    // Thêm bản ghi vào danh sách historyList
                    historyList.add(AttendanceRecord(checkInTime, date, email, status))
                }
                adapter.notifyDataSetChanged()  // Cập nhật adapter
            } else {
                Log.w("AttendanceListActivity", "Error getting attendance data.", attendanceTask.exception)
            }
        }
    }

}
