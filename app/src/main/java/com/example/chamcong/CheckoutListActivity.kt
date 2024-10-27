package com.example.chamcong
import android.os.Bundle
import android.util.Log
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
class CheckoutListActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: CheckoutAdapter
    private val checkoutList = mutableListOf<AttendanceRecord>()  // Danh sách để lưu bản ghi check-out
    private lateinit var listView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout_list) // Sử dụng layout cho activity này

        listView = findViewById(R.id.listViewCheckout)

        // Thiết lập adapter
        adapter = CheckoutAdapter(this, checkoutList)
        listView.adapter = adapter

        db = FirebaseFirestore.getInstance()
        loadCheckoutRecords()  // Gọi phương thức để tải tất cả bản ghi check-out
    }

    private fun loadCheckoutRecords() {
        val checkoutRef = db.collection("CheckOut")

        // Truy xuất tất cả các tài liệu trong collection CheckOut
        checkoutRef.get().addOnCompleteListener { checkoutTask ->
            if (checkoutTask.isSuccessful) {
                for (recordDocument in checkoutTask.result) {
                    val checkOutTime = recordDocument.getString("checkOutTime") ?: ""
                    val date = recordDocument.getString("date") ?: "" // Nếu cần, thay đổi cách lấy date
                    val email = recordDocument.getString("email") ?: ""
                    val earlyLeaveStatus = recordDocument.getString("earlyLeaveStatus") ?: ""

                    // Thêm bản ghi vào danh sách checkoutList
                    checkoutList.add(AttendanceRecord(checkOutTime, date, email, earlyLeaveStatus))
                }
                adapter.notifyDataSetChanged()  // Cập nhật adapter
            } else {
                Log.w("CheckoutListActivity", "Error getting checkout records.", checkoutTask.exception)
            }
        }
    }

}