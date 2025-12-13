package com.moshitech.workmate.feature.imagestudio.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Colorize
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.FormatAlignLeft
import androidx.compose.material.icons.automirrored.filled.FormatAlignRight
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatAlignJustify
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.FormatAlignRight
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import com.moshitech.workmate.feature.imagestudio.viewmodel.TextLayer
import androidx.core.graphics.toColorInt
import kotlin.math.abs

enum class TextToolTab {
    FONT, STYLE, COLOR, EFFECTS
}

@Composable
fun TextEditorToolbar(
    layer: TextLayer,
    visible: Boolean,
    onUpdate: (TextLayer) -> Unit,
    onRequestEyedropper: ((Color) -> Unit) -> Unit,
    onRequestTexturePick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!visible) return
    
    var selectedTab by remember { mutableStateOf(TextToolTab.STYLE) }

    Surface(
        modifier = modifier.fillMaxSize(), // Fill the parent panel
        color = Color(0xFF0D121F), // Deep Dark Blue/Black
        tonalElevation = 0.dp
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Tab Row


            // Tab Row
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 0.dp), // Reduced vertical padding due to handle
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
                    .fillMaxSize() // Fill remaining space in parent
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp), // Compact padding
                contentAlignment = Alignment.TopCenter
            ) {
                when (selectedTab) {
                    TextToolTab.FONT -> FontTabContent(layer, onUpdate)
                    TextToolTab.STYLE -> StyleTabContent(layer, onUpdate)
                    TextToolTab.COLOR -> ColorTabContent(
                        layer = layer,
                        onUpdate = onUpdate,
                        onRequestEyedropper = onRequestEyedropper,
                        onRequestTexturePick = onRequestTexturePick
                    )
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
        verticalArrangement = Arrangement.spacedBy(16.dp) // Gap between sections
    ) {
        // Font Family Selection
        Text("Font Family", color = Color.White, fontSize = 14.sp)
        
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(com.moshitech.workmate.feature.imagestudio.viewmodel.AppFont.values()) { font ->
                val isSelected = layer.fontFamily == font
                val fontFamily = when(font) {
                    com.moshitech.workmate.feature.imagestudio.viewmodel.AppFont.DEFAULT -> androidx.compose.ui.text.font.FontFamily.Default
                    com.moshitech.workmate.feature.imagestudio.viewmodel.AppFont.SERIF -> androidx.compose.ui.text.font.FontFamily.Serif
                    com.moshitech.workmate.feature.imagestudio.viewmodel.AppFont.SANS_SERIF -> androidx.compose.ui.text.font.FontFamily.SansSerif
                    com.moshitech.workmate.feature.imagestudio.viewmodel.AppFont.MONOSPACE -> androidx.compose.ui.text.font.FontFamily.Monospace
                    com.moshitech.workmate.feature.imagestudio.viewmodel.AppFont.CURSIVE -> androidx.compose.ui.text.font.FontFamily.Cursive
                    com.moshitech.workmate.feature.imagestudio.viewmodel.AppFont.LOBSTER -> androidx.compose.ui.text.font.FontFamily(androidx.compose.ui.text.font.Font(com.moshitech.workmate.R.font.lobster))
                    com.moshitech.workmate.feature.imagestudio.viewmodel.AppFont.BANGERS -> androidx.compose.ui.text.font.FontFamily(androidx.compose.ui.text.font.Font(com.moshitech.workmate.R.font.bangers))
                    com.moshitech.workmate.feature.imagestudio.viewmodel.AppFont.OSWALD -> androidx.compose.ui.text.font.FontFamily(androidx.compose.ui.text.font.Font(com.moshitech.workmate.R.font.oswald_medium))
                    com.moshitech.workmate.feature.imagestudio.viewmodel.AppFont.PLAYFAIR -> androidx.compose.ui.text.font.FontFamily(androidx.compose.ui.text.font.Font(com.moshitech.workmate.R.font.playfair_display))
                }
                
                Column(
                    modifier = Modifier
                         .width(80.dp)
                         .clickable { onUpdate(layer.copy(fontFamily = font)) },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color(0xFF007AFF) else Color(0xFF1C1C1E))
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) Color(0xFF007AFF) else Color(0xFF3A3A3C),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Abc",
                            fontSize = 20.sp,
                            fontFamily = fontFamily,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = font.name.lowercase().replace("_", " ").replaceFirstChar { it.uppercase() },
                        fontSize = 11.sp,
                        color = if (isSelected) Color.White else Color.Gray,
                        maxLines = 1
                    )
                }
            }
        }
        
        // Font Size
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
        }
        
        // Letter Spacing
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Letter Spacing", color = Color.White, fontSize = 14.sp)
                Text("${layer.letterSpacing.toInt()}", color = Color(0xFF007AFF), fontSize = 14.sp, fontWeight = FontWeight.Medium)
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
        
        // Line Height
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Line Height", color = Color.White, fontSize = 14.sp)
                Text(String.format("%.1f", layer.lineHeight), color = Color(0xFF007AFF), fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
            Slider(
                value = layer.lineHeight,
                onValueChange = { onUpdate(layer.copy(lineHeight = it)) },
                valueRange = 0.5f..2.5f,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF007AFF),
                    activeTrackColor = Color(0xFF007AFF),
                    inactiveTrackColor = Color(0xFF3A3A3C)
                )
            )
        }
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
fun ColorTabContent(
    layer: TextLayer,
    onUpdate: (TextLayer) -> Unit,
    onRequestEyedropper: ((Color) -> Unit) -> Unit,
    onRequestTexturePick: () -> Unit
) {
    var colorMode by remember { mutableStateOf(ColorMode.TEXT) }
    var editingGradientStart by remember { mutableStateOf(true) } // For gradient editing

    // Identify the "Active Color" being edited
    val activeColorInt = if (colorMode == ColorMode.TEXT) {
        if (layer.isGradient) {
            if (editingGradientStart) layer.gradientColors[0] else layer.gradientColors[1]
        } else layer.color
    } else layer.backgroundColor
    
    val currentColor = Color(activeColorInt)
    
    // Base Color Logic (preserves RGB even if Alpha is 0 for sliders)
    var baseColor by remember { mutableStateOf(if (currentColor.alpha > 0f) currentColor else Color.White) }

    // Sync baseColor when active selection changes (to show correct slider position)
    LaunchedEffect(activeColorInt) {
        if (activeColorInt != 0 && currentColor.alpha > 0f) {
             // Only sync if significant difference to prevent drift or jumping
             if (currentColor.red != baseColor.red || currentColor.green != baseColor.green || currentColor.blue != baseColor.blue) {
                 baseColor = currentColor
             }
        }
    }

    // Unified Update Helper
    val updateActiveColor = { newColor: Color ->
        baseColor = newColor
        if (colorMode == ColorMode.TEXT) {
            if (layer.isGradient) {
                 val stops = layer.gradientColors.toMutableList()
                 if (editingGradientStart) stops[0] = newColor.toArgb() else stops[1] = newColor.toArgb()
                 onUpdate(layer.copy(gradientColors = stops))
            } else {
                 onUpdate(layer.copy(color = newColor.toArgb()))
            }
        } else {
            onUpdate(layer.copy(showBackground = true, backgroundColor = newColor.toArgb()))
        }
    }
    
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
            // Text Mode
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(if (colorMode == ColorMode.TEXT) Color(0xFF334155) else Color.Transparent, RoundedCornerShape(6.dp))
                    .clickable { colorMode = ColorMode.TEXT },
                contentAlignment = Alignment.Center
            ) {
                Text("Text", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
            // Background Mode
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(if (colorMode == ColorMode.BACKGROUND) Color(0xFF334155) else Color.Transparent, RoundedCornerShape(6.dp))
                    .clickable { colorMode = ColorMode.BACKGROUND },
                contentAlignment = Alignment.Center
            ) {
                Text("Background", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }

        // Gradient Controls (Only in Text Mode)
        // Fill Mode Toggle (Solid / Gradient / Texture)
        if (colorMode == ColorMode.TEXT) {
             val isTexture = layer.textureUri != null
             val isGradient = layer.isGradient && !isTexture
             val isSolid = !isGradient && !isTexture
             
             Row(
                 modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), 
                 horizontalArrangement = Arrangement.spacedBy(8.dp)
             ) {
                 // Solid Button
                 Box(
                     modifier = Modifier
                         .weight(1f)
                         .height(32.dp)
                         .clip(RoundedCornerShape(16.dp))
                         .background(if(isSolid) Color(0xFF007AFF) else Color(0xFF3A3A3C))
                         .clickable { 
                             onUpdate(layer.copy(isGradient = false, textureUri = null)) 
                         },
                     contentAlignment = Alignment.Center
                 ) {
                      Text("Solid", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                 }
                 
                 // Gradient Button
                 Box(
                     modifier = Modifier
                         .weight(1f)
                         .height(32.dp)
                         .clip(RoundedCornerShape(16.dp))
                         .background(if(isGradient) Color(0xFF007AFF) else Color(0xFF3A3A3C))
                         .clickable { 
                             onUpdate(layer.copy(isGradient = true, textureUri = null)) 
                         },
                     contentAlignment = Alignment.Center
                 ) {
                      Text("Gradient", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                 }
                 
                 // Texture Button
                 Box(
                     modifier = Modifier
                         .weight(1f)
                         .height(32.dp)
                         .clip(RoundedCornerShape(16.dp))
                         .background(if(isTexture) Color(0xFF007AFF) else Color(0xFF3A3A3C))
                         .clickable { 
                             // Keep existing texture if present, otherwise just switch mode (UI will show Pick button)
                             // If no texture yet, maybe auto-trigger pick? For now just switch state.
                             // We don't have a specific boolean for "Texture Mode" other than textureUri being not null.
                             // But we can't set textureUri to not-null without a URI.
                             // So clicking this should probably Trigger Pick if null?
                             if (layer.textureUri == null) {
                                 onRequestTexturePick()
                             } else {
                                 // Already has texture, just ensure gradient is off? 
                                 // Actually textureUri != null overrides gradient in logic, so just do nothing or maybe ensure consistency
                             }
                         },
                     contentAlignment = Alignment.Center
                 ) {
                      Text("Texture", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                 }
             }
             
             // Texture Controls
             if (isTexture) {
                 Column(
                     modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                     horizontalAlignment = Alignment.CenterHorizontally,
                     verticalArrangement = Arrangement.spacedBy(12.dp)
                 ) {
                     Text("Custom Texture Active", color = Color.Gray, fontSize = 12.sp)
                     
                     Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                         Button(
                             onClick = onRequestTexturePick,
                             colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155))
                         ) {
                             Text("Change Image")
                         }
                         
                         Button(
                             onClick = { onUpdate(layer.copy(textureUri = null)) },
                             colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)) // Red
                         ) {
                             Text("Remove")
                         }
                     }
                 }
             } else if (layer.isGradient) {
                 // Gradient Stops & Angle
                 Row(
                     modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), 
                     horizontalArrangement = Arrangement.SpaceBetween, 
                     verticalAlignment = Alignment.CenterVertically
                 ) {
                      // Start Stop
                      Column(horizontalAlignment = Alignment.CenterHorizontally) {
                          Text("Start", color = Color.Gray, fontSize = 10.sp)
                          ColorCircle(color = Color(layer.gradientColors[0]), isSelected = editingGradientStart, onClick = { editingGradientStart = true })
                      }
                      
                      // Angle Slider
                      Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                              Text("Angle", color = Color.Gray, fontSize = 10.sp)
                              Text("${layer.gradientAngle.toInt()}°", color = Color.Gray, fontSize = 10.sp)
                          }
                          Slider(value = layer.gradientAngle, onValueChange = { onUpdate(layer.copy(gradientAngle = it)) }, valueRange = 0f..360f)
                      }
                      
                      // End Stop
                      Column(horizontalAlignment = Alignment.CenterHorizontally) {
                          Text("End", color = Color.Gray, fontSize = 10.sp)
                          ColorCircle(color = Color(layer.gradientColors[1]), isSelected = !editingGradientStart, onClick = { editingGradientStart = false })
                      }
                 }
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
            if (colorMode == ColorMode.BACKGROUND) {
                item {
                    ColorCircle(
                        color = Color.Transparent, isSelected = !layer.showBackground, isNoColor = true,
                        onClick = { onUpdate(layer.copy(showBackground = false)) }
                    )
                }
            }
            items(colors.size) { index ->
                val color = colors[index]
                // Highlight if it matches CURRENT active color
                val isSelected = activeColorInt == color.toArgb()
                
                ColorCircle(
                    color = color, isSelected = isSelected,
                    onClick = { updateActiveColor(color) }
                )
            }
        }

        // Advanced Sliders (Spectrum + Opacity + Hex)
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val visualColor = baseColor.copy(alpha = 1f)
            
            // Spectrum
            SpectrumSlider(
                selectedColor = visualColor,
                onColorSelected = { newColor ->
                    val alpha = currentColor.alpha
                    updateActiveColor(newColor.copy(alpha = alpha))
                }
            )
            
            // Opacity
            OpacitySlider(
                color = visualColor, 
                alpha = currentColor.alpha,
                onAlphaChanged = { newAlpha ->
                    updateActiveColor(visualColor.copy(alpha = newAlpha))
                }
            )

            // Hex + Eyedropper
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                HexColorInput(
                    color = currentColor,
                    onColorChange = { updateActiveColor(it) }
                )
                
                Box(
                    modifier = Modifier.size(36.dp).background(Color(0xFF334155), RoundedCornerShape(8.dp)).clickable { 
                        onRequestEyedropper { color -> updateActiveColor(color) }
                    },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Colorize, "Eyedropper", tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
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
fun HexColorInput(
    color: Color,
    onColorChange: (Color) -> Unit
) {
    var text by remember(color) { 
        mutableStateOf(String.format("#%08X", color.toArgb())) 
    }
    
    // Update local validation when text changes
    fun validateAndSet(newText: String) {
        text = newText
        if (newText.length == 9 && newText.startsWith("#")) {
            try {
                val parsedColor = newText.toColorInt()
                onColorChange(Color(parsedColor))
            } catch (e: IllegalArgumentException) {
                // Ignore invalid hex
            }
        }
    }

    Row(
        modifier = Modifier
            .width(160.dp)
            .height(36.dp)
            .background(Color(0xFF334155), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Hex",
            color = Color.Gray,
            fontSize = 12.sp,
            modifier = Modifier.padding(end = 8.dp)
        )
        BasicTextField(
            value = text,
            onValueChange = { if (it.length <= 9) validateAndSet(it.uppercase()) },
            textStyle = TextStyle(
                color = Color.White,
                fontSize = 14.sp,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            ),
            singleLine = true,
            cursorBrush = SolidColor(Color.White)
        )
    }
}

@Composable
fun EffectsTabContent(layer: TextLayer, onUpdate: (TextLayer) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Blend Mode Section
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Blend Mode", color = Color.Gray, fontSize = 12.sp)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(end = 16.dp)
            ) {
                items(com.moshitech.workmate.feature.imagestudio.viewmodel.LayerBlendMode.values()) { mode ->
                    val isSelected = layer.blendMode == mode
                    Box(
                        modifier = Modifier
                            .height(30.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .background(if (isSelected) Color(0xFF007AFF) else Color(0xFF2C2C2E))
                            .clickable { onUpdate(layer.copy(blendMode = mode)) }
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = mode.name.lowercase().replaceFirstChar { it.uppercase() },
                            color = if (isSelected) Color.White else Color.Gray,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                        )
                    }
                }
            }
        }

        // Shadow Section
        EffectSection(
            title = "Shadow",
            isEnabled = layer.hasShadow,
            onToggle = { onUpdate(layer.copy(hasShadow = it)) }
        ) {
            // Shadow Controls
            if (layer.hasShadow) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Shadow Color
                    Text("Shadow Color", color = Color.Gray, fontSize = 12.sp)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(listOf(Color.Black, Color.DarkGray, Color.Gray, Color(0xFFE91E63), Color(0xFF2196F3))) { color ->
                            ColorCircle(
                                color = color,
                                isSelected = layer.shadowColor == color.toArgb(),
                                onClick = { onUpdate(layer.copy(shadowColor = color.toArgb())) }
                            )
                        }
                    }
                    
                    // Blur Radius
                    Row(
                         horizontalArrangement = Arrangement.SpaceBetween,
                         verticalAlignment = Alignment.CenterVertically,
                         modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Blur Radius", color = Color.Gray, fontSize = 12.sp)
                        Text("${layer.shadowBlur.toInt()}", color = Color.White, fontSize = 12.sp)
                    }
                    PremiumSlider(
                        value = layer.shadowBlur,
                        onValueChange = { onUpdate(layer.copy(shadowBlur = it)) },
                        valueRange = 1f..50f
                    )
                    
                    // Shadow Offset X
                    Row(
                         horizontalArrangement = Arrangement.SpaceBetween,
                         verticalAlignment = Alignment.CenterVertically,
                         modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Offset X", color = Color.Gray, fontSize = 12.sp)
                        Text("${layer.shadowOffsetX.toInt()}", color = Color.White, fontSize = 12.sp)
                    }
                    PremiumSlider(
                        value = layer.shadowOffsetX,
                        onValueChange = { onUpdate(layer.copy(shadowOffsetX = it)) },
                        valueRange = -30f..30f
                    )

                    // Shadow Offset Y
                    Row(
                         horizontalArrangement = Arrangement.SpaceBetween,
                         verticalAlignment = Alignment.CenterVertically,
                         modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Offset Y", color = Color.Gray, fontSize = 12.sp)
                        Text("${layer.shadowOffsetY.toInt()}", color = Color.White, fontSize = 12.sp)
                    }
                    PremiumSlider(
                        value = layer.shadowOffsetY,
                        onValueChange = { onUpdate(layer.copy(shadowOffsetY = it)) },
                        valueRange = -30f..30f
                    )
                }
            }
        }
        
        // Outline Section
        EffectSection(
            title = "Outline",
            isEnabled = layer.outlineWidth > 0f, 
            onToggle = { isEnabled -> 
                if (isEnabled) onUpdate(layer.copy(outlineWidth = 2f, outlineColor = if(layer.outlineColor == 0) android.graphics.Color.BLACK else layer.outlineColor))
                else onUpdate(layer.copy(outlineWidth = 0f))
            }
        ) {
            if (layer.outlineWidth > 0f) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                     // Outline Color
                    Text("Outline Color", color = Color.Gray, fontSize = 12.sp)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(listOf(Color.Black, Color.White, Color.Red, Color.Blue, Color.Green)) { color ->
                            ColorCircle(
                                color = color,
                                isSelected = layer.outlineColor == color.toArgb(),
                                onClick = { onUpdate(layer.copy(outlineColor = color.toArgb())) }
                            )
                        }
                    }
                    
                    // Stroke Width
                    Row(
                         horizontalArrangement = Arrangement.SpaceBetween,
                         verticalAlignment = Alignment.CenterVertically,
                         modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Thickness", color = Color.Gray, fontSize = 12.sp)
                        Text(String.format("%.1f", layer.outlineWidth), color = Color.White, fontSize = 12.sp)
                    }
                    PremiumSlider(
                        value = layer.outlineWidth,
                        onValueChange = { onUpdate(layer.copy(outlineWidth = it)) },
                        valueRange = 0.5f..10f
                    )
                }
            }
        }

        // Background Shape Section
        EffectSection(
            title = "Background",
            isEnabled = layer.showBackground,
            onToggle = { isEnabled ->
                onUpdate(layer.copy(
                    showBackground = isEnabled,
                    // Ensure visible color if enabling
                    backgroundColor = if(isEnabled && (layer.backgroundColor == 0 || layer.backgroundColor == android.graphics.Color.TRANSPARENT)) android.graphics.Color.parseColor("#80000000") else layer.backgroundColor
                )) 
            }
        ) {
            if (layer.showBackground) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Background Padding
                    Row(
                         horizontalArrangement = Arrangement.SpaceBetween,
                         verticalAlignment = Alignment.CenterVertically,
                         modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Padding", color = Color.Gray, fontSize = 12.sp)
                        Text("${layer.backgroundPadding.toInt()}", color = Color.White, fontSize = 12.sp)
                    }
                    PremiumSlider(
                        value = layer.backgroundPadding,
                        onValueChange = { onUpdate(layer.copy(backgroundPadding = it)) },
                        valueRange = 0f..60f
                    )

                    // Corner Radius
                    Row(
                         horizontalArrangement = Arrangement.SpaceBetween,
                         verticalAlignment = Alignment.CenterVertically,
                         modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Roundness", color = Color.Gray, fontSize = 12.sp)
                        Text("${layer.backgroundCornerRadius.toInt()}", color = Color.White, fontSize = 12.sp)
                    }
                    PremiumSlider(
                        value = layer.backgroundCornerRadius,
                        onValueChange = { onUpdate(layer.copy(backgroundCornerRadius = it)) },
                        valueRange = 0f..50f
                    )
                }
            }
        }

        // 3D Perspective Section
        EffectSection(
            title = "3D Perspective",
            isEnabled = layer.rotationX != 0f || layer.rotationY != 0f,
            onToggle = { isEnabled ->
                 if (isEnabled) {
                     // Default tilt of 15 degrees to make effect visible and enable controls
                     onUpdate(layer.copy(rotationX = 15f))
                 } else {
                     onUpdate(layer.copy(rotationX = 0f, rotationY = 0f))
                 }
            }
        ) {
            // Only show controls if enabled (non-zero rotation)
            if (layer.rotationX != 0f || layer.rotationY != 0f) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Tilt X (Vertical)
                    Row(
                         horizontalArrangement = Arrangement.SpaceBetween,
                         verticalAlignment = Alignment.CenterVertically,
                         modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Tilt Vertical (X)", color = Color.Gray, fontSize = 12.sp)
                        Text("${layer.rotationX.toInt()}°", color = Color.White, fontSize = 12.sp)
                    }
                    PremiumSlider(
                        value = layer.rotationX,
                        onValueChange = { onUpdate(layer.copy(rotationX = it)) },
                        valueRange = -60f..60f
                    )

                    // Tilt Y (Horizontal)
                    Row(
                         horizontalArrangement = Arrangement.SpaceBetween,
                         verticalAlignment = Alignment.CenterVertically,
                         modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Tilt Horizontal (Y)", color = Color.Gray, fontSize = 12.sp)
                        Text("${layer.rotationY.toInt()}°", color = Color.White, fontSize = 12.sp)
                    }
                    PremiumSlider(
                        value = layer.rotationY,
                        onValueChange = { onUpdate(layer.copy(rotationY = it)) },
                        valueRange = -60f..60f
                    )
                }
            }
        }

        // Text Blur Section
        EffectSection(
            title = "Blur",
            isEnabled = layer.textBlur > 0f,
            onToggle = { isEnabled ->
                // Default blur 2f if enabling
                if(isEnabled) onUpdate(layer.copy(textBlur = 2f)) else onUpdate(layer.copy(textBlur = 0f))
            }
        ) {
            if (layer.textBlur > 0f) {
                // Blur Slider
                Row(
                     horizontalArrangement = Arrangement.SpaceBetween,
                     verticalAlignment = Alignment.CenterVertically,
                     modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Radius", color = Color.Gray, fontSize = 12.sp)
                    Text("${layer.textBlur.toInt()}", color = Color.White, fontSize = 12.sp)
                }
                PremiumSlider(
                    value = layer.textBlur,
                    onValueChange = { onUpdate(layer.copy(textBlur = it)) },
                    valueRange = 0f..20f
                )
            }
        }

        // Reflection Section
        EffectSection(
            title = "Reflection",
            isEnabled = layer.reflectionOpacity > 0f,
            onToggle = { isEnabled ->
                 if(isEnabled && layer.reflectionOpacity == 0f) onUpdate(layer.copy(reflectionOpacity = 0.5f))
                 else if (!isEnabled) onUpdate(layer.copy(reflectionOpacity = 0f))
            }
        ) {
            if (layer.reflectionOpacity > 0f) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Opacity
                    Row(
                         horizontalArrangement = Arrangement.SpaceBetween,
                         verticalAlignment = Alignment.CenterVertically,
                         modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Opacity", color = Color.Gray, fontSize = 12.sp)
                        Text("${(layer.reflectionOpacity * 100).toInt()}%", color = Color.White, fontSize = 12.sp)
                    }
                    PremiumSlider(
                        value = layer.reflectionOpacity,
                        onValueChange = { onUpdate(layer.copy(reflectionOpacity = it)) },
                        valueRange = 0f..1f
                    )

                    // Vertical Offset (Spacing)
                    Row(
                         horizontalArrangement = Arrangement.SpaceBetween,
                         verticalAlignment = Alignment.CenterVertically,
                         modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Spacing", color = Color.Gray, fontSize = 12.sp)
                        Text("${layer.reflectionOffset.toInt()}", color = Color.White, fontSize = 12.sp)
                    }
                    PremiumSlider(
                        value = layer.reflectionOffset,
                        onValueChange = { onUpdate(layer.copy(reflectionOffset = it)) },
                        valueRange = -20f..60f
                    )
                }
            }
        }

        // Curved Text Section
        EffectSection(
            title = "Curved Text",
            isEnabled = abs(layer.curvature) > 0f,
            onToggle = { isEnabled ->
                 if(isEnabled && abs(layer.curvature) == 0f) onUpdate(layer.copy(curvature = 90f))
                 else if (!isEnabled) onUpdate(layer.copy(curvature = 0f))
            }
        ) {
            if (abs(layer.curvature) > 0f) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                         horizontalArrangement = Arrangement.SpaceBetween,
                         verticalAlignment = Alignment.CenterVertically,
                         modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Angle", color = Color.Gray, fontSize = 12.sp)
                        Text("${layer.curvature.toInt()}°", color = Color.White, fontSize = 12.sp)
                    }
                    PremiumSlider(
                        value = layer.curvature,
                        onValueChange = { onUpdate(layer.copy(curvature = it)) },
                        valueRange = -180f..180f
                    )
                }
            }
        }

        // Neon Effect Section
        EffectSection(
            title = "Neon",
            isEnabled = layer.isNeon,
            onToggle = { onUpdate(layer.copy(isNeon = it, isGlitch = if(it) false else layer.isGlitch)) }
        ) {
             Text("Glows in dark themes", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(top=8.dp))
        }

        // Glitch Effect Section
        EffectSection(
            title = "Glitch",
            isEnabled = layer.isGlitch,
            onToggle = { onUpdate(layer.copy(isGlitch = it, isNeon = if(it) false else layer.isNeon)) }
        ) {
            Text("Chromatic Aberration", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(top=8.dp))
        }
    }
}

@Composable
fun EffectSection(
    title: String,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF007AFF),
                    uncheckedThumbColor = Color.LightGray,
                    uncheckedTrackColor = Color(0xFF3A3A3C)
                )
            )
        }
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    modifier: Modifier = Modifier
) {
    Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = valueRange,
        modifier = modifier.height(30.dp),
        colors = SliderDefaults.colors(
            thumbColor = Color(0xFF007AFF),
            activeTrackColor = Color(0xFF007AFF),
            inactiveTrackColor = Color(0xFF3A3A3C)
        ),
        thumb = {
             Box(modifier = Modifier
                 .size(16.dp)
                 .background(Color(0xFF007AFF), CircleShape)
                 .border(2.dp, Color.White, CircleShape)
                 .shadow(4.dp, CircleShape)
             )
        },
        track = { sliderState -> 
            Box(modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xFF3A3A3C))
            ) {
                // Calculate fraction: (value - min) / (max - min)
                val fraction = (sliderState.value - valueRange.start) / (valueRange.endInclusive - valueRange.start)
                Box(modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF007AFF))
                )
            }
        }
    )
}
