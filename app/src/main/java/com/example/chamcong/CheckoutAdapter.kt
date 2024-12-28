package com.example.chamcong


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class CheckoutAdapter(context: Context, private val checkoutList: List<AttendanceRecord>) :
    ArrayAdapter<AttendanceRecord>(context, 0, checkoutList) {

    // Ghi đè phương thức getView để thiết lập cách hiển thị từng item trong ListView
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val record = getItem(position)

        // Sử dụng ViewHolder để cải thiện hiệu suất
        val viewHolder: ViewHolder
        val view: View

        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_checkout, parent, false)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        // Thiết lập dữ liệu cho ViewHolder
        viewHolder.checkOutTimeTextView.text = record?.checkOutTime // Thay đổi thành checkOutTime nếu cần
        viewHolder.dateTextView.text = record?.date
        viewHolder.emailTextView.text = record?.email
        viewHolder.statusTextView.text = record?.status

        return view
    }

    // Khai báo ViewHolder để giữ các tham chiếu đến các View
    private class ViewHolder(view: View) {
        val checkOutTimeTextView: TextView = view.findViewById(R.id.checkOutTimeTextView)
        val dateTextView: TextView = view.findViewById(R.id.dateTextView)
        val emailTextView: TextView = view.findViewById(R.id.emailTextView)
        val statusTextView: TextView = view.findViewById(R.id.statusTextView)
    }
}
