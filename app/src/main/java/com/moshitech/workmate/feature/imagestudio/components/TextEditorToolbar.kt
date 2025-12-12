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
import androidx.compose.ui.text.font.FontStyle
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
    
    var selectedTab by remember { mutableStateOf(TextToolTab.STYLE) }
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color(0xFF0D121F), // Deep Dark Blue/Black
        tonalElevation = 0.dp
    ) {
        Column {
            // Tab Row
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextToolTab.values().forEach { tab ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedTab = tab }
                        ) {
                            Text(
                                text = tab.name.lowercase().replaceFirstChar { it.uppercase() },
                                color = if (selectedTab == tab) Color(0xFF007AFF) else Color.Gray, // Blue text when selected
                                fontSize = 15.sp,
                                fontWeight = if (selectedTab == tab) FontWeight.SemiBold else FontWeight.Normal,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            // Blue underline indicator
                            Box(
                                modifier = Modifier
                                    .width(40.dp) // Fixed width indicator (not full cell)
                                    .height(2.dp)
                                    .background(
                                        if (selectedTab == tab) Color(0xFF007AFF) else Color.Transparent
                                    )
                            )
                        }
                    }
                }
                
                // Grey Separator Line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0xFF2C2C2E))
                )
            }
            
            // Tab Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight() // Dynamic height
                    .padding(16.dp),
                contentAlignment = Alignment.TopCenter
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
        modifier = Modifier.fillMaxWidth(),
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
        modifier = Modifier.fillMaxWidth(), // Use fillMaxWidth instead of fillMaxSize inside wrapContent parent
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Row 1: B, I, U, Strikethrough
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp), // Add horizontal padding
            horizontalArrangement = Arrangement.SpaceBetween, // Spread out evenly
            verticalAlignment = Alignment.CenterVertically
        ) {
            val buttonSpacing = 0.dp // Not used with SpaceBetween
            StyleToggleButton(
                text = "B",
                isSelected = layer.isBold,
                fontWeight = FontWeight.Bold,
                onClick = { onUpdate(layer.copy(isBold = !layer.isBold)) }
            )
            StyleToggleButton(
                text = "I",
                isSelected = layer.isItalic,
                fontStyle = FontStyle.Italic,
                onClick = { onUpdate(layer.copy(isItalic = !layer.isItalic)) }
            )
            StyleToggleButton(
                text = "U",
                isSelected = layer.isUnderline,
                textDecoration = TextDecoration.Underline,
                onClick = { onUpdate(layer.copy(isUnderline = !layer.isUnderline)) }
            )
            // Strikethrough - using Text with LineThrough
            StyleToggleButton(
                text = "S", // Or specific icon if available
                isSelected = layer.isStrikethrough,
                textDecoration = TextDecoration.LineThrough,
                onClick = { onUpdate(layer.copy(isStrikethrough = !layer.isStrikethrough)) }
            )
        }
        
        // Row 2: All Caps, Small Caps
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CapsButton(
                iconText = "TT",
                label = "All Caps",
                isSelected = layer.isAllCaps,
                onClick = { onUpdate(layer.copy(isAllCaps = !layer.isAllCaps)) },
                modifier = Modifier.weight(1f)
            )
            CapsButton(
                iconText = "aA", // Visual approximation
                label = "Small Caps",
                isSelected = layer.isSmallCaps,
                onClick = { onUpdate(layer.copy(isSmallCaps = !layer.isSmallCaps)) },
                modifier = Modifier.weight(1f)
            )
        }
        
        // Row 3: Alignment (Left, Center, Right, Justify)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconStyleToggleButton(
                icon = Icons.Default.FormatAlignLeft,
                isSelected = layer.alignment == com.moshitech.workmate.feature.imagestudio.viewmodel.TextAlignment.LEFT,
                onClick = { onUpdate(layer.copy(alignment = com.moshitech.workmate.feature.imagestudio.viewmodel.TextAlignment.LEFT)) }
            )
            IconStyleToggleButton(
                icon = Icons.Default.FormatAlignCenter,
                isSelected = layer.alignment == com.moshitech.workmate.feature.imagestudio.viewmodel.TextAlignment.CENTER,
                onClick = { onUpdate(layer.copy(alignment = com.moshitech.workmate.feature.imagestudio.viewmodel.TextAlignment.CENTER)) }
            )
            IconStyleToggleButton(
                icon = Icons.Default.FormatAlignRight,
                isSelected = layer.alignment == com.moshitech.workmate.feature.imagestudio.viewmodel.TextAlignment.RIGHT,
                onClick = { onUpdate(layer.copy(alignment = com.moshitech.workmate.feature.imagestudio.viewmodel.TextAlignment.RIGHT)) }
            )
            IconStyleToggleButton(
                icon = Icons.Default.FormatAlignJustify,
                isSelected = layer.alignment == com.moshitech.workmate.feature.imagestudio.viewmodel.TextAlignment.JUSTIFY,
                onClick = { onUpdate(layer.copy(alignment = com.moshitech.workmate.feature.imagestudio.viewmodel.TextAlignment.JUSTIFY)) }
            )
        }
    }
}

@Composable
fun IconStyleToggleButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) Color(0xFF334155) else Color.Transparent
    val contentColor = Color.White
    
    Box(
        modifier = Modifier
            .size(width = 60.dp, height = 50.dp)
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun StyleToggleButton(
    text: String,
    isSelected: Boolean,
    fontWeight: FontWeight = FontWeight.Normal,
    fontStyle: FontStyle = FontStyle.Normal,
    textDecoration: TextDecoration = TextDecoration.None,
    onClick: () -> Unit
) {
    // Only background if selected
    val backgroundColor = if (isSelected) Color(0xFF334155) else Color.Transparent // Dark Slate Blue vs Transparent
    val contentColor = Color.White // Always white/light
    
    Box(
        modifier = Modifier
            .size(width = 60.dp, height = 50.dp) // Rectangular shape as per image
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = contentColor,
            fontSize = 20.sp,
            fontWeight = fontWeight,
            fontStyle = fontStyle,
            textDecoration = textDecoration
        )
    }
}

@Composable
fun CapsButton(
    iconText: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) Color(0xFF334155) else Color.Transparent
    val contentColor = if (isSelected) Color.White else Color.Gray

    Box(
        modifier = modifier
            .height(50.dp)
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
             Text(
                text = iconText,
                color = if (isSelected) Color.White else Color(0xFF909399), // Muted when not selected
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(
                text = label,
                color = contentColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
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
