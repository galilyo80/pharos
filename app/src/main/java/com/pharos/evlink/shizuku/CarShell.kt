package com.pharos.evlink.shizuku

import android.content.Context
import com.pharos.evlink.PharosLog
import com.pharos.evlink.core.SafetyPolicy
import dadb.AdbKeyPair
import dadb.Dadb
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Runs privileged shell commands on the DiLink head unit.
 *
 * Primary path (same as the reference app): a self-contained ADB client that
 * connects to the local ADB daemon at 127.0.0.1:5555. The head unit already
 * has wireless debugging enabled, so no Shizuku is required. A generated ADB
 * keypair is kept in the app's files dir; the first connection may raise the
 * device's "allow debugging" prompt once.
 *
 * Fallback path: Shizuku, for units where ADB self-connect isn't available.
 */
@Singleton
class CarShell @Inject constructor(
    @ApplicationContext private val context: Context,
    private val shizuku: ShizukuBridge
) {
    @Volatile private var dadb: Dadb? = null

    private fun keyPair(): AdbKeyPair {
        val priv = File(context.filesDir, "adbkey")
        val pub = File(context.filesDir, "adbkey.pub")
        if (!priv.exists() || !pub.exists()) {
            AdbKeyPair.generate(priv, pub)
        }
        return AdbKeyPair.read(priv, pub)
    }

    private fun connectAdb(): Dadb? {
        dadb?.let { return it }
        return try {
            val d = Dadb.create("127.0.0.1", 5555, keyPair())
            dadb = d
            PharosLog.i("CarShell", "ADB connected (127.0.0.1:5555)")
            d
        } catch (t: Throwable) {
            PharosLog.w("CarShell", "ADB connect failed: ${t.message}")
            null
        }
    }

    /** True if we currently have (or can make) an ADB connection. */
    suspend fun isConnected(): Boolean = withContext(Dispatchers.IO) { connectAdb() != null }

    /** Run a command, ADB first then Shizuku. Returns stdout, or null. */
    suspend fun exec(command: String): String? = withContext(Dispatchers.IO) {
        if (!SafetyPolicy.isAllowed(command)) {
            PharosLog.w("CarShell", "REFUSED: ${command.take(80)}")
            return@withContext null
        }
        // ADB path
        connectAdb()?.let { d ->
            try {
                return@withContext d.shell(command).output
            } catch (t: Throwable) {
                PharosLog.w("CarShell", "ADB exec failed, dropping connection: ${t.message}")
                runCatching { d.close() }
                dadb = null
            }
        }
        // Shizuku fallback
        shizuku.execOrNull(command)
    }
}
