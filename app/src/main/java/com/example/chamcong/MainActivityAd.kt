package com.example.chamcong
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivityAd : AppCompatActivity() {
    private lateinit var imgProfilePicture: ImageView
    private lateinit var tvEmail: TextView
    private lateinit var tvName: TextView
    private lateinit var tvPhone: TextView
    private lateinit var tvCCCD: TextView
    private lateinit var tvPosition: TextView
    private lateinit var tvGender: TextView
    private lateinit var btnLogout: Button
    private lateinit var btnChangePassword: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_ad)

        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
//
//                R.id.DSNhanVien -> {
//                    startActivity(Intent(this, AttendanceListActivity::class.java))
//                    true
//                }

                R.id.DSChamCong -> {
                    startActivity(Intent(this, AttendanceListActivity::class.java))
                    true
                }
                R.id.DSTanCa -> {
                    startActivity(Intent(this, CheckoutListActivity::class.java))
                    true
                }
                else -> false
            }
        }

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
        tvName = findViewById(R.id.tvName)
        btnLogout = findViewById(R.id.btn_logout)
        btnChangePassword = findViewById(R.id.btn_change_password)

        // Lấy thông tin email của người dùng đã đăng nhập
        val user = auth.currentUser
        if (user != null) {
            val email = user.email
            if (email != null) {
                loadUserData(email)
            }
        }

        // Thiết lập sự kiện cho nút Đăng xuất
        btnLogout.setOnClickListener {
            logout()
        }
    }

    // Hàm đăng xuất người dùng
    private fun logout() {
        auth.signOut()
        val intent = Intent(this@MainActivityAd, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    // Hàm tải thông tin người dùng từ Firestore
    private fun loadUserData(email: String) {
        firestore.collection("Users").document(email)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val phone = document.getString("phone")
                    val cccd = document.getString("cccd")
                    val position = document.getString("position")
                    val gender = document.getString("gender")
                    val imageUrl = document.getString("picture")
                    val name = document.getString("name")

                    // Cập nhật giao diện với dữ liệu từ Firestore
                    tvEmail.text = "$email"
                    tvPhone.text = "$phone"
                    tvCCCD.text = "$cccd"
                    tvPosition.text = "$position"
                    tvGender.text = "$gender"
                    tvName.text = "$name"

                    // Tải hình ảnh đại diện bằng Glide
                    Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_profile_placeholder) // Ảnh tạm khi đang tải
                        .error(R.drawable.ic_profile_error) // Ảnh lỗi khi không tải được
                        .into(imgProfilePicture)
                } else {
                    Toast.makeText(this, "Không tìm thấy dữ liệu người dùng", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Không thể tải dữ liệu người dùng", Toast.LENGTH_SHORT).show()
            }
    }

}