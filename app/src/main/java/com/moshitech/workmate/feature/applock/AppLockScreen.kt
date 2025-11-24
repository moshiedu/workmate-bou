package com.moshitech.workmate.feature.applock

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@Composable
fun AppLockScreen(
    navController: NavController,
    viewModel: AppLockViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val pin by viewModel.pin.collectAsState()
    val message by viewModel.message.collectAsState()
    val error by viewModel.error.collectAsState()
    
    val isDark = isSystemInDarkTheme()
    val backgroundColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF8F9FA)
    val textColor = if (isDark) Color.White else Color(0xFF111827)
    val primaryBlue = Color(0xFF1976D2)
    val errorColor = MaterialTheme.colorScheme.error

    LaunchedEffect(message) {
        if (message == "Unlocked" || message == "PIN Set Successfully") {
            navController.popBackStack()
        }
    }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = textColor
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "App Lock",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = textColor
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
                color = if (error != null) errorColor else textColor
            )
            
            if (error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error!!,
                    style = MaterialTheme.typography.bodyMedium,
                    color = errorColor
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // PIN Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(4) { index ->
                    val isFilled = index < pin.length
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(if (isFilled) primaryBlue else Color.Transparent)
                            .border(1.dp, if (isFilled) primaryBlue else textColor.copy(alpha = 0.5f), CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(64.dp))

            // Keypad
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                val rows = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("", "0", "backspace")
                )

                rows.forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(32.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        row.forEach { key ->
                            if (key == "backspace") {
                                IconButton(
                                    onClick = { viewModel.onDeleteDigit() },
                                    modifier = Modifier.size(72.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Backspace,
                                        contentDescription = "Delete",
                                        tint = textColor
                                    )
                                }
                            } else if (key.isEmpty()) {
                                Spacer(modifier = Modifier.size(72.dp))
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(CircleShape)
                                        .background(if (isDark) Color(0xFF1E293B) else Color.White)
                                        .clickable { viewModel.onPinDigit(key) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = key,
                                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = textColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
