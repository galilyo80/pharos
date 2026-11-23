package com.pharos.evlink.activation

import android.content.Context
import com.pharos.evlink.PharosLog
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/** License states the app can be in. */
enum class LicenseState { UNKNOWN, TRIAL, ACTIVE, BLOCKED, NOT_FOUND, EXPIRED }

@Serializable
data class LicenseResponse(
    val state: String = "not_found",
    val plan: String? = null,          // e.g. "yearly", "lifetime"
    val expiresEpoch: Long? = null,
    val message: String? = null
)

/**
 * Handles activation + license checks against the Pharos backend.
 *
 * Design goals:
 *  - Bind each license to one [DeviceIdProvider.deviceId].
 *  - Cache the last good result so the app works offline between checks
 *    (a parked car often has no data), then re-validates when back online.
 *  - Support a subscription model (trial / yearly / lifetime) via `plan`.
 */
@Singleton
class LicenseManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val deviceIdProvider: DeviceIdProvider
) {
    private val prefs = context.getSharedPreferences("pharos_license", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }
    private val http = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    /** The cached state, read instantly at startup so the UI never waits. */
    fun cachedState(): LicenseState =
        runCatching { LicenseState.valueOf(prefs.getString(KEY_STATE, "UNKNOWN")!!) }
            .getOrDefault(LicenseState.UNKNOWN)

    fun cachedExpiry(): Long = prefs.getLong(KEY_EXPIRES, 0L)

    /** Verify with the backend. Falls back to cache on network failure. */
    suspend fun verify(): LicenseState {
        val id = deviceIdProvider.deviceId()
        return try {
            val url = "$BASE_URL/license/verify?device_id=$id"
            val req = Request.Builder().url(url).header("User-Agent", "Pharos/1.0").build()
            http.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return cachedState()
                val body = resp.body?.string().orEmpty()
                val parsed = json.decodeFromString<LicenseResponse>(body)
                val state = parsed.state.toLicenseState()
                persist(state, parsed.expiresEpoch ?: 0L)
                PharosLog.i("License", "verify -> $state")
                state
            }
        } catch (t: Throwable) {
            PharosLog.w("License", "verify offline, using cache: ${t.message}")
            cachedState()
        }
    }

    /** Submit a new activation request (name/phone/email/model) for a device. */
    suspend fun requestActivation(form: ActivationForm): Boolean {
        return try {
            val url = "$BASE_URL/license/request"
            val payload = json.encodeToString(ActivationForm.serializer(), form)
            val req = Request.Builder().url(url)
                .post(payload.toRequestBody(JSON_MEDIA))
                .build()
            http.newCall(req).execute().use { it.isSuccessful }
        } catch (t: Throwable) {
            PharosLog.e("License", "activation request failed", t)
            false
        }
    }

    private fun persist(state: LicenseState, expires: Long) {
        prefs.edit().putString(KEY_STATE, state.name).putLong(KEY_EXPIRES, expires).apply()
    }

    private fun String.toLicenseState() = when (lowercase()) {
        "active" -> LicenseState.ACTIVE
        "trial" -> LicenseState.TRIAL
        "blocked" -> LicenseState.BLOCKED
        "expired" -> LicenseState.EXPIRED
        else -> LicenseState.NOT_FOUND
    }

    companion object {
        // Set to your own backend. Kept here so there is one source of truth.
        private const val BASE_URL = "http://10.211.55.3:8988"
        private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()
        private const val KEY_STATE = "state"
        private const val KEY_EXPIRES = "expires"
    }
}

@Serializable
data class ActivationForm(
    val deviceId: String,
    val name: String,
    val phone: String,
    val email: String,
    val country: String,
    val brand: String,
    val model: String,
    val powertrain: String
)
