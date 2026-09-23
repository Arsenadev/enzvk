package com.enzvuck.icon.storage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

/**
 * Manages clean on-device file storage for custom icons, previews, and exports.
 * All operations run on background threads.
 */
class StorageManager(private val context: Context) {

    private val iconsDir = File(context.filesDir, "icons").apply { if (!exists()) mkdirs() }
    private val sourcesDir = File(context.filesDir, "sources").apply { if (!exists()) mkdirs() }
    private val previewsDir = File(context.filesDir, "previews").apply { if (!exists()) mkdirs() }
    private val exportsDir = File(context.cacheDir, "exports").apply { if (!exists()) mkdirs() }

    /**
     * Saves a processed icon bitmap cleanly as WEBP or PNG.
     * Absolutely no watermark or external branding is added.
     */
    suspend fun saveCustomIcon(
        packageName: String,
        bitmap: Bitmap,
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.WEBP
    ): String = withContext(Dispatchers.IO) {
        val extension = if (format == Bitmap.CompressFormat.PNG) "png" else "webp"
        val fileName = "${packageName.replace('.', '_')}_icon.$extension"
        val file = File(iconsDir, fileName)
        FileOutputStream(file).use { out ->
            bitmap.compress(format, 100, out)
            out.flush()
        }
        file.absolutePath
    }

    /**
     * Saves a source image stream or URI to internal storage for re-editing.
     */
    suspend fun saveSourceImage(packageName: String, uri: Uri): String = withContext(Dispatchers.IO) {
        val fileName = "${packageName.replace('.', '_')}_source_${System.currentTimeMillis()}.png"
        val file = File(sourcesDir, fileName)
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
                output.flush()
            }
        }
        file.absolutePath
    }

    /**
     * Saves a source bitmap.
     */
    suspend fun saveSourceBitmap(packageName: String, bitmap: Bitmap): String = withContext(Dispatchers.IO) {
        val fileName = "${packageName.replace('.', '_')}_source_${System.currentTimeMillis()}.png"
        val file = File(sourcesDir, fileName)
        FileOutputStream(file).use { output ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
            output.flush()
        }
        file.absolutePath
    }

    /**
     * Saves an icon pack preview bitmap.
     */
    suspend fun savePackPreview(packName: String, bitmap: Bitmap): String = withContext(Dispatchers.IO) {
        val fileName = "preview_${packName.replace(Regex("[^a-zA-Z0-9]"), "_").lowercase()}.webp"
        val file = File(previewsDir, fileName)
        FileOutputStream(file).use { output ->
            bitmap.compress(Bitmap.CompressFormat.WEBP, 95, output)
            output.flush()
        }
        file.absolutePath
    }

    /**
     * Loads a bitmap safely from internal storage path.
     */
    suspend fun loadBitmap(filePath: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            if (file.exists() && file.canRead()) {
                BitmapFactory.decodeFile(file.absolutePath)
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Creates a temporary file in exports directory for sharing/saving.
     */
    fun getExportFile(name: String): File {
        if (!exportsDir.exists()) exportsDir.mkdirs()
        return File(exportsDir, name)
    }

    /**
     * Deletes icon and source files for a given package name.
     */
    suspend fun deleteIconFiles(packageName: String) = withContext(Dispatchers.IO) {
        val prefix = packageName.replace('.', '_')
        iconsDir.listFiles { file -> file.name.startsWith(prefix) }?.forEach { it.delete() }
        sourcesDir.listFiles { file -> file.name.startsWith(prefix) }?.forEach { it.delete() }
    }

    /**
     * Clears cached exports.
     */
    suspend fun clearCache() = withContext(Dispatchers.IO) {
        context.cacheDir.deleteRecursively()
        exportsDir.mkdirs()
    }

    /**
     * Clears all saved icons and sources.
     */
    suspend fun clearAllIcons() = withContext(Dispatchers.IO) {
        iconsDir.listFiles()?.forEach { it.delete() }
        sourcesDir.listFiles()?.forEach { it.delete() }
        previewsDir.listFiles()?.forEach { it.delete() }
    }

    /**
     * Calculates storage used by icons, sources, and cache.
     */
    suspend fun getStorageStats(): StorageStats = withContext(Dispatchers.IO) {
        val iconsBytes = calculateDirSize(iconsDir) + calculateDirSize(sourcesDir) + calculateDirSize(previewsDir)
        val cacheBytes = calculateDirSize(context.cacheDir)
        val iconCount = (iconsDir.listFiles()?.size ?: 0)
        StorageStats(
            iconsSizeBytes = iconsBytes,
            cacheSizeBytes = cacheBytes,
            savedIconsCount = iconCount
        )
    }

    private fun calculateDirSize(dir: File): Long {
        if (!dir.exists()) return 0L
        var bytes = 0L
        dir.listFiles()?.forEach { file ->
            bytes += if (file.isDirectory) calculateDirSize(file) else file.length()
        }
        return bytes
    }

    data class StorageStats(
        val iconsSizeBytes: Long,
        val cacheSizeBytes: Long,
        val savedIconsCount: Int
    ) {
        val formattedIconsSize: String get() = formatBytes(iconsSizeBytes)
        val formattedCacheSize: String get() = formatBytes(cacheSizeBytes)

        private fun formatBytes(bytes: Long): String {
            if (bytes < 1024) return "$bytes B"
            val kb = bytes / 1024.0
            if (kb < 1024) return String.format("%.1f KB", kb)
            val mb = kb / 1024.0
            return String.format("%.1f MB", mb)
        }
    }
}
