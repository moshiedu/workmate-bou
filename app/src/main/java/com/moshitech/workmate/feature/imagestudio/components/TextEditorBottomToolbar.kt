package com.moshitech.workmate.feature.imagestudio.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import com.moshitech.workmate.feature.imagestudio.viewmodel.*

enum class TextEditorTab {
    FONT, STYLE, FORMAT, COLOR, EFFECTS
}

@Composable
fun TextEditorBottomToolbar(
    layer: TextLayer,
    visible: Boolean,
    onUpdate: (TextLayer) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!visible) return
    
    var selectedTab by remember { mutableStateOf(TextEditorTab.STYLE) }
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color(0xFF1E1E1E),
        tonalElevation = 8.dp
    ) {
        Column {
            // Tab Row
            ScrollableTabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = Color(0xFF1E1E1E),
                contentColor = Color.White,
                edgePadding = 0.dp
            ) {
                TextEditorTab.values().forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = {
                            Text(
                                tab.name.lowercase().replaceFirstChar { it.uppercase() },
                                fontSize = 14.sp,
                                fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }
            
            Divider(color = Color.Gray.copy(alpha = 0.2f))
            
            // Tab Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .padding(16.dp)
            ) {
                when (selectedTab) {
                    TextEditorTab.FONT -> FontTab(layer, onUpdate)
                    TextEditorTab.STYLE -> StyleTab(layer, onUpdate)
                    TextEditorTab.FORMAT -> FormatTab(layer, onUpdate)
                    TextEditorTab.COLOR -> ColorTab(layer, onUpdate)
                    TextEditorTab.EFFECTS -> EffectsTab(layer, onUpdate)
                }
            }
        }
    }
}

@Composable
fun FontTab(layer: TextLayer, onUpdate: (TextLayer) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Font Size
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Font Size", color = Color.White, fontSize = 12.sp)
            Text("${layer.fontSize.toInt()}pt", color = Color(0xFF2196F3), fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = layer.fontSize,
            onValueChange = { onUpdate(layer.copy(fontSize = it)) },
            valueRange = 10f..72f,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF2196F3),
                activeTrackColor = Color(0xFF2196F3)
            )
        )
        
        // Letter Spacing
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Letter Spacing", color = Color.White, fontSize = 12.sp)
            Text("${layer.letterSpacing.toInt()}", color = Color(0xFF2196F3), fontSize = 14.sp)
        }
        Slider(
            value = layer.letterSpacing,
            onValueChange = { onUpdate(layer.copy(letterSpacing = it)) },
            valueRange = -5f..10f,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF2196F3),
                activeTrackColor = Color(0xFF2196F3)
            )
        )
        
        // Line Spacing
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Line Spacing", color = Color.White, fontSize = 12.sp)
            Text("${String.format("%.1f", layer.lineHeight)}", color = Color(0xFF2196F3), fontSize = 14.sp)
        }
        Slider(
            value = layer.lineHeight,
            onValueChange = { onUpdate(layer.copy(lineHeight = it)) },
            valueRange = 0.8f..3f,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF2196F3),
                activeTrackColor = Color(0xFF2196F3)
            )
        )
    }
}

@Composable
fun StyleTab(layer: TextLayer, onUpdate: (TextLayer) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Row 1: B, I, U, S
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StyleButton("B", layer.isBold, FontWeight.Bold) {
                onUpdate(layer.copy(isBold = !layer.isBold))
            }
            StyleButton("I", layer.isItalic, textDecoration = TextDecoration.None) {
                onUpdate(layer.copy(isItalic = !layer.isItalic))
            }
            StyleButton("U", layer.isUnderline, textDecoration = TextDecoration.Underline) {
                onUpdate(layer.copy(isUnderline = !layer.isUnderline))
            }
            StyleButton("S", layer.isStrikethrough, textDecoration = TextDecoration.LineThrough) {
                onUpdate(layer.copy(isStrikethrough = !layer.isStrikethrough))
            }
        }
        
        // Row 2: All Caps, Small Caps
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ToggleButton(
                text = "All Caps",
                isSelected = layer.isAllCaps,
                onClick = { onUpdate(layer.copy(isAllCaps = !layer.isAllCaps)) }
            )
            ToggleButton(
                text = "Small Caps",
                isSelected = layer.isSmallCaps,
                onClick = { onUpdate(layer.copy(isSmallCaps = !layer.isSmallCaps)) }
            )
        }
    }
}

@Composable
fun FormatTab(layer: TextLayer, onUpdate: (TextLayer) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Horizontal Alignment
        Text("Alignment", color = Color.White, fontSize = 12.sp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            AlignButton(Icons.Default.FormatAlignLeft, layer.alignment == TextAlignment.LEFT) {
                onUpdate(layer.copy(alignment = TextAlignment.LEFT))
            }
            AlignButton(Icons.Default.FormatAlignCenter, layer.alignment == TextAlignment.CENTER) {
                onUpdate(layer.copy(alignment = TextAlignment.CENTER))
            }
            AlignButton(Icons.Default.FormatAlignRight, layer.alignment == TextAlignment.RIGHT) {
                onUpdate(layer.copy(alignment = TextAlignment.RIGHT))
            }
            AlignButton(Icons.Default.FormatAlignJustify, layer.alignment == TextAlignment.JUSTIFY) {
                onUpdate(layer.copy(alignment = TextAlignment.JUSTIFY))
            }
        }
        
        // Vertical Alignment
        Text("Vertical", color = Color.White, fontSize = 12.sp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ToggleButton("Top", layer.verticalAlignment == VerticalAlignment.TOP) {
                onUpdate(layer.copy(verticalAlignment = VerticalAlignment.TOP))
            }
            ToggleButton("Middle", layer.verticalAlignment == VerticalAlignment.MIDDLE) {
                onUpdate(layer.copy(verticalAlignment = VerticalAlignment.MIDDLE))
            }
            ToggleButton("Bottom", layer.verticalAlignment == VerticalAlignment.BOTTOM) {
                onUpdate(layer.copy(verticalAlignment = VerticalAlignment.BOTTOM))
            }
        }
    }
}

@Composable
fun ColorTab(layer: TextLayer, onUpdate: (TextLayer) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Text Color
        Text("Text Color", color = Color.White, fontSize = 12.sp)
        ColorPalette(layer.color) { onUpdate(layer.copy(color = it)) }
        
        Spacer(Modifier.height(8.dp))
        
        // Background Color
        Text("Background Color", color = Color.White, fontSize = 12.sp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Transparent option
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .border(2.dp, if (!layer.showBackground) Color(0xFF2196F3) else Color.Gray, RoundedCornerShape(8.dp))
                    .clickable { onUpdate(layer.copy(showBackground = false)) },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Close, null, tint = Color.Red)
            }
            
            ColorPalette(layer.backgroundColor, compact = true) {
                onUpdate(layer.copy(backgroundColor = it, showBackground = true))
            }
        }
        
        // Background Opacity
        if (layer.showBackground) {
            Text("Background Opacity", color = Color.White, fontSize = 12.sp)
            Slider(
                value = layer.backgroundOpacity,
                onValueChange = { onUpdate(layer.copy(backgroundOpacity = it)) },
                valueRange = 0f..1f,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF2196F3),
                    activeTrackColor = Color(0xFF2196F3)
                )
            )
        }
    }
}

@Composable
fun EffectsTab(layer: TextLayer, onUpdate: (TextLayer) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Shadow
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Shadow", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Switch(
                checked = layer.hasShadow,
                onCheckedChange = { onUpdate(layer.copy(hasShadow = it)) },
                colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF2196F3))
            )
        }
        
        if (layer.hasShadow) {
            Text("Blur: ${layer.shadowBlur.toInt()}", color = Color.White, fontSize = 11.sp)
            Slider(
                value = layer.shadowBlur,
                onValueChange = { onUpdate(layer.copy(shadowBlur = it)) },
                valueRange = 0f..20f,
                colors = SliderDefaults.colors(thumbColor = Color(0xFF2196F3), activeTrackColor = Color(0xFF2196F3))
            )
            
            Text("Offset X: ${layer.shadowOffsetX.toInt()}", color = Color.White, fontSize = 11.sp)
            Slider(
                value = layer.shadowOffsetX,
                onValueChange = { onUpdate(layer.copy(shadowOffsetX = it)) },
                valueRange = -20f..20f,
                colors = SliderDefaults.colors(thumbColor = Color(0xFF2196F3), activeTrackColor = Color(0xFF2196F3))
            )
            
            Text("Offset Y: ${layer.shadowOffsetY.toInt()}", color = Color.White, fontSize = 11.sp)
            Slider(
                value = layer.shadowOffsetY,
                onValueChange = { onUpdate(layer.copy(shadowOffsetY = it)) },
                valueRange = -20f..20f,
                colors = SliderDefaults.colors(thumbColor = Color(0xFF2196F3), activeTrackColor = Color(0xFF2196F3))
            )
        }
        
        Divider(color = Color.Gray.copy(alpha = 0.3f))
        
        // Outline
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Outline", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Switch(
                checked = layer.hasOutline,
                onCheckedChange = { onUpdate(layer.copy(hasOutline = it)) },
                colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF2196F3))
            )
        }
        
        if (layer.hasOutline) {
            Text("Thickness: ${layer.outlineThickness.toInt()}", color = Color.White, fontSize = 11.sp)
            Slider(
                value = layer.outlineThickness,
                onValueChange = { onUpdate(layer.copy(outlineThickness = it)) },
                valueRange = 1f..10f,
                colors = SliderDefaults.colors(thumbColor = Color(0xFF2196F3), activeTrackColor = Color(0xFF2196F3))
            )
        }
        
        Divider(color = Color.Gray.copy(alpha = 0.3f))
        
        // Layer Opacity
        Text("Layer Opacity: ${(layer.layerOpacity * 100).toInt()}%", color = Color.White, fontSize = 12.sp)
        Slider(
            value = layer.layerOpacity,
            onValueChange = { onUpdate(layer.copy(layerOpacity = it)) },
            valueRange = 0f..1f,
            colors = SliderDefaults.colors(thumbColor = Color(0xFF2196F3), activeTrackColor = Color(0xFF2196F3))
        )
    }
}

@Composable
fun StyleButton(
    text: String,
    isSelected: Boolean,
    fontWeight: FontWeight = FontWeight.Normal,
    textDecoration: TextDecoration = TextDecoration.None,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .background(
                if (isSelected) Color(0xFF2196F3) else Color(0xFF2C2C2C),
                RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = fontWeight,
            textDecoration = textDecoration
        )
    }
}

@Composable
fun ToggleButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFF2196F3) else Color(0xFF2C2C2C)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(text, fontSize = 12.sp)
    }
}

@Composable
fun AlignButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(48.dp)
            .background(
                if (isSelected) Color(0xFF2196F3) else Color(0xFF2C2C2C),
                RoundedCornerShape(8.dp)
            )
    ) {
        Icon(icon, null, tint = Color.White)
    }
}

@Composable
fun ColorPalette(selectedColor: Int, compact: Boolean = false, onSelect: (Int) -> Unit) {
    val colors = listOf(
        0xFFFFFFFF, 0xFF000000, 0xFFFF0000, 0xFF00FF00, 0xFF0000FF,
        0xFFFFFF00, 0xFF00FFFF, 0xFFFF00FF, 0xFFFFA500, 0xFF800080
    )
    
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        colors.forEach { color ->
            Box(
                modifier = Modifier
                    .size(if (compact) 36.dp else 40.dp)
                    .background(Color(color), CircleShape)
                    .border(
                        if (color.toInt() == selectedColor) 3.dp else 1.dp,
                        if (color.toInt() == selectedColor) Color(0xFF2196F3) else Color.Gray,
                        CircleShape
                    )
                    .clickable { onSelect(color.toInt()) }
            )
        }
    }
}
