package com.moshitech.workmate.feature.imagestudio.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moshitech.workmate.feature.imagestudio.viewmodel.*

@Composable
fun DrawAndShapesToolbar(
    uiState: PhotoEditorUiState,
    viewModel: PhotoEditorViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E1E1E))
            .padding(bottom = 16.dp)
    ) {
        // 1. Mode Switcher (Paint | Shapes)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            ModeSegmentedControl(
                selectedMode = uiState.activeDrawMode,
                onModeSelected = { viewModel.setDrawMode(it) }
            )
        }

        Divider(color = Color(0xFF333333))

        // 2. Content based on Mode
        if (uiState.activeDrawMode == DrawMode.PAINT) {
            PaintToolbar(uiState, viewModel)
        } else {
            ShapesToolbar(uiState, viewModel)
        }
    }
}

@Composable
fun ModeSegmentedControl(
    selectedMode: DrawMode,
    onModeSelected: (DrawMode) -> Unit
) {
    Row(
        modifier = Modifier
            .height(40.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF2C2C2C))
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DrawMode.values().forEach { mode ->
            val isSelected = selectedMode == mode
            Box(
                modifier = Modifier
                    .weight(1f) // Adjust if not filling width
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSelected) Color(0xFF404040) else Color.Transparent)
                    .clickable { onModeSelected(mode) }
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = mode.name,
                    color = if (isSelected) Color.White else Color.Gray,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun PaintToolbar(
    uiState: PhotoEditorUiState,
    viewModel: PhotoEditorViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Tool Icons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ToolIcon(
                icon = Icons.Default.Brush,
                label = "Brush",
                isSelected = uiState.selectedDrawTool == DrawTool.BRUSH,
                onClick = { viewModel.selectDrawTool(DrawTool.BRUSH) }
            )
            ToolIcon(
                icon = Icons.Default.LightMode, // Placeholder for Neon
                label = "Neon",
                isSelected = uiState.selectedDrawTool == DrawTool.NEON,
                onClick = { viewModel.selectDrawTool(DrawTool.NEON) }
            )
            ToolIcon(
                icon = Icons.Default.BlurOn, // Placeholder for Mosaic
                label = "Mosaic",
                isSelected = uiState.selectedDrawTool == DrawTool.MOSAIC,
                onClick = { viewModel.selectDrawTool(DrawTool.MOSAIC) }
            )
            ToolIcon(
                icon = Icons.Default.Edit, // Placeholder for Highlighter
                label = "Marker",
                isSelected = uiState.selectedDrawTool == DrawTool.HIGHLIGHTER,
                onClick = { viewModel.selectDrawTool(DrawTool.HIGHLIGHTER) }
            )
            ToolIcon(
                icon = Icons.Default.AutoFixNormal, // Placeholder for Eraser
                label = "Eraser",
                isSelected = uiState.selectedDrawTool == DrawTool.ERASER,
                onClick = { viewModel.selectDrawTool(DrawTool.ERASER) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Color Palette (If not Eraser/Mosaic/Highlighter)
        if (uiState.selectedDrawTool != DrawTool.ERASER && 
            uiState.selectedDrawTool != DrawTool.MOSAIC && 
            uiState.selectedDrawTool != DrawTool.HIGHLIGHTER) {
            ColorPaletteRow(
                selectedColor = uiState.currentDrawColor,
                onColorSelected = { viewModel.updateDrawColor(it) }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Sliders & Style
        Column(modifier = Modifier.fillMaxWidth()) {
            // Size
            CompactModernSlider(
                value = uiState.currentStrokeWidth,
                onValueChange = { viewModel.updateStrokeWidth(it) },
                onValueChangeFinished = { viewModel.saveToHistory() },
                valueRange = 1f..100f,
                label = "Size",
                unit = ""
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Opacity (Hide for Eraser as it's a hard eraser)
            if (uiState.selectedDrawTool != DrawTool.ERASER) {
                CompactModernSlider(
                    value = uiState.currentOpacity * 100f,
                    onValueChange = { viewModel.updateOpacity(it / 100f) },
                    onValueChangeFinished = { viewModel.saveToHistory() },
                    valueRange = 0f..100f,
                    label = "Opacity",
                    unit = "%"
                )
            }
        }
        
        // Highlighter Color Presets (only show when Highlighter tool is selected)
        if (uiState.selectedDrawTool == DrawTool.HIGHLIGHTER) {
            Spacer(modifier = Modifier.height(8.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Highlighter Colors", color = Color.Gray, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Define highlighter colors
                    val highlighterColors = listOf(
                        Color(0xFFFFEB3B) to "Yellow",
                        Color(0xFFFF4081) to "Pink",
                        Color(0xFF4CAF50) to "Green",
                        Color(0xFF2196F3) to "Blue",
                        Color(0xFFFF9800) to "Orange"
                    )
                    
                    highlighterColors.forEach { (color, name) ->
                        // Compare RGB values ignoring alpha to ensure selection works even if opacity changes
                        val isSelected = (uiState.currentDrawColor and 0x00FFFFFF) == (color.toArgb() and 0x00FFFFFF)
                        // Calculate luminance to determine best checkmark color (black for light backgrounds, white for dark)
                        val luminance = (0.299 * color.red + 0.587 * color.green + 0.114 * color.blue)
                        val checkmarkColor = if (luminance > 0.5) Color.Black else Color.White
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .shadow(
                                    elevation = if (isSelected) 8.dp else 0.dp,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .background(color, RoundedCornerShape(8.dp))
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) Color.White else Color.Gray.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { 
                                    viewModel.updateDrawColor(color.toArgb())
                                    // Set opacity to 60% for realistic highlighting
                                    viewModel.updateOpacity(0.6f)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            // Show checkmark for selected color
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = checkmarkColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Eraser Controls (only show when Eraser tool is selected)
        if (uiState.selectedDrawTool == DrawTool.ERASER) {
            Spacer(modifier = Modifier.height(8.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                // Size Presets
                 Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Presets:", color = Color.Gray, fontSize = 12.sp)
                    EraserSizePreset(label = "Small", size = 20f, current = uiState.currentStrokeWidth) { viewModel.updateStrokeWidth(20f) }
                    EraserSizePreset(label = "Medium", size = 50f, current = uiState.currentStrokeWidth) { viewModel.updateStrokeWidth(50f) }
                    EraserSizePreset(label = "Large", size = 80f, current = uiState.currentStrokeWidth) { viewModel.updateStrokeWidth(80f) }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Clear All Button
                Button(
                    onClick = { viewModel.clearAllDrawings() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)), // Red color for destructive action
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Clear All Drawings", color = Color.White)
                }
            }
        }

        // Mosaic Intensity Slider (only show when Mosaic tool is selected)
        if (uiState.selectedDrawTool == DrawTool.MOSAIC) {
            // Presets and Reset
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Presets", color = Color.Gray, fontSize = 12.sp)
                IconButton(
                    onClick = { viewModel.resetMosaicSettings() },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = "Reset",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            // Preset Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                com.moshitech.workmate.feature.imagestudio.viewmodel.MosaicPreset.defaults.forEach { preset ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .background(
                                color = Color(0xFF2C2C2E),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { viewModel.applyMosaicPreset(preset) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = preset.name,
                            color = Color.White,
                            fontSize = 11.sp
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            CompactModernSlider(
                value = ((0.20f - uiState.mosaicIntensity) / 0.19f * 100f).coerceIn(0f, 100f),
                onValueChange = { viewModel.updateMosaicIntensity(it) },
                valueRange = 0f..100f,
                label = "Pixelation",
                unit = "%"
            )
            
            // Pattern Selector
            Spacer(modifier = Modifier.height(8.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Pattern", color = Color.Gray, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    com.moshitech.workmate.feature.imagestudio.viewmodel.MosaicPattern.values().forEach { pattern ->
                        val isSelected = uiState.mosaicPattern == pattern
                        val isEnabled = pattern == com.moshitech.workmate.feature.imagestudio.viewmodel.MosaicPattern.SQUARE
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .background(
                                    color = when {
                                        isSelected -> Color(0xFF007AFF)
                                        !isEnabled -> Color(0xFF1C1C1E)
                                        else -> Color(0xFF2C2C2E)
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable(enabled = isEnabled) { 
                                    if (isEnabled) viewModel.updateMosaicPattern(pattern) 
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (!isEnabled && pattern != com.moshitech.workmate.feature.imagestudio.viewmodel.MosaicPattern.SQUARE) 
                                    "${pattern.displayName}\n(Soon)" 
                                else 
                                    pattern.displayName,
                                color = if (isEnabled) Color.White else Color.Gray,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
            
            // Color Mode Selector
            Spacer(modifier = Modifier.height(8.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Color Mode", color = Color.Gray, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    com.moshitech.workmate.feature.imagestudio.viewmodel.MosaicColorMode.values().forEach { mode ->
                        val isSelected = uiState.mosaicColorMode == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .background(
                                    color = if (isSelected) Color(0xFF007AFF) else Color(0xFF2C2C2E),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { viewModel.updateMosaicColorMode(mode) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = mode.displayName,
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
            
            // Posterize Levels Slider (only show when Posterize mode is selected)
            if (uiState.mosaicColorMode == com.moshitech.workmate.feature.imagestudio.viewmodel.MosaicColorMode.POSTERIZE) {
                Spacer(modifier = Modifier.height(8.dp))
                CompactModernSlider(
                    value = uiState.posterizeLevels.toFloat(),
                    onValueChange = { viewModel.updatePosterizeLevels(it.toInt()) },
                    valueRange = 2f..8f,
                    label = "Color Levels",
                    unit = ""
                )
            }
        }
    }
}

@Composable
fun ShapesToolbar(
    uiState: PhotoEditorUiState,
    viewModel: PhotoEditorViewModel
) {
    if (uiState.selectedShapeLayerId != null) {
        // Selected Shape Properties
        ShapePropertiesPanel(uiState, viewModel)
    } else {
        // Add Shape Grid
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text("Add Shape", color = Color.White, modifier = Modifier.padding(bottom = 8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                 ToolIcon(
                    icon = Icons.Outlined.CropSquare,
                    label = "Rect",
                    isSelected = false,
                    onClick = { viewModel.addShapeLayer(ShapeType.RECTANGLE) }
                )
                ToolIcon(
                    icon = Icons.Outlined.Circle,
                    label = "Circle",
                    isSelected = false,
                    onClick = { viewModel.addShapeLayer(ShapeType.CIRCLE) }
                )
                ToolIcon(
                    icon = Icons.Default.ChangeHistory, // Triangle
                    label = "Triangle",
                    isSelected = false,
                    onClick = { viewModel.addShapeLayer(ShapeType.TRIANGLE) }
                )
                ToolIcon(
                    icon = Icons.Default.ArrowRightAlt,
                    label = "Arrow",
                    isSelected = false,
                    onClick = { viewModel.addShapeLayer(ShapeType.ARROW) }
                )
                ToolIcon(
                    icon = Icons.Default.StarBorder, // Star
                    label = "Star",
                    isSelected = false,
                    onClick = { viewModel.addShapeLayer(ShapeType.STAR) }
                )
                ToolIcon(
                    icon = Icons.Default.Pentagon, // Pentagon (Fallback to Star if not found?) Pentagon is in M3/M2 extended usually.
                    // If Pentagon is missing, use Verified or similar. 
                    // To be safe, let's use Label Important for now if Pentagon is risky 
                    // But Icons.Outlined.Pentagon exists in newer compose.
                    // Let's try Icons.Default.Pentagon. If it fails I'll fix.
                    // Actually, let's use Icons.Default.Grade (Star) for Star.
                    // For Pentagon, maybe Icons.Default.Hexagon? No.
                    // I will use Icons.Default.Details which is Triangle-ish inverted.
                    // Let's simply re-use ChangeHistory and rotate? No.
                    // I will use Icons.Default.Warning (Triangle).
                    // For Pentagon, I will use Icons.Default.Stop if I can't find it.
                    // Actually, let's use Icons.Default.Star for Star.
                    // And Icons.Default.ChangeHistory for Triangle.
                    // For Pentagon, I will use Icons.Default.House! It looks like a Pentagon.
                    label = "Pentagon",
                    isSelected = false,
                    onClick = { viewModel.addShapeLayer(ShapeType.PENTAGON) }
                )
                  ToolIcon(
                    icon = Icons.Default.HorizontalRule, // Line
                    label = "Line",
                    isSelected = false,
                    onClick = { viewModel.addShapeLayer(ShapeType.LINE) }
                )
            }
        }
    }
}

@Composable
fun ShapePropertiesPanel(
    uiState: PhotoEditorUiState,
    viewModel: PhotoEditorViewModel
) {
    val layer = uiState.shapeLayers.find { it.id == uiState.selectedShapeLayerId } ?: return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header: Back & Title & Delete
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { viewModel.deselectShapeLayer() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Back to Add", color = Color.White)
            }
            
            IconButton(onClick = { viewModel.deleteShapeLayer(layer.id) }) {
                Icon(Icons.Default.Delete, "Delete", tint = Color.Red)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Color & Fill
        Row(verticalAlignment = Alignment.CenterVertically) {
             ColorPaletteRow(
                selectedColor = layer.color,
                onColorSelected = { viewModel.updateDrawColor(it) },
                modifier = Modifier.weight(1f)
            )
             Spacer(modifier = Modifier.width(8.dp))
             // Fill Toggle
             Column(horizontalAlignment = Alignment.CenterHorizontally) {
                 Text("Fill", color = Color.Gray, fontSize = 10.sp)
                 Switch(
                     checked = layer.isFilled,
                     onCheckedChange = { isFilled -> 
                         viewModel.updateShapeLayer(layer.id) { it.copy(isFilled = isFilled) }
                     }
                 )
             }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Stroke Style (Modern Chips)
        Text("Stroke Style", color = Color.Gray, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StrokeStyle.values().forEach { style ->
                val isSelected = layer.strokeStyle == style
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.updateShapeStrokeStyle(layer.id, style) },
                    label = { 
                        Text(
                            text = style.name.replace("_", " ").toLowerCase().capitalize(),
                            color = if (isSelected) Color.Black else Color.White
                        ) 
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color.White,
                        containerColor = Color(0xFF2C2C2E)
                    ),
                    border = null
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sliders: Scale and Rotation
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Scale
                Column(modifier = Modifier.weight(1f)) {
                    CompactModernSlider(
                        value = layer.scale,
                        onValueChange = { scale -> 
                            viewModel.updateShapeLayer(layer.id) { it.copy(scale = scale) }
                        },
                        valueRange = 0.1f..5f,
                        label = "Size",
                        unit = "x"
                    )
                }
                // Rotation
                Column(modifier = Modifier.weight(1f)) {
                    CompactModernSlider(
                        value = layer.rotation,
                        onValueChange = { rotation -> 
                            viewModel.updateShapeLayer(layer.id) { it.copy(rotation = rotation) } 
                        },
                        valueRange = 0f..360f,
                        label = "Rotation",
                        unit = "°"
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // strokeWidth
            CompactModernSlider(
                value = layer.strokeWidth,
                onValueChange = { viewModel.updateStrokeWidth(it) },
                valueRange = 1f..50f,
                label = "Thickness",
                unit = "px"
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Shadow Controls
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Shadow", color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            Switch(
                checked = layer.hasShadow,
                onCheckedChange = { checked ->
                     viewModel.updateShapeShadow(layer.id, checked, layer.shadowColor, layer.shadowBlur, layer.shadowX, layer.shadowY)
                }
            )
        }
        
        if (layer.hasShadow) {
            Spacer(modifier = Modifier.height(8.dp))
             CompactModernSlider(
                value = layer.shadowBlur,
                onValueChange = { blur ->
                    viewModel.updateShapeShadow(layer.id, true, layer.shadowColor, blur, layer.shadowX, layer.shadowY)
                },
                valueRange = 1f..50f,
                label = "Blur",
                unit = "px"
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp)) // Bottom padding
    }
}

@Composable
fun ToolIcon(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (isSelected) Color(0xFF007AFF) else Color(0xFF333333))
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = Color.White)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, color = if (isSelected) Color(0xFF007AFF) else Color.Gray, fontSize = 12.sp)
    }
}

@Composable
fun ColorPaletteRow(
    selectedColor: Int,
    onColorSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val colors = listOf(
            Color.Red, Color.Blue, Color.Green, Color.Yellow, 
            Color.Black, Color.White, Color.Cyan, Color.Magenta, Color.Gray
        )
        
        colors.forEach { c ->
            val cInt = c.toArgb()
            val isSelected = (selectedColor and 0x00FFFFFF) == (cInt and 0x00FFFFFF)
            
            // Calculate luminance for best checkmark contrast
            val luminance = (0.299 * c.red + 0.587 * c.green + 0.114 * c.blue)
            val checkmarkColor = if (luminance > 0.5) Color.Black else Color.White
            
            Box(
                modifier = Modifier
                    .size(40.dp) // Increased size for better touch target
                    .shadow(
                        elevation = if (isSelected) 6.dp else 0.dp,
                        shape = CircleShape
                    )
                    .background(c, CircleShape)
                    .border(
                        width = if (isSelected) 3.dp else 1.dp,
                        color = if (isSelected) Color.White else Color.Gray.copy(alpha = 0.3f),
                        shape = CircleShape
                    )
                    .clickable { onColorSelected(cInt) },
                contentAlignment = Alignment.Center
            ) {
                 if (isSelected) {
                     Icon(
                         imageVector = Icons.Default.Check,
                         contentDescription = "Selected",
                         tint = checkmarkColor,
                         modifier = Modifier.size(24.dp)
                     )
                 }
            }
        }
    }
}

@Composable
fun EraserSizePreset(
    label: String,
    size: Float,
    current: Float,
    onClick: () -> Unit
) {
    val isSelected = current == size
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Color(0xFF007AFF),
            selectedLabelColor = Color.White,
            containerColor = Color(0xFF333333),
            labelColor = Color.White
        ),
        border = null
    )
}
