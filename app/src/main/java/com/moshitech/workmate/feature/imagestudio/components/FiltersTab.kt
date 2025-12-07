package com.moshitech.workmate.feature.imagestudio.components

import android.graphics.ColorMatrix
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.moshitech.workmate.feature.imagestudio.util.MonetizationManager

data class FilterItem(
    val id: String,
    val name: String,
    val matrix: FloatArray,
    val colorOverlay: Color
)

@Composable
fun FiltersTab(
    activeFilterId: String?,
    onFilterSelected: (String, FloatArray) -> Unit,
    onClearFilter: () -> Unit
) {
    val filters = getFilterPresets()

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
    ) {
        // Original / None
        item {
            FilterPreviewItem(
                name = "None",
                color = Color.Gray,
                isSelected = activeFilterId == null,
                isLocked = false,
                onClick = onClearFilter
            )
        }

        items(filters) { filter ->
            val isLocked = MonetizationManager.isFilterLocked(filter.id)
            FilterPreviewItem(
                name = filter.name,
                color = filter.colorOverlay,
                isSelected = activeFilterId == filter.id,
                isLocked = isLocked,
                onClick = { onFilterSelected(filter.id, filter.matrix) }
            )
        }
    }
}

@Composable
fun FilterPreviewItem(
    name: String,
    color: Color,
    isSelected: Boolean,
    isLocked: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(color)
                .border(
                    width = if (isSelected) 3.dp else 0.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isLocked) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
        )
    }
}

fun getFilterPresets(): List<FilterItem> {
    return listOf(
        FilterItem("bw", "B&W", createBWMatrix(), Color.Gray),
        FilterItem("sepia", "Sepia", createSepiaMatrix(), Color(0xFF704214)),
        // Blur Filters
        FilterItem("blur", "Blur", createBlurMatrix(), Color(0xFFB0B0B0)),
        FilterItem("motion_blur", "Motion", createMotionBlurMatrix(), Color(0xFF909090)),
        // Artistic Filters
        FilterItem("oil_paint", "Oil Paint", createOilPaintMatrix(), Color(0xFFFF6347)),
        FilterItem("sketch", "Sketch", createSketchMatrix(), Color(0xFF696969)),
        // Enhancement Filters
        FilterItem("sharpen", "Sharpen", createSharpenMatrix(), Color(0xFF00CED1)),
        FilterItem("edge_detect", "Edge", createEdgeDetectMatrix(), Color(0xFF4B0082)),
        // Pro Filters - Advanced
        FilterItem("pro_vivid", "Vivid", createVividMatrix(), Color(0xFFFF1493)),
        FilterItem("pro_cinematic", "Cinematic", createCinematicMatrix(), Color(0xFF1E90FF)),
        FilterItem("pro_hdr", "HDR", createHDRMatrix(), Color(0xFFFFD700)),
        FilterItem("pro_warm", "Warm", createWarmMatrix(), Color(0xFFFFD700)),
        FilterItem("pro_cool", "Cool", createCoolMatrix(), Color(0xFF00BFFF)),
        FilterItem("pro_vintage", "Vintage", createVintageMatrix(), Color(0xFF8B4513))
    )
}

// Helpers for Matrices
fun createBWMatrix(): FloatArray {
    val cm = ColorMatrix()
    cm.setSaturation(0f)
    return cm.array
}

fun createSepiaMatrix(): FloatArray {
    val cm = ColorMatrix()
    cm.setSaturation(0f)
    val sepia = ColorMatrix()
    sepia.setScale(1f, 0.95f, 0.82f, 1f)
    cm.postConcat(sepia)
    return cm.array
}

// Simple warmth increase (Red up, Blue down)
fun createWarmMatrix(): FloatArray {
    return floatArrayOf(
        1.1f, 0f, 0f, 0f, 0f,
        0f, 1f, 0f, 0f, 0f,
        0f, 0f, 0.9f, 0f, 0f,
        0f, 0f, 0f, 1f, 0f
    )
}

// Simple cool increase (Red down, Blue up)
fun createCoolMatrix(): FloatArray {
    return floatArrayOf(
        0.9f, 0f, 0f, 0f, 0f,
        0f, 1f, 0f, 0f, 0f,
        0f, 0f, 1.1f, 0f, 0f,
        0f, 0f, 0f, 1f, 0f
    )
}

// Vintage: Low saturation + contrast boost
fun createVintageMatrix(): FloatArray {
    val cm = ColorMatrix()
    cm.setSaturation(0.5f)
    val contrast = ColorMatrix(floatArrayOf(
        1.2f, 0f, 0f, 0f, 20f,
        0f, 1.2f, 0f, 0f, 20f,
        0f, 0f, 1.2f, 0f, 20f,
        0f, 0f, 0f, 1f, 0f
    ))
    cm.postConcat(contrast)
    return cm.array
}

// ADVANCED PRO FILTERS

// Vivid: Enhanced saturation and vibrance
fun createVividMatrix(): FloatArray {
    val cm = ColorMatrix()
    cm.setSaturation(1.4f) // Boost saturation
    
    // Enhance contrast slightly
    val contrast = ColorMatrix(floatArrayOf(
        1.15f, 0f, 0f, 0f, -10f,
        0f, 1.15f, 0f, 0f, -10f,
        0f, 0f, 1.15f, 0f, -10f,
        0f, 0f, 0f, 1f, 0f
    ))
    cm.postConcat(contrast)
    return cm.array
}

// Cinematic: Teal/Orange color grading (popular in movies)
fun createCinematicMatrix(): FloatArray {
    return floatArrayOf(
        1.1f, 0.05f, 0f, 0f, 0f,      // Red: boost + slight green
        0f, 0.95f, 0.05f, 0f, 0f,     // Green: slight reduction + blue tint
        0f, 0.1f, 1.15f, 0f, 0f,      // Blue: boost + teal from green
        0f, 0f, 0f, 1f, 0f
    )
}

// HDR: High Dynamic Range effect (enhanced contrast and detail)
fun createHDRMatrix(): FloatArray {
    val cm = ColorMatrix()
    cm.setSaturation(1.2f) // Moderate saturation boost
    
    // Strong contrast with midtone preservation
    val hdrContrast = ColorMatrix(floatArrayOf(
        1.3f, 0f, 0f, 0f, -25f,
        0f, 1.3f, 0f, 0f, -25f,
        0f, 0f, 1.3f, 0f, -25f,
        0f, 0f, 0f, 1f, 0f
    ))
    cm.postConcat(hdrContrast)
    return cm.array
}

// NEW FILTERS - PHASE 3

// Blur: Simulated blur effect using desaturation and brightness
fun createBlurMatrix(): FloatArray {
    val cm = ColorMatrix()
    cm.setSaturation(0.8f) // Slight desaturation for blur effect
    
    // Reduce contrast slightly
    val blur = ColorMatrix(floatArrayOf(
        0.9f, 0.05f, 0.05f, 0f, 10f,
        0.05f, 0.9f, 0.05f, 0f, 10f,
        0.05f, 0.05f, 0.9f, 0f, 10f,
        0f, 0f, 0f, 1f, 0f
    ))
    cm.postConcat(blur)
    return cm.array
}

// Motion Blur: Directional blur simulation
fun createMotionBlurMatrix(): FloatArray {
    return floatArrayOf(
        0.85f, 0.1f, 0.05f, 0f, 5f,
        0.05f, 0.85f, 0.1f, 0f, 5f,
        0.05f, 0.05f, 0.9f, 0f, 5f,
        0f, 0f, 0f, 1f, 0f
    )
}

// Oil Painting: Rich, saturated colors with enhanced contrast
fun createOilPaintMatrix(): FloatArray {
    val cm = ColorMatrix()
    cm.setSaturation(1.5f) // High saturation
    
    // Boost contrast
    val oilPaint = ColorMatrix(floatArrayOf(
        1.25f, 0f, 0f, 0f, -15f,
        0f, 1.25f, 0f, 0f, -15f,
        0f, 0f, 1.25f, 0f, -15f,
        0f, 0f, 0f, 1f, 0f
    ))
    cm.postConcat(oilPaint)
    return cm.array
}

// Sketch: High contrast black and white with edge emphasis
fun createSketchMatrix(): FloatArray {
    val cm = ColorMatrix()
    cm.setSaturation(0f) // Full desaturation
    
    // Very high contrast for sketch effect
    val sketch = ColorMatrix(floatArrayOf(
        1.5f, 0f, 0f, 0f, -40f,
        0f, 1.5f, 0f, 0f, -40f,
        0f, 0f, 1.5f, 0f, -40f,
        0f, 0f, 0f, 1f, 0f
    ))
    cm.postConcat(sketch)
    return cm.array
}

// Sharpen: Enhanced edges and clarity
fun createSharpenMatrix(): FloatArray {
    return floatArrayOf(
        1.5f, -0.2f, -0.2f, 0f, 0f,
        -0.2f, 1.5f, -0.2f, 0f, 0f,
        -0.2f, -0.2f, 1.5f, 0f, 0f,
        0f, 0f, 0f, 1f, 0f
    )
}

// Edge Detect: Emphasize edges, reduce midtones
fun createEdgeDetectMatrix(): FloatArray {
    val cm = ColorMatrix()
    cm.setSaturation(0.3f) // Low saturation
    
    // Edge detection emphasis
    val edge = ColorMatrix(floatArrayOf(
        2f, -0.5f, -0.5f, 0f, -50f,
        -0.5f, 2f, -0.5f, 0f, -50f,
        -0.5f, -0.5f, 2f, 0f, -50f,
        0f, 0f, 0f, 1f, 0f
    ))
    cm.postConcat(edge)
    return cm.array
}
