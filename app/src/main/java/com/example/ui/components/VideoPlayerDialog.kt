package com.example.ui.components

import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.FileItem
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayerDialog(
    item: FileItem,
    onClose: () -> Unit
) {
    var videoViewRef by remember { mutableStateOf<VideoView?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            try {
                videoViewRef?.stopPlayback()
            } catch (_: Exception) {}
        }
    }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            containerColor = Color.Black,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "${item.formattedSize} • فيديو",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.LightGray
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onClose) {
                            Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Black.copy(alpha = 0.7f)
                    )
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color.Black)
                    .testTag("video_player_dialog"),
                contentAlignment = Alignment.Center
            ) {
                AndroidView(
                    factory = { ctx ->
                        VideoView(ctx).apply {
                            val mediaController = MediaController(ctx)
                            mediaController.setAnchorView(this)
                            setMediaController(mediaController)
                            setVideoURI(Uri.fromFile(File(item.path)))
                            setOnPreparedListener { mp ->
                                mp.isLooping = true
                                start()
                            }
                            videoViewRef = this
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
