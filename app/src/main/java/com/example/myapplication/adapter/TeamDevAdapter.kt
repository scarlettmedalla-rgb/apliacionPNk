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

        val photoUri = if (!dev.photoUri.isNullOrEmpty()) {
            dev.photoUri
        } else if (dev.id == 1 || dev.name.contains("Jorge", ignoreCase = true)) {
            "android.resource://${context.packageName}/${R.drawable.jorge_cortes}"
        } else if (dev.id == 2 || dev.name.contains("Kevin", ignoreCase = true)) {
            "android.resource://${context.packageName}/${R.drawable.kevin_encina}"
        } else if (dev.id == 3 || dev.name.contains("Scarlett", ignoreCase = true) || dev.name.contains("Scar", ignoreCase = true)) {
            "android.resource://${context.packageName}/${R.drawable.scarlett_williams}"
        } else {
            null
        }

        if (!photoUri.isNullOrEmpty()) {
            val file = File(photoUri)
            holder.ivPhoto.clearColorFilter()
            holder.ivPhoto.imageTintList = null
            holder.ivPhoto.setPadding(0, 0, 0, 0)
            holder.ivPhoto.scaleType = ImageView.ScaleType.CENTER_CROP

            if (file.exists()) {
                holder.ivPhoto.setImageURI(Uri.fromFile(file))
            } else {
                val uri = Uri.parse(photoUri)
                if (uri.scheme == "android.resource" || uri.scheme == "content" || uri.scheme == "file") {
                    holder.ivPhoto.setImageURI(uri)
                } else {
                    val resId = context.resources.getIdentifier(photoUri, "drawable", context.packageName)
                    if (resId != 0) {
                        holder.ivPhoto.setImageResource(resId)
                    } else {
                        holder.ivPhoto.setImageURI(uri)
                    }
                }
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
