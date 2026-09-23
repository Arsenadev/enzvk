package com.enzvuck.icon

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.enzvuck.icon.domain.model.AppInfo
import com.enzvuck.icon.domain.model.CustomIcon
import com.enzvuck.icon.domain.model.IconPack
import com.enzvuck.icon.ui.screens.AppDetailDialog
import com.enzvuck.icon.ui.screens.AppsScreen
import com.enzvuck.icon.ui.screens.IconEditorScreen
import com.enzvuck.icon.ui.screens.IconPacksScreen
import com.enzvuck.icon.ui.screens.MyIconsScreen
import com.enzvuck.icon.ui.screens.SettingsScreen
import com.enzvuck.icon.ui.screens.SimulatedHomeScreen
import com.enzvuck.icon.ui.theme.AccentLime
import com.enzvuck.icon.ui.theme.BackgroundDark
import com.enzvuck.icon.ui.theme.SurfaceDark
import com.enzvuck.icon.ui.theme.SurfaceHighDark
import com.enzvuck.icon.ui.theme.TextMuted
import com.enzvuck.icon.ui.theme.TextOnAccent
import com.enzvuck.icon.ui.theme.TextPrimary
import com.enzvuck.icon.ui.theme.TextSecondary
import com.enzvuck.icon.ui.theme.BorderDark
import com.enzvuck.icon.ui.theme.EnzvuckTheme
import com.enzvuck.icon.ui.viewmodel.MainViewModel
import com.enzvuck.icon.ui.viewmodel.NavSection
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val customIcons by viewModel.customIcons.collectAsStateWithLifecycle()
            val iconPacks by viewModel.iconPacks.collectAsStateWithLifecycle()

            val isDark = when (uiState.themeMode) {
                "light" -> false
                "system" -> isSystemInDarkTheme()
                else -> true // default dark-first
            }

            EnzvuckTheme(darkTheme = isDark) {
                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()
                val context = LocalContext.current

                LaunchedEffect(Unit) {
                    viewModel.toastEvent.collect { message ->
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        snackbarHostState.showSnackbar(message)
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = BackgroundDark,
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    bottomBar = {
                        // Navigation bar only shown when editor is not open
                        if (!uiState.isEditorOpen) {
                            NavigationBar(
                                containerColor = SurfaceDark,
                                tonalElevation = 0.dp,
                                modifier = Modifier
                                    .border(1.dp, BorderDark)
                                    .testTag("main_navigation_bar")
                            ) {
                                NavigationBarItem(
                                    selected = uiState.currentNav == NavSection.APPS,
                                    onClick = { viewModel.setNavSection(NavSection.APPS) },
                                    icon = { Icon(Icons.Default.Apps, contentDescription = "Apps") },
                                    label = { Text("Apps", fontSize = 11.sp) },
                                    colors = navItemColors(),
                                    modifier = Modifier.testTag("nav_item_apps")
                                )
                                NavigationBarItem(
                                    selected = uiState.currentNav == NavSection.MY_ICONS,
                                    onClick = { viewModel.setNavSection(NavSection.MY_ICONS) },
                                    icon = { Icon(Icons.Default.Collections, contentDescription = "My Icons") },
                                    label = { Text("My Icons", fontSize = 11.sp) },
                                    colors = navItemColors(),
                                    modifier = Modifier.testTag("nav_item_my_icons")
                                )
                                NavigationBarItem(
                                    selected = uiState.currentNav == NavSection.PACKS,
                                    onClick = { viewModel.setNavSection(NavSection.PACKS) },
                                    icon = { Icon(Icons.Default.Archive, contentDescription = "Packs") },
                                    label = { Text("Packs", fontSize = 11.sp) },
                                    colors = navItemColors(),
                                    modifier = Modifier.testTag("nav_item_packs")
                                )
                                NavigationBarItem(
                                    selected = uiState.currentNav == NavSection.PREVIEW,
                                    onClick = { viewModel.setNavSection(NavSection.PREVIEW) },
                                    icon = { Icon(Icons.Default.PhoneAndroid, contentDescription = "Preview") },
                                    label = { Text("Preview", fontSize = 11.sp) },
                                    colors = navItemColors(),
                                    modifier = Modifier.testTag("nav_item_preview")
                                )
                                NavigationBarItem(
                                    selected = uiState.currentNav == NavSection.SETTINGS,
                                    onClick = { viewModel.setNavSection(NavSection.SETTINGS) },
                                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                                    label = { Text("Settings", fontSize = 11.sp) },
                                    colors = navItemColors(),
                                    modifier = Modifier.testTag("nav_item_settings")
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (uiState.currentNav) {
                            NavSection.APPS -> {
                                AppsScreen(
                                    apps = uiState.filteredApps,
                                    searchQuery = uiState.searchQuery,
                                    isLoading = uiState.isLoadingApps,
                                    onSearchChanged = { viewModel.onSearchQueryChanged(it) },
                                    onAppSelected = { viewModel.selectApp(it) },
                                    onEditAppIcon = { viewModel.openEditorForApp(it) },
                                    onRefresh = { viewModel.loadInstalledApps() }
                                )
                            }
                            NavSection.MY_ICONS -> {
                                MyIconsScreen(
                                    customIcons = customIcons,
                                    onEditIcon = { viewModel.openEditorForCustomIcon(it) },
                                    onExportIcon = { icon -> shareSingleIcon(context, icon) },
                                    onAddToPack = { viewModel.openAddToPack(it) },
                                    onDuplicateIcon = { viewModel.duplicateCustomIcon(it) },
                                    onDeleteIcon = { viewModel.deleteCustomIcon(it) }
                                )
                            }
                            NavSection.PACKS -> {
                                IconPacksScreen(
                                    iconPacks = iconPacks,
                                    allCustomIcons = customIcons,
                                    isCreatingPack = uiState.isCreatingPack,
                                    onOpenCreatePack = { viewModel.setIsCreatingPack(true) },
                                    onCloseCreatePack = { viewModel.setIsCreatingPack(false) },
                                    onCreatePackSubmit = { name, desc, author, ver, icons ->
                                        viewModel.createIconPack(name, desc, author, ver, icons)
                                    },
                                    onApplyPack = { pack ->
                                        viewModel.requestApplyPack(pack)
                                    },
                                    onExportThemeZip = { pack ->
                                        scope.launch {
                                            val items = viewModel.repository.getPackItems(pack.id)
                                            val zip = viewModel.repository.themePackager.createPortableThemeZip(pack, items)
                                            shareFile(context, zip, "application/zip", "Share Theme ZIP")
                                        }
                                    },
                                    onExportIconsZip = { pack ->
                                        scope.launch {
                                            val items = viewModel.repository.getPackItems(pack.id)
                                            val zip = viewModel.repository.themePackager.createIconsZip(items.map { it.second })
                                            shareFile(context, zip, "application/zip", "Share Icons ZIP")
                                        }
                                    },
                                    onExportPackProject = { pack ->
                                        scope.launch {
                                            val items = viewModel.repository.getPackItems(pack.id)
                                            val zip = viewModel.repository.themePackager.createIconPackProjectZip(pack, items)
                                            shareFile(context, zip, "application/zip", "Share Icon Pack Project")
                                        }
                                    },
                                    onExportApk = { pack ->
                                        scope.launch {
                                            val items = viewModel.repository.getPackItems(pack.id)
                                            val apk = viewModel.repository.themePackager.createIconPackApk(pack, items)
                                            shareFile(context, apk, "application/vnd.android.package-archive", "Share Icon Pack APK")
                                        }
                                    },
                                    onDeletePack = { viewModel.deleteIconPack(it) },
                                    unsupportedLauncherNoticePack = uiState.unsupportedLauncherNoticePack,
                                    onDismissLauncherNotice = { viewModel.dismissUnsupportedLauncherNotice() }
                                )
                            }
                            NavSection.PREVIEW -> {
                                SimulatedHomeScreen(
                                    installedApps = uiState.installedApps,
                                    customIcons = customIcons
                                )
                            }
                            NavSection.SETTINGS -> {
                                SettingsScreen(
                                    storageStats = uiState.storageStats,
                                    themeMode = uiState.themeMode,
                                    defaultShape = uiState.defaultShape,
                                    defaultBackground = uiState.defaultBackground,
                                    defaultExportFormat = uiState.defaultExportFormat,
                                    onThemeModeChanged = { viewModel.updateThemeMode(it) },
                                    onDefaultShapeChanged = { viewModel.updateDefaultShape(it) },
                                    onDefaultBackgroundChanged = { viewModel.updateDefaultBackground(it) },
                                    onDefaultExportFormatChanged = { viewModel.updateDefaultExportFormat(it) },
                                    onClearCache = { viewModel.clearCache() },
                                    onClearSavedIcons = { viewModel.clearAllSavedIcons() },
                                    onImportThemeStream = { stream -> viewModel.importThemeFromInputStream(stream) },
                                    importResult = uiState.importResult,
                                    onDismissImportResult = { viewModel.dismissImportResult() }
                                )
                            }
                        }

                        // App Detail Dialog
                        if (uiState.selectedApp != null && !uiState.isEditorOpen) {
                            val selected = uiState.selectedApp!!
                            AppDetailDialog(
                                app = selected,
                                onDismiss = { viewModel.selectApp(null) },
                                onEditIcon = {
                                    viewModel.openEditorForApp(selected)
                                },
                                onExportIcon = {
                                    val icon = customIcons.find { it.packageName == selected.packageName }
                                    if (icon != null) {
                                        shareSingleIcon(context, icon)
                                    }
                                },
                                onAddToPack = {
                                    val icon = customIcons.find { it.packageName == selected.packageName }
                                    viewModel.selectApp(null)
                                    if (icon != null) {
                                        viewModel.openAddToPack(icon)
                                    } else {
                                        viewModel.setIsCreatingPack(true)
                                    }
                                },
                                onResetIcon = {
                                    viewModel.resetAppIcon(selected.packageName)
                                }
                            )
                        }

                        // Add to Pack Dialog
                        if (uiState.isPackPickerOpen && uiState.iconForPackPicker != null) {
                            val targetIcon = uiState.iconForPackPicker!!
                            AlertDialog(
                                onDismissRequest = { viewModel.dismissPackPicker() },
                                title = {
                                    Text(
                                        text = "Add to Icon Pack",
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                },
                                text = {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Text(
                                            text = "Select an icon pack to add ${targetIcon.appName} into:",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextSecondary
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        LazyColumn(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(max = 240.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            items(iconPacks) { pack ->
                                                Card(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable {
                                                            viewModel.addIconToPack(pack.id, targetIcon)
                                                        },
                                                    colors = CardDefaults.cardColors(containerColor = SurfaceHighDark),
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(12.dp),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Column {
                                                            Text(
                                                                text = pack.name,
                                                                fontWeight = FontWeight.Bold,
                                                                color = TextPrimary
                                                            )
                                                            Text(
                                                                text = "${pack.iconCount} icons",
                                                                fontSize = 12.sp,
                                                                color = AccentLime
                                                            )
                                                        }
                                                        Icon(Icons.Default.Add, contentDescription = "Add", tint = AccentLime)
                                                    }
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))
                                        OutlinedButton(
                                            onClick = {
                                                viewModel.dismissPackPicker()
                                                viewModel.setIsCreatingPack(true)
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Create New Pack")
                                        }
                                    }
                                },
                                confirmButton = {
                                    TextButton(onClick = { viewModel.dismissPackPicker() }) {
                                        Text("Cancel", color = TextSecondary)
                                    }
                                },
                                containerColor = SurfaceDark
                            )
                        }

                        // Full Screen Icon Editor
                        AnimatedVisibility(
                            visible = uiState.isEditorOpen && uiState.selectedApp != null,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            IconEditorScreen(
                                app = uiState.selectedApp!!,
                                sourceBitmap = uiState.editorSourceBitmap,
                                initialConfig = uiState.editorConfig,
                                onClose = { viewModel.closeEditor() },
                                onSaveIcon = { renderedBitmap ->
                                    viewModel.saveEditorIcon(renderedBitmap)
                                },
                                onPickImageFromUri = { uri ->
                                    viewModel.setEditorSourceImageFromUri(uri)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun shareSingleIcon(context: android.content.Context, icon: CustomIcon) {
        val file = File(icon.iconPath)
        if (file.exists()) {
            val mimeType = if (icon.iconPath.endsWith(".png")) "image/png" else "image/webp"
            shareFile(context, file, mimeType, "Share Icon: ${icon.appName}")
        }
    }

    private fun shareFile(context: android.content.Context, file: File, mimeType: String, title: String) {
        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, title))
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot share file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    @Composable
    private fun navItemColors() = NavigationBarItemDefaults.colors(
        selectedIconColor = TextOnAccent,
        selectedTextColor = AccentLime,
        indicatorColor = AccentLime,
        unselectedIconColor = TextSecondary,
        unselectedTextColor = TextMuted
    )
}
