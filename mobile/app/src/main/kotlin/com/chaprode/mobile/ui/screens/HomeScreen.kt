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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chaprode.mobile.model.MatchWithPredictionItem
import com.chaprode.mobile.model.User
import com.chaprode.mobile.ui.auth.AuthViewModel
import com.chaprode.mobile.ui.components.ButtonVariant
import com.chaprode.mobile.ui.components.ChaProdeButton
import com.chaprode.mobile.ui.components.RequestNotificationPermissionEffect
import com.chaprode.mobile.ui.components.TeamBadge
import com.chaprode.mobile.ui.components.TeamBadgeColor
import com.chaprode.mobile.ui.league.LeagueUiEvent
import com.chaprode.mobile.ui.league.LeagueViewModel
import com.chaprode.mobile.ui.prediction.PredictionUiEvent
import com.chaprode.mobile.ui.prediction.PredictionViewModel
import com.chaprode.mobile.ui.ranking.RankingUiEvent
import com.chaprode.mobile.ui.ranking.RankingViewModel
import com.chaprode.mobile.ui.theme.*
import kotlinx.coroutines.delay
import java.util.Calendar

@Composable
fun HomeScreen(
    viewModel: AuthViewModel,
    predictionViewModel: PredictionViewModel? = null,
    rankingViewModel: RankingViewModel? = null,
    leagueViewModel: LeagueViewModel? = null,
    onNavigateToFixture: () -> Unit = {},
    onNavigateToRanking: () -> Unit = {},
    onNavigateToLeagues: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    RequestNotificationPermissionEffect()

    // Cargar datos reales en segundo plano para el Hero match, stats y rankings
    LaunchedEffect(Unit) {
        predictionViewModel?.onEvent(PredictionUiEvent.LoadMatches("todos"))
        rankingViewModel?.onEvent(RankingUiEvent.LoadRanking("todos"))
        leagueViewModel?.onEvent(LeagueUiEvent.LoadLeagues)
    }

    val authState by viewModel.uiState.collectAsState()
    val predictionState = predictionViewModel?.uiState?.collectAsState()?.value
    val rankingState = rankingViewModel?.uiState?.collectAsState()?.value
    val leagueState = leagueViewModel?.uiState?.collectAsState()?.value

    val user = authState.user
    val matches = predictionState?.matches.orEmpty()
    val nextMatch = matches.firstOrNull { it.estado == "PENDIENTE" || it.estado == "EN_JUEGO" }
    
    // Cálculo de estadísticas reales del usuario
    val myPredictedMatches = matches.filter { it.miPronosticoLocal != null && it.miPronosticoVisitante != null }
    val totalUserPoints = myPredictedMatches.sumOf { it.puntosGanados }
    val plenosCount = myPredictedMatches.count { it.puntosGanados == 3 }
    val pendingMatchesCount = matches.count { it.estado == "PENDIENTE" && (it.miPronosticoLocal == null || it.miPronosticoVisitante == null) }

    val userRank = rankingState?.ranking?.firstOrNull { it.isCurrentUser }
    val userRankText = if (userRank != null) "Vas #${userRank.posicion} global" else "Ver clasificación"
    val leaguesCount = leagueState?.leagues?.size ?: 0

    var notificationToast by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(notificationToast) {
        notificationToast?.let {
            snackbarHostState.showSnackbar(it)
            notificationToast = null
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            HomeTopBar(
                username = user?.username ?: "Pronosticador",
                points = totalUserPoints,
                onNotificationsClick = {
                    notificationToast = "No tienes nuevas notificaciones pendientes"
                }
            )
        },
        containerColor = ChaProdeBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // 1. HERO MATCH: Próximo Partido Destacado
            HeroMatchSection(
                nextMatch = nextMatch,
                onPredictClick = onNavigateToFixture
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 2. ACCESOS RÁPIDOS ASIMÉTRICOS
            SectionHeader(
                eyebrow = "ACCESOS RÁPIDOS",
                title = "¿Qué quieres hacer?"
            )

            Spacer(modifier = Modifier.height(10.dp))

            QuickAccessGrid(
                pendingMatchesCount = pendingMatchesCount,
                userRankText = userRankText,
                leaguesCount = leaguesCount,
                onFixtureClick = onNavigateToFixture,
                onRankingClick = onNavigateToRanking,
                onLeaguesClick = onNavigateToLeagues
            )

            Spacer(modifier = Modifier.height(26.dp))

            // 3. SECCIÓN TU RENDIMIENTO
            SectionHeader(
                eyebrow = "TU RENDIMIENTO",
                title = "Estadísticas generales",
                actionLabel = "Ver ranking",
                onActionClick = onNavigateToRanking
            )

            Spacer(modifier = Modifier.height(10.dp))

            PerformanceStatsCard(
                predictedCount = myPredictedMatches.size,
                totalMatchesCount = matches.size,
                exactCount = plenosCount,
                totalPoints = totalUserPoints
            )

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

/**
 * Barra superior de Inicio con saludo contextual, chip de puntos y campana (Figma Make reference: .topbar).
 */
@Composable
fun HomeTopBar(
    username: String,
    points: Int,
    onNotificationsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 6..12 -> "BUENOS DÍAS"
            in 13..19 -> "BUENAS TARDES"
            else -> "BUENAS NOCHES"
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(ChaProdeBg)
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = greeting,
                color = ChaProdeMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.2.sp
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Text(
                    text = "Hola, $username",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "⚽", fontSize = 17.sp)
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Chip de Puntos Dorados
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = ChaProdeGold.copy(alpha = 0.10f),
                border = androidx.compose.foundation.BorderStroke(1.dp, ChaProdeGold.copy(alpha = 0.30f)),
                modifier = Modifier.height(36.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 11.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = ChaProdeGold,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = points.toString(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = ChaProdeGold
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "pts",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = ChaProdeMuted
                    )
                }
            }

            // Botón de Notificaciones con punto rojo activo
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, ChaProdeBorder, RoundedCornerShape(12.dp))
                    .background(ChaProdeSurface)
                    .clickable(onClick = onNotificationsClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notificaciones",
                    tint = ChaProdeSilver,
                    modifier = Modifier.size(20.dp)
                )
                // Punto rojo indicador
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = (-6).dp, y = 6.dp)
                        .background(ChaProdeDanger, CircleShape)
                        .border(1.5.dp, ChaProdeSurface, CircleShape)
                )
            }
        }
    }
}

/**
 * Tarjeta Hero del Próximo Partido Destacado con contador regresivo y CTA (Figma Make reference: .hero-match).
 */
@Composable
fun HeroMatchSection(
    nextMatch: MatchWithPredictionItem?,
    onPredictClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, ChaProdeSky.copy(alpha = 0.30f), RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ChaProdeSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xFF1E3048), ChaProdeSurface)
                    )
                )
        ) {
            // Acento vertical en el borde izquierdo (Sky a Gold)
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .align(Alignment.CenterStart)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(ChaProdeSky, ChaProdeGold)
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Cabecera del Hero: Eyebrow + Countdown
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(ChaProdeSuccess, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "PRÓXIMO PARTIDO",
                                color = ChaProdeSky,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.1.sp
                            )
                        }
                        Text(
                            text = nextMatch?.torneoNombre ?: "Champions League · Mundial 2026",
                            color = ChaProdeMuted,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    // Contador regresivo visual
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = ChaProdeBg.copy(alpha = 0.75f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ChaProdeBorder)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = ChaProdeGold,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (nextMatch?.estado == "EN_JUEGO") "EN JUEGO" else "Cierra en 15m",
                                color = ChaProdeGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Equipos versus
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Equipo Local
                    val localName = nextMatch?.localNombre ?: "Real Madrid"
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        TeamBadge(
                            code = localName.take(3),
                            badgeColor = TeamBadgeColor.White,
                            width = 52.dp,
                            height = 56.dp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = localName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Separador VS con horario
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    ) {
                        Text(
                            text = nextMatch?.fechaHora?.take(16)?.replace("T", " ") ?: "HOY 21:00",
                            color = ChaProdeMuted,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "VS",
                            color = ChaProdeSilver,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    // Equipo Visitante
                    val visitorName = nextMatch?.visitanteNombre ?: "Man. City"
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        TeamBadge(
                            code = visitorName.take(3),
                            badgeColor = TeamBadgeColor.Sky,
                            width = 52.dp,
                            height = 56.dp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = visitorName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Botón CTA directo para ingresar pronóstico
                ChaProdeButton(
                    text = "Ingresar pronóstico",
                    variant = ButtonVariant.Primary,
                    trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                    onClick = onPredictClick,
                    modifier = Modifier.fillMaxWidth().height(46.dp)
                )
            }
        }
    }
}

/**
 * Cuadrícula Asimétrica de Accesos Rápidos (Figma Make reference: .quick-grid).
 */
@Composable
fun QuickAccessGrid(
    pendingMatchesCount: Int,
    userRankText: String,
    leaguesCount: Int,
    onFixtureClick: () -> Unit,
    onRankingClick: () -> Unit,
    onLeaguesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Card Grande: Fixture & Pronósticos (Ocupa ancho completo)
        Card(
            onClick = onFixtureClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ChaProdeSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, ChaProdeBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(ChaProdeSky.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsSoccer,
                        contentDescription = null,
                        tint = ChaProdeSky,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Fixture & Pronósticos",
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontSize = 15.sp
                    )
                    Text(
                        text = if (pendingMatchesCount > 0) "$pendingMatchesCount partidos pendientes" else "Cartelera al día",
                        color = ChaProdeMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = ChaProdeSky,
                    modifier = Modifier.size(19.dp)
                )
            }
        }

        // Fila con 2 Cards Cuadradas: Ranking Global y Ligas de Amigos
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Card Ranking
            Card(
                onClick = onRankingClick,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ChaProdeSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, ChaProdeBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(ChaProdeGold.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = ChaProdeGold,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Tabla de\nPosiciones",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        lineHeight = 18.sp
                    )
                    Text(
                        text = userRankText,
                        color = ChaProdeMuted,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Card Ligas de Amigos
            Card(
                onClick = onLeaguesClick,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ChaProdeSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, ChaProdeBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFA78BFA).copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            tint = Color(0xFFA78BFA),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Mis Ligas\nde Amigos",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        lineHeight = 18.sp
                    )
                    Text(
                        text = "$leaguesCount ligas activas",
                        color = ChaProdeMuted,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

/**
 * Tarjeta de Rendimiento Semanal / General (Figma Make reference: .stats-card).
 */
@Composable
fun PerformanceStatsCard(
    predictedCount: Int,
    totalMatchesCount: Int,
    exactCount: Int,
    totalPoints: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, ChaProdeBorder, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ChaProdeSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            // Columna Pronósticos
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Pronósticos", fontSize = 11.sp, color = ChaProdeMuted, fontWeight = FontWeight.Bold)
                Text(
                    text = predictedCount.toString(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
                Text(
                    text = "de $totalMatchesCount partidos",
                    fontSize = 9.sp,
                    color = ChaProdeSubtle
                )
            }

            // Divisor vertical
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(44.dp)
                    .background(ChaProdeBorder)
            )

            // Columna Plenos Exactos
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Exactos", fontSize = 11.sp, color = ChaProdeMuted, fontWeight = FontWeight.Bold)
                Text(
                    text = exactCount.toString(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = ChaProdeSuccess,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
                val rate = if (predictedCount > 0) ((exactCount.toFloat() / predictedCount) * 100).toInt() else 0
                Text(
                    text = "$rate% acierto",
                    fontSize = 9.sp,
                    color = ChaProdeSubtle
                )
            }

            // Divisor vertical
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(44.dp)
                    .background(ChaProdeBorder)
            )

            // Columna Puntos
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Puntos", fontSize = 11.sp, color = ChaProdeMuted, fontWeight = FontWeight.Bold)
                Text(
                    text = if (totalPoints > 0) "+$totalPoints" else "$totalPoints",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = ChaProdeGold,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
                Text(
                    text = "Puntos acumulados",
                    fontSize = 9.sp,
                    color = ChaProdeSubtle
                )
            }
        }
    }
}

/**
 * Encabezado de sección con kicker y acción opcional.
 */
@Composable
fun SectionHeader(
    eyebrow: String,
    title: String,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column {
            Text(
                text = eyebrow,
                color = ChaProdeSky,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.1.sp
            )
            Text(
                text = title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        if (actionLabel != null && onActionClick != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onActionClick)
                    .padding(vertical = 4.dp, horizontal = 6.dp)
            ) {
                Text(
                    text = actionLabel,
                    color = ChaProdeSky,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = ChaProdeSky,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Preview(name = "Home Screen Modern", showBackground = true, backgroundColor = 0xFF0F172A)
@Composable
fun HomeScreenPreview() {
    HomeScreen(
        viewModel = AuthViewModel(),
        onNavigateToFixture = {},
        onNavigateToRanking = {},
        onNavigateToLeagues = {},
        onLogout = {}
    )
}
