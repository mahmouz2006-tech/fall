package com.example.model

import java.util.Locale

data class StorageStats(
    val totalBytes: Long = 64L * 1024 * 1024 * 1024, // 64 GB default
    val usedBytes: Long = 28L * 1024 * 1024 * 1024,
    val categoryBytes: Map<FileCategory, Long> = emptyMap(),
    val categoryCounts: Map<FileCategory, Int> = emptyMap()
) {
    val freeBytes: Long
        get() = (totalBytes - usedBytes).coerceAtLeast(0L)

    val usedPercentage: Float
        get() = if (totalBytes > 0) (usedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f) else 0f

    companion object {
        fun formatBytes(bytes: Long): String {
            if (bytes <= 0) return "0 بايت"
            val units = arrayOf("بايت", "ك.ب", "م.ب", "ج.ب", "ت.ب")
            var s = bytes.toDouble()
            var unitIndex = 0
            while (s >= 1024 && unitIndex < units.size - 1) {
                s /= 1024
                unitIndex++
            }
            return String.format(Locale.US, "%.1f %s", s, units[unitIndex])
        }
    }
}
