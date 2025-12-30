package com.moshitech.workmate.feature.imagestudio.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moshitech.workmate.feature.imagestudio.viewmodel.StickerLayer

private enum class StickerTool(val label: String, val icon: ImageVector? = null) {
    OPACITY("Opacity"),
    BLEND("Blend"),
    BORDER("Border"),
    SHADOW("Shadow"),
    FLIP("Flip/Rotate")  // Combined for simplicity
}

@Composable
fun StickerEditorToolbar(
    layer: StickerLayer,
    onUpdateOpacity: (Float) -> Unit,
    onUpdateBlend: (BlendMode) -> Unit,
    onUpdateBorder: (Boolean, Int, Float) -> Unit,
    onUpdateShadow: (Boolean, Int, Float, Float, Float) -> Unit,
    onFlip: () -> Unit,
    onDone: () -> Unit
) {
    var activeTool by remember { mutableStateOf(StickerTool.OPACITY) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E1E1E))
            .padding(bottom = 16.dp)
    ) {
        // --- CONTROL PANEL (Changes based on selection) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .heightIn(min = 60.dp),
            contentAlignment = Alignment.Center
        ) {
            when (activeTool) {
                StickerTool.OPACITY -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Opacity: ${(layer.opacity * 100).toInt()}%", color = Color.White, fontSize = 12.sp)
                        Slider(
                            value = layer.opacity,
                            onValueChange = onUpdateOpacity,
                            valueRange = 0f..1f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color.White,
                                activeTrackColor = Color.White,
                                inactiveTrackColor = Color.Gray
                            )
                        )
                    }
                }
                StickerTool.BLEND -> {
                    val blendModes = listOf(
                        BlendMode.SrcOver to "Normal",
                        BlendMode.Screen to "Screen",
                        BlendMode.Multiply to "Multiply",
                        BlendMode.Overlay to "Overlay",
                        BlendMode.Darken to "Darken",
                        BlendMode.Lighten to "Lighten",
                        BlendMode.ColorDodge to "Add"
                    )
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        blendModes.forEach { (mode, label) ->
                            FilterChip(
                                selected = layer.blendMode == mode,
                                onClick = { onUpdateBlend(mode) },
                                label = { Text(label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color.White,
                                    selectedLabelColor = Color.Black,
                                    containerColor = Color(0xFF2C2C2C),
                                    labelColor = Color.White
                                ),
                                border = null
                            )
                        }
                    }
                }
                StickerTool.BORDER -> {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Enable Border", color = Color.White, modifier = Modifier.weight(1f))
                            Switch(
                                checked = layer.hasBorder,
                                onCheckedChange = { onUpdateBorder(it, layer.borderColor, layer.borderWidth) },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF007AFF))
                            )
                        }
                        if (layer.hasBorder) {
                            Spacer(modifier = Modifier.height(8.dp))
                            // Simple Width Slider
                             Slider(
                                value = layer.borderWidth,
                                onValueChange = { onUpdateBorder(true, layer.borderColor, it) },
                                valueRange = 0f..20f
                            )
                             // Simple Color Row (Mock)
                             Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                 val colors = listOf(Color.White, Color.Black, Color.Red, Color.Blue, Color.Green, Color.Yellow)
                                 colors.forEach { color ->
                                     Box(
                                         modifier = Modifier
                                             .size(24.dp)
                                             .background(color, CircleShape)
                                             .border(1.dp, Color.Gray, CircleShape)
                                             .clickable { onUpdateBorder(true, android.graphics.Color.argb((color.alpha*255).toInt(), (color.red*255).toInt(), (color.green*255).toInt(), (color.blue*255).toInt()), layer.borderWidth) }
                                     )
                                 }
                             }
                        }
                    }
                }
                StickerTool.SHADOW -> {
                     Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Drop Shadow", color = Color.White, modifier = Modifier.weight(1f))
                            Switch(
                                checked = layer.hasShadow,
                                onCheckedChange = { onUpdateShadow(it, layer.shadowColor, layer.shadowBlur, layer.shadowOffsetX, layer.shadowOffsetY) },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF007AFF))
                            )
                        }
                    }
                }
                StickerTool.FLIP -> {
                    Button(
                        onClick = onFlip,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2C))
                    ) {
                        Text("Flip Horizontal", color = Color.White)
                    }
                }
            }
        }

        Divider(color = Color(0xFF2C2C2C))

        // --- MENU TABS ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            StickerTool.values().forEach { tool ->
                Column(
                    modifier = Modifier
                        .clickable { activeTool = tool }
                        .padding(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = tool.label,
                        color = if (activeTool == tool) Color.White else Color.Gray,
                        fontSize = 14.sp,
                        fontWeight = if (activeTool == tool) FontWeight.Bold else FontWeight.Normal
                    )
                    if (activeTool == tool) {
                        Box(
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .size(4.dp)
                                .background(Color.White, CircleShape)
                        )
                    }
                }
            }
        }
        
        // --- BOTTOM ACTION ROW (Cancel/Done) ---
        // Optional: If this toolbar replaces the main nav, we need a Done button.
        // If it's just a selection state, clicking outside handles it.
        // But users like explicit "Done".
        
         Divider(color = Color(0xFF2C2C2C))
          Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Cancel", // Actually just deselect
                color = Color.White,
                modifier = Modifier.clickable { onDone() } // Usually Cancel would revert, but for now just close
            )
            Icon(Icons.Default.Done, "Done", tint = Color.White, modifier = Modifier.clickable { onDone() })
        }
    }
}
