package com.enzvuck.icon.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.enzvuck.icon.domain.model.AppInfo
import com.enzvuck.icon.domain.model.CustomIcon
import com.enzvuck.icon.ui.theme.AccentLime
import com.enzvuck.icon.ui.theme.BorderDark
import com.enzvuck.icon.ui.theme.SurfaceDark
import com.enzvuck.icon.ui.theme.SurfaceHighDark
import com.enzvuck.icon.ui.theme.TextMuted
import com.enzvuck.icon.ui.theme.TextPrimary
import com.enzvuck.icon.ui.theme.TextSecondary
import java.io.File

/**
 * Simulated Home Screen preview.
 *
 * CRITICAL REQUIREMENTS (Section 34):
 * This is ONLY a preview to visualize how customized icons look on a modern launcher home screen.
 * It NEVER creates real home screen shortcuts or accesses ShortcutManager APIs.
 */
@Composable
fun SimulatedHomeScreen(
    installedApps: List<AppInfo>,
    customIcons: List<CustomIcon>,
    modifier: Modifier = Modifier
) {
    val customIconMap = customIcons.associateBy { it.packageName }

    // Take a selection of popular apps or customized apps
    val previewApps = if (installedApps.isNotEmpty()) {
        installedApps.take(12)
    } else {
        emptyList()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("simulated_home_screen")
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Home Screen Preview",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
            color = TextPrimary
        )
        Text(
            text = "Simulated visual preview only • No shortcuts created",
            style = MaterialTheme.typography.bodyMedium,
            color = AccentLime
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Important Disclaimer Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(BorderDark)
            )
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Info, contentDescription = null, tint = AccentLime, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "This preview simulates a home screen layout using your actual custom icons. enzvuck icon never creates pinned shortcuts or alters other apps.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Simulated Phone Frame
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(32.dp))
                .border(2.5.dp, BorderDark, RoundedCornerShape(32.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1E2430),
                            Color(0xFF0F121A)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top status bar (09:41)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "09:41",
                        color = TextPrimary,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("5G", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("100%", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Clock Widget on Phone Preview
                Text(
                    text = "09:41",
                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Light),
                    color = TextPrimary
                )
                Text(
                    text = "Wednesday, September 23",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Grid of 4 columns of apps
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val rows = previewApps.chunked(4).take(2)
                    for (row in rows) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            for (app in row) {
                                val custom = customIconMap[app.packageName]
                                SimulatedAppIcon(
                                    appName = app.appName,
                                    iconPath = custom?.iconPath
                                )
                            }
                        }
                    }
                }

                // Dock at bottom
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(24.dp))
                        .padding(vertical = 12.dp, horizontal = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        val dockApps = previewApps.drop(8).take(4)
                        for (app in dockApps) {
                            val custom = customIconMap[app.packageName]
                            SimulatedAppIcon(
                                appName = app.appName,
                                iconPath = custom?.iconPath,
                                showLabel = false
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(84.dp))
    }
}

@Composable
fun SimulatedAppIcon(
    appName: String,
    iconPath: String?,
    showLabel: Boolean = true
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(64.dp)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(SurfaceHighDark)
                .border(
                    1.dp,
                    if (iconPath != null) AccentLime.copy(alpha = 0.6f) else BorderDark,
                    RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (iconPath != null && File(iconPath).exists()) {
                AsyncImage(
                    model = File(iconPath),
                    contentDescription = appName,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    text = appName.take(1).uppercase(),
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
        }

        if (showLabel) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = appName,
                style = MaterialTheme.typography.labelSmall,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}
