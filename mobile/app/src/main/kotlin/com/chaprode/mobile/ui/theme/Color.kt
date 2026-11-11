package com.chaprode.mobile.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * ChaProde Design Tokens
 * Extraídos y normalizados a partir del prototipo visual de Figma Make (CSS variables & Tailwind tokens).
 */

// Fondos y Superficies
val ChaProdeBg = Color(0xFF0F172A)              // --bg (Deep Slate)
val ChaProdeSurface = Color(0xFF1E293B)         // --surface (Card Dark Slate)
val ChaProdeSurfaceSoft = Color(0xFF172338)     // --surface-soft (Gradientes internos)
val ChaProdeSurfaceRaised = Color(0xFF24334A)   // --surface-raised (Controles elevados, steppers)

// Bordes
val ChaProdeBorder = Color(0xFF334155)          // --border
val ChaProdeBorderLight = Color(0xFF475569)     // Bordes secundarios / destacados

// Acentos de Marca
val ChaProdeGold = Color(0xFFFBBF24)            // --gold (Marca, trofeos, 1er puesto podio)
val ChaProdeSky = Color(0xFF38BDF8)             // --sky (Acento deportivo, subtítulos, cursores)
val ChaProdeCta = Color(0xFF0284C7)             // --cta (Acción principal / Botones)
val ChaProdeCtaDark = Color(0xFF0369A1)         // Gradiente inferior de botones CTA

// Estados
val ChaProdeSuccess = Color(0xFF10B981)         // --success (Partidos en vivo, aciertos)
val ChaProdeDanger = Color(0xFFEF4444)          // --danger (Errores, partidos bloqueados, logout)

// Metales / Podio
val ChaProdeSilver = Color(0xFFCBD5E1)          // --silver (2do puesto podio)
val ChaProdeBronze = Color(0xFFF97316)          // --bronze (3er puesto podio)

// Tipografía
val ChaProdeText = Color(0xFFF8FAFC)            // --text (Texto primario de alto contraste)
val ChaProdeMuted = Color(0xFF94A3B8)           // --muted (Subtítulos, fechas, leyendas)
val ChaProdeSubtle = Color(0xFF64748B)          // Texto deshabilitado o de baja prioridad

// Pinceles / Gradientes
val ChaProdeCtaGradient = Brush.horizontalGradient(
    colors = listOf(ChaProdeCta, ChaProdeCtaDark)
)

val ChaProdeBrandGradient = Brush.horizontalGradient(
    colors = listOf(ChaProdeGold, ChaProdeSky)
)

val ChaProdeCardGradient = Brush.verticalGradient(
    colors = listOf(ChaProdeSurface, ChaProdeSurfaceSoft)
)
