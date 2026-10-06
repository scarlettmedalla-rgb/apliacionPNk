package com.example.myapplication.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.model.Developer
import java.io.File

class DeveloperAdapter(
    private var developers: List<Developer>,
    private val onItemClick: ((Developer) -> Unit)? = null
) : RecyclerView.Adapter<DeveloperAdapter.DevViewHolder>() {

    class DevViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tv_dev_name)
        val tvRole: TextView = itemView.findViewById(R.id.tv_dev_role)
        val tvDesc: TextView = itemView.findViewById(R.id.tv_dev_desc)
        val tvEmail: TextView = itemView.findViewById(R.id.tv_dev_email)
        val tvGithub: TextView = itemView.findViewById(R.id.tv_dev_github)
        val ivAvatar: ImageView = itemView.findViewById(R.id.iv_dev_avatar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DevViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_developer, parent, false)
        return DevViewHolder(view)
    }

    override fun onBindViewHolder(holder: DevViewHolder, position: Int) {
        val dev = developers[position]
        val context = holder.itemView.context

        holder.tvName.text = dev.name
        holder.tvRole.text = dev.role
        holder.tvDesc.text = dev.description
        holder.tvEmail.text = "✉ ${dev.email}"
        holder.tvGithub.text = "🔗 ${dev.github}"

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
            holder.ivAvatar.clearColorFilter()
            holder.ivAvatar.imageTintList = null
            holder.ivAvatar.setPadding(0, 0, 0, 0)
            holder.ivAvatar.scaleType = ImageView.ScaleType.CENTER_CROP

            if (file.exists()) {
                holder.ivAvatar.setImageURI(Uri.fromFile(file))
            } else {
                val uri = Uri.parse(photoUri)
                if (uri.scheme == "android.resource" || uri.scheme == "content" || uri.scheme == "file") {
                    holder.ivAvatar.setImageURI(uri)
                } else {
                    val resId = context.resources.getIdentifier(photoUri, "drawable", context.packageName)
                    if (resId != 0) {
                        holder.ivAvatar.setImageResource(resId)
                    } else {
                        holder.ivAvatar.setImageURI(uri)
                    }
                }
            }
        }

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(dev)
        }
    }

    override fun getItemCount(): Int = developers.size

    fun updateData(newDevs: List<Developer>) {
        developers = newDevs
        notifyDataSetChanged()
    }
}
