package com.example.myapplication.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.adapter.DeveloperAdapter
import com.example.myapplication.model.Developer

class DevelopersFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_developers, container, false)

        val rvDevelopers = view.findViewById<RecyclerView>(R.id.rv_developers)
        rvDevelopers.layoutManager = LinearLayoutManager(requireContext())

        val devList = listOf(
            Developer(
                id = 1,
                name = "Kevin Encina Molina",
                role = "Ingeniero Informático • Lead Systems Architect",
                email = "kevin.encina@thermosense.cl",
                github = "github.com/kevinencina",
                description = "Especialista en arquitectura de software, procesamiento de datos en tiempo real para sensores térmicos e integración de sistemas embebidos."
            ),
            Developer(
                id = 2,
                name = "Scarlett Williams Medalla",
                role = "Ingeniera Informática • Mobile & UI/UX Lead",
                email = "scarlett.williams@thermosense.cl",
                github = "github.com/scarlettwilliams",
                description = "Especialista en desarrollo móvil nativo con Kotlin, diseño de experiencias de usuario en interfaz rosado/negro y gestión de datos SQLite/Room."
            )
        )

        val adapter = DeveloperAdapter(devList)
        rvDevelopers.adapter = adapter

        return view
    }
}
