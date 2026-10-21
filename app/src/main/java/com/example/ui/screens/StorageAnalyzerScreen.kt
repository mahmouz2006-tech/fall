package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FileCategory
import com.example.model.FileItem
import com.example.model.StorageStats
import com.example.ui.components.FileListItem
import com.example.ui.theme.SkyPrimary
import com.example.ui.theme.TealSecondary

@Composable
fun StorageAnalyzerScreen(
    stats: StorageStats,
    largeFiles: List<FileItem>,
    onOpenFile: (FileItem) -> Unit,
    onFileOptions: (FileItem) -> Unit,
    onCleanCacheClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = stats.usedPercentage,
        animationSpec = tween(1000),
        label = "analyzer_storage_anim"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("storage_analyzer_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Storage Gauge Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "تحليل السعة التخزينية",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    // Circular Storage Gauge
                    val primaryColor = MaterialTheme.colorScheme.primary
                    val trackColor = MaterialTheme.colorScheme.surfaceVariant
                    Box(
                        modifier = Modifier.size(170.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeWidth = 18.dp.toPx()
                            // Background track
                            drawArc(
                                color = trackColor,
                                startAngle = 135f,
                                sweepAngle = 270f,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                            // Used progress
                            drawArc(
                                color = primaryColor,
                                startAngle = 135f,
                                sweepAngle = 270f * animatedProgress,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${(animatedProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "المستخدم",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Stats row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatColumn(
                            title = "المستخدم",
                            value = StorageStats.formatBytes(stats.usedBytes),
                            color = MaterialTheme.colorScheme.primary
                        )
                        StatColumn(
                            title = "المتبقي",
                            value = StorageStats.formatBytes(stats.freeBytes),
                            color = MaterialTheme.colorScheme.secondary
                        )
                        StatColumn(
                            title = "الإجمالي",
                            value = StorageStats.formatBytes(stats.totalBytes),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Quick Clean Button
                    FilledTonalButton(
                        onClick = onCleanCacheClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("clean_cache_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CleaningServices,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "تنظيف الملفات المؤقتة والكاش",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Category Breakdown Header
        item {
            Text(
                text = "استهلاك السعة حسب النوع",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Category cards
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FileCategory.entries.forEach { category ->
                    val bytes = stats.categoryBytes[category] ?: 0L
                    val count = stats.categoryCounts[category] ?: 0
                    val percent = if (stats.usedBytes > 0) (bytes.toFloat() / stats.usedBytes.toFloat()) else 0f

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(category.color)
                                )
                                Column {
                                    Text(
                                        text = category.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "$count عنصر",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = StorageStats.formatBytes(bytes),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${(percent * 100).toInt()}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            }
        }

        // Large Files Header
        item {
            Text(
                text = "الملفات الكبيرة (> 10 ميجابايت)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (largeFiles.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "لا توجد ملفات كبيرة تستهلك مساحة زائدة",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(largeFiles, key = { "large_${it.path}" }) { item ->
                FileListItem(
                    item = item,
                    onClick = { onOpenFile(item) },
                    onLongClick = { onFileOptions(item) },
                    onMoreClick = { onFileOptions(item) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(70.dp))
        }
    }
}

@Composable
private fun StatColumn(
    title: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
