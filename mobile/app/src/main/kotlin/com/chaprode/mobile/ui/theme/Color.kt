package com.chaprode.mobile.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * =========================================================================================
 * ChaProde Design Tokens: Paleta de Colores y Gradientes Oficiales de la Marca.
 * =========================================================================================
 * Extraídos y normalizados a partir del prototipo visual de Figma Make (variables CSS y tokens Tailwind).
 * Esta paleta centraliza todos los tonos oscuros, acentos deportivos y metales de podio.
 */

// -----------------------------------------------------------------------------------------
// 1. FONDOS Y SUPERFICIES (Estructura espacial y tarjetas)
// -----------------------------------------------------------------------------------------

/** Fondo general de toda la aplicación (Deep Slate #0F172A, equivale a --bg) */
val ChaProdeBg = Color(0xFF0F172A)

/** Superficie base de tarjetas, modales y barras (Slate #1E293B, equivale a --surface) */
val ChaProdeSurface = Color(0xFF1E293B)

/** Superficie secundaria para gradientes internos y fondos de cabecera (#172338) */
val ChaProdeSurfaceSoft = Color(0xFF172338)

/** Superficie elevada para controles interactivos, steppers y tarjetas activas (#24334A) */
val ChaProdeSurfaceRaised = Color(0xFF24334A)

// -----------------------------------------------------------------------------------------
// 2. BORDES Y SEPARADORES
// -----------------------------------------------------------------------------------------

/** Borde estructural estándar entre componentes (#334155, equivale a --border) */
val ChaProdeBorder = Color(0xFF334155)

/** Borde sutil más visible para elementos destacados o estados de foco (#475569) */
val ChaProdeBorderLight = Color(0xFF475569)

// -----------------------------------------------------------------------------------------
// 3. ACENTOS DE MARCA E IDENTIDAD DEPORTIVA
// -----------------------------------------------------------------------------------------

/** Oro brillante para trofeos, 1er puesto de podio y logo (#FBBF24, equivale a --gold) */
val ChaProdeGold = Color(0xFFFBBF24)

/** Celeste cielo deportivo para acentos, pestaña activa y enlaces (#38BDF8, equivale a --sky) */
val ChaProdeSky = Color(0xFF38BDF8)

/** Azul eléctrico principal para llamadas a la acción / botones (#0284C7, equivale a --cta) */
val ChaProdeCta = Color(0xFF0284C7)

/** Azul oscuro para la transición inferior en gradientes de botones CTA (#0369A1) */
val ChaProdeCtaDark = Color(0xFF0369A1)

// -----------------------------------------------------------------------------------------
// 4. ESTADOS Y RESULTADOS DEPORTIVOS
// -----------------------------------------------------------------------------------------

/** Verde esmeralda para partidos en vivo, aciertos y notificaciones activas (#10B981) */
val ChaProdeSuccess = Color(0xFF10B981)

/** Rojo carmesí para partidos cerrados/bloqueados, errores y logout (#EF4444) */
val ChaProdeDanger = Color(0xFFEF4444)

// -----------------------------------------------------------------------------------------
// 5. METALES DE PODIO (Tabla de Posiciones)
// -----------------------------------------------------------------------------------------

/** Plata metálica para el 2do puesto del podio de clasificación (#CBD5E1) */
val ChaProdeSilver = Color(0xFFCBD5E1)

/** Bronce naranja para el 3er puesto del podio de clasificación (#F97316) */
val ChaProdeBronze = Color(0xFFF97316)

// -----------------------------------------------------------------------------------------
// 6. TIPOGRAFÍA Y TEXTO
// -----------------------------------------------------------------------------------------

/** Texto blanco de alto contraste para títulos principales (#F8FAFC) */
val ChaProdeText = Color(0xFFF8FAFC)

/** Texto atenuado para subtítulos, fechas, metadatos y etiquetas secundarias (#94A3B8) */
val ChaProdeMuted = Color(0xFF94A3B8)

/** Texto sutil o deshabilitado para leyendas técnicas y encabezados de tabla (#64748B) */
val ChaProdeSubtle = Color(0xFF64748B)

// -----------------------------------------------------------------------------------------
// 7. PINCELES Y GRADIENTES (Brushes)
// -----------------------------------------------------------------------------------------

/** Pincel horizontal para botones principales de acción (CTA) */
val ChaProdeCtaGradient = Brush.horizontalGradient(
    colors = listOf(ChaProdeCta, ChaProdeCtaDark)
)

/** Pincel horizontal con la identidad de marca (Oro a Celeste) */
val ChaProdeBrandGradient = Brush.horizontalGradient(
    colors = listOf(ChaProdeGold, ChaProdeSky)
)

/** Pincel vertical para dar profundidad a las tarjetas deportivas */
val ChaProdeCardGradient = Brush.verticalGradient(
    colors = listOf(ChaProdeSurface, ChaProdeSurfaceSoft)
)
