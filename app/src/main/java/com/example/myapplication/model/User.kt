package com.example.myapplication.model

data class User(
    val id: Int = 0,
    val rut: String,
    val firstname: String,
    val lastname: String,
    val email: String,
    val phone: String,
    val password: String = "",
    val role: String = "Usuario",
    val profilePhoto: String? = null
) {
    val fullName: String
        get() = "$firstname $lastname".trim()
}
