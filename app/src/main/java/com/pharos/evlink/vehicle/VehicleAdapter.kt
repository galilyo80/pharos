package com.pharos.evlink.vehicle

import com.pharos.evlink.core.Outcome
import kotlinx.coroutines.flow.Flow

/**
 * The single contract the whole UI talks to.
 *
 * The app never calls BYD- or Geely-specific code directly. It calls this
 * interface, and [VehicleManager] picks the right implementation at runtime.
 * Adding a new brand later = write one new class that implements this, and
 * register it. Nothing else in the app changes.
 */
interface VehicleAdapter {

    /** Which brand this adapter handles. */
    val brand: VehicleBrand

    /** Cheap check: does the current head unit look like this brand's system? */
    suspend fun matches(): Boolean

    /** Read the car's identity (model, VIN, version, device id for licensing). */
    suspend fun readIdentity(): Outcome<VehicleIdentity>

    /** A live stream of battery state. Adapters poll the car and emit updates. */
    fun batteryFlow(): Flow<BatteryState>

    /** Pull historical trips from the car's own storage. */
    suspend fun readTrips(): Outcome<List<TripRecord>>

    /** Which comfort commands this car actually supports. */
    fun supportedCommands(): Set<VehicleCommand>

    /** Execute a comfort command. */
    suspend fun sendCommand(command: VehicleCommand): Outcome<Unit>
}
