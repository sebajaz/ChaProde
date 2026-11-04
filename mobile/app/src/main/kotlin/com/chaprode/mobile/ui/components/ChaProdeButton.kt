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

enum class ButtonVariant {
    Primary,    // Gradiente azul CTA
    Secondary,  // Superficie oscura con borde
    Ghost       // Transparente con texto atenuado
}

/**
 * Botón principal de la aplicación ChaProde.
 * Diseñado con soporte de gradiente azul CTA, variantes Secondary y Ghost, e iconos laterales.
 * Mantiene 100% de compatibilidad con todas las firmas existentes del proyecto.
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

    val resolvedContentColor = contentColor ?: when (variant) {
        ButtonVariant.Primary -> Color.White
        ButtonVariant.Secondary -> Color.White
        ButtonVariant.Ghost -> ChaProdeMuted
    }

    val resolvedContainerColor = when {
        containerColor != null -> containerColor
        variant == ButtonVariant.Primary -> Color.Transparent
        variant == ButtonVariant.Secondary -> ChaProdeSurface
        else -> Color.Transparent
    }

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
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 2.5.dp,
                modifier = Modifier.size(24.dp)
            )
        } else {
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
