package com.moshitech.workmate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.moshitech.workmate.navigation.WorkmateNavigation
import com.moshitech.workmate.ui.theme.WorkmateTheme

import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.rememberNavController
import com.moshitech.workmate.data.repository.AppTheme
import com.moshitech.workmate.MainViewModel

class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val theme by mainViewModel.theme.collectAsState()
            val isDarkTheme = when (theme) {
                AppTheme.LIGHT -> false
                AppTheme.DARK -> true
                AppTheme.SYSTEM -> isSystemInDarkTheme()
            }

            // Quick Share Handling
            val startDestination = androidx.compose.runtime.remember {
                var route = com.moshitech.workmate.navigation.Screen.Splash.route
                val action = intent?.action
                val type = intent?.type

                if (android.content.Intent.ACTION_SEND == action && type?.startsWith("image/") == true) {
                    val uri = if (android.os.Build.VERSION.SDK_INT >= 33) {
                        intent.getParcelableExtra(android.content.Intent.EXTRA_STREAM, android.net.Uri::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra<android.net.Uri>(android.content.Intent.EXTRA_STREAM)
                    }
                    if (uri != null) {
                         mainViewModel.setSharedUris(listOf(uri))
                         route = com.moshitech.workmate.navigation.Screen.BatchConverter.route
                    }
                } else if (android.content.Intent.ACTION_SEND_MULTIPLE == action && type?.startsWith("image/") == true) {
                    val uris = if (android.os.Build.VERSION.SDK_INT >= 33) {
                         intent.getParcelableArrayListExtra(android.content.Intent.EXTRA_STREAM, android.net.Uri::class.java)
                    } else {
                         @Suppress("DEPRECATION")
                         intent.getParcelableArrayListExtra<android.net.Uri>(android.content.Intent.EXTRA_STREAM)
                    }
                    if (!uris.isNullOrEmpty()) {
                         mainViewModel.setSharedUris(uris)
                         route = com.moshitech.workmate.navigation.Screen.BatchConverter.route
                    }
                }
                route
            }

            WorkmateTheme(darkTheme = isDarkTheme) {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    WorkmateNavigation(
                        navController = rememberNavController(),
                        mainViewModel = mainViewModel,
                        startDestination = startDestination
                    )
                }
            }
        }
    }
}