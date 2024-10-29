package com.example.chamcong

data class AttendanceRecord(
    val checkInTime: String? = null,
    val checkOutTime: String? = null,
    val date: String,
    val email: String,
    val status: String
) {
    override fun toString(): String {
        // Hiển thị thời gian check-in hoặc check-out tùy thuộc vào giá trị nào tồn tại
        val timeInfo = when {
            checkInTime != null -> "Check-in: $checkInTime"
            checkOutTime != null -> "Check-out: $checkOutTime"
            else -> "No time recorded"
        }
        return "$timeInfo - Date: $date - Email: $email - Status: $status"
    }
}

