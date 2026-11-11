package com.chaprode.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chaprode.mobile.ui.theme.*

/**
 * =========================================================================================
 * ScoreStepper: Selector numérico compacto de goles tipo Stepper ([-] [Goles] [+]).
 * =========================================================================================
 * Componente interactivo diseñado a partir del prototipo Figma Make (`.stepper`) para
 * la carga ágil y táctil de pronósticos en partidos de fútbol.
 * 
 * Características:
 * - Botón de decremento `[-]` y botón de incremento `[+]` laterales con respuesta táctil.
 * - Pantalla central con tipografía bold de alto contraste para el número de goles.
 * - Validación automática de límites (por defecto entre 0 y 99 goles).
 * - Estado deshabilitado (`disabled = true`) para partidos bloqueados por la regla de 15 minutos
 *   o partidos ya finalizados, cambiando el color a un gris sutil y bloqueando la interacción.
 * 
 * @param value Valor actual del marcador pronosticado (entero).
 * @param onValueChange Callback invocado cuando el usuario pulsa [-] o [+], entregando el nuevo valor.
 * @param modifier Modificador de diseño Compose.
 * @param disabled Si es true, inhabilita los clics y muestra el componente en tonos atenuados.
 * @param minValue Valor mínimo permitido (generalmente 0 goles).
 * @param maxValue Valor máximo permitido (generalmente 99 goles).
 */
@Composable
fun ScoreStepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    disabled: Boolean = false,
    minValue: Int = 0,
    maxValue: Int = 99
) {
    // Esquinas redondeadas compactas de 10dp
    val shape = RoundedCornerShape(10.dp)
    // El color del borde cambia si el control está deshabilitado
    val borderColor = if (disabled) ChaProdeBorder else ChaProdeBorderLight

    Row(
        modifier = modifier
            .width(58.dp)
            .height(38.dp)
            .clip(shape)
            .border(1.dp, borderColor, shape)
            .background(ChaProdeBg),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ---------------------------------------------------------------------------------
        // 1. Botón Restar (-)
        // ---------------------------------------------------------------------------------
        val canDecrement = !disabled && value > minValue
        Box(
            modifier = Modifier
                .width(18.dp)
                .fillMaxHeight()
                .background(ChaProdeSurfaceRaised)
                .clickable(enabled = canDecrement) {
                    onValueChange((value - 1).coerceAtLeast(minValue))
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "−",
                color = if (canDecrement) ChaProdeSky else ChaProdeSubtle,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }

        // ---------------------------------------------------------------------------------
        // 2. Valor Numérico Central (Goles Pronosticados)
        // ---------------------------------------------------------------------------------
        Text(
            text = value.toString(),
            color = if (disabled) ChaProdeMuted else Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )

        // ---------------------------------------------------------------------------------
        // 3. Botón Sumar (+)
        // ---------------------------------------------------------------------------------
        val canIncrement = !disabled && value < maxValue
        Box(
            modifier = Modifier
                .width(18.dp)
                .fillMaxHeight()
                .background(ChaProdeSurfaceRaised)
                .clickable(enabled = canIncrement) {
                    onValueChange((value + 1).coerceAtMost(maxValue))
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "+",
                color = if (canIncrement) ChaProdeSky else ChaProdeSubtle,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}
