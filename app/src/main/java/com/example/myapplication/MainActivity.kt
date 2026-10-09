package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.myapplication.adapter.AmpolletaAdapter
import com.example.myapplication.adapter.TeamDevAdapter
import com.example.myapplication.db.DatabaseHelper
import com.example.myapplication.model.Ampolleta
import com.example.myapplication.util.DeveloperHelper
import com.example.myapplication.util.EdgeToEdgeHelper
import com.example.myapplication.util.FlashlightManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.switchmaterial.SwitchMaterial
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var tvGreetingTime: TextView
    private lateinit var ivWeatherIcon: ImageView
    private lateinit var carouselAdapter: CarouselSensorAdapter

    private lateinit var switchFlashlight: SwitchMaterial
    private lateinit var tvFlashStatus: TextView
    private lateinit var rvAmpolletas: RecyclerView
    private lateinit var ampolletaAdapter: AmpolletaAdapter

    private lateinit var rvTeamDevs: RecyclerView
    private lateinit var teamDevAdapter: TeamDevAdapter

    private var tvPinkHeroTimeMain: TextView? = null
    private var tvPinkHeroTempMain: TextView? = null
    private var tvPinkHeroHumMain: TextView? = null

    private lateinit var datos: RequestQueue
    private val mHandler = Handler(Looper.getMainLooper())

    private val refrescar = object : Runnable {
        override fun run() {
            obtenerDatosApi()
            mHandler.postDelayed(this, 5000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        EdgeToEdgeHelper.applyTransparentSystemBars(this, isLightStatusBar = true, isLightNavBar = false)
        setContentView(R.layout.activity_main)

        dbHelper = DatabaseHelper(this)
        datos = Volley.newRequestQueue(this)

        val cardBottomNavBar = findViewById<MaterialCardView>(R.id.card_bottom_nav_bar)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_coordinator)) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            findViewById<View>(R.id.main_scroll)?.setPadding(0, systemBars.top, 0, 0)

            val layoutParams = cardBottomNavBar?.layoutParams as? ViewGroup.MarginLayoutParams
            if (layoutParams != null) {
                val density = resources.displayMetrics.density
                val baseMargin = (16 * density).toInt()
                layoutParams.bottomMargin = baseMargin + systemBars.bottom
                cardBottomNavBar.layoutParams = layoutParams
            }
            insets
        }

        tvGreetingTime = findViewById(R.id.tv_greeting_time)
        ivWeatherIcon = findViewById(R.id.iv_weather_icon)

        tvPinkHeroTimeMain = findViewById(R.id.tv_pink_hero_time_main)
        tvPinkHeroTempMain = findViewById(R.id.tv_pink_hero_temp_main)
        tvPinkHeroHumMain = findViewById(R.id.tv_pink_hero_hum_main)

        switchFlashlight = findViewById(R.id.switch_flashlight)
        tvFlashStatus = findViewById(R.id.tv_flash_status)

        val btnHeroSensors = findViewById<MaterialButton>(R.id.btn_hero_sensors)

        // 1. Sensores por Localidad Carousel
        val rvCarousel = findViewById<RecyclerView>(R.id.rv_sensors_carousel)
        rvCarousel.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        val initialCarouselData = listOf(
            CarouselSensorItem("Tierras Blancas", "19.4 °C", "Humedad 68% • Operativo"),
            CarouselSensorItem("La Serena Centro", "21.2 °C", "Humedad 62% • Operativo"),
            CarouselSensorItem("Ovalle Central", "23.5 °C", "Humedad 58% • Operativo"),
            CarouselSensorItem("Coquimbo Costa", "17.8 °C", "Humedad 75% • Operativo"),
            CarouselSensorItem("Vicuña / Elqui", "22.0 °C", "Humedad 52% • Operativo")
        )

        carouselAdapter = CarouselSensorAdapter(initialCarouselData) {
            val intent = Intent(this, SensorsActivity::class.java)
            startActivity(intent)
        }
        rvCarousel.adapter = carouselAdapter

        // 2. Section "Mis Ampolletas"
        rvAmpolletas = findViewById(R.id.rv_ampolletas)
        rvAmpolletas.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        val sampleAmpolletas = listOf(
            Ampolleta(1, "Ampolleta Principal", "Encendida • 100%", imageResId = R.drawable.ic_lightbulb_on),
            Ampolleta(2, "Luz de Lectura", "Apagada • Operativa", imageResId = R.drawable.ic_lightbulb_off),
            Ampolleta(3, "Luz Inteligente", "Encendida • 80%", imageResId = R.drawable.ic_lightbulb_on)
        )
        ampolletaAdapter = AmpolletaAdapter(sampleAmpolletas) { ampolleta ->
            // Requerimiento 11: Alternar ampolleta visualmente
            val isOn = ampolleta.imageResId == R.drawable.ic_lightbulb_on
            val newState = if (isOn) "Apagada • Standby" else "Encendida • 100%"
            val newIcon = if (isOn) R.drawable.ic_lightbulb_off else R.drawable.ic_lightbulb_on
            val updatedAmpolleta = ampolleta.copy(status = newState, imageResId = newIcon)
            
            val updatedList = ampolletaAdapter.getItems().map { 
                if (it.id == ampolleta.id) updatedAmpolleta else it 
            }
            ampolletaAdapter.updateData(updatedList)
        }
        rvAmpolletas.adapter = ampolletaAdapter

        // 3. Section "Desarrolladores Pnk" (Vertical List matching User Cards)
        rvTeamDevs = findViewById(R.id.rv_team_devs)
        rvTeamDevs.layoutManager = LinearLayoutManager(this)

        teamDevAdapter = TeamDevAdapter(DeveloperHelper.getDevelopers(this)) { dev ->
            val intent = Intent(this, DeveloperProfileActivity::class.java)
            intent.putExtra("DEV_ID", dev.id)
            startActivity(intent)
        }
        rvTeamDevs.adapter = teamDevAdapter

        // 4. Flashlight Switch behavior
        switchFlashlight.isChecked = FlashlightManager.isTorchOn()
        updateFlashStatusText(FlashlightManager.isTorchOn())

        switchFlashlight.setOnCheckedChangeListener { _, isChecked ->
            handleFlashToggle(isChecked)
        }

        val navItemHome = findViewById<TextView>(R.id.nav_item_home)
        val navItemUsers = findViewById<TextView>(R.id.nav_item_users)
        val navItemSensors = findViewById<TextView>(R.id.nav_item_sensors)
        val navItemSettings = findViewById<TextView>(R.id.nav_item_settings)

        val fabAddNew = findViewById<FloatingActionButton>(R.id.fab_add_new)

        // Set Dynamic Time Greeting and Weather Icon
        updateGreetingAndWeather(tvGreetingTime, ivWeatherIcon)

        // Start silent API updates for home screen cards
        mHandler.post(refrescar)

        // Hero Card Button
        btnHeroSensors.setOnClickListener {
            val intent = Intent(this, SensorsActivity::class.java)
            startActivity(intent)
        }

        // FAB + Click: Dedicated Dialog Sheet
        fabAddNew.setOnClickListener {
            showAddOptionDialog()
        }

        // Bottom Navigation Bar Item Clicks & Styling
        highlightHomeTab(navItemHome, navItemUsers, navItemSensors, navItemSettings)

        navItemHome.setOnClickListener {
            // Already on home screen
        }

        navItemUsers.setOnClickListener {
            val intent = Intent(this, UserListActivity::class.java)
            startActivity(intent)
        }

        navItemSensors.setOnClickListener {
            val intent = Intent(this, SensorsActivity::class.java)
            startActivity(intent)
        }

        navItemSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun handleFlashToggle(isChecked: Boolean) {
        if (isChecked) {
            val success = FlashlightManager.toggleFlashlight(this)
            if (success) {
                updateFlashStatusText(true)
            } else {
                switchFlashlight.setOnCheckedChangeListener(null)
                switchFlashlight.isChecked = false
                updateFlashStatusText(false)
                switchFlashlight.setOnCheckedChangeListener { _, checked ->
                    handleFlashToggle(checked)
                }
            }
        } else {
            FlashlightManager.turnOff(this)
            updateFlashStatusText(false)
        }
    }

    private fun updateFlashStatusText(isOn: Boolean) {
        if (isOn) {
            tvFlashStatus.text = "Encendida • Activa"
            tvFlashStatus.setTextColor(ContextCompat.getColor(this, R.color.pink_primary))
        } else {
            tvFlashStatus.text = "Apagada"
            tvFlashStatus.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
        }
    }

    private fun highlightHomeTab(
        home: TextView,
        users: TextView,
        sensors: TextView,
        settings: TextView
    ) {
        val pinkColor = ContextCompat.getColor(this, R.color.pink_primary)
        val secondaryColor = ContextCompat.getColor(this, R.color.text_secondary)

        home.setTextColor(pinkColor)
        home.compoundDrawableTintList = ContextCompat.getColorStateList(this, R.color.pink_primary)

        users.setTextColor(secondaryColor)
        users.compoundDrawableTintList = ContextCompat.getColorStateList(this, R.color.text_secondary)

        sensors.setTextColor(secondaryColor)
        sensors.compoundDrawableTintList = ContextCompat.getColorStateList(this, R.color.text_secondary)

        settings.setTextColor(secondaryColor)
        settings.compoundDrawableTintList = ContextCompat.getColorStateList(this, R.color.text_secondary)
    }

    override fun onResume() {
        super.onResume()
        if (::teamDevAdapter.isInitialized) {
            teamDevAdapter.updateData(DeveloperHelper.getDevelopers(this))
        }
    }

    private fun obtenerDatosApi() {
        val url = "https://www.pnk.cl/muestra_datos.php"
        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response: JSONObject ->
                try {
                    val tempStr = response.optString("temperatura", "21.5")
                    val humStr = response.optString("humedad", "58%")

                    // Connect ALL sensors to the live reading in DB
                    dbHelper.updateAllActiveSensorsFromApi(tempStr, humStr)

                    val sdf = SimpleDateFormat("dd/MM/yyyy - HH:mm:ss", Locale.getDefault())
                    val nowStr = sdf.format(Date())

                    // Live Time & API Metrics in Hero Pink Card
                    tvPinkHeroTimeMain?.text = "Hora Actual: $nowStr"
                    tvPinkHeroTempMain?.text = "$tempStr °C"
                    tvPinkHeroHumMain?.text = "$humStr %"

                    // Update Carousel items (Sensores por Localidad)
                    val liveCarouselItems = listOf(
                        CarouselSensorItem("Ovalle Central", "$tempStr °C", "Humedad $humStr% • Operativo"),
                        CarouselSensorItem("Coquimbo Costa", "$tempStr °C", "Humedad $humStr% • Operativo"),
                        CarouselSensorItem("La Serena Centro", "$tempStr °C", "Humedad $humStr% • Operativo"),
                        CarouselSensorItem("Tierras Blancas", "$tempStr °C", "Humedad $humStr% • Operativo"),
                        CarouselSensorItem("Vicuña / Elqui", "$tempStr °C", "Humedad $humStr% • Operativo")
                    )
                    carouselAdapter.updateData(liveCarouselItems)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            { error: VolleyError ->
                error.printStackTrace()
            }
        )
        datos.add(request)
    }

    override fun onDestroy() {
        super.onDestroy()
        mHandler.removeCallbacks(refrescar)
    }

    private fun showAddOptionDialog() {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_option, null)
        val dialog = AlertDialog.Builder(this)
            .setView(view)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnClose = view.findViewById<ImageButton>(R.id.btn_dialog_close)
        val btnAddUser = view.findViewById<MaterialButton>(R.id.btn_dialog_add_user)
        val btnAddSensor = view.findViewById<MaterialButton>(R.id.btn_dialog_add_sensor)

        btnClose?.setOnClickListener {
            dialog.dismiss()
        }

        btnAddUser?.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, AddUserActivity::class.java)
            startActivity(intent)
        }

        btnAddSensor?.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, AddSensorActivity::class.java)
            startActivity(intent)
        }

        dialog.show()
    }

    private fun updateGreetingAndWeather(tvGreetingTime: TextView, ivIcon: ImageView) {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..11 -> {
                tvGreetingTime.text = "Buenos días, SmartTemp"
                ivIcon.setImageResource(R.drawable.ic_sun)
            }
            in 12..19 -> {
                tvGreetingTime.text = "Buenas tardes, SmartTemp"
                ivIcon.setImageResource(R.drawable.ic_sun_cloud)
            }
            else -> {
                tvGreetingTime.text = "Buenas noches, SmartTemp"
                ivIcon.setImageResource(R.drawable.ic_moon)
            }
        }
    }

    private data class CarouselSensorItem(
        val location: String,
        val temp: String,
        val humidity: String
    )

    private class CarouselSensorAdapter(
        private var items: List<CarouselSensorItem>,
        private val onItemClick: () -> Unit
    ) : RecyclerView.Adapter<CarouselSensorAdapter.ViewHolder>() {

        fun updateData(newItems: List<CarouselSensorItem>) {
            items = newItems
            notifyDataSetChanged()
        }

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvLocation: TextView = view.findViewById(R.id.tv_carousel_location)
            val tvTemp: TextView = view.findViewById(R.id.tv_carousel_temp)
            val tvHumidity: TextView = view.findViewById(R.id.tv_carousel_humidity)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_carousel_sensor, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.tvLocation.text = item.location
            holder.tvTemp.text = item.temp
            holder.tvHumidity.text = item.humidity
            holder.itemView.setOnClickListener { onItemClick() }
        }

        override fun getItemCount(): Int = items.size
    }
}
