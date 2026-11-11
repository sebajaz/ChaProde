package com.chaprode.mobile.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chaprode.mobile.ui.theme.*

/**
 * =========================================================================================
 * ButtonVariant: Variantes de estilo para los botones del sistema de diseño ChaProde.
 * =========================================================================================
 * - [Primary]: Acción principal con gradiente azul eléctrico [ChaProdeCtaGradient].
 * - [Secondary]: Acción secundaria con superficie oscura elevada y borde sutil [ChaProdeBorder].
 * - [Ghost]: Botón minimalista sin fondo para cancelaciones o enlaces secundarios.
 */
enum class ButtonVariant {
    Primary,    // Gradiente azul CTA
    Secondary,  // Superficie oscura con borde
    Ghost       // Transparente con texto atenuado
}

/**
 * =========================================================================================
 * ChaProdeButton: Botón de acción principal reutilizable (Figma Make: .button).
 * =========================================================================================
 * Características:
 * 1. Soporte para indicador de carga ([CircularProgressIndicator]) integrado que bloquea clics repetidos.
 * 2. Iconos opcionales inicial ([leadingIcon]) o final ([trailingIcon]) con alineación automática.
 * 3. Altura ergonómica táctil de 52dp según las directrices de accesibilidad móvil.
 * 4. Gestión de estados deshabilitados ([enabled] = false) con atenuación visual.
 * 
 * @param text Etiqueta o texto que se muestra en el centro del botón.
 * @param onClick Acción a ejecutar al presionar el botón.
 * @param modifier Modificador Compose para personalizar ancho o márgenes.
 * @param variant Estilo visual ([ButtonVariant.Primary], [Secondary], [Ghost]).
 * @param isLoading Si es true, reemplaza el texto por un spinner giratorio y deshabilita clics.
 * @param enabled Si es false, atenúa el botón y bloquea la interacción táctil.
 * @param leadingIcon Icono vectorial opcional colocado a la izquierda del texto.
 * @param trailingIcon Icono vectorial opcional colocado a la derecha del texto.
 * @param containerColor Sobrescritura opcional del color de fondo si no se usa la variante estándar.
 * @param contentColor Sobrescritura opcional del color de texto e iconos.
 */
@Composable
fun ChaProdeButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.Primary,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    containerColor: Color? = null,
    contentColor: Color? = null
) {
    val shape = RoundedCornerShape(14.dp)
    val isPrimary = variant == ButtonVariant.Primary && containerColor == null

    // Resolución del color del contenido (texto e iconos)
    val resolvedContentColor = contentColor ?: when (variant) {
        ButtonVariant.Primary -> Color.White
        ButtonVariant.Secondary -> Color.White
        ButtonVariant.Ghost -> ChaProdeMuted
    }

    // Resolución del color del contenedor de fondo
    val resolvedContainerColor = when {
        containerColor != null -> containerColor
        variant == ButtonVariant.Primary -> Color.Transparent
        variant == ButtonVariant.Secondary -> ChaProdeSurface
        else -> Color.Transparent
    }

    // Borde aplicado únicamente a la variante secundaria
    val border = when {
        !enabled -> null
        variant == ButtonVariant.Secondary -> BorderStroke(1.dp, ChaProdeBorder)
        else -> null
    }

    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .then(
                // Si es Primary y está habilitado, aplica el gradiente CTA oficial
                if (isPrimary && enabled && !isLoading) {
                    Modifier.background(ChaProdeCtaGradient, shape)
                } else {
                    Modifier
                }
            ),
        shape = shape,
        border = border,
        colors = ButtonDefaults.buttonColors(
            containerColor = resolvedContainerColor,
            contentColor = resolvedContentColor,
            disabledContainerColor = ChaProdeBorder,
            disabledContentColor = ChaProdeSubtle
        ),
        contentPadding = PaddingValues(horizontal = 18.dp)
    ) {
        // Si está en estado de carga (ej: esperando respuesta de autenticación de Ktor)
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 2.5.dp,
                modifier = Modifier.size(24.dp)
            )
        } else {
            // Contenido regular del botón: Icono Izquierdo + Texto + Icono Derecho
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = resolvedContentColor,
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Text(
                    text = text,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (enabled) resolvedContentColor else ChaProdeSubtle
                )

                if (trailingIcon != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = trailingIcon,
                        contentDescription = null,
                        tint = resolvedContentColor,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        }
    }
}
