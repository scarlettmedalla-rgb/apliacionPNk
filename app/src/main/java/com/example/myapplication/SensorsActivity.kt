package com.example.myapplication

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.myapplication.adapter.SensorAdapter
import com.example.myapplication.db.DatabaseHelper
import com.example.myapplication.model.Sensor
import com.example.myapplication.util.EdgeToEdgeHelper
import com.example.myapplication.util.FlashlightManager
import com.example.myapplication.util.SweetAlertHelper
import com.example.myapplication.util.SwipeBackHelper
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SensorsActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var sensorAdapter: SensorAdapter
    private lateinit var rvSensors: RecyclerView

    private var tvPinkHeroTime: TextView? = null
    private var tvPinkHeroTemp: TextView? = null
    private var tvPinkHeroHum: TextView? = null
    private var btnFlashlight: ImageButton? = null

    private lateinit var datos: RequestQueue
    private val mHandler = Handler(Looper.getMainLooper())

    private val refrescar = object : Runnable {
        override fun run() {
            obtenerDatos()
            mHandler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        EdgeToEdgeHelper.applyTransparentSystemBars(this, isLightStatusBar = true, isLightNavBar = false)
        setContentView(R.layout.activity_sensors)

        dbHelper = DatabaseHelper(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.sensors_coordinator)) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            findViewById<android.view.View>(R.id.sensors_scroll)?.setPadding(0, systemBars.top, 0, systemBars.bottom)
            insets
        }

        tvPinkHeroTime = findViewById(R.id.tv_pink_hero_time)
        tvPinkHeroTemp = findViewById(R.id.tv_pink_hero_temp)
        tvPinkHeroHum = findViewById(R.id.tv_pink_hero_hum)
        btnFlashlight = findViewById(R.id.btn_toggle_flashlight)

        val btnAddSensor = findViewById<ImageButton>(R.id.btn_add_sensor_action)
        rvSensors = findViewById(R.id.rv_sensors_list)
        rvSensors.layoutManager = LinearLayoutManager(this)

        val localBtnFlashlight = btnFlashlight
        localBtnFlashlight?.setOnClickListener {
            val success = FlashlightManager.toggleFlashlight(this)
            if (success) {
                updateFlashlightButtonState(localBtnFlashlight, FlashlightManager.isTorchOn())
            }
        }

        btnAddSensor.setOnClickListener {
            showAddSensorDialog()
        }

        // Initialize Volley RequestQueue
        datos = Volley.newRequestQueue(this)

        // Start silent auto refresh for all sensors
        mHandler.post(refrescar)

        loadSensors()
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (SwipeBackHelper.processDispatchTouchEvent(this, ev)) {
            return true
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onResume() {
        super.onResume()
        btnFlashlight?.let {
            updateFlashlightButtonState(it, FlashlightManager.isTorchOn())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mHandler.removeCallbacks(refrescar)
        FlashlightManager.turnOff(this)
    }

    private fun updateFlashlightButtonState(btn: ImageButton, isOn: Boolean) {
        if (isOn) {
            btn.setBackgroundResource(R.drawable.bg_circle_flashlight_on)
        } else {
            btn.setBackgroundResource(R.drawable.bg_circle_flashlight)
        }
    }

    private fun fechahora(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy - HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun obtenerDatos() {
        val url = "https://www.pnk.cl/muestra_datos.php"
        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response: JSONObject ->
                try {
                    val tempStr = response.getString("temperatura")
                    val humStr = response.getString("humedad")

                    // Live Time & API Metrics in Hero Pink Card
                    tvPinkHeroTime?.text = "Hora Actual: ${fechahora()}"
                    tvPinkHeroTemp?.text = "$tempStr °C"
                    tvPinkHeroHum?.text = "$humStr %"

                    // Connect ALL active sensors seamlessly to the live reading
                    dbHelper.updateAllActiveSensorsFromApi(tempStr, humStr)
                    loadSensors()
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            { error: VolleyError ->
                error.printStackTrace()
            }
        )
        datos.add(request)
    }

    private fun loadSensors() {
        val sensors = dbHelper.getAllSensors()
        sensorAdapter = SensorAdapter(
            sensors = sensors,
            onToggleThermometer = { sensor ->
                val newStatus = !sensor.isThermometerOn
                dbHelper.toggleThermometer(sensor.id, newStatus)
                loadSensors()
                SweetAlertHelper.showSuccess(this, "Termostato", "Sensor ${sensor.name} ${if (newStatus) "activado" else "desactivado"}.")
            },
            onEditClick = { sensor ->
                showEditSensorDialog(sensor)
            },
            onDeleteClick = { sensor ->
                SweetAlertHelper.showConfirmation(
                    context = this,
                    title = "¿Eliminar Sensor?",
                    message = "¿Deseas eliminar el sensor ${sensor.name} de la red IoT?",
                    confirmText = "Sí, Eliminar",
                    cancelText = "Cancelar",
                    onConfirm = {
                        dbHelper.deleteSensor(sensor.id)
                        loadSensors()
                        SweetAlertHelper.showSuccess(this, "Sensor Eliminado", "El sensor ha sido removido.")
                    }
                )
            }
        )
        rvSensors.adapter = sensorAdapter
    }

    private fun showAddSensorDialog() {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 24)
        }

        val inputName = EditText(this).apply {
            hint = "Nombre del Sensor (Ej: Sensor Clima Antofagasta)"
            textSize = 14f
        }

        val inputLocation = EditText(this).apply {
            hint = "Localidad / Comuna (Ej: Antofagasta)"
            textSize = 14f
        }

        val inputTemp = EditText(this).apply {
            hint = "Temperatura Inicial (°C, Ej: 21.5)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            textSize = 14f
        }

        val inputHumidity = EditText(this).apply {
            hint = "Humedad Inicial (%, Ej: 60%)"
            textSize = 14f
        }

        container.addView(inputName)
        container.addView(inputLocation)
        container.addView(inputTemp)
        container.addView(inputHumidity)

        AlertDialog.Builder(this)
            .setTitle("Registrar Nuevo Sensor IoT")
            .setView(container)
            .setPositiveButton("Registrar") { _, _ ->
                val name = inputName.text.toString().trim()
                val loc = inputLocation.text.toString().trim()
                val temp = inputTemp.text.toString().trim()
                val hum = inputHumidity.text.toString().trim()

                if (name.isEmpty() || loc.isEmpty() || temp.isEmpty()) {
                    SweetAlertHelper.showError(this, "Campos Vacíos", "Por favor completa el nombre, ubicación y temperatura.")
                    return@setPositiveButton
                }

                val newSensor = Sensor(
                    name = name,
                    location = loc,
                    temperature = temp,
                    humidity = if (hum.isEmpty()) "55%" else hum,
                    date = "",
                    isThermometerOn = true
                )

                dbHelper.insertSensor(newSensor)
                loadSensors()
                SweetAlertHelper.showSuccess(this, "¡Sensor Añadido!", "El nuevo sensor se ha integrado a la red IoT.")
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showEditSensorDialog(sensor: Sensor) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_sensor, null)
        val tvTitle = dialogView.findViewById<TextView>(R.id.tv_dialog_sensor_title)
        val etName = dialogView.findViewById<EditText>(R.id.et_dialog_sensor_name)
        val etLocation = dialogView.findViewById<EditText>(R.id.et_dialog_sensor_location)
        val etTemp = dialogView.findViewById<EditText>(R.id.et_dialog_sensor_temp)
        val etHum = dialogView.findViewById<EditText>(R.id.et_dialog_sensor_hum)

        tvTitle.text = getString(R.string.edit_sensor)
        etName.setText(sensor.name)
        etLocation.setText(sensor.location)
        etTemp.setText(sensor.temperature)
        etHum.setText(sensor.humidity)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val name = etName.text.toString().trim()
                val loc = etLocation.text.toString().trim()
                val temp = etTemp.text.toString().trim()
                val hum = etHum.text.toString().trim()

                if (name.isEmpty() || loc.isEmpty() || temp.isEmpty()) {
                    SweetAlertHelper.showError(this, "Campos Vacíos", "Por favor complete los campos.")
                    return@setPositiveButton
                }

                val updatedSensor = sensor.copy(
                    name = name,
                    location = loc,
                    temperature = temp,
                    humidity = if (hum.isNotEmpty()) hum else sensor.humidity
                )

                dbHelper.updateSensor(updatedSensor)
                loadSensors()
                SweetAlertHelper.showSuccess(this, "¡Actualizado!", "Los datos del sensor fueron modificados.")
            }
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }
}
