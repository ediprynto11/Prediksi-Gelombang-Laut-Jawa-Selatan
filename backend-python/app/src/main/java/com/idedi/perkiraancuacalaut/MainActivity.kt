//package com.idedi.perkiraancuacalaut
//
//import android.graphics.Color
//import android.os.Build
//import android.os.Bundle
//import android.util.Log
//import android.view.View
//import android.widget.Toast
//import androidx.activity.viewModels
//import androidx.annotation.RequiresApi
//import androidx.appcompat.app.AppCompatActivity
//import androidx.recyclerview.widget.LinearLayoutManager
//import com.github.mikephil.charting.components.Legend
//import com.github.mikephil.charting.data.Entry
//import com.github.mikephil.charting.data.LineData
//import com.github.mikephil.charting.data.LineDataSet
//import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
//import com.github.mikephil.charting.components.XAxis
//import com.idedi.perkiraancuacalaut.data.ForecastRepository
//import com.idedi.perkiraancuacalaut.databinding.ActivityMainBinding
//import com.idedi.perkiraancuacalaut.ui.HourlyAdapter
//import com.idedi.perkiraancuacalaut.ui.MainViewModel
//import com.idedi.perkiraancuacalaut.ui.MainViewModelFactory
//import com.idedi.perkiraancuacalaut.data.model.HourlyPrediction
//import java.time.LocalDate
//import java.time.OffsetDateTime
//import java.time.ZoneId
//import java.time.format.DateTimeFormatter
//import java.util.Locale
//
//class MainActivity : AppCompatActivity() {
//
//    private lateinit var binding: ActivityMainBinding
//
//    private val viewModel: MainViewModel by viewModels {
//        MainViewModelFactory(ForecastRepository())
//    }
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//            binding.tvDay1Label.text = getFormattedDate(1) // Besok
//            binding.tvDay2Label.text = getFormattedDate(2) // Lusa
//            binding.tvDay3Label.text = getFormattedDate(3) // Hari ke-3
//
//        val adapterDay1 = HourlyAdapter() // Besok
//        val adapterDay2 = HourlyAdapter() // Lusa
//        val adapterDay3 = HourlyAdapter() // Hari ketiga
//
//        // === RecyclerView Setup ===
////        val adapter = HourlyAdapter()
//        binding.rvForecast.apply {
//            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
//            adapter = adapterDay1
//            clipToPadding = false
//            setPadding(6, 0, 6, 0)
//        }
//
//        binding.rvDay2Forecast.apply {
//            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
//            adapter = adapterDay2
//            clipToPadding = false
//            setPadding(6, 0, 6, 0)
//        }
//
//        binding.rvDay3Forecast.apply {
//            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
//            adapter = adapterDay3
//            clipToPadding = false
//            setPadding(6, 0, 6, 0)
//        }
//
////        viewModel.forecast.observe(this) { forecast ->
////            if (forecast != null) {
////                binding.tvRegion.text = forecast.region
////                binding.tvSafety.text = forecast.safety.today_model
////
////                // Ambil semua data
////                val allData = forecast.predictions_hourly_72h
////
////                // 🔍 Tambahkan log untuk melihat format timestamp mentah
////                Log.d("CHART_DATA", "Contoh timestamp mentah:")
////                allData.take(5).forEach {
////                    Log.d("CHART_DATA", it.timestamp)
////                }
////
////                val todayList = allData.take(24)
////
////                Log.d("CHART_DATA", "Menampilkan 24 jam pertama dari data:")
////                todayList.forEach {
////                    Log.d("CHART_DATA", "timestamp=${it.timestamp}, wave=${it.wave_height_m}")
////                }
////                // Log hasil filter
////                Log.d("CHART_DATA", "Jumlah data hari ini: ${todayList.size}")
////                todayList.forEach {
////                    Log.d("CHART_DATA", "timestamp=${it.timestamp}, wave=${it.wave_height_m}, wind=${it.wind_speed_mps}")
////                }
////
////                // Kirim ke adapter dan chart
////                adapter.submitList(todayList)
////                updateChart(todayList)
////
////            } else {
////                Toast.makeText(this, "Data kosong", Toast.LENGTH_SHORT).show()
////            }
////        }
//
////        viewModel.forecast.observe(this) { forecast ->
////            if (forecast != null) {
////                binding.tvRegion.text = forecast.region
////                binding.tvSafety.text = forecast.safety.today_model
////
//////                 Ambil semua data
////                val allData = forecast.predictions_hourly_72h
////                val today = java.time.LocalDate.now()
////                //Baru
////                val day1 = today.plusDays(1) // Besok
////                val day2 = today.plusDays(2) // Lusa
////                val day3 = today.plusDays(3) // Hari ketiga
////
//////                val tomorrow = today.plusDays(1)
////
////                // --- Data untuk grafik (hari ini) ---
//////                val todayList = forecast.predictions_hourly_72h.filter {
//////                    it.timestamp.contains(today.toString())
//////                }
////
////                val todayList = allData.filter {
////                    it.timestamp.replace("Z", "").startsWith(today.toString())
////                }
////
////                val day1List = allData.filter {
////                    it.timestamp.replace("Z", "").startsWith(day1.toString())
////                }.filterIndexed { index, _ -> index % 3 == 0 }
////
////                val day2List = allData.filter {
////                    it.timestamp.replace("Z", "").startsWith(day2.toString())
////                }.filterIndexed { index, _ -> index % 3 == 0 }
////
////                val day3List = allData.filter {
////                    it.timestamp.replace("Z", "").startsWith(day3.toString())
////                }.filterIndexed { index, _ -> index % 3 == 0 }
////
////                //Baru
//////                val day1List = allData.filter { it.timestamp.contains(day1.toString()) }
//////                    .filterIndexed { index, _ -> index % 3 == 0 }
//////
//////                val day2List = allData.filter { it.timestamp.contains(day2.toString()) }
//////                    .filterIndexed { index, _ -> index % 3 == 0 }
//////
//////                val day3List = allData.filter { it.timestamp.contains(day3.toString()) }
//////                    .filterIndexed { index, _ -> index % 3 == 0 }
////
////
////                // --- Data untuk RecyclerView (besok, tiap 3 jam) ---
//////                val tomorrowList = forecast.predictions_hourly_72h.filter {
//////                    it.timestamp.contains(tomorrow.toString())
//////                }.filterIndexed { index, _ -> index % 3 == 0 }
////
////                if (todayList.isNotEmpty()) {
////                    updateChart(todayList)
////                    Log.d("CHART_DATA", "Grafik pakai data hari ini (${todayList.size})")
////                } else {
////                    Log.w("CHART_DATA", "⚠️ Tidak ada data hari ini (${today})")
////                }
////
////                //Baru
////                adapterDay1.submitList(day1List)
////                adapterDay2.submitList(day2List)
////                adapterDay3.submitList(day3List)
////
////                // Tampilkan daftar jam untuk besok
//////                adapter.submitList(tomorrowList)
////
////                Log.d("RCV_DATA", "Hari ini: ${todayList.size}, Besok: ${day1List.size}")
////            } else {
////                Toast.makeText(this, "Data kosong", Toast.LENGTH_SHORT).show()
////            }
////        }
//
//        viewModel.forecast.observe(this) { forecast ->
//            if (forecast == null) {
//                Toast.makeText(this, "Data kosong", Toast.LENGTH_SHORT).show()
//                return@observe
//            }
//
//            binding.tvRegion.text = forecast.region
//            binding.tvSafety.text = forecast.safety.today_model
//
//            val allData = forecast.predictions_hourly_72h
//
//            // Zona yang dipakai (sesuaikan jika kamu pakai zona lain)
//            val zone = ZoneId.of("Asia/Jakarta")
//            val todayLocalDate = java.time.LocalDate.now(zone)
//
//            // Helper: parse timestamp jadi LocalDate di zone Jakarta (aman untuk beberapa format)
//            fun parseToLocalDate(ts: String): java.time.LocalDate? {
//                return try {
//                    // Terima ts seperti "2025-11-11T00:00:00", "2025-11-11T00:00:00Z" atau dengan +07:00
//                    val odt = OffsetDateTime.parse(
//                        // jika tidak ada offset, tambahkan "Z" agar parse tidak error: kita coba beberapa cara
//                        when {
//                            ts.endsWith("Z") -> ts
//                            ts.contains("+") -> ts
//                            ts.contains("-") && ts.length >= 19 && ts[19] == 'T' -> {
//                                // format like "2025-11-11T00:00:00" (no offset) -> treat as local-offset by adding Z
//                                // but better to parse as LocalDateTime fallback
//                                ts
//                            }
//                            else -> ts
//                        }
//                    )
//                    // convert to zone
//                    odt.atZoneSameInstant(ZoneId.of("UTC")).withZoneSameInstant(zone).toLocalDate()
//                } catch (e: Exception) {
//                    try {
//                        // fallback: parse as LocalDateTime then convert assuming server time is UTC
//                        val ldt = java.time.LocalDateTime.parse(ts.substring(0, minOf(ts.length, 19)))
//                        ldt.atZone(ZoneId.of("UTC")).withZoneSameInstant(zone).toLocalDate()
//                    } catch (ex: Exception) {
//                        Log.w("CHART_DATA", "Gagal parse timestamp: $ts")
//                        null
//                    }
//                }
//            }
//
//            // --- buat map tanggal -> list entry (untuk debugging) ---
//            val byDate = allData.groupBy { item ->
//                parseToLocalDate(item.timestamp) ?: java.time.LocalDate.MIN
//            }
//
//            // Debug: lihat 5 timestamp mentah dan mapping date
//            Log.d("CHART_DATA", "Contoh 5 timestamp mentah:")
//            allData.take(5).forEach { Log.d("CHART_DATA", it.timestamp) }
//            Log.d("CHART_DATA", "Tanggal sekarang (Jakarta): $todayLocalDate")
//            Log.d("CHART_DATA", "Tanggal yg tersedia di data: ${byDate.keys}")
//
//            // Ambil list hari ini berdasarkan konversi zone
//            var todayList = byDate[todayLocalDate] ?: emptyList()
//
//            // Jika kosong, fallback: ambil earliest available day dari data
//            var usedDate = todayLocalDate
//            if (todayList.isEmpty()) {
//                val earliest = byDate.keys.filter { it != java.time.LocalDate.MIN }.minOrNull()
//                if (earliest != null) {
//                    todayList = byDate[earliest] ?: emptyList()
//                    usedDate = earliest
//                    Log.w("CHART_DATA", "⚠️ Tidak ada data hari ini ($todayLocalDate). Menggunakan tanggal terawal di data: $earliest")
//                } else {
//                    Log.w("CHART_DATA", "⚠️ Tidak ada data sama sekali di allData")
//                }
//            }
//
//            // pastikan urut menurut time (parse dan sort)
//            val sortFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
//            val todayListSorted = todayList.sortedBy { it.timestamp }
//
//            // siapkan day1/day2/day3 berdasarkan tanggal asli di data (zona Jakarta)
//            val day1 = todayLocalDate.plusDays(1)
//            val day2 = todayLocalDate.plusDays(2)
//            val day3 = todayLocalDate.plusDays(3)
//
//            fun filterEvery3HoursFor(date: java.time.LocalDate): List<HourlyPrediction> {
//                val list = byDate[date] ?: emptyList()
//                // sort by timestamp then pick every 3rd item starting at 0 (0,3,6,...)
//                val sorted = list.sortedBy { it.timestamp }
//                return sorted.filterIndexed { index, _ -> index % 3 == 0 }
//            }
//
//            val day1List = filterEvery3HoursFor(day1)
//            val day2List = filterEvery3HoursFor(day2)
//            val day3List = filterEvery3HoursFor(day3)
//
//            // Debug log counts
//            Log.d("RCV_DATA", "Used date for chart: $usedDate (items=${todayListSorted.size})")
//            Log.d("RCV_DATA", "Day1: ${day1List.size}, Day2: ${day2List.size}, Day3: ${day3List.size}")
//
//            // ---------- tampilkan ----------
//
//            // Grafik: pakai todayListSorted (jika kosong, kita sudah memilih earliest available)
//            if (todayListSorted.isNotEmpty()) {
//                updateChart(todayListSorted)
//            } else {
//                // tidak ada data sama sekali -> clear chart dan beri toast/info
//                binding.lineChart.clear()
//                binding.lineChart.invalidate()
//                Toast.makeText(this, "Tidak ada data untuk ditampilkan.", Toast.LENGTH_SHORT).show()
//            }
//
//            // RecyclerViews per hari (besok/lusa/k-3)
//            adapterDay1.submitList(day1List)
//            adapterDay2.submitList(day2List)
//            adapterDay3.submitList(day3List)
//        }
//
//
//        viewModel.errorMessage.observe(this) { error ->
//            error?.let {
//                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
//            }
//        }
//
//        // Ambil data awal
//        viewModel.loadForecast("Laut Selatan Jawa")
//    }
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    private fun updateChart(todayList: List<HourlyPrediction>) {
//        val now = java.time.LocalTime.now()
//        val formatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm")
//
//        // Buat daftar jam tetap 9 titik dari 00.00 sampai 00.00 berikutnya
//        val labels = (0..8).map { i -> java.time.LocalTime.MIDNIGHT.plusHours((i * 3).toLong()) }
//        val labelStrings = labels.map { it.format(formatter) }
//
//        // Buat map data yang tersedia
//        val dataMap = todayList.associateBy {
//            val timePart = it.timestamp.substringAfter("T").substring(0, 5) // contoh: "21:00"
//            java.time.LocalTime.parse(timePart)
//        }
//
//        val entries = mutableListOf<Entry>()
//        labels.forEachIndexed { index, time ->
//            // tampilkan hanya data sampai jam sekarang
//            if (time <= now) {
//                val data = dataMap[time]
//                val waveHeight = data?.wave_height_m?.toFloat() ?: 0f
//                entries.add(Entry(index.toFloat(), waveHeight))
//            } else {
//                // setelah jam sekarang, biarkan kosong (tidak ditampilkan titik)
//            }
//        }
//
//        val dataSet = LineDataSet(entries, "Tinggi Gelombang (m)").apply {
//            mode = LineDataSet.Mode.CUBIC_BEZIER
//            setDrawCircles(true)
//            setDrawValues(false)
//            lineWidth = 2f
//            color = Color.BLACK
//            circleRadius = 4f
//            setCircleColor(Color.BLACK)
//        }
//
//        binding.lineChart.apply {
//            data = LineData(dataSet)
//            description.isEnabled = false
//            legend.isEnabled = false
//
//            xAxis.apply {
//                position = XAxis.XAxisPosition.TOP
//                valueFormatter = IndexAxisValueFormatter(labelStrings)
//                labelRotationAngle = 0f
//                granularity = 1f
//                setDrawGridLines(false)
//            }
//
//            axisRight.isEnabled = false
//            axisLeft.axisMinimum = 0f
//
//            setVisibleXRangeMaximum(9f) // tampilkan langsung 9 titik penuh
//            moveViewToX(0f)
//            invalidate()
//        }
//
//        binding.lineChart.legend.apply {
//            isEnabled = true
//            textSize = 12f
//            form = Legend.LegendForm.SQUARE
//            formSize = 10f
//            formToTextSpace = 6f
//            xEntrySpace = 10f
//            textColor = Color.BLACK
//        }
//    }
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    private fun getFormattedDate(daysFromNow: Long): String {
//        val date = LocalDate.now().plusDays(daysFromNow)
//        val formatter = DateTimeFormatter.ofPattern("EEEE, d MMM", Locale("id", "ID"))
//        return date.format(formatter)
//    }
//}



package com.idedi.perkiraancuacalaut

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.idedi.perkiraancuacalaut.data.ForecastRepository
import com.idedi.perkiraancuacalaut.databinding.ActivityMainBinding
import com.idedi.perkiraancuacalaut.ui.HourlyAdapter
import com.idedi.perkiraancuacalaut.ui.MainViewModel
import com.idedi.perkiraancuacalaut.ui.MainViewModelFactory
import com.idedi.perkiraancuacalaut.data.model.HourlyPrediction
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(ForecastRepository())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private val zone = ZoneId.of("Asia/Jakarta") // zona Jakarta

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.swipeRefresh.setOnRefreshListener {
            refreshData()
        }

        refreshData()

        binding.tvDay1Label.text = getFormattedDate(1) // Besok
        binding.tvDay2Label.text = getFormattedDate(2) // Lusa
        binding.tvDay3Label.text = getFormattedDate(3) // Hari ke-3

        val adapterDay1 = HourlyAdapter() // Besok
        val adapterDay2 = HourlyAdapter() // Lusa
        val adapterDay3 = HourlyAdapter() // Hari ke-3

        binding.rvForecast.apply {
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = adapterDay1
            clipToPadding = false
            setPadding(6, 0, 6, 0)
        }

        binding.rvDay2Forecast.apply {
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = adapterDay2
            clipToPadding = false
            setPadding(6, 0, 6, 0)
        }

        binding.rvDay3Forecast.apply {
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = adapterDay3
            clipToPadding = false
            setPadding(6, 0, 6, 0)
        }

        // Observasi data
        viewModel.forecast.observe(this) { forecast ->
            if (forecast == null) {
                Toast.makeText(this, "Data kosong", Toast.LENGTH_SHORT).show()
                return@observe
            }

            binding.tvRegion.text = forecast.region
            binding.tvSafety.text = forecast.safety.today_model

            // --- Gabungkan today_3h + predictions_3h_3days ---
            val allData = mutableListOf<HourlyPrediction>()
            allData.addAll(forecast.today_3h)
            allData.addAll(forecast.predictions_3h_3days)

            // --- Fungsi bantu parse timestamp ke LocalDate di zona Jakarta ---
            fun parseToLocalDate(ts: String): LocalDate? {
                return try {
                    val odt = OffsetDateTime.parse(
                        if (ts.endsWith("Z") || ts.contains("+")) ts else ts + "+07:00"
                    )
                    odt.atZoneSameInstant(zone).toLocalDate()
                } catch (e: Exception) {
                    try {
                        val ldt = java.time.LocalDateTime.parse(ts.substring(0, minOf(ts.length, 19)))
                        ldt.atZone(ZoneId.of("UTC")).withZoneSameInstant(zone).toLocalDate()
                    } catch (_: Exception) {
                        null
                    }
                }
            }

            // --- Kelompokkan data berdasarkan tanggal ---
            val byDate = allData.groupBy { parseToLocalDate(it.timestamp) ?: LocalDate.MIN }

            val todayLocalDate = LocalDate.now(zone)
            val todayList = byDate[todayLocalDate] ?: emptyList()

            // Pastikan urut berdasarkan timestamp
            val todayListSorted = todayList.sortedBy { it.timestamp }

            // --- Prediksi hari 1,2,3 per 3 jam ---
            fun filterEvery3HoursFor(date: LocalDate): List<HourlyPrediction> {
                val list = byDate[date] ?: emptyList()
                return list.sortedBy { it.timestamp }
            }

            val day1List = filterEvery3HoursFor(todayLocalDate.plusDays(1))
            val day2List = filterEvery3HoursFor(todayLocalDate.plusDays(2))
            val day3List = filterEvery3HoursFor(todayLocalDate.plusDays(3))

            // --- Debug log ---
            Log.d("RCV_DATA", "Hari ini: ${todayListSorted.size}, Day1: ${day1List.size}, Day2: ${day2List.size}, Day3: ${day3List.size}")

            // --- Update chart untuk hari ini ---
            if (todayListSorted.isNotEmpty()) {
                updateChart(todayListSorted)
            } else {
                binding.lineChart.clear()
                binding.lineChart.invalidate()
                Toast.makeText(this, "Tidak ada data hari ini untuk ditampilkan.", Toast.LENGTH_SHORT).show()
            }

            day1List.forEachIndexed { i, it ->
                Log.d("DAY1_DATA", "[$i] ${it.timestamp} | wave=${it.wave_height_m} | wind=${it.wind_speed_mps}")
            }
            day2List.forEachIndexed { i, it ->
                Log.d("DAY2_DATA", "[$i] ${it.timestamp} | wave=${it.wave_height_m} | wind=${it.wind_speed_mps}")
            }
            day3List.forEachIndexed { i, it ->
                Log.d("DAY3_DATA", "[$i] ${it.timestamp} | wave=${it.wave_height_m} | wind=${it.wind_speed_mps}")
            }


            // --- Update RecyclerView untuk 3 hari kedepan ---
            adapterDay1.submitList(day1List)
            adapterDay2.submitList(day2List)
            adapterDay3.submitList(day3List)
        }

        viewModel.errorMessage.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        }

        // Ambil data awal
        viewModel.loadForecast("Laut Selatan Jawa")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateChart(todayList: List<HourlyPrediction>) {
        val now = java.time.LocalTime.now()
        val formatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm")

        val labels = (0..8).map { i -> java.time.LocalTime.MIDNIGHT.plusHours((i * 3).toLong()) }
        val labelStrings = labels.map { it.format(formatter) }

        val dataMap = todayList.associateBy {
            val timePart = it.timestamp.substringAfter("T").substring(0, 5)
            java.time.LocalTime.parse(timePart)
        }

        val entries = mutableListOf<Entry>()
        labels.forEachIndexed { index, time ->
            if (time <= now) {
                val data = dataMap[time]
                val waveHeight = data?.wave_height_m ?: 0f
                entries.add(Entry(index.toFloat(), waveHeight))
            }
        }

        val dataSet = LineDataSet(entries, "Tinggi Gelombang (m)").apply {
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawCircles(true)
            setDrawValues(false)
            lineWidth = 2f
            color = Color.BLACK
            circleRadius = 4f
            setCircleColor(Color.BLACK)
        }

        binding.lineChart.apply {
            data = LineData(dataSet)
            description.isEnabled = false
            legend.isEnabled = false

            xAxis.apply {
                position = XAxis.XAxisPosition.TOP
                valueFormatter = IndexAxisValueFormatter(labelStrings)
                labelRotationAngle = 0f
                granularity = 1f
                setDrawGridLines(false)
            }

            axisRight.isEnabled = false
            axisLeft.axisMinimum = 0f
            setVisibleXRangeMaximum(9f)
            moveViewToX(0f)
            invalidate()
        }

        binding.lineChart.legend.apply {
            isEnabled = true
            textSize = 12f
            form = Legend.LegendForm.SQUARE
            formSize = 10f
            formToTextSpace = 6f
            xEntrySpace = 10f
            textColor = Color.BLACK
        }
    }

    private fun refreshData() {
        // Contoh: panggil ulang API / ViewModel
        viewModel.loadForecast("Laut Selatan Jawa")

        // Hentikan animasi loading setelah data selesai (bisa langsung atau di observer)
        viewModel.forecast.observe(this) { forecast ->
            // update tampilan
            if (forecast != null) {
                // update UI kamu di sini
            }
            // hentikan animasi loading
            binding.swipeRefresh.isRefreshing = false
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getFormattedDate(daysFromNow: Long): String {
        val date = LocalDate.now().plusDays(daysFromNow)
        val formatter = DateTimeFormatter.ofPattern("EEEE, d MMM", Locale("id", "ID"))
        return date.format(formatter)
    }
}
