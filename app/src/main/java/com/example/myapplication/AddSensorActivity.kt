package com.example.myapplication

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.db.DatabaseHelper
import com.example.myapplication.model.Sensor
import com.example.myapplication.util.EdgeToEdgeHelper
import com.example.myapplication.util.SweetAlertHelper
import com.google.android.material.button.MaterialButton

class AddSensorActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        EdgeToEdgeHelper.applyTransparentSystemBars(this, isLightStatusBar = true, isLightNavBar = false)
        setContentView(R.layout.activity_add_sensor)

        dbHelper = DatabaseHelper(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.add_sensor_scroll)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        val btnBack = findViewById<ImageButton>(R.id.btn_add_sensor_back)
        btnBack.setOnClickListener { finish() }

        val etName = findViewById<EditText>(R.id.et_sensor_name)
        val etLocation = findViewById<EditText>(R.id.et_sensor_location)
        val etTemp = findViewById<EditText>(R.id.et_sensor_temp)
        val etHumidity = findViewById<EditText>(R.id.et_sensor_humidity)
        val btnSave = findViewById<MaterialButton>(R.id.btn_save_sensor)

        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            val loc = etLocation.text.toString().trim()
            val temp = etTemp.text.toString().trim()
            var hum = etHumidity.text.toString().trim()

            if (name.isEmpty() || loc.isEmpty() || temp.isEmpty()) {
                SweetAlertHelper.showError(this, "Campos Obligatorios", "Por favor ingresa el Nombre, Ubicación y Temperatura del sensor.")
                return@setOnClickListener
            }

            if (hum.isEmpty()) {
                hum = "55%"
            } else if (!hum.endsWith("%")) {
                hum = "$hum%"
            }

            val newSensor = Sensor(
                name = name,
                location = loc,
                temperature = temp,
                humidity = hum,
                date = "",
                isThermometerOn = true
            )

            val result = dbHelper.insertSensor(newSensor)
            if (result != -1L) {
                SweetAlertHelper.showSuccess(
                    context = this,
                    title = "¡Sensor Registrado!",
                    message = "El nuevo sensor $name se ha integrado con éxito a la red IoT."
                ) {
                    finish()
                }
            } else {
                SweetAlertHelper.showError(this, "Error de Inserción", "No se pudo registrar el sensor en la base de datos.")
            }
        }
    }
}
