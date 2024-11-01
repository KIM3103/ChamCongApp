package com.example.chamcong

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class CheckInActivity : AppCompatActivity() {
    private lateinit var tvTime: TextView
    private lateinit var btnCheckIn: Button
    private lateinit var btnCheckOut: Button
    private lateinit var tvResult: TextView
    private lateinit var tvPosition: TextView
    private lateinit var tvName: TextView
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timekeeping)

        // Ánh xạ các view
        tvTime = findViewById(R.id.tvTime)
        btnCheckIn = findViewById(R.id.btnCheckIn)
        btnCheckOut = findViewById(R.id.btnCheckOut)
        tvResult = findViewById(R.id.tvResult)
        tvPosition = findViewById(R.id.tvPosition)
        tvName = findViewById(R.id.tvName)

        // Khởi tạo Firebase Firestore và Authentication
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val user = auth.currentUser
        if (user != null) {
            val email = user.email
            if (email != null) {
                loadUserData(email)
            }
        }

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
            val currentTime = getCurrentTime()

            // Thời gian check-in cố định
            val fixedCheckInTime = "08:00"
            val lateMinutes = calculateMinutesDifference(currentTime, fixedCheckInTime)

            // Lưu thông tin check-in lên Firestore
            saveCheckInToFirestore(userEmail, currentTime, lateMinutes)

            // Hiển thị kết quả chấm công và trạng thái đi làm
            val (lateHours, lateMins) = convertMinutesToHoursAndMinutes(Math.abs(lateMinutes))
            tvResult.text = if (lateMinutes > 0) {
                "Chấm công lúc: $currentTime. Bạn đã đi làm đúng giờ."

            } else {
                "Chấm công lúc: $currentTime. Bạn đã đi làm muộn: $lateHours giờ $lateMins phút."
            }
        } else {
            tvResult.text = "Vui lòng đăng nhập"
        }
    }

    private fun handleCheckOut() {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val userEmail = currentUser.email ?: "Unknown"
            val currentTime = getCurrentTime()

            // Thời gian tan ca cố định
            val fixedCheckOutTime = "17:00"
            val earlyLeaveMinutes = calculateMinutesDifference(fixedCheckOutTime, currentTime)

            // Lưu thông tin tan ca lên Firestore
            saveCheckOutToFirestore(userEmail, currentTime, earlyLeaveMinutes)

            // Hiển thị kết quả tan ca
            val (earlyHours, earlyLeaveMins) = convertMinutesToHoursAndMinutes(Math.abs(earlyLeaveMinutes))
            tvResult.text = if (earlyLeaveMinutes > 0) {
                "Tan ca lúc: $currentTime. Bạn đã tan ca đúng giờ."

            } else {
                "Tan ca lúc: $currentTime. Bạn đã tan ca sớm: $earlyHours giờ $earlyLeaveMins phút."
            }
        } else {
            tvResult.text = "Vui lòng đăng nhập"
        }
    }

    private fun convertMinutesToHoursAndMinutes(totalMinutes: Int): Pair<Int, Int> {
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return Pair(hours, minutes)
    }

    private fun getCurrentTime(): String {
        val calendar = Calendar.getInstance()
        return timeFormat.format(calendar.time)
    }

    private fun calculateMinutesDifference(time1: String, time2: String): Int {
        return try {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val date1 = sdf.parse(time1)!!
            val date2 = sdf.parse(time2)!!

            val differenceInMillis = date2.time - date1.time
            (differenceInMillis / (1000 * 60)).toInt() // Chuyển đổi thành phút
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    private fun saveCheckInToFirestore(email: String, currentTime: String, lateMinutes: Int) {
        val currentDate = getCurrentDate()
        val lateHours = Math.abs(lateMinutes) / 60
        val lateMinutes = Math.abs(lateMinutes) % 60

        // Tạo một HashMap để lưu thông tin chấm công
        val checkInData = hashMapOf(
            "email" to email,
            "date" to currentDate,
            "checkInTime" to currentTime,
            "lateStatus" to if (lateMinutes > 0) "Đi trễ $lateHours giờ $lateMinutes phút" else "Đúng giờ"
        )

        // Lưu thông tin chấm công lên Firestore
        firestore.collection("CheckIn")
            .document("$currentDate-$email") // Sử dụng kết hợp giữa ngày và email làm ID tài liệu
            .set(checkInData)
            .addOnSuccessListener {
                Toast.makeText(this, "Lưu thông tin check-in thành công", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Lỗi khi lưu thông tin", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
    }

    private fun saveCheckOutToFirestore(email: String, currentTime: String, earlyLeaveMinutes: Int) {
        val currentDate = getCurrentDate()
        val earlyHours = Math.abs(earlyLeaveMinutes) / 60
        val earlyLeaveMinutes = Math.abs(earlyLeaveMinutes) % 60

        // Tạo một HashMap để lưu thông tin tan ca
        val checkOutData = hashMapOf(
            "email" to email,
            "date" to currentDate,
            "checkOutTime" to currentTime,
            "earlyLeaveStatus" to if (earlyLeaveMinutes > 0) "Tan ca sớm $earlyHours giờ $earlyLeaveMinutes phút" else "Đúng giờ"
        )

        // Lưu thông tin tan ca lên Firestore
        firestore.collection("CheckOut")
            .document("$currentDate-$email") // Sử dụng kết hợp giữa ngày và email làm ID tài liệu
            .set(checkOutData)
            .addOnSuccessListener {
                Toast.makeText(this, "Lưu thông tin tan ca thành công", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Lỗi khi lưu thông tin tan ca", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
    }

    private fun getCurrentDate(): String {
        return dateFormat.format(Calendar.getInstance().time)
    }

    private fun loadUserData(email: String) {
        firestore.collection("Users").document(email)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val position = document.getString("position")
                    val name = document.getString("name")
                    tvPosition.text = "Vị trí: $position"
                    tvName.text = "Họ và Tên: $name"
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Không thể tải dữ liệu người dùng", Toast.LENGTH_SHORT).show()
            }
    }
}
