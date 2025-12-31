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
        onUpdateColor: (Boolean, Int) -> Unit,
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
                        Modifier.fillMaxWidth()
                                .background(Color(0xFF1E1E1E))
                                .padding(bottom = 16.dp)
        ) {
                // --- CONTROL PANEL (Changes based on selection) ---
                Box(
                        modifier = Modifier.fillMaxWidth().padding(16.dp).heightIn(min = 60.dp),
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
                                                                Modifier.fillMaxWidth()
                                                                        .padding(bottom = 8.dp),
                                                        horizontalArrangement = Arrangement.Center
                                                ) {
                                                        Text(
                                                                "Solid",
                                                                modifier =
                                                                        Modifier.clickable {
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
                                                                        Modifier.clickable {
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
                                                                                        layer.tintColor
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
                                                                                        Modifier.size(
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
                                                                                                Modifier.size(
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
                                                                                                                                )
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
                                                                                listOf(
                                                                                        android.graphics
                                                                                                .Color
                                                                                                .RED,
                                                                                        android.graphics
                                                                                                .Color
                                                                                                .YELLOW
                                                                                ),
                                                                                listOf(
                                                                                        android.graphics
                                                                                                .Color
                                                                                                .BLUE,
                                                                                        android.graphics
                                                                                                .Color
                                                                                                .CYAN
                                                                                ),
                                                                                listOf(
                                                                                        android.graphics
                                                                                                .Color
                                                                                                .MAGENTA,
                                                                                        android.graphics
                                                                                                .Color
                                                                                                .BLUE
                                                                                ),
                                                                                listOf(
                                                                                        android.graphics
                                                                                                .Color
                                                                                                .GREEN,
                                                                                        android.graphics
                                                                                                .Color
                                                                                                .YELLOW
                                                                                ),
                                                                                listOf(
                                                                                        android.graphics
                                                                                                .Color
                                                                                                .BLACK,
                                                                                        android.graphics
                                                                                                .Color
                                                                                                .WHITE
                                                                                ),
                                                                                listOf(
                                                                                        android.graphics
                                                                                                .Color
                                                                                                .RED,
                                                                                        android.graphics
                                                                                                .Color
                                                                                                .BLUE
                                                                                ),
                                                                                listOf(
                                                                                        0xFFFF0000
                                                                                                .toInt(),
                                                                                        0xFFFFA500
                                                                                                .toInt(),
                                                                                        0xFFFFFF00
                                                                                                .toInt(),
                                                                                        0xFF008000
                                                                                                .toInt(),
                                                                                        0xFF0000FF
                                                                                                .toInt(),
                                                                                        0xFF4B0082
                                                                                                .toInt(),
                                                                                        0xFFEE82EE
                                                                                                .toInt()
                                                                                ), // Rainbow
                                                                                listOf(
                                                                                        0xFFFF512F
                                                                                                .toInt(),
                                                                                        0xFFDD2476
                                                                                                .toInt()
                                                                                ), // Sunset
                                                                                listOf(
                                                                                        0xFF4568DC
                                                                                                .toInt(),
                                                                                        0xFFB06AB3
                                                                                                .toInt()
                                                                                ), // Purple Blue
                                                                                listOf(
                                                                                        0xFF009FFF
                                                                                                .toInt(),
                                                                                        0xFFec2F4B
                                                                                                .toInt()
                                                                                ), // Blue Red
                                                                                listOf(
                                                                                        0xFFCC2B5E
                                                                                                .toInt(),
                                                                                        0xFF753A88
                                                                                                .toInt()
                                                                                ), // Purple Pink
                                                                                listOf(
                                                                                        0xFF1D976C
                                                                                                .toInt(),
                                                                                        0xFF93F9B9
                                                                                                .toInt()
                                                                                ) // Green Teal
                                                                        )

                                                                gradients.forEach { gradParams ->
                                                                        Box(
                                                                                modifier =
                                                                                        Modifier.size(
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
                                                                                        Modifier.size(
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
                                Modifier.fillMaxWidth()
                                        .horizontalScroll(rememberScrollState())
                                        .padding(vertical = 12.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                        StickerTool.values().forEach { tool ->
                                Column(
                                        modifier =
                                                Modifier.clickable { activeTool = tool }
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
                                                                Modifier.padding(top = 4.dp)
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
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
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
