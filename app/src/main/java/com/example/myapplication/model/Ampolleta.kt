package com.example.myapplication.model

data class Ampolleta(
    val id: Int,
    val name: String,
    val status: String,
    val imageResId: Int? = null,
    val imageUri: String? = null
)
