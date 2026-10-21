package com.example.ui.components

import android.media.MediaPlayer
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.FileItem
import kotlinx.coroutines.delay
import java.io.File
import java.util.Locale

@Composable
fun AudioPlayerDialog(
    item: FileItem,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var currentPositionMs by remember { mutableIntStateOf(0) }
    var durationMs by remember { mutableIntStateOf(0) }
    var isLooping by remember { mutableStateOf(false) }

    val mediaPlayer = remember {
        MediaPlayer().apply {
            try {
                setDataSource(item.path)
                prepare()
                durationMs = duration
                start()
                isPlaying = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                mediaPlayer.stop()
                mediaPlayer.release()
            } catch (_: Exception) {}
        }
    }

    // Position update loop
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            try {
                if (mediaPlayer.isPlaying) {
                    currentPositionMs = mediaPlayer.currentPosition
                } else if (currentPositionMs >= durationMs && durationMs > 0) {
                    isPlaying = false
                }
            } catch (_: Exception) {}
            delay(250)
        }
    }

    // Pulse animation for audio disc
    val infiniteTransition = rememberInfiniteTransition(label = "disc_anim")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isPlaying) 1.08f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    fun formatMs(ms: Int): String {
        val totalSec = (ms / 1000).coerceAtLeast(0)
        val minutes = totalSec / 60
        val seconds = totalSec % 60
        return String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .testTag("audio_player_dialog"),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top header with close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "مشغل الصوتيات",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Pulsing vinyl / music visualizer
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
                                    MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Track name & size
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    text = "${item.formattedSize} • ${item.extension.uppercase()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Progress slider
                val progress = if (durationMs > 0) (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
                Slider(
                    value = progress,
                    onValueChange = { newPos ->
                        val targetMs = (newPos * durationMs).toInt()
                        try {
                            mediaPlayer.seekTo(targetMs)
                            currentPositionMs = targetMs
                        } catch (_: Exception) {}
                    },
                    modifier = Modifier.fillMaxWidth().testTag("audio_seek_slider"),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    )
                )

                // Time labels
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatMs(currentPositionMs),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatMs(durationMs),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Playback Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Replay 10s
                    IconButton(
                        onClick = {
                            try {
                                val target = (mediaPlayer.currentPosition - 10000).coerceAtLeast(0)
                                mediaPlayer.seekTo(target)
                                currentPositionMs = target
                            } catch (_: Exception) {}
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay10,
                            contentDescription = "رجوع 10 ثوان",
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Play/Pause Main Button
                    FilledIconButton(
                        onClick = {
                            try {
                                if (mediaPlayer.isPlaying) {
                                    mediaPlayer.pause()
                                    isPlaying = false
                                } else {
                                    mediaPlayer.start()
                                    isPlaying = true
                                }
                            } catch (_: Exception) {}
                        },
                        modifier = Modifier.size(64.dp).testTag("audio_play_pause_button"),
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "إيقاف مؤقت" else "تشغيل",
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    // Forward 10s
                    IconButton(
                        onClick = {
                            try {
                                val target = (mediaPlayer.currentPosition + 10000).coerceAtMost(durationMs)
                                mediaPlayer.seekTo(target)
                                currentPositionMs = target
                            } catch (_: Exception) {}
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Forward10,
                            contentDescription = "تقديم 10 ثوان",
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Loop toggle
                    IconButton(
                        onClick = {
                            isLooping = !isLooping
                            try {
                                mediaPlayer.isLooping = isLooping
                            } catch (_: Exception) {}
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Repeat,
                            contentDescription = "تكرار",
                            tint = if (isLooping) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}
