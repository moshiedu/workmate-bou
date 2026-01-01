package com.moshitech.workmate.feature.imagestudio.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.RotateLeft
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moshitech.workmate.feature.imagestudio.viewmodel.StickerLayer

private enum class StickerTool(val label: String, val icon: ImageVector? = null) {
    STICKER("Sticker"),
    OPACITY("Opacity"),
    COLOR("Color"),
    BLEND("Blend"),
    BORDER("Border"),
    SHADOW("Shadow"),
    FLIP("Flip/Rotate") // Combined for simplicity
}

@Composable
fun StickerEditorToolbar(
    layer: StickerLayer,
    onUpdateOpacity: (Float) -> Unit,
    onUpdateColor: (Boolean, Int, Float) -> Unit,
    onUpdateGradient: (Boolean, List<Int>) -> Unit,
    onRequestEyedropper: () -> Unit,
    onUpdateBlend: (BlendMode) -> Unit,
    onUpdateBorder: (Boolean, Int, Float) -> Unit,
    onUpdateShadow: (Boolean, Int, Float, Float, Float) -> Unit,
    onFlip: (Boolean) -> Unit, // isVertical
    onRotate: (Boolean) -> Unit, // isClockwise
    onChangeSticker: () -> Unit, // NEW: Open sticker picker
    onDone: () -> Unit
) {
    var activeTool by remember { mutableStateOf(StickerTool.OPACITY) }

    Column(
        modifier =
            Modifier
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
                StickerTool.STICKER -> {
                    // Show button to change sticker
                    Button(
                        onClick = onChangeSticker,
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50)
                            )
                    ) {
                        Icon(Icons.Default.Edit, "Change Sticker")
                        Spacer(Modifier.width(8.dp))
                        Text("Change Sticker")
                    }
                }

                StickerTool.OPACITY -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Opacity: ${(layer.opacity * 100).toInt()}%",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                        Slider(
                            value = layer.opacity,
                            onValueChange = onUpdateOpacity,
                            valueRange = 0f..1f,
                            colors =
                                SliderDefaults.colors(
                                    thumbColor = Color.White,
                                    activeTrackColor =
                                        Color.White,
                                    inactiveTrackColor =
                                        Color.Gray
                                )
                        )
                    }
                }

                StickerTool.COLOR -> {
                    var showGradientTab by remember {
                        mutableStateOf(layer.isGradient)
                    }

                    Column {
                        // Sub-tabs: Solid vs Gradient
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "Solid",
                                modifier =
                                    Modifier
                                        .clickable {
                                            showGradientTab =
                                                false
                                        }
                                        .padding(8.dp),
                                color =
                                    if (!showGradientTab)
                                        Color.White
                                    else Color.Gray,
                                fontWeight =
                                    if (!showGradientTab)
                                        FontWeight.Bold
                                    else FontWeight.Normal
                            )
                            Text(
                                "Gradient",
                                modifier =
                                    Modifier
                                        .clickable {
                                            showGradientTab =
                                                true
                                        }
                                        .padding(8.dp),
                                color =
                                    if (showGradientTab)
                                        Color.White
                                    else Color.Gray,
                                fontWeight =
                                    if (showGradientTab)
                                        FontWeight.Bold
                                    else FontWeight.Normal
                            )
                        }

                        if (!showGradientTab) {
                            // --- SOLID COLOR (Tint) ---
                            Row(
                                verticalAlignment =
                                    Alignment.CenterVertically
                            ) {
                                Text(
                                    "Enable Tint",
                                    color = Color.White,
                                    modifier =
                                        Modifier.weight(1f)
                                )
                                Switch(
                                    checked = layer.hasTint,
                                    onCheckedChange = {
                                        onUpdateColor(
                                            it,
                                            layer.tintColor,
                                            layer.tintStrength
                                        )
                                    },
                                    colors =
                                        SwitchDefaults
                                            .colors(
                                                checkedThumbColor =
                                                    Color.White,
                                                checkedTrackColor =
                                                    Color(
                                                        0xFF007AFF
                                                    )
                                            )
                                )
                            }

                            if (layer.hasTint) {
                                // TINT STRENGTH SLIDER
                                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                    Text(
                                        "Intensity: ${(layer.tintStrength * 100).toInt()}%",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(start = 16.dp)
                                    )
                                    Slider(
                                        value = layer.tintStrength,
                                        onValueChange = { strength ->
                                            onUpdateColor(true, layer.tintColor, strength)
                                        },
                                        valueRange = 0f..1f,
                                        colors = SliderDefaults.colors(
                                            thumbColor = Color.White,
                                            activeTrackColor = Color.White,
                                            inactiveTrackColor = Color.Gray
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )
                                }

                                Spacer(
                                    modifier =
                                        Modifier.height(
                                            8.dp
                                        )
                                )
                                Row(
                                    modifier =
                                        Modifier.horizontalScroll(
                                            rememberScrollState()
                                        ),
                                    horizontalArrangement =
                                        Arrangement
                                            .spacedBy(
                                                12.dp
                                            ),
                                    verticalAlignment =
                                        Alignment
                                            .CenterVertically
                                ) {
                                    // Eyedropper
                                    Box(
                                        modifier =
                                            Modifier
                                                .size(
                                                    36.dp
                                                )
                                                .background(
                                                    Color.DarkGray,
                                                    CircleShape
                                                )
                                                .clickable {
                                                    onRequestEyedropper()
                                                },
                                        contentAlignment =
                                            Alignment
                                                .Center
                                    ) {
                                        Icon(
                                            Icons.Default
                                                .Edit,
                                            "Eyedropper",
                                            tint =
                                                Color.White
                                        )
                                    }

                                    // Colors
                                    val colors =
                                        listOf(
                                            Color.White,
                                            Color.Black,
                                            Color.Red,
                                            Color.Blue,
                                            Color.Green,
                                            Color.Yellow,
                                            Color.Cyan,
                                            Color.Magenta,
                                            Color(
                                                0xFFFFA500
                                            ), // Orange
                                            Color(
                                                0xFF800080
                                            ), // Purple
                                            Color(
                                                0xFFFFC0CB
                                            ), // Pink
                                            Color(
                                                0xFF008080
                                            ), // Teal
                                            Color(
                                                0xFF4B0082
                                            ), // Indigo
                                            Color(
                                                0xFFEE82EE
                                            ), // Violet
                                            Color(
                                                0xFFA52A2A
                                            ), // Brown
                                            Color(
                                                0xFF808080
                                            ), // Gray
                                            Color(
                                                0xFF00FF7F
                                            ), // Spring
                                            // Green
                                            Color(
                                                0xFFFFD700
                                            ), // Gold
                                            Color(
                                                0xFFDC143C
                                            ) // Crimson
                                        )
                                    colors.forEach { color ->
                                        Box(
                                            modifier =
                                                Modifier
                                                    .size(
                                                        36.dp
                                                    )
                                                    .background(
                                                        color,
                                                        CircleShape
                                                    )
                                                    .border(
                                                        width =
                                                            if (layer.tintColor ==
                                                                android.graphics
                                                                    .Color
                                                                    .argb(
                                                                        (color.alpha *
                                                                                255)
                                                                            .toInt(),
                                                                        (color.red *
                                                                                255)
                                                                            .toInt(),
                                                                        (color.green *
                                                                                255)
                                                                            .toInt(),
                                                                        (color.blue *
                                                                                255)
                                                                            .toInt()
                                                                    )
                                                            )
                                                                2.dp
                                                            else
                                                                0.dp,
                                                        color = 
                                                            if (androidx.core.graphics.ColorUtils.calculateLuminance(android.graphics.Color.argb((color.alpha*255).toInt(), (color.red*255).toInt(), (color.green*255).toInt(), (color.blue*255).toInt())) > 0.5) 
                                                                Color.Black 
                                                            else 
                                                                Color.White,
                                                        shape =
                                                            CircleShape
                                                    )
                                                    .clickable {
                                                        onUpdateColor(
                                                            true,
                                                            android.graphics
                                                                .Color
                                                                .argb(
                                                                    (color.alpha *
                                                                            255)
                                                                        .toInt(),
                                                                    (color.red *
                                                                            255)
                                                                        .toInt(),
                                                                    (color.green *
                                                                            255)
                                                                        .toInt(),
                                                                    (color.blue *
                                                                            255)
                                                                        .toInt()
                                                                ),
                                                            layer.tintStrength // Pass current strength
                                                        )

                                                    }
                                        )
                                    }
                                }
                            }
                        } else {
                            // --- GRADIENT ---
                            Text(
                                "Standard Gradients",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                modifier =
                                    Modifier.padding(
                                        bottom = 8.dp
                                    )
                            )
                            Row(
                                modifier =
                                    Modifier.horizontalScroll(
                                        rememberScrollState()
                                    ),
                                horizontalArrangement =
                                    Arrangement.spacedBy(12.dp)
                            ) {
                                val gradients =
                                    listOf(
                                        // Modern Gradients
                                        listOf(0xFF00C6FF.toInt(), 0xFF0072FF.toInt()), // Facebook Messenger
                                        listOf(0xFF833ab4.toInt(), 0xFFfd1d1d.toInt(), 0xFFfcb045.toInt()), // Instagram
                                        listOf(0xFF11998e.toInt(), 0xFF38ef7d.toInt()), // Quepal
                                        listOf(0xFFFC466B.toInt(), 0xFF3F5EFB.toInt()), // Sublime Vivid
                                        listOf(0xFF00b09b.toInt(), 0xFF96c93d.toInt()), // Ohhappiness
                                        listOf(0xFF8E2DE2.toInt(), 0xFF4A00E0.toInt()), // Amin
                                        listOf(0xFFff9966.toInt(), 0xFFff5e62.toInt()), // Orange coral
                                        listOf(0xFF56ab2f.toInt(), 0xFFa8e063.toInt()), // Lush
                                        listOf(0xFFF2994A.toInt(), 0xFFF2C94C.toInt()), // Sun
                                        listOf(0xFFe1eec3.toInt(), 0xFFf05053.toInt()), // Kyoo Pal
                                        listOf(0xFF7f00ff.toInt(), 0xFFe100ff.toInt()), // Amin (Purple)
                                        listOf(0xFFc21500.toInt(), 0xFFffc500.toInt()), // Kyoo Tah
                                        listOf(0xFF00c3ff.toInt(), 0xFFffff1c.toInt()), // By Design
                                        listOf(0xFF654ea3.toInt(), 0xFFeaafc8.toInt()), // Ultra Voilet
                                        listOf(0xFF333333.toInt(), 0xFFdd1818.toInt()), // Pure Lust
                                        listOf(0xFF1D976C.toInt(), 0xFF93F9B9.toInt()), // Green Beach
                                        listOf(0xFF232526.toInt(), 0xFF414345.toInt()), // Midnight City
                                        listOf(0xFFbdc3c7.toInt(), 0xFF2c3e50.toInt()), // Grade Grey
                                        listOf(0xFFee9ca7.toInt(), 0xFFffdde1.toInt()), // Piggy Pink
                                        listOf(0xFF2193b0.toInt(), 0xFF6dd5ed.toInt()), // Cool Blues
                                        listOf(0xFFcc2b5e.toInt(), 0xFF753a88.toInt()), // Purple Love
                                        listOf(0xFF42275a.toInt(), 0xFF734b6d.toInt()), // Mauve
                                        listOf(0xFFde6262.toInt(), 0xFFffb88c.toInt()), // A Lost Memory
                                        listOf(0xFF06beb6.toInt(), 0xFF48b1bf.toInt()), // Sea Blizz
                                        listOf(0xFFeb3349.toInt(), 0xFFf45c43.toInt()), // Cherry
                                        listOf(0xFFDD5E89.toInt(), 0xFFF7BB97.toInt()), // Pinky
                                        listOf(0xFF516395.toInt(), 0xFF614385.toInt()), // Kashmir
                                        listOf(0xFFeacda3.toInt(), 0xFFd6ae7b.toInt()), // Metalic 
                                        listOf(0xFF02aab0.toInt(), 0xFF00cdac.toInt()), // Green Teel
                                        listOf(0xFFd3cce3.toInt(), 0xFFe9e4f0.toInt()), // Light Purple
                                        listOf(0xFF20002c.toInt(), 0xFFcbb4d4.toInt()), // Kye Meh
                                        listOf(0xFFC9FFBF.toInt(), 0xFFFFAFBD.toInt()), // Dep
                                        listOf(0xFF3ca55c.toInt(), 0xFFb5ac49.toInt()), // Emerald Water
                                        listOf(0xFFCC95C0.toInt(), 0xFFDBD4B4.toInt(), 0xFF7AA1D2.toInt()), // Pastel
                                        listOf(0xFF000428.toInt(), 0xFF004e92.toInt()), // Frost
                                        listOf(0xFFDA22FF.toInt(), 0xFF9733EE.toInt())  // 80's Purple
                                    )


                                gradients.forEach { gradParams ->
                                    Box(
                                        modifier =
                                            Modifier
                                                .size(
                                                    40.dp
                                                )
                                                .clip(
                                                    CircleShape
                                                )
                                                .background(
                                                    androidx.compose
                                                        .ui
                                                        .graphics
                                                        .Brush
                                                        .linearGradient(
                                                            colors =
                                                                gradParams
                                                                    .map {
                                                                        Color(
                                                                            it
                                                                        )
                                                                    }
                                                        )
                                                )
                                                .clickable {
                                                    onUpdateGradient(
                                                        true,
                                                        gradParams
                                                    )
                                                }
                                    )
                                }
                            }
                        }
                    }
                }

                StickerTool.BLEND -> {
                    val blendModes =
                        listOf(
                            BlendMode.SrcOver to "Normal",
                            BlendMode.Multiply to "Multiply",
                            BlendMode.Screen to "Screen",
                            BlendMode.Overlay to "Overlay",
                            BlendMode.Darken to "Darken",
                            BlendMode.Lighten to "Lighten",
                            BlendMode.ColorDodge to "Color Dodge",
                            BlendMode.ColorBurn to "Color Burn",
                            BlendMode.Hardlight to "Hard Light",
                            BlendMode.Softlight to "Soft Light",
                            BlendMode.Difference to "Difference",
                            BlendMode.Exclusion to "Exclusion",
                            BlendMode.Hue to "Hue",
                            BlendMode.Saturation to "Saturation",
                            BlendMode.Color to "Color",
                            BlendMode.Luminosity to "Luminosity",
                            BlendMode.Plus to "Plus",
                            BlendMode.Modulate to "Modulate"
                        )
                    Row(
                        modifier =
                            Modifier.horizontalScroll(
                                rememberScrollState()
                            ),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        blendModes.forEach { (mode, label) ->
                            FilterChip(
                                selected = layer.blendMode == mode,
                                onClick = { onUpdateBlend(mode) },
                                label = { Text(label) },
                                colors =
                                    FilterChipDefaults
                                        .filterChipColors(
                                            selectedContainerColor =
                                                Color.White,
                                            selectedLabelColor =
                                                Color.Black,
                                            containerColor =
                                                Color(
                                                    0xFF2C2C2C
                                                ),
                                            labelColor =
                                                Color.White
                                        ),
                                border = null
                            )
                        }
                    }
                }

                StickerTool.BORDER -> {
                    Column {
                        Row(
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {
                            Text(
                                "Enable Border",
                                color = Color.White,
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = layer.hasBorder,
                                onCheckedChange = {
                                    onUpdateBorder(
                                        it,
                                        layer.borderColor,
                                        layer.borderWidth
                                    )
                                },
                                colors =
                                    SwitchDefaults.colors(
                                        checkedThumbColor =
                                            Color.White,
                                        checkedTrackColor =
                                            Color(
                                                0xFF007AFF
                                            )
                                    )
                            )
                        }
                        if (layer.hasBorder) {
                            Spacer(modifier = Modifier.height(8.dp))
                            // Simple Width Slider
                            Slider(
                                value = layer.borderWidth,
                                onValueChange = {
                                    onUpdateBorder(
                                        true,
                                        layer.borderColor,
                                        it
                                    )
                                },
                                valueRange = 0f..20f
                            )
                            // Simple Color Row (Mock)
                            Row(
                                horizontalArrangement =
                                    Arrangement.spacedBy(8.dp)
                            ) {
                                val colors =
                                    listOf(
                                        Color.White,
                                        Color.Black,
                                        Color.Red,
                                        Color.Blue,
                                        Color.Green,
                                        Color.Yellow
                                    )
                                colors.forEach { color ->
                                    Box(
                                        modifier =
                                            Modifier
                                                .size(
                                                    24.dp
                                                )
                                                .background(
                                                    color,
                                                    CircleShape
                                                )
                                                .border(
                                                    1.dp,
                                                    Color.Gray,
                                                    CircleShape
                                                )
                                                .clickable {
                                                    onUpdateBorder(
                                                        true,
                                                        android.graphics
                                                            .Color
                                                            .argb(
                                                                (color.alpha *
                                                                        255)
                                                                    .toInt(),
                                                                (color.red *
                                                                        255)
                                                                    .toInt(),
                                                                (color.green *
                                                                        255)
                                                                    .toInt(),
                                                                (color.blue *
                                                                        255)
                                                                    .toInt()
                                                            ),
                                                        layer.borderWidth
                                                    )
                                                }
                                    )
                                }
                            }
                        }
                    }
                }

                StickerTool.SHADOW -> {
                    Column {
                        Row(
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {
                            Text(
                                "Drop Shadow",
                                color = Color.White,
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = layer.hasShadow,
                                onCheckedChange = {
                                    onUpdateShadow(
                                        it,
                                        layer.shadowColor,
                                        layer.shadowBlur,
                                        layer.shadowOffsetX,
                                        layer.shadowOffsetY
                                    )
                                },
                                colors =
                                    SwitchDefaults.colors(
                                        checkedThumbColor =
                                            Color.White,
                                        checkedTrackColor =
                                            Color(
                                                0xFF007AFF
                                            )
                                    )
                            )
                        }
                    }
                }

                StickerTool.FLIP -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Rotate Left
                        IconButton(onClick = { onRotate(false) }) {
                            Icon(
                                imageVector =
                                    Icons.Default.RotateLeft,
                                contentDescription = "Rotate Left",
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))

                        // Rotate Right
                        IconButton(onClick = { onRotate(true) }) {
                            Icon(
                                imageVector =
                                    Icons.Default.RotateRight,
                                contentDescription = "Rotate Right",
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))

                        // Flip Horizontal
                        IconButton(onClick = { onFlip(false) }) {
                            Icon(
                                imageVector =
                                    Icons.Default.SwapHoriz,
                                contentDescription =
                                    "Flip Horizontal",
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))

                        // Flip Vertical
                        IconButton(onClick = { onFlip(true) }) {
                            Icon(
                                imageVector =
                                    Icons.Default.SwapHoriz,
                                contentDescription =
                                    "Flip Vertical",
                                tint = Color.White,
                                modifier =
                                    Modifier.graphicsLayer(
                                        rotationZ = 90f
                                    )
                            )
                        }
                    }
                }
            }
        }

        Divider(color = Color(0xFF2C2C2C))

        // --- MENU TABS ---
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            StickerTool.values().forEach { tool ->
                Column(
                    modifier =
                        Modifier
                            .clickable { activeTool = tool }
                            .padding(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = tool.label,
                        color =
                            if (activeTool == tool) Color.White
                            else Color.Gray,
                        fontSize = 14.sp,
                        fontWeight =
                            if (activeTool == tool) FontWeight.Bold
                            else FontWeight.Normal
                    )
                    if (activeTool == tool) {
                        Box(
                            modifier =
                                Modifier
                                    .padding(top = 4.dp)
                                    .size(4.dp)
                                    .background(
                                        Color.White,
                                        CircleShape
                                    )
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
                modifier =
                    Modifier.clickable {
                        onDone()
                    } // Usually Cancel would revert, but for now just close
            )
            Icon(
                Icons.Default.Done,
                "Done",
                tint = Color.White,
                modifier = Modifier.clickable { onDone() }
            )
        }
    }
}
