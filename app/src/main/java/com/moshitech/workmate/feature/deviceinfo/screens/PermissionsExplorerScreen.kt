package com.moshitech.workmate.feature.deviceinfo.screens

import androidx.compose.animation.AnimatedVisibility
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
import com.moshitech.workmate.feature.deviceinfo.viewmodel.GroupingMode
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
    val searchQuery by viewModel.searchQuery.collectAsState()
    val groupingMode by viewModel.groupingMode.collectAsState()
    val stats by viewModel.stats.collectAsState()

    val backgroundColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF8F9FA)
    val cardColor = if (isDark) Color(0xFF1E293B) else Color.White
    val textColor = if (isDark) Color.White else Color(0xFF111827)
    val subtitleColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF6B7280)

    var showSearch by remember { mutableStateOf(false) }

    // Refresh permissions when screen resumes (e.g., returning from app settings)
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.loadPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = { 
                    if (showSearch) {
                        TextField(
                            value = searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = { Text("Search apps...", color = subtitleColor) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedTextColor = textColor,
                                unfocusedTextColor = textColor
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Column {
                            Text("Permissions Explorer", fontWeight = FontWeight.Bold, color = textColor)
                            Text("Pro Feature", fontSize = 12.sp, color = Color(0xFFFF9800), fontWeight = FontWeight.Bold)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { 
                        if (showSearch) {
                            showSearch = false
                            viewModel.setSearchQuery("")
                        } else {
                            navController.popBackStack()
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = textColor)
                    }
                },
                actions = {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = textColor
                        )
                    }
                    IconButton(onClick = { showSearch = !showSearch }) {
                        Icon(
                            if (showSearch) Icons.Default.Close else Icons.Default.Search,
                            "Search",
                            tint = textColor
                        )
                    }
                    IconButton(onClick = { viewModel.toggleGroupingMode() }) {
                        Icon(
                            if (groupingMode == GroupingMode.BY_PERMISSION) Icons.Default.Apps else Icons.Default.Security,
                            "Toggle View",
                            tint = textColor
                        )
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
                
                // Statistics Card
                item {
                    stats?.let { statistics ->
                        StatisticsCard(
                            stats = statistics,
                            cardColor = cardColor,
                            textColor = textColor,
                            subtitleColor = subtitleColor
                        )
                    }
                }
                
                // Group/App Cards
                items(permissionGroups) { group ->
                    if (groupingMode == GroupingMode.BY_PERMISSION) {
                        PermissionGroupCard(
                            group = group,
                            cardColor = cardColor,
                            textColor = textColor,
                            subtitleColor = subtitleColor
                        )
                    } else {
                        AppGroupCard(
                            group = group,
                            cardColor = cardColor,
                            textColor = textColor,
                            subtitleColor = subtitleColor
                        )
                    }
                }
                
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
fun StatisticsCard(
    stats: com.moshitech.workmate.feature.deviceinfo.viewmodel.PermissionStats,
    cardColor: Color,
    textColor: Color,
    subtitleColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.BarChart,
                    contentDescription = null,
                    tint = Color(0xFF3B82F6),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Statistics",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth()) {
                StatItem("Permissions", stats.totalPermissions.toString(), Modifier.weight(1f), textColor, subtitleColor)
                StatItem("Apps", stats.totalApps.toString(), Modifier.weight(1f), textColor, subtitleColor)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            StatItem("Most Requested", stats.mostRequestedPermission, Modifier.fillMaxWidth(), textColor, subtitleColor)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            StatItem("App w/ Most Perms", stats.appsWithMostPermissions, Modifier.fillMaxWidth(), textColor, subtitleColor)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Permission Status Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFF10B981).copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "= Allowed",
                            fontSize = 12.sp,
                            color = textColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFFEF4444).copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Cancel,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "= Denied",
                            fontSize = 12.sp,
                            color = textColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, modifier: Modifier, textColor: Color, subtitleColor: Color) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = subtitleColor
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}

@Composable
fun AppGroupCard(
    group: PermissionGroup,
    cardColor: Color,
    textColor: Color,
    subtitleColor: Color
) {
    var expanded by remember { mutableStateOf(false) }
    val app = group.apps.firstOrNull() ?: return

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
                if (app.icon != null) {
                    Image(
                        painter = rememberAsyncImagePainter(app.icon),
                        contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.Gray.copy(alpha = 0.2f), CircleShape)
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
                        text = group.description,
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
            
            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = subtitleColor.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    app.permissions.forEach { permission ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = getPermissionIcon(permission),
                                contentDescription = null,
                                tint = getPermissionColor(permission),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = permission,
                                fontSize = 14.sp,
                                color = textColor
                            )
                        }
                    }
                }
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
            
            AnimatedVisibility(visible = expanded) {
                Column {
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
}

@Composable
fun AppPermissionItem(app: AppPermissionInfo, textColor: Color) {
    val context = androidx.compose.ui.platform.LocalContext.current
    
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
        
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = app.appName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = textColor
                )
                Spacer(modifier = Modifier.width(6.dp))
                // Status Icon
                Icon(
                    imageVector = if (app.isGranted) Icons.Default.CheckCircle else Icons.Default.Cancel,
                    contentDescription = if (app.isGranted) "Allowed" else "Denied",
                    tint = if (app.isGranted) Color(0xFF10B981) else Color(0xFFEF4444),
                    modifier = Modifier.size(14.dp)
                )
            }
            Text(
                text = app.packageName,
                fontSize = 11.sp,
                color = textColor.copy(alpha = 0.6f)
            )
        }
        
        // Action Button
        IconButton(
            onClick = {
                // Open app settings
                val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = android.net.Uri.fromParts("package", app.packageName, null)
                    flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "App Settings",
                tint = textColor.copy(alpha = 0.7f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

private fun getPermissionIcon(name: String): ImageVector {
    return when {
        name.contains("Camera", true) -> Icons.Default.CameraAlt
        name.contains("Location", true) -> Icons.Default.LocationOn
        name.contains("Microphone", true) || name.contains("Audio", true) || name.contains("Record", true) -> Icons.Default.Mic
        name.contains("Contacts", true) -> Icons.Default.Contacts
        name.contains("Storage", true) || name.contains("Read", true) || name.contains("Write", true) -> Icons.Default.Storage
        name.contains("Phone", true) || name.contains("Call", true) -> Icons.Default.Phone
        name.contains("Sms", true) || name.contains("Message", true) -> Icons.Default.Sms
        name.contains("Calendar", true) -> Icons.Default.CalendarToday
        name.contains("Sensors", true) || name.contains("Body", true) -> Icons.Default.Sensors
        else -> Icons.Default.Security
    }
}

private fun getPermissionColor(name: String): Color {
    return when {
        name.contains("Camera", true) -> Color(0xFFEF4444)
        name.contains("Location", true) -> Color(0xFF3B82F6)
        name.contains("Microphone", true) || name.contains("Audio", true) || name.contains("Record", true) -> Color(0xFFF59E0B)
        name.contains("Contacts", true) -> Color(0xFF10B981)
        name.contains("Storage", true) || name.contains("Read", true) || name.contains("Write", true) -> Color(0xFF8B5CF6)
        else -> Color(0xFF6366F1)
    }
}
