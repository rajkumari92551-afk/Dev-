package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.ThemeMode
import com.example.ui.DevSocialUiState
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.LiquidGlassPill
import com.example.ui.components.LiquidGlassTopBar
import com.example.ui.theme.CobaltBlue
import com.example.ui.theme.LikeRose
import com.example.ui.theme.LocalLiquidGlassColors

@Composable
fun SettingsScreen(
    state: DevSocialUiState,
    onBack: () -> Unit,
    onSetThemeMode: (ThemeMode) -> Unit,
    onSetGlassIntensity: (Boolean) -> Unit,
    onSetPrivateAccount: (Boolean) -> Unit,
    onSetShowOnlineStatus: (Boolean) -> Unit,
    onSetNotifyLikes: (Boolean) -> Unit,
    onSetNotifyComments: (Boolean) -> Unit,
    onSetNotifyRequests: (Boolean) -> Unit,
    onResetDemoData: () -> Unit,
    onTestErrorBoundary: () -> Unit = {}
) {
    val glass = LocalLiquidGlassColors.current
    val settings = state.settings
    var showResetDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        LiquidGlassTopBar {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("settings_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back"
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Appearance, privacy & account preferences",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Appearance & Theme Card
            item {
                LiquidGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 640.dp),
                    shape = RoundedCornerShape(26.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SectionTitle(
                            icon = Icons.Rounded.Palette,
                            title = "Appearance & Liquid Glass"
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ThemeMode.entries.forEach { mode ->
                                val selected = settings.themeMode == mode
                                LiquidGlassPill(
                                    selected = selected,
                                    onClick = { onSetThemeMode(mode) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("theme_option_${mode.name}")
                                ) {
                                    Text(
                                        text = mode.label,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = glass.subtleDivider)
                        SettingsToggleRow(
                            title = "High Glass Refraction",
                            subtitle = "Boosts background mesh blur and panel translucency",
                            checked = settings.highGlassIntensity,
                            onCheckedChange = onSetGlassIntensity,
                            testTag = "toggle_glass_intensity"
                        )
                    }
                }
            }

            // Privacy Card
            item {
                LiquidGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 640.dp),
                    shape = RoundedCornerShape(26.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SectionTitle(
                            icon = Icons.Rounded.Lock,
                            title = "Privacy & Visibility"
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        SettingsToggleRow(
                            title = "Private Creator Account",
                            subtitle = "Only approved friends can view your full posts",
                            checked = settings.privateAccount,
                            onCheckedChange = onSetPrivateAccount,
                            testTag = "toggle_private_account"
                        )
                        HorizontalDivider(color = glass.subtleDivider)
                        SettingsToggleRow(
                            title = "Show Active Status",
                            subtitle = "Display green availability dot in Messenger & Circle",
                            checked = settings.showOnlineStatus,
                            onCheckedChange = onSetShowOnlineStatus,
                            testTag = "toggle_online_status"
                        )
                    }
                }
            }

            // Notifications Card
            item {
                LiquidGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 640.dp),
                    shape = RoundedCornerShape(26.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SectionTitle(
                            icon = Icons.Rounded.NotificationsActive,
                            title = "Notifications"
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        SettingsToggleRow(
                            title = "Likes & Reactions",
                            subtitle = "Notify when creators like your posts or reels",
                            checked = settings.notifyLikes,
                            onCheckedChange = onSetNotifyLikes,
                            testTag = "toggle_notify_likes"
                        )
                        HorizontalDivider(color = glass.subtleDivider)
                        SettingsToggleRow(
                            title = "Comments & Mentions",
                            subtitle = "Notify on new replies and @handle mentions",
                            checked = settings.notifyComments,
                            onCheckedChange = onSetNotifyComments,
                            testTag = "toggle_notify_comments"
                        )
                        HorizontalDivider(color = glass.subtleDivider)
                        SettingsToggleRow(
                            title = "Friend Requests",
                            subtitle = "Alert when developers want to connect",
                            checked = settings.notifyRequests,
                            onCheckedChange = onSetNotifyRequests,
                            testTag = "toggle_notify_requests"
                        )
                    }
                }
            }

            // Demo Safety Notice & Reset
            item {
                LiquidGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 640.dp),
                    shape = RoundedCornerShape(26.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SectionTitle(
                            icon = Icons.Rounded.Shield,
                            title = "Demo Sandbox & Data"
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "DEV SOCIAL is running in local demo mode with fictional profiles and sample posts. Theme and preference toggles persist locally via DataStore, and no personal user data is collected or transmitted.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { showResetDialog = true },
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LikeRose.copy(alpha = 0.14f),
                                contentColor = LikeRose
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reset_demo_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.RestartAlt,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Reset Demo Content & Settings",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = onTestErrorBoundary,
                            shape = CircleShape,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("test_error_boundary_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.WarningAmber,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Preview Global Error Boundary",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "DEV SOCIAL v1.0 • Original Liquid Glass UI",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Demo Data?") },
            text = {
                Text("This restores the initial sample posts, stories, friend requests, messages, and default settings.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResetDemoData()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CobaltBlue)
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SectionTitle(icon: ImageVector, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag)
        )
    }
}
