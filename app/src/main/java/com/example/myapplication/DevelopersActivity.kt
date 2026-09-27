package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.adapter.DeveloperAdapter
import com.example.myapplication.util.DeveloperHelper
import com.example.myapplication.util.SwipeBackHelper

class DevelopersActivity : AppCompatActivity() {

    private lateinit var rvDevs: RecyclerView
    private lateinit var adapter: DeveloperAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_developers)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.devs_scroll)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val extraBottomPadding = (40 * resources.displayMetrics.density).toInt()
            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom + extraBottomPadding
            )
            insets
        }

        rvDevs = findViewById(R.id.rv_devs_list)
        rvDevs.layoutManager = LinearLayoutManager(this)

        adapter = DeveloperAdapter(emptyList()) { dev ->
            val intent = Intent(this, DeveloperProfileActivity::class.java)
            intent.putExtra("DEV_ID", dev.id)
            startActivity(intent)
        }
        rvDevs.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        val devs = DeveloperHelper.getDevelopers(this)
        adapter.updateData(devs)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (SwipeBackHelper.processDispatchTouchEvent(this, ev)) {
            return true
        }
        return super.dispatchTouchEvent(ev)
    }
}
