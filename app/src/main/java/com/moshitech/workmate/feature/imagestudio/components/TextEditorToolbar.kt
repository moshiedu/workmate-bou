package com.moshitech.workmate.feature.imagestudio.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
        verticalArrangement = Arrangement.spacedBy(10.dp), // Reduced spacing
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Row 1: B, I, U, Strikethrough
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp), // Reduced horizontal padding
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
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), // Match others
            horizontalArrangement = Arrangement.spacedBy(8.dp) // Reduced spacing
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
                .padding(horizontal = 16.dp), // Reduced padding
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
            .size(width = 48.dp, height = 40.dp) // Reduced size
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
            .size(width = 48.dp, height = 40.dp) // Reduced size
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
    var colorMode by remember { mutableStateOf(ColorMode.TEXT) }
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Mode Selector (Text | Background)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .padding(horizontal = 16.dp)
                .background(Color(0xFF2C2C2E), RoundedCornerShape(8.dp))
                .padding(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Text Mode Button
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        if (colorMode == ColorMode.TEXT) Color(0xFF334155) else Color.Transparent,
                        RoundedCornerShape(6.dp)
                    )
                    .clickable { colorMode = ColorMode.TEXT },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Text",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = if (colorMode == ColorMode.TEXT) FontWeight.SemiBold else FontWeight.Medium
                )
            }
            
            // Background Mode Button
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        if (colorMode == ColorMode.BACKGROUND) Color(0xFF334155) else Color.Transparent,
                        RoundedCornerShape(6.dp)
                    )
                    .clickable { colorMode = ColorMode.BACKGROUND },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Background",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = if (colorMode == ColorMode.BACKGROUND) FontWeight.SemiBold else FontWeight.Medium
                )
            }
        }
        
        // Color Palette
        val colors = listOf(
            Color.White, Color.Black, Color(0xFFE91E63), Color(0xFFF44336), 
            Color(0xFFFF9800), Color(0xFFFFEB3B), Color(0xFF4CAF50), Color(0xFF009688), 
            Color(0xFF2196F3), Color(0xFF3F51B5), Color(0xFF9C27B0), Color(0xFF673AB7),
            Color(0xFF795548), Color(0xFF607D8B)
        )
        
        androidx.compose.foundation.lazy.LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // No Color Option (Only for Background)
            if (colorMode == ColorMode.BACKGROUND) {
                item {
                    ColorCircle(
                        color = Color.Transparent,
                        isSelected = !layer.showBackground,
                        isNoColor = true,
                        onClick = { 
                            onUpdate(layer.copy(showBackground = false)) 
                        }
                    )
                }
            }
            
            items(colors.size) { index ->
                val color = colors[index]
                val isSelected = if (colorMode == ColorMode.TEXT) {
                    layer.color == color.toArgb()
                } else {
                    layer.showBackground && layer.backgroundColor == color.toArgb()
                }
                
                ColorCircle(
                    color = color,
                    isSelected = isSelected,
                    onClick = {
                        if (colorMode == ColorMode.TEXT) {
                            onUpdate(layer.copy(color = color.toArgb()))
                        } else {
                            onUpdate(layer.copy(
                                showBackground = true,
                                backgroundColor = color.toArgb()
                            ))
                        }
                    }
                )
            }
        }

        // Advanced Sliders
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val currentColorInt = if (colorMode == ColorMode.TEXT) layer.color else layer.backgroundColor
            val currentColor = Color(currentColorInt)
            
            // Spectrum Slider (Hue)
            SpectrumSlider(
                selectedColor = currentColor,
                onColorSelected = { newColor ->
                    // Preserve alpha of current selection
                    val alpha = if (colorMode == ColorMode.TEXT) Color(layer.color).alpha else Color(layer.backgroundColor).alpha
                    val finalColor = newColor.copy(alpha = alpha)
                    
                    if (colorMode == ColorMode.TEXT) {
                        onUpdate(layer.copy(color = finalColor.toArgb()))
                    } else {
                        onUpdate(layer.copy(
                            showBackground = true,
                            backgroundColor = finalColor.toArgb()
                        ))
                    }
                }
            )
            
            // Opacity Slider
            OpacitySlider(
                color = currentColor,
                alpha = currentColor.alpha,
                onAlphaChanged = { newAlpha ->
                    val finalColor = currentColor.copy(alpha = newAlpha)
                    if (colorMode == ColorMode.TEXT) {
                        onUpdate(layer.copy(color = finalColor.toArgb()))
                    } else {
                        onUpdate(layer.copy(
                            showBackground = true,
                            backgroundColor = finalColor.toArgb()
                        ))
                    }
                }
            )
        }
    }
}

enum class ColorMode { TEXT, BACKGROUND }

@Composable
fun ColorCircle(
    color: Color,
    isSelected: Boolean,
    isNoColor: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp) // Compact size
            .background(
                if (isNoColor) Color.Transparent else color,
                CircleShape
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) Color(0xFF007AFF) else if (isNoColor) Color.Gray else Color.Transparent,
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isNoColor) {
            Icon(
                imageVector = Icons.Default.Block,
                contentDescription = "No Color",
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun SpectrumSlider(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(20.dp)
            .clip(RoundedCornerShape(10.dp))
    ) {
        val width = maxWidth
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red
                        )
                    )
                )
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        if (size.width > 0) {
                            val hue = (offset.x / size.width.toFloat()).coerceIn(0f, 1f) * 360f
                            val color = android.graphics.Color.HSVToColor(floatArrayOf(hue, 1f, 1f))
                            onColorSelected(Color(color))
                        }
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        if (size.width > 0) {
                            val hue = (change.position.x.coerceIn(0f, size.width.toFloat()) / size.width.toFloat()) * 360f
                            val color = android.graphics.Color.HSVToColor(floatArrayOf(hue, 1f, 1f))
                            onColorSelected(Color(color))
                        }
                    }
                }
        )
    }
}

@Composable
fun OpacitySlider(
    color: Color,
    alpha: Float,
    onAlphaChanged: (Float) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth().height(30.dp),
        contentAlignment = Alignment.Center
    ) {
        val width = maxWidth
        val widthPx = constraints.maxWidth.toFloat()
        
        // Track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.LightGray) // Checkerboard placeholder
        ) {
             Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(color.copy(alpha = 0f), color.copy(alpha = 1f))
                        )
                    )
            )
        }
        
        // Interaction Area (Invisible but captures touches)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        if (size.width > 0) {
                            val newAlpha = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                            onAlphaChanged(newAlpha)
                        }
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        if (size.width > 0) {
                            val newAlpha = (change.position.x.coerceIn(0f, size.width.toFloat()) / size.width.toFloat()).coerceIn(0f, 1f)
                            onAlphaChanged(newAlpha)
                        }
                    }
                }
        )
        
        // Thumb - Manually calculated offset
        val thumbOffset = if (widthPx > 0) width * alpha else 0.dp
        // Adjust to center thumb on the point (subtract half thumb size, but clamp?)
        // Simple visual approximation: 
        val constrainedOffset = (width - 20.dp) * alpha // Keep inside bounds
        
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = constrainedOffset)
                .size(20.dp)
                .background(Color.White, CircleShape)
                .border(2.dp, Color(0xFF007AFF), CircleShape)
                .shadow(2.dp, CircleShape)
        )
    }
}

@Composable
fun EffectsTabContent(layer: TextLayer, onUpdate: (TextLayer) -> Unit) {
    Text("Effects options", color = Color.White)
}
