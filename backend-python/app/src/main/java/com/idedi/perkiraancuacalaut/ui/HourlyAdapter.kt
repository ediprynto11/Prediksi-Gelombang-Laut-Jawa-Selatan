package com.idedi.perkiraancuacalaut.ui

import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.idedi.perkiraancuacalaut.data.model.HourlyPrediction
import com.idedi.perkiraancuacalaut.databinding.ItemHourBinding
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class HourlyAdapter : ListAdapter<HourlyPrediction, HourlyAdapter.VH>(DiffCallback()) {

    inner class VH(private val binding: ItemHourBinding) : RecyclerView.ViewHolder(binding.root) {
        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(item: HourlyPrediction) {
            Log.d("ADAPTER_TIMESTAMP", "timestamp=${item.timestamp}")
            // Format timestamp dengan aman
            val formattedTime = try {
                val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
                val dateTime = LocalDateTime.parse(item.timestamp, formatter)
                dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
            } catch (e: Exception) {
                item.timestamp.takeLast(5)
            }

            binding.tvTime.text = formattedTime
            binding.tvWave.text = "Tinggi gelombang: %.1f m".format(item.wave_height_m)
            binding.tvWind.text = "Kecepatan angin: %.1f kt".format(item.wind_speed_mps)
            // Tidak ada tvRain karena layout tidak menyediakannya
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemHourBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    private class DiffCallback : DiffUtil.ItemCallback<HourlyPrediction>() {
        override fun areItemsTheSame(oldItem: HourlyPrediction, newItem: HourlyPrediction): Boolean {
            return oldItem.timestamp == newItem.timestamp
        }

        override fun areContentsTheSame(oldItem: HourlyPrediction, newItem: HourlyPrediction): Boolean {
            return oldItem == newItem
        }
    }
}
