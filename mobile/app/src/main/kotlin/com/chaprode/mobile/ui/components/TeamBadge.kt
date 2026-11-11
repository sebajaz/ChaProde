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

enum class TeamBadgeColor(val accentColor: Color) {
    White(Color.White),
    Sky(ChaProdeSky),
    Red(Color(0xFFF43F5E)),
    Blue(Color(0xFF3B82F6)),
    Gold(ChaProdeGold),
    Green(ChaProdeSuccess)
}

/**
 * Escudo deportivo estilizado inspirado en el componente TeamBadge del prototipo Figma Make.
 * Dibuja un escudo vectorial con borde acentuado y la abreviatura de 3 letras en el centro,
 * sin dependencias externas adicionales.
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
        // Silueta de escudo deportivo vectorial
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            val path = Path().apply {
                moveTo(w * 0.5f, 0f)
                lineTo(w * 0.95f, h * 0.18f)
                cubicTo(w * 0.95f, h * 0.55f, w * 0.75f, h * 0.82f, w * 0.5f, h)
                cubicTo(w * 0.25f, h * 0.82f, w * 0.05f, h * 0.55f, w * 0.05f, h * 0.18f)
                close()
            }

            // Relleno de fondo del escudo
            drawPath(
                path = path,
                color = ChaProdeSurfaceRaised,
                style = Fill
            )

            // Borde acentuado con el color del equipo
            drawPath(
                path = path,
                color = badgeColor.accentColor,
                style = Stroke(width = 2.dp.toPx())
            )
        }

        // Abreviatura del equipo (ej. RMA, MCI, ARG)
        Text(
            text = code.take(3).uppercase(),
            color = Color.White,
            fontSize = (width.value * 0.24f).sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )
    }
}
