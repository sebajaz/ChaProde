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

/**
 * =========================================================================================
 * MatchStatusType: Representación sellada de los posibles estados de un partido de fútbol.
 * =========================================================================================
 * Permite modelar con precisión quirúrgica el momento deportivo:
 * - [Closing]: Cuenta regresiva previa al cierre de pronósticos (15 minutos antes del pitazo inicial).
 * - [Live]: Partido en disputa con animación de punto pulsante y minuto de juego opcional.
 * - [Finished]: Partido finalizado con marcador definitivo registrado.
 * - [Locked]: Partido bloqueado porque faltan menos de 15 minutos o ya comenzó; no admite cambios.
 * - [Saved]: Confirmación visual de que el usuario ya guardó su predicción para este encuentro.
 */
sealed class MatchStatusType {
    data class Closing(val label: String = "CIERRA EN 15M") : MatchStatusType()
    data class Live(val minuteText: String? = null) : MatchStatusType()
    data class Finished(val label: String = "FINALIZADO") : MatchStatusType()
    data class Locked(val label: String = "BLOQUEADO") : MatchStatusType()
    data class Saved(val label: String = "PRONÓSTICO GUARDADO") : MatchStatusType()
}

/**
 * =========================================================================================
 * MatchStatusBadge: Píldora de estado de partido (Figma Make reference: .status).
 * =========================================================================================
 * Propósito:
 * Proporcionar al usuario retroalimentación inmediata sobre la disponibilidad y condición
 * de un partido en la tarjeta del Fixture o en el Hero de Inicio.
 * 
 * Comportamiento por estado:
 * - **En Vivo (Live):** Muestra un punto verde esmeralda con animación pulsante ([infiniteRepeatable])
 *   y texto en negrita.
 * - **Cierre Próximo (Closing):** Fondo dorado con icono de reloj [Icons.Default.Schedule].
 * - **Bloqueado (Locked):** Fondo y texto rojo carmesí con icono de candado [Icons.Default.Lock].
 * - **Guardado (Saved):** Fondo verde con icono de tilde [Icons.Default.Check].
 * - **Finalizado (Finished):** Tono gris atenuado para partidos ya concluidos.
 * 
 * @param status Tipo de estado [MatchStatusType] que determina el color, icono y texto del badge.
 * @param modifier Modificador Compose para dimensiones o margen.
 */
@Composable
fun MatchStatusBadge(
    status: MatchStatusType,
    modifier: Modifier = Modifier
) {
    // -------------------------------------------------------------------------------------
    // Selección de la paleta según el estado del partido
    // -------------------------------------------------------------------------------------
    val (textColor, bgColor) = when (status) {
        is MatchStatusType.Closing -> ChaProdeGold to ChaProdeGold.copy(alpha = 0.12f)
        is MatchStatusType.Live -> ChaProdeSuccess to ChaProdeSuccess.copy(alpha = 0.12f)
        is MatchStatusType.Finished -> ChaProdeMuted to ChaProdeMuted.copy(alpha = 0.12f)
        is MatchStatusType.Locked -> ChaProdeDanger to ChaProdeDanger.copy(alpha = 0.12f)
        is MatchStatusType.Saved -> ChaProdeSuccess to ChaProdeSuccess.copy(alpha = 0.12f)
    }

    // Contenedor píldora redondeada de 24dp de altura
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
                // -------------------------------------------------------------------------
                // Caso 1: Partido próximo a cerrar pronósticos (Alerta Amarilla/Oro)
                // -------------------------------------------------------------------------
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

                // -------------------------------------------------------------------------
                // Caso 2: Partido en Vivo (Punto Verde Pulsante Animado)
                // -------------------------------------------------------------------------
                is MatchStatusType.Live -> {
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

                // -------------------------------------------------------------------------
                // Caso 3: Partido Finalizado (Gris Atenuado)
                // -------------------------------------------------------------------------
                is MatchStatusType.Finished -> {
                    Text(
                        text = status.label,
                        color = textColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                }

                // -------------------------------------------------------------------------
                // Caso 4: Pronóstico Bloqueado por la regla de 15 minutos (Candado Rojo)
                // -------------------------------------------------------------------------
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

                // -------------------------------------------------------------------------
                // Caso 5: Pronóstico guardado exitosamente por el usuario (Tilde Verde)
                // -------------------------------------------------------------------------
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
