package com.example.myapplication.util

import android.content.Context
import com.example.myapplication.R
import com.example.myapplication.model.Developer
import java.io.File

object DeveloperHelper {

    fun getDevelopers(context: Context): List<Developer> {
        val prefs = context.getSharedPreferences("smarttemp_dev_photos", Context.MODE_PRIVATE)

        val defaultJorgePhoto = "jorge_cortes"
        val defaultKevinPhoto = "kevin_encina"
        val defaultScarlettPhoto = "scarlett_williams"

        val savedJorgePhoto = prefs.getString("dev_photo_1", null)
        val jorgePhoto = if (!savedJorgePhoto.isNullOrEmpty() && File(savedJorgePhoto).exists()) {
            savedJorgePhoto
        } else {
            defaultJorgePhoto
        }

        val savedKevinPhoto = prefs.getString("dev_photo_2", null)
        val kevinPhoto = if (!savedKevinPhoto.isNullOrEmpty() && File(savedKevinPhoto).exists()) {
            savedKevinPhoto
        } else {
            defaultKevinPhoto
        }

        val savedScarlettPhoto = prefs.getString("dev_photo_3", null)
        val scarlettPhoto = if (!savedScarlettPhoto.isNullOrEmpty() && File(savedScarlettPhoto).exists()) {
            savedScarlettPhoto
        } else {
            defaultScarlettPhoto
        }

        return listOf(
            Developer(
                id = 1,
                name = "Jorge Cortés",
                role = "Lead Developer",
                email = "jorge.cortes@empresa.com",
                github = "github.com/jorgecortes",
                description = "Institución: Desarrollo de Aplicaciones Móviles IoT\nCarrera: Ingeniería en Informática / Sección: 001D",
                photoUri = jorgePhoto
            ),
            Developer(
                id = 2,
                name = "Kevin Encina",
                role = "Backend & Mobile",
                email = "kevin.encina@empresa.com",
                github = "github.com/kevinencina",
                description = "Institución: Desarrollo de Aplicaciones Móviles IoT\nCarrera: Ingeniería en Informática / Sección: 001D",
                photoUri = kevinPhoto
            ),
            Developer(
                id = 3,
                name = "Scarlett Williams",
                role = "UI/UX & Mobile",
                email = "scarlett.williams@empresa.com",
                github = "github.com/scarlettwilliams",
                description = "Institución: Desarrollo de Aplicaciones Móviles IoT\nCarrera: Ingeniería en Informática / Sección: 001D",
                photoUri = scarlettPhoto
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
