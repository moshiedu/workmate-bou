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
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.CheckBoxOutlineBlank
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Opacity
import androidx.compose.material.icons.outlined.CropSquare
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.animation.animateContentSize
import androidx.compose.runtime.*
import androidx.compose.ui.draw.scale
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
import com.moshitech.workmate.feature.imagestudio.components.ToolIcon
import com.moshitech.workmate.feature.imagestudio.viewmodel.*
import com.moshitech.workmate.feature.imagestudio.viewmodel.ShapePropertyTab
import java.util.Locale

@Composable
fun DrawAndShapesToolbar(
    uiState: PhotoEditorUiState,
    viewModel: PhotoEditorViewModel
) {
    // Direct call to PaintToolbar as Shapes are now separate
    PaintToolbar(uiState, viewModel)
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
            val context = androidx.compose.ui.platform.LocalContext.current
            
            ToolIcon(
                icon = Icons.Default.BlurCircular, // NEW: Blur brush
                label = "Blur",
                isSelected = uiState.selectedDrawTool == DrawTool.BLUR,
                onClick = { 
                    android.widget.Toast.makeText(context, "Blur Tool Coming Soon!", android.widget.Toast.LENGTH_SHORT).show()
                }
            )
            ToolIcon(
                icon = Icons.Default.Grain, // NEW: Spray paint
                label = "Spray",
                isSelected = uiState.selectedDrawTool == DrawTool.SPRAY,
                onClick = { 
                    android.widget.Toast.makeText(context, "Spray Tool Coming Soon!", android.widget.Toast.LENGTH_SHORT).show()
                }
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
                onColorSelected = { viewModel.setDrawColor(it) }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Sliders & Style
        Column(modifier = Modifier.fillMaxWidth()) {
            // Size
            CompactModernSlider(
                value = uiState.currentStrokeWidth,
                onValueChange = { viewModel.setStrokeWidth(it) },
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
                                    viewModel.setDrawColor(color.toArgb())
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
                    EraserSizePreset(label = "Small", size = 20f, current = uiState.currentStrokeWidth) { viewModel.setStrokeWidth(20f) }
                    EraserSizePreset(label = "Medium", size = 50f, current = uiState.currentStrokeWidth) { viewModel.setStrokeWidth(50f) }
                    EraserSizePreset(label = "Large", size = 80f, current = uiState.currentStrokeWidth) { viewModel.setStrokeWidth(80f) }
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
    // State is now persisted in ViewModel
    val activeTab = uiState.lastActiveShapeTab

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0D121F))
    ) {
        // 1. DETAIL PANEL (Upper Area) - changes based on activeTab
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // Takes remaining space (or fixed height if needed)
                .padding(vertical = 16.dp)
        ) {
            when (activeTab) {
                ShapePropertyTab.SHAPES -> ShapeSelectionList(uiState, viewModel)
                ShapePropertyTab.COLOR -> ShapeColorPicker(uiState, viewModel)
                ShapePropertyTab.BORDER -> ShapeBorderControls(uiState, viewModel)
                ShapePropertyTab.SHADOW -> ShapeShadowControls(uiState, viewModel)
                ShapePropertyTab.OPACITY -> ShapeOpacityControls(uiState, viewModel)
            }
        }

        Divider(color = Color(0xFF2C2C2E))

        // 2. BOTTOM TABS (Navigation Row)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Helper for Tab Item
            @Composable
            fun PropertyTab(
                icon: androidx.compose.ui.graphics.vector.ImageVector,
                label: String,
                tab: ShapePropertyTab
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { viewModel.updateLastShapeTab(tab) }
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .border(
                                width = if (activeTab == tab) 1.dp else 0.dp,
                                color = if (activeTab == tab) Color.White else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            ), // Simple selection indicator
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            icon, 
                            contentDescription = label, 
                            tint = if (activeTab == tab) Color.White else Color.Gray
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = label,
                        color = if (activeTab == tab) Color.White else Color.Gray,
                        fontSize = 11.sp
                    )
                }
            }

            PropertyTab(Icons.Outlined.Category, "Shapes", ShapePropertyTab.SHAPES)
            PropertyTab(Icons.Outlined.Palette, "Color", ShapePropertyTab.COLOR)
            PropertyTab(Icons.Outlined.CheckBoxOutlineBlank, "Border", ShapePropertyTab.BORDER) // Border Icon
            PropertyTab(Icons.Outlined.Layers, "Shadow", ShapePropertyTab.SHADOW)
            PropertyTab(Icons.Outlined.Opacity, "Opacity", ShapePropertyTab.OPACITY)
        }
    }
}

// Removed local ShapePropertyTab enum as it is now in PhotoEditorState.kt

@Composable
fun ShapeSelectionList(uiState: PhotoEditorUiState, viewModel: PhotoEditorViewModel) {
    // Existing horizontal list of shapes logic
    // ... (Use code from previous ShapesToolbar)
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Shape Style", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(start = 16.dp, bottom = 8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
              val currentShapeId = uiState.selectedShapeLayerId
             val currentShape = uiState.shapeLayers.find { it.id == currentShapeId }
             
             val onShapeClick: (ShapeType) -> Unit = { type ->
                 if (currentShape != null) {
                     viewModel.setShapeType(currentShape.id, type)
                 } else {
                     viewModel.addShapeLayer(type)
                 }
             }
             
             ToolIcon(Icons.Outlined.CropSquare, "Rect", currentShape?.type == ShapeType.RECTANGLE) { onShapeClick(ShapeType.RECTANGLE) }
             ToolIcon(Icons.Outlined.Circle, "Circle", currentShape?.type == ShapeType.CIRCLE) { onShapeClick(ShapeType.CIRCLE) }
             ToolIcon(Icons.Default.ChangeHistory, "Triangle", currentShape?.type == ShapeType.TRIANGLE) { onShapeClick(ShapeType.TRIANGLE) }
             ToolIcon(Icons.Default.ArrowForward, "Arrow", currentShape?.type == ShapeType.ARROW) { onShapeClick(ShapeType.ARROW) }
             ToolIcon(Icons.Default.StarBorder, "Star", currentShape?.type == ShapeType.STAR) { onShapeClick(ShapeType.STAR) }
             ToolIcon(Icons.Default.Pentagon, "Pentagon", currentShape?.type == ShapeType.PENTAGON) { onShapeClick(ShapeType.PENTAGON) }
             ToolIcon(Icons.Default.HorizontalRule, "Line", currentShape?.type == ShapeType.LINE) { onShapeClick(ShapeType.LINE) }
        }
    }
}

// ... Additional Composables for Color, Border, Shadow, Opacity ...
// I will reuse/extract logic from ShapePropertiesPanel here.

@Composable
fun ShapeBorderControls(uiState: PhotoEditorUiState, viewModel: PhotoEditorViewModel) {
    val shape = uiState.shapeLayers.find { it.id == uiState.selectedShapeLayerId } ?: return
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        ModernSlider(
            value = shape.strokeWidth,
            onValueChange = { newValue -> viewModel.updateShapeLayer(shape.id) { it.copy(strokeWidth = newValue) } },
            valueRange = 0f..100f,
            label = "Stroke Width",
            unit = "px"
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        Text("Border Color", color = Color.White)
        Spacer(modifier = Modifier.height(8.dp))
        com.moshitech.workmate.feature.imagestudio.components.ColorPaletteRow(
            selectedColor = shape.borderColor,
            onColorSelected = { newColor ->
                viewModel.updateShapeLayer(shape.id) { 
                    // If stroke width is 0, set it to default (e.g. 10) so user sees the border immediately
                    val newWidth = if (it.strokeWidth == 0f) 10f else it.strokeWidth
                    it.copy(borderColor = newColor, strokeWidth = newWidth) 
                }
            }
        )
    }
}

@Composable
fun ShapeOpacityControls(uiState: PhotoEditorUiState, viewModel: PhotoEditorViewModel) {
    val shape = uiState.shapeLayers.find { it.id == uiState.selectedShapeLayerId } ?: return
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        ModernSlider(
            value = shape.opacity * 100f,
            onValueChange = { newValue -> viewModel.updateShapeLayer(shape.id) { it.copy(opacity = newValue / 100f) } },
            valueRange = 0f..100f,
            label = "Opacity",
            unit = "%"
        )
    }
}

@Composable
fun ShapeShadowControls(uiState: PhotoEditorUiState, viewModel: PhotoEditorViewModel) {
     val shape = uiState.shapeLayers.find { it.id == uiState.selectedShapeLayerId } ?: return
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Enable Shadow", color = Color.White, modifier = Modifier.weight(1f))
            Switch(checked = shape.hasShadow, onCheckedChange = { isChecked -> viewModel.updateShapeLayer(shape.id) { it.copy(hasShadow = isChecked) } })
        }
        if (shape.hasShadow) {
            Text("Blur Radius", color = Color.White)
            Slider(value = shape.shadowBlur, onValueChange = { newValue -> viewModel.updateShapeLayer(shape.id) { it.copy(shadowBlur = newValue) } }, valueRange = 1f..100f)
        }
    }
}

@Composable
fun ShapeColorPicker(uiState: PhotoEditorUiState, viewModel: PhotoEditorViewModel) {
     val shape = uiState.shapeLayers.find { it.id == uiState.selectedShapeLayerId } ?: return
    // Color Palette Reuse
    // Assuming we have a ColorPalette composable or create a simple one here
    com.moshitech.workmate.feature.imagestudio.components.ColorPaletteRow(
        selectedColor = if (shape.isFilled) shape.color else 0,
        onColorSelected = { newColor ->
            viewModel.updateShapeLayer(shape.id) { it.copy(color = newColor, isFilled = true) } // Force fill when coloring? PicsArt separates Fill vs Stroke Color usually.
            // For now, assume changing main color.
        }
    )
}

private enum class ShapeSubTool {
    EDIT, COLOR, STROKE, SHADOW, OPACITY, ARRANGE
}

@Composable
fun ShapePropertiesPanel(
    uiState: PhotoEditorUiState,
    viewModel: PhotoEditorViewModel
) {
    val layer = uiState.shapeLayers.find { it.id == uiState.selectedShapeLayerId } ?: return
    var activeSubTool by remember { mutableStateOf(ShapeSubTool.EDIT) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .animateContentSize()
    ) {
        // 1. Contextual Panel (Top)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .heightIn(min = 120.dp), // Ensure minimal height for controls
            contentAlignment = Alignment.Center
        ) {
            when (activeSubTool) {
                ShapeSubTool.EDIT -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Transform", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                             // Quick Actions
                             Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                 IconButton(onClick = { viewModel.duplicateShape(layer.id) }) {
                                     Icon(Icons.Default.ContentCopy, "Duplicate", tint = Color.White)
                                 }
                                 Text("Copy", color = Color.Gray, fontSize = 10.sp)
                             }
                             Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                 IconButton(onClick = { viewModel.deleteShapeLayer(layer.id) }) {
                                     Icon(Icons.Default.Delete, "Delete", tint = Color.Red)
                                 }
                                 Text("Delete", color = Color.Gray, fontSize = 10.sp)
                             }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        // Sliders for precise control?
                        // Sliders for precise control?
                        CompactModernSlider(
                             value = layer.rotation,
                             onValueChange = { rotation -> viewModel.updateShapeLayer(layer.id) { it.copy(rotation = rotation) } },
                             valueRange = 0f..360f,
                             label = "Rotation",
                             unit = "°"
                        )
                    }
                }
                ShapeSubTool.COLOR -> {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Text("Fill Color", color = Color.Gray, fontSize = 12.sp)
                            Spacer(Modifier.weight(1f))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                 Text("Fill", color = Color.White, fontSize = 12.sp)
                                 Switch(
                                     checked = layer.isFilled,
                                     onCheckedChange = { isFilled -> 
                                         viewModel.updateShapeLayer(layer.id) { it.copy(isFilled = isFilled) }
                                     },
                                     modifier = Modifier.scale(0.8f)
                                 )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        ColorPaletteRow(
                            selectedColor = layer.color,
                            onColorSelected = { viewModel.updateShapeLayer(layer.id) { s -> s.copy(color = it) } }
                        )
                    }
                }
                ShapeSubTool.STROKE -> {
                    Column {
                        Text("Stroke", color = Color.Gray, fontSize = 12.sp)
                        Spacer(Modifier.height(8.dp))
                        CompactModernSlider(
                            value = layer.strokeWidth,
                            onValueChange = { viewModel.setStrokeWidth(it) },
                            valueRange = 1f..50f,
                            label = "Thickness",
                            unit = "px"
                        )
                        Spacer(Modifier.height(16.dp))
                        // Style Chips
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StrokeStyle.entries.forEach { style ->
                                val isSelected = layer.strokeStyle == style
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.updateShapeStrokeStyle(layer.id, style) },
                                    label = { Text(style.name.lowercase().replaceFirstChar { it.titlecase() }) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color.White,
                                        selectedLabelColor = Color.Black,
                                        containerColor = Color(0xFF2C2C2E),
                                        labelColor = Color.White
                                    ),
                                    border = null
                                )
                            }
                        }
                    }
                }
                ShapeSubTool.SHADOW -> {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Shadow", color = Color.Gray, fontSize = 12.sp)
                            Spacer(Modifier.weight(1f))
                             Switch(
                                 checked = layer.hasShadow,
                                 onCheckedChange = { checked ->
                                      viewModel.updateShapeShadow(layer.id, checked, layer.shadowColor, layer.shadowBlur, layer.shadowX, layer.shadowY)
                                 },
                                 modifier = Modifier.scale(0.8f)
                             )
                        }
                        if (layer.hasShadow) {
                            Spacer(Modifier.height(8.dp))
                            CompactModernSlider(
                                value = layer.shadowBlur,
                                onValueChange = { viewModel.updateShapeShadow(layer.id, true, layer.shadowColor, it, layer.shadowX, layer.shadowY, false) },
                                onValueChangeFinished = { viewModel.saveToHistory() },
                                valueRange = 1f..50f,
                                label = "Blur"
                            )
                            Spacer(Modifier.height(8.dp))
                            CompactModernSlider(
                                value = layer.shadowX,
                                onValueChange = { viewModel.updateShapeShadow(layer.id, true, layer.shadowColor, layer.shadowBlur, it, layer.shadowY, false) },
                                onValueChangeFinished = { viewModel.saveToHistory() },
                                valueRange = -20f..20f,
                                label = "Offset X"
                            )
                            Spacer(Modifier.height(8.dp))
                            CompactModernSlider(
                                value = layer.shadowY,
                                onValueChange = { viewModel.updateShapeShadow(layer.id, true, layer.shadowColor, layer.shadowBlur, layer.shadowX, it, false) },
                                onValueChangeFinished = { viewModel.saveToHistory() },
                                valueRange = -20f..20f,
                                label = "Offset Y"
                            )
                        }
                    }
                }
                ShapeSubTool.OPACITY -> {
                    Column {
                        Text("Opacity", color = Color.Gray, fontSize = 12.sp)
                        Spacer(Modifier.height(8.dp))
                        CompactModernSlider(
                             value = layer.opacity * 100f,
                             onValueChange = { viewModel.updateShapeOpacity(layer.id, it / 100f, saveHistory = false) },
                             onValueChangeFinished = { viewModel.updateShapeOpacity(layer.id, layer.opacity, saveHistory = true) },
                             valueRange = 0f..100f,
                             label = "Opacity",
                             unit = "%"
                        )
                    }
                }
                ShapeSubTool.ARRANGE -> {
                     Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                         Column(horizontalAlignment = Alignment.CenterHorizontally) {
                             IconButton(onClick = { viewModel.bringShapeToFront(layer.id) }) {
                                 Icon(Icons.Default.ArrowUpward, "Front", tint = Color.White)
                             }
                             Text("To Front", color = Color.White, fontSize = 10.sp)
                         }
                         Column(horizontalAlignment = Alignment.CenterHorizontally) {
                             IconButton(onClick = { viewModel.sendShapeToBack(layer.id) }) {
                                 Icon(Icons.Default.ArrowDownward, "Back", tint = Color.White)
                             }
                             Text("To Back", color = Color.White, fontSize = 10.sp)
                         }
                     }
                }
            }
        }

        Divider(color = Color(0xFF2C2C2E))

        // 2. Sub-Tool Navigation (Bottom)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(top = 16.dp, start = 8.dp, end = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            SubToolItem(
                label = "Edit",
                icon = Icons.Outlined.Edit,
                isSelected = activeSubTool == ShapeSubTool.EDIT
            ) { activeSubTool = ShapeSubTool.EDIT }

            SubToolItem(
                label = "Color",
                icon = Icons.Outlined.Palette,
                isSelected = activeSubTool == ShapeSubTool.COLOR
            ) { activeSubTool = ShapeSubTool.COLOR }

            SubToolItem(
                label = "Border",
                icon = Icons.Outlined.CheckBoxOutlineBlank,
                isSelected = activeSubTool == ShapeSubTool.STROKE
            ) { activeSubTool = ShapeSubTool.STROKE }

            SubToolItem(
                label = "Shadow",
                icon = Icons.Outlined.ContentCopy, // Placeholder
                isSelected = activeSubTool == ShapeSubTool.SHADOW
            ) { activeSubTool = ShapeSubTool.SHADOW }

            SubToolItem(
                label = "Opacity",
                icon = Icons.Outlined.Opacity,
                isSelected = activeSubTool == ShapeSubTool.OPACITY
            ) { activeSubTool = ShapeSubTool.OPACITY }
            
            SubToolItem(
                label = "Arrange",
                icon = Icons.Outlined.Layers,
                isSelected = activeSubTool == ShapeSubTool.ARRANGE
            ) { activeSubTool = ShapeSubTool.ARRANGE }
        }
    }
}

@Composable
fun SubToolItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) Color(0xFF2196F3) else Color.Gray,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            color = if (isSelected) Color(0xFF2196F3) else Color.Gray,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
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
