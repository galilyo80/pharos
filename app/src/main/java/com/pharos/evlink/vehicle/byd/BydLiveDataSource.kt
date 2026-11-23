package com.pharos.evlink.vehicle.byd

import com.pharos.evlink.PharosLog
import com.pharos.evlink.shizuku.ShizukuBridge
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Live vehicle data for BYD DiLink, read exactly the way the DiLink system
 * itself exposes it: through the Android `service call autoservice ...` binder
 * interface, parsing the returned Parcel hex word.
 *
 * Command shape:  service call autoservice <a> i32 <b> i32 <c>
 * Result shape:   Result: Parcel(00000000 <HEXWORD>  '........')
 *
 * The codes and the per-metric scaling below are the confirmed DiLink values.
 * Sentinels 0xFFFF (65535) and -10011 mean "not available" -> null.
 */
@Singleton
class BydLiveDataSource @Inject constructor(
    private val shizuku: ShizukuBridge
) {
    data class LiveSnapshot(
        val socPercent: Float? = null,
        val sohPercent: Int? = null,
        val batteryTempC: Int? = null,
        val motorTempC: Int? = null,
        val minCellMv: Int? = null,
        val maxCellMv: Int? = null,
        val cellDeltaMv: Int? = null,
        val aux12v: Double? = null,
        val powerKw: Float? = null,
        val rangeKm: Int? = null,
        val odometerKm: Double? = null,
        val gear: Int? = null,
        val speedKmh: Int? = null
    )

    private suspend fun raw(a: Int, b: Int, c: Int): Int? {
        val out = shizuku.execOrNull("service call autoservice $a i32 $b i32 $c") ?: return null
        val hex = extractHex(out) ?: return null
        val v = runCatching { java.lang.Long.parseLong(hex, 16).toInt() }.getOrNull() ?: return null
        return if (v == 65535 || v == -10011) null else v
    }

    /** Pulls the second whitespace-separated word after "Parcel(" — the data word. */
    private fun extractHex(s: String): String? {
        val idx = s.indexOf("Parcel(")
        if (idx < 0) return null
        val parts = s.substring(idx + 7).trim().split(Regex("\\s+"))
        return when {
            parts.size > 1 -> parts[1].trim()
            parts.isNotEmpty() -> parts[0].trim()
            else -> null
        }
    }

    /** Reads every metric once and returns a snapshot. */
    suspend fun snapshot(): LiveSnapshot {
        val soc = raw(7, 1014, 1246777400)?.toFloat()
        val soh = raw(5, 1014, 1145045032)
        val battTemp = raw(5, 1014, 1148190752)?.let { it - 40 }
        val motorTemp = raw(5, 1014, 1148190736)?.let { it - 40 }
        val maxMv = raw(5, 1014, 1147142192)
        val minMv = raw(5, 1014, 1147142160)
        val delta = if (maxMv != null && minMv != null) maxMv - minMv else null
        val aux = raw(7, 1001, 1128267816)?.let { it / 10.0 }
        val power = raw(5, 1012, 339738656)?.let { it / 1000.0f }
        val range = raw(5, 1014, 1246765104)
        val odo = raw(5, 1014, 1246765072)?.let { it / 10.0 }
        val gear = raw(5, 1014, 1246777424)
        val speed = raw(5, 1014, 1246777388)

        PharosLog.d("BydLive", "snapshot soc=$soc soh=$soh battT=$battTemp")
        return LiveSnapshot(
            socPercent = soc, sohPercent = soh, batteryTempC = battTemp, motorTempC = motorTemp,
            minCellMv = minMv, maxCellMv = maxMv, cellDeltaMv = delta, aux12v = aux,
            powerKw = power, rangeKm = range, odometerKm = odo, gear = gear, speedKmh = speed
        )
    }
}
