package com.pharos.evlink.ui.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pharos.evlink.shizuku.CarShell
import com.pharos.evlink.ui.theme.*
import com.pharos.evlink.vehicle.byd.BydLiveDataSource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * Live telemetry screen — reads the car every 2s via BydLiveDataSource and
 * shows the values as Pharos tiles. Needs Shizuku (or ADB) granted so the
 * privileged `service call` commands can run.
 */
@AndroidEntryPoint
class LiveDashboardActivity : ComponentActivity() {

    @Inject lateinit var live: BydLiveDataSource
    @Inject lateinit var shell: CarShell

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { PharosTheme { LiveScreen(live, shell) } }
    }
}

private data class Metric(val label: String, val value: String, val tint: Color)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LiveScreen(live: BydLiveDataSource, shell: CarShell) {
    var snap by remember { mutableStateOf<BydLiveDataSource.LiveSnapshot?>(null) }
    var connected by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(Unit) {
        while (true) {
            connected = shell.isConnected()
            snap = live.snapshot()
            delay(2000)
        }
    }

    val statusText = when (connected) {
        true -> "ADB connected — reading your car…"
        false -> "Connecting to car (ADB 127.0.0.1:5555)…"
        null -> "Connecting…"
    }

    Scaffold(
        containerColor = PharosBg,
        topBar = {
            Column(Modifier.background(PharosSurface).padding(20.dp)) {
                Text("LIVE TELEMETRY", fontWeight = FontWeight.Black, letterSpacing = 3.sp,
                    fontSize = 20.sp, color = PharosText)
                Text(statusText,
                    color = if (connected == true) PharosGreen else PharosTextDim, fontSize = 13.sp)
            }
        }
    ) { pad ->
        val s = snap
        val metrics = listOf(
            Metric("SOC", s?.socPercent?.let { "%.0f%%".format(it) } ?: "—", PharosGreen),
            Metric("HEALTH", s?.sohPercent?.let { "$it%" } ?: "—", PharosGreen),
            Metric("RANGE", s?.rangeKm?.let { "$it km" } ?: "—", PharosCyan),
            Metric("POWER", s?.powerKw?.let { "%.1f kW".format(it) } ?: "—", PharosCyan),
            Metric("BATT TEMP", s?.batteryTempC?.let { "$it°C" } ?: "—", PharosAmber),
            Metric("MOTOR TEMP", s?.motorTempC?.let { "$it°C" } ?: "—", PharosAmber),
            Metric("CELL Δ", s?.cellDeltaMv?.let { "$it mV" } ?: "—", PharosTeal),
            Metric("AUX 12V", s?.aux12v?.let { "%.1f V".format(it) } ?: "—", PharosTeal),
            Metric("ODOMETER", s?.odometerKm?.let { "%.1f km".format(it) } ?: "—", PharosTextDim),
            Metric("SPEED", s?.speedKmh?.let { "$it km/h" } ?: "—", PharosCyan),
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.padding(pad).fillMaxSize().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(metrics) { m ->
                Column(
                    Modifier.fillMaxWidth().height(96.dp).clip(RoundedCornerShape(16.dp))
                        .background(PharosSurface).padding(16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(m.value, color = m.tint, fontWeight = FontWeight.Bold, fontSize = 26.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(m.label, color = PharosTextDim, fontSize = 11.sp, letterSpacing = 1.sp)
                }
            }
        }
    }
}
