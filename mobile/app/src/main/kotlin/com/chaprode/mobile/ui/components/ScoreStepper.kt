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
 * Control interactivo de entrada numérica tipo Stepper: [-] [valor] [+]
 * Diseñado a partir del componente ScoreStepper de Figma Make para la predicción rápida de goles.
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
    val shape = RoundedCornerShape(10.dp)
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
        // Botón Restar (-)
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

        // Valor Numérico Central
        Text(
            text = value.toString(),
            color = if (disabled) ChaProdeMuted else Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )

        // Botón Sumar (+)
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
