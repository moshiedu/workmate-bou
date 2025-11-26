package com.moshitech.workmate.feature.compass

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlin.math.abs
// Tune this as you like
private const val ARRIVAL_THRESHOLD_METERS = 15f

@Composable
fun ARTab(
    viewModel: CompassViewModel,
    isDark: Boolean,
    textColor: Color
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // ---- ViewModel state ----
    val azimuth by viewModel.azimuth.collectAsState()
    val selectedWaypoint by viewModel.selectedWaypoint.collectAsState()
    val useTrueNorth by viewModel.useTrueNorth.collectAsState()
    val locationInfo by viewModel.locationInfo.collectAsState()

    // ---- UI flags ----
    val showDebugInfo = rememberSaveable { mutableStateOf(true) }
    val showAwareness = rememberSaveable { mutableStateOf(true) }

    // ---- Camera permission ----
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    // ---- Guards: GPS / True North ----
    if (!useTrueNorth || locationInfo == null) {
        GpsRequiredContent(textColor = textColor)
        return
    }

    // ---- Guards: Camera permission ----
    if (!hasCameraPermission) {
        CameraPermissionContent(
            textColor = textColor,
            onRequestPermission = {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        )
        return
    }

    // ---- Camera preview setup ----
    val previewView = remember { PreviewView(context) }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }

    // Get camera provider once
    LaunchedEffect(Unit) {
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener(
            {
                try {
                    cameraProvider = future.get()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            ContextCompat.getMainExecutor(context)
        )
    }

    // Bind/unbind camera to lifecycle
    DisposableEffect(lifecycleOwner, cameraProvider) {
        val provider = cameraProvider
        if (provider != null) {
            try {
                val preview = Preview.Builder().build().apply {
                    setSurfaceProvider(previewView.surfaceProvider)
                }
                provider.unbindAll()
                provider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        onDispose {
            try {
                cameraProvider?.unbindAll()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ---- Paints (remember, not recreated every frame) ----
    val headingPaint = remember {
        android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 40f
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
    }

    val waypointTextPaint = remember {
        android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 36f
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
            setShadowLayer(10f, 0f, 0f, android.graphics.Color.BLACK)
        }
    }

    val instructionPaint = remember {
        android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 48f
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
            setShadowLayer(10f, 0f, 0f, android.graphics.Color.BLACK)
        }
    }

    val instructionSecondaryPaint = remember {
        android.graphics.Paint().apply {
            color = android.graphics.Color.LTGRAY
            textSize = 32f
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }
    }

    val arrivalPaint = remember {
        android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 46f
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
            setShadowLayer(10f, 0f, 0f, android.graphics.Color.BLACK)
        }
    }

    val arrivalSecondaryPaint = remember {
        android.graphics.Paint().apply {
            color = android.graphics.Color.LTGRAY
            textSize = 32f
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }
    }

    // ---- Pulse animation for aligned arrow ----
    val infiniteTransition = rememberInfiniteTransition(label = "arrow_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Signed minimal angle between bearing & heading, in -180..+180
    fun computeDiff(bearing: Float, heading: Float): Float {
        var diff = bearing - heading
        diff = ((diff + 540f) % 360f) - 180f
        return diff
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Camera preview
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        // Compass + AR overlay
        Canvas(modifier = Modifier.fillMaxSize()) {
            val fov = 60f                            // horizontal field of view in degrees
            val pixelsPerDegree = size.width / fov
            val centerX = size.width / 2
            val centerY = size.height / 2
            val stripHeight = 80.dp.toPx()

            // ---- Top compass strip background ----
            drawRect(
                color = Color.Black.copy(alpha = 0.4f),
                topLeft = androidx.compose.ui.geometry.Offset(0f, 0f),
                size = androidx.compose.ui.geometry.Size(size.width, stripHeight)
            )

            // ---- Compass ticks based on heading (azimuth) ----
            val centerHeading = azimuth             // 0..360, 0 = North
            for (i in (centerHeading - fov / 2).toInt()..(centerHeading + fov / 2).toInt()) {
                val normalizedDegree = (i + 360) % 360
                val x = centerX + ((i - centerHeading) * pixelsPerDegree)

                if (normalizedDegree % 10 == 0) {
                    // major tick
                    drawLine(
                        color = Color.White,
                        start = androidx.compose.ui.geometry.Offset(
                            x,
                            stripHeight - 20.dp.toPx()
                        ),
                        end = androidx.compose.ui.geometry.Offset(
                            x,
                            stripHeight
                        ),
                        strokeWidth = 2.dp.toPx()
                    )

                    if (normalizedDegree % 45 == 0) {
                        val label = when (normalizedDegree) {
                            0 -> "N"
                            45 -> "NE"
                            90 -> "E"
                            135 -> "SE"
                            180 -> "S"
                            225 -> "SW"
                            270 -> "W"
                            315 -> "NW"
                            else -> ""
                        }
                        if (label.isNotEmpty()) {
                            drawContext.canvas.nativeCanvas.drawText(
                                label,
                                x,
                                stripHeight - 30.dp.toPx(),
                                headingPaint
                            )
                        }
                    }
                } else if (normalizedDegree % 2 == 0) {
                    // minor tick
                    drawLine(
                        color = Color.White.copy(alpha = 0.5f),
                        start = androidx.compose.ui.geometry.Offset(
                            x,
                            stripHeight - 10.dp.toPx()
                        ),
                        end = androidx.compose.ui.geometry.Offset(
                            x,
                            stripHeight
                        ),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            }

            // ---- Center heading marker ----
            drawPath(
                path = Path().apply {
                    moveTo(centerX, stripHeight + 10.dp.toPx())
                    lineTo(centerX - 10.dp.toPx(), stripHeight + 25.dp.toPx())
                    lineTo(centerX + 10.dp.toPx(), stripHeight + 25.dp.toPx())
                    close()
                },
                color = Color.Yellow
            )

            // ---- Waypoint / Arrival logic ----
            if (selectedWaypoint != null) {
                val waypoint = selectedWaypoint!!
                val bearing = viewModel.getWaypointBearing(waypoint)               // 0..360
                val heading = azimuth                                              // 0..360

                val diff = computeDiff(bearing, heading)                           // -180..+180
                val distanceMeters = viewModel.getWaypointDistanceMeters(waypoint) // numeric
                val distanceText = viewModel.getWaypointDistance(waypoint)         // formatted

                val isArrived = distanceMeters <= ARRIVAL_THRESHOLD_METERS

                if (isArrived) {
                    // 🎯 ARRIVAL UI – no navigation arrow, clear “you’re here” message
                    val cardWidth = 340.dp.toPx()
                    val cardHeight = 170.dp.toPx()
                    val left = centerX - cardWidth / 2
                    val top = centerY - cardHeight / 2

                    drawRect(
                        color = Color.Black.copy(alpha = 0.7f),
                        topLeft = androidx.compose.ui.geometry.Offset(left, top),
                        size = androidx.compose.ui.geometry.Size(cardWidth, cardHeight)
                    )

                    drawContext.canvas.nativeCanvas.drawText(
                        "You’re at your destination",
                        centerX,
                        centerY - 10.dp.toPx(),
                        arrivalPaint
                    )

                    val approxText = when {
                        distanceMeters < 5f -> "within ~5 m"
                        distanceMeters < 20f -> "within ~${distanceMeters.toInt()} m"
                        else -> distanceText
                    }

                    drawContext.canvas.nativeCanvas.drawText(
                        approxText,
                        centerX,
                        centerY + 40.dp.toPx(),
                        arrivalSecondaryPaint
                    )

                    // Do not draw arrows / side indicators if arrived
                    return@Canvas
                }

                // ---- Normal navigation mode (not arrived) ----
                val isAligned = abs(diff) < 15f
                val arrowColor = if (isAligned) Color(0xFF10B981) else Color(0xFF3B82F6)

                // Big center arrow, rotated toward waypoint
                withTransform({
                    translate(left = centerX, top = centerY)
                    rotate(diff)
                    scale(if (isAligned) pulseScale else 1.0f)
                }) {
                    val arrowSize = 80.dp.toPx()
                    val arrowPath = Path().apply {
                        moveTo(0f, -arrowSize)
                        lineTo(arrowSize / 2, arrowSize / 2)
                        lineTo(0f, arrowSize / 4)
                        lineTo(-arrowSize / 2, arrowSize / 2)
                        close()
                    }
                    drawPath(arrowPath, arrowColor)

                    val shadowPath = Path().apply {
                        moveTo(0f, -arrowSize)
                        lineTo(0f, arrowSize / 4)
                        lineTo(-arrowSize / 2, arrowSize / 2)
                        close()
                    }
                    drawPath(shadowPath, Color.Black.copy(alpha = 0.2f))
                }

                // Waypoint indication relative to FOV
                val x = centerX + (diff * pixelsPerDegree)

                if (abs(diff) < fov / 2) {
                    // Inside FOV → dot + text under compass strip
                    drawCircle(
                        color = arrowColor,
                        radius = 8.dp.toPx(),
                        center = androidx.compose.ui.geometry.Offset(
                            x,
                            stripHeight + 40.dp.toPx()
                        )
                    )
                    drawContext.canvas.nativeCanvas.drawText(
                        waypoint.name,
                        x,
                        stripHeight + 80.dp.toPx(),
                        waypointTextPaint
                    )
                    drawContext.canvas.nativeCanvas.drawText(
                        distanceText,
                        x,
                        stripHeight + 115.dp.toPx(),
                        waypointTextPaint
                    )
                } else {
                    // Out of FOV → side arrow (left or right)
                    val isRight = diff > 0
                    val edgeX = if (isRight) size.width - 60.dp.toPx() else 60.dp.toPx()

                    withTransform({
                        translate(left = edgeX, top = centerY)
                        if (!isRight) scale(scaleX = -1f, scaleY = 1f)
                    }) {
                        val arrowSize = 50.dp.toPx()
                        val curvedPath = Path().apply {
                            moveTo(-arrowSize / 2, -arrowSize / 2)
                            cubicTo(
                                arrowSize / 4, -arrowSize / 2,
                                arrowSize / 2, -arrowSize / 4,
                                arrowSize / 2, arrowSize / 4
                            )
                            lineTo(
                                arrowSize / 2 - 15.dp.toPx(),
                                arrowSize / 4 - 15.dp.toPx()
                            )
                            moveTo(arrowSize / 2, arrowSize / 4)
                            lineTo(
                                arrowSize / 2 + 15.dp.toPx(),
                                arrowSize / 4 - 15.dp.toPx()
                            )
                        }
                        drawPath(
                            curvedPath,
                            arrowColor,
                            style = Stroke(
                                width = 6.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        )
                        drawPath(
                            curvedPath,
                            Color.Black.copy(alpha = 0.3f),
                            style = Stroke(
                                width = 8.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        )
                    }

                    drawContext.canvas.nativeCanvas.drawText(
                        distanceText,
                        edgeX,
                        centerY + 60.dp.toPx(),
                        waypointTextPaint
                    )
                }
            } else {
                // ---- No waypoint selected instructions ----
                val cardWidth = 360.dp.toPx()
                val cardHeight = 200.dp.toPx()
                val left = centerX - cardWidth / 2
                val top = centerY - cardHeight / 2

                drawRect(
                    color = Color.Black.copy(alpha = 0.7f),
                    topLeft = androidx.compose.ui.geometry.Offset(left, top),
                    size = androidx.compose.ui.geometry.Size(cardWidth, cardHeight)
                )

                // Center target icon
                drawCircle(
                    color = Color(0xFF3B82F6),
                    radius = 30.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(
                        centerX,
                        centerY - 50.dp.toPx()
                    )
                )
                drawCircle(
                    color = Color.White,
                    radius = 15.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(
                        centerX,
                        centerY - 50.dp.toPx()
                    )
                )

                drawContext.canvas.nativeCanvas.drawText(
                    "No Waypoint Selected",
                    centerX,
                    centerY + 10.dp.toPx(),
                    instructionPaint
                )

                drawContext.canvas.nativeCanvas.drawText(
                    "1. Go to Waypoints tab",
                    centerX,
                    centerY + 50.dp.toPx(),
                    instructionSecondaryPaint
                )
                drawContext.canvas.nativeCanvas.drawText(
                    "2. Tap + to save a location",
                    centerX,
                    centerY + 80.dp.toPx(),
                    instructionSecondaryPaint
                )
                drawContext.canvas.nativeCanvas.drawText(
                    "3. Select a waypoint",
                    centerX,
                    centerY + 110.dp.toPx(),
                    instructionSecondaryPaint
                )
            }
        }

        // ---- Awareness overlay (first entry tap) ----
        if (showAwareness.value) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { showAwareness.value = false },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Tap to start AR navigation",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        // ---- Top debug info (optional) ----
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 100.dp)
        ) {
            Text(
                "${azimuth.toInt()}°",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = Color.Yellow,
                modifier = Modifier
                    .background(
                        Color.Black.copy(alpha = 0.3f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp)
            )

            if (showDebugInfo.value && selectedWaypoint != null) {
                val waypoint = selectedWaypoint!!
                val bearing = viewModel.getWaypointBearing(waypoint)
                val heading = azimuth
                val diff = computeDiff(bearing, heading)

                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .background(
                            Color.Black.copy(alpha = 0.5f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp)
                ) {
                    Text(
                        "Bearing: ${bearing.toInt()}°",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White
                    )
                    Text(
                        "Heading: ${heading.toInt()}°",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White
                    )
                    Text(
                        "Diff: ${diff.toInt()}°",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (abs(diff) < 15f) Color.Green else Color.White
                    )
                }
            }
        }
    }
}

// ---- Helper composables ----

@Composable
private fun GpsRequiredContent(textColor: Color) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.LocationOn,
                contentDescription = "GPS Required",
                modifier = Modifier.size(64.dp),
                tint = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "GPS Required",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = textColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "AR mode needs True North (GPS). Please enable it in the Compass tab.",
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun CameraPermissionContent(
    textColor: Color,
    onRequestPermission: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Camera Permission Required",
                style = MaterialTheme.typography.titleLarge,
                color = textColor
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "AR mode needs camera access",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onRequestPermission) {
                Text("Grant Permission")
            }
        }
    }
}
