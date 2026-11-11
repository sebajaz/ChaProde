package com.chaprode.mobile.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chaprode.mobile.data.local.preferences.SessionManager
import com.chaprode.mobile.model.LeagueItem
import com.chaprode.mobile.model.RankingUserItem
import com.chaprode.mobile.ui.components.ChaProdeButton
import com.chaprode.mobile.ui.components.ChaProdeTextField
import com.chaprode.mobile.ui.league.LeagueUiEvent
import com.chaprode.mobile.ui.league.LeagueUiState
import com.chaprode.mobile.ui.league.LeagueViewModel
import com.chaprode.mobile.ui.theme.*
import kotlinx.coroutines.launch

/**
 * =========================================================================================
 * LeaguesScreen: Pantalla de Comunidades y Ligas Privadas (Torneos de Amigos).
 * =========================================================================================
 * Propósito:
 * Permite a los usuarios crear grupos cerrados con amigos, compañeros de trabajo o familiares,
 * compartir un código de acceso único de 6 caracteres, y competir en una tabla de posiciones privada.
 * 
 * Funcionalidades clave:
 * 1. Banner Héroe deportivo con degradado y llamado a la acción ([LeagueBanner]).
 * 2. Acciones rápidas: "Crear Nueva Liga" y "Unirse con Código" ([LeagueActionsRow]).
 * 3. Tarjeta de liga ([LeagueCard]) con badge de rol (Admin vs Miembro) y copiado de código al portapapeles.
 * 4. Diálogos modales para creación ([CreateLeagueDialog]), unión ([JoinLeagueDialog])
 *    y tabla de posiciones de la liga ([LeagueDetailRankingDialog]).
 * 
 * @param viewModel ViewModel que gestiona las llamadas de ligas a la API Ktor.
 * @param showBack Si es true, renderiza un botón para volver atrás.
 * @param onNavigateBack Callback de navegación hacia atrás.
 */
@Composable
fun LeaguesScreen(
    viewModel: LeagueViewModel,
    showBack: Boolean = false,
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    LeaguesContent(
        state = state,
        onEvent = viewModel::onEvent,
        showBack = showBack,
        onNavigateBack = onNavigateBack
    )
}

/**
 * Contenido principal composable de Ligas Privadas.
 */
@Composable
fun LeaguesContent(
    state: LeagueUiState,
    onEvent: (LeagueUiEvent) -> Unit,
    showBack: Boolean = false,
    onNavigateBack: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Carga las ligas a las que pertenece el usuario al abrir la pantalla
    LaunchedEffect(Unit) {
        onEvent(LeagueUiEvent.LoadLeagues)
    }

    // Muestra notificaciones flotantes (Snackbars) ante éxito o error
    LaunchedEffect(state.successMessage, state.errorMessage) {
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            onEvent(LeagueUiEvent.OnDismissMessage)
        }
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            onEvent(LeagueUiEvent.OnDismissMessage)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = ChaProdeBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Cabecera superior con botón de actualizar
            LeaguesTopBar(
                showBack = showBack,
                onNavigateBack = onNavigateBack,
                onRefresh = { onEvent(LeagueUiEvent.LoadLeagues) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 28.dp)
            ) {
                // -------------------------------------------------------------------------
                // 1. BANNER HÉROE (Figma Make: .league-banner)
                // -------------------------------------------------------------------------
                item {
                    LeagueBanner()
                }

                // -------------------------------------------------------------------------
                // 2. BOTONES DE ACCIÓN: CREAR Y UNIRSE (Figma Make: .league-actions)
                // -------------------------------------------------------------------------
                item {
                    LeagueActionsRow(
                        onCreateClick = { onEvent(LeagueUiEvent.OnOpenCreateDialog(true)) },
                        onJoinClick = { onEvent(LeagueUiEvent.OnOpenJoinDialog(true)) }
                    )
                }

                // -------------------------------------------------------------------------
                // 3. TÍTULO DE SECCIÓN
                // -------------------------------------------------------------------------
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Column {
                        Text(
                            text = "TUS COMUNIDADES",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.2.sp,
                            color = ChaProdeGold
                        )
                        Text(
                            text = if (state.leagues.isNotEmpty()) "Mis Ligas (${state.leagues.size})" else "Mis Ligas",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ChaProdeText,
                            letterSpacing = (-0.5).sp
                        )
                    }
                }

                // -------------------------------------------------------------------------
                // 4. LISTADO O ESTADOS DE CARGA/VACÍO
                // -------------------------------------------------------------------------
                if (state.isLoading && state.leagues.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = ChaProdeSky, strokeWidth = 3.dp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Cargando tus ligas...",
                                    color = ChaProdeMuted,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                } else if (state.leagues.isEmpty()) {
                    // Estado vacío cuando el usuario aún no tiene ligas creadas ni unidas
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = ChaProdeSurface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ChaProdeBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .background(ChaProdeSky.copy(alpha = 0.1f), shape = CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Group,
                                        contentDescription = null,
                                        tint = ChaProdeSky,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Sin Ligas Privadas Aún",
                                    fontWeight = FontWeight.Bold,
                                    color = ChaProdeText,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Crea un grupo con tus amigos o ingresa con un código de invitación para competir en privado.",
                                    color = ChaProdeMuted,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    // Lista de tarjetas de ligas
                    items(state.leagues, key = { it.id }) { league ->
                        LeagueCard(
                            league = league,
                            onCopyCode = {
                                // Copia el código de acceso al portapapeles del sistema operativo
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Código de Liga", league.codigoAcceso)
                                clipboard.setPrimaryClip(clip)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Código '${league.codigoAcceso}' copiado al portapapeles")
                                }
                            },
                            onViewLeaderboard = {
                                // Abre el modal de ranking específico de esta liga
                                onEvent(LeagueUiEvent.OnSelectLeague(league.id))
                            }
                        )
                    }
                }
            }
        }
    }

    // -------------------------------------------------------------------------------------
    // Diálogos Modales (Figma Make: .modal)
    // -------------------------------------------------------------------------------------
    if (state.isCreateDialogOpen) {
        CreateLeagueDialog(
            leagueName = state.newLeagueName,
            onNameChange = { onEvent(LeagueUiEvent.OnNewLeagueNameChanged(it)) },
            onConfirm = { onEvent(LeagueUiEvent.OnCreateLeagueClicked) },
            onDismiss = { onEvent(LeagueUiEvent.OnOpenCreateDialog(false)) }
        )
    }

    if (state.isJoinDialogOpen) {
        JoinLeagueDialog(
            joinCode = state.joinCode,
            onCodeChange = { onEvent(LeagueUiEvent.OnJoinCodeChanged(it)) },
            onConfirm = { onEvent(LeagueUiEvent.OnJoinLeagueClicked) },
            onDismiss = { onEvent(LeagueUiEvent.OnOpenJoinDialog(false)) }
        )
    }

    state.selectedLeagueDetail?.let { detail ->
        LeagueDetailRankingDialog(
            detail = detail,
            onDismiss = { onEvent(LeagueUiEvent.OnCloseDetail) }
        )
    }
}

/**
 * Cabecera superior con título y botón de recarga.
 */
@Composable
fun LeaguesTopBar(
    showBack: Boolean,
    onNavigateBack: () -> Unit,
    onRefresh: () -> Unit
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
            text = "Ligas y Comunidades",
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = ChaProdeText,
            modifier = Modifier.weight(1f)
        )

        IconButton(
            onClick = onRefresh,
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(ChaProdeSurface)
                .border(1.dp, ChaProdeBorder, RoundedCornerShape(10.dp))
        ) {
            Icon(
                Icons.Default.Refresh,
                contentDescription = "Actualizar",
                tint = ChaProdeMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Banner Héroe de Ligas Privadas (Figma Make: .league-banner).
 */
@Composable
fun LeagueBanner() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                // Resplandor radial celeste en la esquina superior derecha
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(ChaProdeSky.copy(alpha = 0.22f), Color.Transparent),
                        center = Offset(size.width * 0.9f, size.height * 0.2f),
                        radius = size.width * 0.45f
                    )
                )
            },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = androidx.compose.foundation.BorderStroke(1.dp, ChaProdeSky.copy(alpha = 0.32f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF1E3048), ChaProdeSurface)
                    )
                )
                .padding(22.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Ícono de Comunidad dentro de caja redondeada
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .background(ChaProdeSky.copy(alpha = 0.12f))
                        .border(1.dp, ChaProdeSky.copy(alpha = 0.25f), RoundedCornerShape(15.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Group,
                        contentDescription = null,
                        tint = ChaProdeSky,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "LIGAS PRIVADAS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.4.sp,
                    color = ChaProdeSky
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Compite con\ntus amigos",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 28.sp,
                    letterSpacing = (-0.5).sp,
                    color = ChaProdeText
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Crea tu propia comunidad, suma puntos y demuestra quién sabe más de fútbol.",
                    fontSize = 11.5.sp,
                    color = ChaProdeMuted,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

/**
 * Botones de Acción Rápida (Figma Make: .league-actions).
 */
@Composable
fun LeagueActionsRow(
    onCreateClick: () -> Unit,
    onJoinClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Crear Nueva Liga (Primary CTA)
        Button(
            onClick = onCreateClick,
            modifier = Modifier
                .weight(1.15f)
                .height(46.dp),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ChaProdeCta)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Crear Nueva Liga",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1
                )
            }
        }

        // Unirse con Código (Secondary Surface)
        Button(
            onClick = onJoinClick,
            modifier = Modifier
                .weight(1f)
                .height(46.dp),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ChaProdeSurfaceRaised),
            border = androidx.compose.foundation.BorderStroke(1.dp, ChaProdeBorder)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Key,
                    contentDescription = null,
                    tint = ChaProdeGold,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Unirse con Código",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ChaProdeText,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Tarjeta individual de Liga Privada (Figma Make: .league-card).
 */
@Composable
fun LeagueCard(
    league: LeagueItem,
    onCopyCode: () -> Unit,
    onViewLeaderboard: () -> Unit
) {
    val currentUserId = SessionManager.getUser()?.id
    val isCreator = league.creadorId == currentUserId

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = androidx.compose.foundation.BorderStroke(1.dp, ChaProdeBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(ChaProdeSurface, ChaProdeSurfaceSoft)
                    )
                )
                .padding(16.dp)
        ) {
            // Fila Superior: Logo, Nombre, Miembros y Rol
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Logotipo de Trofeo de la Liga
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (isCreator) ChaProdeGold.copy(alpha = 0.12f) else ChaProdeSky.copy(alpha = 0.12f)
                        )
                        .border(
                            1.dp,
                            if (isCreator) ChaProdeGold.copy(alpha = 0.25f) else ChaProdeSky.copy(alpha = 0.25f),
                            RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = if (isCreator) ChaProdeGold else ChaProdeSky,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = league.nombre,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = ChaProdeText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Group,
                            contentDescription = null,
                            tint = ChaProdeMuted,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${league.totalMiembros} jugadores • ${league.torneoNombre}",
                            fontSize = 11.sp,
                            color = ChaProdeMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Badge distintivo de Rol (Administrador vs Miembro)
                if (isCreator) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ChaProdeGold.copy(alpha = 0.14f))
                            .border(1.dp, ChaProdeGold.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "👑 Admin",
                            color = ChaProdeGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ChaProdeSky.copy(alpha = 0.12f))
                            .border(1.dp, ChaProdeSky.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "⚽ Miembro",
                            color = ChaProdeSky,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // -----------------------------------------------------------------------------
            // Caja con Código de Invitación y Botón de Copiado Rápido
            // -----------------------------------------------------------------------------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ChaProdeBg)
                    .border(1.dp, ChaProdeBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "CÓDIGO DE INVITACIÓN",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp,
                            color = ChaProdeMuted
                        )
                        Text(
                            text = league.codigoAcceso,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            color = ChaProdeGold
                        )
                    }

                    IconButton(
                        onClick = onCopyCode,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(ChaProdeSurface)
                    ) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = "Copiar Código",
                            tint = ChaProdeSky,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botón para ver las posiciones de la comunidad
            Button(
                onClick = onViewLeaderboard,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ChaProdeSurfaceRaised),
                border = androidx.compose.foundation.BorderStroke(1.dp, ChaProdeBorder)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = ChaProdeGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Ver Posiciones de la Liga",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ChaProdeText
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = ChaProdeSky,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Diálogo Estilizado para Crear Liga (Figma Make: .modal).
 */
@Composable
fun CreateLeagueDialog(
    leagueName: String,
    onNameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
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
                        Icons.Default.Add,
                        contentDescription = null,
                        tint = ChaProdeSky,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Crear nueva liga",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ChaProdeText
                    )
                }
            }
        },
        text = {
            Column {
                Text(
                    text = "Ponle un nombre memorable a tu comunidad e invita a tus amigos a competir.",
                    color = ChaProdeMuted,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                ChaProdeTextField(
                    value = leagueName,
                    onValueChange = onNameChange,
                    label = "Nombre de la Liga (ej: Los Pibes FC)"
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = leagueName.trim().length >= 3,
                colors = ButtonDefaults.buttonColors(containerColor = ChaProdeCta),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Crear Liga", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = ChaProdeMuted, fontSize = 13.sp)
            }
        },
        containerColor = ChaProdeSurface,
        shape = RoundedCornerShape(22.dp)
    )
}

/**
 * Diálogo Estilizado para Unirse a Liga con Código (Figma Make: .modal).
 */
@Composable
fun JoinLeagueDialog(
    joinCode: String,
    onCodeChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ChaProdeGold.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Key,
                        contentDescription = null,
                        tint = ChaProdeGold,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Unirse con código",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ChaProdeText
                )
            }
        },
        text = {
            Column {
                Text(
                    text = "Ingresa el código de 6 caracteres que te compartió el administrador de la liga.",
                    color = ChaProdeMuted,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                ChaProdeTextField(
                    value = joinCode,
                    onValueChange = onCodeChange,
                    label = "Código de Invitación (ej: ARG26X)"
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = joinCode.trim().length >= 4,
                colors = ButtonDefaults.buttonColors(containerColor = ChaProdeCta),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Unirme", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = ChaProdeMuted, fontSize = 13.sp)
            }
        },
        containerColor = ChaProdeSurface,
        shape = RoundedCornerShape(22.dp)
    )
}

/**
 * Diálogo de Detalle y Clasificación de la Liga Privada.
 */
@Composable
fun LeagueDetailRankingDialog(
    detail: com.chaprode.mobile.model.LeagueDetailItem,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = detail.liga.nombre,
                    color = ChaProdeGold,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 19.sp
                )
                Text(
                    text = "Tabla de Posiciones de la Comunidad",
                    color = ChaProdeMuted,
                    fontSize = 12.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp)
            ) {
                if (detail.ranking.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Aún no hay pronósticos puntuados en esta liga.",
                            color = ChaProdeMuted,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(detail.ranking) { user ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (user.isCurrentUser) ChaProdeSurfaceRaised else ChaProdeBg)
                                    .border(
                                        width = 1.dp,
                                        color = if (user.isCurrentUser) ChaProdeSky else ChaProdeBorder,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "#${user.posicion}",
                                    color = if (user.posicion == 1) ChaProdeGold else ChaProdeMuted,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    modifier = Modifier.width(32.dp)
                                )
                                Text(
                                    text = user.username,
                                    color = ChaProdeText,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${user.puntosTotales} pts",
                                    color = if (user.posicion == 1) ChaProdeGold else ChaProdeSilver,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cerrar",
                    color = ChaProdeSky,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        },
        containerColor = ChaProdeSurface,
        shape = RoundedCornerShape(22.dp)
    )
}

@Preview(name = "Ligas Privadas Moderno", showBackground = true, backgroundColor = 0xFF0F172A)
@Composable
fun LeaguesScreenModernPreview() {
    val sampleLeagues = listOf(
        LeagueItem(
            id = "1",
            nombre = "Los Pibes de la Oficina",
            codigoAcceso = "ARG26X",
            creadorId = "1",
            creadorUsername = "seba",
            torneoId = "1",
            torneoNombre = "Champions League",
            totalMiembros = 12,
            createdAt = "2026-06-01"
        ),
        LeagueItem(
            id = "2",
            nombre = "Familia Futbolera",
            codigoAcceso = "FAM2026",
            creadorId = "2",
            creadorUsername = "gonza",
            torneoId = "1",
            torneoNombre = "Copa Mundial FIFA 2026",
            totalMiembros = 8,
            createdAt = "2026-06-05"
        )
    )

    LeaguesContent(
        state = LeagueUiState(leagues = sampleLeagues),
        onEvent = {},
        showBack = false,
        onNavigateBack = {}
    )
}
