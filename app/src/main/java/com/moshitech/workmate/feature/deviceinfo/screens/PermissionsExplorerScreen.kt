package com.moshitech.workmate.feature.deviceinfo.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.moshitech.workmate.feature.deviceinfo.viewmodel.AppPermissionInfo
import com.moshitech.workmate.feature.deviceinfo.viewmodel.PermissionGroup
import com.moshitech.workmate.feature.deviceinfo.viewmodel.PermissionsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsExplorerScreen(
    navController: NavController,
    isDark: Boolean = true
) {
    val viewModel: PermissionsViewModel = viewModel()
    val permissionGroups by viewModel.permissionGroups.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val backgroundColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF8F9FA)
    val cardColor = if (isDark) Color(0xFF1E293B) else Color.White
    val textColor = if (isDark) Color.White else Color(0xFF111827)
    val subtitleColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF6B7280)

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Permissions Explorer", fontWeight = FontWeight.Bold, color = textColor)
                        Text("Pro Feature", fontSize = 12.sp, color = Color(0xFFFF9800), fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = textColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor)
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }
                
                items(permissionGroups) { group ->
                    PermissionGroupCard(
                        group = group,
                        cardColor = cardColor,
                        textColor = textColor,
                        subtitleColor = subtitleColor
                    )
                }
                
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
fun PermissionGroupCard(
    group: PermissionGroup,
    cardColor: Color,
    textColor: Color,
    subtitleColor: Color
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            getPermissionColor(group.name).copy(alpha = 0.1f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getPermissionIcon(group.name),
                        contentDescription = null,
                        tint = getPermissionColor(group.name),
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = group.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor
                    )
                    Text(
                        text = "${group.apps.size} apps allowed",
                        fontSize = 13.sp,
                        color = subtitleColor
                    )
                }
                
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = subtitleColor
                )
            }
            
            if (expanded) {
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = subtitleColor.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(8.dp))
                
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    group.apps.forEach { app ->
                        AppPermissionItem(app, textColor)
                    }
                }
            }
        }
    }
}

@Composable
fun AppPermissionItem(app: AppPermissionInfo, textColor: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        if (app.icon != null) {
            Image(
                painter = rememberAsyncImagePainter(app.icon),
                contentDescription = null,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(Color.Gray.copy(alpha = 0.2f), CircleShape)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column {
            Text(
                text = app.appName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
            Text(
                text = app.packageName,
                fontSize = 11.sp,
                color = textColor.copy(alpha = 0.6f)
            )
        }
    }
}

private fun getPermissionIcon(name: String): ImageVector {
    return when {
        name.contains("Camera", true) -> Icons.Default.CameraAlt
        name.contains("Location", true) -> Icons.Default.LocationOn
        name.contains("Microphone", true) -> Icons.Default.Mic
        name.contains("Contacts", true) -> Icons.Default.Contacts
        name.contains("Storage", true) -> Icons.Default.Storage
        name.contains("Phone", true) -> Icons.Default.Phone
        name.contains("Sms", true) -> Icons.Default.Sms
        name.contains("Calendar", true) -> Icons.Default.CalendarToday
        name.contains("Sensors", true) -> Icons.Default.Sensors
        else -> Icons.Default.Security
    }
}

private fun getPermissionColor(name: String): Color {
    return when {
        name.contains("Camera", true) -> Color(0xFFEF4444) // Red
        name.contains("Location", true) -> Color(0xFF3B82F6) // Blue
        name.contains("Microphone", true) -> Color(0xFFF59E0B) // Orange
        name.contains("Contacts", true) -> Color(0xFF10B981) // Green
        else -> Color(0xFF8B5CF6) // Purple
    }
}
