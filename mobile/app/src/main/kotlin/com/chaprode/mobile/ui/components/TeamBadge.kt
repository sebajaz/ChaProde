package com.chaprode.mobile.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chaprode.mobile.ui.theme.*

/**
 * =========================================================================================
 * TeamBadgeColor: Paleta de acentos para los escudos vectoriales de los equipos.
 * =========================================================================================
 * Permite asignar de forma visual e identitaria colores emblemáticos a cada club o selección.
 */
enum class TeamBadgeColor(val accentColor: Color) {
    White(Color.White),
    Sky(ChaProdeSky),
    Red(Color(0xFFF43F5E)),
    Blue(Color(0xFF3B82F6)),
    Gold(ChaProdeGold),
    Green(ChaProdeSuccess)
}

/**
 * =========================================================================================
 * TeamBadge: Escudo deportivo vectorial dibujado por Canvas en Compose.
 * =========================================================================================
 * Propósito:
 * Representa de forma estética e instantánea el escudo de cualquier club o selección nacional
 * utilizando su código abreviado de 3 letras (ej: ARG, BRA, MCI, RMA, FCB).
 * 
 * Ventajas arquitectónicas:
 * 1. Cero dependencias externas: Se dibuja matemáticamente mediante un [Path] de Bézier en Compose
 *    evitando problemas de carga de imágenes en red, caché de Coil o SVG rotos.
 * 2. Rendimiento optimizado: Renderizado nativo por GPU en el hilo de dibujo.
 * 3. Escalable: El tamaño del escudo y la fuente tipográfica se adaptan proporcionalmente al [width] y [height].
 * 
 * @param code Abreviatura de 3 letras del equipo (ej: "ARG", "RMA"). Si es más largo, toma los primeros 3 caracteres.
 * @param modifier Modificador Compose para posicionamiento.
 * @param badgeColor Color temático para el borde del escudo (blanco, celeste, rojo, azul, oro, verde).
 * @param width Ancho del escudo (por defecto 46dp).
 * @param height Alto del escudo (por defecto 50dp).
 */
@Composable
fun TeamBadge(
    code: String,
    modifier: Modifier = Modifier,
    badgeColor: TeamBadgeColor = TeamBadgeColor.Sky,
    width: Dp = 46.dp,
    height: Dp = 50.dp
) {
    Box(
        modifier = modifier
            .width(width)
            .height(height),
        contentAlignment = Alignment.Center
    ) {
        // ---------------------------------------------------------------------------------
        // Trazo Vectorial del Escudo (Bézier Curves en Canvas)
        // ---------------------------------------------------------------------------------
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Construcción del contorno clásico de escudo de fútbol
            val path = Path().apply {
                moveTo(w * 0.5f, 0f)                                                    // Vértice superior central
                lineTo(w * 0.95f, h * 0.18f)                                           // Hombro superior derecho
                cubicTo(w * 0.95f, h * 0.55f, w * 0.75f, h * 0.82f, w * 0.5f, h)      // Curva lateral hacia la punta inferior
                cubicTo(w * 0.25f, h * 0.82f, w * 0.05f, h * 0.55f, w * 0.05f, h * 0.18f) // Curva izquierda hacia el hombro
                close()                                                                // Cierre de la figura
            }

            // 1. Relleno de fondo del escudo (superficie elevada oscura)
            drawPath(
                path = path,
                color = ChaProdeSurfaceRaised,
                style = Fill
            )

            // 2. Trazo de borde metálico/acentuado según el color de equipo
            drawPath(
                path = path,
                color = badgeColor.accentColor,
                style = Stroke(width = 2.dp.toPx())
            )
        }

        // ---------------------------------------------------------------------------------
        // Texto Central: Sigla de 3 caracteres del equipo en mayúsculas
        // ---------------------------------------------------------------------------------
        Text(
            text = code.take(3).uppercase(),
            color = Color.White,
            fontSize = (width.value * 0.24f).sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )
    }
}
