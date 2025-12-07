package com.moshitech.workmate.feature.imagestudio.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.moshitech.workmate.feature.imagestudio.util.MonetizationManager

@Composable
fun AdContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val isPro by MonetizationManager.isPro.collectAsState()

    Column(modifier = modifier) {
        // Main Content Area
        Box(modifier = Modifier.weight(1f)) {
            content()
        }

        // Warning: Do not show ad if Pro
        if (!isPro) {
            BannerAdPlaceholder()
        }
    }
}

@Composable
private fun BannerAdPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp) // Standard Banner Height
            .background(Color.Gray.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Ad Banner Area",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
    }
}
