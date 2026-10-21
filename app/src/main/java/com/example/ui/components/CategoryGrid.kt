package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FileCategory

@Composable
fun CategoryGrid(
    categoryCounts: Map<FileCategory, Int>,
    onCategoryClick: (FileCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Row 1: Images, Videos, Audio, Documents
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CategoryCardItem(
                category = FileCategory.IMAGES,
                icon = Icons.Default.Image,
                count = categoryCounts[FileCategory.IMAGES] ?: 0,
                onClick = { onCategoryClick(FileCategory.IMAGES) },
                modifier = Modifier.weight(1f)
            )
            CategoryCardItem(
                category = FileCategory.VIDEOS,
                icon = Icons.Default.VideoFile,
                count = categoryCounts[FileCategory.VIDEOS] ?: 0,
                onClick = { onCategoryClick(FileCategory.VIDEOS) },
                modifier = Modifier.weight(1f)
            )
            CategoryCardItem(
                category = FileCategory.AUDIO,
                icon = Icons.Default.AudioFile,
                count = categoryCounts[FileCategory.AUDIO] ?: 0,
                onClick = { onCategoryClick(FileCategory.AUDIO) },
                modifier = Modifier.weight(1f)
            )
            CategoryCardItem(
                category = FileCategory.DOCUMENTS,
                icon = Icons.Default.Description,
                count = categoryCounts[FileCategory.DOCUMENTS] ?: 0,
                onClick = { onCategoryClick(FileCategory.DOCUMENTS) },
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2: Downloads, Archives, Apps
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CategoryCardItem(
                category = FileCategory.DOWNLOADS,
                icon = Icons.Default.Download,
                count = categoryCounts[FileCategory.DOWNLOADS] ?: 0,
                onClick = { onCategoryClick(FileCategory.DOWNLOADS) },
                modifier = Modifier.weight(1f)
            )
            CategoryCardItem(
                category = FileCategory.ARCHIVES,
                icon = Icons.Default.Archive,
                count = categoryCounts[FileCategory.ARCHIVES] ?: 0,
                onClick = { onCategoryClick(FileCategory.ARCHIVES) },
                modifier = Modifier.weight(1f)
            )
            CategoryCardItem(
                category = FileCategory.APPS,
                icon = Icons.Default.Android,
                count = categoryCounts[FileCategory.APPS] ?: 0,
                onClick = { onCategoryClick(FileCategory.APPS) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun CategoryCardItem(
    category: FileCategory,
    icon: ImageVector,
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("category_${category.name.lowercase()}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(category.color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = category.title,
                    tint = category.color,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = category.title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (count > 0) "$count" else "—",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
