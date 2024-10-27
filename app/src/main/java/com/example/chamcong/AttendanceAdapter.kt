package com.example.chamcong
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
class AttendanceAdapter(private val historyList: List<AttendanceRecord>) :
    RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttendanceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_attendance_record, parent, false)
        return AttendanceViewHolder(view)
    }

    override fun onBindViewHolder(holder: AttendanceViewHolder, position: Int) {
        val record = historyList[position]
        holder.tvEmail.text = record.email
        holder.tvDate.text = record.date
        holder.tvCheckInTime.text = record.checkInTime
        holder.tvStatus.text = record.status
    }

    override fun getItemCount(): Int = historyList.size

    inner class AttendanceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvEmail: TextView = itemView.findViewById(R.id.tvEmail)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvCheckInTime: TextView = itemView.findViewById(R.id.tvCheckInTime)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
    }
}