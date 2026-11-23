package com.pharos.evlink.vehicle.geely

import com.pharos.evlink.activation.DeviceIdProvider
import com.pharos.evlink.core.Outcome
import com.pharos.evlink.shizuku.ShizukuBridge
import com.pharos.evlink.vehicle.BatteryState
import com.pharos.evlink.vehicle.TripRecord
import com.pharos.evlink.vehicle.VehicleAdapter
import com.pharos.evlink.vehicle.VehicleBrand
import com.pharos.evlink.vehicle.VehicleCommand
import com.pharos.evlink.vehicle.VehicleIdentity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Geely placeholder adapter.
 *
 * Geely head units run GKUI / Flyme Auto, NOT BYD DiLink, so none of the BYD
 * hooks apply. This class exists so the abstraction layer is already
 * multi-brand: detection runs against it, and when we get a Geely EV to test
 * on we fill these methods in — without touching the UI or any other brand.
 */
@Singleton
class GeelyAdapter @Inject constructor(
    private val shizuku: ShizukuBridge,
    private val deviceIdProvider: DeviceIdProvider
) : VehicleAdapter {

    override val brand = VehicleBrand.GEELY

    override suspend fun matches(): Boolean {
        // Geely system apps live under different package prefixes (e.g. GKUI).
        // Left false until confirmed on a real Geely unit.
        return shizuku.execOrNull("pm list packages | grep -i -E 'geely|ecarx|gkui'")
            ?.contains("package:") == true
    }

    override suspend fun readIdentity(): Outcome<VehicleIdentity> =
        Outcome.Err("Geely support not implemented yet")

    override fun batteryFlow(): Flow<BatteryState> = emptyFlow()

    override suspend fun readTrips(): Outcome<List<TripRecord>> = Outcome.Ok(emptyList())

    override fun supportedCommands(): Set<VehicleCommand> = emptySet()

    override suspend fun sendCommand(command: VehicleCommand): Outcome<Unit> =
        Outcome.Err("Geely support not implemented yet")
}
