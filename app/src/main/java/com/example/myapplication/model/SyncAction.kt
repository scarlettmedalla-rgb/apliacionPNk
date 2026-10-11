package com.example.myapplication.model

/**
 * Modelo para las acciones de sincronización encoladas en SQLite
 * Criterios 11, 12 y 13: Cola de cambios pendientes sin conexión
 */
data class SyncAction(
    val id: Int = 0,
    val action: String, // "INSERT", "UPDATE", "DELETE"
    val recordId: Int,
    val payload: String, // JSON con los datos del registro
    val timestamp: String = ""
)
