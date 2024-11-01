package com.example.chamcong

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot

class LeaveHistoryActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var leaveHistoryLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leave_history)

        // Initialize Firebase instances
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Initialize the layout to display leave requests
        leaveHistoryLayout = findViewById(R.id.leaveHistoryLayout)

        // Fetch leave history
        fetchLeaveHistory()
    }

    private fun fetchLeaveHistory() {
        val currentUserEmail = auth.currentUser?.email

        if (currentUserEmail != null) {
            firestore.collection("leaveRequests")
                .whereEqualTo("employeeId", currentUserEmail)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        // Loop through each document and display the details
                        for (document in querySnapshot) {
                            displayLeaveRequestDetails(document)
                        }
                    } else {
                        Toast.makeText(this, "Không có lịch sử nghỉ phép", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Lỗi khi tải lịch sử nghỉ phép: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Người dùng chưa đăng nhập", Toast.LENGTH_SHORT).show()
        }
    }

    private fun displayLeaveRequestDetails(document: QueryDocumentSnapshot) {
        // Get data from the Firestore document
        val startDate = document.getString("startDate") ?: "Không có"
        val endDate = document.getString("endDate") ?: "Không có"
        val reason = document.getString("reason") ?: "Không có"
        val status = document.getString("status") ?: "Không có"
        val submittedAt = document.getString("submittedAt") ?: "Không có"
        val userName = document.getString("userName") ?: "Không có"
        val userPosition = document.getString("userPosition") ?: "Không có"

        // Create a TextView and set the formatted text using resource string
        val leaveDetailsTextView = TextView(this).apply {
            text = getString(
                R.string.leave_request_details,
                userName,
                userPosition,
                startDate,
                endDate,
                reason,
                status,
                submittedAt
            )
            textSize = 16f
            setPadding(16, 16, 16, 16)
        }

        // Add TextView to the layout
        leaveHistoryLayout.addView(leaveDetailsTextView)
    }
}