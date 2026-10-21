package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.model.FileCategory
import com.example.model.FileItem
import com.example.ui.theme.*

object FileIconHelper {
    fun getIconAndColor(item: FileItem): Pair<ImageVector, Color> {
        if (item.isDirectory) {
            return Pair(Icons.Default.Folder, FolderColor)
        }
        return when (item.category) {
            FileCategory.IMAGES -> Pair(Icons.Default.Image, ImageFileColor)
            FileCategory.VIDEOS -> Pair(Icons.Default.VideoFile, VideoFileColor)
            FileCategory.AUDIO -> Pair(Icons.Default.AudioFile, AudioFileColor)
            FileCategory.DOCUMENTS -> Pair(Icons.Default.Description, DocFileColor)
            FileCategory.ARCHIVES -> Pair(Icons.Default.Archive, ArchiveFileColor)
            FileCategory.APPS -> Pair(Icons.Default.Android, ApkFileColor)
            FileCategory.DOWNLOADS, null -> {
                if (item.isTextFile) {
                    Pair(Icons.Default.Description, DocFileColor)
                } else {
                    Pair(Icons.Default.InsertDriveFile, OtherFileColor)
                }
            }
        }
    }
}
