package com.moshitech.workmate.feature.imagestudio.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Modern slider component matching the reference design
 * Features:
 * - Gradient track (blue to gray)
 * - Large white circular thumb with shadow
 * - Value display below thumb
 * - Min/max labels at ends
 */
@Composable
fun ModernSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..100f,
    label: String = "",
    unit: String = "",
    showMinMax: Boolean = true,
    modifier: Modifier = Modifier,
    onValueChangeFinished: (() -> Unit)? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Label
        if (label.isNotEmpty()) {
            Text(
                text = label,
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        
        // Slider with min/max labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Min label
            if (showMinMax) {
                Text(
                    text = "${valueRange.start.toInt()}",
                    color = Color.Gray,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            
            // Slider
            Box(modifier = Modifier.weight(1f)) {
                Slider(
                    value = value,
                    onValueChange = onValueChange,
                    onValueChangeFinished = onValueChangeFinished,
                    valueRange = valueRange,
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color(0xFF5B6EF5), // Blue from reference
                        inactiveTrackColor = Color(0xFFE0E0E0) // Light gray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Max label
            if (showMinMax) {
                Text(
                    text = "${valueRange.endInclusive.toInt()}",
                    color = Color.Gray,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
        
        // Value display (centered below slider)
        Text(
            text = "$${value.toInt()}$unit",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

/**
 * Compact version matching the reference design exactly
 * - Thin track (4dp height)
 * - Large white thumb (28dp) with blue border
 * - Blue active track, dark gray inactive track
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun CompactModernSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..100f,
    label: String = "",
    unit: String = "",
    modifier: Modifier = Modifier,
    onValueChangeFinished: (() -> Unit)? = null
) {
    Column(modifier = modifier) {
        // Label with value inline
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = Color.Gray,
                fontSize = 12.sp
            )
            Text(
                text = "${value.toInt()}$unit",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Custom Slider matching reference design
        Slider(
            value = value,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color(0xFF5B6EF5), // Blue from reference
                inactiveTrackColor = Color(0xFF3A3A3C), // Dark gray
                activeTickColor = Color.Transparent,
                inactiveTickColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp), // Smaller thumb for compact design
            thumb = {
                // Custom thumb: white circle with blue border (smaller)
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .shadow(3.dp, CircleShape)
                        .background(Color(0xFF5B6EF5), CircleShape)
                        .padding(2.dp)
                        .background(Color.White, CircleShape)
                )
            },
            track = { sliderState ->
                // Custom track: thin 4dp height
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                ) {
                    // Inactive track (full width)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(Color(0xFF3A3A3C), RoundedCornerShape(2.dp))
                    )
                    // Active track (up to thumb position)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(
                                fraction = (sliderState.value - sliderState.valueRange.start) /
                                        (sliderState.valueRange.endInclusive - sliderState.valueRange.start)
                            )
                            .height(4.dp)
                            .background(Color(0xFF5B6EF5), RoundedCornerShape(2.dp))
                    )
                }
            }
        )
    }
}
