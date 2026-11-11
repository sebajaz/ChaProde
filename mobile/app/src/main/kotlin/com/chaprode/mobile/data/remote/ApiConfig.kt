package com.chaprode.mobile.data.remote

import android.content.Context
import android.content.SharedPreferences
import android.os.Build

/**
 * =========================================================================================
 * ApiConfig: Gestor de Configuración Dinámica de Conexión de Red (Base URL del Backend Ktor).
 * =========================================================================================
 * Propósito:
 * Resuelve de forma inteligente la dirección IP / URL a la que la app debe enviar sus peticiones HTTP.
 * 
 * ¿Por qué es necesario?
 * 1. En el Emulador oficial de Android, la máquina anfitriona (tu PC) no se accede por 'localhost'
 *    sino mediante la IP virtual especial del emulador: 10.0.2.2.
 * 2. En un celular físico (como tu Motorola One Hyper conectado por Wi-Fi), la IP 10.0.2.2 no existe;
 *    debe conectarse a la IP real de la tarjeta de red de la PC (ej: 192.168.0.180).
 * 3. En producción o con túnel público (Cloudflare / ngrok), se utiliza una URL pública con HTTPS.
 * 
 * Además, almacena cualquier URL personalizada en SharedPreferences para que los cambios manuales
 * que el usuario realice persistan incluso después de cerrar la aplicación.
 */
object ApiConfig {

    // Nombre del archivo de preferencias compartidas para guardar la configuración de red
    private const val PREF_NAME = "chaprode_api_config"

    // Clave para guardar la URL personalizada en SharedPreferences
    private const val KEY_BASE_URL = "custom_base_url"

    // -------------------------------------------------------------------------------------
    // URLs Predefinidas de Entorno:
    // -------------------------------------------------------------------------------------

    /** URL fija para el emulador de Android Studio (mapeo loopback de Android a la PC anfitriona) */
    const val DEFAULT_EMULATOR_URL = "http://10.0.2.2:8080"

    /** IP local de la computadora en la red doméstica Wi-Fi / Ethernet */
    const val DEFAULT_PC_WIFI_URL = "http://192.168.0.180:8080"

    // Instancia local de SharedPreferences
    private var sharedPrefs: SharedPreferences? = null

    /**
     * URL Base activa que consumen todos los servicios API de Retrofit/KtorClient en la aplicación.
     * Su valor puede ser leído públicamente, pero solo modificado mediante [setBaseUrl] o [resetToDefault].
     */
    var BASE_URL: String = DEFAULT_EMULATOR_URL
        private set

    /**
     * Inicializa el gestor de configuración con el contexto de la aplicación.
     * 1. Lee si el usuario ya había guardado previamente una URL personalizada.
     * 2. Si no hay ninguna guardada, detecta automáticamente si el dispositivo es un emulador
     *    o un teléfono físico mediante [getDefaultUrl].
     * 
     * @param context Contexto de la aplicación (proporcionado desde MainActivity).
     */
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
     * Detecta mediante inspección del hardware y del sistema operativo (Build Properties)
     * si la aplicación se está ejecutando en un emulador Android (Google SDK, Genymotion, QEMU)
     * o en un dispositivo físico real (Motorola, Samsung, etc.).
     * 
     * @return true si es un emulador; false si es un teléfono físico.
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
     * Retorna la URL por defecto correspondiente al entorno de hardware detectado:
     * - Si corre en emulador: http://10.0.2.2:8080
     * - Si corre en celular físico: http://192.168.0.180:8080
     */
    fun getDefaultUrl(): String = if (isEmulator()) DEFAULT_EMULATOR_URL else DEFAULT_PC_WIFI_URL

    /**
     * Establece una nueva URL base para la API y la persiste en SharedPreferences.
     * Sanitiza la entrada removiendo barras diagonales finales y asegurando el prefijo http:// o https://.
     * 
     * @param newUrl Dirección del servidor introducida por el usuario o asignada por túnel.
     */
    fun setBaseUrl(newUrl: String) {
        val trimmed = newUrl.trim().removeSuffix("/")
        BASE_URL = if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) trimmed else "http://$trimmed"
        sharedPrefs?.edit()?.putString(KEY_BASE_URL, BASE_URL)?.apply()
    }

    /**
     * Restablece la URL a la configuración original detectada según el hardware,
     * borrando cualquier personalización previa de SharedPreferences.
     */
    fun resetToDefault() {
        sharedPrefs?.edit()?.remove(KEY_BASE_URL)?.apply()
        BASE_URL = getDefaultUrl()
    }
}
