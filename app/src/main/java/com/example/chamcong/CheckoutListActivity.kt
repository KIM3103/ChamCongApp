package com.example.chamcong

//Admin xem danh sach tan ca//

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class CheckoutListActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: CheckoutAdapter
    private val checkoutList = mutableListOf<AttendanceRecord>() // Full list of checkout records
    private val filteredList = mutableListOf<AttendanceRecord>() // Filtered list for display
    private lateinit var listView: ListView
    private lateinit var buttonBack: Button
    private lateinit var searchDate: EditText
    private lateinit var searchEmail: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout_list)

        listView = findViewById(R.id.listViewCheckout)
        buttonBack = findViewById(R.id.buttonBack)
        searchDate = findViewById(R.id.editTextSearchDay)
        searchEmail = findViewById(R.id.editTextSearchEmail)

        // Set up the adapter
        adapter = CheckoutAdapter(this, filteredList)
        listView.adapter = adapter

        // Back button to return to AttendanceListActivity
        buttonBack.setOnClickListener {
            val intent = Intent(this, AttendanceListActivity::class.java)
            startActivity(intent)
        }

        db = FirebaseFirestore.getInstance()
        loadCheckoutRecords()

        // Set up search listeners
        setupSearchListeners()
    }

    private fun loadCheckoutRecords() {
        val checkoutRef = db.collection("CheckOut")

        // Retrieve all documents in the CheckOut collection
        checkoutRef.get().addOnCompleteListener { checkoutTask ->
            if (checkoutTask.isSuccessful) {
                for (recordDocument in checkoutTask.result) {
                    val checkOutTime = recordDocument.getString("checkOutTime") ?: ""
                    val date = recordDocument.getString("date") ?: ""
                    val email = recordDocument.getString("email") ?: ""
                    val status = recordDocument.getString("earlyLeaveStatus") ?: ""

                    // Add each record to the checkoutList
                    val record = AttendanceRecord(checkOutTime = checkOutTime, date = date, email = email, status = status)
                    checkoutList.add(record)
                }
                filteredList.addAll(checkoutList) // Initially, display all records
                adapter.notifyDataSetChanged()
            } else {
                Log.w("CheckoutListActivity", "Error getting checkout records.", checkoutTask.exception)
            }
        }
    }

    private fun setupSearchListeners() {
        // Listener for search by date
        searchDate.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterRecords()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Listener for search by email
        searchEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterRecords()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterRecords() {
        val dateQuery = searchDate.text.toString().trim()
        val emailQuery = searchEmail.text.toString().trim()

        filteredList.clear()

        for (record in checkoutList) {
            val matchesDate = dateQuery.isEmpty() || record.date.contains(dateQuery, ignoreCase = true)
            val matchesEmail = emailQuery.isEmpty() || record.email.contains(emailQuery, ignoreCase = true)

            if (matchesDate && matchesEmail) {
                filteredList.add(record)
            }
        }

        adapter.notifyDataSetChanged() // Refresh the ListView with filtered data
    }
}
