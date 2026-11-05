package com.chaprode.mobile.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chaprode.mobile.model.MatchWithPredictionItem
import com.chaprode.mobile.ui.components.ButtonVariant
import com.chaprode.mobile.ui.components.ChaProdeButton
import com.chaprode.mobile.ui.components.MatchStatusBadge
import com.chaprode.mobile.ui.components.MatchStatusType
import com.chaprode.mobile.ui.components.ScoreStepper
import com.chaprode.mobile.ui.components.TeamBadge
import com.chaprode.mobile.ui.components.TeamBadgeColor
import com.chaprode.mobile.ui.prediction.PredictionUiEvent
import com.chaprode.mobile.ui.prediction.PredictionUiState
import com.chaprode.mobile.ui.prediction.PredictionViewModel
import com.chaprode.mobile.ui.theme.*

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

    var selectedTournament by remember { mutableStateOf("Todos") }
    var selectedFilter by remember { mutableStateOf("Todos") } // Todos, Pendientes, En Juego, Finalizados, Mis Pronósticos

    // Lista de torneos presentes en el fixture
    val tournamentList = remember(state.matches) {
        val unique = state.matches.mapNotNull { it.torneoNombre }.distinct()
        listOf("Todos") + unique
    }

    // Filtros disponibles
    val filterOptions = listOf("Todos", "Pendientes", "En Juego", "Finalizados", "Mis Pronósticos")

    // Partidos filtrados
    val filteredMatches = remember(state.matches, selectedTournament, selectedFilter) {
        state.matches.filter { match ->
            val matchesTournament = if (selectedTournament == "Todos") true else match.torneoNombre == selectedTournament
            val matchesFilter = when (selectedFilter) {
                "Pendientes" -> match.estado == "PENDIENTE"
                "En Juego" -> match.estado == "EN_JUEGO"
                "Finalizados" -> match.estado == "FINALIZADO"
                "Mis Pronósticos" -> match.tienePronosticoGuardado
                else -> true
            }
            matchesTournament && matchesFilter
        }
    }

    // Métricas del usuario
    val myPredictedMatches = remember(state.matches) { state.matches.filter { it.tienePronosticoGuardado } }
    val totalPoints = remember(myPredictedMatches) { myPredictedMatches.sumOf { it.puntosGanados } }
    val plenosCount = remember(myPredictedMatches) { myPredictedMatches.count { it.puntosGanados == 3 } }
    val tendenciasCount = remember(myPredictedMatches) { myPredictedMatches.count { it.puntosGanados == 1 } }

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
                            text = "PRONOSTICA Y SUMA",
                            color = ChaProdeSky,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = if (torneoNombre.isNotBlank() && torneoNombre != "Partidos Mundiales") "Fixture · $torneoNombre" else "Fixture",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onEvent(PredictionUiEvent.LoadMatches("todos")) }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Actualizar",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ChaProdeBg)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = ChaProdeBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 1. Carrusel Horizontal de Torneos (Figma Make reference: .chip-scroller)
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tournamentList) { tournament ->
                    val isSelected = selectedTournament == tournament
                    Surface(
                        onClick = { selectedTournament = tournament },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) ChaProdeSky else ChaProdeSurface,
                        border = if (isSelected) null else BorderStroke(1.dp, ChaProdeBorder),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(
                            text = tournament,
                            color = if (isSelected) ChaProdeBg else ChaProdeMuted,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            // 2. Herramientas de Fixture y Filtro de Estado (Figma Make reference: .fixture-tools)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Selector deslizable o pestañas de filtro de estado
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filterOptions) { filter ->
                        val isFilterSelected = selectedFilter == filter
                        Surface(
                            onClick = { selectedFilter = filter },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isFilterSelected) ChaProdeBorderLight else ChaProdeSurface.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, if (isFilterSelected) ChaProdeSky else ChaProdeBorder),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(
                                text = filter,
                                color = if (isFilterSelected) ChaProdeSky else ChaProdeMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Contador de partidos encontrados
                Text(
                    text = "${filteredMatches.size} partidos",
                    color = ChaProdeMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // 3. Resumen Rápido de Puntos cuando se filtra "Mis Pronósticos" o "Finalizados"
            if (selectedFilter == "Mis Pronósticos" || selectedFilter == "Finalizados") {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = ChaProdeSurface,
                    border = BorderStroke(1.dp, ChaProdeBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("PUNTOS", fontSize = 10.sp, color = ChaProdeMuted, fontWeight = FontWeight.Bold)
                            Text("$totalPoints pts", fontSize = 16.sp, color = ChaProdeGold, fontWeight = FontWeight.Black)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("PLENOS (3p)", fontSize = 10.sp, color = ChaProdeMuted, fontWeight = FontWeight.Bold)
                            Text("$plenosCount", fontSize = 16.sp, color = ChaProdeSuccess, fontWeight = FontWeight.Black)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("TENDENCIA (1p)", fontSize = 10.sp, color = ChaProdeMuted, fontWeight = FontWeight.Bold)
                            Text("$tendenciasCount", fontSize = 16.sp, color = ChaProdeSky, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }

            // 4. Lista de Partidos (Figma Make reference: .match-list)
            if (state.isLoading && state.matches.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = ChaProdeSky)
                }
            } else if (filteredMatches.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = ChaProdeBorderLight,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "No hay partidos disponibles",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Prueba seleccionando otro torneo o filtro de estado.",
                            color = ChaProdeMuted,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredMatches, key = { it.id }) { match ->
                        MatchCardItem(
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

/**
 * Tarjeta de Partido estilizada según el prototipo de Figma Make (.match-card).
 * Incluye cabecera con badge animado, 3 columnas con ScoreStepper y pie de resultado o guardado.
 */
@Composable
fun MatchCardItem(
    match: MatchWithPredictionItem,
    isSaving: Boolean,
    onLocalGoalsChange: (Int) -> Unit,
    onVisitorGoalsChange: (Int) -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isFinished = match.estado == "FINALIZADO"
    val isLive = match.estado == "EN_JUEGO"
    val isLocked = match.cerrado || isFinished || isLive
    val canPredict = !match.cerrado && match.estado == "PENDIENTE"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (isLive) ChaProdeSuccess.copy(alpha = 0.45f) else ChaProdeBorder,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ChaProdeSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Cabecera de la Tarjeta: Torneo, Fecha y Badge de Estado (Figma Make: .match-card__top)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ChaProdeSurfaceSoft)
                    .padding(horizontal = 14.dp, vertical = 9.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Fecha o Torneo
                Text(
                    text = "${match.torneoNombre ?: "Partido"} · ${match.fechaHora.take(16).replace("T", " ")}",
                    color = ChaProdeMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // Badge de Estado oficial
                val statusType = when {
                    isFinished -> MatchStatusType.Finished()
                    isLive -> MatchStatusType.Live()
                    match.cerrado -> MatchStatusType.Locked("BLOQUEADO")
                    match.minutosRestantesParaCierre in 1..15 -> MatchStatusType.Closing("CIERRA EN ${match.minutosRestantesParaCierre}M")
                    match.tienePronosticoGuardado -> MatchStatusType.Saved("GUARDADO")
                    else -> MatchStatusType.Closing("ABIERTO")
                }

                MatchStatusBadge(status = statusType)
            }

            HorizontalDivider(color = ChaProdeBorder, thickness = 0.5.dp)

            // Cuerpo Central: Local vs Visitante con Steppers (Figma Make: .match-card__body)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Equipo Local
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TeamBadge(
                        code = match.localNombre.take(3),
                        badgeColor = TeamBadgeColor.White,
                        width = 46.dp,
                        height = 50.dp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = match.localNombre,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Área Central de Marcadores / Steppers
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = if (isFinished) "RESULTADO REAL" else if (isLocked) "PRONÓSTICO BLOQUEADO" else "TU PRONÓSTICO",
                        color = if (isFinished) ChaProdeGold else ChaProdeMuted,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.8.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (isFinished) {
                            // Mostrar marcadores reales oficiales
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = ChaProdeBg,
                                border = BorderStroke(1.dp, ChaProdeBorderLight),
                                modifier = Modifier.height(38.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(horizontal = 14.dp)
                                ) {
                                    Text(
                                        text = "${match.golesLocalReal ?: 0}  :  ${match.golesVisitanteReal ?: 0}",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                }
                            }
                        } else {
                            // Stepper Local
                            ScoreStepper(
                                value = match.editedGolesLocal,
                                disabled = !canPredict,
                                onValueChange = onLocalGoalsChange
                            )

                            Text(
                                text = ":",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = ChaProdeMuted
                            )

                            // Stepper Visitante
                            ScoreStepper(
                                value = match.editedGolesVisitante,
                                disabled = !canPredict,
                                onValueChange = onVisitorGoalsChange
                            )
                        }
                    }
                }

                // Equipo Visitante
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TeamBadge(
                        code = match.visitanteNombre.take(3),
                        badgeColor = TeamBadgeColor.Sky,
                        width = 46.dp,
                        height = 50.dp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = match.visitanteNombre,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            HorizontalDivider(color = ChaProdeBorder, thickness = 0.5.dp)

            // Pie de Tarjeta: Estado del pronóstico, puntos o botón de guardar (Figma Make: .match-card__footer)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ChaProdeSurfaceSoft.copy(alpha = 0.5f))
                    .padding(horizontal = 14.dp, vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isFinished -> {
                        val resultText = when (match.puntosGanados) {
                            3 -> "⭐ ¡PLENO EXACTO! (+3 PUNTOS)"
                            1 -> "✓ TENDENCIA ACERTADA (+1 PUNTO)"
                            else -> "✗ SIN PUNTOS (0 PUNTOS)"
                        }
                        val resultColor = when (match.puntosGanados) {
                            3 -> ChaProdeSuccess
                            1 -> ChaProdeSky
                            else -> ChaProdeMuted
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = resultText,
                                color = resultColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            if (match.tienePronosticoGuardado) {
                                Text(
                                    text = "  ·  Tu predicción: ${match.miPronosticoLocal}-${match.miPronosticoVisitante}",
                                    color = ChaProdeMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    isSaving -> {
                        CircularProgressIndicator(
                            color = ChaProdeSky,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    match.isEdited && canPredict -> {
                        // Botón de guardar cuando se modificaron los goles
                        ChaProdeButton(
                            text = "Guardar pronóstico",
                            variant = ButtonVariant.Primary,
                            trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                            onClick = onSaveClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                        )
                    }

                    match.tienePronosticoGuardado -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = ChaProdeSuccess,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Pronóstico guardado: ${match.miPronosticoLocal} - ${match.miPronosticoVisitante}",
                                color = ChaProdeSuccess,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    match.cerrado -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = ChaProdeDanger,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Pronósticos cerrados 15 min antes del inicio",
                                color = ChaProdeDanger,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    else -> {
                        // Partido abierto sin pronóstico guardado aún
                        ChaProdeButton(
                            text = "Confirmar pronóstico",
                            variant = ButtonVariant.Secondary,
                            trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                            onClick = onSaveClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F172A)
@Composable
fun FixtureScreenModernPreview() {
    FixtureContent(
        torneoNombre = "Champions League",
        state = PredictionUiState(),
        onEvent = {},
        onNavigateBack = {}
    )
}
