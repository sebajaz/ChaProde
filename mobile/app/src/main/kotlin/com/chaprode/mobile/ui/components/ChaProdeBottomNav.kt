package com.chaprode.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chaprode.mobile.ui.navigation.Screen
import com.chaprode.mobile.ui.theme.*

data class BottomNavDestination(
    val screen: Screen,
    val label: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavDestination(Screen.Home, "Inicio", Icons.Default.Home),
    BottomNavDestination(Screen.Fixture, "Fixture", Icons.Default.SportsSoccer),
    BottomNavDestination(Screen.Leagues, "Ligas", Icons.Default.Group),
    BottomNavDestination(Screen.Ranking, "Ranking", Icons.Default.EmojiEvents),
    BottomNavDestination(Screen.Profile, "Perfil", Icons.Default.Person)
)

/**
 * Barra de navegación inferior persistente de cinco destinos (Figma Make reference: .bottom-nav).
 * Proporciona acceso rápido a Inicio, Fixture, Ligas, Ranking y Perfil con indicador activo en celeste.
 */
@Composable
fun ChaProdeBottomNav(
    currentRoute: String?,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 16.dp, ambientColor = ChaProdeBg, spotColor = ChaProdeBg)
            .border(width = 1.dp, color = ChaProdeBorder)
            .background(ChaProdeBg)
            .navigationBarsPadding()
            .height(72.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            bottomNavItems.forEach { destination ->
                val isSelected = currentRoute == destination.screen.route
                val contentColor = if (isSelected) ChaProdeSky else ChaProdeSubtle

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = rememberRipple(bounded = false, radius = 28.dp),
                            onClick = { onNavigate(destination.screen) }
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(bottom = 2.dp)
                    ) {
                        Icon(
                            imageVector = destination.icon,
                            contentDescription = destination.label,
                            tint = contentColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Línea indicadora activa debajo del icono (Figma Make reference: .bottom-nav button i)
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .width(14.dp)
                                .height(2.5.dp)
                                .background(ChaProdeSky, RoundedCornerShape(2.dp))
                        )
                    } else {
                        Spacer(modifier = Modifier.height(2.5.dp))
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = destination.label,
                        color = contentColor,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                    )
                }
            }
        }
    }
}
