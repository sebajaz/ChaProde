package com.chaprode.mobile.data.remote

import android.content.Context
import android.content.SharedPreferences
import android.os.Build

object ApiConfig {
    private const val PREF_NAME = "chaprode_api_config"
    private const val KEY_BASE_URL = "custom_base_url"

    const val DEFAULT_EMULATOR_URL = "http://10.0.2.2:8080"
    const val DEFAULT_PC_WIFI_URL = "http://192.168.0.180:8080"

    private var sharedPrefs: SharedPreferences? = null
    var BASE_URL: String = DEFAULT_EMULATOR_URL
        private set

    fun init(context: Context) {
        if (sharedPrefs == null) {
            sharedPrefs = context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val savedUrl = sharedPrefs?.getString(KEY_BASE_URL, null)
            BASE_URL = if (!savedUrl.isNullOrBlank()) {
                savedUrl
            } else {
                getDefaultUrl()
            }
        }
    }

    /**
     * Detecta si la aplicación se está ejecutando en un emulador Android o en un dispositivo físico.
     */
    fun isEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk" == Build.PRODUCT)
    }

    /**
     * Retorna la URL por defecto según el entorno detectado:
     * - Emulador: http://10.0.2.2:8080
     * - Celular físico (Wi-Fi): http://192.168.0.180:8080
     */
    fun getDefaultUrl(): String = if (isEmulator()) DEFAULT_EMULATOR_URL else DEFAULT_PC_WIFI_URL

    fun setBaseUrl(newUrl: String) {
        val trimmed = newUrl.trim().removeSuffix("/")
        BASE_URL = if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) trimmed else "http://$trimmed"
        sharedPrefs?.edit()?.putString(KEY_BASE_URL, BASE_URL)?.apply()
    }

    fun resetToDefault() {
        sharedPrefs?.edit()?.remove(KEY_BASE_URL)?.apply()
        BASE_URL = getDefaultUrl()
    }
}
