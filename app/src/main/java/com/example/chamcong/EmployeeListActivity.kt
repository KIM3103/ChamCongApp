package com.example.chamcong

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import androidx.appcompat.widget.SearchView
import kotlin.math.min

class EmployeeListActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var employeeListAdapter: EmployeeAdapter
    private lateinit var employeeListView: RecyclerView
    private lateinit var btnAddEmployee: Button
    private lateinit var nextPageButton: Button
    private lateinit var prevPageButton: Button
    private lateinit var pageNumberTextView: TextView
    private lateinit var searchView: SearchView

    private var employeeList: MutableList<Employee> = mutableListOf()
    private var filteredEmployeeList: MutableList<Employee> = mutableListOf()  // Filtered list for search
    private var currentPage = 0
    private val employeesPerPage = 8

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_employee_list)

        db = FirebaseFirestore.getInstance()
        employeeListView = findViewById(R.id.employeeListRecyclerView)
        btnAddEmployee = findViewById(R.id.addEmployeeButton)
        nextPageButton = findViewById(R.id.nextPageButton)
        prevPageButton = findViewById(R.id.prevPageButton)
        pageNumberTextView = findViewById(R.id.pageNumberTextView)
        searchView = findViewById(R.id.searchEmployee)

        // Initialize RecyclerView and Adapter
        employeeListView.layoutManager = LinearLayoutManager(this)
        employeeListAdapter = EmployeeAdapter(mutableListOf(), { employee ->
            showEditEmployeeDialog(employee)  // Edit employee
        }, { employee ->
            deleteEmployee(employee)  // Delete employee
        })

        employeeListView.adapter = employeeListAdapter

        // Fetch employee data from Firestore
        fetchEmployees()

        // Pagination buttons
        nextPageButton.setOnClickListener {
            if (currentPage < (filteredEmployeeList.size / employeesPerPage)) {
                currentPage++
                updateEmployeeList()
            }
        }

        prevPageButton.setOnClickListener {
            if (currentPage > 0) {
                currentPage--
                updateEmployeeList()
            }
        }

        // Search functionality
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterEmployees(newText ?: "")
                return true
            }
        })

        // Add employee button listener
        btnAddEmployee.setOnClickListener {
            addEmployee()
        }
    }

    private fun showEditEmployeeDialog(employee: Employee) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_employee_operations, null)
        val etName = dialogView.findViewById<EditText>(R.id.etEmployeeName)
        val etPosition = dialogView.findViewById<EditText>(R.id.etEmployeePosition)
        val etPhone = dialogView.findViewById<EditText>(R.id.etEmployeePhone)
        val etCccd = dialogView.findViewById<EditText>(R.id.etEmployeeCccd)
        val etGender = dialogView.findViewById<EditText>(R.id.etEmployeeGender)
        val etRole = dialogView.findViewById<EditText>(R.id.etEmployeeRole)

        // Populate the EditText fields with current employee details
        etName.setText(employee.name)
        etPosition.setText(employee.position)
        etPhone.setText(employee.phone)
        etCccd.setText(employee.cccd)
        etGender.setText(employee.gender)
        etRole.setText(employee.role)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Edit Employee")
            .setPositiveButton("Save") { dialogInterface, _ ->
                val name = etName.text.toString()
                val position = etPosition.text.toString()
                val phone = etPhone.text.toString()
                val cccd = etCccd.text.toString()
                val gender = etGender.text.toString()
                val role = etRole.text.toString()

                if (name.isNotEmpty() && position.isNotEmpty() && phone.isNotEmpty() && cccd.isNotEmpty()) {
                    // Update the employee object
                    val updatedEmployee = Employee(name, position, phone, cccd, gender, role)
                    updateEmployeeInFirestore(updatedEmployee)
                    dialogInterface.dismiss()
                } else {
                    Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun updateEmployeeInFirestore(updatedEmployee: Employee) {
        val employeeDocRef = db.collection("Users").document(updatedEmployee.cccd)
        employeeDocRef.set(updatedEmployee)  // Update the employee record in Firestore
            .addOnSuccessListener {
                Toast.makeText(this, "Employee updated successfully!", Toast.LENGTH_SHORT).show()
                fetchEmployees()  // Refresh the employee list after updating
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update employee!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchEmployees() {
        db.collection("Users")
            .get()
            .addOnSuccessListener { result ->
                employeeList.clear()
                for (document in result) {
                    val employee = document.toObject(Employee::class.java)
                    employeeList.add(employee)
                }
                filteredEmployeeList = employeeList.toMutableList()  // Initially, filtered list is the same as the employee list
                updateEmployeeList()  // Display initial page after loading data
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading employees: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateEmployeeList() {
        val startIndex = currentPage * employeesPerPage
        val endIndex = min(startIndex + employeesPerPage, filteredEmployeeList.size)

        val paginatedList = filteredEmployeeList.subList(startIndex, endIndex)
        employeeListAdapter.updateEmployeeList(paginatedList)

        nextPageButton.isEnabled = endIndex < filteredEmployeeList.size
        prevPageButton.isEnabled = currentPage > 0

        pageNumberTextView.text = "Page ${currentPage + 1}"
    }

    private fun filterEmployees(query: String) {
        filteredEmployeeList = employeeList.filter {
            it.name.contains(query, ignoreCase = true) || it.position.contains(query, ignoreCase = true)
        }.toMutableList()

        currentPage = 0  // Reset to first page when search is applied
        updateEmployeeList()
    }

    private fun addEmployee() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_employee_operations, null)
        val etName = dialogView.findViewById<EditText>(R.id.etEmployeeName)
        val etPosition = dialogView.findViewById<EditText>(R.id.etEmployeePosition)
        val etPhone = dialogView.findViewById<EditText>(R.id.etEmployeePhone)
        val etCccd = dialogView.findViewById<EditText>(R.id.etEmployeeCccd)
        val etGender = dialogView.findViewById<EditText>(R.id.etEmployeeGender)
        val etRole = dialogView.findViewById<EditText>(R.id.etEmployeeRole)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Add New Employee")
            .setPositiveButton("Save") { dialogInterface, _ ->
                val name = etName.text.toString()
                val position = etPosition.text.toString()
                val phone = etPhone.text.toString()
                val cccd = etCccd.text.toString()
                val gender = etGender.text.toString()
                val role = etRole.text.toString()

                if (name.isNotEmpty() && position.isNotEmpty() && phone.isNotEmpty() && cccd.isNotEmpty()) {
                    val newEmployee = Employee(name, position, phone, cccd, gender, role)
                    addEmployeeToFirestore(newEmployee)
                    dialogInterface.dismiss()
                } else {
                    Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun addEmployeeToFirestore(employee: Employee) {
        db.collection("Users")
            .add(employee)
            .addOnSuccessListener {
                Toast.makeText(this, "Employee added successfully", Toast.LENGTH_SHORT).show()
                fetchEmployees()  // Refresh the employee list after adding
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error adding employee: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteEmployee(employee: Employee) {
        val employeeDocRef = db.collection("Users").document(employee.cccd)
        employeeDocRef.delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Employee deleted successfully!", Toast.LENGTH_SHORT).show()
                fetchEmployees()  // Refresh the employee list after deletion
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to delete employee!", Toast.LENGTH_SHORT).show()
            }
    }
}
