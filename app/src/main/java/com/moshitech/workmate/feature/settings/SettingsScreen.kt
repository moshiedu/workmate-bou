package com.moshitech.workmate.feature.settings

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

import androidx.compose.runtime.collectAsState
import com.moshitech.workmate.MainViewModel
import com.moshitech.workmate.data.repository.AppTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.RadioButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, mainViewModel: MainViewModel) {
    val theme by mainViewModel.theme.collectAsState()
    val isDark = isSystemInDarkTheme()
    
    // Colors
    val backgroundColor = MaterialTheme.colorScheme.background // Use theme background
    val textColor = MaterialTheme.colorScheme.onBackground
    val secondaryTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val sectionHeaderColor = MaterialTheme.colorScheme.primary
    val iconBackgroundColor = MaterialTheme.colorScheme.surfaceVariant
    val dividerColor = MaterialTheme.colorScheme.outlineVariant

    // State
    var showThemeDialog by remember { mutableStateOf(false) }
    var isAppLockEnabled by remember { mutableStateOf(false) }
    var areNotificationsEnabled by remember { mutableStateOf(true) }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Choose Theme") },
            text = {
                Column {
                    AppTheme.values().forEach { appTheme ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = (theme == appTheme),
                                    onClick = {
                                        mainViewModel.setTheme(appTheme)
                                        showThemeDialog = false
                                    }
                                )
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (theme == appTheme),
                                onClick = null
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = when (appTheme) {
                                    AppTheme.LIGHT -> "Light"
                                    AppTheme.DARK -> "Dark"
                                    AppTheme.SYSTEM -> "System Default"
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold, color = textColor) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = textColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp)
        ) {
            // Appearance
            item { SectionHeader("Appearance", sectionHeaderColor) }
            item {
                SettingsItem(
                    icon = Icons.Default.DarkMode,
                    title = "Theme",
                    subtitle = when (theme) {
                        AppTheme.LIGHT -> "Light"
                        AppTheme.DARK -> "Dark"
                        AppTheme.SYSTEM -> "System Default"
                    },
                    iconBgColor = iconBackgroundColor,
                    textColor = textColor,
                    subtitleColor = secondaryTextColor,
                    trailing = { Chevron(secondaryTextColor) },
                    onClick = { showThemeDialog = true }
                )
                SettingsDivider(dividerColor)
                SettingsItem(
                    icon = Icons.Default.Language,
                    title = "Language",
                    subtitle = "English",
                    iconBgColor = iconBackgroundColor,
                    textColor = textColor,
                    subtitleColor = secondaryTextColor,
                    trailing = { Chevron(secondaryTextColor) }
                )
            }

            // General
            item { SectionHeader("General", sectionHeaderColor) }
            item {
                SettingsItem(
                    icon = Icons.Default.Lock,
                    title = "App Lock",
                    subtitle = null,
                    iconBgColor = iconBackgroundColor,
                    textColor = textColor,
                    trailing = {
                        Switch(
                            checked = isAppLockEnabled,
                            onCheckedChange = { isAppLockEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = sectionHeaderColor)
                        )
                    }
                )
            }

            // Modules
            item { SectionHeader("Modules", sectionHeaderColor) }
            item {
                SettingsItem(
                    icon = Icons.Default.Image,
                    title = "Photo Conversion",
                    subtitle = "Default format: JPG, Quality: 85%",
                    iconBgColor = iconBackgroundColor,
                    textColor = textColor,
                    subtitleColor = secondaryTextColor,
                    trailing = { Chevron(secondaryTextColor) }
                )
                SettingsDivider(dividerColor)
                SettingsItem(
                    icon = Icons.Default.Calculate,
                    title = "Unit Conversion",
                    subtitle = "Decimal places: 2",
                    iconBgColor = iconBackgroundColor,
                    textColor = textColor,
                    subtitleColor = secondaryTextColor,
                    trailing = { Chevron(secondaryTextColor) }
                )
                SettingsDivider(dividerColor)
                SettingsItem(
                    icon = Icons.Default.RocketLaunch,
                    title = "RAM Booster",
                    subtitle = "Auto-boost disabled",
                    iconBgColor = iconBackgroundColor,
                    textColor = textColor,
                    subtitleColor = secondaryTextColor,
                    trailing = { Chevron(secondaryTextColor) }
                )
            }

            // Notifications
            item { SectionHeader("Notifications", sectionHeaderColor) }
            item {
                SettingsItem(
                    icon = Icons.Default.Notifications,
                    title = "All Notifications",
                    subtitle = null,
                    iconBgColor = iconBackgroundColor,
                    textColor = textColor,
                    trailing = {
                        Switch(
                            checked = areNotificationsEnabled,
                            onCheckedChange = { areNotificationsEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = sectionHeaderColor)
                        )
                    }
                )
            }

            // Data & Storage
            item { SectionHeader("Data & Storage", sectionHeaderColor) }
            item {
                SettingsItem(
                    icon = Icons.Default.Storage,
                    title = "Storage Usage",
                    subtitle = "124 MB used",
                    iconBgColor = iconBackgroundColor,
                    textColor = textColor,
                    subtitleColor = secondaryTextColor,
                    trailing = { Chevron(secondaryTextColor) }
                )
                SettingsDivider(dividerColor)
                SettingsItem(
                    icon = Icons.Default.CleaningServices,
                    title = "Clear Cache",
                    subtitle = "Remove temporary files",
                    iconBgColor = iconBackgroundColor,
                    textColor = textColor,
                    subtitleColor = secondaryTextColor,
                    trailing = null // Action item
                )
            }

            // About & Support
            item { SectionHeader("About & Support", sectionHeaderColor) }
            item {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "App Version",
                    subtitle = "1.2.3",
                    iconBgColor = iconBackgroundColor,
                    textColor = textColor,
                    subtitleColor = secondaryTextColor,
                    trailing = null
                )
                SettingsDivider(dividerColor)
                SettingsItem(
                    icon = Icons.Default.Star,
                    title = "Rate Us",
                    subtitle = null,
                    iconBgColor = iconBackgroundColor,
                    textColor = textColor,
                    trailing = { Chevron(secondaryTextColor) }
                )
                SettingsDivider(dividerColor)
                SettingsItem(
                    icon = Icons.Default.Share,
                    title = "Share App",
                    subtitle = null,
                    iconBgColor = iconBackgroundColor,
                    textColor = textColor,
                    trailing = { Chevron(secondaryTextColor) }
                )
                SettingsDivider(dividerColor)
                SettingsItem(
                    icon = Icons.AutoMirrored.Filled.Help,
                    title = "Help & FAQ",
                    subtitle = null,
                    iconBgColor = iconBackgroundColor,
                    textColor = textColor,
                    trailing = { Chevron(secondaryTextColor) }
                )
                SettingsDivider(dividerColor)
                SettingsItem(
                    icon = Icons.Default.Description,
                    title = "Terms of Service",
                    subtitle = null,
                    iconBgColor = iconBackgroundColor,
                    textColor = textColor,
                    trailing = { Chevron(secondaryTextColor) }
                )
                SettingsDivider(dividerColor)
                SettingsItem(
                    icon = Icons.Default.PrivacyTip,
                    title = "Privacy Policy",
                    subtitle = null,
                    iconBgColor = iconBackgroundColor,
                    textColor = textColor,
                    trailing = { Chevron(secondaryTextColor) }
                )
            }
        }
    }
}

@Composable
fun SectionHeader(text: String, color: Color) {
    Text(
        text = text,
        color = color,
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    iconBgColor: Color,
    textColor: Color,
    subtitleColor: Color = Color.Gray,
    trailing: (@Composable () -> Unit)? = null,
    onClick: () -> Unit = { /* TODO */ }
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor, // Or specific icon color if needed
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = textColor
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = subtitleColor
                )
            }
        }
        if (trailing != null) {
            trailing()
        }
    }
}

@Composable
fun SettingsDivider(color: Color) {
    HorizontalDivider(
        modifier = Modifier.padding(start = 72.dp), // Indent to match text start
        thickness = 0.5.dp,
        color = color
    )
}

@Composable
fun Chevron(color: Color) {
    Icon(
        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
        contentDescription = null,
        tint = color
    )
}
