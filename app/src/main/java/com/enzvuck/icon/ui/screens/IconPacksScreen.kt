package com.enzvuck.icon.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import com.enzvuck.icon.domain.model.CustomIcon
import com.enzvuck.icon.domain.model.IconPack
import com.enzvuck.icon.ui.theme.AccentCoral
import com.enzvuck.icon.ui.theme.AccentCyan
import com.enzvuck.icon.ui.theme.AccentIndigo
import com.enzvuck.icon.ui.theme.AccentLime
import com.enzvuck.icon.ui.theme.BorderDark
import com.enzvuck.icon.ui.theme.SurfaceDark
import com.enzvuck.icon.ui.theme.SurfaceHighDark
import com.enzvuck.icon.ui.theme.SurfaceVariantDark
import com.enzvuck.icon.ui.theme.TextMuted
import com.enzvuck.icon.ui.theme.TextOnAccent
import com.enzvuck.icon.ui.theme.TextPrimary
import com.enzvuck.icon.ui.theme.TextSecondary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun IconPacksScreen(
    iconPacks: List<IconPack>,
    allCustomIcons: List<CustomIcon>,
    isCreatingPack: Boolean,
    onOpenCreatePack: () -> Unit,
    onCloseCreatePack: () -> Unit,
    onCreatePackSubmit: (name: String, desc: String, author: String, ver: String, icons: List<CustomIcon>) -> Unit,
    onExportThemeZip: (IconPack) -> Unit,
    onExportIconsZip: (IconPack) -> Unit,
    onExportPackProject: (IconPack) -> Unit,
    onDeletePack: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var apkNoticePackName by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header Row with "+ Create Icon Pack"
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Icon Packs",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                    color = TextPrimary
                )
                Text(
                    text = "${iconPacks.size} packs generated",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            Button(
                onClick = onOpenCreatePack,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentLime,
                    contentColor = TextOnAccent
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("create_pack_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Create Pack", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (iconPacks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceDark)
                            .border(1.5.dp, BorderDark, RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Archive, contentDescription = null, tint = AccentLime, modifier = Modifier.size(36.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No icon packs yet",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Create an icon pack from your custom icons to export standard theme packages and launcher resources.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(iconPacks, key = { it.id }) { pack ->
                    IconPackItemCard(
                        pack = pack,
                        onExportTheme = { onExportThemeZip(pack) },
                        onExportIcons = { onExportIconsZip(pack) },
                        onExportProject = { onExportPackProject(pack) },
                        onExportApk = { apkNoticePackName = pack.name },
                        onDelete = { onDeletePack(pack.id) }
                    )
                }
            }
        }
    }

    // Create Pack Dialog Wizard
    if (isCreatingPack) {
        CreateIconPackDialog(
            customIcons = allCustomIcons,
            onDismiss = onCloseCreatePack,
            onSubmit = onCreatePackSubmit
        )
    }

    // APK notice dialog (Section 29 & 35)
    if (apkNoticePackName != null) {
        AlertDialog(
            onDismissRequest = { apkNoticePackName = null },
            title = {
                Text("APK Export Notice", fontWeight = FontWeight.Bold, color = TextPrimary)
            },
            text = {
                Column {
                    Text(
                        text = "APK generation is unavailable on this device.",
                        fontWeight = FontWeight.SemiBold,
                        color = AccentLime
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Android does not include on-device DEX / AAPT binary linkers. Never create a fake APK!\n\nUse 'Portable Theme ZIP' (usable in launchers like Nova/Smart/Lawnchair) or 'Export Icon Pack Project' to build a signed APK on your computer.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { apkNoticePackName = null }) {
                    Text("Understood", color = AccentLime, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = SurfaceDark
        )
    }
}

@Composable
fun IconPackItemCard(
    pack: IconPack,
    onExportTheme: () -> Unit,
    onExportIcons: () -> Unit,
    onExportProject: () -> Unit,
    onExportApk: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, BorderDark, RoundedCornerShape(20.dp))
            .testTag("icon_pack_card_${pack.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Title & Count row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = pack.name,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Text(
                        text = "${pack.iconCount} icons • by ${pack.author}",
                        style = MaterialTheme.typography.bodySmall,
                        color = AccentLime
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Pack", tint = AccentCoral)
                }
            }

            if (pack.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = pack.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Export Actions
            Text("Export Options", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onExportTheme,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = SurfaceVariantDark, contentColor = TextPrimary),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                        brush = androidx.compose.ui.graphics.SolidColor(BorderDark)
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.FolderZip, contentDescription = null, modifier = Modifier.size(16.dp), tint = AccentLime)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Theme ZIP", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onExportIcons,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = SurfaceVariantDark, contentColor = TextPrimary),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                        brush = androidx.compose.ui.graphics.SolidColor(BorderDark)
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Archive, contentDescription = null, modifier = Modifier.size(16.dp), tint = AccentCyan)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Icons ZIP", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onExportProject,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = SurfaceVariantDark, contentColor = TextPrimary),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                        brush = androidx.compose.ui.graphics.SolidColor(BorderDark)
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text("Pack Project", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onExportApk,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = SurfaceVariantDark, contentColor = TextPrimary),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                        brush = androidx.compose.ui.graphics.SolidColor(BorderDark)
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Android, contentDescription = null, modifier = Modifier.size(16.dp), tint = TextSecondary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export APK", fontSize = 12.sp)
                }
            }
        }
    }
}

/**
 * Multi-step wizard to create an Icon Pack:
 * Step 1: Select icons
 * Step 2: Pack Info
 */
@Composable
fun CreateIconPackDialog(
    customIcons: List<CustomIcon>,
    onDismiss: () -> Unit,
    onSubmit: (name: String, desc: String, author: String, ver: String, icons: List<CustomIcon>) -> Unit
) {
    var step by remember { mutableStateOf(1) }
    val selectedIcons = remember { mutableStateListOf<CustomIcon>().apply { addAll(customIcons) } }

    var packName by remember { mutableStateOf("enzvuck mono") }
    var description by remember { mutableStateOf("minimal monochrome icon pack") }
    var author by remember { mutableStateOf("enzvuck") }
    var version by remember { mutableStateOf("1.0.0") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, BorderDark, RoundedCornerShape(24.dp))
                .testTag("create_pack_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (step == 1) "1. Select Icons" else "2. Pack Info",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (step == 1) {
                    // Step 1: Icon selection
                    if (customIcons.isEmpty()) {
                        Text(
                            "You don't have any custom icons saved yet. Edit and save some icons first!",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${selectedIcons.size} of ${customIcons.size} selected",
                                color = AccentLime,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )

                            TextButton(
                                onClick = {
                                    if (selectedIcons.size == customIcons.size) {
                                        selectedIcons.clear()
                                    } else {
                                        selectedIcons.clear()
                                        selectedIcons.addAll(customIcons)
                                    }
                                }
                            ) {
                                Text(
                                    text = if (selectedIcons.size == customIcons.size) "Deselect All" else "Select All",
                                    color = TextPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp)
                        ) {
                            items(customIcons, key = { it.id }) { icon ->
                                val isChecked = selectedIcons.contains(icon)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (isChecked) selectedIcons.remove(icon) else selectedIcons.add(icon)
                                        }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { checked ->
                                            if (checked) selectedIcons.add(icon) else selectedIcons.remove(icon)
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = AccentLime,
                                            checkmarkColor = TextOnAccent
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = icon.appName,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "(${icon.packageName})",
                                        color = TextMuted,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { step = 2 },
                        enabled = selectedIcons.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentLime,
                            contentColor = TextOnAccent
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Next: Pack Information (${selectedIcons.size} icons)", fontWeight = FontWeight.Bold)
                    }
                } else {
                    // Step 2: Pack Info
                    OutlinedTextField(
                        value = packName,
                        onValueChange = { packName = it },
                        label = { Text("Pack Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = AccentLime,
                            unfocusedBorderColor = BorderDark
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = AccentLime,
                            unfocusedBorderColor = BorderDark
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = author,
                            onValueChange = { author = it },
                            label = { Text("Author") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = AccentLime,
                                unfocusedBorderColor = BorderDark
                            )
                        )

                        OutlinedTextField(
                            value = version,
                            onValueChange = { version = it },
                            label = { Text("Version") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = AccentLime,
                                unfocusedBorderColor = BorderDark
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = { step = 1 },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Back")
                        }

                        Button(
                            onClick = {
                                onSubmit(packName, description, author, version, selectedIcons)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentLime,
                                contentColor = TextOnAccent
                            )
                        ) {
                            Text("Build Pack", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
