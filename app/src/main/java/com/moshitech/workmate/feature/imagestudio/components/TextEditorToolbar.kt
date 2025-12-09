package com.moshitech.workmate.feature.imagestudio.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moshitech.workmate.feature.imagestudio.viewmodel.TextLayer

enum class TextToolTab {
    FONT, STYLE, FORMAT, COLOR, EFFECTS
}

@Composable
fun TextEditorToolbar(
    layer: TextLayer,
    visible: Boolean,
    onUpdate: (TextLayer) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!visible) return
    
    var selectedTab by remember { mutableStateOf(TextToolTab.FONT) }
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color(0xFF1C1C1E),
        tonalElevation = 8.dp
    ) {
        Column {
            // Tab Row with underline indicator
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextToolTab.values().forEach { tab ->
                        Text(
                            text = tab.name.lowercase().replaceFirstChar { it.uppercase() },
                            color = if (selectedTab == tab) Color.White else Color.Gray,
                            fontSize = 15.sp,
                            fontWeight = if (selectedTab == tab) FontWeight.Medium else FontWeight.Normal,
                            modifier = Modifier
                                .clickable { selectedTab = tab }
                                .padding(vertical = 4.dp)
                        )
                    }
                }
                
                // Blue underline indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextToolTab.values().forEach { tab ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(2.dp)
                                .background(
                                    if (selectedTab == tab) Color(0xFF007AFF) else Color.Transparent
                                )
                        )
                    }
                }
            }
            
            // Tab Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .padding(16.dp)
            ) {
                when (selectedTab) {
                    TextToolTab.FONT -> FontTabContent(layer, onUpdate)
                    TextToolTab.STYLE -> StyleTabContent(layer, onUpdate)
                    TextToolTab.FORMAT -> FormatTabContent(layer, onUpdate)
                    TextToolTab.COLOR -> ColorTabContent(layer, onUpdate)
                    TextToolTab.EFFECTS -> EffectsTabContent(layer, onUpdate)
                }
            }
        }
    }
}

@Composable
fun FontTabContent(layer: TextLayer, onUpdate: (TextLayer) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Font Size
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Font Size", color = Color.White, fontSize = 14.sp)
            Text("${layer.fontSize.toInt()}pt", color = Color(0xFF007AFF), fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
        Slider(
            value = layer.fontSize,
            onValueChange = { onUpdate(layer.copy(fontSize = it)) },
            valueRange = 10f..72f,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF007AFF),
                activeTrackColor = Color(0xFF007AFF),
                inactiveTrackColor = Color(0xFF3A3A3C)
            )
        )
        
        // Letter Spacing
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Letter Spacing", color = Color.White, fontSize = 14.sp)
        }
        Slider(
            value = layer.letterSpacing,
            onValueChange = { onUpdate(layer.copy(letterSpacing = it)) },
            valueRange = -5f..10f,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF007AFF),
                activeTrackColor = Color(0xFF007AFF),
                inactiveTrackColor = Color(0xFF3A3A3C)
            )
        )
    }
}

@Composable
fun StyleTabContent(layer: TextLayer, onUpdate: (TextLayer) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Row 1: B, I, U, Strikethrough
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StyleToggleButton(
                text = "B",
                isSelected = layer.isBold,
                fontWeight = FontWeight.Bold,
                onClick = { onUpdate(layer.copy(isBold = !layer.isBold)) }
            )
            StyleToggleButton(
                text = "I",
                isSelected = layer.isItalic,
                onClick = { onUpdate(layer.copy(isItalic = !layer.isItalic)) }
            )
            StyleToggleButton(
                text = "U",
                isSelected = layer.isUnderline,
                textDecoration = TextDecoration.Underline,
                onClick = { onUpdate(layer.copy(isUnderline = !layer.isUnderline)) }
            )
            StyleToggleButton(
                text = "S",
                isSelected = layer.isStrikethrough,
                textDecoration = TextDecoration.LineThrough,
                onClick = { onUpdate(layer.copy(isStrikethrough = !layer.isStrikethrough)) }
            )
        }
        
        // Row 2: All Caps, Small Caps
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CapsButton(
                text = "Tt  All Caps",
                isSelected = layer.isAllCaps,
                onClick = { onUpdate(layer.copy(isAllCaps = !layer.isAllCaps)) },
                modifier = Modifier.weight(1f)
            )
            CapsButton(
                text = "aA  Small Caps",
                isSelected = layer.isSmallCaps,
                onClick = { onUpdate(layer.copy(isSmallCaps = !layer.isSmallCaps)) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StyleToggleButton(
    text: String,
    isSelected: Boolean,
    fontWeight: FontWeight = FontWeight.Normal,
    textDecoration: TextDecoration = TextDecoration.None,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .background(
                if (isSelected) Color(0xFF3A3A3C) else Color(0xFF2C2C2C),
                RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = fontWeight,
            textDecoration = textDecoration
        )
    }
}

@Composable
fun CapsButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .background(
                if (isSelected) Color(0xFF3A3A3C) else Color(0xFF2C2C2C),
                RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 13.sp
        )
    }
}

@Composable
fun FormatTabContent(layer: TextLayer, onUpdate: (TextLayer) -> Unit) {
    Text("Format options", color = Color.White)
}

@Composable
fun ColorTabContent(layer: TextLayer, onUpdate: (TextLayer) -> Unit) {
    Text("Color options", color = Color.White)
}

@Composable
fun EffectsTabContent(layer: TextLayer, onUpdate: (TextLayer) -> Unit) {
    Text("Effects options", color = Color.White)
}
