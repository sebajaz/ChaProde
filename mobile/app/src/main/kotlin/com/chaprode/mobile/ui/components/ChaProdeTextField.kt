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
 * =========================================================================================
 * ChaProdeTextField: Campo de texto de entrada de datos estilizado (Figma Make: .input / .modal input).
 * =========================================================================================
 * Propósito:
 * Centralizar el diseño, colores de foco, validaciones de error y comportamiento de los
 * formularios de Login, Registro, Creación de Ligas y configuración de IP.
 * 
 * Detalles de implementación:
 * 1. Estética deportiva: Esquinas redondeadas de 14dp, fondo translúcido sobre el Deep Slate
 *    y borde que transiciona a celeste cielo ([ChaProdeSky]) al recibir el foco del usuario.
 * 2. Visual Transformation: Permite alternar entre texto plano y caracteres enmascarados (contraseñas).
 * 3. Gestión reactiva de errores: Si [errorMessage] no es nulo, el borde cambia automáticamente
 *    a rojo carmesí ([ChaProdeDanger]) y despliega el mensaje de error debajo del campo.
 * 4. Control de teclado virtual: Soporte completo para [KeyboardOptions] (tipo de teclado email, contraseña, número)
 *    y [KeyboardActions] (acción de presionar Enter / Enviar / Siguiente).
 * 
 * @param value Texto actual ingresado por el usuario.
 * @param onValueChange Callback invocado cada vez que el usuario escribe o borra un carácter.
 * @param label Etiqueta descriptiva flotante del campo (ej: "Correo Electrónico").
 * @param modifier Modificador Compose.
 * @param leadingIcon Icono colocado al inicio del campo (ej: candado, persona, correo).
 * @param trailingIcon Componente composable al final del campo (ej: botón para ver/ocultar contraseña).
 * @param visualTransformation Transformación visual (ej: [androidx.compose.ui.text.input.PasswordVisualTransformation]).
 * @param keyboardOptions Configuración del teclado virtual (tipo IME, mayúsculas, etc.).
 * @param keyboardActions Acciones al pulsar la tecla de confirmación del teclado virtual.
 * @param errorMessage Mensaje de error para validaciones fallidas (ej: "Contraseña demasiado corta").
 * @param enabled Si es false, deshabilita el campo.
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

        // ---------------------------------------------------------------------------------
        // Mensaje de Error Condicional (Alineado debajo del input)
        // ---------------------------------------------------------------------------------
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
