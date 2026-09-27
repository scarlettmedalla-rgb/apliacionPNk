package com.example.myapplication.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.model.Ampolleta
import java.io.File

class AmpolletaAdapter(
    private var items: List<Ampolleta>,
    private val onItemClick: ((Ampolleta) -> Unit)? = null
) : RecyclerView.Adapter<AmpolletaAdapter.AmpolletaViewHolder>() {

    class AmpolletaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivPhoto: ImageView = itemView.findViewById(R.id.iv_ampolleta_photo)
        val tvName: TextView = itemView.findViewById(R.id.tv_ampolleta_name)
        val tvStatus: TextView = itemView.findViewById(R.id.tv_ampolleta_status)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AmpolletaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ampolleta, parent, false)
        return AmpolletaViewHolder(view)
    }

    override fun onBindViewHolder(holder: AmpolletaViewHolder, position: Int) {
        val item = items[position]
        holder.tvName.text = item.name
        holder.tvStatus.text = item.status

        if (!item.imageUri.isNullOrEmpty()) {
            try {
                val file = File(item.imageUri)
                if (file.exists()) {
                    holder.ivPhoto.setImageURI(Uri.fromFile(file))
                    holder.ivPhoto.clearColorFilter()
                } else {
                    holder.ivPhoto.setImageURI(Uri.parse(item.imageUri))
                    holder.ivPhoto.clearColorFilter()
                }
            } catch (e: Exception) {
                holder.ivPhoto.setImageResource(item.imageResId ?: R.drawable.ic_sun)
            }
        } else if (item.imageResId != null) {
            holder.ivPhoto.setImageResource(item.imageResId)
            holder.ivPhoto.clearColorFilter()
        } else {
            holder.ivPhoto.setImageResource(R.drawable.ic_sun)
        }

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(item)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<Ampolleta>) {
        items = newItems
        notifyDataSetChanged()
    }
}
