package com.frostre1997.droidutility.navigation

import androidx.compose.foundation.BorderStroke
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

    // Outermost container is completely transparent by default
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding() // Pushes the layout safely above the system nav keys
            .padding(bottom = 16.dp), // Adds a floating gap between the dock and screen edge
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 440.dp) // Adjusted slightly higher to comfortably house 5 items
                .height(68.dp)
                .padding(horizontal = 16.dp),
            color = Color.Black.copy(alpha = 0.85f),
            shape = RoundedCornerShape(34.dp),
            shadowElevation = 10.dp,
            tonalElevation = 0.dp,
            border = BorderStroke(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.12f)
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
                            tint = if (selected) colorScheme.primary else Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            screen.title,
                            fontSize = 11.sp,
                            color = if (selected) colorScheme.primary else Color.White.copy(alpha = 0.6f),
                            maxLines = 1,
                            modifier = Modifier.padding(top = 3.dp)
                        )
                    }
                }
            }
        }
    }
}
