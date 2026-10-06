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
import com.example.myapplication.util.DeveloperHelper

class DevelopersFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_developers, container, false)

        val rvDevelopers = view.findViewById<RecyclerView>(R.id.rv_developers)
        rvDevelopers.layoutManager = LinearLayoutManager(requireContext())

        val devList = DeveloperHelper.getDevelopers(requireContext())

        val adapter = DeveloperAdapter(devList)
        rvDevelopers.adapter = adapter

        return view
    }
}
