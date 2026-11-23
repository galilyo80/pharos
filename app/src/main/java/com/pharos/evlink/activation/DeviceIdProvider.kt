package com.pharos.evlink.activation

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Produces a STABLE, per-head-unit device id used to bind a license to one car.
 *
 * It hashes the Android ID (stable per device/app-signing) so the raw id never
 * leaves the device in clear form. The same car always yields the same id, so
 * a license issued to a customer keeps working across app updates and reboots.
 */
@Singleton
class DeviceIdProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    @SuppressLint("HardwareIds")
    fun deviceId(): String {
        val androidId = Settings.Secure.getString(
            context.contentResolver, Settings.Secure.ANDROID_ID
        ) ?: "unknown"
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(("PHAROS:$androidId").toByteArray())
        // Short, readable, car-friendly id: PH-XXXXXXXXXXXX
        val hex = digest.joinToString("") { "%02X".format(it) }
        return "PH-" + hex.take(12)
    }
}
