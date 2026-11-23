package com.pharos.evlink.activation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pharos.evlink.MainActivity
import com.pharos.evlink.ui.theme.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * The customer's FIRST screen. A branded splash that fades into a live
 * activation check. Designed to feel like a premium automotive product:
 * animated lighthouse pulse, device id shown, clear status.
 */
@AndroidEntryPoint
class ActivationActivity : ComponentActivity() {

    @Inject lateinit var licenseManager: LicenseManager
    @Inject lateinit var deviceIdProvider: DeviceIdProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PharosTheme {
                ActivationScreen(
                    deviceId = deviceIdProvider.deviceId(),
                    cached = licenseManager.cachedState(),
                    verify = { licenseManager.verify() },
                    onActivated = { goToMain() }
                )
            }
        }
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}

@Composable
private fun ActivationScreen(
    deviceId: String,
    cached: LicenseState,
    verify: suspend () -> LicenseState,
    onActivated: () -> Unit
) {
    var status by remember { mutableStateOf(cached) }
    var checking by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(900)                       // let the splash breathe
        status = verify()
        checking = false
        // Debug builds run fully unlocked: show the splash briefly, then enter.
        if (com.pharos.evlink.BuildConfig.DEBUG ||
            status == LicenseState.ACTIVE || status == LicenseState.TRIAL) {
            delay(700)
            onActivated()
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(PharosBg, Color_0B1C2C, PharosBg))
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            LighthouseMark()
            Spacer(Modifier.height(28.dp))
            Text("PHAROS", fontSize = 34.sp, fontWeight = FontWeight.Black,
                letterSpacing = 6.sp, color = PharosText)
            Text("EV INTELLIGENCE", fontSize = 12.sp, letterSpacing = 4.sp, color = PharosCyan)
            Spacer(Modifier.height(40.dp))

            when {
                checking -> StatusPill("Verifying your license…", PharosCyan, loading = true)
                status == LicenseState.ACTIVE -> StatusPill("License Active ✓", PharosGreen)
                status == LicenseState.TRIAL -> StatusPill("Trial Active", PharosTeal)
                status == LicenseState.BLOCKED -> StatusPill("License Blocked", PharosRed)
                status == LicenseState.EXPIRED -> StatusPill("Subscription Expired", PharosAmber)
                else -> StatusPill("Not Activated", PharosAmber)
            }

            Spacer(Modifier.height(32.dp))
            DeviceIdCard(deviceId)

            if (!checking && status !in setOf(LicenseState.ACTIVE, LicenseState.TRIAL)) {
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = { /* open LicenseRequest form (next milestone) */ },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PharosCyan),
                    modifier = Modifier.fillMaxWidth().height(54.dp)
                ) { Text("REQUEST ACTIVATION", color = PharosBg,
                    fontWeight = FontWeight.Bold, letterSpacing = 1.sp) }

                // Debug builds only: explore the app without a reachable server.
                if (com.pharos.evlink.BuildConfig.DEBUG) {
                    Spacer(Modifier.height(12.dp))
                    TextButton(onClick = onActivated) {
                        Text("Developer entry →", color = PharosTextDim)
                    }
                }
            }
        }
    }
}

/** Animated pulsing lighthouse beam — the brand's signature first impression. */
@Composable
private fun LighthouseMark() {
    val t = rememberInfiniteTransition(label = "pulse")
    val scale by t.animateFloat(
        0.9f, 1.12f,
        infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "scale"
    )
    Box(contentAlignment = Alignment.Center) {
        Box(
            Modifier.size(140.dp).scale(scale).clip(CircleShape)
                .background(Brush.radialGradient(listOf(PharosCyan.copy(alpha = 0.25f), Color_Transparent)))
        )
        Box(
            Modifier.size(84.dp).clip(CircleShape)
                .background(Brush.linearGradient(listOf(PharosCyan, PharosTeal))),
            contentAlignment = Alignment.Center
        ) { Text("⌖", fontSize = 40.sp, color = PharosBg) }
    }
}

@Composable
private fun StatusPill(text: String, color: Color, loading: Boolean = false) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.12f)).padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        if (loading) {
            CircularProgressIndicator(Modifier.size(16.dp), color = color, strokeWidth = 2.dp)
            Spacer(Modifier.width(10.dp))
        }
        Text(text, color = color, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun DeviceIdCard(deviceId: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
            .background(PharosSurface2).padding(18.dp)
    ) {
        Text("YOUR CAR DEVICE ID", fontSize = 11.sp, letterSpacing = 2.sp, color = PharosTextDim)
        Spacer(Modifier.height(8.dp))
        Text(deviceId, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = PharosCyan,
            textAlign = TextAlign.Center)
    }
}

private val Color_0B1C2C = Color(0xFF0B1C2C)
private val Color_Transparent = Color(0x00000000)
