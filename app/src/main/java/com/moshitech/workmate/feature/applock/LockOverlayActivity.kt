package com.moshitech.workmate.feature.applock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moshitech.workmate.ui.theme.WorkmateTheme

class LockOverlayActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val repository = com.moshitech.workmate.data.repository.UserPreferencesRepository(applicationContext)
        
        setContent {
            val theme by repository.theme.collectAsState(initial = com.moshitech.workmate.data.repository.AppTheme.SYSTEM)
            
            // Determine if dark mode should be used
            val systemDark = isSystemInDarkTheme()
            val isDark = when (theme) {
                com.moshitech.workmate.data.repository.AppTheme.LIGHT -> false
                com.moshitech.workmate.data.repository.AppTheme.DARK -> true
                com.moshitech.workmate.data.repository.AppTheme.SYSTEM -> systemDark
            }
            
            WorkmateTheme {
                LockOverlayScreen(
                    isDark = isDark,
                    onUnlocked = { finish() }
                )
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Go to home instead of allowing back navigation
        val intent = android.content.Intent(android.content.Intent.ACTION_MAIN)
        intent.addCategory(android.content.Intent.CATEGORY_HOME)
        intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }
}

@Composable
fun LockOverlayScreen(
    isDark: Boolean,
    onUnlocked: () -> Unit,
    viewModel: AppLockViewModel = viewModel()
) {
    val backgroundColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF8F9FA)
    val textColor = if (isDark) Color.White else Color(0xFF111827)
    
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(pin) {
        if (pin.length == 4) {
            viewModel.verifyPin(pin) { isCorrect ->
                if (isCorrect) {
                    onUnlocked()
                } else {
                    error = "Incorrect PIN"
                    pin = ""
                }
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = backgroundColor
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "App Locked",
                style = MaterialTheme.typography.headlineLarge,
                color = textColor,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                "Enter PIN to unlock",
                style = MaterialTheme.typography.bodyLarge,
                color = textColor.copy(alpha = 0.7f)
            )
            
            if (error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(error!!, color = Color.Red, style = MaterialTheme.typography.bodyMedium)
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Pin Dots
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                repeat(4) { index ->
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(
                                if (index < pin.length) textColor else Color.Gray.copy(alpha = 0.3f),
                                CircleShape
                            )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(64.dp))
            
            // Number Pad
            val numbers = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("", "0", "⌫")
            )

            numbers.forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                    row.forEach { digit ->
                        if (digit.isEmpty()) {
                            Spacer(modifier = Modifier.size(80.dp))
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .background(if (isDark) Color(0xFF1E293B) else Color.White, CircleShape)
                                    .clickable {
                                        error = null
                                        if (digit == "⌫") {
                                            if (pin.isNotEmpty()) pin = pin.dropLast(1)
                                        } else {
                                            if (pin.length < 4) pin += digit
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    digit,
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = textColor
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
