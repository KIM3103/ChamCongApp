package com.example.chamcong

import EmployeeAdapter
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore

class EmployeeListActivity : AppCompatActivity() {

    private lateinit var employeeListView: ListView
    private lateinit var searchEmployeeEditText: EditText
    private lateinit var employeeListAdapter: EmployeeAdapter
    private lateinit var employeeList: MutableList<Employee>
    private lateinit var filteredEmployeeList: MutableList<Employee>
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_employee_list)

        // Khởi tạo các view
        employeeListView = findViewById(R.id.employeeListView)
        searchEmployeeEditText = findViewById(R.id.searchEmployee)
        val addEmployeeButton: Button = findViewById(R.id.addEmployeeButton)

        // Khởi tạo danh sách nhân viên
        employeeList = mutableListOf()
        filteredEmployeeList = mutableListOf()

        employeeListAdapter = EmployeeAdapter(this, filteredEmployeeList)
        employeeListView.adapter = employeeListAdapter

        // Fetch employee data from Firestore
        fetchEmployeeData()

        // Set up search functionality
        searchEmployeeEditText.addTextChangedListener {
            val query = it.toString()
            searchEmployees(query)
        }

        // Handle employee list item click
        employeeListView.setOnItemClickListener { _, _, position, _ ->
            val selectedEmployee = filteredEmployeeList[position]

            val intent = Intent(this, EditEmployeeActivity::class.java).apply {
                putExtra("EMPLOYEE_NAME", selectedEmployee.name)
                putExtra("EMPLOYEE_POSITION", selectedEmployee.position)
                putExtra("EMPLOYEE_PHONE", selectedEmployee.phone)
                putExtra("EMPLOYEE_URL", selectedEmployee.picture)
                putExtra("EMPLOYEE_CCCD", selectedEmployee.cccd)
                putExtra("EMPLOYEE_GENDER", selectedEmployee.gender)
                putExtra("EMPLOYEE_ROLE", selectedEmployee.role)
                putExtra("EMPLOYEE_EMAIL", selectedEmployee.email)
            }
            startActivityForResult(intent, 1)
        }

        addEmployeeButton.setOnClickListener {
            val intent = Intent(this, AddEmployeeActivity::class.java)
            startActivityForResult(intent, 1)
        }

        // Khởi tạo BottomNavigationView và xử lý sự kiện chọn mục
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

    private fun fetchEmployeeData() {
        firestore.collection("Users")
            .get()
            .addOnSuccessListener { result ->
                employeeList.clear()
                for (document in result) {
                    val employee = document.toObject(Employee::class.java)
                    employee?.let { employeeList.add(it) }
                }
                filteredEmployeeList.clear()
                filteredEmployeeList.addAll(employeeList)
                employeeListAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this@EmployeeListActivity,
                    "Failed to load employee data: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun searchEmployees(query: String) {
        filteredEmployeeList.clear()
        if (query.isEmpty()) {
            filteredEmployeeList.addAll(employeeList)
        } else {
            for (employee in employeeList) {
                if (employee.name.contains(query, true)) {
                    filteredEmployeeList.add(employee)
                }
            }
        }
        employeeListAdapter.notifyDataSetChanged()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            fetchEmployeeData() // Reload employee list after update or delete
        }
    }
}
