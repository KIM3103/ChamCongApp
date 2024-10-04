package com.example.chamcong

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {
    private lateinit var imgProfilePicture: ImageView
    private lateinit var tvEmail: TextView
    private lateinit var tvPhone: TextView
    private lateinit var tvCCCD: TextView
    private lateinit var tvPosition: TextView
    private lateinit var tvGender: TextView
    private lateinit var database: DatabaseReference // Khai báo biến database

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imgProfilePicture = findViewById(R.id.imgProfilePicture)
        tvEmail = findViewById(R.id.tvEmail)
        tvPhone = findViewById(R.id.tvPhone)
        tvCCCD = findViewById(R.id.tvCCCD)
        tvPosition = findViewById(R.id.tvPosition)
        tvGender = findViewById(R.id.tvGender)

        // Khởi tạo Firebase Database
        database = FirebaseDatabase.getInstance().getReference("users") // Thay đổi đường dẫn theo cơ sở dữ liệu của bạn

        // Giả sử bạn có ID người dùng để lấy dữ liệu
        val userId = "user_id" // Thay đổi thành ID thực tế
        database.child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val email = dataSnapshot.child("email").getValue(String::class.java)
                    val phone = dataSnapshot.child("phone").getValue(String::class.java)
                    val cccd = dataSnapshot.child("cccd").getValue(String::class.java)
                    val position = dataSnapshot.child("position").getValue(String::class.java)
                    val gender = dataSnapshot.child("gender").getValue(String::class.java)
                    val imageUrl = dataSnapshot.child("image").getValue(String::class.java) // Trường hình ảnh

                    // Cập nhật UI
                    tvEmail.text = "Gmail: $email"
                    tvPhone.text = "Số điện thoại: $phone"
                    tvCCCD.text = "CCCD/CMND: $cccd"
                    tvPosition.text = "Vị trí: $position"
                    tvGender.text = "Giới tính: $gender"

                    Glide.with(this@MainActivity)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_profile_placeholder) // Hình ảnh mặc định khi đang tải
                        .error(R.drawable.ic_profile_error) // Hình ảnh hiển thị khi có lỗi
                        .into(imgProfilePicture)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Xử lý lỗi
            }
        })
    }
}
