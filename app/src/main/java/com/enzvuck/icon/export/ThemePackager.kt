package com.enzvuck.icon.export

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.enzvuck.icon.domain.model.AppInfo
import com.enzvuck.icon.domain.model.CustomIcon
import com.enzvuck.icon.domain.model.IconPack
import com.enzvuck.icon.domain.model.IconPackItem
import com.enzvuck.icon.domain.model.ThemePackageMetadata
import com.enzvuck.icon.storage.StorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Handles packaging, exporting, and importing of portable theme packages and icon ZIPs.
 */
class ThemePackager(
    private val context: Context,
    private val storageManager: StorageManager
) {

    /**
     * Builds a Portable Theme ZIP according to Section 30 specification:
     * <pack-name>/
     * ├── theme.json
     * ├── preview.webp
     * ├── icons/
     * │   ├── spotify.webp
     * │   └── ...
     * └── metadata/
     *     ├── editor.json
     *     └── appfilter.xml
     */
    suspend fun createPortableThemeZip(
        pack: IconPack,
        items: List<Pair<IconPackItem, CustomIcon>>
    ): File = withContext(Dispatchers.IO) {
        val sanitizedName = pack.name.replace(Regex("[^a-zA-Z0-9_-]"), "_").lowercase()
        val zipFile = storageManager.getExportFile("$sanitizedName-theme.zip")

        ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
            // 1. theme.json
            val themeMeta = ThemePackageMetadata(
                format = "enzvuck-theme",
                version = 1,
                name = pack.name,
                description = pack.description,
                author = pack.author,
                iconCount = items.size,
                preview = "preview.webp"
            )
            zos.putNextEntry(ZipEntry("theme.json"))
            zos.write(themeMeta.toJsonObject().toString(2).toByteArray(Charsets.UTF_8))
            zos.closeEntry()

            // 2. preview.webp
            val previewBytes = generatePackPreviewBytes(pack, items)
            zos.putNextEntry(ZipEntry("preview.webp"))
            zos.write(previewBytes)
            zos.closeEntry()

            // 3. icons/<drawableName>.webp
            for ((item, customIcon) in items) {
                val iconFile = File(customIcon.iconPath)
                if (iconFile.exists()) {
                    zos.putNextEntry(ZipEntry("icons/${item.drawableName}.webp"))
                    iconFile.inputStream().use { it.copyTo(zos) }
                    zos.closeEntry()
                }
            }

            // 4. metadata/appfilter.xml
            val appFilterXml = AppFilterGenerator.generate(items.map { it.first })
            zos.putNextEntry(ZipEntry("metadata/appfilter.xml"))
            zos.write(appFilterXml.toByteArray(Charsets.UTF_8))
            zos.closeEntry()

            // 5. metadata/editor.json
            val editorJson = JSONObject()
            val configsArray = JSONArray()
            for ((item, customIcon) in items) {
                val itemObj = JSONObject().apply {
                    put("packageName", item.packageName)
                    put("appName", item.appName)
                    put("drawableName", item.drawableName)
                    put("launcherActivity", item.launcherActivity)
                    put("configuration", JSONObject(customIcon.configuration.toJsonString()))
                }
                configsArray.put(itemObj)
            }
            editorJson.put("icons", configsArray)
            zos.putNextEntry(ZipEntry("metadata/editor.json"))
            zos.write(editorJson.toString(2).toByteArray(Charsets.UTF_8))
            zos.closeEntry()
        }

        zipFile
    }

    /**
     * Builds a clean Icons ZIP according to Section 31:
     * enzvuck-icons/
     * ├── spotify.webp
     * ├── chrome.webp
     * └── metadata.json
     */
    suspend fun createIconsZip(
        icons: List<CustomIcon>
    ): File = withContext(Dispatchers.IO) {
        val zipFile = storageManager.getExportFile("enzvuck-icons.zip")

        ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
            val metaArray = JSONArray()

            for (customIcon in icons) {
                val iconFile = File(customIcon.iconPath)
                if (iconFile.exists()) {
                    val drawableName = ResourceNameNormalizer.normalize(customIcon.packageName, customIcon.appName)
                    val filename = "$drawableName.webp"
                    zos.putNextEntry(ZipEntry("enzvuck-icons/$filename"))
                    iconFile.inputStream().use { it.copyTo(zos) }
                    zos.closeEntry()

                    val entryMeta = JSONObject().apply {
                        put("packageName", customIcon.packageName)
                        put("appName", customIcon.appName)
                        put("file", filename)
                    }
                    metaArray.put(entryMeta)
                }
            }

            val metaJson = JSONObject().apply {
                put("generatedBy", "enzvuck icon")
                put("iconCount", metaArray.length())
                put("icons", metaArray)
            }
            zos.putNextEntry(ZipEntry("enzvuck-icons/metadata.json"))
            zos.write(metaJson.toString(2).toByteArray(Charsets.UTF_8))
            zos.closeEntry()
        }

        zipFile
    }

    /**
     * Exports a buildable Android Icon Pack project package.
     * Contains standard launcher structure: AndroidManifest.xml, res/xml/appfilter.xml, res/drawable-nodpi/
     */
    suspend fun createIconPackProjectZip(
        pack: IconPack,
        items: List<Pair<IconPackItem, CustomIcon>>
    ): File = withContext(Dispatchers.IO) {
        val sanitizedName = pack.name.replace(Regex("[^a-zA-Z0-9_-]"), "_").lowercase()
        val zipFile = storageManager.getExportFile("$sanitizedName-iconpack-project.zip")

        ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
            // AndroidManifest.xml
            val manifest = """
                <?xml version="1.0" encoding="utf-8"?>
                <manifest xmlns:android="http://schemas.android.com/apk/res/android"
                    package="com.enzvuck.iconpack.${sanitizedName}">
                    <application
                        android:icon="@mipmap/ic_launcher"
                        android:label="${pack.name}">
                        <activity
                            android:name=".MainActivity"
                            android:exported="true">
                            <intent-filter>
                                <action android:name="android.intent.action.MAIN" />
                                <category android:name="android.intent.category.LAUNCHER" />
                            </intent-filter>
                            <intent-filter>
                                <action android:name="org.adw.launcher.THEMES" />
                                <category android:name="android.intent.category.DEFAULT" />
                            </intent-filter>
                            <intent-filter>
                                <action android:name="com.novalauncher.THEME" />
                                <category android:name="android.intent.category.DEFAULT" />
                            </intent-filter>
                        </activity>
                    </application>
                </manifest>
            """.trimIndent()
            zos.putNextEntry(ZipEntry("app/src/main/AndroidManifest.xml"))
            zos.write(manifest.toByteArray(Charsets.UTF_8))
            zos.closeEntry()

            // appfilter.xml
            val appFilterXml = AppFilterGenerator.generate(items.map { it.first })
            zos.putNextEntry(ZipEntry("app/src/main/res/xml/appfilter.xml"))
            zos.write(appFilterXml.toByteArray(Charsets.UTF_8))
            zos.closeEntry()

            // Drawables
            for ((item, customIcon) in items) {
                val iconFile = File(customIcon.iconPath)
                if (iconFile.exists()) {
                    zos.putNextEntry(ZipEntry("app/src/main/res/drawable-nodpi/${item.drawableName}.webp"))
                    iconFile.inputStream().use { it.copyTo(zos) }
                    zos.closeEntry()
                }
            }
        }

        zipFile
    }

    /**
     * Imports a Theme ZIP file.
     * Validates format, version, metadata, and compares against installed applications.
     */
    suspend fun importThemeZip(
        zipInputStream: InputStream,
        installedApps: List<AppInfo>
    ): ThemeImportResult = withContext(Dispatchers.IO) {
        var themeMetadata: ThemePackageMetadata? = null
        val iconFiles = mutableMapOf<String, ByteArray>()
        var appFilterXml: String? = null
        var editorJsonStr: String? = null

        val zis = ZipInputStream(zipInputStream)
        var entry: ZipEntry? = zis.nextEntry
        while (entry != null) {
            val name = entry.name.trimStart('/')
            val bytes = zis.readBytes()

            when {
                name == "theme.json" || name.endsWith("/theme.json") -> {
                    try {
                        val json = JSONObject(String(bytes, Charsets.UTF_8))
                        themeMetadata = ThemePackageMetadata.fromJsonObject(json)
                    } catch (_: Exception) {}
                }
                name == "metadata/appfilter.xml" || name.endsWith("/appfilter.xml") -> {
                    appFilterXml = String(bytes, Charsets.UTF_8)
                }
                name == "metadata/editor.json" || name.endsWith("/editor.json") -> {
                    editorJsonStr = String(bytes, Charsets.UTF_8)
                }
                name.startsWith("icons/") && (name.endsWith(".webp") || name.endsWith(".png")) -> {
                    val fileName = File(name).name
                    iconFiles[fileName] = bytes
                }
            }
            zis.closeEntry()
            entry = zis.nextEntry
        }

        if (themeMetadata == null) {
            return@withContext ThemeImportResult.Error("Theme metadata (theme.json) is invalid or missing.")
        }

        if (themeMetadata.format != "enzvuck-theme") {
            return@withContext ThemeImportResult.Error("Unsupported theme format: ${themeMetadata.format}")
        }

        // Parse mappings
        val parsedEntries = if (appFilterXml != null) {
            AppFilterGenerator.parse(appFilterXml)
        } else {
            emptyList()
        }

        val installedPackageSet = installedApps.map { it.packageName }.toSet()
        val installedAppMap = installedApps.associateBy { it.packageName }

        var matchedCount = 0
        var uninstalledCount = 0
        val importedIcons = mutableListOf<ImportedIconEntry>()

        for (parsed in parsedEntries) {
            val isMatched = installedPackageSet.contains(parsed.packageName)
            if (isMatched) matchedCount++ else uninstalledCount++

            val appLabel = installedAppMap[parsed.packageName]?.appName ?: parsed.packageName
            val targetFilename = "${parsed.drawableName}.webp"
            val iconBytes = iconFiles[targetFilename] ?: iconFiles["${parsed.drawableName}.png"]

            if (iconBytes != null) {
                // Save locally
                val bmp = BitmapFactory.decodeByteArray(iconBytes, 0, iconBytes.size)
                if (bmp != null) {
                    val savedPath = storageManager.saveCustomIcon(parsed.packageName, bmp)
                    importedIcons.add(
                        ImportedIconEntry(
                            packageName = parsed.packageName,
                            appName = appLabel,
                            launcherActivity = parsed.launcherActivity,
                            drawableName = parsed.drawableName,
                            savedIconPath = savedPath,
                            isMatched = isMatched
                        )
                    )
                }
            }
        }

        ThemeImportResult.Success(
            metadata = themeMetadata,
            totalImported = importedIcons.size,
            matchedCount = matchedCount,
            uninstalledCount = uninstalledCount,
            importedIcons = importedIcons
        )
    }

    private fun generatePackPreviewBytes(
        pack: IconPack,
        items: List<Pair<IconPackItem, CustomIcon>>
    ): ByteArray {
        val size = 512
        val previewBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(previewBitmap)
        canvas.drawColor(android.graphics.Color.parseColor("#151821"))

        // Draw a 2x2 or 3x3 grid of the first few icons as preview
        val sampleIcons = items.take(4).mapNotNull {
            val f = File(it.second.iconPath)
            if (f.exists()) BitmapFactory.decodeFile(f.absolutePath) else null
        }

        if (sampleIcons.isNotEmpty()) {
            val tileSize = 200
            val pad = 30
            sampleIcons.forEachIndexed { index, bmp ->
                val row = index / 2
                val col = index % 2
                val x = pad + col * (tileSize + pad)
                val y = pad + row * (tileSize + pad)
                val scaled = Bitmap.createScaledBitmap(bmp, tileSize, tileSize, true)
                canvas.drawBitmap(scaled, x.toFloat(), y.toFloat(), null)
            }
        }

        val baos = ByteArrayOutputStream()
        previewBitmap.compress(Bitmap.CompressFormat.WEBP, 90, baos)
        return baos.toByteArray()
    }

    data class ImportedIconEntry(
        val packageName: String,
        val appName: String,
        val launcherActivity: String,
        val drawableName: String,
        val savedIconPath: String,
        val isMatched: Boolean
    )

    sealed class ThemeImportResult {
        data class Success(
            val metadata: ThemePackageMetadata,
            val totalImported: Int,
            val matchedCount: Int,
            val uninstalledCount: Int,
            val importedIcons: List<ImportedIconEntry>
        ) : ThemeImportResult()

        data class Error(val message: String) : ThemeImportResult()
    }
}
