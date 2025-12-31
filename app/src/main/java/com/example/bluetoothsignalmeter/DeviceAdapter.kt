package com.example.bluetoothsignalmeter

import android.view.LayoutInflater
<<<<<<< HEAD
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bluetoothsignalmeter.databinding.ItemDeviceBinding

class DeviceAdapter : RecyclerView.Adapter<DeviceAdapter.VH>() {

    private val data = mutableListOf<Pair<String, Int>>()

    fun update(map: Map<String, Int>) {
        data.clear()
        data.addAll(map.entries.map { it.toPair() })
=======
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.graphics.Color

class DeviceAdapter : RecyclerView.Adapter<DeviceAdapter.VH>() {

    private val devices = mutableListOf<BleDevice>()

    fun update(list: List<BleDevice>) {
        devices.clear()
        devices.addAll(list.sortedByDescending { it.rssi })
>>>>>>> 22e59a1 (Udate the bluetooth scan add search option)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
<<<<<<< HEAD
        val binding = ItemDeviceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val (mac, rssi) = data[position]
        holder.binding.txtMac.text = mac
        holder.binding.txtRssi.text = "$rssi dBm"
        holder.binding.progressSignal.progress = (100 + rssi).coerceIn(0, 100)
    }

    override fun getItemCount() = data.size

    class VH(val binding: ItemDeviceBinding) : RecyclerView.ViewHolder(binding.root)
=======
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_device, parent, false)
        return VH(v)
    }

    override fun getItemCount() = devices.size

    override fun onBindViewHolder(h: VH, pos: Int) {
        val d = devices[pos]

        h.name.text = if (d.name.isNullOrEmpty()) "Unknown Device" else d.name
        h.mac.text = d.mac
        h.packet.text = "Type: ${d.packetType}"
        h.company.text = "ID: ${d.companyId ?: "N/A"}"
        h.raw.text = d.rawData

        // Distance
        if (d.distance != null) {
            h.layoutDistance.visibility = View.VISIBLE
            val dist = d.distance!!
            val distText = if (dist < 1) "< 1 m" else "~ ${String.format("%.1f", dist)} m"
            h.distance.text = distText
        } else {
            h.layoutDistance.visibility = View.GONE
        }

        // RSSI
        h.rssi.text = "${d.smoothRssi.toInt()} dBm"
        
        // Dynamic color for RSSI background/badge
        val rssiColor = when {
            d.rssi >= -50 -> Color.parseColor("#00C853") // Strong (Green)
            d.rssi >= -70 -> Color.parseColor("#FFD600") // Moderate (Yellow)
            else -> Color.parseColor("#FF3D00") // Weak (Red)
        }
        
        // IMPORTANT: use .mutate() so we don't change the color for all other items sharing the drawable
        h.rssi.background.mutate().setTint(rssiColor)

        // Beacon Info
        if (d.isIBeacon) {
            h.beacon.text = "iBeacon\nUUID: ${d.uuid}\nMajor: ${d.major}  Minor: ${d.minor}\nTx Power: ${d.txPower}"
            h.beacon.visibility = View.VISIBLE
        } else {
            h.beacon.visibility = View.GONE
        }

        // Analyzer expand / collapse
        // Bind visibility from model
        if (d.isExpanded) {
            h.raw.visibility = View.VISIBLE
            h.toggle.text = "Hide Raw Data"
        } else {
            h.raw.visibility = View.GONE
            h.toggle.text = "Show Raw Data"
        }

        h.toggle.setOnClickListener {
            // Toggle model state
            d.isExpanded = !d.isExpanded
            // Update UI immediately (or could notifyItemChanged)
            if (d.isExpanded) {
                h.raw.visibility = View.VISIBLE
                h.toggle.text = "Hide Raw Data"
            } else {
                h.raw.visibility = View.GONE
                h.toggle.text = "Show Raw Data"
            }
        }
    }

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val name: TextView = v.findViewById(R.id.txtName)
        val mac: TextView = v.findViewById(R.id.txtMac)
        val rssi: TextView = v.findViewById(R.id.txtRssi)
        val packet: TextView = v.findViewById(R.id.txtPacket)
        val company: TextView = v.findViewById(R.id.txtCompany)
        val beacon: TextView = v.findViewById(R.id.txtBeacon)
        val raw: TextView = v.findViewById(R.id.txtRaw)
        val toggle: TextView = v.findViewById(R.id.txtToggle)
        val distance: TextView = v.findViewById(R.id.txtDistance)
        val layoutDistance: LinearLayout = v.findViewById(R.id.layoutDistance)
    }
>>>>>>> 22e59a1 (Udate the bluetooth scan add search option)
}
