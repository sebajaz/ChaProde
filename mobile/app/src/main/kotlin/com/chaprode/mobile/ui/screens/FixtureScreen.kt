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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Remove
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
import com.chaprode.mobile.model.MatchWithPredictionItem
import com.chaprode.mobile.ui.prediction.PredictionUiEvent
import com.chaprode.mobile.ui.prediction.PredictionUiState
import com.chaprode.mobile.ui.prediction.PredictionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FixtureScreen(
    viewModel: PredictionViewModel,
    torneoId: String = "d96b16df-448a-43ca-9fa7-140af2e63e14",
    torneoNombre: String = "Copa Mundial FIFA 2026",
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
                            text = "Fixture y Pronósticos",
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFF0F172A)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (state.isLoading && state.matches.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF38BDF8))
                }
            } else if (state.matches.isEmpty() && state.errorMessage != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = state.errorMessage, color = Color(0xFFEF4444), fontSize = 16.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.matches, key = { it.id }) { match ->
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
            // Header: Fecha y Estado de Corte de 15 Minutos
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
