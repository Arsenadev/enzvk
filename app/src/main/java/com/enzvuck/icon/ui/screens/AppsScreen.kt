package com.enzvuck.icon.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.enzvuck.icon.domain.model.AppInfo
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
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppsScreen(
    apps: List<AppInfo>,
    searchQuery: String,
    isLoading: Boolean,
    onSearchChanged: (String) -> Unit,
    onAppSelected: (AppInfo) -> Unit,
    onEditAppIcon: (AppInfo) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    var filterType by remember { mutableStateOf("all") } // all, custom, original

    val displayApps = remember(apps, filterType) {
        when (filterType) {
            "custom" -> apps.filter { it.isCustomized }
            "original" -> apps.filter { !it.isCustomized }
            else -> apps
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // App Header
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "enzvuck icon",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.5).sp
                    ),
                    color = TextPrimary
                )
                Text(
                    text = "customise your icons.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AccentLime,
                    fontWeight = FontWeight.Medium
                )
            }

            IconButton(
                onClick = onRefresh,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceVariantDark)
                    .border(1.5.dp, BorderDark, RoundedCornerShape(12.dp))
                    .testTag("refresh_apps_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh installed apps",
                    tint = TextPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChanged,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_apps_input"),
            placeholder = { Text("Search apps or packages...", color = TextMuted) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = TextSecondary)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChanged("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear search", tint = TextSecondary)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceDark,
                unfocusedContainerColor = SurfaceDark,
                focusedBorderColor = AccentLime,
                unfocusedBorderColor = BorderDark,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Filter chips: All / Custom / Original
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = filterType == "all",
                onClick = { filterType = "all" },
                label = { Text("All (${apps.size})") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AccentLime,
                    selectedLabelColor = TextOnAccent,
                    containerColor = SurfaceDark,
                    labelColor = TextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = filterType == "all",
                    borderColor = BorderDark,
                    selectedBorderColor = AccentLime
                ),
                shape = RoundedCornerShape(10.dp)
            )

            val customCount = apps.count { it.isCustomized }
            FilterChip(
                selected = filterType == "custom",
                onClick = { filterType = "custom" },
                label = { Text("Custom ($customCount)") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AccentLime,
                    selectedLabelColor = TextOnAccent,
                    containerColor = SurfaceDark,
                    labelColor = TextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = filterType == "custom",
                    borderColor = BorderDark,
                    selectedBorderColor = AccentLime
                ),
                shape = RoundedCornerShape(10.dp)
            )

            val originalCount = apps.count { !it.isCustomized }
            FilterChip(
                selected = filterType == "original",
                onClick = { filterType = "original" },
                label = { Text("Original ($originalCount)") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AccentLime,
                    selectedLabelColor = TextOnAccent,
                    containerColor = SurfaceDark,
                    labelColor = TextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = filterType == "original",
                    borderColor = BorderDark,
                    selectedBorderColor = AccentLime
                ),
                shape = RoundedCornerShape(10.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = AccentLime)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Detecting launchable apps...", color = TextSecondary)
                }
            }
        } else if (displayApps.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (searchQuery.isNotBlank()) "No apps found matching '$searchQuery'" else "No applications detected",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(displayApps, key = { it.packageName }) { app ->
                    AppListItem(
                        app = app,
                        onAppClick = { onAppSelected(app) },
                        onEditClick = { onEditAppIcon(app) }
                    )
                }
            }
        }
    }
}

@Composable
fun AppListItem(
    app: AppInfo,
    onAppClick: () -> Unit,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, BorderDark, RoundedCornerShape(16.dp))
            .clickable { onAppClick() }
            .testTag("app_item_${app.packageName}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Icon preview (custom if available, else original system drawable)
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceHighDark)
                    .border(1.dp, if (app.isCustomized) AccentLime else BorderDark, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (app.customIconPath != null && File(app.customIconPath).exists()) {
                    AsyncImage(
                        model = File(app.customIconPath),
                        contentDescription = "${app.appName} custom icon",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Load original app icon
                    AsyncImage(
                        model = "android.resource://system/icon/${app.packageName}",
                        contentDescription = "${app.appName} original icon",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(6.dp),
                        fallback = null
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // App Label & Package
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = app.appName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // Status badge
                    StatusBadge(isCustom = app.isCustomized)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Action: Edit Icon button
            Button(
                onClick = onEditClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (app.isCustomized) SurfaceVariantDark else AccentLime,
                    contentColor = if (app.isCustomized) AccentLime else TextOnAccent
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                modifier = Modifier.testTag("edit_button_${app.packageName}")
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Edit",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
fun StatusBadge(isCustom: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isCustom) AccentLime.copy(alpha = 0.2f) else SurfaceVariantDark)
            .border(0.5.dp, if (isCustom) AccentLime else BorderDark, RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = if (isCustom) "Custom" else "Original",
            color = if (isCustom) AccentLime else TextMuted,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 10.sp)
        )
    }
}
