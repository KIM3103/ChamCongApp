package com.example.chamcong

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class EditEmployeeActivity : AppCompatActivity() {

    private lateinit var employeeNameEditText: EditText
    private lateinit var employeePositionEditText: EditText
    private lateinit var employeePhoneEditText: EditText
    private lateinit var employeeCCCDEditText: EditText
    private lateinit var employeeURLEditText: EditText
    private lateinit var employeeGenderEditText: EditText
    private lateinit var employeeRoleEditText: EditText
    private val firestore = FirebaseFirestore.getInstance()
    private var employeeEmail: String? = null // Email của nhân viên được chọn

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_employee)

        // Initialize views
        employeeNameEditText = findViewById(R.id.employeeName)
        employeePositionEditText = findViewById(R.id.employeePosition)
        employeePhoneEditText = findViewById(R.id.employeePhone)
        employeeCCCDEditText = findViewById(R.id.employeeCCCD)
        employeeURLEditText = findViewById(R.id.employeeURL)
        employeeGenderEditText = findViewById(R.id.employeeGender)
        employeeRoleEditText = findViewById(R.id.employeeRole)

        // Lấy dữ liệu từ Intent
        employeeEmail = intent.getStringExtra("EMPLOYEE_EMAIL") // Lưu email của nhân viên được chọn
        val employeeName = intent.getStringExtra("EMPLOYEE_NAME")
        val employeePosition = intent.getStringExtra("EMPLOYEE_POSITION")
        val employeePhone = intent.getStringExtra("EMPLOYEE_PHONE")
        val employeeCCCD = intent.getStringExtra("EMPLOYEE_CCCD")
        val employeeURL = intent.getStringExtra("EMPLOYEE_URL")
        val employeeGender = intent.getStringExtra("EMPLOYEE_GENDER")
        val employeeRole = intent.getStringExtra("EMPLOYEE_ROLE")

        // Set data to EditTexts
        employeeNameEditText.setText(employeeName)
        employeePositionEditText.setText(employeePosition)
        employeePhoneEditText.setText(employeePhone)
        employeeCCCDEditText.setText(employeeCCCD)
        employeeURLEditText.setText(employeeURL)
        employeeGenderEditText.setText(employeeGender)
        employeeRoleEditText.setText(employeeRole)

        val saveButton: Button = findViewById(R.id.saveButton)
        val deleteButton: Button = findViewById(R.id.deleteButton)
        val backButton: Button = findViewById(R.id.backButton)

        saveButton.setOnClickListener {
            updateEmployee()
        }

        deleteButton.setOnClickListener {
            deleteEmployee()
        }

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun updateEmployee() {
        val name = employeeNameEditText.text.toString().trim()
        val position = employeePositionEditText.text.toString().trim()
        val phone = employeePhoneEditText.text.toString().trim()
        val cccd = employeeCCCDEditText.text.toString().trim()
        val url = employeeURLEditText.text.toString().trim()
        val gender = employeeGenderEditText.text.toString().trim()
        val role = employeeRoleEditText.text.toString().trim()

        // Kiểm tra nếu email không null hoặc trống
        if (!employeeEmail.isNullOrEmpty()) {
            val updatedEmployee = mapOf(
                "name" to name,
                "position" to position,
                "phone" to phone,
                "cccd" to cccd,
                "picture" to url,
                "gender" to gender,
                "role" to role,
                "email" to employeeEmail
            )

            firestore.collection("Users")
                .document(employeeEmail!!) // Sử dụng email làm documentId
                .set(updatedEmployee)
                .addOnSuccessListener {
                    Toast.makeText(this, "Employee updated successfully", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to update employee", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Employee email is missing, cannot update", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteEmployee() {
        val employeeRole = employeeRoleEditText.text.toString()

        if (employeeRole.equals("user", ignoreCase = true) && !employeeEmail.isNullOrEmpty()) {
            firestore.collection("Users")
                .document(employeeEmail!!)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists() && document.getString("role") == "user") {
                        firestore.collection("Users")
                            .document(employeeEmail!!)
                            .delete()
                            .addOnSuccessListener {
                                Toast.makeText(this, "Employee deleted successfully", Toast.LENGTH_SHORT).show()
                                setResult(RESULT_OK)
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Failed to delete employee", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(this, "No employee with this role and email found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to find employee", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Cannot delete employee with role: $employeeRole", Toast.LENGTH_SHORT).show()
        }
    }
}
