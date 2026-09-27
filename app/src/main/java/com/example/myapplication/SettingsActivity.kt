package com.example.myapplication

import android.content.Context
import android.os.Bundle
import android.widget.ImageButton
import android.widget.RelativeLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.util.EdgeToEdgeHelper
import com.example.myapplication.util.SweetAlertHelper
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        EdgeToEdgeHelper.applyTransparentSystemBars(this, isLightStatusBar = true, isLightNavBar = false)
        setContentView(R.layout.activity_settings)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.settings_scroll)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                0
            )
            insets
        }

        val btnBack = findViewById<ImageButton>(R.id.btn_settings_back)
        val switchNotifications = findViewById<SwitchMaterial>(R.id.switch_notifications)
        val switchDarkMode = findViewById<SwitchMaterial>(R.id.switch_dark_mode)
        val btnLanguage = findViewById<RelativeLayout>(R.id.btn_setting_language)
        val btnAbout = findViewById<RelativeLayout>(R.id.btn_setting_about)
        val btnClearCache = findViewById<MaterialButton>(R.id.btn_clear_cache)

        btnBack.setOnClickListener { finish() }

        // Load Preferences
        val prefs = getSharedPreferences("smarttemp_settings", Context.MODE_PRIVATE)
        val isDarkModeOn = prefs.getBoolean("dark_mode", false)
        switchDarkMode.isChecked = isDarkModeOn

        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            val status = if (isChecked) "ACTIVADAS" else "DESACTIVADAS"
            SweetAlertHelper.showSuccess(this, "Ajuste Guardado", "Notificaciones IoT $status.")
        }

        // Functional Dark Mode Switch Toggle
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("dark_mode", isChecked).apply()

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        btnLanguage.setOnClickListener {
            SweetAlertHelper.showSuccess(this, "Idioma", "Idioma configurado en Español (Chile).")
        }

        btnAbout.setOnClickListener {
            SweetAlertHelper.showSuccess(
                this,
                "SmartTemp v1.0",
                "Aplicación Móvil para Monitoreo de Sensores Térmicos IoT.\nDesarrollado para la Evaluación de Aplicaciones Móviles."
            )
        }

        btnClearCache.setOnClickListener {
            SweetAlertHelper.showConfirmation(
                context = this,
                title = "¿Limpiar Caché?",
                message = "¿Deseas eliminar la memoria caché temporal de lecturas?",
                confirmText = "Sí, Limpiar",
                cancelText = "Cancelar",
                onConfirm = {
                    SweetAlertHelper.showSuccess(this, "Caché Limpia", "Memoria caché de sensores liberada.")
                }
            )
        }
    }

    override fun dispatchTouchEvent(ev: android.view.MotionEvent): Boolean {
        if (com.example.myapplication.util.SwipeBackHelper.processDispatchTouchEvent(this, ev)) {
            return true
        }
        return super.dispatchTouchEvent(ev)
    }
}
