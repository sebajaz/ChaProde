package com.chaprode.mobile.data.remote

import android.content.Context
import android.content.SharedPreferences

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
            if (!savedUrl.isNullOrBlank()) {
                BASE_URL = savedUrl
            }
        }
    }

    fun setBaseUrl(newUrl: String) {
        val trimmed = newUrl.trim().removeSuffix("/")
        BASE_URL = if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) trimmed else "http://$trimmed"
        sharedPrefs?.edit()?.putString(KEY_BASE_URL, BASE_URL)?.apply()
    }
}
