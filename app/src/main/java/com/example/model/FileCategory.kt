package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

enum class FileCategory(
    val title: String,
    val color: Color,
    val extensions: Set<String>
) {
    IMAGES(
        title = "الصور",
        color = ImageFileColor,
        extensions = setOf("jpg", "jpeg", "png", "webp", "gif", "bmp", "svg", "heic")
    ),
    VIDEOS(
        title = "الفيديوهات",
        color = VideoFileColor,
        extensions = setOf("mp4", "mkv", "mov", "avi", "webm", "flv", "3gp", "wmv")
    ),
    AUDIO(
        title = "الصوتيات",
        color = AudioFileColor,
        extensions = setOf("mp3", "wav", "m4a", "flac", "aac", "ogg", "wma")
    ),
    DOCUMENTS(
        title = "المستندات",
        color = DocFileColor,
        extensions = setOf("pdf", "doc", "docx", "txt", "xlsx", "xls", "pptx", "ppt", "csv", "json", "md", "html")
    ),
    DOWNLOADS(
        title = "التنزيلات",
        color = FolderAccent,
        extensions = emptySet() // matched by folder or download history
    ),
    ARCHIVES(
        title = "المضغوطات",
        color = ArchiveFileColor,
        extensions = setOf("zip", "rar", "7z", "tar", "gz", "bz2")
    ),
    APPS(
        title = "التطبيقات",
        color = ApkFileColor,
        extensions = setOf("apk", "xapk", "apks")
    );

    companion object {
        fun fromExtension(ext: String): FileCategory? {
            val lower = ext.lowercase()
            return entries.firstOrNull { it.extensions.contains(lower) }
        }
    }
}
