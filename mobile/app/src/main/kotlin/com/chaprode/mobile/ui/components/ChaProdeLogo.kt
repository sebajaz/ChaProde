package com.chaprode.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chaprode.mobile.ui.theme.*

/**
 * Logotipo oficial de ChaProde inspirado en el componente Logo de Figma Make.
 * Combina un contenedor de trofeo con gradiente Oro a Celeste y tipografía de alto impacto.
 */
@Composable
fun ChaProdeLogo(
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    if (compact) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(ChaProdeBrandGradient, RoundedCornerShape(11.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = ChaProdeBg,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = "ChaProde",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        }
    } else {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .background(ChaProdeBrandGradient, RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = ChaProdeBg,
                    modifier = Modifier.size(30.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "ChaProde",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.8).sp,
                color = Color.White
            )
            Text(
                text = "Pronósticos Deportivos Mundiales",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = ChaProdeMuted,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
