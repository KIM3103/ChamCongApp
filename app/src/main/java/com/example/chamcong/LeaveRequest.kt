package com.example.chamcong

data class LeaveRequest(
    var employeeId: String? = null,  // ID of the employee making the request
    var startDate: String? = null,    // Start date
    var endDate: String? = null,      // End date
    var reason: String? = null,       // Reason for leave
    var status: String? = "pending",  // Status of the request (default to pending)
    var submittedAt: String? = null    // Timestamp for when the request was submitted
)


