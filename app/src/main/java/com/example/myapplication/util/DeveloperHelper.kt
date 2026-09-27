package com.example.myapplication.util

import android.content.Context
import com.example.myapplication.model.Developer

object DeveloperHelper {

    fun getDevelopers(context: Context): List<Developer> {
        val prefs = context.getSharedPreferences("smarttemp_dev_photos", Context.MODE_PRIVATE)
        return listOf(
            Developer(
                id = 1,
                name = "Jorge Cortés",
                role = "Lead Developer",
                email = "jorge.cortes@empresa.com",
                github = "github.com/jorgecortes",
                description = "Institución: Desarrollo de Aplicaciones Móviles IoT\nCarrera: Ingeniería en Informática / Sección: 001D",
                photoUri = prefs.getString("dev_photo_1", null)
            ),
            Developer(
                id = 2,
                name = "Kevin Encina",
                role = "Backend & Mobile",
                email = "kevin.encina@empresa.com",
                github = "github.com/kevinencina",
                description = "Institución: Desarrollo de Aplicaciones Móviles IoT\nCarrera: Ingeniería en Informática / Sección: 001D",
                photoUri = prefs.getString("dev_photo_2", null)
            ),
            Developer(
                id = 3,
                name = "Scarlett Williams",
                role = "UI/UX & Mobile",
                email = "scarlett.williams@empresa.com",
                github = "github.com/scarlettwilliams",
                description = "Institución: Desarrollo de Aplicaciones Móviles IoT\nCarrera: Ingeniería en Informática / Sección: 001D",
                photoUri = prefs.getString("dev_photo_3", null)
            )
        )
    }

    fun getDeveloperById(context: Context, id: Int): Developer? {
        return getDevelopers(context).find { it.id == id }
    }

    fun saveDeveloperPhoto(context: Context, id: Int, photoPath: String) {
        val prefs = context.getSharedPreferences("smarttemp_dev_photos", Context.MODE_PRIVATE)
        prefs.edit().putString("dev_photo_$id", photoPath).apply()
    }
}
