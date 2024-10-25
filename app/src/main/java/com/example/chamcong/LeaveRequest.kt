package com.example.chamcong

data class LeaveRequest(
    var employeeId: String? = null,     // ID or email of the employee making the request
    var userName: String? = null,       // Name of the employee
    var userPosition: String? = null,   // Position of the employee
    var startDate: String? = null,      // Start date of leave
    var endDate: String? = null,        // End date of leave
    var reason: String? = null,         // Reason for leave
    var status: String? = "pending",    // Status of the request (default to pending)

    var submittedAt: String? = null    // Timestamp for when the request was submitted

)



