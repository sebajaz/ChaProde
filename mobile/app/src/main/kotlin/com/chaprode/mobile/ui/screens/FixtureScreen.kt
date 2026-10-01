package com.chaprode.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.SportsSoccer
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
import com.chaprode.mobile.model.PartidoItem
import com.chaprode.mobile.ui.FixtureUiState
import com.chaprode.mobile.ui.FixtureViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FixtureScreen(
    viewModel: FixtureViewModel,
    torneoId: String,
    torneoNombre: String = "Copa Mundial FIFA 2026",
    onNavigateBack: () -> Unit,
    onMatchClick: (PartidoItem) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(torneoId) {
        viewModel.loadMatches(torneoId)
    }

    FixtureContent(
        torneoNombre = torneoNombre,
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onMatchClick = onMatchClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FixtureContent(
    torneoNombre: String,
    uiState: FixtureUiState,
    onNavigateBack: () -> Unit,
    onMatchClick: (PartidoItem) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        torneoNombre,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFBBF24),
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
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
            when (uiState) {
                is FixtureUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF38BDF8))
                    }
                }
                is FixtureUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = uiState.message, color = Color(0xFFEF4444), fontSize = 16.sp)
                    }
                }
                is FixtureUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.matches) { partido ->
                            MatchCard(partido = partido, onClick = { onMatchClick(partido) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MatchCard(
    partido: PartidoItem,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Fecha y Estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = partido.fechaHora.replace("T", " ").replace("Z", " UTC"),
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = when (partido.estado) {
                        "FINALIZADO" -> Color(0xFF10B981).copy(alpha = 0.2f)
                        "EN_JUEGO" -> Color(0xFFEF4444).copy(alpha = 0.2f)
                        else -> Color(0xFF38BDF8).copy(alpha = 0.2f)
                    }
                ) {
                    Text(
                        text = partido.estado,
                        color = when (partido.estado) {
                            "FINALIZADO" -> Color(0xFF34D399)
                            "EN_JUEGO" -> Color(0xFFF87171)
                            else -> Color(0xFF38BDF8)
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Body: Equipo Local vs Visitante
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Local
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.SportsSoccer,
                        contentDescription = partido.localNombre,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = partido.localNombre,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center
                    )
                }

                // Marcador central
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                    if (partido.golesLocal != null && partido.golesVisitante != null) {
                        Text(
                            text = "${partido.golesLocal} - ${partido.golesVisitante}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFBBF24)
                        )
                    } else {
                        Text(
                            text = "VS",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                // Visitante
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.SportsSoccer,
                        contentDescription = partido.visitanteNombre,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = partido.visitanteNombre,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F172A)
@Composable
fun MatchCardPreview() {
    MatchCard(
        partido = PartidoItem(
            id = "1",
            localNombre = "Argentina",
            localBandera = "",
            visitanteNombre = "Brasil",
            visitanteBandera = "",
            fechaHora = "2026-06-18 20:00 UTC",
            estado = "PENDIENTE"
        ),
        onClick = {}
    )
}
