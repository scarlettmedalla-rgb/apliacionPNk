package com.example.myapplication.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.model.User
import java.io.File

class UserAdapter(
    private var users: List<User>,
    private val onEditClick: (User) -> Unit,
    private val onDeleteClick: (User) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    private var expandedPosition: Int = -1

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardItem: View = itemView.findViewById(R.id.card_user_item)
        val tvInitial: TextView = itemView.findViewById(R.id.tv_user_initial)
        val ivAvatar: ImageView = itemView.findViewById(R.id.iv_user_avatar)
        val tvName: TextView = itemView.findViewById(R.id.tv_user_name)
        val tvEmail: TextView = itemView.findViewById(R.id.tv_user_email)
        val tvPhone: TextView = itemView.findViewById(R.id.tv_user_phone)

        val tvRut: TextView = itemView.findViewById(R.id.tv_user_rut)
        val tvRole: TextView = itemView.findViewById(R.id.tv_user_role)
        val tvStatus: TextView = itemView.findViewById(R.id.tv_user_status)
        val layoutExpandable: LinearLayout = itemView.findViewById(R.id.layout_expandable_details)
        val ivArrow: ImageView = itemView.findViewById(R.id.iv_expand_arrow)

        val btnEdit: ImageButton = itemView.findViewById(R.id.btn_edit_user)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btn_delete_user)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        val context = holder.itemView.context
        val isExpanded = position == expandedPosition

        val photoToLoad = if (!user.profilePhoto.isNullOrEmpty()) {
            user.profilePhoto
        } else if (user.email.contains("jorge", ignoreCase = true) || user.firstname.contains("Jorge", ignoreCase = true)) {
            "android.resource://${context.packageName}/${R.drawable.jorge_cortes}"
        } else if (user.email.contains("kevin", ignoreCase = true) || user.firstname.contains("Kevin", ignoreCase = true)) {
            "android.resource://${context.packageName}/${R.drawable.kevin_encina}"
        } else if (user.email.contains("scarlett", ignoreCase = true) || user.firstname.contains("Scarlett", ignoreCase = true) || user.firstname.contains("Scar", ignoreCase = true)) {
            "android.resource://${context.packageName}/${R.drawable.scarlett_williams}"
        } else {
            null
        }

        if (!photoToLoad.isNullOrEmpty()) {
            val file = File(photoToLoad)
            holder.ivAvatar.clearColorFilter()
            holder.ivAvatar.imageTintList = null
            holder.ivAvatar.setPadding(0, 0, 0, 0)
            holder.ivAvatar.scaleType = ImageView.ScaleType.CENTER_CROP

            if (file.exists()) {
                holder.ivAvatar.setImageURI(Uri.fromFile(file))
            } else {
                val uri = Uri.parse(photoToLoad)
                if (uri.scheme == "android.resource" || uri.scheme == "content" || uri.scheme == "file") {
                    holder.ivAvatar.setImageURI(uri)
                } else {
                    val resId = context.resources.getIdentifier(photoToLoad, "drawable", context.packageName)
                    if (resId != 0) {
                        holder.ivAvatar.setImageResource(resId)
                    } else {
                        holder.ivAvatar.setImageURI(uri)
                    }
                }
            }
            holder.ivAvatar.visibility = View.VISIBLE
            holder.tvInitial.visibility = View.GONE
        } else {
            holder.ivAvatar.visibility = View.GONE
            holder.tvInitial.visibility = View.VISIBLE
            holder.tvInitial.text = if (user.firstname.isNotEmpty()) user.firstname.take(1).uppercase() else "U"
        }

        holder.tvName.text = user.fullName
        holder.tvEmail.text = user.email
        holder.tvPhone.text = "Tel: ${user.phone}"

        // Expanded Details
        holder.tvRut.text = user.rut
        val userRole = if (user.role.isNotBlank()) user.role else "Usuario"
        holder.tvRole.text = "Rol: $userRole"

        // Estado: Rosado si está activo, Negro si no
        val isActive = true
        if (isActive) {
            holder.tvStatus.text = "Estado: Activo"
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.pink_primary))
        } else {
            holder.tvStatus.text = "Estado: Inactivo"
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.text_dark))
        }

        holder.layoutExpandable.visibility = if (isExpanded) View.VISIBLE else View.GONE
        holder.ivArrow.rotation = if (isExpanded) 180f else 0f

        // Click on card to expand / collapse
        holder.cardItem.setOnClickListener {
            val previousExpanded = expandedPosition
            expandedPosition = if (isExpanded) -1 else position
            if (previousExpanded != -1) notifyItemChanged(previousExpanded)
            if (expandedPosition != -1) notifyItemChanged(expandedPosition)
        }

        holder.btnEdit.visibility = View.GONE
        holder.btnDelete.visibility = View.GONE
    }

    override fun getItemCount(): Int = users.size

    fun updateData(newUsers: List<User>) {
        users = newUsers
        expandedPosition = -1
        notifyDataSetChanged()
    }
}
