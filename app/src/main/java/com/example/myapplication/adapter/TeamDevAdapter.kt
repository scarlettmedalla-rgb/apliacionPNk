package com.example.myapplication.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.model.Developer
import com.example.myapplication.util.ImageHelper

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

        holder.tvName.text = dev.name
        holder.tvRole.text = dev.role

        val defaultRes = when {
            dev.id == 1 || dev.name.contains("Jorge", ignoreCase = true) -> R.drawable.jorge_cortes
            dev.id == 2 || dev.name.contains("Kevin", ignoreCase = true) -> R.drawable.kevin_encina
            dev.id == 3 || dev.name.contains("Scarlett", ignoreCase = true) || dev.name.contains("Scar", ignoreCase = true) -> R.drawable.scarlett_williams
            else -> R.drawable.ic_users
        }
        ImageHelper.loadProfilePhoto(holder.ivPhoto, dev.photoUri, defaultRes)

        holder.itemView.setOnClickListener { onItemClick(dev) }
    }

    override fun getItemCount(): Int = developers.size

    fun updateData(newDevs: List<Developer>) {
        developers = newDevs
        notifyDataSetChanged()
    }
}
