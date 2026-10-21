package com.example.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.FileItem
import com.example.ui.components.*
import com.example.ui.screens.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: FileManagerViewModel) {
    val context = LocalContext.current
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val currentDir by viewModel.currentDir.collectAsStateWithLifecycle()
    val files by viewModel.files.collectAsStateWithLifecycle()
    val recentFiles by viewModel.recentFiles.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val trashFiles by viewModel.trashFiles.collectAsStateWithLifecycle()
    val largeFiles by viewModel.largeFiles.collectAsStateWithLifecycle()
    val storageStats by viewModel.storageStats.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val categoryFiles by viewModel.categoryFiles.collectAsStateWithLifecycle()
    val isGridView by viewModel.isGridView.collectAsStateWithLifecycle()
    val sortMode by viewModel.sortMode.collectAsStateWithLifecycle()
    val clipboard by viewModel.clipboard.collectAsStateWithLifecycle()

    // Search state
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()
    var searchActive by remember { mutableStateOf(false) }

    // Dialog & sheet states
    val activeActionItem by viewModel.activeActionItem.collectAsStateWithLifecycle()
    val showCreateFolder by viewModel.showCreateFolderDialog.collectAsStateWithLifecycle()
    val showCreateFile by viewModel.showCreateFileDialog.collectAsStateWithLifecycle()
    val showRename by viewModel.showRenameDialog.collectAsStateWithLifecycle()
    val showDelete by viewModel.showDeleteDialog.collectAsStateWithLifecycle()
    val showDetails by viewModel.showDetailsDialog.collectAsStateWithLifecycle()
    val previewImage by viewModel.previewImageItem.collectAsStateWithLifecycle()
    val textEditorState by viewModel.textEditorState.collectAsStateWithLifecycle()
    val audioItem by viewModel.audioItem.collectAsStateWithLifecycle()
    val videoItem by viewModel.videoItem.collectAsStateWithLifecycle()
    val pdfItem by viewModel.pdfItem.collectAsStateWithLifecycle()
    val zipItem by viewModel.zipItem.collectAsStateWithLifecycle()
    val zipEntries by viewModel.zipEntries.collectAsStateWithLifecycle()
    val isExtractingZip by viewModel.isExtractingZip.collectAsStateWithLifecycle()
    val apkItem by viewModel.apkItem.collectAsStateWithLifecycle()
    val systemOpenItem by viewModel.systemOpenItem.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(systemOpenItem) {
        systemOpenItem?.let { item ->
            try {
                val file = java.io.File(item.path)
                val uri = androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                val mime = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension.lowercase()) ?: "*/*"
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, mime)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(Intent.createChooser(intent, "فتح بواسطة"))
            } catch (_: Exception) {
                viewModel.selectItemForAction(item)
                viewModel.showDetails(true)
            }
            viewModel.closeSystemOpen()
        }
    }

    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    // Storage permission state & launcher
    var hasPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Environment.isExternalStorageManager()
            } else {
                true
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        hasPermission = true
        viewModel.refreshDashboard()
        viewModel.loadDirectory(viewModel.currentDir.value)
    }

    BackHandler(enabled = currentScreen != Screen.HOME || searchActive) {
        if (searchActive) {
            searchActive = false
            viewModel.clearSearch()
        } else {
            viewModel.navigateToScreen(Screen.HOME)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (currentScreen != Screen.CATEGORY) {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text(
                                text = "مدير الملفات",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { searchActive = !searchActive },
                            modifier = Modifier.testTag("toggle_search_button")
                        ) {
                            Icon(
                                imageVector = if (searchActive) Icons.Default.Close else Icons.Default.Search,
                                contentDescription = "بحث"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_nav_bar")
            ) {
                NavigationBarItem(
                    selected = currentScreen == Screen.HOME && !searchActive,
                    onClick = {
                        searchActive = false
                        viewModel.clearSearch()
                        viewModel.navigateToScreen(Screen.HOME)
                    },
                    icon = { Icon(Icons.Default.Home, contentDescription = "الرئيسية") },
                    label = { Text("الرئيسية") },
                    modifier = Modifier.testTag("nav_home")
                )
                NavigationBarItem(
                    selected = currentScreen == Screen.EXPLORER,
                    onClick = {
                        searchActive = false
                        viewModel.clearSearch()
                        viewModel.navigateToScreen(Screen.EXPLORER)
                    },
                    icon = { Icon(Icons.Default.FolderOpen, contentDescription = "التصفح") },
                    label = { Text("التصفح") },
                    modifier = Modifier.testTag("nav_explorer")
                )
                NavigationBarItem(
                    selected = currentScreen == Screen.ANALYZER,
                    onClick = {
                        searchActive = false
                        viewModel.clearSearch()
                        viewModel.navigateToScreen(Screen.ANALYZER)
                    },
                    icon = { Icon(Icons.Default.PieChart, contentDescription = "الذاكرة") },
                    label = { Text("الذاكرة") },
                    modifier = Modifier.testTag("nav_analyzer")
                )
                NavigationBarItem(
                    selected = currentScreen == Screen.TRASH,
                    onClick = {
                        searchActive = false
                        viewModel.clearSearch()
                        viewModel.navigateToScreen(Screen.TRASH)
                    },
                    icon = { Icon(Icons.Default.Delete, contentDescription = "السلة") },
                    label = { Text("السلة") },
                    modifier = Modifier.testTag("nav_trash")
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Permission request banner if needed
            if (!hasPermission) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            Text(
                                text = "يمكنك منح إذن التخزين الكامل للوصول لجميع ملفات الجهاز",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Button(
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                    try {
                                        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                                            data = Uri.parse("package:${context.packageName}")
                                        }
                                        context.startActivity(intent)
                                    } catch (_: Exception) {
                                        val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                                        context.startActivity(intent)
                                    }
                                } else {
                                    permissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.READ_EXTERNAL_STORAGE,
                                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                                        )
                                    )
                                }
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("grant_permission_button")
                        ) {
                            Text("منح الإذن", fontSize = 11.sp)
                        }
                    }
                }
            }

            // Search Bar overlay
            AnimatedVisibility(visible = searchActive) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text("ابحث عن أي ملف أو مجلد...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.clearSearch() }) {
                                    Icon(Icons.Default.Clear, contentDescription = "مسح")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_text_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (isSearching) {
                        Text(
                            text = "نتائج البحث (${searchResults.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        if (searchResults.isEmpty()) {
                            Text(
                                text = "لم يتم العثور على أي ملف يطابق \"$searchQuery\"",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f, fill = false),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(searchResults, key = { "search_${it.path}" }) { item ->
                                    FileListItem(
                                        item = item,
                                        onClick = {
                                            searchActive = false
                                            viewModel.openItem(item)
                                        },
                                        onLongClick = { viewModel.selectItemForAction(item) },
                                        onMoreClick = { viewModel.selectItemForAction(item) },
                                        onFavoriteToggle = { viewModel.toggleFavorite(item) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Screen Content
            if (!searchActive || !isSearching) {
                when (currentScreen) {
                    Screen.HOME -> {
                        HomeScreen(
                            storageStats = storageStats,
                            recentFiles = recentFiles,
                            favorites = favorites,
                            onCategoryClick = { viewModel.openCategory(it) },
                            onAnalyzeClick = { viewModel.navigateToScreen(Screen.ANALYZER) },
                            onExploreClick = { viewModel.navigateToScreen(Screen.EXPLORER) },
                            onTrashClick = { viewModel.navigateToScreen(Screen.TRASH) },
                            onOpenFile = { viewModel.openItem(it) },
                            onFileOptions = { viewModel.selectItemForAction(it) },
                            onToggleFavorite = { viewModel.toggleFavorite(it) }
                        )
                    }

                    Screen.EXPLORER -> {
                        ExplorerScreen(
                            currentDir = currentDir,
                            rootDir = viewModel.repository.getRootDirectory(),
                            files = files,
                            isGridView = isGridView,
                            sortMode = sortMode,
                            clipboard = clipboard,
                            onNavigateUp = { viewModel.navigateUp() },
                            onNavigateToDir = { viewModel.navigateToBreadcrumb(it) },
                            onOpenFile = { viewModel.openItem(it) },
                            onFileOptions = { viewModel.selectItemForAction(it) },
                            onToggleFavorite = { viewModel.toggleFavorite(it) },
                            onToggleViewMode = { viewModel.toggleViewMode() },
                            onSetSortMode = { viewModel.setSortMode(it) },
                            onCreateFolderClick = { viewModel.showCreateFolder(true) },
                            onCreateFileClick = { viewModel.showCreateFile(true) },
                            onPasteClipboard = { viewModel.pasteClipboard() },
                            onCancelClipboard = { viewModel.clearClipboard() }
                        )
                    }

                    Screen.CATEGORY -> {
                        selectedCategory?.let { cat ->
                            CategoryScreen(
                                category = cat,
                                files = categoryFiles,
                                onBack = { viewModel.navigateToScreen(Screen.HOME) },
                                onOpenFile = { viewModel.openItem(it) },
                                onFileOptions = { viewModel.selectItemForAction(it) },
                                onToggleFavorite = { viewModel.toggleFavorite(it) }
                            )
                        }
                    }

                    Screen.ANALYZER -> {
                        StorageAnalyzerScreen(
                            stats = storageStats,
                            largeFiles = largeFiles,
                            onOpenFile = { viewModel.openItem(it) },
                            onFileOptions = { viewModel.selectItemForAction(it) },
                            onCleanCacheClick = {
                                viewModel.refreshDashboard()
                            }
                        )
                    }

                    Screen.TRASH -> {
                        RecycleBinScreen(
                            trashFiles = trashFiles,
                            onRestore = { viewModel.restoreTrashItem(it) },
                            onDeletePermanently = { viewModel.deleteItem(it, moveToTrash = false) },
                            onEmptyTrash = { viewModel.emptyTrash() }
                        )
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet for File Options
    activeActionItem?.let { item ->
        FileOptionsBottomSheet(
            item = item,
            onDismiss = { viewModel.selectItemForAction(null as FileItem?) },
            onOpen = { viewModel.openItem(item) },
            onRename = { viewModel.showRename(true) },
            onDelete = { viewModel.showDelete(true) },
            onCopy = { viewModel.copyToClipboard(item, isCut = false) },
            onCut = { viewModel.copyToClipboard(item, isCut = true) },
            onToggleFavorite = { viewModel.toggleFavorite(item) },
            onDetails = { viewModel.showDetails(true) }
        )
    }

    // Dialogs
    if (showCreateFolder) {
        CreateFolderDialog(
            onDismiss = { viewModel.showCreateFolder(false) },
            onConfirm = { name -> viewModel.createFolder(name) }
        )
    }

    if (showCreateFile) {
        CreateFileDialog(
            onDismiss = { viewModel.showCreateFile(false) },
            onConfirm = { name, content -> viewModel.createTextFile(name, content) }
        )
    }

    if (showRename) {
        activeActionItem?.let { item ->
            RenameDialog(
                currentName = item.name,
                onDismiss = { viewModel.showRename(false) },
                onConfirm = { newName -> viewModel.renameItem(item, newName) }
            )
        }
    }

    if (showDelete) {
        activeActionItem?.let { item ->
            DeleteConfirmDialog(
                itemName = item.name,
                onDismiss = { viewModel.showDelete(false) },
                onConfirm = { moveToTrash -> viewModel.deleteItem(item, moveToTrash) }
            )
        }
    }

    if (showDetails) {
        activeActionItem?.let { item ->
            FileDetailsDialog(
                item = item,
                onDismiss = { viewModel.showDetails(false) }
            )
        }
    }

    // Text Editor Dialog
    textEditorState?.let { editorState ->
        TextEditorDialog(
            state = editorState,
            onContentChange = { viewModel.updateEditorContent(it) },
            onSave = { viewModel.saveTextEditor() },
            onClose = { viewModel.closeTextEditor() }
        )
    }

    // Image Viewer Dialog
    previewImage?.let { imgItem ->
        ImageViewerDialog(
            item = imgItem,
            onClose = { viewModel.closePreviewImage() }
        )
    }

    // Audio Player Dialog
    audioItem?.let { aud ->
        AudioPlayerDialog(
            item = aud,
            onClose = { viewModel.closeAudio() }
        )
    }

    // Video Player Dialog
    videoItem?.let { vid ->
        VideoPlayerDialog(
            item = vid,
            onClose = { viewModel.closeVideo() }
        )
    }

    // PDF Viewer Dialog
    pdfItem?.let { pdf ->
        PdfViewerDialog(
            item = pdf,
            onClose = { viewModel.closePdf() }
        )
    }

    // ZIP Viewer Dialog
    zipItem?.let { zip ->
        ZipViewerDialog(
            item = zip,
            entries = zipEntries,
            isExtracting = isExtractingZip,
            onExtractAll = { viewModel.extractCurrentZip() },
            onClose = { viewModel.closeZip() }
        )
    }

    // APK Info Dialog
    apkItem?.let { apk ->
        ApkInfoDialog(
            item = apk,
            onClose = { viewModel.closeApk() }
        )
    }
}
