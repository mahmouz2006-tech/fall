package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.outlined.FolderOff
import androidx.compose.material.icons.outlined.NoteAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FileItem
import com.example.ui.Clipboard
import com.example.ui.SortMode
import com.example.ui.components.BreadcrumbBar
import com.example.ui.components.FileGridItem
import com.example.ui.components.FileListItem
import java.io.File

@Composable
fun ExplorerScreen(
    currentDir: File,
    rootDir: File,
    files: List<FileItem>,
    isGridView: Boolean,
    sortMode: SortMode,
    clipboard: Clipboard?,
    onNavigateUp: () -> Boolean,
    onNavigateToDir: (File) -> Unit,
    onOpenFile: (FileItem) -> Unit,
    onFileOptions: (FileItem) -> Unit,
    onToggleFavorite: (FileItem) -> Unit,
    onToggleViewMode: () -> Unit,
    onSetSortMode: (SortMode) -> Unit,
    onCreateFolderClick: () -> Unit,
    onCreateFileClick: () -> Unit,
    onPasteClipboard: () -> Unit,
    onCancelClipboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(enabled = currentDir.absolutePath != rootDir.absolutePath) {
        onNavigateUp()
    }

    var showSortMenu by remember { mutableStateOf(false) }
    var showFabMenu by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("explorer_screen")
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Navigation & Actions Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (currentDir.absolutePath != rootDir.absolutePath) {
                                IconButton(
                                    onClick = { onNavigateUp() },
                                    modifier = Modifier.testTag("explorer_back_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "رجوع للخلف"
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = if (currentDir.absolutePath == rootDir.absolutePath) "التخزين الرئيسي" else currentDir.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${files.size} عنصر",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // View toggle (List / Grid)
                            IconButton(
                                onClick = onToggleViewMode,
                                modifier = Modifier.testTag("toggle_view_mode_button")
                            ) {
                                Icon(
                                    imageVector = if (isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                                    contentDescription = "تبديل العرض"
                                )
                            }

                            // Sort Menu
                            Box {
                                IconButton(
                                    onClick = { showSortMenu = true },
                                    modifier = Modifier.testTag("sort_menu_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Sort,
                                        contentDescription = "ترتيب"
                                    )
                                }
                                DropdownMenu(
                                    expanded = showSortMenu,
                                    onDismissRequest = { showSortMenu = false }
                                ) {
                                    SortMode.entries.forEach { mode ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = mode.title,
                                                    fontWeight = if (mode == sortMode) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (mode == sortMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                                )
                                            },
                                            onClick = {
                                                onSetSortMode(mode)
                                                showSortMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Breadcrumb hierarchy
                    BreadcrumbBar(
                        currentDir = currentDir,
                        rootDir = rootDir,
                        onNavigateToDir = onNavigateToDir
                    )
                }
            }

            // Clipboard Paste Bar
            AnimatedVisibility(
                visible = clipboard != null,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                if (clipboard != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ContentPaste,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = if (clipboard.isCut) "جاهز للنقل (${clipboard.files.size})" else "جاهز للنسخ (${clipboard.files.size})",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = onPasteClipboard,
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("paste_button")
                                ) {
                                    Text("لصق هنا", fontSize = 12.sp)
                                }
                                TextButton(
                                    onClick = onCancelClipboard,
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text("إلغاء", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Files Content
            if (files.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FolderOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = "هذا المجلد فارغ",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "يمكنك إضافة مجلد جديد أو ملف نصي باستخدام الزر أدناه",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else if (isGridView) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 80.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(files, key = { it.path }) { item ->
                        FileGridItem(
                            item = item,
                            onClick = { onOpenFile(item) },
                            onLongClick = { onFileOptions(item) },
                            onMoreClick = { onFileOptions(item) }
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(files, key = { it.path }) { item ->
                        FileListItem(
                            item = item,
                            onClick = { onOpenFile(item) },
                            onLongClick = { onFileOptions(item) },
                            onMoreClick = { onFileOptions(item) },
                            onFavoriteToggle = { onToggleFavorite(item) }
                        )
                    }
                }
            }
        }

        // FAB Actions for Creation
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AnimatedVisibility(visible = showFabMenu) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExtendedFloatingActionButton(
                        onClick = {
                            showFabMenu = false
                            onCreateFileClick()
                        },
                        icon = { Icon(Icons.Outlined.NoteAdd, contentDescription = null) },
                        text = { Text("ملف نصي جديد") },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.testTag("create_file_fab")
                    )

                    ExtendedFloatingActionButton(
                        onClick = {
                            showFabMenu = false
                            onCreateFolderClick()
                        },
                        icon = { Icon(Icons.Outlined.CreateNewFolder, contentDescription = null) },
                        text = { Text("مجلد جديد") },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.testTag("create_folder_fab")
                    )
                }
            }

            FloatingActionButton(
                onClick = { showFabMenu = !showFabMenu },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("main_add_fab")
            ) {
                Icon(
                    imageVector = if (showFabMenu) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = "إضافة"
                )
            }
        }
    }
}
