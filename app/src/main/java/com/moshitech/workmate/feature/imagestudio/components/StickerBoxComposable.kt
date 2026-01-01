package com.moshitech.workmate.feature.imagestudio.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateRotation
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.moshitech.workmate.feature.imagestudio.viewmodel.StickerLayer
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun StickerBoxComposable(
        layer: StickerLayer,
        isSelected: Boolean,
        bitmapScale: Float,
        bitmapOffset: Offset,
        onSelect: (String) -> Unit,
        onTransform:
                (
                        String,
                        Offset,
                        Float,
                        Float,
                        Float) -> Unit, // id, pan, scaleXChange, scaleYChange, rotation
        onTransformEnd: (String) -> Unit,
        onDelete: (String) -> Unit,
        onFlip: ((String) -> Unit)? = null,
        modifier: Modifier = Modifier
) {
    val density = androidx.compose.ui.platform.LocalDensity.current
    val viewConfiguration = androidx.compose.ui.platform.LocalViewConfiguration.current

    val currentLayerState = androidx.compose.runtime.rememberUpdatedState(layer)
    val currentLayer = currentLayerState.value

    // CRITICAL: Match CompositeRenderer's size calculation exactly
    // CompositeRenderer uses: baseSize = (100f * density) / bitmapScale
    // This gives us the base size in BITMAP PIXELS
    // Then we apply scale and convert back to screen coordinates

    // Calculate base size in bitmap pixels (matches renderer)
    val baseSizePx = (100f * density.density) / bitmapScale

    // Apply sticker scale to get final size in bitmap pixels
    val widthPx = baseSizePx * abs(layer.scaleX)
    val heightPx = baseSizePx * abs(layer.scaleY)

    // Convert to screen pixels by applying bitmapScale
    val screenWidthPx = widthPx * bitmapScale
    val screenHeightPx = heightPx * bitmapScale

    // Convert to DP for Compose
    val widthDp = with(density) { screenWidthPx.toDp() }
    val heightDp = with(density) { screenHeightPx.toDp() }

    Box(
            modifier =
                    modifier
                            // Position in screen coordinates using bitmap coords
                            .offset {
                                IntOffset(
                                        (layer.x * bitmapScale + bitmapOffset.x).roundToInt(),
                                        (layer.y * bitmapScale + bitmapOffset.y).roundToInt()
                                )
                            }
                            .size(widthDp, heightDp)
                            .graphicsLayer {
                                rotationZ = layer.rotation
                                // Scale is handled by layout size now.
                                // Handles follow layout corners naturally.
                            }
                            // Main Body Gesture (Pan, Zoom, Rotate - MultiTouch)
                            .pointerInput(layer.id, layer.isLocked || !isSelected) {
                                if (currentLayer.isLocked || !isSelected) return@pointerInput

                                awaitEachGesture {
                                    var zoom = 1f
                                    var pan = Offset.Zero
                                    var rotation = 0f
                                    var pastTouchSlop = false

                                    awaitFirstDown(requireUnconsumed = false)

                                    while (true) {
                                        val event = awaitPointerEvent()
                                        if (event.changes.any { it.isConsumed }) break

                                        val zoomChange = event.calculateZoom()
                                        val rotationChange = event.calculateRotation()
                                        val panChange = event.calculatePan()

                                        if (!pastTouchSlop) {
                                            zoom *= zoomChange
                                            rotation += rotationChange
                                            pan += panChange

                                            val centroidSize =
                                                    event.calculateCentroidSize(useCurrent = false)
                                            val zoomMotion = abs(1 - zoom) * centroidSize
                                            val rotationMotion =
                                                    abs(
                                                            rotation *
                                                                    Math.PI.toFloat() *
                                                                    centroidSize / 180f
                                                    )
                                            val panMotion = pan.getDistance()

                                            if (zoomMotion > viewConfiguration.touchSlop ||
                                                            rotationMotion >
                                                                    viewConfiguration.touchSlop ||
                                                            panMotion > viewConfiguration.touchSlop
                                            ) {
                                                pastTouchSlop = true
                                            }
                                        }

                                        if (pastTouchSlop) {
                                            // Global Shift Correction for Pan
                                            // 1. Un-Rotate (Convert Local Rotated -> Global)
                                            // Pan is in screen pixels (layout coordinates)
                                            val rad =
                                                    Math.toRadians(currentLayer.rotation.toDouble())
                                            val cos = Math.cos(rad)
                                            val sin = Math.sin(rad)

                                            val rotX = panChange.x * cos - panChange.y * sin
                                            val rotY = panChange.x * sin + panChange.y * cos

                                            // 2. Apply Bitmap Scale Correction (Pixels -> Image
                                            // Coords)
                                            val correctedPan =
                                                    Offset(
                                                            rotX.toFloat() / bitmapScale,
                                                            rotY.toFloat() / bitmapScale
                                                    )

                                            // Apply local zoom to both X and Y (Aspect Ratio
                                            // Preserved on MultiTouch Zoom)
                                            onTransform(
                                                    currentLayer.id,
                                                    correctedPan,
                                                    zoomChange,
                                                    zoomChange,
                                                    rotationChange
                                            )
                                            event.changes.forEach {
                                                if (it.positionChanged()) it.consume()
                                            }
                                        }

                                        if (!event.changes.any { it.pressed }) break
                                    }

                                    if (pastTouchSlop) {
                                        onTransformEnd(currentLayer.id)
                                    }
                                }
                            }
                            .pointerInput(layer.id) { detectTapGestures { onSelect(layer.id) } }
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {

            // --- CONTENT BOX (Handles Flip, Opacity, Border, Shadow) ---
            Box(
                    modifier =
                            Modifier.fillMaxSize()
                                    .graphicsLayer {
                                        // Apply Flip specific to content
                                        scaleX = if (layer.isFlipped) -1f else 1f
                                        scaleY = if (layer.isFlippedV) -1f else 1f
                                        // Apply Opacity specific to content
                                        alpha = layer.opacity
                                        // Apply Blend Mode
                                        this.blendMode = layer.blendMode
                                    }
                                    // Border Logic (Applies to the rectangular content box)
                                    // CRITICAL: Use drawBehind() instead of .border() to avoid
                                    // layout shifts
                                    // that cause position mismatches when saving
                                    .drawBehind {
                                        if (layer.hasBorder) {
                                            drawRect(
                                                    color = Color(layer.borderColor),
                                                    style =
                                                            androidx.compose.ui.graphics.drawscope
                                                                    .Stroke(
                                                                            width =
                                                                                    layer.borderWidth
                                                                                            .dp
                                                                                            .toPx()
                                                                    )
                                            )
                                        }
                                    }
                                    // Shadow Logic (Only for Images/Shapes - Text uses native
                                    // shadow)
                                    .then(
                                            if (layer.hasShadow && layer.text == null) {
                                                Modifier.offset(
                                                                x =
                                                                        with(density) {
                                                                            layer.shadowOffsetX
                                                                                    .toDp()
                                                                        },
                                                                y =
                                                                        with(density) {
                                                                            layer.shadowOffsetY
                                                                                    .toDp()
                                                                        }
                                                        )
                                                        .shadow(
                                                                elevation = layer.shadowBlur.dp,
                                                                shape =
                                                                        androidx.compose.ui.graphics
                                                                                .RectangleShape,
                                                                ambientColor =
                                                                        Color(layer.shadowColor),
                                                                spotColor = Color(layer.shadowColor)
                                                        )
                                                        .offset(
                                                                x =
                                                                        with(density) {
                                                                            -layer.shadowOffsetX
                                                                                    .toDp()
                                                                        },
                                                                y =
                                                                        with(density) {
                                                                            -layer.shadowOffsetY
                                                                                    .toDp()
                                                                        }
                                                        )
                                            } else Modifier
                                    )
            ) {
                if (layer.text != null) {
                    val gradientBrush =
                            if (layer.isGradient && layer.gradientColors.size >= 2) {
                                androidx.compose.ui.graphics.Brush.linearGradient(
                                        colors = layer.gradientColors.map { Color(it) }
                                )
                            } else null

                    // Text Shadow Logic
                    val textShadow =
                            if (layer.hasShadow) {
                                androidx.compose.ui.graphics.Shadow(
                                        color = Color(layer.shadowColor),
                                        offset = Offset(layer.shadowOffsetX, layer.shadowOffsetY),
                                        blurRadius = layer.shadowBlur
                                )
                            } else null

                    Text(
                            text = layer.text,
                            fontSize = (heightDp.value * 0.9f).sp,
                            color =
                                    if (layer.hasTint) Color(layer.tintColor)
                                    else Color.Unspecified,
                            style =
                                    if (gradientBrush != null) {
                                        androidx.compose.ui.text.TextStyle(
                                                brush = gradientBrush,
                                                shadow = textShadow // Native Shadow
                                        )
                                    } else {
                                        androidx.compose.ui.text.TextStyle(
                                                color =
                                                        if (layer.hasTint) Color.Unspecified
                                                        else Color.Black,
                                                shadow = textShadow // Native Shadow
                                        )
                                    },
                            modifier =
                                    Modifier.align(Alignment.Center)
                                            .graphicsLayer {
                                                if (layer.hasTint) {
                                                    colorFilter =
                                                            androidx.compose.ui.graphics.ColorFilter
                                                                    .tint(
                                                                            Color(layer.tintColor)
                                                                                    .copy(
                                                                                            alpha =
                                                                                                    layer
                                                                                                            .tintStrength
                                                                                    ),
                                                                            androidx.compose.ui.graphics
                                                                                    .BlendMode.SrcAtop
                                                                    )
                                                }
                                                // Gradients for Emojis must be applied via
                                                // drawWithContent + SrcAtop
                                                // because standard TextStyle brush is ignored by
                                                // Emoji renderer
                                                if (layer.isGradient &&
                                                                layer.gradientColors.size >= 2
                                                ) {
                                                    alpha = 0.99f // Force off-screen buffer
                                                }
                                            }
                                            .drawWithContent {
                                                drawContent()
                                                if (layer.isGradient &&
                                                                layer.gradientColors.size >= 2
                                                ) {
                                                    drawRect(
                                                            brush =
                                                                    androidx.compose.ui.graphics
                                                                            .Brush
                                                                            .linearGradient(
                                                                                    colors =
                                                                                            layer
                                                                                                    .gradientColors
                                                                                                    .map {
                                                                                                        Color(
                                                                                                                it
                                                                                                        )
                                                                                                    }
                                                                            ),
                                                            blendMode =
                                                                    androidx.compose.ui.graphics
                                                                            .BlendMode.SrcAtop
                                                    )
                                                }
                                            }
                    )
                } else if (layer.resId != 0) {
                    Image(
                            painter = painterResource(id = layer.resId),
                            contentDescription = null,
                            contentScale = ContentScale.FillBounds,
                            colorFilter =
                                    if (layer.hasTint) {
                                        androidx.compose.ui.graphics.ColorFilter.tint(
                                                Color(layer.tintColor)
                                                        .copy(alpha = layer.tintStrength),
                                                androidx.compose.ui.graphics.BlendMode.SrcAtop
                                        )
                                    } else null,
                            modifier =
                                    Modifier.fillMaxSize()
                                            .then(
                                                    if (layer.isGradient &&
                                                                    layer.gradientColors.size >= 2
                                                    ) {
                                                        Modifier.graphicsLayer { alpha = 0.99f }
                                                                .drawWithContent {
                                                                    drawContent()
                                                                    drawRect(
                                                                            brush =
                                                                                    androidx.compose
                                                                                            .ui
                                                                                            .graphics
                                                                                            .Brush
                                                                                            .linearGradient(
                                                                                                    colors =
                                                                                                            layer.gradientColors
                                                                                                                    .map {
                                                                                                                        Color(
                                                                                                                                        it
                                                                                                                                )
                                                                                                                                .copy(
                                                                                                                                        alpha =
                                                                                                                                                0.8f
                                                                                                                                )
                                                                                                                    }
                                                                                            ),
                                                                            blendMode =
                                                                                    androidx.compose
                                                                                            .ui
                                                                                            .graphics
                                                                                            .BlendMode
                                                                                            .SrcAtop
                                                                    )
                                                                }
                                                    } else Modifier
                                            )
                    )
                } else if (layer.uri != null) {
                    Image(
                            painter =
                                    rememberAsyncImagePainter(
                                            ImageRequest.Builder(LocalContext.current)
                                                    .data(layer.uri)
                                                    .build()
                                    ),
                            contentDescription = null,
                            contentScale = ContentScale.FillBounds,
                            colorFilter =
                                    if (layer.hasTint) {
                                        androidx.compose.ui.graphics.ColorFilter.tint(
                                                Color(layer.tintColor)
                                                        .copy(alpha = layer.tintStrength),
                                                androidx.compose.ui.graphics.BlendMode.SrcAtop
                                        )
                                    } else null,
                            modifier =
                                    Modifier.fillMaxSize()
                                            .then(
                                                    if (layer.isGradient &&
                                                                    layer.gradientColors.size >= 2
                                                    ) {
                                                        Modifier.graphicsLayer { alpha = 0.99f }
                                                                .drawWithContent {
                                                                    drawContent()
                                                                    drawRect(
                                                                            brush =
                                                                                    androidx.compose
                                                                                            .ui
                                                                                            .graphics
                                                                                            .Brush
                                                                                            .linearGradient(
                                                                                                    colors =
                                                                                                            layer.gradientColors
                                                                                                                    .map {
                                                                                                                        Color(
                                                                                                                                        it
                                                                                                                                )
                                                                                                                                .copy(
                                                                                                                                        alpha =
                                                                                                                                                0.8f
                                                                                                                                )
                                                                                                                    }
                                                                                            ),
                                                                            blendMode =
                                                                                    androidx.compose
                                                                                            .ui
                                                                                            .graphics
                                                                                            .BlendMode
                                                                                            .SrcAtop
                                                                    )
                                                                }
                                                    } else Modifier
                                            )
                    )
                }
            }

            // Selection Overlay
            if (isSelected) {
                Box(
                        modifier =
                                Modifier.matchParentSize().drawBehind {
                                    drawRect(
                                            color = Color.White,
                                            style =
                                                    androidx.compose.ui.graphics.drawscope.Stroke(
                                                            width = 2.dp.toPx(),
                                                            pathEffect =
                                                                    androidx.compose.ui.graphics
                                                                            .PathEffect
                                                                            .dashPathEffect(
                                                                                    floatArrayOf(
                                                                                            10f,
                                                                                            10f
                                                                                    ),
                                                                                    0f
                                                                            )
                                                    )
                                    )
                                }
                )
            }
        }

        // --- HANDLES ---
        if (isSelected && !layer.isLocked) {
            val touchSize = 48.dp
            val halfTouch = 24.dp

            @Composable
            fun Handle(
                    alignment: Alignment,
                    offsetX: androidx.compose.ui.unit.Dp = 0.dp,
                    offsetY: androidx.compose.ui.unit.Dp = 0.dp,
                    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
                    iconTint: Color = Color.Black,
                    isDelete: Boolean = false,
                    onClick: (() -> Unit)? = null,
                    onDrag: ((Offset) -> Unit)? = null,
                    onDragEnd: (() -> Unit)? = null
            ) {
                val alignOffsetX =
                        when (alignment) {
                            Alignment.TopStart, Alignment.CenterStart, Alignment.BottomStart ->
                                    -halfTouch
                            Alignment.TopEnd, Alignment.CenterEnd, Alignment.BottomEnd -> halfTouch
                            else -> 0.dp
                        }
                val alignOffsetY =
                        when (alignment) {
                            Alignment.TopStart, Alignment.TopCenter, Alignment.TopEnd -> -halfTouch
                            Alignment.BottomEnd, Alignment.BottomCenter, Alignment.BottomStart ->
                                    halfTouch
                            else -> 0.dp
                        }

                Box(
                        modifier =
                                Modifier.align(alignment)
                                        .offset(
                                                x = alignOffsetX + offsetX,
                                                y = alignOffsetY + offsetY
                                        )
                                        .size(touchSize)
                                        .then(
                                                if (onDrag != null) {
                                                    Modifier.pointerInput(Unit) {
                                                        detectDragGestures(
                                                                onDragEnd = { onDragEnd?.invoke() }
                                                        ) { change, dragAmount ->
                                                            change.consume()
                                                            onDrag(dragAmount)
                                                        }
                                                    }
                                                } else if (onClick != null) {
                                                    Modifier.pointerInput(Unit) {
                                                        detectTapGestures { onClick() }
                                                    }
                                                } else Modifier
                                        ),
                        contentAlignment = Alignment.Center
                ) {
                    Box(
                            modifier =
                                    Modifier.size(
                                                    if (isDelete) 20.dp
                                                    else if (icon != null)
                                                            14.dp // Corner handles with icons
                                                    else 10.dp // Side handles
                                            )
                                            .background(
                                                    if (isDelete) Color(0xFFFF3B30)
                                                    else Color.White,
                                                    CircleShape
                                            )
                                            .border(
                                                    if (isDelete) 2.dp else 1.dp,
                                                    if (isDelete) Color.White
                                                    else Color(0xFFCCCCCC),
                                                    CircleShape
                                            )
                                            .shadow(2.dp, CircleShape),
                            contentAlignment = Alignment.Center
                    ) {
                        if (icon != null) {
                            Icon(
                                    icon,
                                    null,
                                    tint = if (isDelete) Color.White else iconTint,
                                    modifier = Modifier.size(if (isDelete) 12.dp else 10.dp)
                            )
                        }
                    }
                }
            }

            // Scaled Handle Drag Logic
            fun onScaleHandleDrag(
                    dragAmount: Offset,
                    handleSignX: Float,
                    handleSignY: Float,
                    lockAspect: Boolean = false
            ) {
                // 1. Inverse Rotate Drag Vector to align with Local Sticker Axes
                val rad = -Math.toRadians(layer.rotation.toDouble()) // Inverse Rotation
                val cos = Math.cos(rad)
                val sin = Math.sin(rad)
                val localDragX = (dragAmount.x * cos - dragAmount.y * sin).toFloat()
                val localDragY = (dragAmount.x * sin + dragAmount.y * cos).toFloat()

                // 2. Project onto Handle Normal Direction (from center)
                // If handle is Right (1, 0), and I drag Right (+x), localDragX is +ve. SignX is
                // +ve. Product +ve -> Grow.
                // If handle is Left (-1, 0), and I drag Left (-x), localDragX is -ve. SignX is -ve.
                // Product +ve -> Grow.

                // Sensitivity Factor (Lower = Faster, Higher = Slower)
                // Originally 200f, reducing to 40f makes it 5x faster
                val widthSensitivity = 40f
                val heightSensitivity = 40f

                // Calculate Delta Factors
                // We apply drag * sign.
                // If scaleX is negative (flipped), does logic hold?
                // Visual handle is "Right". "Right" in local un-flipped space is +X.
                // If Flipped, "Right" Visual is -X Local?
                // No, Flipped is scaleX = -1. Local axes are flipped?
                // Let's assume Handles rotate with the object. If flipped, the object is flipped
                // visually.
                // But handles position logic in Box(Modifier.align) ignores render transform
                // (flip).
                // So Handles are always Top/Right/Left/Bottom relative to Un-Transformed Box.
                // BUT the Content is Flipped.
                // So "Right" Handle is still +X in layout coordinates.
                // So we don't need to account for Flip in drag direction unless Flip affects local
                // axes.
                // ScaleX being negative affects content drawing, NOT layout bounds or handle
                // positions usually in this simple box model.

                // 2. Calculate Pan Compensation (Anchored Resizing)
                // Determine the visual size change in Screen Pixels
                val currentWidthPx = (widthDp.value * density.density).coerceAtLeast(1f)
                val currentHeightPx = (heightDp.value * density.density).coerceAtLeast(1f)

                // Linear Resizing: Calculate scale factor such that Size Change == Drag Distance
                // sxChange = (Current + dx) / Current = 1 + dx / Current
                val dx = localDragX * handleSignX
                val dy = localDragY * handleSignY

                var sxChange = 1f + (dx / currentWidthPx)
                var syChange = 1f + (dy / currentHeightPx)

                if (handleSignX == 0f) sxChange = 1f
                if (handleSignY == 0f) syChange = 1f

                val newWidthPx = currentWidthPx * sxChange
                val newHeightPx = currentHeightPx * syChange

                val deltaWidth = newWidthPx - currentWidthPx
                val deltaHeight = newHeightPx - currentHeightPx

                // Calculate local top-left shift required to keep opposite edge fixed
                // If dragging Left/Top (-1), we must move the Origin (Top-Left) by -Delta to
                // compensate for growth
                // If dragging Right/Bottom (+1), Origin stays fixed, growth extends outwards
                // naturally
                val shiftLocalX = if (handleSignX < 0) -deltaWidth else 0f
                val shiftLocalY = if (handleSignY < 0) -deltaHeight else 0f

                // 3. Rotate Shift back to Global Screen Coordinates
                val radGlobal = Math.toRadians(layer.rotation.toDouble())
                val cosG = Math.cos(radGlobal)
                val sinG = Math.sin(radGlobal)

                val rotShiftX = shiftLocalX * cosG - shiftLocalY * sinG
                val rotShiftY = shiftLocalX * sinG + shiftLocalY * cosG

                // 4. Scale to Bitmap Coordinates
                val panCompensation =
                        Offset(
                                (rotShiftX / bitmapScale).toFloat(),
                                (rotShiftY / bitmapScale).toFloat()
                        )

                onTransform(layer.id, panCompensation, sxChange, syChange, 0f)
            }

            // Top-Left: Delete
            Handle(
                    alignment = Alignment.TopStart,
                    icon = Icons.Default.Close,
                    isDelete = true,
                    onClick = { onDelete(layer.id) }
            )

            // Top-Right: Rotate
            Handle(
                    alignment = Alignment.TopEnd,
                    icon = Icons.Default.Refresh,
                    onDrag = { dragAmount ->
                        val degrees = (dragAmount.x + dragAmount.y) * 0.2f
                        onTransform(layer.id, Offset.Zero, 1f, 1f, degrees)
                    },
                    onDragEnd = { onTransformEnd(layer.id) }
            )

            // Bottom-Right: Scale (Uniform)
            Handle(
                    alignment = Alignment.BottomEnd,
                    icon = Icons.Default.OpenInFull,
                    onDrag = { dragAmount ->
                        // Shape-style: Uniform scaling from diagonal drag
                        val distance = (dragAmount.x + dragAmount.y) / 2f
                        val currentSize = ((widthPx + heightPx) / 2f).coerceAtLeast(1f)
                        val finalScale = (currentSize + distance) / currentSize
                        onTransform(layer.id, Offset.Zero, finalScale, finalScale, 0f)
                    },
                    onDragEnd = { onTransformEnd(layer.id) }
            )

            // Bottom-Left: Scale (Uniform)
            Handle(
                    alignment = Alignment.BottomStart,
                    onDrag = { dragAmount ->
                        // Shape-style: Uniform scaling from diagonal drag (inverted X)
                        val distance = (-dragAmount.x + dragAmount.y) / 2f
                        val currentSize = ((widthPx + heightPx) / 2f).coerceAtLeast(1f)
                        val finalScale = (currentSize + distance) / currentSize
                        onTransform(layer.id, Offset.Zero, finalScale, finalScale, 0f)
                    },
                    onDragEnd = { onTransformEnd(layer.id) }
            )

            // --- SIDES (Stretching) ---

            // Top Center - Stretch Y
            Handle(
                    alignment = Alignment.TopCenter,
                    offsetY = (-8).dp, // Extra padding from edge
                    onDrag = { dragAmount ->
                        // Shape-style: Rotation-aware top drag
                        val rad = -Math.toRadians(layer.rotation.toDouble())
                        val dy =
                                (dragAmount.x * Math.sin(rad) + dragAmount.y * Math.cos(rad))
                                        .toFloat()

                        val currentHeight = heightPx.coerceAtLeast(1f)
                        val syChange = (currentHeight - dy) / currentHeight
                        val panY = dy / bitmapScale

                        onTransform(layer.id, Offset(0f, panY), 1f, syChange, 0f)
                    },
                    onDragEnd = { onTransformEnd(layer.id) }
            )

            // Bottom Center - Stretch Y
            Handle(
                    alignment = Alignment.BottomCenter,
                    offsetY = 8.dp, // Extra padding from edge
                    onDrag = { dragAmount ->
                        // Shape-style: Direct height increase
                        val dHeight = dragAmount.y / bitmapScale
                        val currentHeight = heightPx.coerceAtLeast(1f)
                        val syChange = (currentHeight + dHeight) / currentHeight
                        onTransform(layer.id, Offset.Zero, 1f, syChange, 0f)
                    },
                    onDragEnd = { onTransformEnd(layer.id) }
            )

            // Left Center - Stretch X
            Handle(
                    alignment = Alignment.CenterStart,
                    onDrag = { dragAmount ->
                        // Shape-style: Rotation-aware left drag
                        val rad = -Math.toRadians(layer.rotation.toDouble())
                        val dx =
                                (dragAmount.x * Math.cos(rad) - dragAmount.y * Math.sin(rad))
                                        .toFloat()

                        val currentWidth = widthPx.coerceAtLeast(1f)
                        val sxChange = (currentWidth - dx) / currentWidth
                        val panX = dx / bitmapScale

                        onTransform(layer.id, Offset(panX, 0f), sxChange, 1f, 0f)
                    },
                    onDragEnd = { onTransformEnd(layer.id) }
            )

            // Right Center - Stretch X
            Handle(
                    alignment = Alignment.CenterEnd,
                    offsetX = 8.dp, // Extra padding from edge
                    onDrag = { dragAmount ->
                        // Shape-style: Direct width increase
                        val dWidth = dragAmount.x / bitmapScale
                        val currentWidth = widthPx.coerceAtLeast(1f)
                        val sxChange = (currentWidth + dWidth) / currentWidth
                        onTransform(layer.id, Offset.Zero, sxChange, 1f, 0f)
                    },
                    onDragEnd = { onTransformEnd(layer.id) }
            )
        }
    }
}
