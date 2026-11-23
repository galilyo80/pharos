package com.pharos.evlink.vehicle

import com.pharos.evlink.PharosLog
import com.pharos.evlink.vehicle.byd.BydAdapter
import com.pharos.evlink.vehicle.geely.GeelyAdapter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Detects the car once and hands the rest of the app the correct adapter.
 *
 * Detection order: ask each adapter "is this your platform?" and take the
 * first match. BYD is checked first because it's the launch target; Geely
 * is a stub for now but already participates in detection.
 */
@Singleton
class VehicleManager @Inject constructor(
    bydAdapter: BydAdapter,
    geelyAdapter: GeelyAdapter
) {
    private val adapters: List<VehicleAdapter> = listOf(bydAdapter, geelyAdapter)

    @Volatile
    private var active: VehicleAdapter? = null

    /** Resolve (and cache) the adapter for the connected car. */
    suspend fun resolve(): VehicleAdapter? {
        active?.let { return it }
        for (adapter in adapters) {
            if (runCatching { adapter.matches() }.getOrDefault(false)) {
                PharosLog.i("VehicleManager", "Matched ${adapter.brand}")
                active = adapter
                return adapter
            }
        }
        PharosLog.w("VehicleManager", "No supported vehicle detected")
        return null
    }

    fun current(): VehicleAdapter? = active
}
