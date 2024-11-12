package com.example.chamcong

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// Define the EmployeeAdapter class
class EmployeeAdapter(
    private var employees: List<Employee>,  // List of Employee objects
    private val onClick: (Employee) -> Unit, // Lambda function to handle item click
    private val onDelete: (Employee) -> Unit // Lambda function to handle delete button click
) : RecyclerView.Adapter<EmployeeAdapter.EmployeeViewHolder>() {

    // ViewHolder class to hold references to the views in each item
    inner class EmployeeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.etEmployeeName)  // Use tvEmployeeName instead of etEmployeeName
        val tvPosition: TextView = itemView.findViewById(R.id.etEmployeePosition)  // Use tvEmployeePosition instead of etEmployeePosition
        val btnDelete: Button = itemView.findViewById(R.id.btnDelete) // Button to delete employee

        // Bind employee data to the views
        fun bind(employee: Employee) {
            tvName.text = employee.name
            tvPosition.text = employee.position

            // Set up the click listener for the item
            itemView.setOnClickListener { onClick(employee) }

            // Set up the delete button
            btnDelete.setOnClickListener {
                // Handle delete click (optional confirmation dialog)
                onDelete(employee)
            }
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmployeeViewHolder {
        // Inflate the item layout for each employee
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_employee, parent, false)
        return EmployeeViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: EmployeeViewHolder, position: Int) {
        val employee = employees[position]
        holder.bind(employee) // Bind the employee data to the views
    }

    // Return the total number of items in the data set
    override fun getItemCount(): Int = employees.size

    // Update the list of employees in the adapter
    fun updateEmployeeList(newEmployees: List<Employee>) {
        employees = newEmployees
        notifyDataSetChanged()  // Notify that the data set has changed
    }
}
