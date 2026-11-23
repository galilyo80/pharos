package com.pharos.evlink.vehicle.byd

import com.pharos.evlink.PharosLog
import com.pharos.evlink.activation.DeviceIdProvider
import com.pharos.evlink.core.Outcome
import com.pharos.evlink.vehicle.BatteryState
import com.pharos.evlink.vehicle.Powertrain
import com.pharos.evlink.vehicle.TripRecord
import com.pharos.evlink.vehicle.VehicleAdapter
import com.pharos.evlink.vehicle.VehicleBrand
import com.pharos.evlink.vehicle.VehicleCommand
import com.pharos.evlink.vehicle.VehicleIdentity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

/**
 * BYD DiLink implementation of [VehicleAdapter].
 *
 * This is the launch-target adapter. It talks to the car through BydShell
 * (getprop / content providers) and the ShizukuBridge underneath it.
 *
 * The battery/trip readers below are structured but intentionally left as
 * clearly-marked TODOs for the real provider URIs — we wire those to the
 * actual DiLink data sources once we confirm them on a live car, so we never
 * ship guessed paths.
 */
@Singleton
class BydAdapter @Inject constructor(
    private val shell: BydShell,
    private val deviceIdProvider: DeviceIdProvider
) : VehicleAdapter {

    override val brand = VehicleBrand.BYD

    override suspend fun matches(): Boolean = shell.hasBydSystem()

    override suspend fun readIdentity(): Outcome<VehicleIdentity> {
        return try {
            val model = shell.readModel() ?: "Unknown BYD"
            val version = shell.readDiLinkVersion()
            val vin = shell.readVin()
            Outcome.Ok(
                VehicleIdentity(
                    brand = VehicleBrand.BYD,
                    model = model,
                    powertrain = Powertrain.EV, // refined later from model map
                    vin = vin,
                    systemVersion = version,
                    deviceId = deviceIdProvider.deviceId()
                )
            )
        } catch (t: Throwable) {
            PharosLog.e("BydAdapter", "readIdentity failed", t)
            Outcome.Err("Could not read vehicle identity", t)
        }
    }

    override fun batteryFlow(): Flow<BatteryState> = flow {
        while (true) {
            // TODO(byd-live): read from the confirmed DiLink battery source and
            // map into BatteryState. Emitting an empty snapshot keeps the UI
            // wiring testable until the live source is confirmed on a car.
            emit(BatteryState(null, null, null, null, null, null, null, null, null, null))
            delay(2_000)
        }
    }

    override suspend fun readTrips(): Outcome<List<TripRecord>> {
        // TODO(byd-trips): query the confirmed trip content provider and map rows.
        return Outcome.Ok(emptyList())
    }

    override fun supportedCommands(): Set<VehicleCommand> = setOf(
        VehicleCommand.LIGHTS_FLASH,
        VehicleCommand.CLIMATE_ON,
        VehicleCommand.CLIMATE_OFF
    )

    override suspend fun sendCommand(command: VehicleCommand): Outcome<Unit> {
        // TODO(byd-cmd): map each command to its confirmed BYD intent/broadcast.
        return Outcome.Err("Command $command not yet wired")
    }
}
