package com.example.chamcong

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    private lateinit var imgProfilePicture: ImageView
    private lateinit var tvEmail: TextView
    private lateinit var tvPhone: TextView
    private lateinit var tvCCCD: TextView
    private lateinit var tvPosition: TextView
    private lateinit var tvGender: TextView
    private lateinit var btnLogout: Button // Nút đăng xuất
    private lateinit var auth: FirebaseAuth // FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Khởi tạo FirebaseAuth và Firestore
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Liên kết các thành phần giao diện
        imgProfilePicture = findViewById(R.id.imgProfilePicture)
        tvEmail = findViewById(R.id.tvEmail)
        tvPhone = findViewById(R.id.tvPhone)
        tvCCCD = findViewById(R.id.tvCCCD)
        tvPosition = findViewById(R.id.tvPosition)
        tvGender = findViewById(R.id.tvGender)
        btnLogout = findViewById(R.id.btn_logout) // Liên kết nút Đăng xuất

        // Lấy thông tin email của người dùng đã đăng nhập
        val user = auth.currentUser
        if (user != null) {
            val email = user.email
            // Kiểm tra nếu email không null, gọi hàm loadUserData
            if (email != null) {
                loadUserData(email)
            }
        }

        // Thiết lập sự kiện cho nút Đăng xuất
        btnLogout.setOnClickListener {
            logout() // Gọi hàm logout khi nhấn nút Đăng xuất
        }
    }

    // Hàm đăng xuất người dùng
    private fun logout() {
        // Đăng xuất người dùng khỏi Firebase
        auth.signOut()

        // Chuyển hướng về màn hình đăng nhập (LoginActivity)
        val intent = Intent(this@MainActivity, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Xóa ngăn xếp Activity
        startActivity(intent)
        finish() // Đóng MainActivity hiện tại
    }

    // Hàm tải thông tin người dùng từ Firestore
    private fun loadUserData(email: String) {
        // Truy vấn Firestore trong collection "Users" với email là document ID
        firestore.collection("Users").document(email)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val phone = document.getString("phone")
                    val cccd = document.getString("cccd")
                    val position = document.getString("position")
                    val gender = document.getString("gender")
                    val imageUrl = document.getString("picture")

                    // Cập nhật giao diện với dữ liệu từ Firestore
                    tvEmail.text = "G-mail: $email"
                    tvPhone.text = "Số điện thoại: $phone"
                    tvCCCD.text = "CCCD/CMND: $cccd"
                    tvPosition.text = "Vị trí: $position"
                    tvGender.text = "Giới tính: $gender"

                    // Tải hình ảnh đại diện bằng Glide
                    Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_profile_placeholder) // Ảnh tạm khi đang tải
                        .error(R.drawable.ic_profile_error) // Ảnh lỗi khi không tải được
                        .into(imgProfilePicture)
                }
            }
            .addOnFailureListener { exception ->
                // Xử lý lỗi nếu truy vấn không thành công
                Toast.makeText(this, "Không thể tải dữ liệu người dùng", Toast.LENGTH_SHORT).show()
            }
    }
}
