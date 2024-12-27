package com.example.chamcong

//Admin CheckIn//
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore

class AttendanceListActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: AttendanceAdapter
    private val historyList = mutableListOf<AttendanceRecord>() // Danh sách tất cả bản ghi check-in
    private val filteredList = mutableListOf<AttendanceRecord>() // Danh sách đã lọc để hiển thị
    private lateinit var listView: ListView
    private lateinit var buttonCheckout: Button
    private lateinit var editTextEmail: EditText
    private lateinit var editTextDate: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.listcheckin)

        listView = findViewById(R.id.listView)
        buttonCheckout = findViewById(R.id.buttonCheckout)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextDate = findViewById(R.id.editTextDate)

        // Thiết lập adapter
        adapter = AttendanceAdapter(this, filteredList)
        listView.adapter = adapter

        // Khởi động CheckoutActivity khi nhấn nút
        buttonCheckout.setOnClickListener {
            val intent = Intent(this, CheckoutListActivity::class.java)
            startActivity(intent)
        }

        db = FirebaseFirestore.getInstance()
        loadCheckInRecords()

        // Thiết lập các listener cho tìm kiếm
        setupSearchListeners()
        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.DSNhanVien -> {
                    startActivity(Intent(this, EmployeeListActivity::class.java))
                    true
                }
                R.id.DSChamCong -> {
                    startActivity(Intent(this, AttendanceListActivity::class.java))
                    true
                }
                R.id.DSTongCong -> {
                    startActivity(Intent(this, AllTimeActivity::class.java))
                    true
                }
                R.id.DSTanCa -> {
                    startActivity(Intent(this, CheckoutListActivity::class.java))
                    true
                }
                R.id.TaiKhoanAd -> {
                    startActivity(Intent(this, MainActivityAd::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun loadCheckInRecords() {
        val attendanceRef = db.collection("CheckIn")

        attendanceRef.get().addOnCompleteListener { attendanceTask ->
            if (attendanceTask.isSuccessful) {
                for (record in attendanceTask.result) {
                    val checkInTime = record.getString("checkInTime") ?: ""
                    val date = record.getString("date") ?: ""
                    val email = record.getString("email") ?: ""
                    val status = record.getString("lateStatus") ?: ""

                    // Thêm bản ghi vào danh sách historyList
                    historyList.add(AttendanceRecord(checkInTime = checkInTime, date = date, email = email, status = status))
                }
                filteredList.addAll(historyList) // Hiển thị tất cả bản ghi ban đầu
                adapter.notifyDataSetChanged()  // Cập nhật adapter
            } else {
                Log.w("AttendanceListActivity", "Error getting attendance data.", attendanceTask.exception)
            }
        }
    }

    private fun setupSearchListeners() {
        // Listener cho tìm kiếm theo ngày
        editTextDate.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterRecords()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Listener cho tìm kiếm theo email
        editTextEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterRecords()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterRecords() {
        val dateQuery = editTextDate.text.toString().trim()
        val emailQuery = editTextEmail.text.toString().trim()

        filteredList.clear()

        for (record in historyList) {
            val matchesDate = dateQuery.isEmpty() || record.date.contains(dateQuery, ignoreCase = true)
            val matchesEmail = emailQuery.isEmpty() || record.email.contains(emailQuery, ignoreCase = true)

            if (matchesDate && matchesEmail) {
                filteredList.add(record)
            }
        }

        adapter.notifyDataSetChanged() // Cập nhật ListView với dữ liệu đã lọc
    }
}

