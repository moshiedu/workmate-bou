package com.moshitech.workmate.feature.compass

import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun ARTab(
    viewModel: CompassViewModel,
    isDark: Boolean,
    textColor: Color
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val azimuth by viewModel.azimuth.collectAsState()
    val selectedWaypoint by viewModel.selectedWaypoint.collectAsState()
    val useTrueNorth by viewModel.useTrueNorth.collectAsState()
    val locationInfo by viewModel.locationInfo.collectAsState()
    
    // UI state for debug panel and awareness overlay
    val showDebugInfo = remember { mutableStateOf(true) }
    val showAwareness = remember { mutableStateOf(true) }
    
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }
    
    if (!useTrueNorth || locationInfo == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                androidx.compose.material3.Icon(Icons.Default.LocationOn, "GPS Required", modifier = Modifier.size(64.dp), tint = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
                Text("GPS Required", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = textColor)
                Spacer(modifier = Modifier.height(8.dp))
                Text("AR mode needs True North (GPS). Please enable it in the Compass tab.", textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = Color.Gray)
            }
        }
        return
    }
    
    if (!hasCameraPermission) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Camera Permission Required", style = MaterialTheme.typography.titleLarge, color = textColor)
                Spacer(modifier = Modifier.height(16.dp))
                Text("AR mode needs camera access", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text("Grant Permission")
                }
            }
        }
        return
    }

    DisposableEffect(Unit) {
        onDispose {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                try {
                    val cameraProvider = cameraProviderFuture.get()
                    cameraProvider.unbindAll()
                } catch (e: Exception) { e.printStackTrace() }
            }, ContextCompat.getMainExecutor(context))
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    ProcessCameraProvider.getInstance(ctx).addListener({
                        try {
                            val cameraProvider = ProcessCameraProvider.getInstance(ctx).get()
                            val preview = Preview.Builder().build().also { it.setSurfaceProvider(surfaceProvider) }
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview)
                        } catch (e: Exception) { e.printStackTrace() }
                    }, ContextCompat.getMainExecutor(ctx))
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        val textPaint = remember {
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

        val infiniteTransition = rememberInfiniteTransition(label = "arrow_pulse")
        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = 1.1f,
            animationSpec = infiniteRepeatable(animation = tween(1000, easing = LinearEasing), repeatMode = RepeatMode.Reverse),
            label = "pulse"
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            val fov = 60f
            val pixelsPerDegree = size.width / fov
            val centerX = size.width / 2
            val centerY = size.height / 2
            val stripHeight = 80.dp.toPx()
            
            drawRect(color = Color.Black.copy(alpha = 0.4f), topLeft = Offset(0f, 0f), size = androidx.compose.ui.geometry.Size(size.width, stripHeight))

            val adjustedAzimuth = (azimuth + 180) % 360
            for (i in (adjustedAzimuth - fov / 2).toInt()..(adjustedAzimuth + fov / 2).toInt()) {
                val normalizedDegree = (i + 360) % 360
                val x = centerX + ((i - adjustedAzimuth) * pixelsPerDegree)

                if (normalizedDegree % 10 == 0) {
                    drawLine(color = Color.White, start = Offset(x, stripHeight - 20.dp.toPx()), end = Offset(x, stripHeight), strokeWidth = 2.dp.toPx())
                    if (normalizedDegree % 45 == 0) {
                        val label = when (normalizedDegree) {
                            0 -> "N"; 45 -> "NE"; 90 -> "E"; 135 -> "SE"
                            180 -> "S"; 225 -> "SW"; 270 -> "W"; 315 -> "NW"
                            else -> ""
                        }
                        drawContext.canvas.nativeCanvas.drawText(label, x, stripHeight - 30.dp.toPx(), textPaint)
                    }
                } else if (normalizedDegree % 2 == 0) {
                    drawLine(color = Color.White.copy(alpha = 0.5f), start = Offset(x, stripHeight - 10.dp.toPx()), end = Offset(x, stripHeight), strokeWidth = 1.dp.toPx())
                }
            }
            
            drawPath(androidx.compose.ui.graphics.Path().apply {
                moveTo(centerX, stripHeight + 10.dp.toPx())
                lineTo(centerX - 10.dp.toPx(), stripHeight + 25.dp.toPx())
                lineTo(centerX + 10.dp.toPx(), stripHeight + 25.dp.toPx())
                close()
            }, Color.Yellow)

            if (selectedWaypoint != null) {
                val bearing = viewModel.getWaypointBearing(selectedWaypoint!!)
                val adjustedAzimuth = (azimuth + 180) % 360
                var diff = bearing - adjustedAzimuth
                while (diff < -180) diff += 360
                while (diff > 180) diff -= 360
                
                val isAligned = kotlin.math.abs(diff) < 15
                val arrowColor = if (isAligned) Color(0xFF10B981) else Color(0xFF3B82F6)
                
                withTransform({
                    translate(left = centerX, top = centerY)
                    rotate(diff)
                    scale(if (isAligned) pulseScale else 1.0f)
                }) {
                    val arrowSize = 80.dp.toPx()
                    drawPath(androidx.compose.ui.graphics.Path().apply {
                        moveTo(0f, -arrowSize)
                        lineTo(arrowSize / 2, arrowSize / 2)
                        lineTo(0f, arrowSize / 4)
                        lineTo(-arrowSize / 2, arrowSize / 2)
                        close()
                    }, arrowColor)
                    drawPath(androidx.compose.ui.graphics.Path().apply {
                        moveTo(0f, -arrowSize)
                        lineTo(0f, arrowSize / 4)
                        lineTo(-arrowSize / 2, arrowSize / 2)
                        close()
                    }, Color.Black.copy(alpha = 0.2f))
                }

                val x = centerX + (diff * pixelsPerDegree)
                
                if (kotlin.math.abs(diff) < fov / 2) {
                    drawCircle(color = arrowColor, radius = 8.dp.toPx(), center = Offset(x, stripHeight + 40.dp.toPx()))
                    val distance = viewModel.getWaypointDistance(selectedWaypoint!!)
                    drawContext.canvas.nativeCanvas.drawText(selectedWaypoint!!.name, x, stripHeight + 80.dp.toPx(), waypointTextPaint)
                    drawContext.canvas.nativeCanvas.drawText(distance, x, stripHeight + 115.dp.toPx(), waypointTextPaint)
                } else {
                    val isRight = diff > 0
                    val edgeX = if (isRight) size.width - 60.dp.toPx() else 60.dp.toPx()
                    
                    withTransform({
                        translate(left = edgeX, top = centerY)
                        if (!isRight) scale(scaleX = -1f, scaleY = 1.0f)
                    }) {
                        val arrowSize = 50.dp.toPx()
                        val curvedPath = androidx.compose.ui.graphics.Path().apply {
                            moveTo(-arrowSize / 2, -arrowSize / 2)
                            cubicTo(arrowSize / 4, -arrowSize / 2, arrowSize / 2, -arrowSize / 4, arrowSize / 2, arrowSize / 4)
                            lineTo(arrowSize / 2 - 15.dp.toPx(), arrowSize / 4 - 15.dp.toPx())
                            moveTo(arrowSize / 2, arrowSize / 4)
                            lineTo(arrowSize / 2 + 15.dp.toPx(), arrowSize / 4 - 15.dp.toPx())
                        }
                        drawPath(curvedPath, arrowColor, style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round))
                        drawPath(curvedPath, Color.Black.copy(alpha = 0.3f), style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round))
                    }
                    
                    val distance = viewModel.getWaypointDistance(selectedWaypoint!!)
                    drawContext.canvas.nativeCanvas.drawText(distance, edgeX, centerY + 60.dp.toPx(), waypointTextPaint)
                }
            } else {
                val instructionPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 48f
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                    setShadowLayer(10f, 0f, 0f, android.graphics.Color.BLACK)
                }
                
                drawRect(color = Color.Black.copy(alpha = 0.7f), topLeft = Offset(centerX - 180.dp.toPx(), centerY - 100.dp.toPx()), size = androidx.compose.ui.geometry.Size(360.dp.toPx(), 200.dp.toPx()))
                drawCircle(color = Color(0xFF3B82F6), radius = 30.dp.toPx(), center = Offset(centerX, centerY - 50.dp.toPx()))
                drawCircle(color = Color.White, radius = 15.dp.toPx(), center = Offset(centerX, centerY - 50.dp.toPx()))
                drawContext.canvas.nativeCanvas.drawText("No Waypoint Selected", centerX, centerY + 10.dp.toPx(), instructionPaint)
                
                val smallerPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.LTGRAY
                    textSize = 32f
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                }
                
                drawContext.canvas.nativeCanvas.drawText("1. Go to Waypoints tab", centerX, centerY + 50.dp.toPx(), smallerPaint)
                drawContext.canvas.nativeCanvas.drawText("2. Tap + to save a location", centerX, centerY + 80.dp.toPx(), smallerPaint)
                drawContext.canvas.nativeCanvas.drawText("3. Select a waypoint", centerX, centerY + 110.dp.toPx(), smallerPaint)
            }
        }

        // Awareness overlay shown on first entry
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

        Column(modifier = Modifier.align(Alignment.TopCenter).padding(top = 100.dp)) {
            Text("${azimuth.toInt()}°", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color.Yellow, modifier = Modifier.background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp)).padding(8.dp))
            
            if (showDebugInfo.value) {
                if (selectedWaypoint != null) {
                    val bearing = viewModel.getWaypointBearing(selectedWaypoint!!)
                    val adjustedAzimuth = (azimuth + 180) % 360
                    var diff = bearing - adjustedAzimuth
                    while (diff < -180) diff += 360
                    while (diff > 180) diff -= 360
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp)).padding(8.dp)) {
                        Text("Bearing: ${bearing.toInt()}°", style = MaterialTheme.typography.bodySmall, color = Color.White)
                        Text("Azimuth: ${azimuth.toInt()}° → ${adjustedAzimuth.toInt()}°", style = MaterialTheme.typography.bodySmall, color = Color.White)
                        Text("Diff: ${diff.toInt()}°", style = MaterialTheme.typography.bodySmall, color = if (kotlin.math.abs(diff) < 15) Color.Green else Color.White)
                    }
                }
            }
        }
    }
}
