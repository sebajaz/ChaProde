package com.chaprode.mobile.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chaprode.mobile.data.repository.AuthRepository
import com.chaprode.mobile.model.Resource
import com.chaprode.mobile.ui.components.ButtonVariant
import com.chaprode.mobile.ui.components.ChaProdeButton
import com.chaprode.mobile.ui.components.ChaProdeLogo
import com.chaprode.mobile.ui.components.ChaProdeTextField
import com.chaprode.mobile.ui.theme.*
import kotlinx.coroutines.launch

/**
 * Pantalla Stateful de Registro:
 * Administra el formulario de alta de nuevo usuario, la validación de entrada
 * y la llamada asíncrona a `AuthRepository.register`.
 *
 * @param onNavigateToLogin Callback para regresar a la pantalla de login.
 * @param onRegisterSuccess Callback ejecutado cuando el registro y sesión automática son confirmados.
 * @param repository Repositorio de autenticación para interactuar con la API REST de ChaProde.
 */
@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit,
    repository: AuthRepository = AuthRepository()
) {
    // --------------------------------------------------------------------------------------------
    // ESTADO LOCAL DEL FORMULARIO DE REGISTRO
    // --------------------------------------------------------------------------------------------
    var username by remember { mutableStateOf("") }                  // Nombre de usuario elegido
    var email by remember { mutableStateOf("") }                        // Correo electrónico del usuario
    var password by remember { mutableStateOf("") }                     // Contraseña (mínimo 6 caracteres)
    var isLoading by remember { mutableStateOf(false) }                 // Indicador visual de petición en progreso
    var errorMessage by remember { mutableStateOf<String?>(null) }      // Mensaje de error retornado por la API
    val scope = rememberCoroutineScope()                                // Corrutina para despachar el Flow de registro

    RegisterContent(
        username = username,
        onUsernameChange = { username = it; errorMessage = null },
        email = email,
        onEmailChange = { email = it; errorMessage = null },
        password = password,
        onPasswordChange = { password = it; errorMessage = null },
        isLoading = isLoading,
        errorMessage = errorMessage,
        onRegisterClick = {
            if (username.isBlank() || email.isBlank() || password.length < 6) {
                errorMessage = "Completa los datos (contraseña mínimo 6 caracteres)."
                return@RegisterContent
            }
            scope.launch {
                repository.register(username, email, password).collect { resource ->
                    when (resource) {
                        is Resource.Loading -> isLoading = true
                        is Resource.Success -> {
                            isLoading = false
                            onRegisterSuccess()
                        }
                        is Resource.Error -> {
                            isLoading = false
                            errorMessage = resource.message
                        }
                    }
                }
            }
        },
        onNavigateToLogin = onNavigateToLogin
    )
}

/**
 * Pantalla Stateless de Registro:
 * Renderiza la tarjeta visual del formulario de alta con validación en vivo,
 * campos adaptados con teclado IME inteligente y navegación fluida hacia login.
 *
 * @param username Valor actual del campo nombre de usuario.
 * @param onUsernameChange Notifica cambios en el nombre de usuario.
 * @param email Valor actual del campo correo electrónico.
 * @param onEmailChange Notifica cambios en el correo electrónico.
 * @param password Valor actual del campo contraseña.
 * @param onPasswordChange Notifica cambios en la contraseña.
 * @param isLoading Bandera de progreso activo para deshabilitar botones y mostrar loader.
 * @param errorMessage Mensaje descriptivo de error si la validación o el backend fallan.
 * @param onRegisterClick Acción para enviar el formulario de registro.
 * @param onNavigateToLogin Acción para navegar a la pantalla de inicio de sesión.
 */
@Composable
fun RegisterContent(
    username: String,
    onUsernameChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
    onRegisterClick: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ChaProdeBg)
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(ChaProdeSky.copy(alpha = 0.20f), Color.Transparent),
                        center = androidx.compose.ui.geometry.Offset(size.width * 0.95f, size.height * 0.05f),
                        radius = size.width * 0.65f
                    )
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(ChaProdeGold.copy(alpha = 0.16f), Color.Transparent),
                        center = androidx.compose.ui.geometry.Offset(size.width * 0.05f, size.height * 0.95f),
                        radius = size.width * 0.70f
                    )
                )
            }
            .padding(horizontal = 20.dp, vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 440.dp)
                .verticalScroll(rememberScrollState())
                .border(1.dp, ChaProdeBorder, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = ChaProdeSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Eyebrow / Kicker
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(bottom = 18.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(ChaProdeSuccess, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ÚNETE A LA COMPETENCIA",
                        color = ChaProdeSky,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.2.sp
                    )
                }

                // Logotipo oficial
                ChaProdeLogo()

                Spacer(modifier = Modifier.height(28.dp))

                // Input Usuario
                ChaProdeTextField(
                    value = username,
                    onValueChange = onUsernameChange,
                    label = "Nombre de Usuario",
                    leadingIcon = Icons.Default.Person,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Input Email
                ChaProdeTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    label = "Correo Electrónico",
                    leadingIcon = Icons.Default.Email,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Input Contraseña
                ChaProdeTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    label = "Contraseña (mínimo 6 caracteres)",
                    leadingIcon = Icons.Default.Lock,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { onRegisterClick() })
                )

                if (errorMessage != null) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = ChaProdeDanger.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = errorMessage,
                            color = Color(0xFFF87171),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(12.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(26.dp))

                // Botón Crear cuenta
                ChaProdeButton(
                    text = "Crear mi cuenta",
                    variant = ButtonVariant.Primary,
                    trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                    isLoading = isLoading,
                    enabled = username.isNotBlank() && email.isNotBlank() && password.length >= 6,
                    onClick = onRegisterClick
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Enlace a Login
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "¿Ya tienes una cuenta? ",
                        color = ChaProdeMuted,
                        fontSize = 13.sp
                    )
                    TextButton(onClick = onNavigateToLogin, contentPadding = PaddingValues(0.dp)) {
                        Text(
                            text = "Inicia sesión",
                            color = ChaProdeSky,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Pie de tarjeta con sello de seguridad
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(width = 0.5.dp, color = ChaProdeBorder.copy(alpha = 0.6f), shape = RoundedCornerShape(8.dp))
                        .padding(vertical = 10.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = ChaProdeSubtle,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Datos protegidos y pronósticos seguros",
                        color = ChaProdeSubtle,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F172A)
@Composable
fun RegisterScreenPreview() {
    RegisterContent(
        username = "seba",
        onUsernameChange = {},
        email = "seba@chaprode.com",
        onEmailChange = {},
        password = "password123",
        onPasswordChange = {},
        isLoading = false,
        errorMessage = null,
        onRegisterClick = {},
        onNavigateToLogin = {}
    )
}
