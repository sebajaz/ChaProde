package com.chaprode.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Star
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
import com.chaprode.mobile.model.MatchWithPredictionItem
import com.chaprode.mobile.ui.prediction.PredictionUiEvent
import com.chaprode.mobile.ui.prediction.PredictionUiState
import com.chaprode.mobile.ui.prediction.PredictionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FixtureScreen(
    viewModel: PredictionViewModel,
    torneoId: String = "todos",
    torneoNombre: String = "Partidos Mundiales",
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(torneoId) {
        viewModel.onEvent(PredictionUiEvent.LoadMatches(torneoId))
    }

    FixtureContent(
        torneoNombre = torneoNombre,
        state = state,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FixtureContent(
    torneoNombre: String,
    state: PredictionUiState,
    onEvent: (PredictionUiEvent) -> Unit,
    onNavigateBack: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTab by remember { mutableStateOf(0) } // 0 = Cartelera / Pronosticar, 1 = Mi Historial
    var selectedLeagueFilter by remember { mutableStateOf("todos") }
    var selectedStatusFilter by remember { mutableStateOf("TODOS") } // TODOS, PENDIENTE, FINALIZADO

    val tournamentsInList = remember(state.matches) {
        state.matches.mapNotNull { it.torneoNombre }.distinct()
    }

    // Filtrar partidos de cartelera
    val displayedMatches = remember(state.matches, selectedLeagueFilter, selectedStatusFilter) {
        state.matches.filter { match ->
            val leagueMatches = if (selectedLeagueFilter == "todos") true else match.torneoNombre == selectedLeagueFilter
            val statusMatches = when (selectedStatusFilter) {
                "PENDIENTE" -> match.estado == "PENDIENTE"
                "FINALIZADO" -> match.estado == "FINALIZADO"
                "EN_JUEGO" -> match.estado == "EN_JUEGO"
                else -> true
            }
            leagueMatches && statusMatches
        }
    }

    // Filtrar historial del usuario (partidos que tienen pronóstico guardado)
    val myHistoryMatches = remember(state.matches, selectedLeagueFilter) {
        state.matches.filter { match ->
            val hasPrediction = match.miPronosticoLocal != null && match.miPronosticoVisitante != null
            val leagueMatches = if (selectedLeagueFilter == "todos") true else match.torneoNombre == selectedLeagueFilter
            hasPrediction && leagueMatches
        }
    }

    val totalPoints = remember(myHistoryMatches) { myHistoryMatches.sumOf { it.puntosGanados } }
    val plenosCount = remember(myHistoryMatches) { myHistoryMatches.count { it.puntosGanados == 3 } }
    val tendenciasCount = remember(myHistoryMatches) { myHistoryMatches.count { it.puntosGanados == 1 } }

    LaunchedEffect(state.successMessage, state.errorMessage) {
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            onEvent(PredictionUiEvent.OnDismissMessage)
        }
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            onEvent(PredictionUiEvent.OnDismissMessage)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "🏆 $torneoNombre",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFBBF24),
                            fontSize = 18.sp
                        )
                        Text(
                            text = "${state.matches.size} partidos disponibles en el mundo",
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
                actions = {
                    IconButton(onClick = { onEvent(PredictionUiEvent.LoadMatches(state.selectedTorneoId)) }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Actualizar",
                            tint = Color(0xFF38BDF8)
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
        ) {
            // PESTAÑAS SUPERIORES: CARTELERA VS HISTORIAL
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF1E293B),
                contentColor = Color(0xFF38BDF8)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SportsSoccer, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Cartelera (${state.matches.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Mi Historial (${myHistoryMatches.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                )
            }

            // BARRA DE FILTROS POR LIGA MUNDIAL (Compartida para ambas pestañas)
            if (tournamentsInList.size > 1) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedLeagueFilter == "todos",
                            onClick = { selectedLeagueFilter = "todos" },
                            label = { Text("🌍 Todas") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF0284C7),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                    items(tournamentsInList) { name ->
                        val count = if (selectedTab == 0) {
                            state.matches.count { it.torneoNombre == name }
                        } else {
                            myHistoryMatches.count { it.torneoNombre == name }
                        }
                        FilterChip(
                            selected = selectedLeagueFilter == name,
                            onClick = { selectedLeagueFilter = name },
                            label = { Text("$name ($count)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF0284C7),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // CONTENIDO SEGÚN LA PESTAÑA SELECCIONADA
            if (state.isLoading && state.matches.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF38BDF8))
                }
            } else if (selectedTab == 0) {
                // ==========================================
                // PESTAÑA 0: CARTELERA DE PARTIDOS Y PRONÓSTICOS
                // ==========================================
                Column(modifier = Modifier.fillMaxSize()) {
                    // Filtros secundarios por estado del partido
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedStatusFilter == "TODOS",
                            onClick = { selectedStatusFilter = "TODOS" },
                            label = { Text("Todos (${state.matches.size})", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF334155),
                                selectedLabelColor = Color.White
                            )
                        )
                        FilterChip(
                            selected = selectedStatusFilter == "PENDIENTE",
                            onClick = { selectedStatusFilter = "PENDIENTE" },
                            label = { Text("⏳ Por Jugar", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF0369A1),
                                selectedLabelColor = Color.White
                            )
                        )
                        FilterChip(
                            selected = selectedStatusFilter == "FINALIZADO",
                            onClick = { selectedStatusFilter = "FINALIZADO" },
                            label = { Text("🏁 Finalizados", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF065F46),
                                selectedLabelColor = Color.White
                            )
                        )
                    }

                    if (displayedMatches.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.SportsSoccer, contentDescription = null, tint = Color(0xFF475569), modifier = Modifier.size(50.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No hay partidos en esta categoría o filtro.",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(displayedMatches, key = { it.id }) { match ->
                                InteractiveMatchCard(
                                    match = match,
                                    isSaving = state.savingMatchId == match.id,
                                    onLocalGoalsChange = { newGoals ->
                                        onEvent(PredictionUiEvent.OnLocalGoalsChanged(match.id, newGoals))
                                    },
                                    onVisitorGoalsChange = { newGoals ->
                                        onEvent(PredictionUiEvent.OnVisitorGoalsChanged(match.id, newGoals))
                                    },
                                    onSaveClick = {
                                        onEvent(PredictionUiEvent.OnSavePrediction(match.id))
                                    }
                                )
                            }
                        }
                    }
                }
            } else {
                // ==========================================
                // PESTAÑA 1: MI HISTORIAL DE PRONÓSTICOS
                // ==========================================
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header Card con Resumen de Puntos y Plenos
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("PUNTOS", fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                                Text("$totalPoints pts", fontSize = 20.sp, color = Color(0xFFFBBF24), fontWeight = FontWeight.Black)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("PLENOS (3p)", fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                                Text("$plenosCount", fontSize = 20.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Black)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("ACIERTOS (1p)", fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                                Text("$tendenciasCount", fontSize = 20.sp, color = Color(0xFF38BDF8), fontWeight = FontWeight.Black)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("JUGADOS", fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                                Text("${myHistoryMatches.size}", fontSize = 20.sp, color = Color.White, fontWeight = FontWeight.Black)
                            }
                        }
                    }

                    if (myHistoryMatches.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.History, contentDescription = null, tint = Color(0xFF475569), modifier = Modifier.size(56.dp))
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "Aún no tienes pronósticos registrados en este torneo o liga.",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Ve a la pestaña 'Cartelera' para ingresar tus pronósticos antes de que comiencen los partidos.",
                                    color = Color(0xFF64748B),
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(myHistoryMatches, key = { it.id }) { match ->
                                PredictionHistoryCard(match = match)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PredictionHistoryCard(match: MatchWithPredictionItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Competición y Fecha
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF0F172A),
                    border = BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Text(
                        text = "🏆 ${match.torneoNombre ?: "Competición"}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8)
                    )
                }
                Text(
                    text = match.fechaHora.replace("T", " ").replace("Z", ""),
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Equipos y Comparativa Pronóstico vs Resultado Real
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Local
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = match.localNombre,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }

                // Centro: Pronóstico vs Real
                Column(
                    modifier = Modifier.weight(1.3f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0F172A),
                        border = BorderStroke(1.dp, Color(0xFF475569))
                    ) {
                        Text(
                            text = "Mi Pronóstico: ${match.miPronosticoLocal} - ${match.miPronosticoVisitante}",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFBBF24)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    if (match.estado == "FINALIZADO" && match.golesLocalReal != null && match.golesVisitanteReal != null) {
                        Text(
                            text = "Resultado Final: ${match.golesLocalReal} - ${match.golesVisitanteReal}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF94A3B8)
                        )
                    } else if (match.estado == "EN_JUEGO") {
                        Text(
                            text = "En Juego: ${match.golesLocalReal ?: 0} - ${match.golesVisitanteReal ?: 0}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444)
                        )
                    } else {
                        Text(
                            text = "En espera de inicio",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                // Visitante
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = match.visitanteNombre,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Footer: Insignia de Resultado & Puntos Obtenidos
            if (match.estado == "FINALIZADO") {
                val (badgeColor, badgeText, badgeBorder) = when (match.puntosGanados) {
                    3 -> Triple(Color(0xFF065F46), "⭐ ¡PLENO EXACTO! (+3 PUNTOS)", Color(0xFF10B981))
                    1 -> Triple(Color(0xFF0369A1), "✓ TENDENCIA ACERTADA (+1 PUNTO)", Color(0xFF38BDF8))
                    else -> Triple(Color(0xFF334155), "✗ NO ACERTADO (0 PUNTOS)", Color(0xFF475569))
                }
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = badgeColor.copy(alpha = 0.4f),
                    border = BorderStroke(1.dp, badgeBorder.copy(alpha = 0.6f))
                ) {
                    Text(
                        text = badgeText,
                        modifier = Modifier.padding(vertical = 8.dp),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }
            } else if (match.estado == "EN_JUEGO") {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF7F1D1D).copy(alpha = 0.4f),
                    border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.6f))
                ) {
                    Text(
                        text = "⚡ PARTIDO EN VIVO - Marcador parcial",
                        modifier = Modifier.padding(vertical = 8.dp),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color(0xFFFCA5A5)
                    )
                }
            } else {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF1E293B),
                    border = BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Text(
                        text = "⏳ PRONÓSTICO REGISTRADO (Cierre: 5 min antes del inicio)",
                        modifier = Modifier.padding(vertical = 8.dp),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }
    }
}

@Composable
fun InteractiveMatchCard(
    match: MatchWithPredictionItem,
    isSaving: Boolean,
    onLocalGoalsChange: (Int) -> Unit,
    onVisitorGoalsChange: (Int) -> Unit,
    onSaveClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Badge de Torneo / Competición
            if (!match.torneoNombre.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF0F172A).copy(alpha = 0.8f),
                    modifier = Modifier.padding(bottom = 10.dp)
                ) {
                    Text(
                        text = "🏆 ${match.torneoNombre}",
                        color = Color(0xFF38BDF8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Header: Fecha y Estado de Corte de 5 Minutos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = match.fechaHora.replace("T", " ").replace("Z", " UTC"),
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )

                // Badge de Estado y Puntos
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (match.estado == "FINALIZADO" && match.puntosGanados > 0) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFBBF24).copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "+${match.puntosGanados} Pts",
                                color = Color(0xFFFBBF24),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = when {
                            match.estado == "FINALIZADO" -> Color(0xFF10B981).copy(alpha = 0.2f)
                            match.estado == "EN_JUEGO" -> Color(0xFFEF4444).copy(alpha = 0.2f)
                            match.cerrado -> Color(0xFFF97316).copy(alpha = 0.2f)
                            else -> Color(0xFF38BDF8).copy(alpha = 0.2f)
                        }
                    ) {
                        Text(
                            text = when {
                                match.estado == "FINALIZADO" -> "FINALIZADO"
                                match.estado == "EN_JUEGO" -> "EN JUEGO"
                                match.cerrado -> "🔒 Cerrado (-5 min)"
                                else -> "⏱️ Cierra en ${match.minutosRestantesParaCierre} min"
                            },
                            color = when {
                                match.estado == "FINALIZADO" -> Color(0xFF34D399)
                                match.estado == "EN_JUEGO" -> Color(0xFFF87171)
                                match.cerrado -> Color(0xFFFB923C)
                                else -> Color(0xFF38BDF8)
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
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
                        contentDescription = match.localNombre,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(38.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = match.localNombre,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center
                    )
                }

                // Marcador Central Real (si ya terminó o está en juego)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                    if (match.golesLocalReal != null && match.golesVisitanteReal != null) {
                        Text(
                            text = "${match.golesLocalReal} - ${match.golesVisitanteReal}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFBBF24)
                        )
                        Text(
                            text = "Resultado Real",
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8)
                        )
                    } else {
                        Text(
                            text = "VS",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF475569)
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
                        contentDescription = match.visitanteNombre,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(38.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = match.visitanteNombre,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFF334155), thickness = 1.dp)
            Spacer(modifier = Modifier.height(14.dp))

            // SECCIÓN DE PRONÓSTICO
            if (match.cerrado) {
                // Modo Bloqueado (menos de 15 minutos o en juego)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF0F172A).copy(alpha = 0.6f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        if (match.tienePronosticoGuardado) {
                            Text(
                                text = "Tu pronóstico: ${match.miPronosticoLocal} - ${match.miPronosticoVisitante}",
                                color = Color(0xFFFBBF24),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        } else {
                            Text(
                                text = "Pronósticos cerrados (no ingresaste resultado)",
                                color = Color(0xFF94A3B8),
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            } else {
                // Modo Abierto: Controles interactivos para ingresar goles predichos
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tu Pronóstico:",
                            color = Color(0xFF38BDF8),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )

                        if (match.tienePronosticoGuardado && !match.isEdited) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF34D399),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Guardado (${match.miPronosticoLocal} - ${match.miPronosticoVisitante})",
                                    color = Color(0xFF34D399),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Contador Local
                        GoalStepper(
                            value = match.editedGolesLocal,
                            onValueChange = onLocalGoalsChange
                        )

                        Text(
                            text = "-",
                            color = Color(0xFF94A3B8),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // Contador Visitante
                        GoalStepper(
                            value = match.editedGolesVisitante,
                            onValueChange = onVisitorGoalsChange
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Botón Guardar Pronóstico
                    Button(
                        onClick = onSaveClick,
                        enabled = !isSaving && (match.isEdited || !match.tienePronosticoGuardado),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0284C7),
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFF334155),
                            disabledContentColor = Color(0xFF64748B)
                        )
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text(
                                text = if (match.tienePronosticoGuardado) "Actualizar Pronóstico" else "Guardar Pronóstico",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GoalStepper(
    value: Int,
    onValueChange: (Int) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        IconButton(
            onClick = { if (value > 0) onValueChange(value - 1) },
            enabled = value > 0,
            modifier = Modifier
                .size(36.dp)
                .background(Color(0xFF334155), shape = CircleShape)
        ) {
            Icon(
                Icons.Default.Remove,
                contentDescription = "Restar",
                tint = if (value > 0) Color.White else Color(0xFF64748B),
                modifier = Modifier.size(18.dp)
            )
        }

        Surface(
            modifier = Modifier
                .width(44.dp)
                .height(40.dp)
                .border(1.dp, Color(0xFF475569), RoundedCornerShape(10.dp)),
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFF0F172A)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = value.toString(),
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
            }
        }

        IconButton(
            onClick = { if (value < 99) onValueChange(value + 1) },
            enabled = value < 99,
            modifier = Modifier
                .size(36.dp)
                .background(Color(0xFF334155), shape = CircleShape)
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Sumar",
                tint = if (value < 99) Color.White else Color(0xFF64748B),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Preview(name = "Partido Abierto para Pronosticar", showBackground = true, backgroundColor = 0xFF0F172A)
@Composable
fun MatchCardOpenPreview() {
    InteractiveMatchCard(
        match = MatchWithPredictionItem(
            id = "1",
            localNombre = "Argentina",
            localBandera = "",
            visitanteNombre = "Brasil",
            visitanteBandera = "",
            fechaHora = "2026-06-18 20:00 UTC",
            estado = "PENDIENTE",
            miPronosticoLocal = null,
            miPronosticoVisitante = null,
            editedGolesLocal = 2,
            editedGolesVisitante = 1,
            isEdited = true,
            cerrado = false,
            minutosRestantesParaCierre = 120
        ),
        isSaving = false,
        onLocalGoalsChange = {},
        onVisitorGoalsChange = {},
        onSaveClick = {}
    )
}

@Preview(name = "Partido Cerrado (<5 min)", showBackground = true, backgroundColor = 0xFF0F172A)
@Composable
fun MatchCardClosedPreview() {
    InteractiveMatchCard(
        match = MatchWithPredictionItem(
            id = "2",
            localNombre = "España",
            localBandera = "",
            visitanteNombre = "Alemania",
            visitanteBandera = "",
            fechaHora = "2026-06-12 21:00 UTC",
            estado = "PENDIENTE",
            miPronosticoLocal = 1,
            miPronosticoVisitante = 1,
            cerrado = true,
            minutosRestantesParaCierre = 0
        ),
        isSaving = false,
        onLocalGoalsChange = {},
        onVisitorGoalsChange = {},
        onSaveClick = {}
    )
}

@Preview(name = "Partido Finalizado con 3 Puntos", showBackground = true, backgroundColor = 0xFF0F172A)
@Composable
fun MatchCardFinishedPreview() {
    InteractiveMatchCard(
        match = MatchWithPredictionItem(
            id = "3",
            localNombre = "Francia",
            localBandera = "",
            visitanteNombre = "Estados Unidos",
            visitanteBandera = "",
            fechaHora = "2026-06-13 18:00 UTC",
            estado = "FINALIZADO",
            golesLocalReal = 2,
            golesVisitanteReal = 0,
            miPronosticoLocal = 2,
            miPronosticoVisitante = 0,
            puntosGanados = 3,
            cerrado = true,
            minutosRestantesParaCierre = 0
        ),
        isSaving = false,
        onLocalGoalsChange = {},
        onVisitorGoalsChange = {},
        onSaveClick = {}
    )
}
