package com.moshitech.workmate.feature.imagestudio.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.moshitech.workmate.feature.imagestudio.components.AdContainer
import com.moshitech.workmate.feature.imagestudio.ui.PhotoGalleryScreen

@Composable
fun ImageStudioNavigator(
    navController: NavController
) {
    AdContainer(modifier = Modifier.fillMaxSize()) {
        PhotoGalleryScreen(navController = navController)
    }
}


