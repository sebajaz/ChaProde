package com.chaprode.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chaprode.mobile.data.local.preferences.SessionManager
import com.chaprode.mobile.data.remote.ApiConfig
import com.chaprode.mobile.model.User
import com.chaprode.mobile.ui.components.ChaProdeTextField
import com.chaprode.mobile.ui.prediction.PredictionViewModel
import com.chaprode.mobile.ui.ranking.RankingViewModel
import com.chaprode.mobile.ui.theme.*
import kotlinx.coroutines.launch

/**
 * Pantalla de Perfil de Usuario completa (Figma Make reference: .profile, .profile-stats, .settings-list).
 * Presenta la identidad del usuario real, métricas acumuladas de pronósticos,
 * panel de configuración y cierre de sesión seguro.
 */
@Composable
fun ProfileScreen(
    predictionViewModel: PredictionViewModel? = null,
    rankingViewModel: RankingViewModel? = null,
    showBack: Boolean = false,
    onNavigateBack: () -> Unit = {},
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val user: User? = SessionManager.getUser()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Cálculo de estadísticas reales del usuario a partir de los ViewModels
    val predictionState = predictionViewModel?.uiState?.collectAsState()?.value
    val rankingState = rankingViewModel?.uiState?.collectAsState()?.value

    val myPredictions = remember(predictionState?.matches) {
        predictionState?.matches?.filter { it.miPronosticoLocal != null && it.miPronosticoVisitante != null } ?: emptyList()
    }

    val userRank = remember(rankingState?.ranking) {
        rankingState?.ranking?.firstOrNull { it.isCurrentUser }
    }

    val totalPoints = remember(myPredictions, userRank) {
        userRank?.puntosTotales ?: myPredictions.sumOf { it.puntosGanados }
    }

    val exactCount = remember(myPredictions, userRank) {
        userRank?.plenosExactos ?: myPredictions.count { it.puntosGanados == 3 }
    }

    val rankPositionText = remember(userRank) {
        if (userRank != null) "#${userRank.posicion}" else "# -"
    }

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showServerConfigDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = ChaProdeBg
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Cabecera superior
            ProfileTopBar(
                showBack = showBack,
                onNavigateBack = onNavigateBack
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 1. IDENTIDAD DEL USUARIO (Figma Make: .profile)
            ProfileIdentitySection(
                user = user
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 2. MÉTRICAS DE RENDIMIENTO (Figma Make: .profile-stats)
            ProfileStatsCard(
                totalPoints = totalPoints,
                exactCount = exactCount,
                rankPosition = rankPositionText
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 3. TÍTULO DE SECCIÓN: CONFIGURACIÓN
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "CUENTA",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.2.sp,
                    color = ChaProdeGold
                )
                Text(
                    text = "Configuración y Preferencias",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ChaProdeText,
                    letterSpacing = (-0.5).sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 4. LISTA DE AJUSTES Y ACCIONES (Figma Make: .settings-list)
            SettingsListCard(
                notificationsEnabled = notificationsEnabled,
                serverBaseUrl = ApiConfig.BASE_URL,
                onEditProfileClick = { showEditProfileDialog = true },
                onNotificationsToggle = {
                    notificationsEnabled = !notificationsEnabled
                    coroutineScope.launch {
                        val msg = if (notificationsEnabled) "Notificaciones activadas" else "Notificaciones silenciadas"
                        snackbarHostState.showSnackbar(msg)
                    }
                },
                onServerConfigClick = { showServerConfigDialog = true },
                onLogoutClick = { showLogoutDialog = true }
            )

            Spacer(modifier = Modifier.height(28.dp))

            // 5. PIE DE PÁGINA INSTITUCIONAL
            Text(
                text = "ChaProde Mobile v1.0.0",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = ChaProdeSubtle
            )
            Text(
                text = "Tu pasión futbolera, tus pronósticos en tiempo real",
                fontSize = 10.sp,
                color = ChaProdeMuted,
                modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
            )
        }
    }

    // DIÁLOGO DE CIERRE DE SESIÓN
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(ChaProdeDanger.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = null,
                            tint = ChaProdeDanger,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "¿Cerrar Sesión?",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ChaProdeText
                    )
                }
            },
            text = {
                Text(
                    text = "¿Estás seguro de que deseas salir de tu cuenta? Deberás iniciar sesión nuevamente para enviar o consultar tus pronósticos.",
                    color = ChaProdeMuted,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ChaProdeDanger),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cerrar Sesión", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar", color = ChaProdeMuted, fontSize = 13.sp)
                }
            },
            containerColor = ChaProdeSurface,
            shape = RoundedCornerShape(22.dp)
        )
    }

    // DIÁLOGO DE CONFIGURACIÓN DEL SERVIDOR
    if (showServerConfigDialog) {
        var tempUrl by remember { mutableStateOf(ApiConfig.BASE_URL) }
        AlertDialog(
            onDismissRequest = { showServerConfigDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(ChaProdeSky.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Dns,
                            contentDescription = null,
                            tint = ChaProdeSky,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Servidor API Backend",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ChaProdeText
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "Configura la dirección IP local de tu servidor Ktor en Docker para pruebas en celular físico o emulador.",
                        color = ChaProdeMuted,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(bottom = 14.dp)
                    )
                    ChaProdeTextField(
                        value = tempUrl,
                        onValueChange = { tempUrl = it },
                        label = "URL Base de la API"
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(
                            onClick = {
                                tempUrl = ApiConfig.DEFAULT_PC_WIFI_URL
                            }
                        ) {
                            Text("Wi-Fi (PC)", fontSize = 11.sp, color = ChaProdeSky)
                        }
                        TextButton(
                            onClick = {
                                tempUrl = ApiConfig.DEFAULT_EMULATOR_URL
                            }
                        ) {
                            Text("Emulador (10.0.2.2)", fontSize = 11.sp, color = ChaProdeMuted)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        ApiConfig.setBaseUrl(tempUrl)
                        showServerConfigDialog = false
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("URL del servidor actualizada: ${ApiConfig.BASE_URL}")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ChaProdeCta),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Guardar", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showServerConfigDialog = false }) {
                    Text("Cancelar", color = ChaProdeMuted, fontSize = 13.sp)
                }
            },
            containerColor = ChaProdeSurface,
            shape = RoundedCornerShape(22.dp)
        )
    }

    // DIÁLOGO INFORMATIVO DE EDICIÓN DE PERFIL
    if (showEditProfileDialog) {
        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = {
                Text(
                    text = "Editar Perfil",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ChaProdeText
                )
            },
            text = {
                Text(
                    text = "La edición de avatar personalizado y biografía estará disponible en la próxima actualización de ChaProde.",
                    color = ChaProdeMuted,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Entendido", color = ChaProdeSky, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = ChaProdeSurface,
            shape = RoundedCornerShape(22.dp)
        )
    }
}

/**
 * Barra superior de la pantalla de perfil
 */
@Composable
fun ProfileTopBar(
    showBack: Boolean,
    onNavigateBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showBack) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(ChaProdeSurface)
                    .border(1.dp, ChaProdeBorder, RoundedCornerShape(10.dp))
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = ChaProdeText,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
        }

        Text(
            text = "Mi Perfil",
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = ChaProdeText,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Sección de Identidad de Usuario (Figma Make: .profile)
 */
@Composable
fun ProfileIdentitySection(user: User?) {
    val username = user?.username ?: "Leo_El10"
    val initials = username.take(2).uppercase()
    val email = user?.email ?: "${username.lowercase()}@chaprode.com"
    val rol = user?.rol ?: "USER"

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar circular de 82×82dp con resplandor cyan (Figma Make: .profile__avatar)
        Box(
            modifier = Modifier
                .size(82.dp)
                .drawBehind {
                    // Halo exterior cyan (Figma Make: box-shadow: 0 0 0 6px rgba(56,189,248,.08))
                    drawCircle(
                        color = ChaProdeSky.copy(alpha = 0.12f),
                        radius = (size.width / 2f) + 6.dp.toPx()
                    )
                }
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF075985), ChaProdeSurface)
                    )
                )
                .border(3.dp, ChaProdeSky, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Nombre de Usuario (Figma Make: .profile__name)
        Text(
            text = username,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = ChaProdeText,
            letterSpacing = (-0.5).sp
        )

        // Email o Handle
        Text(
            text = "@${username.lowercase()} · $email",
            fontSize = 11.5.sp,
            color = ChaProdeMuted,
            modifier = Modifier.padding(top = 2.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Badge de Nivel y Rol (Figma Make: .profile__level)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(ChaProdeGold.copy(alpha = 0.12f))
                .border(1.dp, ChaProdeGold.copy(alpha = 0.28f), RoundedCornerShape(20.dp))
                .padding(horizontal = 12.dp, vertical = 5.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = ChaProdeGold,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Rol: $rol · Participante Prode",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = ChaProdeGold
                )
            }
        }
    }
}

/**
 * Tarjeta de 3 Métricas de Rendimiento (Figma Make: .profile-stats)
 */
@Composable
fun ProfileStatsCard(
    totalPoints: Int,
    exactCount: Int,
    rankPosition: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = ChaProdeSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, ChaProdeBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Columna 1: Puntos
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$totalPoints",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = ChaProdeGold
                )
                Text(
                    text = "Puntos",
                    fontSize = 10.sp,
                    color = ChaProdeMuted,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // Separador vertical
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(34.dp)
                    .background(ChaProdeBorder)
            )

            // Columna 2: Exactos
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$exactCount",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = ChaProdeSuccess
                )
                Text(
                    text = "Exactos",
                    fontSize = 10.sp,
                    color = ChaProdeMuted,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // Separador vertical
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(34.dp)
                    .background(ChaProdeBorder)
            )

            // Columna 3: Ranking
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = rankPosition,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = ChaProdeSky
                )
                Text(
                    text = "Ranking",
                    fontSize = 10.sp,
                    color = ChaProdeMuted,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

/**
 * Lista de Configuraciones y Ajustes (Figma Make: .settings-list)
 */
@Composable
fun SettingsListCard(
    notificationsEnabled: Boolean,
    serverBaseUrl: String,
    onEditProfileClick: () -> Unit,
    onNotificationsToggle: () -> Unit,
    onServerConfigClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = ChaProdeSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, ChaProdeBorder)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Opción 1: Editar Perfil
            SettingsListItem(
                icon = Icons.Default.Person,
                iconTint = ChaProdeSky,
                title = "Editar perfil",
                subtitle = "Nombre y datos de la cuenta",
                onClick = onEditProfileClick
            )

            HorizontalDivider(color = ChaProdeBorder, thickness = 1.dp)

            // Opción 2: Notificaciones
            SettingsListItem(
                icon = Icons.Default.Notifications,
                iconTint = ChaProdeGold,
                title = "Notificaciones",
                subtitle = "Avisos de cierre y resultados en vivo",
                badge = {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (notificationsEnabled) ChaProdeSuccess.copy(alpha = 0.14f) else ChaProdeMuted.copy(alpha = 0.14f)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (notificationsEnabled) "Activadas" else "Pausadas",
                            color = if (notificationsEnabled) ChaProdeSuccess else ChaProdeMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                onClick = onNotificationsToggle
            )

            HorizontalDivider(color = ChaProdeBorder, thickness = 1.dp)

            // Opción 3: Servidor API Backend
            SettingsListItem(
                icon = Icons.Default.Dns,
                iconTint = ChaProdeSky,
                title = "Servidor API Backend",
                subtitle = serverBaseUrl,
                onClick = onServerConfigClick
            )

            HorizontalDivider(color = ChaProdeBorder, thickness = 1.dp)

            // Opción 4: Cerrar Sesión (Danger / Red)
            SettingsListItem(
                icon = Icons.AutoMirrored.Filled.ExitToApp,
                iconTint = ChaProdeDanger,
                title = "Cerrar sesión",
                subtitle = "Salir de tu cuenta en este dispositivo",
                isDanger = true,
                onClick = onLogoutClick
            )
        }
    }
}

@Composable
fun SettingsListItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    isDanger: Boolean = false,
    badge: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconTint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDanger) ChaProdeDanger else ChaProdeText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                fontSize = 10.5.sp,
                color = if (isDanger) ChaProdeDanger.copy(alpha = 0.7f) else ChaProdeMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (badge != null) {
            badge()
            Spacer(modifier = Modifier.width(8.dp))
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = if (isDanger) ChaProdeDanger.copy(alpha = 0.6f) else ChaProdeMuted,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Preview(name = "Profile Screen Moderno", showBackground = true, backgroundColor = 0xFF0F172A)
@Composable
fun ProfileScreenModernPreview() {
    ProfileScreen(
        onLogout = {}
    )
}
