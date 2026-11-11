package com.chaprode.mobile.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * =========================================================================================
 * ChaProde Material 3 Dark Color Scheme:
 * =========================================================================================
 * Mapea los tokens de diseño extraídos del prototipo Figma Make hacia el sistema de temas
 * oficial de Material Design 3 en Jetpack Compose.
 * 
 * - primary: Botones de llamada a la acción principales (CTA en azul eléctrico).
 * - secondary: Acento deportivo en celeste cielo (Sky) para selecciones y cursor.
 * - tertiary: Acento de prestigio en oro (Gold) para primeros puestos y trofeos.
 * - background: Fondo general Deep Slate (#0F172A).
 * - surface: Superficie de tarjetas y modales (#1E293B).
 * - outline: Bordes tenues (#334155).
 * - error: Indicadores de bloqueo, cierre o errores (#EF4444).
 */
private val DarkColorScheme = darkColorScheme(
    primary = ChaProdeCta,
    onPrimary = Color.White,
    secondary = ChaProdeSky,
    onSecondary = ChaProdeBg,
    tertiary = ChaProdeGold,
    background = ChaProdeBg,
    onBackground = ChaProdeText,
    surface = ChaProdeSurface,
    onSurface = ChaProdeText,
    surfaceVariant = ChaProdeSurfaceSoft,
    onSurfaceVariant = ChaProdeMuted,
    outline = ChaProdeBorder,
    error = ChaProdeDanger,
    onError = Color.White
)

/**
 * Tema raíz de la aplicación ChaProde.
 * Envuelve todos los composables proporcionando el esquema de colores oscuros, tipografía y formas.
 * 
 * @param content Árbol de componentes visuales que heredará el tema.
 */
@Composable
fun ChaProdeTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
