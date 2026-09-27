package com.example.myapplication.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.model.Sensor

class SensorAdapter(
    private var sensors: List<Sensor>,
    private val onToggleThermometer: (Sensor) -> Unit,
    private val onEditClick: (Sensor) -> Unit,
    private val onDeleteClick: (Sensor) -> Unit
) : RecyclerView.Adapter<SensorAdapter.SensorViewHolder>() {

    class SensorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tv_sensor_name)
        val tvLocation: TextView = itemView.findViewById(R.id.tv_sensor_location)
        val tvDate: TextView = itemView.findViewById(R.id.tv_sensor_date)
        val tvTemp: TextView = itemView.findViewById(R.id.tv_sensor_temp)
        val tvHumidity: TextView = itemView.findViewById(R.id.tv_sensor_humidity)
        val ivTempIcon: ImageView = itemView.findViewById(R.id.iv_sensor_temp_icon)
        val btnThermometer: ImageButton = itemView.findViewById(R.id.btn_thermometer_toggle)
        val tvThermometerStatus: TextView = itemView.findViewById(R.id.tv_thermometer_status)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btn_edit_sensor)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btn_delete_sensor)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SensorViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sensor, parent, false)
        return SensorViewHolder(view)
    }

    override fun onBindViewHolder(holder: SensorViewHolder, position: Int) {
        val sensor = sensors[position]
        val context = holder.itemView.context

        holder.tvName.text = sensor.name
        holder.tvLocation.text = "Localidad: ${sensor.location}"
        holder.tvDate.text = "Última lectura: ${sensor.date}"
        holder.tvTemp.text = "${sensor.temperature} °C"
        holder.tvHumidity.text = sensor.humidity

        // Dynamic Temperature Color & Thermometer Image (Red >= 20°C / Blue < 20°C)
        val tempVal = sensor.temperature.toFloatOrNull() ?: 0f
        if (tempVal >= 20f) {
            holder.ivTempIcon.setImageResource(R.drawable.tempalta)
            holder.tvTemp.setTextColor(ContextCompat.getColor(context, R.color.pink_primary))
        } else {
            holder.ivTempIcon.setImageResource(R.drawable.tempbaja)
            holder.tvTemp.setTextColor(ContextCompat.getColor(context, R.color.pastel_blue_dark))
        }

        if (sensor.isThermometerOn) {
            holder.btnThermometer.setImageResource(R.drawable.ic_thermometer_on)
            holder.tvThermometerStatus.text = context.getString(R.string.thermometer_on)
            holder.tvThermometerStatus.setTextColor(ContextCompat.getColor(context, R.color.pink_primary))
        } else {
            holder.btnThermometer.setImageResource(R.drawable.ic_thermometer_off)
            holder.tvThermometerStatus.text = context.getString(R.string.thermometer_off)
            holder.tvThermometerStatus.setTextColor(ContextCompat.getColor(context, R.color.text_muted))
        }

        holder.btnThermometer.setOnClickListener {
            holder.btnThermometer.animate()
                .scaleX(1.3f)
                .scaleY(1.3f)
                .setDuration(120)
                .withEndAction {
                    holder.btnThermometer.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(120)
                        .start()
                    onToggleThermometer(sensor)
                }
                .start()
        }

        holder.btnEdit.setOnClickListener { onEditClick(sensor) }
        holder.btnDelete.setOnClickListener { onDeleteClick(sensor) }
    }

    override fun getItemCount(): Int = sensors.size

    fun updateData(newSensors: List<Sensor>) {
        sensors = newSensors
        notifyDataSetChanged()
    }
}
