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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.enzvuck.icon.domain.model.CustomIcon
import com.enzvuck.icon.ui.theme.AccentCoral
import com.enzvuck.icon.ui.theme.AccentLime
import com.enzvuck.icon.ui.theme.BorderDark
import com.enzvuck.icon.ui.theme.SurfaceDark
import com.enzvuck.icon.ui.theme.SurfaceHighDark
import com.enzvuck.icon.ui.theme.SurfaceVariantDark
import com.enzvuck.icon.ui.theme.TextMuted
import com.enzvuck.icon.ui.theme.TextPrimary
import com.enzvuck.icon.ui.theme.TextSecondary
import java.io.File

@Composable
fun MyIconsScreen(
    customIcons: List<CustomIcon>,
    onEditIcon: (CustomIcon) -> Unit,
    onExportIcon: (CustomIcon) -> Unit,
    onAddToPack: (CustomIcon) -> Unit,
    onDuplicateIcon: (CustomIcon) -> Unit,
    onDeleteIcon: (CustomIcon) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "My Icons",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                    color = TextPrimary
                )
                Text(
                    text = "${customIcons.size} saved custom icons",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (customIcons.isEmpty()) {
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
                        Text("✧", color = AccentLime, fontSize = 32.sp)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No custom icons yet",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Pick an installed app from the 'Apps' tab to customize your first icon.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 96.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(customIcons, key = { it.id }) { icon ->
                    CustomIconGridCard(
                        icon = icon,
                        onEdit = { onEditIcon(icon) },
                        onExport = { onExportIcon(icon) },
                        onAddToPack = { onAddToPack(icon) },
                        onDuplicate = { onDuplicateIcon(icon) },
                        onDelete = { onDeleteIcon(icon) }
                    )
                }
            }
        }
    }
}

@Composable
fun CustomIconGridCard(
    icon: CustomIcon,
    onEdit: () -> Unit,
    onExport: () -> Unit,
    onAddToPack: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, BorderDark, RoundedCornerShape(18.dp))
            .clickable { onEdit() }
            .testTag("custom_icon_card_${icon.packageName}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with more menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(AccentLime.copy(alpha = 0.2f))
                        .border(0.5.dp, AccentLime, RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        "Custom",
                        color = AccentLime,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = TextSecondary, modifier = Modifier.size(18.dp))
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier.background(SurfaceHighDark)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit Icon", color = TextPrimary) },
                            onClick = { menuExpanded = false; onEdit() },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = AccentLime) }
                        )
                        DropdownMenuItem(
                            text = { Text("Export", color = TextPrimary) },
                            onClick = { menuExpanded = false; onExport() },
                            leadingIcon = { Icon(Icons.Default.Share, contentDescription = null, tint = TextSecondary) }
                        )
                        DropdownMenuItem(
                            text = { Text("Add to Pack", color = TextPrimary) },
                            onClick = { menuExpanded = false; onAddToPack() },
                            leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, tint = TextSecondary) }
                        )
                        DropdownMenuItem(
                            text = { Text("Duplicate", color = TextPrimary) },
                            onClick = { menuExpanded = false; onDuplicate() },
                            leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null, tint = TextSecondary) }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = AccentCoral) },
                            onClick = { menuExpanded = false; onDelete() },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = AccentCoral) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Icon thumbnail
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(SurfaceHighDark)
                    .border(1.dp, BorderDark, RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = File(icon.iconPath),
                    contentDescription = "${icon.appName} custom icon",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title & package
            Text(
                text = icon.appName,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = icon.packageName,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = TextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
