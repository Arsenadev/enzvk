package com.enzvuck.icon.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.enzvuck.icon.data.database.EnzvuckDatabase
import com.enzvuck.icon.data.repository.IconRepository
import com.enzvuck.icon.domain.model.AppInfo
import com.enzvuck.icon.domain.model.BackgroundType
import com.enzvuck.icon.domain.model.CustomIcon
import com.enzvuck.icon.domain.model.EditorConfiguration
import com.enzvuck.icon.domain.model.IconPack
import com.enzvuck.icon.domain.model.IconPackItem
import com.enzvuck.icon.domain.model.IconShape
import com.enzvuck.icon.export.ThemePackager
import com.enzvuck.icon.icons.InstalledAppsManager
import com.enzvuck.icon.storage.StorageManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.InputStream

enum class NavSection {
    APPS,
    MY_ICONS,
    PACKS,
    PREVIEW,
    SETTINGS
}

data class AppUiState(
    val currentNav: NavSection = NavSection.APPS,
    val searchQuery: String = "",
    val installedApps: List<AppInfo> = emptyList(),
    val filteredApps: List<AppInfo> = emptyList(),
    val isLoadingApps: Boolean = true,
    val selectedApp: AppInfo? = null,
    val activeEditorIcon: CustomIcon? = null,
    val isEditorOpen: Boolean = false,
    val editorSourceBitmap: Bitmap? = null,
    val editorConfig: EditorConfiguration = EditorConfiguration(),
    val isCreatingPack: Boolean = false,
    val isImportingTheme: Boolean = false,
    val importResult: ThemePackager.ThemeImportResult.Success? = null,
    val storageStats: StorageManager.StorageStats? = null,
    val themeMode: String = "dark", // "dark", "light", "system"
    val defaultShape: IconShape = IconShape.ROUNDED_SQUARE,
    val defaultBackground: BackgroundType = BackgroundType.TRANSPARENT,
    val defaultExportFormat: String = "WEBP" // "WEBP", "PNG"
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val repository: IconRepository

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    val customIcons: StateFlow<List<CustomIcon>>
    val iconPacks: StateFlow<List<IconPack>>

    init {
        val database = EnzvuckDatabase.getInstance(application)
        val storageManager = StorageManager(application)
        val appsManager = InstalledAppsManager(application)
        val themePackager = ThemePackager(application, storageManager)
        repository = IconRepository(application, database, storageManager, appsManager, themePackager)

        customIcons = repository.allCustomIcons
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        iconPacks = repository.allIconPacks
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        loadInstalledApps()
        refreshStorageStats()

        // Sync installed apps customization state whenever customIcons changes
        viewModelScope.launch {
            customIcons.collect { savedIcons ->
                val iconMap = savedIcons.associateBy { it.packageName }
                val updatedApps = _uiState.value.installedApps.map { app ->
                    val custom = iconMap[app.packageName]
                    app.copy(
                        isCustomized = custom != null,
                        customIconId = custom?.id,
                        customIconPath = custom?.iconPath
                    )
                }
                _uiState.value = _uiState.value.copy(
                    installedApps = updatedApps,
                    filteredApps = filterApps(updatedApps, _uiState.value.searchQuery)
                )
            }
        }
    }

    fun setNavSection(section: NavSection) {
        _uiState.value = _uiState.value.copy(currentNav = section)
        if (section == NavSection.SETTINGS) {
            refreshStorageStats()
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            filteredApps = filterApps(_uiState.value.installedApps, query)
        )
    }

    private fun filterApps(apps: List<AppInfo>, query: String): List<AppInfo> {
        if (query.isBlank()) return apps
        val q = query.trim().lowercase()
        return apps.filter {
            it.appName.lowercase().contains(q) || it.packageName.lowercase().contains(q)
        }
    }

    fun loadInstalledApps() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingApps = true)
            val apps = repository.appsManager.getLaunchableApps()
            val savedIcons = repository.storageManager // sync with current saved icons
            val iconMap = customIcons.value.associateBy { it.packageName }
            val mapped = apps.map { app ->
                val custom = iconMap[app.packageName]
                app.copy(
                    isCustomized = custom != null,
                    customIconId = custom?.id,
                    customIconPath = custom?.iconPath
                )
            }
            _uiState.value = _uiState.value.copy(
                installedApps = mapped,
                filteredApps = filterApps(mapped, _uiState.value.searchQuery),
                isLoadingApps = false
            )
        }
    }

    fun selectApp(app: AppInfo?) {
        _uiState.value = _uiState.value.copy(selectedApp = app)
    }

    /**
     * Opens the editor for an application or an existing custom icon.
     */
    fun openEditorForApp(app: AppInfo) {
        viewModelScope.launch {
            val existing = repository.getCustomIconByPackage(app.packageName)
            val originalBitmap = repository.appsManager.loadOriginalAppIconBitmap(app.packageName)

            val initialConfig = existing?.configuration ?: EditorConfiguration(
                shape = com.enzvuck.icon.domain.model.ShapeConfig(shape = _uiState.value.defaultShape),
                background = com.enzvuck.icon.domain.model.BackgroundConfig(type = _uiState.value.defaultBackground)
            )

            _uiState.value = _uiState.value.copy(
                selectedApp = app,
                activeEditorIcon = existing,
                editorConfig = initialConfig,
                editorSourceBitmap = originalBitmap,
                isEditorOpen = true
            )
        }
    }

    fun openEditorForCustomIcon(customIcon: CustomIcon) {
        viewModelScope.launch {
            val bitmap = repository.storageManager.loadBitmap(customIcon.iconPath)
                ?: repository.appsManager.loadOriginalAppIconBitmap(customIcon.packageName)

            val app = _uiState.value.installedApps.find { it.packageName == customIcon.packageName }
                ?: AppInfo(customIcon.packageName, customIcon.appName, "")

            _uiState.value = _uiState.value.copy(
                selectedApp = app,
                activeEditorIcon = customIcon,
                editorConfig = customIcon.configuration,
                editorSourceBitmap = bitmap,
                isEditorOpen = true
            )
        }
    }

    fun setEditorSourceImageFromUri(uri: Uri) {
        viewModelScope.launch {
            val context = getApplication<Application>()
            val stream = context.contentResolver.openInputStream(uri)
            val bitmap = android.graphics.BitmapFactory.decodeStream(stream)
            if (bitmap != null) {
                _uiState.value = _uiState.value.copy(editorSourceBitmap = bitmap)
                emitToast("Image loaded into editor")
            } else {
                emitToast("Couldn't load this image.")
            }
        }
    }

    fun updateEditorConfig(config: EditorConfiguration) {
        _uiState.value = _uiState.value.copy(editorConfig = config)
    }

    fun closeEditor() {
        _uiState.value = _uiState.value.copy(isEditorOpen = false, editorSourceBitmap = null)
    }

    /**
     * Saves the edited icon cleanly. No watermark, no E logo, no shortcuts!
     */
    fun saveEditorIcon(renderedBitmap: Bitmap) {
        val app = _uiState.value.selectedApp ?: return
        val config = _uiState.value.editorConfig
        viewModelScope.launch {
            try {
                repository.saveCustomIcon(
                    packageName = app.packageName,
                    appName = app.appName,
                    bitmap = renderedBitmap,
                    sourceImagePath = null,
                    configuration = config
                )
                closeEditor()
                emitToast("Custom icon saved for ${app.appName}")
                refreshStorageStats()
            } catch (e: Exception) {
                emitToast("Failed to save icon: ${e.message}")
            }
        }
    }

    fun resetAppIcon(packageName: String) {
        viewModelScope.launch {
            val existing = repository.getCustomIconByPackage(packageName)
            if (existing != null) {
                repository.deleteCustomIcon(existing.id, packageName)
                emitToast("Icon reset to original")
                refreshStorageStats()
            }
            selectApp(null)
        }
    }

    fun deleteCustomIcon(icon: CustomIcon) {
        viewModelScope.launch {
            repository.deleteCustomIcon(icon.id, icon.packageName)
            emitToast("Deleted ${icon.appName} custom icon")
            refreshStorageStats()
        }
    }

    fun duplicateCustomIcon(icon: CustomIcon) {
        viewModelScope.launch {
            try {
                repository.duplicateCustomIcon(icon)
                emitToast("Duplicated ${icon.appName}")
                refreshStorageStats()
            } catch (e: Exception) {
                emitToast("Failed to duplicate: ${e.message}")
            }
        }
    }

    fun createIconPack(
        name: String,
        description: String,
        author: String,
        version: String,
        selectedIcons: List<CustomIcon>
    ) {
        if (selectedIcons.isEmpty()) {
            emitToast("Please select at least one icon for the pack.")
            return
        }
        viewModelScope.launch {
            try {
                val packId = repository.createIconPack(
                    name = name.ifBlank { "enzvuck pack" },
                    description = description,
                    author = author.ifBlank { "enzvuck" },
                    version = version.ifBlank { "1.0.0" },
                    selectedIcons = selectedIcons,
                    installedApps = _uiState.value.installedApps
                )
                emitToast("Icon pack '$name' created (${selectedIcons.size} icons)")
                _uiState.value = _uiState.value.copy(isCreatingPack = false)
            } catch (e: Exception) {
                emitToast("Failed to create icon pack: ${e.message}")
            }
        }
    }

    fun deleteIconPack(packId: Long) {
        viewModelScope.launch {
            repository.deletePack(packId)
            emitToast("Icon pack deleted")
        }
    }

    fun setIsCreatingPack(isOpen: Boolean) {
        _uiState.value = _uiState.value.copy(isCreatingPack = isOpen)
    }

    fun importThemeFromInputStream(inputStream: InputStream) {
        viewModelScope.launch {
            try {
                val result = repository.themePackager.importThemeZip(inputStream, _uiState.value.installedApps)
                when (result) {
                    is ThemePackager.ThemeImportResult.Success -> {
                        // Insert imported icons into database
                        for (entry in result.importedIcons) {
                            repository.saveCustomIcon(
                                packageName = entry.packageName,
                                appName = entry.appName,
                                bitmap = repository.storageManager.loadBitmap(entry.savedIconPath)
                                    ?: continue,
                                sourceImagePath = null,
                                configuration = EditorConfiguration()
                            )
                        }

                        // Also create an Icon Pack from imported icons
                        if (result.importedIcons.isNotEmpty()) {
                            val savedIconsList = customIcons.value.filter { icon ->
                                result.importedIcons.any { it.packageName == icon.packageName }
                            }
                            if (savedIconsList.isNotEmpty()) {
                                repository.createIconPack(
                                    name = result.metadata.name,
                                    description = result.metadata.description,
                                    author = result.metadata.author,
                                    version = "1.0.0",
                                    selectedIcons = savedIconsList,
                                    installedApps = _uiState.value.installedApps
                                )
                            }
                        }

                        _uiState.value = _uiState.value.copy(importResult = result, isImportingTheme = true)
                        emitToast("Imported ${result.totalImported} icons (${result.matchedCount} apps matched)")
                        refreshStorageStats()
                    }
                    is ThemePackager.ThemeImportResult.Error -> {
                        emitToast(result.message)
                    }
                }
            } catch (e: Exception) {
                emitToast("Theme metadata is invalid.")
            }
        }
    }

    fun dismissImportResult() {
        _uiState.value = _uiState.value.copy(isImportingTheme = false, importResult = null)
    }

    fun clearCache() {
        viewModelScope.launch {
            repository.storageManager.clearCache()
            refreshStorageStats()
            emitToast("Cache cleared")
        }
    }

    fun clearAllSavedIcons() {
        viewModelScope.launch {
            repository.clearAllData()
            loadInstalledApps()
            refreshStorageStats()
            emitToast("All saved icons and packs cleared")
        }
    }

    fun updateThemeMode(mode: String) {
        _uiState.value = _uiState.value.copy(themeMode = mode)
    }

    fun updateDefaultShape(shape: IconShape) {
        _uiState.value = _uiState.value.copy(defaultShape = shape)
    }

    fun updateDefaultBackground(type: BackgroundType) {
        _uiState.value = _uiState.value.copy(defaultBackground = type)
    }

    fun updateDefaultExportFormat(format: String) {
        _uiState.value = _uiState.value.copy(defaultExportFormat = format)
    }

    private fun refreshStorageStats() {
        viewModelScope.launch {
            val stats = repository.storageManager.getStorageStats()
            _uiState.value = _uiState.value.copy(storageStats = stats)
        }
    }

    fun emitToast(message: String) {
        viewModelScope.launch {
            _toastEvent.emit(message)
        }
    }
}
