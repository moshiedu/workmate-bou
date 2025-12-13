package com.moshitech.workmate.feature.imagestudio.ui

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.graphics.toArgb
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import com.moshitech.workmate.feature.imagestudio.components.DrawAndShapesToolbar
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.RotateLeft
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.outlined.Crop
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PhotoFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatAlignRight
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape

import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LinearScale
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.runtime.key
import androidx.compose.ui.geometry.Offset
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.moshitech.workmate.feature.imagestudio.components.AdContainer
import com.moshitech.workmate.feature.imagestudio.viewmodel.PhotoEditorViewModel
import com.moshitech.workmate.feature.imagestudio.viewmodel.DrawAction


enum class EditorTab {
    CROP, ADJUST, FILTERS, STICKERS, SHAPES, ROTATE, TEXT, DRAW
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoEditorScreen(
    navController: NavController,
    imageUri: Uri?,
    viewModel: PhotoEditorViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var currentTab by remember { mutableStateOf(EditorTab.TEXT) }
    var showOriginal by remember { mutableStateOf(false) }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Eyedropper State (Lifted for Toolbar access)
    var eyedropperCallback by remember { mutableStateOf<((Color) -> Unit)?>(null) }
    val isEyedropperActive = eyedropperCallback != null
    
    // Reset zoom when entering eyedropper mode for accurate picking
    LaunchedEffect(isEyedropperActive) {
        if (isEyedropperActive) {
            viewModel.clearMessage() // Clear any previous messages
        }
    }

    // Load image on start
    LaunchedEffect(imageUri) {
        if (imageUri != null) {
            viewModel.loadImage(imageUri)
        }
    }
    
    // Texture Picker for Text
    val texturePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            uiState.selectedTextLayerId?.let { id ->
                viewModel.updateTextProperty(id) { it.copy(textureUri = uri.toString()) }
            }
        }
    }
    
    // Camera & Gallery Launchers
    val cameraImageUri = remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && cameraImageUri.value != null) {
            viewModel.loadImage(cameraImageUri.value!!)
        }
    }
    
    // Gallery picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.loadImage(it) }
    }
    
    // Function to create temp URI for camera
    fun createImageUri(): Uri {
        val contentValues = android.content.ContentValues().apply {
            put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, "temp_${System.currentTimeMillis()}.jpg")
            put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        }
        return navController.context.contentResolver.insert(
            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )!!
    }
    
    // Message Handling
    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    // Cropper Launcher
    val cropImageLauncher = rememberLauncherForActivityResult(contract = CropImageContract()) { result ->
        if (result.isSuccessful) {
            result.uriContent?.let { 
                viewModel.loadImage(it) 
            }
        }
        // Always switch back to a content view (e.g. TEXT/Default) to avoid staying on the "Crop Button" screen
        currentTab = EditorTab.TEXT 
    }

    AdContainer(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color(0xFF121212),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            // floatingActionButton removed as per design request
            topBar = {
                // Custom Top Bar for Pixel Perfect Look
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0D121F)) // Deep dark blue/black
                        .statusBarsPadding()
                        .height(56.dp)
                        .padding(horizontal = 8.dp)
                ) {
                    // Left: Close
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(Icons.Default.Close, "Close", tint = Color.White)
                    }
                    
                    // Center: Undo/Redo
                    Row(
                        modifier = Modifier.align(Alignment.Center),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.undo() },
                            enabled = uiState.canUndo
                        ) {
                            Icon(
                                imageVector = Icons.Default.Undo,
                                contentDescription = "Undo",
                                tint = if (uiState.canUndo) Color.White else Color.Gray,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        IconButton(
                            onClick = { viewModel.redo() },
                            enabled = uiState.canRedo
                        ) {
                            Icon(
                                imageVector = Icons.Default.Redo,
                                contentDescription = "Redo",
                                tint = if (uiState.canRedo) Color.White else Color.Gray,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    
                    // Right: Save/Check
                    IconButton(
                        onClick = { viewModel.showSaveDialog() },
                        enabled = !uiState.isLoading && !uiState.isSaving,
                         modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                        } else {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Save",
                                tint = Color(0xFF007AFF) // iOS Blue for Done
                            )
                        }
                    }
                }
            },
            // Removed standard bottomBar to implement resizable layout manually
        ) { padding ->
            // Main Content Area with BottomBar handling
            Scaffold(
                containerColor = Color.Transparent,
                bottomBar = {
                    com.moshitech.workmate.feature.imagestudio.components.PhotoEditorBottomNav(
                        selectedTool = when (currentTab) {
                            EditorTab.CROP -> com.moshitech.workmate.feature.imagestudio.components.EditorTool.CROP
                            EditorTab.FILTERS -> com.moshitech.workmate.feature.imagestudio.components.EditorTool.FILTERS
                            EditorTab.STICKERS -> com.moshitech.workmate.feature.imagestudio.components.EditorTool.STICKERS
                            EditorTab.ROTATE -> com.moshitech.workmate.feature.imagestudio.components.EditorTool.ROTATE
                            EditorTab.ADJUST -> com.moshitech.workmate.feature.imagestudio.components.EditorTool.ADJUST
                            EditorTab.SHAPES -> com.moshitech.workmate.feature.imagestudio.components.EditorTool.SHAPES
                            EditorTab.TEXT -> com.moshitech.workmate.feature.imagestudio.components.EditorTool.TEXT
                            EditorTab.DRAW -> com.moshitech.workmate.feature.imagestudio.components.EditorTool.DRAW
                        },
                        onToolSelected = { tool ->
                            if (tool == com.moshitech.workmate.feature.imagestudio.components.EditorTool.CROP) {
                                if (imageUri != null) {
                                    val options = CropImageContractOptions(uri = imageUri, cropImageOptions = CropImageOptions())
                                    cropImageLauncher.launch(options)
                                }
                            } else {
                                currentTab = when (tool) {
                                    com.moshitech.workmate.feature.imagestudio.components.EditorTool.FILTERS -> EditorTab.FILTERS
                                    com.moshitech.workmate.feature.imagestudio.components.EditorTool.STICKERS -> EditorTab.STICKERS
                                    com.moshitech.workmate.feature.imagestudio.components.EditorTool.SHAPES -> {
                                        viewModel.setDrawMode(com.moshitech.workmate.feature.imagestudio.viewmodel.DrawMode.SHAPES)
                                        EditorTab.DRAW
                                    }
                                    com.moshitech.workmate.feature.imagestudio.components.EditorTool.ROTATE -> EditorTab.ROTATE
                                    com.moshitech.workmate.feature.imagestudio.components.EditorTool.ADJUST -> EditorTab.ADJUST
                                    com.moshitech.workmate.feature.imagestudio.components.EditorTool.TEXT -> EditorTab.TEXT
                                    com.moshitech.workmate.feature.imagestudio.components.EditorTool.DRAW -> {
                                        viewModel.setDrawMode(com.moshitech.workmate.feature.imagestudio.viewmodel.DrawMode.PAINT)
                                        EditorTab.DRAW
                                    }
                                    else -> currentTab
                                }
                            }
                        }
                    )
                }
            ) { innerPadding ->
                Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                
                // 1. Image Canvas Area (Flexible Weight)
                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color(0xFF0D121F))
                        .clipToBounds()
                ) {
                    val boxWidth = constraints.maxWidth.toFloat()
                    val boxHeight = constraints.maxHeight.toFloat()
                    
                    // Calculate Image Fit Params
                    val imageBitmap = uiState.previewBitmap
                    var bitScale = 1f
                    var bitOffsetX = 0f
                    var bitOffsetY = 0f
                    
                    if (imageBitmap != null) {
                        val bmpW = imageBitmap.width.toFloat()
                        val bmpH = imageBitmap.height.toFloat()
                        val scaleX = boxWidth / bmpW
                        val scaleY = boxHeight / bmpH
                        bitScale = minOf(scaleX, scaleY)
                        bitOffsetX = (boxWidth - bmpW * bitScale) / 2f
                        bitOffsetY = (boxHeight - bmpH * bitScale) / 2f
                    }

                    // Canvas Content (Moved from original content lambda)
                    // Zoom and Pan state
                    var scale by remember { mutableStateOf(1f) }
                    var offset by remember { mutableStateOf(Offset.Zero) }

                    // Reset zoom when entering eyedropper mode for accurate picking
                    LaunchedEffect(isEyedropperActive) {
                        if (isEyedropperActive) {
                            scale = 1f
                            offset = Offset.Zero
                        }
                    }

                    // Reset zoom if rotation changes significantly (optional, but good for UX)
                    // LaunchedEffect(uiState.rotationAngle) { scale = 1f; offset = Offset.Zero }

                    val isDrawMode = currentTab == EditorTab.DRAW
                    
                    var currentDrawPath by remember { mutableStateOf<List<Offset>?>(null) }

                    
                    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
                        if (!isEyedropperActive && !isDrawMode) {
                            scale = (scale * zoomChange).coerceIn(1f, 5f)
                     
                            // Only allow panning when zoomed in
                            if (scale > 1f) {
                                // Adjust pan logic if rotated? Complex. 
                                // For now, standard pan relative to screen.
                                offset = offset + panChange
                            }
                        }
                    }
             
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = { viewModel.exitTextEditMode() },
                                    onDoubleTap = {
                                        scale = 1f
                                        offset = Offset.Zero
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(color = Color.White)
                        } else {
                            val bitmapToShow = if (showOriginal) uiState.originalBitmap else uiState.previewBitmap
                            if (bitmapToShow != null) {
                                Image(
                                    bitmap = bitmapToShow.asImageBitmap(),
                                    contentDescription = "Preview",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .transformable(state = transformableState)
                                        .graphicsLayer(
                                            scaleX = scale * (if (uiState.flipX) -1f else 1f),
                                            scaleY = scale * (if (uiState.flipY) -1f else 1f),
                                            rotationZ = uiState.rotationAngle,
                                            translationX = offset.x,
                                            translationY = offset.y
                                        ),
                                    contentScale = ContentScale.Fit
                                )

                            
                            // Drawing Overlay
                            if (imageBitmap != null) {
                                Canvas(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .graphicsLayer(
                                            // Apply SAME transformations as Image so paths stick to it visually during Zoom/Pan
                                            // EXCEPT when drawing? No, usually you want to draw on the zoomed image.
                                            // BUT if we use simple pointerInput on Box, screen coords don't match zoomed image.
                                            // Valid strategy: 
                                            // 1. Lock Zoom/Pan when Draw Mode (Done).
                                            // 2. Reset Zoom/Pan when entering Draw Mode? Or keep it?
                                            // If we keep it, we must map Screen -> Zoomed View -> Bitmap.
                                            // For MVP robustness, let's RESET Zoom when entering Draw Mode or just ignore Zoom in drawing.
                                            scaleX = scale,
                                            scaleY = scale,
                                            translationX = offset.x,
                                            translationY = offset.y
                                        )
                                        .pointerInput(isDrawMode) {
                                            if (isDrawMode) {
                                                detectDragGestures(
                                                    onDragStart = { startOffset ->
                                                        // RESTRICT TO IMAGE BOUNDS
                                                        val normX = (startOffset.x - bitOffsetX) / bitScale
                                                        val normY = (startOffset.y - bitOffsetY) / bitScale
                                                        
                                                        // Only start drawing if within bounds
                                                        if (normX in 0f..imageBitmap.width.toFloat() && normY in 0f..imageBitmap.height.toFloat()) {
                                                            currentDrawPath = listOf(Offset(normX, normY))
                                                        }
                                                    },
                                                    onDragEnd = {
                                                        if (uiState.selectedDrawTool == com.moshitech.workmate.feature.imagestudio.viewmodel.DrawTool.BRUSH || 
                                                            uiState.selectedDrawTool == com.moshitech.workmate.feature.imagestudio.viewmodel.DrawTool.NEON ||
                                                            uiState.selectedDrawTool == com.moshitech.workmate.feature.imagestudio.viewmodel.DrawTool.MOSAIC ||
                                                            uiState.selectedDrawTool == com.moshitech.workmate.feature.imagestudio.viewmodel.DrawTool.ERASER ||
                                                            uiState.selectedDrawTool == com.moshitech.workmate.feature.imagestudio.viewmodel.DrawTool.HIGHLIGHTER) {
                                                                currentDrawPath?.let { points ->
                                                                     viewModel.addDrawAction(
                                                                         com.moshitech.workmate.feature.imagestudio.viewmodel.DrawAction.Path(
                                                                             com.moshitech.workmate.feature.imagestudio.viewmodel.DrawPath(
                                                                                points = points,
                                                                                color = uiState.currentDrawColor,
                                                                                strokeWidth = uiState.currentStrokeWidth / bitScale,
                                                                                isEraser = uiState.selectedDrawTool == com.moshitech.workmate.feature.imagestudio.viewmodel.DrawTool.ERASER,
                                                                                isHighlighter = uiState.selectedDrawTool == com.moshitech.workmate.feature.imagestudio.viewmodel.DrawTool.HIGHLIGHTER,
                                                                                isNeon = uiState.selectedDrawTool == com.moshitech.workmate.feature.imagestudio.viewmodel.DrawTool.NEON,
                                                                                isMosaic = uiState.selectedDrawTool == com.moshitech.workmate.feature.imagestudio.viewmodel.DrawTool.MOSAIC
                                                                             )
                                                                         )
                                                                     )
                                                                     currentDrawPath = null
                                                                }
                                                        }
                                                    },
                                                    onDrag = { change, _ ->
                                                        val newPos = change.position
                                                        // Clamp to bounds
                                                        val clampedX = newPos.x.coerceIn(bitOffsetX, bitOffsetX + imageBitmap.width * bitScale)
                                                        val clampedY = newPos.y.coerceIn(bitOffsetY, bitOffsetY + imageBitmap.height * bitScale)
                                                        
                                                        val normX = (clampedX - bitOffsetX) / bitScale
                                                        val normY = (clampedY - bitOffsetY) / bitScale
                                                        
                                                        if (uiState.selectedDrawTool == com.moshitech.workmate.feature.imagestudio.viewmodel.DrawTool.BRUSH || 
                                                            uiState.selectedDrawTool == com.moshitech.workmate.feature.imagestudio.viewmodel.DrawTool.NEON ||
                                                            uiState.selectedDrawTool == com.moshitech.workmate.feature.imagestudio.viewmodel.DrawTool.MOSAIC ||
                                                            uiState.selectedDrawTool == com.moshitech.workmate.feature.imagestudio.viewmodel.DrawTool.ERASER || 
                                                            uiState.selectedDrawTool == com.moshitech.workmate.feature.imagestudio.viewmodel.DrawTool.HIGHLIGHTER) {
                                                            if (currentDrawPath != null) {
                                                                currentDrawPath = currentDrawPath?.plus(Offset(normX, normY))
                                                            }
                                                        }
                                                    }
                                                )
                                            }
                                        }
                                ) {
                                            // 1. Render All Actions in Order (Unified Z-Index)
                                            uiState.drawActions.forEach { action ->
                                                when (action) {
                                                    is com.moshitech.workmate.feature.imagestudio.viewmodel.DrawAction.Path -> {
                                                        val path = action.path
                                                        val pathObj = androidx.compose.ui.graphics.Path().apply {
                                                            if (path.points.isNotEmpty()) {
                                                                val start = path.points.first()
                                                                moveTo(start.x * bitScale + bitOffsetX, start.y * bitScale + bitOffsetY)
                                                                path.points.drop(1).forEach { pt ->
                                                                    lineTo(pt.x * bitScale + bitOffsetX, pt.y * bitScale + bitOffsetY)
                                                                }
                                                            }
                                                        }
                                                        drawPath(
                                                            path = pathObj,
                                                            color = if (path.isEraser) Color.Transparent else if (path.isHighlighter) Color(path.color).copy(alpha = 0.5f) else Color(path.color),
                                                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                                                width = path.strokeWidth * bitScale,
                                                                cap = if (path.isHighlighter) androidx.compose.ui.graphics.StrokeCap.Square else androidx.compose.ui.graphics.StrokeCap.Round,
                                                                join = androidx.compose.ui.graphics.StrokeJoin.Round
                                                            ),
                                                            blendMode = if (path.isEraser) androidx.compose.ui.graphics.BlendMode.Clear else androidx.compose.ui.graphics.BlendMode.SrcOver
                                                        )
                                                    }
                                                    is com.moshitech.workmate.feature.imagestudio.viewmodel.DrawAction.Shape -> {
                                                        val shape = action.shape
                                                        val color = Color(shape.color)
                                                        val map = { o: Offset -> Offset(o.x * bitScale + bitOffsetX, o.y * bitScale + bitOffsetY) }
                                                        val stroke = androidx.compose.ui.graphics.drawscope.Stroke(
                                                            width = shape.strokeWidth * bitScale,
                                                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                                                        )
                                                        val fill = androidx.compose.ui.graphics.drawscope.Fill

                                                        when (shape) {
                                                            is com.moshitech.workmate.feature.imagestudio.viewmodel.Shape.Line -> {
                                                                drawLine(color, map(shape.start), map(shape.end), strokeWidth = shape.strokeWidth * bitScale, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                                                            }
                                                            is com.moshitech.workmate.feature.imagestudio.viewmodel.Shape.Rectangle -> {
                                                                val tl = map(shape.topLeft)
                                                                val sz = androidx.compose.ui.geometry.Size(shape.size.width * bitScale, shape.size.height * bitScale)
                                                                drawRect(color, tl, sz, style = if (shape.filled) fill else stroke)
                                                            }
                                                            is com.moshitech.workmate.feature.imagestudio.viewmodel.Shape.Circle -> {
                                                                drawCircle(color, radius = shape.radius * bitScale, center = map(shape.center), style = if (shape.filled) fill else stroke)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                    
                                    // 3. Render Current Drag Path (Freehand)
                                    currentDrawPath?.let { points ->
                                        if (points.isNotEmpty()) {
                                            val pathObj = androidx.compose.ui.graphics.Path().apply {
                                                val start = points.first()
                                                moveTo(start.x * bitScale + bitOffsetX, start.y * bitScale + bitOffsetY)
                                                points.drop(1).forEach { pt ->
                                                    lineTo(pt.x * bitScale + bitOffsetX, pt.y * bitScale + bitOffsetY)
                                                }
                                            }
                                            drawPath(
                                                path = pathObj,
                                                color = when (uiState.selectedDrawTool) {
                                                    com.moshitech.workmate.feature.imagestudio.viewmodel.DrawTool.HIGHLIGHTER -> Color(uiState.currentDrawColor).copy(alpha = 0.5f)
                                                    com.moshitech.workmate.feature.imagestudio.viewmodel.DrawTool.ERASER -> Color(0xFFFFCDD2) // Light Red for Eraser Preview
                                                    com.moshitech.workmate.feature.imagestudio.viewmodel.DrawTool.NEON -> Color.White // Neon Core Preview
                                                    else -> Color(uiState.currentDrawColor)
                                                },
                                                style = androidx.compose.ui.graphics.drawscope.Stroke(
                                                    width = uiState.currentStrokeWidth / bitScale * bitScale, // == currentStrokeWidth
                                                    cap = if (uiState.selectedDrawTool == com.moshitech.workmate.feature.imagestudio.viewmodel.DrawTool.HIGHLIGHTER) androidx.compose.ui.graphics.StrokeCap.Square else androidx.compose.ui.graphics.StrokeCap.Round,
                                                    join = androidx.compose.ui.graphics.StrokeJoin.Round
                                                )
                                            )
                                            
                                            // Optional: Add Glow for Neon Preview
                                            if (uiState.selectedDrawTool == com.moshitech.workmate.feature.imagestudio.viewmodel.DrawTool.NEON) {
                                                drawPath(
                                                    path = pathObj,
                                                    color = Color(uiState.currentDrawColor).copy(alpha = 0.5f),
                                                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                                                        width = (uiState.currentStrokeWidth * 2f),
                                                        cap = androidx.compose.ui.graphics.StrokeCap.Round,
                                                        join = androidx.compose.ui.graphics.StrokeJoin.Round
                                                    )
                                                )
                                            }
                                        }
                                    }


                                }
                            }
                            } else {
                                Text("No Image Loaded", color = Color.Gray)
                            }
                        }
                 
                        // Eyedropper Overlay
                        if (isEyedropperActive && uiState.previewBitmap != null) {
                           Box(
                               modifier = Modifier
                                   .matchParentSize()
                                   .pointerInput(Unit) {
                                       detectTapGestures { tapOffset ->
                                           val bitmap = uiState.previewBitmap
                                           if (bitmap != null) {
                                               // Assuming ContentScale.Fit logic (simplified for brevity, ideal implementation matches Image scale)
                                               // For robust eyedropper, we need exact mapping which is complex with Fit/Fill.
                                               // Using simplified mapping or requiring Zoom=1 for picking.
                                               if (scale == 1f) {
                                                   // Basic mapping attempt
                                                    val viewWidth = size.width
                                                    val viewHeight = size.height.toFloat()
                                                    val bmpWidth = bitmap.width.toFloat()
                                                    val bmpHeight = bitmap.height.toFloat()
                                                    
                                                    val scaleFactor = minOf(viewWidth / bmpWidth, viewHeight / bmpHeight)
                                                    val scaledWidth = bmpWidth * scaleFactor
                                                    val scaledHeight = bmpHeight * scaleFactor
                                                    
                                                    val offsetX = (viewWidth - scaledWidth) / 2f
                                                    val offsetY = (viewHeight - scaledHeight) / 2f
                                                    
                                                    val bitmapX = ((tapOffset.x - offsetX) / scaleFactor).toInt()
                                                    val bitmapY = ((tapOffset.y - offsetY) / scaleFactor).toInt()
                                                    
                                                    if (bitmapX in 0 until bitmap.width && bitmapY in 0 until bitmap.height) {
                                                         val pixel = bitmap.getPixel(bitmapX, bitmapY)
                                                         val color = Color(pixel)
                                                         eyedropperCallback?.invoke(color)
                                                         eyedropperCallback = null
                                                    }
                                               }
                                           }
                                       }
                                   }
                           ) {
                               Box(
                                   modifier = Modifier.align(Alignment.Center).background(Color.Black.copy(0.7f), RoundedCornerShape(8.dp)).padding(16.dp)
                               ) { Text("Tap image to pick color", color = Color.White) }
                           }
                        }
                 
                        // Compare Label
                        if (showOriginal) {
                            Box(modifier = Modifier.align(Alignment.TopCenter).padding(top = 16.dp).background(Color.Black.copy(0.6f), RoundedCornerShape(4.dp)).padding(8.dp, 4.dp)) {
                                Text("Original", color = Color.White)
                            }
                        }
                 
                        // Zoom indicator
                        if (scale > 1f) {
                            Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp).background(Color.Black.copy(0.6f), RoundedCornerShape(4.dp)).padding(8.dp, 4.dp)) {
                                Text("${(scale * 100).toInt()}%", color = Color.White)
                            }
                        }
                 
                        // Text Layers
                        uiState.textLayers.forEach { textLayer ->
                            key(textLayer.id) {
                                com.moshitech.workmate.feature.imagestudio.components.TextBoxComposable(
                                    layer = textLayer,
                                    isSelected = textLayer.id == uiState.selectedTextLayerId,
                                    isEditing = textLayer.id == uiState.editingTextLayerId,
                                    onSelect = { viewModel.selectTextLayer(it) },
                                    onEdit = { viewModel.enterTextEditMode(it) },
                                    onTransform = { id, pan, zoom, rotation -> viewModel.updateTextLayerTransform(id, pan, zoom, rotation) },
                                    onTextChange = { id, text -> viewModel.updateTextInline(id, text) },
                                    onDuplicate = { viewModel.duplicateTextLayer(it) },
                                    onDelete = { viewModel.removeTextLayer(it) },
                                )
                            }
                        }
        
                        // Shape Layers
                        uiState.shapeLayers.forEach { layer ->
                            key(layer.id) {
                                com.moshitech.workmate.feature.imagestudio.components.ShapeBoxComposable(
                                    layer = layer,
                                    isSelected = layer.id == uiState.selectedShapeLayerId,
                                    onSelect = { viewModel.selectShapeLayer(it) },
                                    onTransform = { id, pan, zoom, rot -> viewModel.updateShapeLayerTransform(id, pan, zoom, rot) },
                                    onDelete = { viewModel.deleteShapeLayer(it) }
                                )
                            }
                        }

                        // Sticker Layers
                        uiState.stickerLayers.forEach { layer ->
                           key(layer.id) {
                               com.moshitech.workmate.feature.imagestudio.components.StickerBoxComposable(
                                   layer = layer,
                                   isSelected = layer.id == uiState.selectedStickerLayerId,
                                   onSelect = { viewModel.selectSticker(it) },
                                   onTransform = { id, pan, zoom, rot -> viewModel.updateStickerTransform(id, pan, zoom, rot) },
                                   onDelete = { viewModel.removeSticker(it) },
                                   onFlip = { viewModel.flipSticker(it) } 
                               )
                           }
                        }

                        // ADD NEW TEXT Pill Button (Overlay on Image Canvas)
                        if (currentTab == EditorTab.TEXT) {
                            Button(
                                onClick = { viewModel.createTextBoxAtCenter() },
                                modifier = Modifier
                                    .height(40.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF007AFF) // iOS Blue
                                ),
                                shape = RoundedCornerShape(50), // Pill Shape
                                contentPadding = PaddingValues(horizontal = 16.dp)
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Add New Text",
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                } // End Canvas Box

                // 2. Resizable Toolbar Panel (The area user wants to adjust)
                // Increased default height to accommodate Draw tools (300dp)
                var panelHeight by remember { mutableStateOf(300.dp) }
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(panelHeight)
                        .background(Color(0xFF0D121F))
                        .clipToBounds() // Prevent content from bleeding into Bottom Navigation
                ) {
                    // Drag Handle (Grip)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                            .background(Color(0xFF1E1E1E)) // Grip color
                            .pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    // Inverted logic: Dragging UP increases height (y is negative)
                                    // But wait, change.position is relative. dragAmount.y < 0 means UP.
                                    // If we drag UP, we want panel to GROW.
                                    // So newHeight = currentHeight - dragAmount.y
                                    val newHeight = (panelHeight - dragAmount.y.dp).coerceIn(150.dp, 600.dp)
                                    panelHeight = newHeight
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        // Visual Grip Pill
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp)
                                .background(Color.Gray, RoundedCornerShape(2.dp))
                        )
                    }

                    // Tool Content Area (Scrollable contents go here)
                    Box(modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0D121F))
                        .background(Color(0xFF0D121F))
                        // REMOVED verticalScroll(rememberScrollState()) here to avoid conflict with Lazy lists in tabs
                    ) {
                         when (currentTab) {
                            EditorTab.CROP -> {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    androidx.compose.material3.Button(onClick = {
                                        if (imageUri != null) {
                                            val options = CropImageContractOptions(uri = imageUri, cropImageOptions = CropImageOptions())
                                            cropImageLauncher.launch(options)
                                        }
                                    }) { Text("Crop Image") }
                                }
                            }
                            EditorTab.ADJUST -> Box(Modifier.fillMaxSize()) {
                                AdjustTab(
                                brightness = uiState.brightness,
                                contrast = uiState.contrast,
                                saturation = uiState.saturation,
                                hue = uiState.hue,
                                temperature = uiState.temperature,
                                tint = uiState.tint,
                                onBrightnessChange = viewModel::updateBrightness,
                                onContrastChange = viewModel::updateContrast,
                                onSaturationChange = viewModel::updateSaturation,
                                onHueChange = viewModel::setHue,
                                onTemperatureChange = viewModel::setTemperature,
                                onTintChange = viewModel::setTint,
                                onReset = viewModel::resetAdjustments
                            )
                            }
                            EditorTab.FILTERS -> {
                                com.moshitech.workmate.feature.imagestudio.components.FiltersTab(
                                    activeFilterId = uiState.activeFilterId,
                                    previewBitmap = uiState.originalBitmap,
                                    onFilterSelected = viewModel::applyFilter,
                                    onClearFilter = viewModel::clearFilter
                                )
                            }
                            EditorTab.STICKERS -> {
                                com.moshitech.workmate.feature.imagestudio.components.StickersTab(
                                    onStickerSelected = { emoji -> viewModel.addSticker(text = emoji) }
                                )
                            }
                            EditorTab.TEXT -> {
                                // Text Editor Toolbar Content
                                if (uiState.selectedTextLayerId != null) {
                                    val selectedLayer = uiState.textLayers.find { it.id == uiState.selectedTextLayerId }
                                    selectedLayer?.let { layer ->
                                        // This component needs to fill the panel to be useful in this new layout
                                        Box(Modifier.fillMaxSize()) {
                                            com.moshitech.workmate.feature.imagestudio.components.TextEditorToolbar(
                                                layer = layer,
                                                visible = true,
                                                onUpdate = { updated -> viewModel.updateTextProperty(layer.id) { updated } },
                                                onRequestEyedropper = { callback -> eyedropperCallback = callback },
                                                onRequestTexturePick = { texturePickerLauncher.launch("image/*") },
                                                modifier = Modifier.fillMaxSize() // Force fill
                                            )
                                        }
                                    }
                                } else {
                                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text("Select or Add Text to Edit", color = Color.Gray)
                                    }
                                }
                            }
                            EditorTab.SHAPES -> {
                                // Shapes now handled in Draw tab
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("Switching to Shapes...", color = Color.White)
                                    LaunchedEffect(Unit) {
                                        viewModel.setDrawMode(com.moshitech.workmate.feature.imagestudio.viewmodel.DrawMode.SHAPES)
                                        currentTab = EditorTab.DRAW
                                    }
                                }
                            }
                            EditorTab.DRAW -> {
                                com.moshitech.workmate.feature.imagestudio.components.DrawAndShapesToolbar(uiState, viewModel)
                            }
                            // EditorTab.SHAPES merged into DRAW
                            EditorTab.ROTATE -> {
                                Column(
                                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(24.dp)
                                ) {
                                    // Icons Area
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceAround,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Left Rotate
                                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { viewModel.rotate90CCW() }) {
                                            Icon(Icons.Default.RotateLeft, "Left", tint = Color.White, modifier = Modifier.size(28.dp))
                                            Spacer(Modifier.height(4.dp))
                                            Text("Left", color = Color.Gray, fontSize = 12.sp)
                                        }

                                        // Right Rotate
                                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { viewModel.rotate90CW() }) {
                                            Icon(Icons.Default.RotateRight, "Right", tint = Color.White, modifier = Modifier.size(28.dp))
                                            Spacer(Modifier.height(4.dp))
                                            Text("Right", color = Color.Gray, fontSize = 12.sp)
                                        }

                                        // Flip
                                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { viewModel.flipHorizontal() }) {
                                            Icon(Icons.Default.SwapHoriz, "Flip", tint = Color.White, modifier = Modifier.size(28.dp))
                                            Spacer(Modifier.height(4.dp))
                                            Text("Flip", color = Color.Gray, fontSize = 12.sp)
                                        }
                                    }

                                    // Slider Area
                                    Column(Modifier.fillMaxWidth()) {
                                        Slider(
                                            value = uiState.rotationAngle,
                                            onValueChange = { viewModel.setRotationAngle(it) },
                                            valueRange = 0f..360f,
                                            colors = SliderDefaults.colors(
                                                thumbColor = Color(0xFF007AFF),
                                                activeTrackColor = Color(0xFF007AFF),
                                                inactiveTrackColor = Color(0xFF2C2C2E)
                                            ),
                                            thumb = {
                                                Box(
                                                    modifier = Modifier
                                                        .size(20.dp)
                                                        .background(Color.White, CircleShape)
                                                        .padding(4.dp)
                                                        .background(Color(0xFF007AFF), CircleShape)
                                                )
                                            },
                                            track = { sliderState ->
                                                SliderDefaults.Track(
                                                    colors = SliderDefaults.colors(
                                                        activeTrackColor = Color(0xFF007AFF),
                                                        inactiveTrackColor = Color(0xFF2C2C2E)
                                                    ),
                                                    sliderState = sliderState,
                                                    modifier = Modifier.height(4.dp)
                                                )
                                            }
                                        )
                                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                            Text("${uiState.rotationAngle.toInt()}°", color = Color.Gray, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }

                        }
                    }
                } // End Resizable Panel

                } // End Resizable Panel Content
            } // End Scaffold Content Column (Panel + Image)
            
            // NOTE: The BottomBar is handled by the Inner Scaffold above.
            // The redundant code block for Drawing Canvas was removed here.
        } // End Inner Scaffold Block
        
        // Save Dialog - inside Scaffold where uiState is accessible
        if (uiState.showSaveDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissSaveDialog() },
                title = { Text("Save Image") },
                text = {
                    Column {
                        Text("Enter filename:", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = uiState.saveFilename,
                            onValueChange = { viewModel.updateSaveFilename(it) },
                            label = { Text("Filename") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            ".jpg will be added automatically",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.saveImage(uiState.saveFilename) {
                                navController.popBackStack()
                            }
                        },
                        enabled = uiState.saveFilename.isNotBlank()
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissSaveDialog() }) {
                        Text("Cancel")
                    }
                }
            )
        }
        
        // Image Source Selection Dialog
        if (showImageSourceDialog) {
            AlertDialog(
                onDismissRequest = { showImageSourceDialog = false },
                title = { Text("Select Image Source") },
                text = {
                    Column {
                        // Camera option
                        TextButton(
                            onClick = {
                                showImageSourceDialog = false
                                val uri = createImageUri()
                                cameraImageUri.value = uri
                                cameraLauncher.launch(uri)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CameraAlt, "Camera", modifier = Modifier.padding(end = 8.dp))
                            Text("Take Photo")
                        }
                        
                        // Gallery option
                        TextButton(
                            onClick = {
                                showImageSourceDialog = false
                                galleryLauncher.launch(
                                    androidx.activity.result.PickVisualMediaRequest(
                                        androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Image, "Gallery", modifier = Modifier.padding(end = 8.dp))
                            Text("Choose from Gallery")
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showImageSourceDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
        
        // Text Input Dialog
        if (uiState.showTextDialog) {
            val editingLayer = remember(uiState.editingTextId) { 
                uiState.textLayers.find { it.id == uiState.editingTextId } 
            }
            
            var textInput by remember { mutableStateOf(editingLayer?.text ?: "") }
            var selectedColor by remember { mutableStateOf(editingLayer?.color ?: android.graphics.Color.WHITE) }
            var fontSize by remember { mutableStateOf(editingLayer?.fontSize ?: 24f) }
            var fontFamily by remember { mutableStateOf(editingLayer?.fontFamily ?: com.moshitech.workmate.feature.imagestudio.viewmodel.AppFont.DEFAULT) }
            var isBold by remember { mutableStateOf(editingLayer?.isBold ?: false) }
            var isItalic by remember { mutableStateOf(editingLayer?.isItalic ?: false) }
            var alignment by remember { mutableStateOf(editingLayer?.alignment ?: com.moshitech.workmate.feature.imagestudio.viewmodel.TextAlignment.LEFT) }
            var hasOutline by remember { mutableStateOf(editingLayer?.hasOutline ?: false) }
            var outlineColor by remember { mutableStateOf(editingLayer?.outlineColor ?: android.graphics.Color.BLACK) }
            var hasShadow by remember { mutableStateOf(editingLayer?.hasShadow ?: false) }
            
            AlertDialog(
                onDismissRequest = { viewModel.dismissTextDialog() },
                title = { Text(if (uiState.editingTextId != null) "Edit Text" else "Add Text") },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp) // Fixed height to allow scrolling inner content
                            .verticalScroll(androidx.compose.foundation.rememberScrollState())
                    ) {
                        OutlinedTextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            label = { Text("Enter text") },
                            singleLine = false,
                            maxLines = 3,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Font Family Selection
                        Text("Font:", style = MaterialTheme.typography.bodyMedium)
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            com.moshitech.workmate.feature.imagestudio.viewmodel.AppFont.values().forEach { font ->
                                androidx.compose.material3.FilterChip(
                                    selected = fontFamily == font,
                                    onClick = { fontFamily = font },
                                    label = { Text(font.name.lowercase().replace("_", " ").capitalize(java.util.Locale.ROOT)) }
                                )
                            }
                        }

                        // Style & Alignment
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Style
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                androidx.compose.material3.FilledIconToggleButton(
                                    checked = isBold,
                                    onCheckedChange = { isBold = it }
                                ) {
                                    Text("B", style = androidx.compose.ui.text.TextStyle(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold))
                                }
                                androidx.compose.material3.FilledIconToggleButton(
                                    checked = isItalic,
                                    onCheckedChange = { isItalic = it }
                                ) {
                                    Text("I", style = androidx.compose.ui.text.TextStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic))
                                }
                            }
                            
                            // Alignment
                            Row(
                                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, androidx.compose.foundation.shape.RoundedCornerShape(8.dp)),
                                horizontalArrangement = Arrangement.spacedBy(0.dp)
                            ) {
                                com.moshitech.workmate.feature.imagestudio.viewmodel.TextAlignment.values().forEach { align ->
                                    androidx.compose.material3.IconButton(
                                        onClick = { alignment = align },
                                        colors = androidx.compose.material3.IconButtonDefaults.iconButtonColors(
                                            containerColor = if (alignment == align) MaterialTheme.colorScheme.primary else Color.Transparent,
                                            contentColor = if (alignment == align) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    ) {
                                        when (align) {
                                            com.moshitech.workmate.feature.imagestudio.viewmodel.TextAlignment.LEFT -> Icon(Icons.Filled.FormatAlignLeft, "Left")
                                            com.moshitech.workmate.feature.imagestudio.viewmodel.TextAlignment.CENTER -> Icon(Icons.Filled.FormatAlignCenter, "Center")
                                            com.moshitech.workmate.feature.imagestudio.viewmodel.TextAlignment.RIGHT -> Icon(Icons.Filled.FormatAlignRight, "Right")
                                            com.moshitech.workmate.feature.imagestudio.viewmodel.TextAlignment.JUSTIFY -> Icon(Icons.Filled.FormatAlignCenter, "Justify") // Temp icon
                                        }
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Text Color
                        Text("Color:", style = MaterialTheme.typography.bodyMedium)
                        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(androidx.compose.foundation.rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                android.graphics.Color.WHITE, android.graphics.Color.BLACK, android.graphics.Color.RED,
                                android.graphics.Color.BLUE, android.graphics.Color.GREEN, android.graphics.Color.YELLOW,
                                android.graphics.Color.CYAN, android.graphics.Color.MAGENTA, android.graphics.Color.LTGRAY
                            ).forEach { color ->
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(Color(color), androidx.compose.foundation.shape.CircleShape)
                                        .border(
                                            width = if (selectedColor == color) 3.dp else 1.dp,
                                            color = if (selectedColor == color) MaterialTheme.colorScheme.primary else Color.Gray,
                                            shape = androidx.compose.foundation.shape.CircleShape
                                        )
                                        .clickable { selectedColor = color }
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Effects
                        Text("Effects:", style = MaterialTheme.typography.bodyMedium)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            androidx.compose.material3.Checkbox(checked = hasOutline, onCheckedChange = { hasOutline = it })
                            Text("Outline")
                            if (hasOutline) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier.size(24.dp).background(Color(outlineColor), androidx.compose.foundation.shape.CircleShape)
                                        .border(1.dp, Color.Gray, androidx.compose.foundation.shape.CircleShape)
                                        .clickable { 
                                            // Toggle basic outline colors for simplicity
                                            outlineColor = if (outlineColor == android.graphics.Color.BLACK) android.graphics.Color.WHITE else android.graphics.Color.BLACK 
                                        }
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            androidx.compose.material3.Checkbox(checked = hasShadow, onCheckedChange = { hasShadow = it })
                            Text("Shadow")
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Size: ${fontSize.toInt()}sp", style = MaterialTheme.typography.bodyMedium)
                        Slider(
                            value = fontSize,
                            onValueChange = { fontSize = it },
                            valueRange = 12f..80f,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (textInput.isNotBlank()) {
                                if (uiState.editingTextId != null) {
                                    viewModel.updateTextLayer(
                                        textId = uiState.editingTextId!!,
                                        text = textInput,
                                        color = selectedColor,
                                        fontSize = fontSize,
                                        fontFamily = fontFamily,
                                        isBold = isBold,
                                        isItalic = isItalic,
                                        alignment = alignment,
                                        hasOutline = hasOutline,
                                        outlineColor = outlineColor,
                                        hasShadow = hasShadow

                                    )
                                } else {
                                    viewModel.addTextLayer(
                                        text = textInput,
                                        color = selectedColor,
                                        fontSize = fontSize,
                                        fontFamily = fontFamily,
                                        isBold = isBold,
                                        isItalic = isItalic,
                                        alignment = alignment,
                                        hasOutline = hasOutline,
                                        outlineColor = outlineColor,
                                        hasShadow = hasShadow
                                    )
                                }
                            }
                        },
                        enabled = textInput.isNotBlank()
                    ) {
                        Text(if (uiState.editingTextId != null) "Update" else "Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissTextDialog() }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}


@Composable
fun AdjustTab(
    brightness: Float,
    contrast: Float,
    saturation: Float,
    hue: Float,
    temperature: Float,
    tint: Float,
    onBrightnessChange: (Float) -> Unit,
    onContrastChange: (Float) -> Unit,
    onSaturationChange: (Float) -> Unit,
    onHueChange: (Float) -> Unit,
    onTemperatureChange: (Float) -> Unit,
    onTintChange: (Float) -> Unit,
    onReset: () -> Unit // New Callback
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp) // Reduced Gap 
    ) {
        // Header with Title and Reset
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Adjustments",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(start = 8.dp)
            )
            
            TextButton(onClick = onReset) {
                Text("Reset Default", color = Color(0xFF007AFF), fontSize = 14.sp)
            }
        }

        // Helper functions
        fun formatPercent(value: Float): String {
             val intVal = (value * 100).toInt()
             return if (intVal > 0) "+$intVal" else "$intVal"
        }
        
        fun formatFactor(value: Float): String {
            val intVal = ((value - 1f) * 100).toInt()
            return if (intVal > 0) "+$intVal" else "$intVal"
        }

        CompactSlider(
            label = "Brightness",
            value = brightness,
            onValueChange = onBrightnessChange,
            range = -1f..1f,
            displayValue = formatPercent(brightness)
        )

        CompactSlider(
            label = "Contrast",
            value = contrast,
            onValueChange = onContrastChange,
            range = 0f..2f,
            displayValue = formatFactor(contrast)
        )

        CompactSlider(
            label = "Saturation",
            value = saturation,
            onValueChange = onSaturationChange,
            range = 0f..2f,
            displayValue = formatFactor(saturation)
        )

        CompactSlider(
            label = "Hue",
            value = hue,
            onValueChange = onHueChange,
            range = -180f..180f,
            displayValue = "${hue.toInt()}°"
        )
        
        CompactSlider(
            label = "Temp",
            value = temperature,
            onValueChange = onTemperatureChange,
            range = -1f..1f,
            displayValue = formatPercent(temperature)
        )
        
        CompactSlider(
            label = "Tint",
            value = tint,
            onValueChange = onTintChange,
            range = -1f..1f,
            displayValue = formatPercent(tint)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompactSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    range: ClosedFloatingPointRange<Float>,
    displayValue: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color(0xFFE0E0E0),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(70.dp)
        )
        
        // Custom Slider with Thin Track and Custom Thumb
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                activeTrackColor = Color(0xFF007AFF),
                inactiveTrackColor = Color(0xFF2C2C2E)
            ),
            thumb = {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(Color.White, CircleShape)
                        .padding(6.dp) 
                        .background(Color(0xFF007AFF), CircleShape)
                )
            },
            track = { sliderState ->
                SliderDefaults.Track(
                    colors = SliderDefaults.colors(
                        activeTrackColor = Color(0xFF007AFF),
                        inactiveTrackColor = Color(0xFF2C2C2E)
                    ),
                    sliderState = sliderState,
                    modifier = Modifier.height(4.dp) // Thinner Track (Default is often thicker)
                )
            }
        )
        
        Text(
            text = displayValue,
            color = Color(0xFFB0B0B0),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(42.dp),
            textAlign = TextAlign.End
        )
    }
}

