package com.pharos.evlink

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import com.pharos.evlink.shizuku.ShizukuBridge
import javax.inject.Inject
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pharos.evlink.ui.theme.*
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main hub shown after activation. A clean feature grid — each tile opens a
 * module (battery, trips, controls, …) wired up in the coming milestones.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var shizuku: ShizukuBridge

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { /* best effort */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestAllPermissions()
        setContent { PharosTheme { HomeScreen() } }
    }

    /** Ask for every runtime permission the app's features use, plus Shizuku. */
    private fun requestAllPermissions() {
        val perms = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.RECORD_AUDIO
        )
        if (Build.VERSION.SDK_INT >= 33) {
            perms.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        runCatching { permissionLauncher.launch(perms.toTypedArray()) }
        // Privileged shell access (no root) — pops the Shizuku grant dialog.
        runCatching { shizuku.requestPermission() }
    }
}

private data class Feature(
    val title: String, val subtitle: String, val icon: ImageVector, val tint: Color,
    val opensLive: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val features = listOf(
        Feature("Battery Health", "SOC · SOH · cells · temp", Icons.Default.BatteryChargingFull, PharosGreen, opensLive = true),
        Feature("Live Telemetry", "power · range · speed", Icons.Default.Speed, PharosCyan, opensLive = true),
        Feature("Cameras & Vision", "BSD · AVM", Icons.Default.Videocam, PharosTeal),
        Feature("Comfort Controls", "doors · lights · climate", Icons.Default.Tune, PharosAmber),
        Feature("Connectivity", "WiFi · eSIM · hotspot", Icons.Default.Wifi, PharosCyan),
        Feature("Settings", "profile · license", Icons.Default.Settings, PharosTextDim),
    )

    Scaffold(
        containerColor = PharosBg,
        topBar = {
            Column(Modifier.background(Brush.verticalGradient(listOf(PharosSurface, PharosBg)))
                .padding(horizontal = 20.dp, vertical = 18.dp)) {
                Text("PHAROS", fontWeight = FontWeight.Black, letterSpacing = 4.sp,
                    fontSize = 22.sp, color = PharosText)
                Text("Welcome back — your car is ready", color = PharosTextDim, fontSize = 13.sp)
            }
        }
    ) { pad ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.padding(pad).fillMaxSize().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(features) { f ->
                FeatureTile(f) {
                    if (f.opensLive) {
                        context.startActivity(
                            Intent(context, com.pharos.evlink.ui.screens.LiveDashboardActivity::class.java)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureTile(f: Feature, onClick: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().height(130.dp).clip(RoundedCornerShape(18.dp))
            .background(PharosSurface)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Box(Modifier.size(44.dp).clip(RoundedCornerShape(12.dp))
            .background(f.tint.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
            Icon(f.icon, null, tint = f.tint)
        }
        Column {
            Text(f.title, color = PharosText, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Text(f.subtitle, color = PharosTextDim, fontSize = 11.sp)
        }
    }
}
