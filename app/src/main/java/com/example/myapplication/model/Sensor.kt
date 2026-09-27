package com.example.myapplication.model

data class Sensor(
    val id: Int = 0,
    val name: String,
    val location: String,
    val temperature: String,
    val humidity: String,
    val date: String,
    val isThermometerOn: Boolean = true
)
