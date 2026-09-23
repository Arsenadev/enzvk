package com.enzvuck.icon.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.enzvuck.icon.domain.model.BackgroundType
import com.enzvuck.icon.domain.model.IconShape
import com.enzvuck.icon.export.ThemePackager
import com.enzvuck.icon.storage.StorageManager
import com.enzvuck.icon.ui.theme.AccentCoral
import com.enzvuck.icon.ui.theme.AccentCyan
import com.enzvuck.icon.ui.theme.AccentLime
import com.enzvuck.icon.ui.theme.BorderDark
import com.enzvuck.icon.ui.theme.SurfaceDark
import com.enzvuck.icon.ui.theme.SurfaceHighDark
import com.enzvuck.icon.ui.theme.SurfaceVariantDark
import com.enzvuck.icon.ui.theme.TextMuted
import com.enzvuck.icon.ui.theme.TextOnAccent
import com.enzvuck.icon.ui.theme.TextPrimary
import com.enzvuck.icon.ui.theme.TextSecondary
import java.io.InputStream

@Composable
fun SettingsScreen(
    storageStats: StorageManager.StorageStats?,
    themeMode: String,
    defaultShape: IconShape,
    defaultBackground: BackgroundType,
    defaultExportFormat: String,
    onThemeModeChanged: (String) -> Unit,
    onDefaultShapeChanged: (IconShape) -> Unit,
    onDefaultBackgroundChanged: (BackgroundType) -> Unit,
    onDefaultExportFormatChanged: (String) -> Unit,
    onClearCache: () -> Unit,
    onClearSavedIcons: () -> Unit,
    onImportThemeStream: (InputStream) -> Unit,
    importResult: ThemePackager.ThemeImportResult.Success?,
    onDismissImportResult: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showClearIconsConfirm by remember { mutableStateOf(false) }

    // File picker for theme ZIP / JSON import
    val themePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val stream = context.contentResolver.openInputStream(uri)
            if (stream != null) {
                onImportThemeStream(stream)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("settings_screen")
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
            color = TextPrimary
        )
        Text(
            text = "Preferences, storage & offline integrity",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Section: Import Theme
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(BorderDark)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, tint = AccentLime)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Import Theme Package", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Import a portable theme ZIP containing theme.json and launcher-compatible appfilter mappings.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { themePicker.launch("*/*") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentLime,
                        contentColor = TextOnAccent
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Choose Theme ZIP / JSON", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section: Appearance
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(BorderDark)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Appearance", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("dark" to "Dark", "light" to "Light", "system" to "System").forEach { (mode, label) ->
                        val isSel = themeMode == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSel) AccentLime else SurfaceVariantDark)
                                .border(1.dp, if (isSel) AccentLime else BorderDark, RoundedCornerShape(10.dp))
                                .clickable { onThemeModeChanged(mode) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(label, color = if (isSel) TextOnAccent else TextSecondary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section: Editor Defaults
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(BorderDark)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Editor Defaults", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)

                // Default Shape
                Column {
                    Text("Default Shape", color = TextSecondary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            IconShape.ROUNDED_SQUARE to "Rounded",
                            IconShape.SQUIRCLE to "Squircle",
                            IconShape.CIRCLE to "Circle",
                            IconShape.ORIGINAL to "Original"
                        ).forEach { (sh, label) ->
                            val isSel = defaultShape == sh
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) AccentLime else SurfaceVariantDark)
                                    .border(1.dp, if (isSel) AccentLime else BorderDark, RoundedCornerShape(8.dp))
                                    .clickable { onDefaultShapeChanged(sh) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(label, color = if (isSel) TextOnAccent else TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Default Export Format
                Column {
                    Text("Default Export Format", color = TextSecondary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("WEBP", "PNG").forEach { fmt ->
                            val isSel = defaultExportFormat == fmt
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) AccentLime else SurfaceVariantDark)
                                    .border(1.dp, if (isSel) AccentLime else BorderDark, RoundedCornerShape(8.dp))
                                    .clickable { onDefaultExportFormatChanged(fmt) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(fmt, color = if (isSel) TextOnAccent else TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section: Storage
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(BorderDark)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Storage Management", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                Spacer(modifier = Modifier.height(8.dp))

                if (storageStats != null) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Saved Icons Size:", color = TextSecondary, fontSize = 13.sp)
                        Text(storageStats.formattedIconsSize, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Cache Size:", color = TextSecondary, fontSize = 13.sp)
                        Text(storageStats.formattedCacheSize, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Saved Custom Icons:", color = TextSecondary, fontSize = 13.sp)
                        Text("${storageStats.savedIconsCount}", color = AccentLime, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onClearCache,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                    ) {
                        Icon(Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Clear Cache", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { showClearIconsConfirm = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentCoral),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                            brush = androidx.compose.ui.graphics.SolidColor(AccentCoral.copy(alpha = 0.5f))
                        )
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Clear Icons", fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section: About & Privacy
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(BorderDark)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "enzvuck icon",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = AccentLime
                )
                Text(
                    text = "customise your icons.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Version: 1.0.0", color = TextMuted, fontSize = 12.sp)
                Text("Developer: enzvuck", color = TextMuted, fontSize = 12.sp)

                Spacer(modifier = Modifier.height(14.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Privacy & Offline Integrity", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Your icons stay on your device. 100% offline-first. No cloud backends, no accounts, no analytics, no external tracking. Clean icons with zero watermark overlays.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }

    // Confirm Clear All Dialog
    if (showClearIconsConfirm) {
        AlertDialog(
            onDismissRequest = { showClearIconsConfirm = false },
            title = { Text("Clear All Saved Icons?", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Text("This will permanently delete all custom icons and packs from your device.", color = TextSecondary)
            },
            confirmButton = {
                Button(
                    onClick = {
                        showClearIconsConfirm = false
                        onClearSavedIcons()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCoral, contentColor = Color.White)
                ) {
                    Text("Delete Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearIconsConfirm = false }) {
                    Text("Cancel", color = TextPrimary)
                }
            },
            containerColor = SurfaceDark
        )
    }

    // Import Results Dialog (Section 32 & 33)
    if (importResult != null) {
        AlertDialog(
            onDismissRequest = onDismissImportResult,
            title = {
                Text("Theme Imported Successfully", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        text = importResult.metadata.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = AccentLime
                    )
                    Text(
                        text = "by ${importResult.metadata.author}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "${importResult.totalImported} icons imported\n${importResult.matchedCount} applications matched\n${importResult.uninstalledCount} applications not installed",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Imported icons are now available in My Icons and Icon Packs.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = onDismissImportResult,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentLime, contentColor = TextOnAccent)
                ) {
                    Text("Done", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = SurfaceDark
        )
    }
}
