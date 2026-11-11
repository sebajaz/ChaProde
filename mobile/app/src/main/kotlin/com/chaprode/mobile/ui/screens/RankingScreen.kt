package com.chaprode.mobile.ui.screens

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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chaprode.mobile.model.RankingUserItem
import com.chaprode.mobile.ui.ranking.RankingUiEvent
import com.chaprode.mobile.ui.ranking.RankingUiState
import com.chaprode.mobile.ui.ranking.RankingViewModel
import com.chaprode.mobile.ui.theme.*

/**
 * =========================================================================================
 * Paleta de Colores para Avatares de la Tabla de Posiciones (Figma Make: .avatar--*)
 * =========================================================================================
 * Lista de colores vivos para distinguir visualmente a los usuarios de la tabla.
 */
private val avatarColors = listOf(
    Color(0xFFC2410C), // Orange
    Color(0xFF2563EB), // Blue
    Color(0xFF059669), // Green
    Color(0xFFDB2777), // Pink
    Color(0xFF7C3AED), // Violet
    Color(0xFF0284C7)  // Sky
)

/**
 * Función determinista que asigna un color fijo de avatar según el nombre de usuario (hash).
 * Garantiza que el mismo usuario siempre tenga el mismo color en toda la aplicación.
 */
private fun getAvatarColor(name: String): Color {
    val index = kotlin.math.abs(name.hashCode()) % avatarColors.size
    return avatarColors[index]
}

/**
 * Modelo de opción para el selector desplegable de torneos en la pantalla de ranking.
 */
data class TournamentOption(
    val id: String,
    val label: String
)

/**
 * Torneos disponibles para consultar la tabla de posiciones general o por torneo.
 */
private val tournamentOptions = listOf(
    TournamentOption("todos", "Global · Todos los Torneos"),
    TournamentOption("champions", "UEFA Champions League"),
    TournamentOption("mundial", "Copa Mundial FIFA 2026"),
    TournamentOption("premier", "Premier League"),
    TournamentOption("activo", "Torneo Activo")
)

/**
 * =========================================================================================
 * RankingScreen: Pantalla de Tabla de Posiciones y Podio de Campeones.
 * =========================================================================================
 * Propósito:
 * Mostrar la clasificación general de los participantes del prode ordenados por puntaje total.
 * 
 * Componentes clave:
 * 1. Cabecera con icono de trofeo en caja dorada ([RankingHeader]).
 * 2. Selector de Torneo desplegable ([TournamentSelectorDropdown]).
 * 3. Podio de los 3 primeros puestos ([PodiumSection]) con tarimas metálicas (Oro, Plata, Bronce).
 * 4. Tabla de clasificación para puestos 4 en adelante ([LeaderboardHeader] y [LeaderboardRow]).
 * 5. Barra flotante sticky al pie ([MyRankingStickyBar]) que destaca la posición del usuario conectado.
 * 
 * @param viewModel ViewModel de clasificación que consulta los puntajes consolidados en la API Ktor.
 * @param torneoId Torneo inicialmente seleccionado ("todos").
 * @param torneoNombre Título descriptivo para la barra superior.
 * @param showBack Si es true, muestra una flecha para retroceder (útil si se abre desde Home).
 * @param onNavigateBack Callback de navegación hacia atrás.
 */
@Composable
fun RankingScreen(
    viewModel: RankingViewModel,
    torneoId: String = "todos",
    torneoNombre: String = "Ranking Global",
    showBack: Boolean = false,
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    var selectedTournamentId by remember { mutableStateOf(torneoId) }

    // Dispara la carga de clasificación cada vez que el usuario selecciona otro torneo
    LaunchedEffect(selectedTournamentId) {
        viewModel.onEvent(RankingUiEvent.LoadRanking(selectedTournamentId))
    }

    RankingContent(
        torneoNombre = torneoNombre,
        selectedTournamentId = selectedTournamentId,
        onTournamentChange = { newId ->
            selectedTournamentId = newId
        },
        state = state,
        showBack = showBack,
        onRefresh = {
            viewModel.onEvent(RankingUiEvent.LoadRanking(selectedTournamentId))
        },
        onNavigateBack = onNavigateBack
    )
}

/**
 * Contenido visual de la pantalla de Ranking.
 */
@Composable
fun RankingContent(
    torneoNombre: String = "Ranking Global",
    selectedTournamentId: String = "todos",
    onTournamentChange: (String) -> Unit = {},
    state: RankingUiState,
    showBack: Boolean = false,
    onRefresh: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    // Identifica al usuario actual dentro de la lista de ranking (si ya tiene puntos)
    val currentUser = remember(state.ranking) {
        state.ranking.firstOrNull { it.isCurrentUser }
    }

    Scaffold(
        containerColor = ChaProdeBg
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                // -------------------------------------------------------------------------
                // 1. Cabecera (Figma Make: .ranking-header)
                // -------------------------------------------------------------------------
                RankingHeader(
                    showBack = showBack,
                    onNavigateBack = onNavigateBack,
                    onRefresh = onRefresh
                )

                Spacer(modifier = Modifier.height(10.dp))

                // -------------------------------------------------------------------------
                // 2. Selector de Torneo (Figma Make: .ranking-select)
                // -------------------------------------------------------------------------
                TournamentSelectorDropdown(
                    selectedTournamentId = selectedTournamentId,
                    fallbackLabel = torneoNombre,
                    onTournamentChange = onTournamentChange
                )

                Spacer(modifier = Modifier.height(14.dp))

                // -------------------------------------------------------------------------
                // 3. Estado de Carga / Vacío / Listado con Podio y Filas
                // -------------------------------------------------------------------------
                if (state.isLoading && state.ranking.isEmpty()) {
                    // Carga inicial
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = ChaProdeSky, strokeWidth = 3.dp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Cargando clasificación...",
                                color = ChaProdeMuted,
                                fontSize = 13.sp
                            )
                        }
                    }
                } else if (state.ranking.isEmpty()) {
                    // Estado sin puntos registrados aún
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = ChaProdeSurface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ChaProdeBorder),
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
                                        .size(60.dp)
                                        .background(ChaProdeGold.copy(alpha = 0.1f), shape = CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.EmojiEvents,
                                        contentDescription = null,
                                        tint = ChaProdeGold,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Sin Puntos Aún",
                                    fontWeight = FontWeight.Bold,
                                    color = ChaProdeText,
                                    fontSize = 17.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Aún no hay puntos registrados en este torneo. Los puntajes se actualizarán al finalizar los partidos jugados.",
                                    color = ChaProdeMuted,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    // Lista con Podio + Cabecera de Tabla + Jugadores
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = if (currentUser != null) 92.dp else 24.dp)
                    ) {
                        // A) PODIO DE LOS 3 PRIMEROS (Figma Make: .podium)
                        item {
                            PodiumSection(
                                top1 = state.top1,
                                top2 = state.top2,
                                top3 = state.top3
                            )
                        }

                        // B) CABECERA DE TABLA DE POSICIONES (Figma Make: .leaderboard-title)
                        item {
                            Spacer(modifier = Modifier.height(12.dp))
                            LeaderboardHeader()
                        }

                        // C) JUGADORES DEL PUESTO 4 EN ADELANTE (Figma Make: .leader-row)
                        items(state.restOfRanking, key = { it.usuarioId }) { item ->
                            LeaderboardRow(item = item)
                        }
                    }
                }
            }

            // -----------------------------------------------------------------------------
            // 4. BARRA FLOTANTE / STICKY: "MI POSICIÓN" (Figma Make: .my-ranking)
            // -----------------------------------------------------------------------------
            if (currentUser != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    MyRankingStickyBar(currentUser = currentUser)
                }
            }
        }
    }
}

/**
 * Cabecera de la Pantalla de Ranking (Figma Make: .ranking-header).
 */
@Composable
fun RankingHeader(
    showBack: Boolean = false,
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

        // Ícono de Trofeo dentro de Caja de Acento Dorada (Figma Make: .ranking-header__icon)
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(ChaProdeGold.copy(alpha = 0.12f))
                .border(1.dp, ChaProdeGold.copy(alpha = 0.25f), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = ChaProdeGold,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "CLASIFICACIÓN",
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.5.sp,
                color = ChaProdeGold
            )
            Text(
                text = "Tabla de Posiciones",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = ChaProdeText,
                letterSpacing = (-0.5).sp
            )
        }

        // Botón de recarga manual
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
 * Selector desplegable de torneo (Figma Make: .ranking-select).
 */
@Composable
fun TournamentSelectorDropdown(
    selectedTournamentId: String,
    fallbackLabel: String = "Torneo Seleccionado",
    onTournamentChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val currentLabel = tournamentOptions.firstOrNull { it.id == selectedTournamentId }?.label
        ?: fallbackLabel

    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(ChaProdeSurface)
                .border(1.dp, ChaProdeBorder, RoundedCornerShape(14.dp))
                .clickable { expanded = true }
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.SportsSoccer,
                contentDescription = null,
                tint = ChaProdeSky,
                modifier = Modifier.size(18.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = currentLabel,
                color = ChaProdeText,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Icon(
                Icons.Default.ArrowDropDown,
                contentDescription = "Seleccionar torneo",
                tint = ChaProdeMuted,
                modifier = Modifier.size(20.dp)
            )
        }

        // Menú desplegable con las opciones de torneo
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(ChaProdeSurface)
                .border(1.dp, ChaProdeBorder, RoundedCornerShape(8.dp))
        ) {
            tournamentOptions.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option.label,
                            color = if (option.id == selectedTournamentId) ChaProdeSky else ChaProdeText,
                            fontWeight = if (option.id == selectedTournamentId) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    },
                    onClick = {
                        expanded = false
                        onTournamentChange(option.id)
                    }
                )
            }
        }
    }
}

/**
 * =========================================================================================
 * PodiumSection: Podio Olímpico/Deportivo de 3 Columnas (Figma Make: .podium).
 * =========================================================================================
 * Disposición:
 * - Izquierda: 2do Puesto (Plata - 78dp)
 * - Centro: 1er Puesto (Oro / Campeón - 105dp con corona ♛ y halo)
 * - Derecha: 3er Puesto (Bronce - 64dp)
 */
@Composable
fun PodiumSection(
    top1: RankingUserItem?,
    top2: RankingUserItem?,
    top3: RankingUserItem?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                // Brillo radial superior en oro
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(ChaProdeGold.copy(alpha = 0.16f), Color.Transparent),
                        center = Offset(size.width / 2f, 35f),
                        radius = size.width * 0.55f
                    )
                )
            },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = ChaProdeSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, ChaProdeBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp, start = 8.dp, end = 8.dp, bottom = 0.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                // 2DO PUESTO (Plata - Izquierda)
                PodiumColumn(
                    modifier = Modifier.weight(0.30f),
                    item = top2,
                    placeNumber = 2,
                    placeLabel = "PLATA",
                    standHeight = 78.dp,
                    avatarSize = 53.dp,
                    accentColor = ChaProdeSilver,
                    isFirst = false
                )

                // 1ER PUESTO (Oro - Centro / Destacado con Corona)
                PodiumColumn(
                    modifier = Modifier.weight(0.38f),
                    item = top1,
                    placeNumber = 1,
                    placeLabel = "CAMPEÓN",
                    standHeight = 105.dp,
                    avatarSize = 62.dp,
                    accentColor = ChaProdeGold,
                    isFirst = true
                )

                // 3ER PUESTO (Bronce - Derecha)
                PodiumColumn(
                    modifier = Modifier.weight(0.30f),
                    item = top3,
                    placeNumber = 3,
                    placeLabel = "BRONCE",
                    standHeight = 64.dp,
                    avatarSize = 53.dp,
                    accentColor = ChaProdeBronze,
                    isFirst = false
                )
            }
        }
    }
}

/**
 * Columna individual de cada puesto del podio.
 */
@Composable
fun PodiumColumn(
    modifier: Modifier = Modifier,
    item: RankingUserItem?,
    placeNumber: Int,
    placeLabel: String,
    standHeight: androidx.compose.ui.unit.Dp,
    avatarSize: androidx.compose.ui.unit.Dp,
    accentColor: Color,
    isFirst: Boolean
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Corona dorada para el Campeón
        if (isFirst) {
            Text(
                text = "♛",
                fontSize = 22.sp,
                color = ChaProdeGold,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        } else {
            Spacer(modifier = Modifier.height(18.dp))
        }

        // Avatar circular con borde metálico (Oro / Plata / Bronce)
        Box(
            modifier = Modifier
                .size(avatarSize)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF475569), ChaProdeSurface)
                    )
                )
                .border(
                    width = if (isFirst) 3.dp else 2.5.dp,
                    color = accentColor,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = item?.username?.take(2)?.uppercase() ?: "-",
                fontSize = if (isFirst) 16.sp else 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Nombre de Usuario
        Text(
            text = item?.username ?: "Sin definir",
            fontSize = if (isFirst) 12.sp else 11.sp,
            fontWeight = FontWeight.Bold,
            color = ChaProdeText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp)
        )

        // Puntaje del jugador
        Text(
            text = "${item?.puntosTotales ?: 0} pts",
            fontSize = if (isFirst) 11.sp else 10.sp,
            fontWeight = if (isFirst) FontWeight.Bold else FontWeight.SemiBold,
            color = if (isFirst) ChaProdeGold else ChaProdeMuted,
            modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
        )

        // Bloque del Podio con Gradiente Vertical y Borde Superior
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(standHeight)
                .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            accentColor.copy(alpha = if (isFirst) 0.24f else 0.15f),
                            accentColor.copy(alpha = 0.03f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    color = accentColor.copy(alpha = if (isFirst) 0.45f else 0.32f),
                    shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)
                )
                .padding(top = 8.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$placeNumber",
                    fontSize = if (isFirst) 28.sp else 23.sp,
                    fontWeight = FontWeight.Black,
                    fontStyle = FontStyle.Italic,
                    color = accentColor
                )
                Text(
                    text = placeLabel,
                    fontSize = if (isFirst) 7.5.sp else 6.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.2.sp,
                    color = accentColor
                )
            }
        }
    }
}

/**
 * Encabezado de Columnas de la Tabla (Figma Make: .leaderboard-title).
 */
@Composable
fun LeaderboardHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "POS.",
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.8.sp,
            color = ChaProdeSubtle,
            modifier = Modifier.width(32.dp),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = "JUGADOR",
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.8.sp,
            color = ChaProdeSubtle,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = "EXACTOS",
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.8.sp,
            color = ChaProdeSubtle,
            modifier = Modifier.width(55.dp),
            textAlign = TextAlign.End
        )

        Text(
            text = "PUNTOS",
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.8.sp,
            color = ChaProdeSubtle,
            modifier = Modifier.width(55.dp),
            textAlign = TextAlign.End
        )
    }
}

/**
 * Fila de Jugador en la Tabla de Posiciones (Puestos 4+) (Figma Make: .leader-row).
 */
@Composable
fun LeaderboardRow(item: RankingUserItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isCurrentUser) ChaProdeSurfaceRaised else ChaProdeSurface
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (item.isCurrentUser) 1.5.dp else 1.dp,
            color = if (item.isCurrentUser) ChaProdeSky else ChaProdeBorder
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Posición (#4, #5, etc.)
            Text(
                text = "${item.posicion}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = ChaProdeMuted,
                modifier = Modifier.width(32.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.width(6.dp))

            // Avatar Circular con Iniciales coloreadas
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(getAvatarColor(item.username)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.username.take(2).uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Nombre y Plenos / Aciertos
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.username,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = ChaProdeText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    // Badge "TÚ" en celeste si es el usuario autenticado
                    if (item.isCurrentUser) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(ChaProdeSky.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "TÚ",
                                color = ChaProdeSky,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }

                Text(
                    text = "🎯 ${item.plenosExactos} plenos  •  ⭐ ${item.aciertosTendencia} aciertos",
                    fontSize = 10.sp,
                    color = ChaProdeMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Cantidad de Plenos Exactos
            Text(
                text = "${item.plenosExactos}",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = ChaProdeMuted,
                modifier = Modifier.width(55.dp),
                textAlign = TextAlign.End
            )

            // Puntos Totales
            Text(
                text = "${item.puntosTotales} pts",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (item.isCurrentUser) ChaProdeGold else ChaProdeSilver,
                modifier = Modifier.width(55.dp),
                textAlign = TextAlign.End
            )
        }
    }
}

/**
 * Barra Flotante Inferior: "Mi Posición" (Figma Make: .my-ranking).
 */
@Composable
fun MyRankingStickyBar(currentUser: RankingUserItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ChaProdeSurface.copy(alpha = 0.98f)),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, ChaProdeSky.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Insignia de Posición
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(ChaProdeSky.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#${currentUser.posicion}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ChaProdeSky
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Nombre y Rol / Tu posición
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Tu posición",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    color = ChaProdeMuted
                )
                Text(
                    text = currentUser.username,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = ChaProdeText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Puntaje del Usuario
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Tu puntaje",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    color = ChaProdeMuted
                )
                Text(
                    text = "${currentUser.puntosTotales} pts",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = ChaProdeGold
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = ChaProdeSky,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Preview(name = "Ranking Screen Moderno", showBackground = true, backgroundColor = 0xFF0F172A)
@Composable
fun RankingScreenModernPreview() {
    val sampleRanking = listOf(
        RankingUserItem(1, "1", "Leo M.", 137, 8, 3, 11),
        RankingUserItem(2, "2", "Sofi", 124, 7, 2, 11),
        RankingUserItem(3, "3", "Juli", 121, 6, 3, 11),
        RankingUserItem(4, "4", "Marcos R.", 118, 5, 4, 11),
        RankingUserItem(5, "5", "Mateo G.", 112, 4, 5, 11),
        RankingUserItem(6, "6", "Lucas P.", 108, 4, 4, 11),
        RankingUserItem(7, "7", "Nico T.", 105, 3, 5, 11),
        RankingUserItem(8, "8", "Leo_El10", 98, 3, 3, 11, isCurrentUser = true)
    )

    RankingContent(
        torneoNombre = "Ranking Global",
        selectedTournamentId = "todos",
        onTournamentChange = {},
        state = RankingUiState(
            isLoading = false,
            ranking = sampleRanking
        ),
        onRefresh = {},
        onNavigateBack = {}
    )
}
