package com.example.chamcong

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
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
    private val auth = FirebaseAuth.getInstance()
    private var employeeEmail: String? = null

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

        // Retrieve data from the intent
        employeeEmail = intent.getStringExtra("EMPLOYEE_EMAIL")
        val name = intent.getStringExtra("EMPLOYEE_NAME")
        val position = intent.getStringExtra("EMPLOYEE_POSITION")
        val phone = intent.getStringExtra("EMPLOYEE_PHONE")
        val url = intent.getStringExtra("EMPLOYEE_URL")
        val cccd = intent.getStringExtra("EMPLOYEE_CCCD")
        val gender = intent.getStringExtra("EMPLOYEE_GENDER")
        val role = intent.getStringExtra("EMPLOYEE_ROLE")

        // Set the data into the EditTexts
        employeeNameEditText.setText(name)
        employeePositionEditText.setText(position)
        employeePhoneEditText.setText(phone)
        employeeCCCDEditText.setText(cccd)
        employeeURLEditText.setText(url)
        employeeGenderEditText.setText(gender)
        employeeRoleEditText.setText(role)

        val saveButton: Button = findViewById(R.id.saveButton)
        val deleteButton: Button = findViewById(R.id.deleteButton)
        val backButton: Button = findViewById(R.id.backButton)

        saveButton.setOnClickListener {
            updateEmployee()
        }

        deleteButton.setOnClickListener {
            showDeleteConfirmationDialog()
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

        if (name.isEmpty() || position.isEmpty() || phone.isEmpty() || cccd.isEmpty() || gender.isEmpty() || role.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (!employeeEmail.isNullOrEmpty()) {
            val updatedEmployee = mapOf(
                "name" to name,
                "position" to position,
                "phone" to phone,
                "cccd" to cccd,
                "picture" to url,
                "gender" to gender,
                "role" to role
            )

            // Update Firestore first
            updateEmployeeInFirestore(updatedEmployee)
        } else {
            Toast.makeText(this, "Employee email is missing, cannot update", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateEmployeeInFirestore(updatedEmployee: Map<String, Any>) {
        firestore.collection("Users")
            .document(employeeEmail!!)
            .update(updatedEmployee)
            .addOnSuccessListener {
                Toast.makeText(this, "Employee updated in Firestore successfully", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to update employee in Firestore: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun showDeleteConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Employee")
        builder.setMessage("Are you sure you want to delete this employee?")

        builder.setPositiveButton("Yes") { _, _ ->
            deleteEmployeeFromFirestore()
        }

        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun deleteEmployeeFromFirestore() {
        if (!employeeEmail.isNullOrEmpty()) {
            // Retrieve the employee's role from Firestore to check if it's "admin" or "user"
            firestore.collection("Users")
                .document(employeeEmail!!)
                .get()
                .addOnSuccessListener { document ->
                    val role = document.getString("role")
                    if (role == "user") {
                        // Proceed with deletion if the role is "user"
                        firestore.collection("Users")
                            .document(employeeEmail!!)
                            .delete()
                            .addOnSuccessListener {
                                Toast.makeText(this, "Employee deleted from Firestore successfully", Toast.LENGTH_SHORT).show()
                                // After Firestore deletion, delete from Firebase Authentication
                                deleteEmployeeFromAuthentication()
                            }
                            .addOnFailureListener { exception ->
                                Toast.makeText(this, "Failed to delete employee from Firestore: ${exception.message}", Toast.LENGTH_LONG).show()
                            }
                    } else {
                        // Prevent deletion if the role is not "user" (e.g., "admin")
                        Toast.makeText(this, "Cannot delete an admin", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to retrieve employee role: ${exception.message}", Toast.LENGTH_LONG).show()
                }
        } else {
            Toast.makeText(this, "Employee email is missing, cannot delete from Firestore", Toast.LENGTH_SHORT).show()
        }
    }
    private fun deleteEmployeeFromAuthentication() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            currentUser.delete()
                .addOnSuccessListener {
                    Toast.makeText(this, "Employee deleted from Authentication", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to delete from Authentication: ${exception.message}", Toast.LENGTH_LONG).show()
                }
        } else {
            Toast.makeText(this, "No authenticated user found, cannot delete from Authentication", Toast.LENGTH_SHORT).show()
        }
    }

}
