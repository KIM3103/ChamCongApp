package com.example.chamcong

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import android.text.TextUtils
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import org.checkerframework.checker.units.qual.C

class LoginActivity : AppCompatActivity() {
    private lateinit var inputEmail: EditText
    private lateinit var inputPassword: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var btnLogin: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get Firebase auth instance
        auth = FirebaseAuth.getInstance()
        auth.signOut()
        // Nếu người dùng đã đăng nhập, chuyển tiếp sang MainActivity
        if (auth.currentUser != null) {
            startActivity(Intent(this@LoginActivity, CheckInActivity::class.java))
            finish()
        }

        // Set layout view cho LoginActivity
        setContentView(R.layout.activity_login)


        inputEmail = findViewById(R.id.email)
        inputPassword = findViewById(R.id.password)
        progressBar = findViewById(R.id.progressBar)

        btnLogin = findViewById(R.id.btn_login)


        // Firebase auth instance
        auth = FirebaseAuth.getInstance()
        btnLogin.setOnClickListener {
            val email = inputEmail.text.toString()
            val password = inputPassword.text.toString()

            // Kiểm tra nhập liệu
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(applicationContext, "Enter email address!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(applicationContext, "Enter password!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            progressBar.visibility = View.VISIBLE

            // Xác thực người dùng
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this@LoginActivity) { task ->
                    progressBar.visibility = View.GONE
                    if (!task.isSuccessful) {
                        // Nếu đăng nhập không thành công
                        if (password.length < 6) {
                            inputPassword.error = getString(R.string.minimum_password)
                        } else {
                            Toast.makeText(this@LoginActivity, getString(R.string.auth_failed), Toast.LENGTH_LONG).show()
                        }
                    } else {
                        val intent = Intent(this@LoginActivity, CheckInActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
      }
    }
}