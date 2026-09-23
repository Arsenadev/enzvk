package com.enzvuck.icon.ui.screens

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Filter
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.RoundedCorner
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.enzvuck.icon.domain.model.AdjustmentConfig
import com.enzvuck.icon.domain.model.AppInfo
import com.enzvuck.icon.domain.model.BackgroundConfig
import com.enzvuck.icon.domain.model.BackgroundType
import com.enzvuck.icon.domain.model.BorderConfig
import com.enzvuck.icon.domain.model.CropConfig
import com.enzvuck.icon.domain.model.EditorConfiguration
import com.enzvuck.icon.domain.model.FilterConfig
import com.enzvuck.icon.domain.model.IconFilter
import com.enzvuck.icon.domain.model.IconShape
import com.enzvuck.icon.domain.model.ShadowConfig
import com.enzvuck.icon.domain.model.ShapeConfig
import com.enzvuck.icon.domain.model.TransformConfig
import com.enzvuck.icon.editor.ImageProcessor
import com.enzvuck.icon.ui.theme.AccentCyan
import com.enzvuck.icon.ui.theme.AccentIndigo
import com.enzvuck.icon.ui.theme.AccentLime
import com.enzvuck.icon.ui.theme.BackgroundDark
import com.enzvuck.icon.ui.theme.BorderDark
import com.enzvuck.icon.ui.theme.BorderLight
import com.enzvuck.icon.ui.theme.SurfaceDark
import com.enzvuck.icon.ui.theme.SurfaceHighDark
import com.enzvuck.icon.ui.theme.SurfaceVariantDark
import com.enzvuck.icon.ui.theme.TextMuted
import com.enzvuck.icon.ui.theme.TextOnAccent
import com.enzvuck.icon.ui.theme.TextPrimary
import com.enzvuck.icon.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconEditorScreen(
    app: AppInfo,
    sourceBitmap: Bitmap?,
    initialConfig: EditorConfiguration,
    onClose: () -> Unit,
    onSaveIcon: (Bitmap) -> Unit,
    onPickImageFromUri: (Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    var config by remember(initialConfig) { mutableStateOf(initialConfig) }
    var selectedTab by remember { mutableIntStateOf(0) }

    // Android Photo Picker launcher (zero broad permissions needed)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onPickImageFromUri(uri)
        }
    }

    // Generic file picker fallback launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            onPickImageFromUri(uri)
        }
    }

    // Live rendered bitmap based on current sourceBitmap and config
    val renderedBitmap = remember(sourceBitmap, config) {
        if (sourceBitmap != null) {
            ImageProcessor.processIcon(sourceBitmap, config, targetSize = 512)
        } else {
            // Default placeholder fallback
            val blank = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(blank)
            canvas.drawColor(android.graphics.Color.DKGRAY)
            blank
        }
    }

    val tabs = listOf(
        "Shape",
        "Crop",
        "Transform",
        "Background",
        "Border",
        "Shadow",
        "Adjust",
        "Filter"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceDark)
                    .border(1.dp, BorderDark, RoundedCornerShape(12.dp))
                    .testTag("editor_back_button")
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Close editor",
                    tint = TextPrimary
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Edit Icon",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Text(
                    text = app.appName,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Button(
                onClick = { onSaveIcon(renderedBitmap) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentLime,
                    contentColor = TextOnAccent
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                modifier = Modifier.testTag("editor_save_icon_button")
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Save", fontWeight = FontWeight.Bold)
            }
        }

        // Preview Box Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(SurfaceDark)
                    .border(2.dp, BorderDark, RoundedCornerShape(28.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = renderedBitmap.asImageBitmap(),
                    contentDescription = "Live custom icon preview",
                    modifier = Modifier
                        .size(144.dp)
                        .testTag("editor_live_preview")
                )
            }
        }

        // Image Source Selection Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            OutlinedButton(
                onClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = SurfaceDark,
                    contentColor = TextPrimary
                ),
                border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                    brush = androidx.compose.ui.graphics.SolidColor(BorderDark)
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp), tint = AccentLime)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Gallery / Photos", fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedButton(
                onClick = {
                    filePickerLauncher.launch("image/*")
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = SurfaceDark,
                    contentColor = TextPrimary
                ),
                border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                    brush = androidx.compose.ui.graphics.SolidColor(BorderDark)
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("Files", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Tool Tab Row
        val scrollStateTabs = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollStateTabs)
                .background(SurfaceDark)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = selectedTab == index
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) AccentLime else SurfaceVariantDark)
                        .border(1.dp, if (isSelected) AccentLime else BorderDark, RoundedCornerShape(10.dp))
                        .clickable { selectedTab = index }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("tab_$title")
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isSelected) TextOnAccent else TextSecondary
                    )
                }
            }
        }

        // Tool Content Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            when (selectedTab) {
                0 -> ShapeTool(
                    config = config.shape,
                    onConfigChanged = { config = config.copy(shape = it) }
                )
                1 -> CropTool(
                    config = config.crop,
                    onConfigChanged = { config = config.copy(crop = it) }
                )
                2 -> TransformTool(
                    config = config.transform,
                    onConfigChanged = { config = config.copy(transform = it) }
                )
                3 -> BackgroundTool(
                    config = config.background,
                    onConfigChanged = { config = config.copy(background = it) }
                )
                4 -> BorderTool(
                    config = config.border,
                    onConfigChanged = { config = config.copy(border = it) }
                )
                5 -> ShadowTool(
                    config = config.shadow,
                    onConfigChanged = { config = config.copy(shadow = it) }
                )
                6 -> AdjustmentTool(
                    config = config.adjustments,
                    onConfigChanged = { config = config.copy(adjustments = it) }
                )
                7 -> FilterTool(
                    config = config.filter,
                    onConfigChanged = { config = config.copy(filter = it) }
                )
            }
        }
    }
}

/* ==========================================================
   Tool Composable Components
   ========================================================== */

@Composable
fun ShapeTool(
    config: ShapeConfig,
    onConfigChanged: (ShapeConfig) -> Unit
) {
    val shapes = listOf(
        IconShape.ORIGINAL to "Original",
        IconShape.ROUNDED_SQUARE to "Rounded",
        IconShape.SQUIRCLE to "Squircle",
        IconShape.CIRCLE to "Circle",
        IconShape.SQUARE to "Square",
        IconShape.HEXAGON to "Hexagon",
        IconShape.CUSTOM_RADIUS to "Custom"
    )

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Shape Preset", style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.Bold)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            shapes.forEach { (shape, name) ->
                val selected = config.shape == shape
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selected) AccentLime else SurfaceDark)
                        .border(1.dp, if (selected) AccentLime else BorderDark, RoundedCornerShape(12.dp))
                        .clickable { onConfigChanged(config.copy(shape = shape)) }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = name,
                        color = if (selected) TextOnAccent else TextSecondary,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 13.sp
                    )
                }
            }
        }

        if (config.shape == IconShape.CUSTOM_RADIUS || config.shape == IconShape.ROUNDED_SQUARE) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Corner Radius", color = TextSecondary, fontSize = 13.sp)
                Text("${(config.customRadius * 100).toInt()}%", color = AccentLime, fontWeight = FontWeight.Bold)
            }
            Slider(
                value = config.customRadius,
                onValueChange = { onConfigChanged(config.copy(shape = IconShape.CUSTOM_RADIUS, customRadius = it)) },
                valueRange = 0.0f..0.5f,
                colors = SliderDefaults.colors(thumbColor = AccentLime, activeTrackColor = AccentLime)
            )
        }
    }
}

@Composable
fun CropTool(
    config: CropConfig,
    onConfigChanged: (CropConfig) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Aspect Ratio: 1:1 (Default)", style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.Bold)
            OutlinedButton(
                onClick = { onConfigChanged(CropConfig()) },
                shape = RoundedCornerShape(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text("Reset Crop", fontSize = 11.sp, color = AccentLime)
            }
        }

        // Zoom Slider
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Zoom", color = TextSecondary, fontSize = 13.sp)
            Text(String.format("%.2fx", config.zoom), color = AccentLime, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = config.zoom,
            onValueChange = { onConfigChanged(config.copy(zoom = it)) },
            valueRange = 0.5f..3.0f,
            colors = SliderDefaults.colors(thumbColor = AccentLime, activeTrackColor = AccentLime)
        )

        // Pan X
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Pan X", color = TextSecondary, fontSize = 13.sp)
            Text(String.format("%.2f", config.panX), color = AccentLime)
        }
        Slider(
            value = config.panX,
            onValueChange = { onConfigChanged(config.copy(panX = it)) },
            valueRange = -0.5f..0.5f,
            colors = SliderDefaults.colors(thumbColor = AccentLime, activeTrackColor = AccentLime)
        )

        // Pan Y
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Pan Y", color = TextSecondary, fontSize = 13.sp)
            Text(String.format("%.2f", config.panY), color = AccentLime)
        }
        Slider(
            value = config.panY,
            onValueChange = { onConfigChanged(config.copy(panY = it)) },
            valueRange = -0.5f..0.5f,
            colors = SliderDefaults.colors(thumbColor = AccentLime, activeTrackColor = AccentLime)
        )
    }
}

@Composable
fun TransformTool(
    config: TransformConfig,
    onConfigChanged: (TransformConfig) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Transform", style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.Bold)
            OutlinedButton(
                onClick = { onConfigChanged(TransformConfig()) },
                shape = RoundedCornerShape(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text("Reset Transform", fontSize = 11.sp, color = AccentLime)
            }
        }

        // Scale
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Scale", color = TextSecondary, fontSize = 13.sp)
            Text(String.format("%.2fx", config.scale), color = AccentLime)
        }
        Slider(
            value = config.scale,
            onValueChange = { onConfigChanged(config.copy(scale = it)) },
            valueRange = 0.5f..2.5f,
            colors = SliderDefaults.colors(thumbColor = AccentLime, activeTrackColor = AccentLime)
        )

        // Rotation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Rotation", color = TextSecondary, fontSize = 13.sp)
            Text("${config.rotation.toInt()}°", color = AccentLime)
        }
        Slider(
            value = config.rotation,
            onValueChange = { onConfigChanged(config.copy(rotation = it)) },
            valueRange = -180f..180f,
            colors = SliderDefaults.colors(thumbColor = AccentLime, activeTrackColor = AccentLime)
        )

        // Flips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { onConfigChanged(config.copy(flipHorizontal = !config.flipHorizontal)) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (config.flipHorizontal) AccentLime else SurfaceDark,
                    contentColor = if (config.flipHorizontal) TextOnAccent else TextPrimary
                )
            ) {
                Text("Flip Horizontal")
            }

            OutlinedButton(
                onClick = { onConfigChanged(config.copy(flipVertical = !config.flipVertical)) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (config.flipVertical) AccentLime else SurfaceDark,
                    contentColor = if (config.flipVertical) TextOnAccent else TextPrimary
                )
            ) {
                Text("Flip Vertical")
            }
        }
    }
}

@Composable
fun BackgroundTool(
    config: BackgroundConfig,
    onConfigChanged: (BackgroundConfig) -> Unit
) {
    val bgTypes = listOf(
        BackgroundType.TRANSPARENT to "Transparent",
        BackgroundType.ORIGINAL to "Original",
        BackgroundType.WHITE to "White",
        BackgroundType.BLACK to "Black",
        BackgroundType.COLOR to "Color",
        BackgroundType.GRADIENT_LINEAR to "Linear Grad",
        BackgroundType.GRADIENT_RADIAL to "Radial Grad"
    )

    val colorPalette = listOf(
        0xFF1C1F26 to "Dark Slate",
        0xFF0D0E12 to "Onyx",
        0xFF1A1B2F to "Deep Midnight",
        0xFF6366F1 to "Indigo",
        0xFF38BDF8 to "Cyan",
        0xFFD8FD49 to "Lime",
        0xFFFF6B6B to "Coral",
        0xFFFFFFFF to "Pure White"
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Background Type", style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.Bold)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            bgTypes.forEach { (type, label) ->
                val selected = config.type == type
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selected) AccentLime else SurfaceDark)
                        .border(1.dp, if (selected) AccentLime else BorderDark, RoundedCornerShape(10.dp))
                        .clickable { onConfigChanged(config.copy(type = type)) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(label, color = if (selected) TextOnAccent else TextSecondary, fontSize = 12.sp)
                }
            }
        }

        if (config.type == BackgroundType.COLOR || config.type == BackgroundType.GRADIENT_LINEAR || config.type == BackgroundType.GRADIENT_RADIAL) {
            Spacer(modifier = Modifier.height(4.dp))
            Text("Palette Color", color = TextSecondary, fontSize = 13.sp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                colorPalette.forEach { (colorVal, _) ->
                    val isCurrent = config.primaryColor == colorVal
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(colorVal))
                            .border(
                                2.dp,
                                if (isCurrent) AccentLime else BorderDark,
                                CircleShape
                            )
                            .clickable { onConfigChanged(config.copy(primaryColor = colorVal)) }
                    )
                }
            }
        }
    }
}

@Composable
fun BorderTool(
    config: BorderConfig,
    onConfigChanged: (BorderConfig) -> Unit
) {
    val borderColors = listOf(
        0xFFD8FD49 to "Lime",
        0xFFFFFFFF to "White",
        0xFF6366F1 to "Indigo",
        0xFF38BDF8 to "Cyan",
        0xFFFF6B6B to "Coral",
        0xFF0D0E12 to "Black"
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Enable Border", style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.Bold)
            Switch(
                checked = config.enabled,
                onCheckedChange = { onConfigChanged(config.copy(enabled = it)) },
                colors = SwitchDefaults.colors(checkedThumbColor = AccentLime, checkedTrackColor = SurfaceVariantDark)
            )
        }

        if (config.enabled) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Thickness", color = TextSecondary, fontSize = 13.sp)
                Text("${config.thickness.toInt()} dp", color = AccentLime)
            }
            Slider(
                value = config.thickness,
                onValueChange = { onConfigChanged(config.copy(thickness = it)) },
                valueRange = 1f..16f,
                colors = SliderDefaults.colors(thumbColor = AccentLime, activeTrackColor = AccentLime)
            )

            Text("Border Color", color = TextSecondary, fontSize = 13.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                borderColors.forEach { (c, _) ->
                    val isSel = config.color == c
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(c))
                            .border(2.dp, if (isSel) AccentLime else BorderDark, CircleShape)
                            .clickable { onConfigChanged(config.copy(color = c)) }
                    )
                }
            }
        }
    }
}

@Composable
fun ShadowTool(
    config: ShadowConfig,
    onConfigChanged: (ShadowConfig) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Enable Shadow", style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.Bold)
            Switch(
                checked = config.enabled,
                onCheckedChange = { onConfigChanged(config.copy(enabled = it)) },
                colors = SwitchDefaults.colors(checkedThumbColor = AccentLime, checkedTrackColor = SurfaceVariantDark)
            )
        }

        if (config.enabled) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Opacity", color = TextSecondary, fontSize = 13.sp)
                Text("${(config.opacity * 100).toInt()}%", color = AccentLime)
            }
            Slider(
                value = config.opacity,
                onValueChange = { onConfigChanged(config.copy(opacity = it)) },
                valueRange = 0.1f..1.0f,
                colors = SliderDefaults.colors(thumbColor = AccentLime, activeTrackColor = AccentLime)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Offset Y", color = TextSecondary, fontSize = 13.sp)
                Text("${config.offsetY.toInt()} px", color = AccentLime)
            }
            Slider(
                value = config.offsetY,
                onValueChange = { onConfigChanged(config.copy(offsetY = it)) },
                valueRange = 0f..24f,
                colors = SliderDefaults.colors(thumbColor = AccentLime, activeTrackColor = AccentLime)
            )
        }
    }
}

@Composable
fun AdjustmentTool(
    config: AdjustmentConfig,
    onConfigChanged: (AdjustmentConfig) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Color Adjustments", style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.Bold)
            OutlinedButton(
                onClick = { onConfigChanged(AdjustmentConfig()) },
                shape = RoundedCornerShape(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text("Reset", fontSize = 11.sp, color = AccentLime)
            }
        }

        // Brightness
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Brightness", color = TextSecondary, fontSize = 13.sp)
            Text(String.format("%.2f", config.brightness), color = AccentLime)
        }
        Slider(
            value = config.brightness,
            onValueChange = { onConfigChanged(config.copy(brightness = it)) },
            valueRange = -1.0f..1.0f,
            colors = SliderDefaults.colors(thumbColor = AccentLime, activeTrackColor = AccentLime)
        )

        // Contrast
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Contrast", color = TextSecondary, fontSize = 13.sp)
            Text(String.format("%.2f", config.contrast), color = AccentLime)
        }
        Slider(
            value = config.contrast,
            onValueChange = { onConfigChanged(config.copy(contrast = it)) },
            valueRange = 0.0f..2.0f,
            colors = SliderDefaults.colors(thumbColor = AccentLime, activeTrackColor = AccentLime)
        )

        // Saturation
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Saturation", color = TextSecondary, fontSize = 13.sp)
            Text(String.format("%.2f", config.saturation), color = AccentLime)
        }
        Slider(
            value = config.saturation,
            onValueChange = { onConfigChanged(config.copy(saturation = it)) },
            valueRange = 0.0f..2.0f,
            colors = SliderDefaults.colors(thumbColor = AccentLime, activeTrackColor = AccentLime)
        )

        // Grayscale & Invert toggles
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { onConfigChanged(config.copy(grayscale = !config.grayscale)) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (config.grayscale) AccentLime else SurfaceDark,
                    contentColor = if (config.grayscale) TextOnAccent else TextPrimary
                )
            ) {
                Text("Grayscale")
            }

            OutlinedButton(
                onClick = { onConfigChanged(config.copy(invert = !config.invert)) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (config.invert) AccentLime else SurfaceDark,
                    contentColor = if (config.invert) TextOnAccent else TextPrimary
                )
            ) {
                Text("Invert")
            }
        }
    }
}

@Composable
fun FilterTool(
    config: FilterConfig,
    onConfigChanged: (FilterConfig) -> Unit
) {
    val filters = listOf(
        IconFilter.ORIGINAL to "Original",
        IconFilter.MONO to "Mono",
        IconFilter.DARK to "Dark",
        IconFilter.LIGHT to "Light",
        IconFilter.CONTRAST to "Contrast",
        IconFilter.SOFT to "Soft",
        IconFilter.INVERT to "Invert"
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Preset Filters", style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.Bold)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filters.forEach { (filter, name) ->
                val selected = config.filter == filter
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selected) AccentLime else SurfaceDark)
                        .border(1.dp, if (selected) AccentLime else BorderDark, RoundedCornerShape(12.dp))
                        .clickable { onConfigChanged(config.copy(filter = filter)) }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = name,
                        color = if (selected) TextOnAccent else TextSecondary,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
