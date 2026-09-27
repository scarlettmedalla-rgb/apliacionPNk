package com.example.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.db.DatabaseHelper
import com.example.myapplication.model.User
import com.example.myapplication.util.EdgeToEdgeHelper
import com.example.myapplication.util.SwipeBackHelper
import java.io.File

class UserListActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var adapter: UserCardAdapter
    private lateinit var rvUsers: RecyclerView
    private lateinit var etSearch: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        EdgeToEdgeHelper.applyTransparentSystemBars(this, isLightStatusBar = true, isLightNavBar = false)
        setContentView(R.layout.activity_user_list)

        dbHelper = DatabaseHelper(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.user_list_coordinator)) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            findViewById<View>(R.id.user_list_main)?.setPadding(0, systemBars.top, 0, systemBars.bottom)
            insets
        }

        val btnBack = findViewById<ImageButton>(R.id.btn_user_list_back)
        btnBack?.setOnClickListener { finish() }

        rvUsers = findViewById(R.id.rv_numbered_users)
        rvUsers.layoutManager = LinearLayoutManager(this)

        etSearch = findViewById(R.id.et_search_user_input)

        adapter = UserCardAdapter(emptyList()) { user ->
            val intent = Intent(this, ModifyUserActivity::class.java)
            intent.putExtra("USER_ID", user.id)
            intent.putExtra("USER_FN", user.firstname)
            intent.putExtra("USER_LN", user.lastname)
            intent.putExtra("USER_EMAIL", user.email)
            startActivity(intent)
        }
        rvUsers.adapter = adapter

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                loadUsers(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (SwipeBackHelper.processDispatchTouchEvent(this, ev)) {
            return true
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onResume() {
        super.onResume()
        loadUsers(etSearch.text.toString())
    }

    private fun loadUsers(query: String) {
        val users = dbHelper.searchUsers(query)
        adapter.updateData(users)
    }

    private class UserCardAdapter(
        private var users: List<User>,
        private val onItemClick: (User) -> Unit
    ) : RecyclerView.Adapter<UserCardAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvName: TextView = view.findViewById(R.id.tv_user_full_name)
            val tvEmail: TextView = view.findViewById(R.id.tv_user_email)
            val ivAvatar: ImageView = view.findViewById(R.id.iv_user_avatar)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user_card, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val user = users[position]
            val context = holder.itemView.context
            holder.tvName.text = user.fullName
            holder.tvEmail.text = user.email

            if (!user.profilePhoto.isNullOrEmpty()) {
                val file = File(user.profilePhoto)
                if (file.exists()) {
                    holder.ivAvatar.clearColorFilter()
                    holder.ivAvatar.setPadding(0, 0, 0, 0)
                    holder.ivAvatar.setImageURI(Uri.fromFile(file))
                } else {
                    holder.ivAvatar.clearColorFilter()
                    holder.ivAvatar.setPadding(0, 0, 0, 0)
                    holder.ivAvatar.setImageURI(Uri.parse(user.profilePhoto))
                }
            } else {
                val density = context.resources.displayMetrics.density
                val p = (8 * density).toInt()
                holder.ivAvatar.setPadding(p, p, p, p)
                holder.ivAvatar.setImageResource(R.drawable.ic_users)
                holder.ivAvatar.setColorFilter(ContextCompat.getColor(context, R.color.pink_primary))
            }

            holder.itemView.setOnClickListener { onItemClick(user) }
        }

        override fun getItemCount(): Int = users.size

        fun updateData(newUsers: List<User>) {
            users = newUsers
            notifyDataSetChanged()
        }
    }
}
