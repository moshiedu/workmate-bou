package com.moshitech.workmate.feature.imagestudio.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class EditorTool {
    NONE, CROP, FILTERS, STICKERS, SHAPES, ROTATE, ADJUST, TEXT, DRAW
}

@Composable
fun PhotoEditorBottomNav(
    selectedTool: EditorTool,
    onToolSelected: (EditorTool) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = Color(0xFF2C2C2E)), // Add Top Border (using full border for simplicity or drawBehind for top only)
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()) // Make scrollable
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly, // This behaves like 'Start' when scrolling, which is fine, or we can use generic Arrangement.spacedBy
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Crop
            BottomNavItem(
                icon = Icons.Outlined.Crop,
                label = "Crop",
                isSelected = selectedTool == EditorTool.CROP,
                onClick = { onToolSelected(EditorTool.CROP) }
            )

            // 2. Rotate
            BottomNavItem(
                icon = Icons.Outlined.RotateRight,
                label = "Rotate",
                isSelected = selectedTool == EditorTool.ROTATE,
                onClick = { onToolSelected(EditorTool.ROTATE) }
            )
            
            // 3. Filters
            BottomNavItem(
                icon = Icons.Outlined.FilterVintage,
                label = "Filters",
                isSelected = selectedTool == EditorTool.FILTERS,
                onClick = { onToolSelected(EditorTool.FILTERS) }
            )

            // 4. Adjust
            BottomNavItem(
                icon = Icons.Outlined.Tune,
                label = "Adjust",
                isSelected = selectedTool == EditorTool.ADJUST,
                onClick = { onToolSelected(EditorTool.ADJUST) }
            )
            
            // 5. Draw
            BottomNavItem(
                icon = Icons.Outlined.Brush,
                label = "Draw",
                isSelected = selectedTool == EditorTool.DRAW,
                onClick = { onToolSelected(EditorTool.DRAW) }
            )

            // 6. Shapes
            BottomNavItem(
                icon = Icons.Outlined.Category, // Use generic shape icon (Category fits shapes well)
                label = "Shapes",
                isSelected = selectedTool == EditorTool.SHAPES,
                onClick = { onToolSelected(EditorTool.SHAPES) }
            )

            // 7. Stickers
            BottomNavItem(
                icon = Icons.Outlined.EmojiEmotions,
                label = "Stickers",
                isSelected = selectedTool == EditorTool.STICKERS,
                onClick = { onToolSelected(EditorTool.STICKERS) }
            )
            
            // 8. Text (Coming Soon)
            BottomNavItem(
                icon = Icons.Outlined.TextFields,
                label = "Text",
                customLabel = "T",
                isSelected = selectedTool == EditorTool.TEXT,
                onClick = { onToolSelected(EditorTool.TEXT) },
                isComingSoon = true // New Flag
            )
        }
    }
}

@Composable
fun BottomNavItem(
    icon: ImageVector,
    label: String,
    customLabel: String? = null,
    isSelected: Boolean,
    onClick: () -> Unit,
    isComingSoon: Boolean = false // New Parameter
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 2.dp) // Little more spacing for scroll
            .alpha(if (isComingSoon) 0.5f else 1f), // Visual alpha cue
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val contentColor = if (isSelected) Color(0xFF007AFF) else Color.Gray
        
        // Icon
        if (customLabel != null) {
            Text(
                text = customLabel,
                color = contentColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        } else {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
        }
        
        // Label
        Text(
            text = if(isComingSoon) "$label (Soon)" else label,
            color = contentColor,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
