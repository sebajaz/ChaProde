package com.chaprode.mobile.data.remote

object ApiConfig {
    /**
     * URL base del backend de ChaProde:
     * - En Emulador Android (incluyendo Android 11 / API 30): "http://10.0.2.2:8080"
     * - En Dispositivo Físico conectado por USB (ejecutando 'adb reverse tcp:8080 tcp:8080'): "http://127.0.0.1:8080"
     */
    var BASE_URL: String = "http://10.0.2.2:8080"
}
