package com.example.chamcong

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class AddEmployeeActivity : AppCompatActivity() {

    private lateinit var employeeNameEditText: EditText
    private lateinit var employeePositionEditText: EditText
    private lateinit var employeePhoneEditText: EditText
    private lateinit var employeeCCCDEditText: EditText
    private lateinit var employeeURLEditText: EditText
    private lateinit var employeeGenderEditText: EditText
    private lateinit var employeeRoleEditText: EditText
    private lateinit var employeeEmailEditText: EditText // Thêm EditText cho email
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_employee)

        // Initialize views
        employeeNameEditText = findViewById(R.id.employeeName)
        employeePositionEditText = findViewById(R.id.employeePosition)
        employeePhoneEditText = findViewById(R.id.employeePhone)
        employeeCCCDEditText = findViewById(R.id.employeeCCCD)
        employeeURLEditText = findViewById(R.id.employeeURL)
        employeeGenderEditText = findViewById(R.id.employeeGender)
        employeeRoleEditText = findViewById(R.id.employeeRole)
        employeeEmailEditText = findViewById(R.id.employeeEmail) // Initialize email EditText

        val saveButton: Button = findViewById(R.id.saveButton)
        val backButton: Button = findViewById(R.id.backButton)

        // Set up save button click listener
        saveButton.setOnClickListener { saveEmployee() }

        // Set up back button click listener
        backButton.setOnClickListener {
            finish() // Close AddEmployeeActivity and go back to EmployeeListActivity
        }
    }

    private fun saveEmployee() {
        val name = employeeNameEditText.text.toString().trim()
        val position = employeePositionEditText.text.toString().trim()
        val phone = employeePhoneEditText.text.toString().trim()
        val cccd = employeeCCCDEditText.text.toString().trim()
        val url = employeeURLEditText.text.toString().trim()
        val gender = employeeGenderEditText.text.toString().trim()
        val role = employeeRoleEditText.text.toString().trim()
        val email = employeeEmailEditText.text.toString().trim() // Lấy email từ EditText

        if (name.isEmpty() || position.isEmpty() || phone.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val newEmployee = Employee(name, position, phone, cccd, gender, role, url, email)

        // Add new employee to Firestore
        firestore.collection("Users").document(email).set(newEmployee)
            .addOnSuccessListener {
                Toast.makeText(this, "Employee added successfully", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK) // Indicate success
                finish() // Close AddEmployeeActivity and go back to EmployeeListActivity
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to add employee", Toast.LENGTH_SHORT).show()
            }
    }
}
