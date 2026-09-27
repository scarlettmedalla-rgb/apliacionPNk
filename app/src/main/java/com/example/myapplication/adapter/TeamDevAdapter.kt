package com.example.myapplication.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.model.Developer
import java.io.File

class TeamDevAdapter(
    private var developers: List<Developer>,
    private val onItemClick: (Developer) -> Unit
) : RecyclerView.Adapter<TeamDevAdapter.TeamDevViewHolder>() {

    class TeamDevViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivPhoto: ImageView = itemView.findViewById(R.id.iv_dev_photo)
        val tvName: TextView = itemView.findViewById(R.id.tv_dev_name)
        val tvRole: TextView = itemView.findViewById(R.id.tv_dev_role)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeamDevViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_team_dev, parent, false)
        return TeamDevViewHolder(view)
    }

    override fun onBindViewHolder(holder: TeamDevViewHolder, position: Int) {
        val dev = developers[position]
        val context = holder.itemView.context

        holder.tvName.text = dev.name
        holder.tvRole.text = dev.role

        if (!dev.photoUri.isNullOrEmpty()) {
            val file = File(dev.photoUri)
            if (file.exists()) {
                holder.ivPhoto.clearColorFilter()
                holder.ivPhoto.setPadding(0, 0, 0, 0)
                holder.ivPhoto.setImageURI(Uri.fromFile(file))
            } else {
                holder.ivPhoto.clearColorFilter()
                holder.ivPhoto.setPadding(0, 0, 0, 0)
                holder.ivPhoto.setImageURI(Uri.parse(dev.photoUri))
            }
        } else {
            val density = context.resources.displayMetrics.density
            val p = (10 * density).toInt()
            holder.ivPhoto.setPadding(p, p, p, p)
            holder.ivPhoto.setImageResource(R.drawable.ic_users)
            holder.ivPhoto.setColorFilter(ContextCompat.getColor(context, R.color.pink_primary))
        }

        holder.itemView.setOnClickListener { onItemClick(dev) }
    }

    override fun getItemCount(): Int = developers.size

    fun updateData(newDevs: List<Developer>) {
        developers = newDevs
        notifyDataSetChanged()
    }
}
