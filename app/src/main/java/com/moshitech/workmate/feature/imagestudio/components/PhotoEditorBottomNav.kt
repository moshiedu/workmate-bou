package com.moshitech.workmate.feature.imagestudio.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
    CROP, FILTERS, ROTATE, ADJUST, TEXT, DRAW
}

@Composable
fun PhotoEditorBottomNav(
    selectedTool: EditorTool,
    onToolSelected: (EditorTool) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color(0xFF1C1C1E), // Dark background matching design
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Crop
            BottomNavItem(
                icon = Icons.Outlined.Crop,
                label = "Crop",
                isSelected = selectedTool == EditorTool.CROP,
                onClick = { onToolSelected(EditorTool.CROP) }
            )
            
            // Filters
            BottomNavItem(
                icon = Icons.Outlined.FilterVintage,
                label = "Filters",
                isSelected = selectedTool == EditorTool.FILTERS,
                onClick = { onToolSelected(EditorTool.FILTERS) }
            )
            
            // Rotate
            BottomNavItem(
                icon = Icons.Outlined.RotateRight,
                label = "Rotate",
                isSelected = selectedTool == EditorTool.ROTATE,
                onClick = { onToolSelected(EditorTool.ROTATE) }
            )
            
            // Adjust
            BottomNavItem(
                icon = Icons.Outlined.Tune,
                label = "Adjust",
                isSelected = selectedTool == EditorTool.ADJUST,
                onClick = { onToolSelected(EditorTool.ADJUST) }
            )
            
            // Text (T)
            BottomNavItem(
                icon = Icons.Outlined.TextFields,
                label = "Text",
                customLabel = "T",
                isSelected = selectedTool == EditorTool.TEXT,
                onClick = { onToolSelected(EditorTool.TEXT) }
            )
            
            // Draw
            BottomNavItem(
                icon = Icons.Outlined.Brush,
                label = "Draw",
                isSelected = selectedTool == EditorTool.DRAW,
                onClick = { onToolSelected(EditorTool.DRAW) }
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
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Icon with blue circle background if selected
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    if (isSelected) Color(0xFF007AFF) else Color.Transparent,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (customLabel != null) {
                // Show "T" text instead of icon
                Text(
                    text = customLabel,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        // Label
        Text(
            text = label,
            color = if (isSelected) Color.White else Color.Gray,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
        )
    }
}
