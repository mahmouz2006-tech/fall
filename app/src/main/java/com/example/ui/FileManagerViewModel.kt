package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FileManagerRepository
import com.example.model.FileCategory
import com.example.model.FileItem
import com.example.model.StorageStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

enum class Screen {
    HOME,
    EXPLORER,
    CATEGORY,
    ANALYZER,
    TRASH
}

enum class SortMode(val title: String) {
    NAME_ASC("الاسم (أ-ي)"),
    NAME_DESC("الاسم (ي-أ)"),
    SIZE_DESC("الحجم (الأكبر أولاً)"),
    DATE_DESC("التاريخ (الأحدث أولاً)")
}

data class Clipboard(
    val files: List<FileItem> = emptyList(),
    val isCut: Boolean = false
)

data class TextEditorState(
    val file: FileItem? = null,
    val content: String = "",
    val isSaving: Boolean = false
)

class FileManagerViewModel(application: Application) : AndroidViewModel(application) {

    val repository = FileManagerRepository(application)

    private val _currentScreen = MutableStateFlow(Screen.HOME)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _currentDir = MutableStateFlow(repository.getRootDirectory())
    val currentDir: StateFlow<File> = _currentDir.asStateFlow()

    private val _files = MutableStateFlow<List<FileItem>>(emptyList())
    val files: StateFlow<List<FileItem>> = _files.asStateFlow()

    private val _recentFiles = MutableStateFlow<List<FileItem>>(emptyList())
    val recentFiles: StateFlow<List<FileItem>> = _recentFiles.asStateFlow()

    private val _favorites = MutableStateFlow<List<FileItem>>(emptyList())
    val favorites: StateFlow<List<FileItem>> = _favorites.asStateFlow()

    private val _trashFiles = MutableStateFlow<List<FileItem>>(emptyList())
    val trashFiles: StateFlow<List<FileItem>> = _trashFiles.asStateFlow()

    private val _largeFiles = MutableStateFlow<List<FileItem>>(emptyList())
    val largeFiles: StateFlow<List<FileItem>> = _largeFiles.asStateFlow()

    private val _storageStats = MutableStateFlow(StorageStats())
    val storageStats: StateFlow<StorageStats> = _storageStats.asStateFlow()

    private val _selectedCategory = MutableStateFlow<FileCategory?>(null)
    val selectedCategory: StateFlow<FileCategory?> = _selectedCategory.asStateFlow()

    private val _categoryFiles = MutableStateFlow<List<FileItem>>(emptyList())
    val categoryFiles: StateFlow<List<FileItem>> = _categoryFiles.asStateFlow()

    // View & Sort Settings
    private val _isGridView = MutableStateFlow(false)
    val isGridView: StateFlow<Boolean> = _isGridView.asStateFlow()

    private val _sortMode = MutableStateFlow(SortMode.NAME_ASC)
    val sortMode: StateFlow<SortMode> = _sortMode.asStateFlow()

    // Search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<FileItem>>(emptyList())
    val searchResults: StateFlow<List<FileItem>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    // Clipboard
    private val _clipboard = MutableStateFlow<Clipboard?>(null)
    val clipboard: StateFlow<Clipboard?> = _clipboard.asStateFlow()

    // Active Dialogs & Viewers
    private val _activeActionItem = MutableStateFlow<FileItem?>(null)
    val activeActionItem: StateFlow<FileItem?> = _activeActionItem.asStateFlow()

    private val _showCreateFolderDialog = MutableStateFlow(false)
    val showCreateFolderDialog: StateFlow<Boolean> = _showCreateFolderDialog.asStateFlow()

    private val _showCreateFileDialog = MutableStateFlow(false)
    val showCreateFileDialog: StateFlow<Boolean> = _showCreateFileDialog.asStateFlow()

    private val _showRenameDialog = MutableStateFlow(false)
    val showRenameDialog: StateFlow<Boolean> = _showRenameDialog.asStateFlow()

    private val _showDeleteDialog = MutableStateFlow(false)
    val showDeleteDialog: StateFlow<Boolean> = _showDeleteDialog.asStateFlow()

    private val _showDetailsDialog = MutableStateFlow(false)
    val showDetailsDialog: StateFlow<Boolean> = _showDetailsDialog.asStateFlow()

    private val _previewImageItem = MutableStateFlow<FileItem?>(null)
    val previewImageItem: StateFlow<FileItem?> = _previewImageItem.asStateFlow()

    private val _textEditorState = MutableStateFlow<TextEditorState?>(null)
    val textEditorState: StateFlow<TextEditorState?> = _textEditorState.asStateFlow()

    // Dedicated file players & viewers
    private val _audioItem = MutableStateFlow<FileItem?>(null)
    val audioItem: StateFlow<FileItem?> = _audioItem.asStateFlow()

    private val _videoItem = MutableStateFlow<FileItem?>(null)
    val videoItem: StateFlow<FileItem?> = _videoItem.asStateFlow()

    private val _pdfItem = MutableStateFlow<FileItem?>(null)
    val pdfItem: StateFlow<FileItem?> = _pdfItem.asStateFlow()

    private val _zipItem = MutableStateFlow<FileItem?>(null)
    val zipItem: StateFlow<FileItem?> = _zipItem.asStateFlow()

    private val _zipEntries = MutableStateFlow<List<FileItem>>(emptyList())
    val zipEntries: StateFlow<List<FileItem>> = _zipEntries.asStateFlow()

    private val _isExtractingZip = MutableStateFlow(false)
    val isExtractingZip: StateFlow<Boolean> = _isExtractingZip.asStateFlow()

    private val _apkItem = MutableStateFlow<FileItem?>(null)
    val apkItem: StateFlow<FileItem?> = _apkItem.asStateFlow()

    private val _systemOpenItem = MutableStateFlow<FileItem?>(null)
    val systemOpenItem: StateFlow<FileItem?> = _systemOpenItem.asStateFlow()

    // User message toast
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    private val directoryHistory = mutableListOf<File>()

    init {
        loadDirectory(_currentDir.value)
        refreshDashboard()
    }

    fun navigateToScreen(screen: Screen) {
        _currentScreen.value = screen
        if (screen == Screen.HOME) {
            refreshDashboard()
        } else if (screen == Screen.EXPLORER) {
            loadDirectory(_currentDir.value)
        } else if (screen == Screen.ANALYZER) {
            loadStorageAnalyzer()
        } else if (screen == Screen.TRASH) {
            loadTrash()
        }
    }

    fun refreshDashboard() {
        viewModelScope.launch {
            _storageStats.value = repository.getStorageStats()
            _recentFiles.value = repository.getRecentFiles()
            _favorites.value = repository.getFavorites()
        }
    }

    fun loadStorageAnalyzer() {
        viewModelScope.launch {
            _storageStats.value = repository.getStorageStats()
            _largeFiles.value = repository.getLargeFiles()
        }
    }

    fun loadTrash() {
        viewModelScope.launch {
            _trashFiles.value = repository.getTrashFiles()
        }
    }

    fun openCategory(category: FileCategory) {
        _selectedCategory.value = category
        _currentScreen.value = Screen.CATEGORY
        viewModelScope.launch {
            _categoryFiles.value = repository.getFilesByCategory(category)
        }
    }

    fun loadDirectory(dir: File) {
        viewModelScope.launch {
            _currentDir.value = dir
            val rawFiles = repository.getFiles(dir)
            _files.value = sortFiles(rawFiles, _sortMode.value)
        }
    }

    fun openItem(item: FileItem) {
        if (item.isDirectory) {
            directoryHistory.add(_currentDir.value)
            _currentScreen.value = Screen.EXPLORER
            loadDirectory(File(item.path))
        } else if (item.isAudioFile) {
            _audioItem.value = item
        } else if (item.isVideoFile) {
            _videoItem.value = item
        } else if (item.isPdfFile) {
            _pdfItem.value = item
        } else if (item.isZipFile) {
            openZip(item)
        } else if (item.isApkFile) {
            _apkItem.value = item
        } else if (item.isTextFile) {
            openTextEditor(item)
        } else if (item.isImageFile) {
            _previewImageItem.value = item
        } else {
            // Universal system launcher fallback
            _systemOpenItem.value = item
        }
    }

    fun openZip(item: FileItem) {
        _zipItem.value = item
        viewModelScope.launch {
            _zipEntries.value = repository.getZipEntries(File(item.path))
        }
    }

    fun extractCurrentZip() {
        val zip = _zipItem.value ?: return
        viewModelScope.launch {
            _isExtractingZip.value = true
            val targetDir = _currentDir.value
            val result = repository.extractZip(File(zip.path), targetDir)
            _isExtractingZip.value = false
            if (result.isSuccess) {
                _userMessage.value = "تم استخراج ${result.getOrNull()} ملف بنجاح في المجلد الحالي"
                _zipItem.value = null
                loadDirectory(_currentDir.value)
                refreshDashboard()
            } else {
                _userMessage.value = "فشل استخراج الأرشيف: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun closeAudio() { _audioItem.value = null }
    fun closeVideo() { _videoItem.value = null }
    fun closePdf() { _pdfItem.value = null }
    fun closeZip() { _zipItem.value = null; _zipEntries.value = emptyList() }
    fun closeApk() { _apkItem.value = null }
    fun closeSystemOpen() { _systemOpenItem.value = null }

    fun navigateUp(): Boolean {
        if (directoryHistory.isNotEmpty()) {
            val previous = directoryHistory.removeAt(directoryHistory.size - 1)
            loadDirectory(previous)
            return true
        }
        val parent = _currentDir.value.parentFile
        val root = repository.getRootDirectory()
        if (parent != null && parent.exists() && _currentDir.value.absolutePath != root.absolutePath) {
            loadDirectory(parent)
            return true
        }
        return false
    }

    fun navigateToBreadcrumb(targetFile: File) {
        directoryHistory.add(_currentDir.value)
        loadDirectory(targetFile)
    }

    fun toggleViewMode() {
        _isGridView.value = !_isGridView.value
    }

    fun setSortMode(mode: SortMode) {
        _sortMode.value = mode
        _files.value = sortFiles(_files.value, mode)
    }

    private fun sortFiles(list: List<FileItem>, mode: SortMode): List<FileItem> {
        val dirs = list.filter { it.isDirectory }
        val nonDirs = list.filter { !it.isDirectory }

        val comparator: Comparator<FileItem> = when (mode) {
            SortMode.NAME_ASC -> compareBy { it.name.lowercase() }
            SortMode.NAME_DESC -> compareByDescending { it.name.lowercase() }
            SortMode.SIZE_DESC -> compareByDescending { it.size }
            SortMode.DATE_DESC -> compareByDescending { it.lastModified }
        }

        return dirs.sortedWith(comparator) + nonDirs.sortedWith(comparator)
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            _isSearching.value = false
        } else {
            _isSearching.value = true
            viewModelScope.launch {
                _searchResults.value = repository.searchFiles(query, _currentDir.value)
            }
        }
    }

    fun clearSearch() {
        _searchQuery.value = ""
        _searchResults.value = emptyList()
        _isSearching.value = false
    }

    // File Action Dialog Triggers
    fun selectItemForAction(item: FileItem?) {
        _activeActionItem.value = item
    }

    fun showCreateFolder(show: Boolean) {
        _showCreateFolderDialog.value = show
    }

    fun showCreateFile(show: Boolean) {
        _showCreateFileDialog.value = show
    }

    fun showRename(show: Boolean) {
        _showRenameDialog.value = show
    }

    fun showDelete(show: Boolean) {
        _showDeleteDialog.value = show
    }

    fun showDetails(show: Boolean) {
        _showDetailsDialog.value = show
    }

    fun closePreviewImage() {
        _previewImageItem.value = null
    }

    fun createFolder(name: String) {
        viewModelScope.launch {
            val result = repository.createFolder(_currentDir.value, name)
            if (result.isSuccess) {
                _userMessage.value = "تم إنشاء المجلد: $name"
                loadDirectory(_currentDir.value)
                refreshDashboard()
            } else {
                _userMessage.value = result.exceptionOrNull()?.message ?: "حدث خطأ أثناء إنشاء المجلد"
            }
            _showCreateFolderDialog.value = false
        }
    }

    fun createTextFile(name: String, content: String = "") {
        viewModelScope.launch {
            val fileName = if (!name.contains(".")) "$name.txt" else name
            val result = repository.createFile(_currentDir.value, fileName, content)
            if (result.isSuccess) {
                _userMessage.value = "تم إنشاء الملف: $fileName"
                loadDirectory(_currentDir.value)
                refreshDashboard()
            } else {
                _userMessage.value = result.exceptionOrNull()?.message ?: "حدث خطأ أثناء إنشاء الملف"
            }
            _showCreateFileDialog.value = false
        }
    }

    fun renameItem(item: FileItem, newName: String) {
        viewModelScope.launch {
            val file = File(item.path)
            val result = repository.rename(file, newName)
            if (result.isSuccess) {
                _userMessage.value = "تمت إعادة التسمية إلى: $newName"
                loadDirectory(_currentDir.value)
                refreshDashboard()
            } else {
                _userMessage.value = result.exceptionOrNull()?.message ?: "فشلت إعادة التسمية"
            }
            _showRenameDialog.value = false
            _activeActionItem.value = null
        }
    }

    fun deleteItem(item: FileItem, moveToTrash: Boolean = true) {
        viewModelScope.launch {
            val file = File(item.path)
            val result = repository.delete(file, moveToTrash)
            if (result.isSuccess) {
                _userMessage.value = if (moveToTrash) "تم نقل الملف إلى سلة المحذوفات" else "تم حذف الملف نهائياً"
                loadDirectory(_currentDir.value)
                refreshDashboard()
                loadTrash()
            } else {
                _userMessage.value = result.exceptionOrNull()?.message ?: "تعذر حذف الملف"
            }
            _showDeleteDialog.value = false
            _activeActionItem.value = null
        }
    }

    fun toggleFavorite(item: FileItem) {
        val newState = repository.toggleFavorite(item.path)
        _userMessage.value = if (newState) "تمت الإضافة للمفضلة" else "تمت الإزالة من المفضلة"
        loadDirectory(_currentDir.value)
        refreshDashboard()
    }

    fun copyToClipboard(item: FileItem, isCut: Boolean = false) {
        _clipboard.value = Clipboard(files = listOf(item), isCut = isCut)
        _userMessage.value = if (isCut) "تم قص: ${item.name}" else "تم نسخ: ${item.name}"
    }

    fun clearClipboard() {
        _clipboard.value = null
    }

    fun pasteClipboard() {
        val clip = _clipboard.value ?: return
        viewModelScope.launch {
            val sourceFiles = clip.files.map { File(it.path) }
            val result = repository.copyOrMove(sourceFiles, _currentDir.value, clip.isCut)
            if (result.isSuccess) {
                _userMessage.value = if (clip.isCut) "تم نقل العناصر بنجاح" else "تم نسخ العناصر بنجاح"
                _clipboard.value = null
                loadDirectory(_currentDir.value)
                refreshDashboard()
            } else {
                _userMessage.value = "فشلت عملية اللصق: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun restoreTrashItem(item: FileItem) {
        viewModelScope.launch {
            val result = repository.restoreFromTrash(item)
            if (result.isSuccess) {
                _userMessage.value = "تمت استعادة: ${item.name}"
                loadTrash()
                loadDirectory(_currentDir.value)
                refreshDashboard()
            } else {
                _userMessage.value = "تعذر استعادة الملف: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun emptyTrash() {
        viewModelScope.launch {
            val result = repository.emptyTrash()
            if (result.isSuccess) {
                _userMessage.value = "تم تفريغ سلة المحذوفات بالكامل"
                loadTrash()
                refreshDashboard()
            } else {
                _userMessage.value = "تعذر تفريغ السلة"
            }
        }
    }

    fun openTextEditor(item: FileItem) {
        viewModelScope.launch {
            val content = repository.readText(File(item.path))
            _textEditorState.value = TextEditorState(file = item, content = content)
        }
    }

    fun updateEditorContent(newContent: String) {
        _textEditorState.value = _textEditorState.value?.copy(content = newContent)
    }

    fun saveTextEditor() {
        val state = _textEditorState.value ?: return
        val fileItem = state.file ?: return
        viewModelScope.launch {
            _textEditorState.value = state.copy(isSaving = true)
            val success = repository.writeText(File(fileItem.path), state.content)
            _textEditorState.value = state.copy(isSaving = false)
            if (success) {
                _userMessage.value = "تم حفظ الملف بنجاح"
                loadDirectory(_currentDir.value)
            } else {
                _userMessage.value = "تعذر حفظ التعديلات"
            }
        }
    }

    fun closeTextEditor() {
        _textEditorState.value = null
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }
}
