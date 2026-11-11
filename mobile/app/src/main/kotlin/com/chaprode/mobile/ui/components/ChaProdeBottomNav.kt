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

/**
 * =========================================================================================
 * BottomNavDestination: Modelo de datos para cada pestaña de la barra inferior.
 * =========================================================================================
 * @property screen Destino de navegación asociado ([Screen.Home], [Screen.Fixture], etc.).
 * @property label Texto en español visible debajo del icono (ej: "Inicio", "Ligas").
 * @property icon Vector de Material Icons representativo de la sección.
 */
data class BottomNavDestination(
    val screen: Screen,
    val label: String,
    val icon: ImageVector
)

/**
 * Lista de las 5 secciones principales que conforman la navegación global de ChaProde.
 */
val bottomNavItems = listOf(
    BottomNavDestination(Screen.Home, "Inicio", Icons.Default.Home),
    BottomNavDestination(Screen.Fixture, "Fixture", Icons.Default.SportsSoccer),
    BottomNavDestination(Screen.Leagues, "Ligas", Icons.Default.Group),
    BottomNavDestination(Screen.Ranking, "Ranking", Icons.Default.EmojiEvents),
    BottomNavDestination(Screen.Profile, "Perfil", Icons.Default.Person)
)

/**
 * =========================================================================================
 * ChaProdeBottomNav: Barra de navegación inferior persistente (Figma Make: .bottom-nav).
 * =========================================================================================
 * Propósito:
 * Proporcionar navegación con un solo toque entre las 5 áreas clave de la aplicación.
 * 
 * Detalles de diseño y experiencia de usuario (UX):
 * 1. Elevación y sombreado: Fondo oscuro Deep Slate con sombra superior para despegarse del contenido.
 * 2. Indicador activo (Active Pill): Cuando una pestaña está seleccionada, se resalta en celeste ([ChaProdeSky])
 *    y despliega una pequeña pastilla luminosa debajo del icono (equivalente a `.bottom-nav button i` de Figma).
 * 3. Safe Area Insets: Utiliza [navigationBarsPadding] para respetar la barra gestual o los 3 botones de Android.
 * 4. Efecto Ripple táctil: Onda suave confinada a 28dp al tocar cada pestaña.
 * 
 * @param currentRoute Ruta actual obtenida del BackStack para saber qué pestaña resaltar.
 * @param onNavigate Callback disparado al hacer clic en una pestaña con la pantalla destino [Screen].
 * @param modifier Modificador Compose para personalizar el contenedor exterior.
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
            .navigationBarsPadding() // Respeta la barra de navegación del sistema Android (API 30+)
            .height(72.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            bottomNavItems.forEach { destination ->
                // Determina si esta pestaña coincide con la ruta activa actual
                val isSelected = currentRoute == destination.screen.route
                val contentColor = if (isSelected) ChaProdeSky else ChaProdeSubtle

                // Columna interactiva de cada pestaña
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
                    // Contenedor del icono
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

                    // ---------------------------------------------------------------------
                    // Línea indicadora activa: Se dibuja solo debajo de la pestaña activa
                    // ---------------------------------------------------------------------
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

                    // Texto de la pestaña
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
