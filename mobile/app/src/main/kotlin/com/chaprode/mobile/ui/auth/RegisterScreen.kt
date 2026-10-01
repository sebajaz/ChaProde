package com.chaprode.mobile.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.chaprode.mobile.ui.components.ChaProdeButton
import com.chaprode.mobile.ui.components.ChaProdeTextField
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit,
    repository: AuthRepository = AuthRepository()
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

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
                Text(
                    text = "Crear Cuenta",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = "Unite a ChaProde y pronosticá el torneo",
                    fontSize = 14.sp,
                    color = Color(0xFF94A3B8),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                )

                ChaProdeTextField(
                    value = username,
                    onValueChange = onUsernameChange,
                    label = "Nombre de Usuario",
                    leadingIcon = Icons.Default.Person,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                Spacer(modifier = Modifier.height(16.dp))

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
                        color = Color(0xFFEF4444).copy(alpha = 0.15f)
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

                Spacer(modifier = Modifier.height(28.dp))

                ChaProdeButton(
                    text = "Registrarme",
                    isLoading = isLoading,
                    enabled = username.isNotBlank() && email.isNotBlank() && password.length >= 6,
                    onClick = onRegisterClick
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "¿Ya tenés una cuenta? ",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp
                    )
                    TextButton(onClick = onNavigateToLogin, contentPadding = PaddingValues(0.dp)) {
                        Text(
                            text = "Iniciar Sesión",
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
