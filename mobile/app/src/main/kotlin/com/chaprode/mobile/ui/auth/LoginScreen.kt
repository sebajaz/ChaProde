package com.chaprode.mobile.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.chaprode.mobile.ui.components.ChaProdeButton
import com.chaprode.mobile.ui.components.ChaProdeTextField

/**
 * Pantalla Stateful: maneja el ViewModel y las navegaciones
 */
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onLoginSuccess()
        }
    }

    LoginContent(
        state = state,
        onEvent = viewModel::onEvent,
        onNavigateToRegister = onNavigateToRegister
    )
}

/**
 * Pantalla Stateless: puramente declarativa, ideal para testing y Previews en Android Studio
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
            .background(Color(0xFF0F172A))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header / Logo
                Text(
                    text = "🏆 ChaProde",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFFBBF24),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Pronósticos Deportivos",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF38BDF8),
                    modifier = Modifier.padding(top = 2.dp)
                )

                Text(
                    text = "Iniciá sesión para predecir los partidos y competir en la tabla de posiciones.",
                    fontSize = 13.sp,
                    color = Color(0xFF94A3B8),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 12.dp, bottom = 28.dp)
                )

                // Input Usuario / Email
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

                // Input Contraseña
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
                                tint = Color(0xFF94A3B8)
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
                        color = Color(0xFFEF4444).copy(alpha = 0.15f)
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

                Spacer(modifier = Modifier.height(28.dp))

                // Botón Iniciar Sesión
                ChaProdeButton(
                    text = "Iniciar Sesión",
                    isLoading = state.isLoading,
                    enabled = state.isLoginButtonEnabled,
                    onClick = { onEvent(AuthUiEvent.OnLoginClicked) }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Enlace a Registro
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "¿No tenés una cuenta? ",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp
                    )
                    TextButton(
                        onClick = onNavigateToRegister,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "Registrate acá",
                            color = Color(0xFF38BDF8),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
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

@Preview(name = "Login Cargando", showBackground = true, backgroundColor = 0xFF0F172A)
@Composable
fun LoginScreenLoadingPreview() {
    LoginContent(
        state = AuthUiState(
            usernameOrEmail = "seba",
            password = "password123",
            isLoading = true
        ),
        onEvent = {},
        onNavigateToRegister = {}
    )
}

@Preview(name = "Login con Error", showBackground = true, backgroundColor = 0xFF0F172A)
@Composable
fun LoginScreenErrorPreview() {
    LoginContent(
        state = AuthUiState(
            usernameOrEmail = "seba",
            password = "wrong",
            errorMessage = "Credenciales incorrectas. Verifica tu usuario y contraseña."
        ),
        onEvent = {},
        onNavigateToRegister = {}
    )
}
