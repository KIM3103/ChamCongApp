package com.example.chamcong

import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var btnChamCong: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        btnChamCong.setOnClickListener {
            chamCong()
        }
    }

    private fun chamCong() {
        val user = auth.currentUser
        val email = user?.email

        // Lấy thời gian hiện tại
        val currentTime = getCurrentTime()

        if (email != null) {
            // Tạo dữ liệu chấm công
            val attendance = hashMapOf(
                "email" to email,
                "time" to currentTime)
            db.collection("attendance")
                .add(attendance)
                .addOnSuccessListener {
                    Toast.makeText(this, "Chấm công thành công!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Chấm công thất bại!", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Không thể lấy email người dùng!", Toast.LENGTH_SHORT).show()
        }
    }

    // Hàm lấy thời gian hiện tại
    private fun getCurrentTime(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }
}