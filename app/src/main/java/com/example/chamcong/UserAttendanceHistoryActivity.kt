package com.example.chamcong

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserAttendanceHistoryActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: ArrayAdapter<AttendanceRecord>
    private val historyList = mutableListOf<AttendanceRecord>()
    private lateinit var listView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_attendance_history)

        listView = findViewById(R.id.listViewUserAttendance)

        // Use a simple ArrayAdapter for displaying AttendanceRecord items
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, historyList)
        listView.adapter = adapter

        db = FirebaseFirestore.getInstance()
        loadCheckInRecords() // Load check-in records
    }

    private fun loadCheckInRecords() {
        // Get the current user's email
        val userEmail = FirebaseAuth.getInstance().currentUser?.email

        // Check if the user is authenticated
        if (userEmail != null) {
            // Fetch records from Firestore for the specific user
            val attendanceRef = db.collection("CheckIn").whereEqualTo("email", userEmail)

            // Get all documents matching the query
            attendanceRef.get().addOnCompleteListener { attendanceTask ->
                if (attendanceTask.isSuccessful) {
                    for (record in attendanceTask.result) {
                        val checkInTime = record.getString("checkInTime") ?: ""
                        val date = record.getString("date") ?: ""
                        val email = record.getString("email") ?: ""
                        val status = record.getString("lateStatus") ?: ""

                        // Add the record to the history list
                        historyList.add(AttendanceRecord(checkInTime, date, email, status))
                    }
                    adapter.notifyDataSetChanged() // Notify the adapter to refresh the ListView
                } else {
                    Log.w(
                        "UserAttendanceHistoryActivity",
                        "Error getting attendance data.",
                        attendanceTask.exception
                    )
                }
            }
        } else {
            Log.w("UserAttendanceHistoryActivity", "User is not authenticated.")
        }
    }
}
