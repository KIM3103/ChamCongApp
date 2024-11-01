package com.example.chamcong
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.widget.ArrayAdapter
import android.content.Context

class AttendanceAdapter(context: Context, private val historyList: List<AttendanceRecord>) :
    ArrayAdapter<AttendanceRecord>(context, R.layout.item_attendance_record, historyList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val record = getItem(position)

        // Use ViewHolder for better performance
        val viewHolder: ViewHolder
        val view: View

        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_attendance_record, parent, false)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        // Set data for ViewHolder
        viewHolder.tvEmail.text = record?.email
        viewHolder.tvDate.text = record?.date
        viewHolder.tvCheckInTime.text = record?.checkInTime
        viewHolder.tvStatus.text = record?.status

        return view
    }

    // ViewHolder class to hold references to the views
    private class ViewHolder(view: View) {
        val tvEmail: TextView = view.findViewById(R.id.tvEmail)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvCheckInTime: TextView = view.findViewById(R.id.tvCheckInTime)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
    }
}
