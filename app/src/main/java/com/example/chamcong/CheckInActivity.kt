package com.example.chamcong
import android.widget.ArrayAdapter

import android.os.Bundle
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class CheckInActivity : AppCompatActivity() {
    private lateinit var tvTime: TextView
    private lateinit var spinnerShift: Spinner
    private lateinit var btnCheckIn: Button
    private lateinit var btnCheckOut: Button
    private lateinit var tvResult: TextView // TextView để hiển thị kết quả

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timekeeping)

        // Ánh xạ các view
        tvTime = findViewById(R.id.tvTime)
        spinnerShift = findViewById(R.id.spinnerShift)
        btnCheckIn = findViewById(R.id.btnCheckIn)
        btnCheckOut = findViewById(R.id.btnCheckOut) // Khởi tạo nút check-out
        tvResult = findViewById(R.id.tvResult) // Khởi tạo TextView cho kết quả

        // Khởi tạo Firebase Firestore và Authentication
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Hiển thị thời gian hiện tại
        updateCurrentTime()

        // Sự kiện khi nhấn nút Chấm công
        btnCheckIn.setOnClickListener {
            handleCheckIn()
        }

        // Sự kiện khi nhấn nút Tan ca
        btnCheckOut.setOnClickListener {
            handleCheckOut()
        }
    }

    private fun updateCurrentTime() {
        val calendar = Calendar.getInstance()
        val currentTime = timeFormat.format(calendar.time)
        tvTime.text = "Giờ: $currentTime"
    }

    private fun handleCheckIn() {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val userEmail = currentUser.email ?: "Unknown"
            val calendar = Calendar.getInstance()
            val currentTime = timeFormat.format(calendar.time)
            val shift = spinnerShift.selectedItem.toString()

            // Xác định thời gian bắt đầu và kết thúc ca làm việc
            val (shiftStartTime, shiftEndTime) = when (shift) {
                "Sáng" -> Pair("08:00", "17:00")
                "Chiều" -> Pair("13:00", "17:00")
                else -> Pair("08:00", "17:00") // Mặc định
            }

            // So sánh giờ
            val isLate = compareTime(currentTime, shiftStartTime)
            val earlyLeave = compareTime(shiftEndTime, currentTime)

            // Tính toán thời gian đi trễ và ra sớm
            val lateMinutes = if (isLate) calculateMinutesDifference(currentTime, shiftStartTime) else 0
            val earlyMinutes = if (earlyLeave) calculateMinutesDifference(shiftEndTime, currentTime) else 0

            // Lưu thông tin chấm công lên Firestore với email người dùng
            saveAttendanceToFirestore(userEmail, currentTime, shift, lateMinutes, earlyMinutes)

            // Hiển thị kết quả lên màn hình
            tvResult.text = when {
                lateMinutes > 0 -> "Đi trễ $lateMinutes phút"
                earlyMinutes > 0 -> "Ra sớm $earlyMinutes phút"
                else -> "Đến đúng giờ"
            }
        } else {
            tvResult.text = "Vui lòng đăng nhập"
        }
    }

    private fun handleCheckOut() {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val userEmail = currentUser.email ?: "Unknown"
            val calendar = Calendar.getInstance()
            val currentTime = timeFormat.format(calendar.time)

            // Thời gian tan ca cố định
            val fixedCheckOutTime = "17:00"

            // So sánh giờ tan ca với giờ cố định
            val earlyLeave = compareTime(currentTime, fixedCheckOutTime)

            // Lưu thông tin tan ca lên Firestore
            saveCheckOutToFirestore(userEmail, currentTime, earlyLeave)

            // Hiển thị thông báo cho người dùng
            if (earlyLeave) {
                tvResult.text = "Bạn đã tan ca sớm lúc: $currentTime. Giờ tan ca là 17:00."
            } else {
                tvResult.text = "Bạn đã tan ca lúc: $currentTime."
            }
        } else {
            tvResult.text = "Vui lòng đăng nhập"
        }
    }

    private fun compareTime(currentTime: String, targetTime: String): Boolean {
        return try {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val currentCalendar = Calendar.getInstance()
            val targetCalendar = Calendar.getInstance()

            currentCalendar.time = sdf.parse(currentTime)!!
            targetCalendar.time = sdf.parse(targetTime)!!

            // Nếu thời gian hiện tại lớn hơn thời gian mục tiêu thì đi trễ
            currentCalendar.after(targetCalendar)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun calculateMinutesDifference(time1: String, time2: String): Int {
        return try {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val calendar1 = Calendar.getInstance()
            val calendar2 = Calendar.getInstance()

            calendar1.time = sdf.parse(time1)!!
            calendar2.time = sdf.parse(time2)!!

            val differenceInMillis = calendar1.timeInMillis - calendar2.timeInMillis
            (differenceInMillis / (1000 * 60)).toInt() // Chuyển đổi thành phút
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    private fun saveAttendanceToFirestore(email: String, currentTime: String, shift: String, lateMinutes: Int, earlyMinutes: Int) {
        // Lấy ngày hiện tại
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = dateFormat.format(Calendar.getInstance().time)

        // Tạo một HashMap để lưu thông tin chấm công
        val attendanceData = hashMapOf(
            "email" to email,
            "date" to currentDate,
            "time" to currentTime,
            "shift" to shift,
            "lateMinutes" to lateMinutes,
            "earlyMinutes" to earlyMinutes,
            "status" to when {
                lateMinutes > 0 -> "Đi trễ $lateMinutes phút"
                earlyMinutes > 0 -> "Ra sớm $earlyMinutes phút"
                else -> "Đến đúng giờ"
            }
        )

        // Lưu thông tin chấm công lên Firestore
        firestore.collection("Attendance")
            .document(currentDate)
            .collection("Records")
            .add(attendanceData)
            .addOnSuccessListener {
                // Lưu thành công
                // Bạn có thể thông báo cho người dùng tại đây
            }
            .addOnFailureListener { e ->
                // Xử lý khi lưu thất bại
                e.printStackTrace()
            }
    }

    private fun saveCheckOutToFirestore(email: String, currentTime: String, earlyLeave: Boolean) {
        // Tương tự như saveAttendanceToFirestore, nhưng lưu thông tin tan ca
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = dateFormat.format(Calendar.getInstance().time)

        val checkOutData = hashMapOf(
            "email" to email,
            "date" to currentDate,
            "checkOutTime" to currentTime,
            "status" to if (earlyLeave) "Tan ca sớm" else "Tan ca đúng giờ"
        )

        firestore.collection("Attendance")
            .document(currentDate)
            .collection("CheckOutRecords")
            .add(checkOutData)
            .addOnSuccessListener {
                // Lưu thành công
                // Bạn có thể thông báo cho người dùng tại đây
            }
            .addOnFailureListener { e ->
                // Xử lý khi lưu thất bại
                e.printStackTrace()
            }
    }
}