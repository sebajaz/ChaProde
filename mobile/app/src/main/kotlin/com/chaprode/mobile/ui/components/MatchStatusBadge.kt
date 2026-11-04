package com.chaprode.mobile.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chaprode.mobile.ui.theme.*

sealed class MatchStatusType {
    data class Closing(val label: String = "CIERRA EN 15M") : MatchStatusType()
    data class Live(val minuteText: String? = null) : MatchStatusType()
    data class Finished(val label: String = "FINALIZADO") : MatchStatusType()
    data class Locked(val label: String = "BLOQUEADO") : MatchStatusType()
    data class Saved(val label: String = "PRONÓSTICO GUARDADO") : MatchStatusType()
}

/**
 * Píldora de estado de partido inspirada en .status de Figma Make.
 * Soporta animación de pulso en vivo, icono de reloj en cierre y estilos consistentes.
 */
@Composable
fun MatchStatusBadge(
    status: MatchStatusType,
    modifier: Modifier = Modifier
) {
    val (textColor, bgColor) = when (status) {
        is MatchStatusType.Closing -> ChaProdeGold to ChaProdeGold.copy(alpha = 0.12f)
        is MatchStatusType.Live -> ChaProdeSuccess to ChaProdeSuccess.copy(alpha = 0.12f)
        is MatchStatusType.Finished -> ChaProdeMuted to ChaProdeMuted.copy(alpha = 0.12f)
        is MatchStatusType.Locked -> ChaProdeDanger to ChaProdeDanger.copy(alpha = 0.12f)
        is MatchStatusType.Saved -> ChaProdeSuccess to ChaProdeSuccess.copy(alpha = 0.12f)
    }

    Surface(
        modifier = modifier.height(24.dp),
        shape = RoundedCornerShape(12.dp),
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            when (status) {
                is MatchStatusType.Closing -> {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = textColor,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = status.label,
                        color = textColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                }
                is MatchStatusType.Live -> {
                    // Punto pulsante en vivo
                    val infiniteTransition = rememberInfiniteTransition(label = "live_pulse")
                    val pulseAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.35f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(750, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "live_dot_alpha"
                    )

                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .alpha(pulseAlpha)
                            .background(ChaProdeSuccess, CircleShape)
                    )
                    Text(
                        text = if (status.minuteText.isNullOrBlank()) "EN VIVO" else "EN VIVO ${status.minuteText}",
                        color = textColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                }
                is MatchStatusType.Finished -> {
                    Text(
                        text = status.label,
                        color = textColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                }
                is MatchStatusType.Locked -> {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = textColor,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = status.label,
                        color = textColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                }
                is MatchStatusType.Saved -> {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = textColor,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = status.label,
                        color = textColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}
