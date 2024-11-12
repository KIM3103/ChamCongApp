package com.example.chamcong

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {
    private lateinit var inputEmail: EditText
    private lateinit var inputPassword: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var btnLogin: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get Firebase auth and Firestore instances
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        auth.signOut()

        // Check if user is already logged in
        if (auth.currentUser != null) {
            checkUserRoleAndRedirect(auth.currentUser!!.email!!)
        }

        // Set layout view for LoginActivity
        setContentView(R.layout.activity_login)

        inputEmail = findViewById(R.id.email)
        inputPassword = findViewById(R.id.password)
        progressBar = findViewById(R.id.progressBar)
        btnLogin = findViewById(R.id.btn_login)

        btnLogin.setOnClickListener {
            val email = inputEmail.text.toString()
            val password = inputPassword.text.toString()

            // Check input fields
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(applicationContext, "Enter email address!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(applicationContext, "Enter password!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            progressBar.visibility = View.VISIBLE

            // Authenticate user
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this@LoginActivity) { task ->
                    progressBar.visibility = View.GONE
                    if (!task.isSuccessful) {
                        // Login failed
                        if (password.length < 6) {
                            inputPassword.error = getString(R.string.minimum_password)
                        } else {
                            Toast.makeText(this@LoginActivity, getString(R.string.auth_failed), Toast.LENGTH_LONG).show()
                        }
                    } else {
                        // Successful login, check user role
                        checkUserRoleAndRedirect(email)
                    }
                }
        }
    }

    private fun checkUserRoleAndRedirect(email: String) {
        // Get user document by email
        db.collection("Users").document(email).get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val role = document.getString("role")
                if (role == "user") {
                    // Redirect to CheckInActivity if role is user
                    val intent = Intent(this@LoginActivity, CheckInActivity::class.java)
                    startActivity(intent)
                } else if (role == "admin") {
                    // Redirect to AttendanceListActivity if role is admin
                    val intent = Intent(this@LoginActivity, AttendanceListActivity::class.java)
                    startActivity(intent)
                }
                finish()
            } else {
                Toast.makeText(this, "User role not found.", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to fetch user data.", Toast.LENGTH_SHORT).show()
        }
    }
}