import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.chamcong.Employee
import com.example.chamcong.R
import com.squareup.picasso.Picasso

class EmployeeAdapter(private val context: Context, private val employeeList: List<Employee>) : BaseAdapter() {

    override fun getCount(): Int = employeeList.size

    override fun getItem(position: Int): Any = employeeList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val holder: ViewHolder

        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_employee, parent, false)
            holder = ViewHolder(view)
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as ViewHolder
        }

        val employee = employeeList[position]

        // Load image using Picasso with error handling
        if (employee.picture.isNotEmpty()) {
            Picasso.get()
                .load(employee.picture)
                .placeholder(R.drawable.ic_avatar_placeholder)
                .error(R.drawable.ic_avatar_placeholder) // Set fallback image on error
                .into(holder.imageView)
        } else {
            holder.imageView.setImageResource(R.drawable.ic_avatar_placeholder)
        }

        // Set the text views with employee information
        holder.nameTextView.text = employee.name
        holder.emailTextView.text = employee.email
        holder.positionTextView.text = employee.position
        holder.genderTextView.text = employee.gender

        return view
    }

    private class ViewHolder(view: View) {
        val imageView: ImageView = view.findViewById(R.id.employeeImageView)
        val nameTextView: TextView = view.findViewById(R.id.employeeName)
        val emailTextView: TextView = view.findViewById(R.id.employeeEmail)
        val positionTextView: TextView = view.findViewById(R.id.employeePosition)
        val genderTextView: TextView = view.findViewById(R.id.employeeGender)
    }
}
