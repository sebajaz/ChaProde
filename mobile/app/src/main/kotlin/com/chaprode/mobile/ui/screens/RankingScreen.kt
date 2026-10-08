package com.chaprode.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chaprode.mobile.model.RankingUserItem
import com.chaprode.mobile.ui.ranking.RankingUiEvent
import com.chaprode.mobile.ui.ranking.RankingUiState
import com.chaprode.mobile.ui.ranking.RankingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RankingScreen(
    viewModel: RankingViewModel,
    torneoId: String = "activo",
    torneoNombre: String = "Copa Mundial FIFA 2026",
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(torneoId) {
        viewModel.onEvent(RankingUiEvent.LoadRanking(torneoId))
    }

    RankingContent(
        torneoNombre = torneoNombre,
        state = state,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RankingContent(
    torneoNombre: String,
    state: RankingUiState,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "🏆 Tabla de Posiciones",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFBBF24),
                            fontSize = 18.sp
                        )
                        Text(
                            text = torneoNombre,
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    }
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F172A))
            )
        },
        containerColor = Color(0xFF0F172A)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (state.isLoading && state.ranking.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF38BDF8))
                }
            } else if (state.ranking.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Aún no hay puntos registrados en este torneo.",
                            color = Color(0xFF94A3B8),
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // SECCIÓN DE PODIO (Top 3)
                    item {
                        PodiumSection(
                            top1 = state.top1,
                            top2 = state.top2,
                            top3 = state.top3
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Clasificación General",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    // RESTO DE PARTICIPANTES (Puestos 4 en adelante)
                    items(state.restOfRanking, key = { it.usuarioId }) { item ->
                        LeaderboardRow(item = item)
                    }
                }
            }
        }
    }
}

@Composable
fun PodiumSection(
    top1: RankingUserItem?,
    top2: RankingUserItem?,
    top3: RankingUserItem?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Podio de Campeones",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF38BDF8),
                modifier = Modifier.padding(bottom = 20.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                // 2do Puesto (Plata - Izquierda)
                PodiumColumn(
                    item = top2,
                    placeNumber = 2,
                    medal = "🥈",
                    color = Color(0xFFCBD5E1),
                    podiumHeight = 90.dp
                )

                // 1er Puesto (Oro - Centro / Más alto)
                PodiumColumn(
                    item = top1,
                    placeNumber = 1,
                    medal = "🥇",
                    color = Color(0xFFFBBF24),
                    podiumHeight = 120.dp
                )

                // 3er Puesto (Bronce - Derecha)
                PodiumColumn(
                    item = top3,
                    placeNumber = 3,
                    medal = "🥉",
                    color = Color(0xFFF97316),
                    podiumHeight = 70.dp
                )
            }
        }
    }
}

@Composable
fun PodiumColumn(
    item: RankingUserItem?,
    placeNumber: Int,
    medal: String,
    color: Color,
    podiumHeight: androidx.compose.ui.unit.Dp
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(90.dp)
    ) {
        // Avatar y Medalla
        Box(contentAlignment = Alignment.TopEnd) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(color.copy(alpha = 0.2f), shape = CircleShape)
                    .border(2.dp, color, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item?.username?.firstOrNull()?.uppercase() ?: "-",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Text(text = medal, fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Nombre de Usuario
        Text(
            text = item?.username ?: "Sin definir",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1,
            textAlign = TextAlign.Center
        )

        // Puntos
        Text(
            text = "${item?.puntosTotales ?: 0} pts",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = color
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Bloque del Podio
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(podiumHeight),
            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
            color = color.copy(alpha = 0.15f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "#$placeNumber",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = color
                )
            }
        }
    }
}

@Composable
fun LeaderboardRow(item: RankingUserItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (item.isCurrentUser) {
                    Modifier.border(1.5.dp, Color(0xFF38BDF8), RoundedCornerShape(16.dp))
                } else Modifier
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isCurrentUser) Color(0xFF1E293B) else Color(0xFF1E293B).copy(alpha = 0.7f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Posición
            Text(
                text = "#${item.posicion}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF94A3B8),
                modifier = Modifier.width(36.dp)
            )

            // Avatar pequeño
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(Color(0xFF334155), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.username.firstOrNull()?.uppercase() ?: "U",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Nombre y Plenos
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.username,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (item.isCurrentUser) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF38BDF8).copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "Tú",
                                color = Color(0xFF38BDF8),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Text(
                    text = "🎯 ${item.plenosExactos} plenos  •  ⭐ ${item.aciertosTendencia} aciertos",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
            }

            // Puntos Totales
            Text(
                text = "${item.puntosTotales} pts",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFFBBF24)
            )
        }
    }
}

@Preview(name = "Ranking con Podio", showBackground = true, backgroundColor = 0xFF0F172A)
@Composable
fun RankingScreenPreview() {
    val sampleRanking = listOf(
        RankingUserItem(1, "1", "seba", 9, 3, 0, 3, isCurrentUser = true),
        RankingUserItem(2, "2", "gonza", 7, 2, 1, 3),
        RankingUserItem(3, "3", "martin", 5, 1, 2, 3),
        RankingUserItem(4, "4", "lucas", 4, 1, 1, 3),
        RankingUserItem(5, "5", "mateo", 2, 0, 2, 3)
    )

    RankingContent(
        torneoNombre = "Copa Mundial FIFA 2026",
        state = RankingUiState(
            isLoading = false,
            ranking = sampleRanking
        ),
        onNavigateBack = {}
    )
}
