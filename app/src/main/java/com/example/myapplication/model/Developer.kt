package com.example.myapplication.model

data class Developer(
    val id: Int,
    val name: String,
    val role: String,
    val email: String,
    val github: String,
    val description: String,
    val photoUri: String? = null
)
