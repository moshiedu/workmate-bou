package com.moshitech.workmate.feature.deviceinfo

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.moshitech.workmate.feature.deviceinfo.tabs.DashboardTab
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceInfoScreen(
    navController: NavController,
    viewModel: DeviceInfoViewModel = viewModel()
) {
    // Read theme from repository
    val context = androidx.compose.ui.platform.LocalContext.current
    val repository = remember { com.moshitech.workmate.data.repository.UserPreferencesRepository(context) }
    val theme by repository.theme.collectAsState(initial = com.moshitech.workmate.data.repository.AppTheme.SYSTEM)
    
    // Determine if dark mode should be used
    val systemDark = isSystemInDarkTheme()
    val isDark = when (theme) {
        com.moshitech.workmate.data.repository.AppTheme.LIGHT -> false
        com.moshitech.workmate.data.repository.AppTheme.DARK -> true
        com.moshitech.workmate.data.repository.AppTheme.SYSTEM -> systemDark
    }
    
    val backgroundColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF8F9FA)
    val textColor = if (isDark) Color.White else Color(0xFF111827)
    
    val tabs = listOf("Dashboard", "Hardware", "System", "Battery", "Network", "Camera", "Sensors", "Apps")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()
    
    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = { Text("Device Info", fontWeight = FontWeight.Bold, color = textColor) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = textColor)
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(com.moshitech.workmate.navigation.Screen.Settings.route) }) {
                        Icon(androidx.compose.material.icons.Icons.Default.Settings, "Settings", tint = textColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            ScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = backgroundColor,
                contentColor = Color(0xFF1890FF),
                edgePadding = 0.dp
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = { Text(title) }
                    )
                }
            }
            
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> com.moshitech.workmate.feature.deviceinfo.tabs.DashboardTabEnhanced(navController, viewModel, isDark, textColor)
                    1 -> com.moshitech.workmate.feature.deviceinfo.tabs.HardwareTabEnhanced(viewModel, isDark, textColor)
                    2 -> com.moshitech.workmate.feature.deviceinfo.tabs.SystemTabEnhanced(viewModel, isDark, textColor)
                    3 -> com.moshitech.workmate.feature.deviceinfo.tabs.BatteryTab(isDark)
                    4 -> com.moshitech.workmate.feature.deviceinfo.tabs.NetworkTab(viewModel, isDark, textColor)
                    5 -> com.moshitech.workmate.feature.deviceinfo.tabs.CameraTab(viewModel, isDark, textColor)
                    6 -> com.moshitech.workmate.feature.deviceinfo.tabs.SensorsTab(navController, viewModel, isDark, textColor)
                    7 -> com.moshitech.workmate.feature.deviceinfo.tabs.AppsTab(viewModel, isDark, textColor)
                }
            }
        }
    }
}
