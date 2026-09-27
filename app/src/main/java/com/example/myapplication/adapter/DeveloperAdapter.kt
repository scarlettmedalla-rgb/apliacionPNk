package com.example.myapplication.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.model.Developer

class DeveloperAdapter(
    private val developers: List<Developer>
) : RecyclerView.Adapter<DeveloperAdapter.DevViewHolder>() {

    class DevViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tv_dev_name)
        val tvRole: TextView = itemView.findViewById(R.id.tv_dev_role)
        val tvDesc: TextView = itemView.findViewById(R.id.tv_dev_desc)
        val tvEmail: TextView = itemView.findViewById(R.id.tv_dev_email)
        val tvGithub: TextView = itemView.findViewById(R.id.tv_dev_github)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DevViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_developer, parent, false)
        return DevViewHolder(view)
    }

    override fun onBindViewHolder(holder: DevViewHolder, position: Int) {
        val dev = developers[position]
        holder.tvName.text = dev.name
        holder.tvRole.text = dev.role
        holder.tvDesc.text = dev.description
        holder.tvEmail.text = "✉ ${dev.email}"
        holder.tvGithub.text = "🔗 ${dev.github}"
    }

    override fun getItemCount(): Int = developers.size
}
