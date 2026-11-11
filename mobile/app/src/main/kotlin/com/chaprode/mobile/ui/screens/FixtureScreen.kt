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

/**
 * =========================================================================================
 * FixtureScreen: Pantalla de Cartelera Deportiva y Gestión de Pronósticos.
 * =========================================================================================
 * Propósito:
 * Permite al usuario explorar todos los partidos programados, filtrarlos por torneo o estado,
 * y cargar/editar sus pronósticos de goles antes de que inicie cada encuentro.
 * 
 * Regla de Negocio Crítica:
 * - Los pronósticos se bloquean estrictamente 15 minutos antes de la hora oficial del partido.
 *   Una vez cerrado o iniciado el juego, los marcadores quedan bloqueados en modo lectura.
 * 
 * @param viewModel ViewModel encargado de la lógica de partidos, persistencia en Room y llamadas a la API Ktor.
 * @param torneoId Identificador del torneo inicial a consultar ("todos" por defecto).
 * @param torneoNombre Título descriptivo del torneo para la cabecera.
 * @param onNavigateBack Callback de navegación para volver a la pantalla anterior.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FixtureScreen(
    viewModel: PredictionViewModel,
    torneoId: String = "todos",
    torneoNombre: String = "Partidos Mundiales",
    onNavigateBack: () -> Unit
) {
    // Observa el estado reactivo del ViewModel de pronósticos
    val state by viewModel.uiState.collectAsState()

    // Carga los partidos del torneo al montar la pantalla o cuando cambia el torneoId
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

/**
 * =========================================================================================
 * FixtureContent: Estructura visual completa del Fixture con filtros y listado.
 * =========================================================================================
 * Componentes internos:
 * 1. TopBar con título dinámico y botón de actualización manual.
 * 2. Carrusel horizontal de torneos (Figma Make: .chip-scroller).
 * 3. Selector de filtros por estado del partido (Todos, Pendientes, En Juego, Finalizados, Mis Pronósticos).
 * 4. Listado perezoso (LazyColumn) con las tarjetas de partido ([MatchCardItem]).
 * 5. Tarjeta de resumen de historial de predicciones al pie ([PredictionsHistorySummary]).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FixtureContent(
    torneoNombre: String,
    state: PredictionUiState,
    onEvent: (PredictionUiEvent) -> Unit,
    onNavigateBack: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // Estados locales para los filtros seleccionados
    var selectedTournament by remember { mutableStateOf("Todos") }
    var selectedFilter by remember { mutableStateOf("Todos") }

    // -------------------------------------------------------------------------------------
    // Extracción dinámica de la lista de torneos presentes en los partidos cargados
    // -------------------------------------------------------------------------------------
    val tournamentList = remember(state.matches) {
        val unique = state.matches.mapNotNull { it.torneoNombre }.distinct()
        listOf("Todos") + unique
    }

    // Opciones disponibles de filtrado rápido
    val filterOptions = listOf("Todos", "Pendientes", "En Juego", "Finalizados", "Mis Pronósticos")

    // -------------------------------------------------------------------------------------
    // Filtrado reactivo en memoria según el torneo y el estado seleccionado
    // -------------------------------------------------------------------------------------
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

    // Manejo de mensajes de retroalimentación (Snackbar) ante eventos de guardado o errores
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
                    // Botón para refrescar partidos desde el backend y sincronizar con football-data.org
                    IconButton(onClick = { onEvent(PredictionUiEvent.LoadMatches("todos")) }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Actualizar",
                            tint = ChaProdeMuted
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
            // -----------------------------------------------------------------------------
            // 1. Carrusel Horizontal de Torneos (Figma Make: .chip-scroller)
            // -----------------------------------------------------------------------------
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tournamentList) { tournament ->
                    val isSelected = tournament == selectedTournament
                    Surface(
                        onClick = { selectedTournament = tournament },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) ChaProdeSky else ChaProdeSurface,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) ChaProdeSky else ChaProdeBorder
                        ),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = 14.dp)
                        ) {
                            Text(
                                text = tournament,
                                color = if (isSelected) ChaProdeBg else ChaProdeSilver,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // -----------------------------------------------------------------------------
            // 2. Filtros por Estado del Partido (Todos, Pendientes, En Juego, Finalizados)
            // -----------------------------------------------------------------------------
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(filterOptions) { filter ->
                    val isSelected = filter == selectedFilter
                    StatusFilterChip(
                        label = filter,
                        isSelected = isSelected,
                        onClick = { selectedFilter = filter }
                    )
                }
            }

            // -----------------------------------------------------------------------------
            // 3. Barra Informativa: Conteo de partidos encontrados
            // -----------------------------------------------------------------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PARTIDOS (${filteredMatches.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                    color = ChaProdeMuted
                )
                if (state.isLoading) {
                    Text(
                        text = "Actualizando...",
                        fontSize = 11.sp,
                        color = ChaProdeSky,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // -----------------------------------------------------------------------------
            // 4. Estados de Carga / Vacío / Listado de Partidos
            // -----------------------------------------------------------------------------
            if (state.isLoading && state.matches.isEmpty()) {
                // Spinner centrado de carga inicial
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = ChaProdeSky, strokeWidth = 3.dp)
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Cargando partidos...",
                            color = ChaProdeMuted,
                            fontSize = 14.sp
                        )
                    }
                }
            } else if (filteredMatches.isEmpty()) {
                // Tarjeta de estado vacío
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = ChaProdeSurface),
                        border = BorderStroke(1.dp, ChaProdeBorder),
                        modifier = Modifier.fillMaxWidth()
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
                                    .background(ChaProdeSky.copy(alpha = 0.12f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = ChaProdeSky,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "No hay partidos en esta categoría",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 15.sp,
                                textAlign = TextAlign.Center
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
                }
            } else {
                // Listado de Tarjetas de Partido
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
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

                    // Resumen de historial al final de la lista
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        PredictionsHistorySummary(matches = state.matches)
                    }
                }
            }
        }
    }
}

/**
 * =========================================================================================
 * MatchCardItem: Tarjeta individual de Partido y Pronóstico (Figma Make: .match-card).
 * =========================================================================================
 * Estructura:
 * 1. Cabecera (.match-card__top): Torneo, fecha/hora y badge de estado ([MatchStatusBadge]).
 * 2. Cuerpo Central (.match-card__body): Escudo Local + Steppers [-][goles][+] + Escudo Visitante.
 * 3. Pie (.match-card__footer): Estado del pronóstico, puntos obtenidos (+3 por pleno, +1 por tendencia)
 *    o botón interactivo para guardar el pronóstico.
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
            // -----------------------------------------------------------------------------
            // Cabecera: Torneo, Fecha y Badge de Estado
            // -----------------------------------------------------------------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ChaProdeSurfaceSoft)
                    .padding(horizontal = 14.dp, vertical = 9.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${match.torneoNombre ?: "Partido"} · ${match.fechaHora.take(16).replace("T", " ")}",
                    color = ChaProdeMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // Resolución del tipo de badge según la regla de negocio
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

            // -----------------------------------------------------------------------------
            // Cuerpo Central: Local vs Visitante con Steppers interactivos
            // -----------------------------------------------------------------------------
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
                            // Mostrar marcadores reales oficiales si el partido ya terminó
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
                            // Stepper Local interactivo
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

                            // Stepper Visitante interactivo
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

            // -----------------------------------------------------------------------------
            // Pie de Tarjeta: Estado del pronóstico, puntos ganados o botón de confirmación
            // -----------------------------------------------------------------------------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ChaProdeSurfaceSoft.copy(alpha = 0.5f))
                    .padding(horizontal = 14.dp, vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                when {
                    // Partido finalizado: Feedback de puntuación
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

                    // Estado de guardado en red en curso
                    isSaving -> {
                        CircularProgressIndicator(
                            color = ChaProdeSky,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Se modificaron los goles en pantalla: Muestra botón primario para guardar
                    match.isEdited && canPredict -> {
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

                    // Ya tiene un pronóstico persistido y no ha sido editado
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

                    // Partido bloqueado por la regla de 15 minutos
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

                    // Partido abierto sin pronóstico guardado aún
                    else -> {
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

/**
 * Chip individual para la barra de filtros por estado del partido.
 */
@Composable
fun StatusFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) ChaProdeSurfaceRaised else Color.Transparent,
        border = BorderStroke(1.dp, if (isSelected) ChaProdeSky.copy(alpha = 0.5f) else ChaProdeBorder),
        modifier = Modifier.height(30.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Text(
                text = label,
                color = if (isSelected) ChaProdeSky else ChaProdeMuted,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

/**
 * =========================================================================================
 * PredictionsHistorySummary: Tarjeta de Resumen del Historial de Pronósticos.
 * =========================================================================================
 * Calcula en tiempo real a partir de la lista de partidos:
 * - Total de partidos que tienen un pronóstico registrado.
 * - Cantidad de plenos exactos conseguidos (+3 pts).
 * - Aciertos de tendencia ganadora (+1 pt).
 * - Sumatoria total de puntos acumulados.
 */
@Composable
fun PredictionsHistorySummary(matches: List<MatchWithPredictionItem>) {
    val myPredicted = matches.filter { it.tienePronosticoGuardado }
    val finishedCount = myPredicted.count { it.estado == "FINALIZADO" }
    val exactHits = myPredicted.count { it.puntosGanados == 3 }
    val tendencyHits = myPredicted.count { it.puntosGanados == 1 }
    val totalPoints = myPredicted.sumOf { it.puntosGanados }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = ChaProdeSurfaceSoft),
        border = BorderStroke(1.dp, ChaProdeBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = ChaProdeGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "HISTORIAL DE PRONÓSTICOS",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ChaProdeGold.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "$totalPoints PTS GANADOS",
                        color = ChaProdeGold,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Fila con 3 estadísticas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Pronosticados", color = ChaProdeMuted, fontSize = 10.sp)
                    Text(
                        text = "${myPredicted.size}",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Plenos Exactos", color = ChaProdeMuted, fontSize = 10.sp)
                    Text(
                        text = "$exactHits",
                        color = ChaProdeSuccess,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Tendencias", color = ChaProdeMuted, fontSize = 10.sp)
                    Text(
                        text = "$tendencyHits",
                        color = ChaProdeSky,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Finalizados", color = ChaProdeMuted, fontSize = 10.sp)
                    Text(
                        text = "$finishedCount",
                        color = ChaProdeSilver,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black
                    )
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
