package com.example.myapplication.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.myapplication.R
import com.example.myapplication.adapter.SensorAdapter
import com.example.myapplication.db.DatabaseHelper
import com.example.myapplication.model.Sensor
import com.example.myapplication.util.SweetAlertHelper
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SensorsFragment : Fragment() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var sensorAdapter: SensorAdapter
    private lateinit var rvSensors: RecyclerView

    private var tvPinkHeroTime: TextView? = null
    private var tvPinkHeroTemp: TextView? = null
    private var tvPinkHeroHum: TextView? = null

    private lateinit var datos: RequestQueue
    private val mHandler = Handler(Looper.getMainLooper())

    private val refrescar = object : Runnable {
        override fun run() {
            obtenerDatos()
            mHandler.postDelayed(this, 1000)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sensors, container, false)

        dbHelper = DatabaseHelper(requireContext())

        rvSensors = view.findViewById(R.id.rv_sensors)
        rvSensors.layoutManager = LinearLayoutManager(requireContext())

        tvPinkHeroTime = view.findViewById(R.id.tv_pink_hero_time)
        tvPinkHeroTemp = view.findViewById(R.id.tv_pink_hero_temp)
        tvPinkHeroHum = view.findViewById(R.id.tv_pink_hero_hum)

        // Initialize Volley RequestQueue
        datos = Volley.newRequestQueue(requireContext())

        // Start silent auto refresh for all sensors
        mHandler.post(refrescar)

        sensorAdapter = SensorAdapter(
            sensors = emptyList(),
            onToggleThermometer = { sensor ->
                dbHelper.toggleThermometer(sensor.id, !sensor.isThermometerOn)
                loadSensors()
                val statusStr = if (!sensor.isThermometerOn) "Encendido" else "Apagado"
                SweetAlertHelper.showSuccess(
                    requireContext(),
                    "Termómetro $statusStr",
                    "El estado del sensor ha sido cambiado a $statusStr."
                )
            },
            onEditClick = { sensor -> showSensorDialog(sensor) },
            onDeleteClick = { sensor -> showDeleteConfirmation(sensor) }
        )
        rvSensors.adapter = sensorAdapter

        val fabAdd = view.findViewById<FloatingActionButton>(R.id.fab_add_sensor)
        fabAdd.setOnClickListener {
            showSensorDialog(null)
        }

        loadSensors()

        return view
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

                    // Live Time in Hero Pink Card
                    tvPinkHeroTime?.text = "Hora Actual: ${fechahora()}"

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

    override fun onDestroyView() {
        super.onDestroyView()
        mHandler.removeCallbacks(refrescar)
    }

    private fun loadSensors() {
        val sensors = dbHelper.getAllSensors()
        sensorAdapter.updateData(sensors)
        updateDashboardStats(sensors)
    }

    private fun updateDashboardStats(sensors: List<Sensor>) {
        if (sensors.isEmpty()) {
            tvPinkHeroTemp?.text = "0.0 °C"
            tvPinkHeroHum?.text = "0 %"
            return
        }

        val activeSensors = sensors.filter { it.isThermometerOn }
        val tempValues = activeSensors.mapNotNull { it.temperature.toDoubleOrNull() }
        val humValues = activeSensors.mapNotNull { it.humidity.replace("%", "").trim().toDoubleOrNull() }

        val avgTemp = if (tempValues.isNotEmpty()) tempValues.average() else 0.0
        val avgHum = if (humValues.isNotEmpty()) humValues.average() else 0.0

        tvPinkHeroTemp?.text = String.format(Locale.US, "%.1f °C", avgTemp)
        tvPinkHeroHum?.text = String.format(Locale.US, "%.1f %%", avgHum)
    }

    private fun showSensorDialog(sensor: Sensor?) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_sensor, null)
        val tvTitle = dialogView.findViewById<TextView>(R.id.tv_dialog_sensor_title)
        val etName = dialogView.findViewById<EditText>(R.id.et_dialog_sensor_name)
        val etLocation = dialogView.findViewById<EditText>(R.id.et_dialog_sensor_location)
        val etTemp = dialogView.findViewById<EditText>(R.id.et_dialog_sensor_temp)
        val etHum = dialogView.findViewById<EditText>(R.id.et_dialog_sensor_hum)

        val isEdit = sensor != null
        if (isEdit && sensor != null) {
            tvTitle.text = getString(R.string.edit_sensor)
            etName.setText(sensor.name)
            etLocation.setText(sensor.location)
            etTemp.setText(sensor.temperature)
            etHum.setText(sensor.humidity)
        } else {
            tvTitle.text = getString(R.string.add_sensor)
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton(R.string.save) { _, _ ->
                val name = etName.text.toString().trim()
                val location = etLocation.text.toString().trim()
                val temp = etTemp.text.toString().trim()
                val hum = etHum.text.toString().trim()

                if (name.isEmpty() || temp.isEmpty()) {
                    SweetAlertHelper.showError(
                        requireContext(),
                        "Error",
                        "Nombre y Temperatura son requeridos."
                    )
                    return@setPositiveButton
                }

                if (isEdit && sensor != null) {
                    val updatedSensor = sensor.copy(
                        name = name,
                        location = location,
                        temperature = temp,
                        humidity = hum
                    )
                    dbHelper.updateSensor(updatedSensor)

                    SweetAlertHelper.showSuccess(
                        requireContext(),
                        "¡Sensor Actualizado!",
                        "Los datos del sensor se guardaron correctamente."
                    )
                } else {
                    val newSensor = Sensor(
                        name = name,
                        location = location,
                        temperature = temp,
                        humidity = if (hum.endsWith("%")) hum else "$hum%",
                        date = "",
                        isThermometerOn = true
                    )
                    dbHelper.insertSensor(newSensor)

                    SweetAlertHelper.showSuccess(
                        requireContext(),
                        "¡Sensor Ingresado!",
                        "El sensor ha sido agregado al sistema."
                    )
                }
                loadSensors()
            }
            .setNegativeButton(R.string.cancel, null)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun showDeleteConfirmation(sensor: Sensor) {
        SweetAlertHelper.showConfirmation(
            context = requireContext(),
            title = "¿Eliminar Sensor?",
            message = "Se eliminará el sensor ${sensor.name} de ${sensor.location}.",
            confirmText = "Sí, Eliminar",
            cancelText = "Cancelar",
            onConfirm = {
                dbHelper.deleteSensor(sensor.id)
                loadSensors()
                SweetAlertHelper.showSuccess(
                    requireContext(),
                    "¡Eliminado!",
                    "El sensor ha sido eliminado del sistema."
                )
            }
        )
    }
}
