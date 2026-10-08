package com.chaprode.mobile.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaguesScreen(
    viewModel: LeagueViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LeaguesContent(
        state = state,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaguesContent(
    state: LeagueUiState,
    onEvent: (LeagueUiEvent) -> Unit,
    onNavigateBack: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        onEvent(LeagueUiEvent.LoadLeagues)
    }

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
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "👥 Ligas Privadas",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFBBF24),
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onEvent(LeagueUiEvent.LoadLeagues) }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Actualizar",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F172A))
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFF0F172A)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Acciones Rápidas: Crear Liga y Unirse con Código
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { onEvent(LeagueUiEvent.OnOpenCreateDialog(true)) },
                    modifier = Modifier.weight(1f).height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Crear Liga", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Button(
                    onClick = { onEvent(LeagueUiEvent.OnOpenJoinDialog(true)) },
                    modifier = Modifier.weight(1f).height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155))
                ) {
                    Icon(Icons.Default.Key, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Unirme", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = if (state.leagues.isNotEmpty()) "Tus Ligas Participantes (${state.leagues.size})" else "Tus Ligas",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (state.isLoading && state.leagues.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF38BDF8))
                }
            } else if (state.leagues.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Group,
                            contentDescription = null,
                            tint = Color(0xFF475569),
                            modifier = Modifier.size(60.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Aún no perteneces a ninguna liga privada.\n¡Crea una con tus amigos o únete con un código!",
                            color = Color(0xFF94A3B8),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(state.leagues, key = { it.id }) { league ->
                        LeagueCard(
                            league = league,
                            onCopyCode = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Código de Liga", league.codigoAcceso)
                                clipboard.setPrimaryClip(clip)
                            },
                            onViewLeaderboard = {
                                onEvent(LeagueUiEvent.OnSelectLeague(league.id))
                            }
                        )
                    }
                }
            }
        }
    }

    // DIÁLOGO CREAR LIGA
    if (state.isCreateDialogOpen) {
        AlertDialog(
            onDismissRequest = { onEvent(LeagueUiEvent.OnOpenCreateDialog(false)) },
            title = { Text("🏆 Crear Nueva Liga", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Elige un nombre para tu liga privada e invita a tus amigos a competir.",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    ChaProdeTextField(
                        value = state.newLeagueName,
                        onValueChange = { onEvent(LeagueUiEvent.OnNewLeagueNameChanged(it)) },
                        label = "Nombre de la Liga (ej: Los Pibes FC)"
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { onEvent(LeagueUiEvent.OnCreateLeagueClicked) },
                    enabled = state.newLeagueName.trim().length >= 3,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                ) {
                    Text("Crear Liga", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(LeagueUiEvent.OnOpenCreateDialog(false)) }) {
                    Text("Cancelar", color = Color(0xFF94A3B8))
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }

    // DIÁLOGO UNIRSE A LIGA CON CÓDIGO
    if (state.isJoinDialogOpen) {
        AlertDialog(
            onDismissRequest = { onEvent(LeagueUiEvent.OnOpenJoinDialog(false)) },
            title = { Text("🔑 Unirse con Código", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Ingresa el código de 6 caracteres que te compartió el creador de la liga.",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    ChaProdeTextField(
                        value = state.joinCode,
                        onValueChange = { onEvent(LeagueUiEvent.OnJoinCodeChanged(it)) },
                        label = "Código de Invitación (ej: ARG26X)"
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { onEvent(LeagueUiEvent.OnJoinLeagueClicked) },
                    enabled = state.joinCode.trim().length >= 4,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                ) {
                    Text("Unirme", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(LeagueUiEvent.OnOpenJoinDialog(false)) }) {
                    Text("Cancelar", color = Color(0xFF94A3B8))
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }

    // MODAL DETALLE DE LIGA CON RANKING
    state.selectedLeagueDetail?.let { detail ->
        AlertDialog(
            onDismissRequest = { onEvent(LeagueUiEvent.OnCloseDetail) },
            title = {
                Column {
                    Text(detail.liga.nombre, color = Color(0xFFFBBF24), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text("Posiciones de la Liga", color = Color(0xFF94A3B8), fontSize = 12.sp)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                    if (detail.ranking.isEmpty()) {
                        Text("No hay pronósticos puntuados en esta liga.", color = Color(0xFF94A3B8), fontSize = 13.sp)
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(detail.ranking) { user ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF0F172A), RoundedCornerShape(10.dp))
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("#${user.posicion}", color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold, modifier = Modifier.width(32.dp))
                                    Text(user.username, color = Color.White, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                                    Text("${user.puntosTotales} pts", color = Color(0xFFFBBF24), fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { onEvent(LeagueUiEvent.OnCloseDetail) }) {
                    Text("Cerrar", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }
}

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
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = league.nombre,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF0284C7).copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Group, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${league.totalMiembros}", color = Color(0xFF38BDF8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Badge de rol (Creador vs Participante)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isCreator) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF59E0B).copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "👑 Administrador",
                            color = Color(0xFFFBBF24),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF38BDF8).copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = "⚽ Participante",
                            color = Color(0xFF38BDF8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = if (isCreator) "Creada por ti" else "Creador: @${league.creadorUsername}",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Torneo: ${league.torneoNombre}",
                color = Color(0xFF64748B),
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Código de Acceso
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF0F172A)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("CÓDIGO DE INVITACIÓN", fontSize = 10.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.SemiBold)
                        Text(league.codigoAcceso, fontSize = 16.sp, color = Color(0xFFFBBF24), fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
                    }

                    IconButton(onClick = onCopyCode) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copiar Código", tint = Color(0xFF38BDF8), modifier = Modifier.size(20.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onViewLeaderboard,
                modifier = Modifier.fillMaxWidth().height(42.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155))
            ) {
                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Ver Tabla de Posiciones", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Preview(name = "Ligas Privadas", showBackground = true, backgroundColor = 0xFF0F172A)
@Composable
fun LeaguesScreenPreview() {
    val sampleLeagues = listOf(
        LeagueItem(
            id = "1",
            nombre = "Los Pibes del Fútbol",
            codigoAcceso = "ARG26X",
            creadorId = "1",
            creadorUsername = "seba",
            torneoId = "1",
            torneoNombre = "Copa Mundial FIFA 2026",
            totalMiembros = 6,
            createdAt = "2026-06-01"
        ),
        LeagueItem(
            id = "2",
            nombre = "Amigos de la Oficina",
            codigoAcceso = "PRODE8",
            creadorId = "2",
            creadorUsername = "gonza",
            torneoId = "1",
            torneoNombre = "Copa Mundial FIFA 2026",
            totalMiembros = 12,
            createdAt = "2026-06-05"
        )
    )

    LeaguesContent(
        state = LeagueUiState(leagues = sampleLeagues),
        onEvent = {},
        onNavigateBack = {}
    )
}
