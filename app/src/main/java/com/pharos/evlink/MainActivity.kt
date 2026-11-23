package com.pharos.evlink

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { PharosTheme { HomeScreen() } }
    }
}

private data class Feature(val title: String, val subtitle: String, val icon: ImageVector, val tint: Color)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen() {
    val features = listOf(
        Feature("Battery Health", "SOH · cells · temp", Icons.Default.BatteryChargingFull, PharosGreen),
        Feature("Trips & Range", "history · kWh/100km", Icons.Default.Timeline, PharosCyan),
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
            items(features) { FeatureTile(it) }
        }
    }
}

@Composable
private fun FeatureTile(f: Feature) {
    Column(
        Modifier.fillMaxWidth().height(130.dp).clip(RoundedCornerShape(18.dp))
            .background(PharosSurface).padding(16.dp),
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
