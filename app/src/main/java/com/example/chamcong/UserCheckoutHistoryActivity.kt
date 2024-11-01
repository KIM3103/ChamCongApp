package com.example.chamcong

import android.os.Bundle
import android.util.Log
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserCheckoutHistoryActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: CheckoutAdapter
    private val checkoutList = mutableListOf<AttendanceRecord>()  // List to hold checkout records
    private lateinit var listView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_checkout_history) // Use the corresponding layout

        listView = findViewById(R.id.listViewUserCheckoutHistory)

        // Set up adapter
        adapter = CheckoutAdapter(this, checkoutList)
        listView.adapter = adapter

        db = FirebaseFirestore.getInstance()
        loadUserCheckoutRecords()  // Load user checkout records
    }

    private fun loadUserCheckoutRecords() {
        // Get the current user's email
        val userEmail = FirebaseAuth.getInstance().currentUser?.email

        // Check if the user is authenticated
        if (userEmail != null) {
            val checkoutRef = db.collection("CheckOut").whereEqualTo("email", userEmail)

            // Get all documents matching the user's email
            checkoutRef.get().addOnCompleteListener { checkoutTask ->
                if (checkoutTask.isSuccessful) {
                    for (recordDocument in checkoutTask.result) {
                        val checkOutTime = recordDocument.getString("checkOutTime") ?: ""
                        val date = recordDocument.getString("date") ?: ""
                        val email = recordDocument.getString("email") ?: ""
                        val earlyLeaveStatus = recordDocument.getString("earlyLeaveStatus") ?: "" // Fixed variable name

                        // Add record to checkoutList with checkInTime as null
                        checkoutList.add(AttendanceRecord(checkInTime = null, checkOutTime = checkOutTime, date = date, email = email, status = earlyLeaveStatus))
                    }
                    adapter.notifyDataSetChanged()  // Update adapter
                } else {
                    Log.w("UserCheckoutHistoryActivity", "Error getting checkout records.", checkoutTask.exception)
                }
            }
        } else {
            Log.w("UserCheckoutHistoryActivity", "User is not authenticated.")
        }
    }
}
