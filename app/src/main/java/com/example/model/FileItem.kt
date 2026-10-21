package com.example.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class FileItem(
    val path: String,
    val name: String,
    val size: Long = 0L,
    val isDirectory: Boolean = false,
    val lastModified: Long = 0L,
    val extension: String = "",
    val itemsCount: Int = 0,
    val isFavorite: Boolean = false,
    val isInTrash: Boolean = false,
    val originalPath: String? = null
) {
    val category: FileCategory?
        get() = if (isDirectory) null else FileCategory.fromExtension(extension)

    val formattedSize: String
        get() {
            if (isDirectory) {
                return "$itemsCount عنصر"
            }
            if (size <= 0) return "0 بايت"
            val units = arrayOf("بايت", "كيلوبايت", "ميجابايت", "جيجابايت", "تيرابايت")
            var s = size.toDouble()
            var unitIndex = 0
            while (s >= 1024 && unitIndex < units.size - 1) {
                s /= 1024
                unitIndex++
            }
            return String.format(Locale.US, "%.1f %s", s, units[unitIndex])
        }

    val formattedDate: String
        get() {
            if (lastModified <= 0) return ""
            val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
            return sdf.format(Date(lastModified))
        }

    val isTextFile: Boolean
        get() = !isDirectory && extension.lowercase() in setOf("txt", "md", "json", "xml", "log", "csv", "html", "kt", "java", "css", "js", "py", "c", "cpp", "sh", "yaml", "yml", "ini", "conf", "sql", "properties", "gradle", "kts")

    val isImageFile: Boolean
        get() = !isDirectory && extension.lowercase() in setOf("jpg", "jpeg", "png", "webp", "gif", "bmp", "svg", "heic")

    val isAudioFile: Boolean
        get() = !isDirectory && extension.lowercase() in setOf("mp3", "wav", "m4a", "flac", "aac", "ogg", "wma", "opus", "amr")

    val isVideoFile: Boolean
        get() = !isDirectory && extension.lowercase() in setOf("mp4", "mkv", "mov", "avi", "webm", "flv", "3gp", "wmv", "m4v")

    val isPdfFile: Boolean
        get() = !isDirectory && extension.lowercase() == "pdf"

    val isZipFile: Boolean
        get() = !isDirectory && extension.lowercase() in setOf("zip", "jar")

    val isApkFile: Boolean
        get() = !isDirectory && extension.lowercase() in setOf("apk", "xapk")
}
