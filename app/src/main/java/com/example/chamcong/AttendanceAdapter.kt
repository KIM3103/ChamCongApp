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
        viewHolder.tvCheckInTime.text = record?.checkInTime
        viewHolder.tvDate.text = record?.date
        viewHolder.tvEmail.text = record?.email
        viewHolder.tvStatus.text = record?.status

        return view
    }

    private class ViewHolder(view: View) {
        val tvCheckInTime: TextView = view.findViewById(R.id.tvCheckInTime)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvEmail: TextView = view.findViewById(R.id.tvEmail)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
    }
}
