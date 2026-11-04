package com.chaprode.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chaprode.mobile.data.local.preferences.SessionManager
import com.chaprode.mobile.model.User
import com.chaprode.mobile.ui.components.ButtonVariant
import com.chaprode.mobile.ui.components.ChaProdeButton
import com.chaprode.mobile.ui.theme.*

/**
 * Pantalla provisional de Perfil (Etapa 2).
 * Proporciona el destino de navegación requerido por el BottomNav de cinco secciones,
 * utilizando únicamente los datos reales de la sesión actual (sin información ficticia).
 * La implementación visual completa con ajustes y nivel de usuario se desarrollará en la Etapa 8.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val user: User? = SessionManager.getUser()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "👤 Mi Perfil",
                        fontWeight = FontWeight.Bold,
                        color = ChaProdeGold,
                        fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ChaProdeBg)
            )
        },
        containerColor = ChaProdeBg
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Avatar con inicial del usuario real
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(CircleShape)
                    .background(ChaProdeSurfaceRaised)
                    .border(3.dp, ChaProdeSky, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (user?.username?.firstOrNull() ?: 'U').uppercase(),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Nombre y Email reales de la sesión
            Text(
                text = user?.username ?: "Usuario ChaProde",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            val email = user?.email
            if (!email.isNullOrBlank()) {
                Text(
                    text = email,
                    fontSize = 13.sp,
                    color = ChaProdeMuted,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Surface(
                modifier = Modifier.padding(top = 8.dp),
                shape = RoundedCornerShape(12.dp),
                color = ChaProdeGold.copy(alpha = 0.12f)
            ) {
                Text(
                    text = "Rol: ${user?.rol ?: "USER"}",
                    color = ChaProdeGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Tarjeta informativa de destino provisional
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ChaProdeSurface)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = ChaProdeSky,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Destino Provisional (Etapa 2)",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "La interfaz completa de Perfil con estadísticas, nivel de usuario y preferencias se construirá en la Etapa 8.",
                            color = ChaProdeMuted,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Botón de Cerrar Sesión
            ChaProdeButton(
                text = "Cerrar Sesión",
                onClick = onLogout,
                variant = ButtonVariant.Secondary,
                leadingIcon = Icons.AutoMirrored.Filled.ExitToApp,
                contentColor = ChaProdeDanger
            )
        }
    }
}
