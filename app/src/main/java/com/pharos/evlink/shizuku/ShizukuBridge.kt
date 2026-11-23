package com.pharos.evlink.shizuku

import com.pharos.evlink.PharosLog
import com.pharos.evlink.core.SafetyPolicy
import com.pharos.evlink.core.ShellResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Central gateway for privileged commands.
 *
 * Pharos never needs root. It uses Shizuku, which exposes a system-level
 * process the user authorises once. Every vehicle adapter runs its shell
 * commands through this one class, so permission handling lives in a single
 * place and the rest of the app stays testable.
 */
@Singleton
class ShizukuBridge @Inject constructor() {

    companion object {
        private const val SCOPE = "Shizuku"
        const val PERMISSION_CODE = 4210
    }

    /** True when the Shizuku service is running and reachable. */
    fun isAvailable(): Boolean = try {
        Shizuku.pingBinder() && !Shizuku.isPreV11()
    } catch (t: Throwable) {
        false
    }

    /** True when the user has already granted Pharos the Shizuku permission. */
    fun isGranted(): Boolean = try {
        isAvailable() && Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED
    } catch (t: Throwable) {
        false
    }

    /** Pops the Shizuku grant dialog. Observe the result via addRequestPermissionResultListener. */
    fun requestPermission() {
        if (isAvailable() && !isGranted()) {
            runCatching { Shizuku.requestPermission(PERMISSION_CODE) }
                .onFailure { PharosLog.e(SCOPE, "requestPermission failed", it) }
        }
    }

    /**
     * Runs a shell command with elevated privileges and returns stdout/stderr/exit.
     * Suspends on the IO dispatcher so callers never block the UI thread.
     */
    suspend fun exec(command: String): ShellResult = withContext(Dispatchers.IO) {
        if (!isGranted()) {
            return@withContext ShellResult(-1, "", "Shizuku not granted")
        }
        // Safety gate: refuse anything that could permanently change the car.
        if (!SafetyPolicy.isAllowed(command)) {
            PharosLog.w(SCOPE, "REFUSED: ${command.take(80)}")
            return@withContext ShellResult(-1, "", SafetyPolicy.reason(command))
        }
        try {
            val process = Shizuku.newProcess(arrayOf("sh", "-c", command), null, null)
            val out = BufferedReader(InputStreamReader(process.inputStream)).readText()
            val err = BufferedReader(InputStreamReader(process.errorStream)).readText()
            val code = process.waitFor()
            PharosLog.d(SCOPE, "exec rc=$code : ${command.take(80)}")
            ShellResult(code, out.trim(), err.trim())
        } catch (t: Throwable) {
            PharosLog.e(SCOPE, "exec error", t)
            ShellResult(-1, "", t.message ?: "exec failed")
        }
    }

    /** Convenience: run a command and return stdout, or null on failure. */
    suspend fun execOrNull(command: String): String? =
        exec(command).takeIf { it.isSuccess }?.stdout
}
