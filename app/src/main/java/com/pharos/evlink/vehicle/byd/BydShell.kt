package com.pharos.evlink.vehicle.byd

import com.pharos.evlink.shizuku.ShizukuBridge
import javax.inject.Inject

/**
 * BYD-specific shell helpers. Keeps all the getprop / content-provider /
 * intent plumbing that is unique to BYD DiLink in one place, so BydAdapter
 * stays readable.
 *
 * NOTE: read-only probes (getprop, VIN) are safe. Anything that writes to the
 * car's system is gated behind explicit user action in the UI and marked here.
 */
class BydShell @Inject constructor(
    private val shizuku: ShizukuBridge
) {
    /** Reads a system property from the head unit. */
    suspend fun getprop(key: String): String? =
        shizuku.execOrNull("getprop $key")?.takeIf { it.isNotBlank() }

    /** BYD stores its model code in a vendor prop. */
    suspend fun readModel(): String? =
        getprop("persist.sys.byd.car.model") ?: getprop("ro.byd.model")

    /** DiLink platform version, used to decide which features are available. */
    suspend fun readDiLinkVersion(): String? =
        getprop("ro.byd.dilink.version")

    /** VIN, best-effort from several known locations. */
    suspend fun readVin(): String? =
        getprop("persist.sys.byd.vin")
            ?: shizuku.execOrNull("getprop | grep -i vin")
                ?.let { Regex("""\[([^\]]*vin[^\]]*)\]:\s*\[([^\]]*)\]""", RegexOption.IGNORE_CASE)
                    .find(it)?.groupValues?.getOrNull(2) }

    /** Is a BYD system service present? Used for brand detection. */
    suspend fun hasBydSystem(): Boolean =
        shizuku.execOrNull("pm path com.byd.car.server")?.contains("package:") == true ||
            readModel() != null
}
