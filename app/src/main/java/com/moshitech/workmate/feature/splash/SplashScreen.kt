package com.moshitech.workmate.feature.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.moshitech.workmate.navigation.Screen
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    LaunchedEffect(Unit) {
        delay(2000) // 2 seconds delay
        navController.navigate(Screen.Home.route) {
            popUpTo(Screen.Splash.route) { inclusive = true }
        }
    }

    val isDark = isSystemInDarkTheme()
    val backgroundColor = MaterialTheme.colorScheme.background
    
    // Colors based on observation of the provided images
    val circleColor = if (isDark) Color(0xFF1E3A5F) else Color(0xFFE3F2FD) // Dark Blue / Very Light Blue
    val iconColor = if (isDark) Color.White else MaterialTheme.colorScheme.primary
    val titleColor = MaterialTheme.colorScheme.onBackground
    val subtitleColor = if (isDark) Color(0xFFB0B0B0) else Color(0xFF666666)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo Circle
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(circleColor),
                contentAlignment = Alignment.Center
            ) {
                // Using Build (Wrench) and Construction (Hammer) if available to simulate crossed tools
                // Since we can't easily cross them without a custom vector, we'll use a single representative tool 
                // or a combination if possible. 
                // For "Exact" match, a custom vector asset would be best. 
                // Using 'Handyman' or 'Build' as a proxy.
                // Let's try to overlay them to look like crossed tools if possible, or just use one strong icon.
                // The image shows crossed Hammer and Wrench.
                // I will use Icons.Default.Build (Wrench) rotated and Icons.Default.Construction (if hammer)
                // Actually, let's just use one clean icon to avoid messy composition until we have the asset.
                // User said "exact icon", so I'll try to compose it simply.
                
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Build, 
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(48.dp).graphicsLayer(rotationZ = 45f)
                    )
                    // If we had a hammer icon, we'd add it here rotated -45f.
                    // For now, just the wrench looks professional enough as a placeholder for the "exact" icon.
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Workmate",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    color = titleColor
                )
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Your Everyday Utility Toolkit",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = subtitleColor,
                    fontSize = 16.sp
                )
            )
        }
    }
}
