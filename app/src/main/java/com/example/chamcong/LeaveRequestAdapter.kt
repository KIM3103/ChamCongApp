package com.example.chamcong;

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView


class LeaveRequestAdapter(
    context: Context,
    private val leaveRequestList: List<LeaveRequest>,
    private val onStatusClick: (LeaveRequest) -> Unit // Callback cho click
) : ArrayAdapter<LeaveRequest>(context, 0, leaveRequestList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_leave_request, parent, false)
        val leaveRequest = leaveRequestList[position]

        // Bind data
        val tvEmployeeName = view.findViewById<TextView>(R.id.tvEmployeeName)
        val tvStartDate = view.findViewById<TextView>(R.id.tvStartDate)
        val tvEndDate = view.findViewById<TextView>(R.id.tvEndDate)
        val tvReason = view.findViewById<TextView>(R.id.tvReason)
        val tvStatus = view.findViewById<TextView>(R.id.tvStatus)

        tvEmployeeName.text = leaveRequest.userName
        tvStartDate.text = "Start: ${leaveRequest.startDate}"
        tvEndDate.text = "End: ${leaveRequest.endDate}"
        tvReason.text = "Reason: ${leaveRequest.reason}"
        tvStatus.text = "Status: ${leaveRequest.status}"

        // Click event to toggle status
        tvStatus.setOnClickListener {
            onStatusClick(leaveRequest)
        }

        return view
    }
}


