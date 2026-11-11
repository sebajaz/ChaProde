package com.chaprode.mobile.ui.components

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

/**
 * Efecto secundario Compose que solicita permiso de notificaciones en tiempo de ejecución.
 * A partir de Android 13 (Tiramisu / API 33), el permiso `POST_NOTIFICATIONS` es mandatorio
 * para poder despachar recordatorios de partidos y alertas de pronósticos en primer plano.
 */
@Composable
fun RequestNotificationPermissionEffect() {
    // Verificación condicional: solo se invoca en Android 13 o superior
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        // Registra el lanzador de resultados de actividad para solicitar permisos del sistema
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { _ ->
                // Callback de resultado: True si el usuario aceptó, False si denegó
                // Las notificaciones funcionarán en función de esta decisión sin bloquear la app
            }
        )

        // Dispara la solicitud una única vez al componer la vista
        LaunchedEffect(Unit) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
