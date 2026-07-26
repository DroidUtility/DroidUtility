package com.frostre1997.droidutility.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.frostre1997.droidutility.Screen

@Composable
fun FloatingBottomBar(
    currentRoute: String,
    onItemClick: (String) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val screens = listOf(
        Screen.Home,
        Screen.Terminal,
        Screen.Debloat,
        Screen.Shell,
        Screen.Settings
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .padding(horizontal = 16.dp)
            .blur(16.dp), // blur effect – works on Android 12+, falls back gracefully
        color = colorScheme.surface.copy(alpha = 0.5f),
        shape = RoundedCornerShape(36.dp),
        shadowElevation = 12.dp,
        tonalElevation = 0.dp,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = Color.White.copy(alpha = 0.15f)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            screens.forEach { screen ->
                val selected = currentRoute == screen.route
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onItemClick(screen.route) }
                ) {
                    Icon(
                        screen.icon,
                        contentDescription = screen.title,
                        tint = if (selected) colorScheme.primary else colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        screen.title,
                        fontSize = 12.sp,
                        color = if (selected) colorScheme.primary else colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
