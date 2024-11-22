package com.example.chamcong

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.squareup.picasso.Picasso

class TotalWorkActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_total_work)

        // Get employee data from Intent
        val employeeName = intent.getStringExtra("EMPLOYEE_NAME")
        val employeeEmail = intent.getStringExtra("EMPLOYEE_EMAIL")
        val employeePosition = intent.getStringExtra("EMPLOYEE_POSITION")
        val employeePicture = intent.getStringExtra("EMPLOYEE_PICTURE")

        // Set data to views
        findViewById<TextView>(R.id.tvEmployeeName).text = employeeName
        findViewById<TextView>(R.id.tvEmployeeEmail).text = employeeEmail
        findViewById<TextView>(R.id.tvEmployeePosition).text = employeePosition

        val imageView = findViewById<ImageView>(R.id.ivEmployeePicture)
        Picasso.get()
            .load(employeePicture)
            .placeholder(R.drawable.ic_avatar_placeholder)
            .error(R.drawable.ic_avatar_placeholder)
            .into(imageView)

        // Set up click listeners for 4 buttons
        findViewById<View>(R.id.totalWorkingDays).setOnClickListener {
            navigateToDetail(WorkingDaysActivity::class.java, employeeName)
        }
        findViewById<View>(R.id.totalLeaveDays).setOnClickListener {
            navigateToDetail(LeaveDaysActivity::class.java, employeeName)
        }
        findViewById<View>(R.id.totalLateTimes).setOnClickListener {
            navigateToDetail(LateTimesActivity::class.java, employeeName)
        }
        findViewById<View>(R.id.totalEarlyLeaves).setOnClickListener {
            navigateToDetail(EarlyLeaveActivity::class.java, employeeName)
        }
    }

    private fun navigateToDetail(activityClass: Class<*>, employeeName: String?) {
        val intent = Intent(this, activityClass).apply {
            putExtra("EMPLOYEE_NAME", employeeName)
        }
        startActivity(intent)
    }
}
