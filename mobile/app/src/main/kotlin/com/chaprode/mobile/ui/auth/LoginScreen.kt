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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chaprode.mobile.data.remote.ApiConfig
import com.chaprode.mobile.ui.components.ButtonVariant
import com.chaprode.mobile.ui.components.ChaProdeButton
import com.chaprode.mobile.ui.components.ChaProdeLogo
import com.chaprode.mobile.ui.components.ChaProdeTextField
import com.chaprode.mobile.ui.theme.*

/**
 * Pantalla Stateful de Inicio de Sesión:
 * Administra el ciclo de vida, la recolección reactiva del estado de `AuthViewModel`
 * y los efectos secundarios de navegación ante un login exitoso.
 *
 * @param viewModel Instancia del ViewModel responsable de la lógica de autenticación.
 * @param onNavigateToRegister Navegación a la pantalla de registro de nuevo usuario.
 * @param onLoginSuccess Navegación al destino principal (`Home`) cuando la sesión se inicia con éxito.
 */
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    // Recolecta el estado reactivo de la UI emitido por el StateFlow del ViewModel
    val state by viewModel.uiState.collectAsState()

    // Reacciona al cambio en state.isSuccess para redirigir al usuario al Home de la aplicación
    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onLoginSuccess()
        }
    }

    // Delega la renderización a la función visual pura (Stateless)
    LoginContent(
        state = state,
        onEvent = viewModel::onEvent,
        onNavigateToRegister = onNavigateToRegister
    )
}

/**
 * Pantalla Stateless de Inicio de Sesión:
 * Implementa el diseño visual de Figma Make con:
 * - Fondo oscuro ambiental con resplandores radiales (Sky Blue superior y Gold inferior).
 * - Tarjeta central de cristal oscuro con bordes redondeados de 24dp.
 * - Logotipo tipográfico y badge de bienvenida.
 * - Campos de entrada estilizados para usuario/email y contraseña con visibilidad alternable.
 * - Botón de llamada a la acción con gradiente cyan y spinner de carga.
 * - Diálogo técnico de configuración de servidor backend.
 *
 * @param state Estado inmutable de la pantalla (campos de texto, visibilidad, carga y errores).
 * @param onEvent Emisor de intenciones del usuario hacia el ViewModel (MVI pattern).
 * @param onNavigateToRegister Callback para alternar a la pantalla de registro.
 */
@Composable
fun LoginContent(
    state: AuthUiState,
    onEvent: (AuthUiEvent) -> Unit,
    onNavigateToRegister: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ChaProdeBg)
            .drawBehind {
                // Brillo ambiental superior derecho (Sky Blue)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(ChaProdeSky.copy(alpha = 0.20f), Color.Transparent),
                        center = androidx.compose.ui.geometry.Offset(size.width * 0.95f, size.height * 0.05f),
                        radius = size.width * 0.65f
                    )
                )
                // Brillo ambiental inferior izquierdo (Gold)
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
                // Eyebrow / Kicker de bienvenida
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
                        text = "TU PASIÓN, TU PRONÓSTICO",
                        color = ChaProdeSky,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.2.sp
                    )
                }

                // Logotipo oficial estilizado
                ChaProdeLogo()

                Spacer(modifier = Modifier.height(28.dp))

                // Campo Usuario / Email
                ChaProdeTextField(
                    value = state.usernameOrEmail,
                    onValueChange = { onEvent(AuthUiEvent.OnUsernameOrEmailChanged(it)) },
                    label = "Usuario o Email",
                    leadingIcon = Icons.Default.Person,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Campo Contraseña con botón de mostrar/ocultar
                ChaProdeTextField(
                    value = state.password,
                    onValueChange = { onEvent(AuthUiEvent.OnPasswordChanged(it)) },
                    label = "Contraseña",
                    leadingIcon = Icons.Default.Lock,
                    visualTransformation = if (state.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { onEvent(AuthUiEvent.OnTogglePasswordVisibility) }) {
                            Icon(
                                imageVector = if (state.isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (state.isPasswordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                                tint = ChaProdeMuted
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { onEvent(AuthUiEvent.OnLoginClicked) }
                    )
                )

                // Mensaje de Error
                if (state.errorMessage != null) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = ChaProdeDanger.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = state.errorMessage,
                            color = Color(0xFFF87171),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(12.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(26.dp))

                // Botón Iniciar Sesión con gradiente CTA y flecha lateral
                ChaProdeButton(
                    text = "Iniciar Sesión",
                    variant = ButtonVariant.Primary,
                    trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                    isLoading = state.isLoading,
                    enabled = state.isLoginButtonEnabled,
                    onClick = { onEvent(AuthUiEvent.OnLoginClicked) }
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Enlace a Registro
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "¿No tienes una cuenta? ",
                        color = ChaProdeMuted,
                        fontSize = 13.sp
                    )
                    TextButton(
                        onClick = onNavigateToRegister,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "Regístrate aquí",
                            color = ChaProdeSky,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Pie de tarjeta: Sello de seguridad (Figma Make reference: .login-card__footer)
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

                Spacer(modifier = Modifier.height(14.dp))

                // Diálogo de configuración del Servidor (Preservado para depuración)
                var showServerDialog by remember { mutableStateOf(false) }
                var currentServerUrl by remember { mutableStateOf(ApiConfig.BASE_URL) }
                var serverInput by remember { mutableStateOf(currentServerUrl) }

                TextButton(
                    onClick = { 
                        serverInput = currentServerUrl
                        showServerDialog = true 
                    },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "⚙️ Servidor: $currentServerUrl",
                        color = ChaProdeSubtle,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }

                if (showServerDialog) {
                    AlertDialog(
                        onDismissRequest = { showServerDialog = false },
                        title = { Text("⚙️ Servidor Backend", color = Color.White, fontWeight = FontWeight.Bold) },
                        text = {
                            Column {
                                Text(
                                    "Selecciona o escribe la IP de tu servidor backend:",
                                    color = ChaProdeMuted,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                                ChaProdeTextField(
                                    value = serverInput,
                                    onValueChange = { serverInput = it },
                                    label = "URL Base (ej: http://10.0.2.2:8080)"
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { serverInput = ApiConfig.DEFAULT_PRODUCTION_TUNNEL_URL },
                                        modifier = Modifier.weight(1.2f)
                                    ) {
                                        Text("🌐 4G/Nube", fontSize = 10.sp, color = ChaProdeSuccess)
                                    }
                                    OutlinedButton(
                                        onClick = { serverInput = ApiConfig.DEFAULT_PC_WIFI_URL },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Wi-Fi PC", fontSize = 10.sp)
                                    }
                                    OutlinedButton(
                                        onClick = { serverInput = ApiConfig.DEFAULT_EMULATOR_URL },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Emulador", fontSize = 10.sp)
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    ApiConfig.setBaseUrl(serverInput)
                                    currentServerUrl = ApiConfig.BASE_URL
                                    showServerDialog = false
                                }
                            ) {
                                Text("Guardar", fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showServerDialog = false }) {
                                Text("Cancelar", color = ChaProdeMuted)
                            }
                        },
                        containerColor = ChaProdeSurface
                    )
                }
            }
        }
    }
}

@Preview(name = "Login Normal", showBackground = true, backgroundColor = 0xFF0F172A)
@Composable
fun LoginScreenPreview() {
    LoginContent(
        state = AuthUiState(
            usernameOrEmail = "seba",
            password = "password123"
        ),
        onEvent = {},
        onNavigateToRegister = {}
    )
}
