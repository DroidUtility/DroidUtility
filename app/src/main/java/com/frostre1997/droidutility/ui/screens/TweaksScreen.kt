package com.frostre1997.droidutility.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.frostre1997.droidutility.data.Tweak
import com.frostre1997.droidutility.engine.TweaksEngine
import kotlinx.coroutines.launch

@Composable
fun TweaksScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    var tweaks by remember { mutableStateOf<List<Tweak>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    // Tracks which tweak is currently running
    var runningTweak by remember { mutableStateOf<String?>(null) }
    // Tracks success/failure state for UI feedback
    var tweakResult by remember { mutableStateOf<Pair<String, Boolean>?>(null) }

    LaunchedEffect(Unit) {
        tweaks = TweaksEngine.getTweaks(context)
        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Tweaks",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Apply general system configurations via Shizuku.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(tweaks) { tweak ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Build,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(
                                    text = tweak.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = tweak.command,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Show success or error icon if this specific tweak was just run
                                if (tweakResult?.first == tweak.name) {
                                    Icon(
                                        imageVector = if (tweakResult?.second == true) Icons.Default.CheckCircle else Icons.Default.Error,
                                        contentDescription = "Result",
                                        tint = if (tweakResult?.second == true) Color.Green else Color.Red,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                }

                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            runningTweak = tweak.name
                                            tweakResult = null
                                            
                                            val success = TweaksEngine.applyTweak(tweak)
                                            
                                            runningTweak = null
                                            tweakResult = Pair(tweak.name, success)
                                        }
                                    },
                                    enabled = runningTweak == null,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    if (runningTweak == tweak.name) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Text("Apply")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
