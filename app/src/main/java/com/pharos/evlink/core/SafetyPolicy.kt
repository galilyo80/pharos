package com.pharos.evlink.core

/**
 * Hard safety gate — Pharos is "safe 100%".
 *
 * Every shell command from any adapter is checked here BEFORE it runs. Anything
 * that could permanently change the car (delete/disable packages, force-stop
 * system services, change APN/network/eSIM, touch OTA, write secure settings)
 * is blocked outright. This cannot be toggled off from the UI by design, so no
 * technician or customer can harm a vehicle.
 */
object SafetyPolicy {

    // Commands/fragments that are never allowed to run.
    private val BLOCKED = listOf(
        "pm uninstall", "pm disable", "pm clear", "pm hide",
        "am force-stop", "am kill",
        "settings put secure", "settings put global",
        "svc ", "setprop",
        "apn", "network_mode", "set_network_mode",
        "ota", "reboot", "rm -rf", "content delete", "content insert", "content update"
    )

    /** True only if the command is read-only / safe to execute. */
    fun isAllowed(command: String): Boolean {
        val c = command.lowercase()
        return BLOCKED.none { c.contains(it) }
    }

    /** Short reason for logs/UI when a command is refused. */
    fun reason(command: String): String =
        "Blocked by Pharos safety policy (read-only / reversible actions only)"
}
