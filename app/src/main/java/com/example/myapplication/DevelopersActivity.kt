package com.example.myapplication

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.adapter.DeveloperAdapter
import com.example.myapplication.model.Developer

class DevelopersActivity : AppCompatActivity() {

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

        val rvDevs = findViewById<RecyclerView>(R.id.rv_devs_list)
        rvDevs.layoutManager = LinearLayoutManager(this)

        val devList = listOf(
            Developer(
                id = 1,
                name = "JORGE LUIS CORTÉS GALLARDO",
                role = "Lead Software Engineer • Fullstack Developer",
                email = "jorge.cortes@institucion.cl",
                github = "github.com/jorgecortes",
                description = "Institución: Desarrollo de Aplicaciones Móviles IoT\nCarrera: Ingeniería en Informática / Sección: 001D"
            ),
            Developer(
                id = 2,
                name = "KEVIN ENCINA MOLINA",
                role = "Software Architect • Backend & Mobile",
                email = "kevin.encina@institucion.cl",
                github = "github.com/kevinencina",
                description = "Institución: Desarrollo de Aplicaciones Móviles IoT\nCarrera: Ingeniería en Informática / Sección: 001D"
            ),
            Developer(
                id = 3,
                name = "SCARLETT WILLIAMS MEDALLA",
                role = "UI/UX & Mobile Developer",
                email = "scarlett.williams@institucion.cl",
                github = "github.com/scarlettwilliams",
                description = "Institución: Desarrollo de Aplicaciones Móviles IoT\nCarrera: Ingeniería en Informática / Sección: 001D"
            )
        )

        rvDevs.adapter = DeveloperAdapter(devList)
    }
}
