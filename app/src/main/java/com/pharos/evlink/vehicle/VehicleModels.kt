package com.pharos.evlink.vehicle

/** Which car platform an adapter targets. */
enum class VehicleBrand { BYD, GEELY, UNKNOWN }

/** Powertrain type, used to show/hide battery-only features. */
enum class Powertrain { EV, PHEV, UNKNOWN }

/** Identity read from the car once at startup. */
data class VehicleIdentity(
    val brand: VehicleBrand,
    val model: String,          // e.g. "Seal", "Song Plus"
    val powertrain: Powertrain,
    val vin: String?,           // null if not readable
    val systemVersion: String?, // DiLink / GKUI version
    val deviceId: String        // stable per-head-unit id used for licensing
)

/** Live battery snapshot. All fields nullable — not every car exposes every value. */
data class BatteryState(
    val socPercent: Int?,          // state of charge
    val sohPercent: Int?,          // state of health (0-100)
    val packVoltage: Double?,      // volts
    val packCurrent: Double?,      // amps (+charge / -discharge)
    val minCellMv: Int?,
    val maxCellMv: Int?,
    val cellDeltaMv: Int?,         // spread = balance quality
    val tempC: Double?,
    val isCharging: Boolean?,
    val chargeKw: Double?
)

/** A single driving trip, normalised across brands. */
data class TripRecord(
    val startEpoch: Long,
    val endEpoch: Long,
    val distanceKm: Double,
    val energyKwh: Double,
    val avgConsumption: Double     // kWh/100km
)

/** A controllable comfort/vehicle action exposed by an adapter. */
enum class VehicleCommand {
    LOCK, UNLOCK,
    WINDOWS_UP, WINDOWS_DOWN,
    LIGHTS_FLASH,
    CLIMATE_ON, CLIMATE_OFF
}
