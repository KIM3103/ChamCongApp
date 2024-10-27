package com.example.chamcong

data class AttendanceRecord(
    val checkInTime: String,
    val date: String,
    val email: String,
    val status: String
) {
    override fun toString(): String {
        return "$checkInTime - $date - $email - $status"
    }
}
