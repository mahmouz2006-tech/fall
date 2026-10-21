package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File

@Composable
fun BreadcrumbBar(
    currentDir: File,
    rootDir: File,
    onNavigateToDir: (File) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Build hierarchy from root down to current
    val pathSegments = mutableListOf<File>()
    var curr: File? = currentDir
    while (curr != null) {
        pathSegments.add(0, curr)
        if (curr.absolutePath == rootDir.absolutePath) {
            break
        }
        curr = curr.parentFile
    }

    LaunchedEffect(currentDir) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("breadcrumb_bar"),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .horizontalScroll(scrollState)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            pathSegments.forEachIndexed { index, file ->
                val isLast = index == pathSegments.size - 1
                val isRoot = index == 0
                val displayName = if (isRoot) "الرئيسية" else file.name

                TextButton(
                    onClick = { onNavigateToDir(file) },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = if (isLast) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.testTag("breadcrumb_${index}")
                ) {
                    if (isRoot) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "الرئيسية",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = displayName,
                        fontSize = 13.sp,
                        fontWeight = if (isLast) FontWeight.Bold else FontWeight.Normal
                    )
                }

                if (!isLast) {
                    Icon(
                        imageVector = if (isRtl) Icons.Default.ChevronLeft else Icons.Default.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}
