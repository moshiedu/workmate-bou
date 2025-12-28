package com.moshitech.workmate.feature.imagestudio.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun TransparencyCheckerboard(
    modifier: Modifier = Modifier,
    tileSize: Dp = 20.dp,
    color1: Color = Color.White,
    color2: Color = Color(0xFFE0E0E0) // Light Gray
) {
    Canvas(modifier = modifier) {
        val sizePx = tileSize.toPx()
        val numCols = (size.width / sizePx).toInt() + 1
        val numRows = (size.height / sizePx).toInt() + 1

        for (row in 0 until numRows) {
            for (col in 0 until numCols) {
                val color = if ((row + col) % 2 == 0) color1 else color2
                drawRect(
                    color = color,
                    topLeft = Offset(col * sizePx, row * sizePx),
                    size = Size(sizePx, sizePx)
                )
            }
        }
    }
}
