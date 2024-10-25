package com.example.chamcong

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.database.FirebaseDatabase

class LeaveRequestActivity : AppCompatActivity() {
    private lateinit var etStartDate: EditText
    private lateinit var etEndDate: EditText
    private lateinit var radioGroupReasons: RadioGroup
    private lateinit var etOtherReason: EditText
    private lateinit var btnSubmit: Button
    private val currentTimestamp = System.currentTimeMillis().toString() // Get current timestamp
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_application_for_leave)

        // Initialize Firestore and FirebaseAuth
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Initialize views from the layout
        etStartDate = findViewById(R.id.etStartDate)
        etEndDate = findViewById(R.id.etEndDate)
        radioGroupReasons = findViewById(R.id.radioGroupReasons)
        etOtherReason = findViewById(R.id.etOtherReason)
        btnSubmit = findViewById(R.id.btnSubmit)

        // Listen for when the 'Other' reason is selected
        radioGroupReasons.setOnCheckedChangeListener { _, checkedId ->
            etOtherReason.visibility = if (checkedId == R.id.rbOther) View.VISIBLE else View.GONE
        }

        // Handle when the user clicks the "Submit" button
        btnSubmit.setOnClickListener {
            // Fetch user data and then submit leave request
            fetchUserDataAndSubmitLeaveRequest()
        }
    }

    private fun fetchUserDataAndSubmitLeaveRequest() {
        // Get the current user's email
        val currentUserEmail = auth.currentUser?.email

        if (currentUserEmail != null) {
            firestore.collection("Users").document(currentUserEmail)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val userName = document.getString("name") ?: "Unknown"
                        val userPosition = document.getString("position") ?: "Unknown"

                        // Now submit the leave request with user data
                        submitLeaveRequest(userName, userPosition)
                    } else {
                        Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error fetching user data: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "User is not authenticated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun submitLeaveRequest(userName: String, userPosition: String) {
        val startDate = etStartDate.text.toString().trim()
        val endDate = etEndDate.text.toString().trim()
        val selectedReasonId = radioGroupReasons.checkedRadioButtonId
        val selectedReason = findViewById<RadioButton>(selectedReasonId)?.text.toString()
        val otherReason = etOtherReason.text.toString().trim()

        // Check the entered data
        if (TextUtils.isEmpty(startDate) || TextUtils.isEmpty(endDate)) {
            Toast.makeText(this, "Please enter start and end dates", Toast.LENGTH_SHORT).show()
            return
        }

        // Determine the reason for the leave request
        val reason = if (selectedReason == "Khác") otherReason else selectedReason
        if (TextUtils.isEmpty(reason)) {
            Toast.makeText(this, "Please select a reason", Toast.LENGTH_SHORT).show()
            return
        }

        // Create a LeaveRequest object with user data included
        val leaveRequest = LeaveRequest(
            employeeId =
            currentUserEmail, // Use email or a unique identifier
            userName = userName,            // Add user's name
            userPosition = userPosition,    // Add user's position
            startDate = startDate,
            endDate = endDate,
            reason = reason,
            submittedAt = currentTimestamp // Use the current timestamp
        )

        // Send to Firebase
        sendToFirebase(leaveRequest)
    }

    private fun sendToFirebase(leaveRequest: LeaveRequest) {
        val database = FirebaseDatabase.getInstance()
        val leaveRequestsRef = database.getReference("leaveRequests")

        // Create a unique ID for each request
        val requestId = leaveRequestsRef.push().key

        if (requestId != null) {
            leaveRequestsRef.child(requestId).setValue(leaveRequest)
                .addOnSuccessListener {
                    Toast.makeText(this, "Leave request submitted successfully", Toast.LENGTH_SHORT).show()
                    clearFields() // Clear input fields after submission
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error submitting data: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Optional: Function to clear input fields after submission
    private fun clearFields() {
        etStartDate.text.clear()
        etEndDate.text.clear()
        radioGroupReasons.clearCheck()
        etOtherReason.visibility = View.GONE
        etOtherReason.text.clear()
    }
}

