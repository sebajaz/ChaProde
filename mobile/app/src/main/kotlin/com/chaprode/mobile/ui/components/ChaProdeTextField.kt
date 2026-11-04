package com.chaprode.mobile.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chaprode.mobile.ui.theme.*

/**
 * Campo de texto estilizado según el prototipo de Figma Make.
 * Posee esquinas redondeadas de 14.dp, fondo oscuro semitransparente y resaltado celeste en foco.
 */
@Composable
fun ChaProdeTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    errorMessage: String? = null,
    enabled: Boolean = true
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = {
                Text(
                    text = label,
                    fontWeight = FontWeight.Medium
                )
            },
            singleLine = true,
            enabled = enabled,
            leadingIcon = leadingIcon?.let {
                { Icon(it, contentDescription = null, tint = ChaProdeMuted) }
            },
            trailingIcon = trailingIcon,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            isError = errorMessage != null,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                disabledTextColor = ChaProdeSubtle,
                focusedContainerColor = ChaProdeBg.copy(alpha = 0.55f),
                unfocusedContainerColor = ChaProdeBg.copy(alpha = 0.55f),
                focusedBorderColor = ChaProdeSky,
                unfocusedBorderColor = ChaProdeBorder,
                focusedLabelColor = ChaProdeSky,
                unfocusedLabelColor = ChaProdeMuted,
                cursorColor = ChaProdeSky,
                errorBorderColor = ChaProdeDanger,
                errorLabelColor = ChaProdeDanger
            )
        )
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = ChaProdeDanger,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 12.dp, top = 4.dp)
            )
        }
    }
}
