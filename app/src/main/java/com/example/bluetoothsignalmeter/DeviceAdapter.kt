package com.example.bluetoothsignalmeter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bluetoothsignalmeter.databinding.ItemDeviceBinding

class DeviceAdapter : RecyclerView.Adapter<DeviceAdapter.VH>() {

    private val data = mutableListOf<Pair<String, Int>>()

    fun update(map: Map<String, Int>) {
        data.clear()
        data.addAll(map.entries.map { it.toPair() })
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
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
}
