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
 * =========================================================================================
 * ChaProdeLogo: Componente de Marca Oficial (Figma Make: .logo / Brand Header).
 * =========================================================================================
 * Propósito:
 * Proporcionar una representación consistente y de alta fidelidad de la identidad de ChaProde
 * en pantallas de autenticación (Login/Registro) y en barras superiores de navegación.
 * 
 * Modos de visualización:
 * 1. **Modo Compacto ([compact] = true):**
 *    - Disposición horizontal (Row): Caja de trofeo de 38dp + Título de 20sp.
 *    - Ideal para TopBars, diálogos y espacios con límite de altura.
 * 2. **Modo Completo / Héroe ([compact] = false):**
 *    - Disposición vertical (Column): Caja de trofeo prominente de 58dp con gradiente de marca
 *      ([ChaProdeBrandGradient] Oro a Celeste), título tipográfico de 32sp en peso Black
 *      y bajada institucional ("Pronósticos Deportivos Mundiales").
 *    - Ideal para las pantallas de bienvenida, Login y Registro.
 * 
 * @param modifier Modificador Compose para ubicación y márgenes.
 * @param compact Si es true, renderiza la versión horizontal reducida; si es false, la versión hero vertical.
 */
@Composable
fun ChaProdeLogo(
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    if (compact) {
        // ---------------------------------------------------------------------------------
        // Disposición Horizontal Compacta (TopBars)
        // ---------------------------------------------------------------------------------
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
        // ---------------------------------------------------------------------------------
        // Disposición Vertical Hero (Login / Registro / Bienvenida)
        // ---------------------------------------------------------------------------------
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Isotipo: Trofeo dentro de caja con gradiente de marca Oro a Celeste
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

            // Logotipo tipográfico en peso Black
            Text(
                text = "ChaProde",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.8).sp,
                color = Color.White
            )

            // Bajada institucional de marca
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
