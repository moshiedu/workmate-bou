package com.moshitech.workmate.feature.imagestudio.ui

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.foundation.layout.requiredSize
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
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
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
import com.moshitech.workmate.feature.imagestudio.components.TextEditorBottomToolbar
import com.moshitech.workmate.feature.imagestudio.components.TextEditorToolbar
import com.moshitech.workmate.feature.imagestudio.components.CompactModernSlider
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
    var showExitConfirmDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Eyedropper State (Lifted for Toolbar access)
    var eyedropperCallback by remember { mutableStateOf<((Color) -> Unit)?>(null) }
    val isEyedropperActive = eyedropperCallback != null
    
    // Deselect layers when switching tabs (standard photo editor behavior)
    LaunchedEffect(currentTab) {
        when (currentTab) {
            EditorTab.TEXT -> {
                // Deselect shapes and stickers when on Text tab
                viewModel.deselectShape()
                viewModel.deselectSticker()
            }
            EditorTab.DRAW -> {
                // Deselect text and stickers when on Draw tab
                viewModel.deselectText()
                viewModel.deselectSticker()
            }
            else -> {
                // Deselect all layers when on other tabs (Adjust, Filters, etc.)
                viewModel.deselectText()
                viewModel.deselectShape()
                viewModel.deselectSticker()
            }
        }
    }
    
    // Reset zoom when entering eyedropper mode for accurate picking
    LaunchedEffect(isEyedropperActive) {
        if (isEyedropperActive) {
            viewModel.clearMessage() // Clear any previous messages
        }
    }
    
    // Back Press Handler - Show confirmation if there are unsaved changes
    androidx.activity.compose.BackHandler(enabled = true) {
        // Check if there are any changes (text layers, shapes, stickers, or adjustments)
        val hasChanges = uiState.textLayers.isNotEmpty() || 
                        uiState.shapeLayers.isNotEmpty() || 
                        uiState.stickerLayers.isNotEmpty() ||
                        uiState.brightness != 0f ||
                        uiState.contrast != 1f ||
                        uiState.saturation != 1f ||
                        uiState.hue != 0f
        
        if (hasChanges) {
            showExitConfirmDialog = true
        } else {
            navController.popBackStack()
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
                    
                    // Center: Undo/Redo/Preview
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
                        Spacer(modifier = Modifier.width(16.dp))
                        IconButton(
                            onClick = { viewModel.togglePreviewMode() }
                        ) {
                            Icon(
                                imageVector = if (uiState.isPreviewMode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (uiState.isPreviewMode) "Show UI" else "Preview",
                                tint = Color.White,
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
                        if (!isEyedropperActive) {
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
                            .transformable(state = transformableState)
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
                            val density = LocalDensity.current
                            // Calculate display size based on Bitmap dimensions (Unscaled)
                            val displayWidth = with(density) { bitmapToShow.width.toDp() }
                            val displayHeight = with(density) { bitmapToShow.height.toDp() }

                            Box(
                                modifier = Modifier
                                    .requiredSize(displayWidth, displayHeight)
                                    .graphicsLayer {
                                        // Apply Fit Scale (bitScale) AND User Transforms (scale/offset/rotation)
                                        // This ensures the Box visual matches the user operation, 
                                        // while internal coordinates match the Bitmap (0..Width)
                                        val fitScale = bitScale
                                        scaleX = fitScale * scale * (if (uiState.flipX) -1f else 1f)
                                        scaleY = fitScale * scale * (if (uiState.flipY) -1f else 1f)
                                        rotationZ = uiState.rotationAngle
                                        translationX = offset.x
                                        translationY = offset.y
                                    }
                            ) {
                                // 1. Transparency Checkerboard
                                com.moshitech.workmate.feature.imagestudio.components.TransparencyCheckerboard(
                                    modifier = Modifier.fillMaxSize()
                                )
                                // 2. Image
                                Image(
                                    bitmap = bitmapToShow.asImageBitmap(),
                                    contentDescription = "Preview",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.FillBounds
                                )

                                // 3. Drawing Overlay
                                val imageBitmap = uiState.previewBitmap
                                if (imageBitmap != null) {
                                     // Mosaic Shader Preparation
                                    val pixelatedBitmap = remember(imageBitmap, uiState.mosaicIntensity, uiState.mosaicColorMode, uiState.posterizeLevels) {
                                         // Create pixelated bitmap with color mode support
                                        val width = imageBitmap.width
                                        val height = imageBitmap.height
                                        val pScale = uiState.mosaicIntensity.coerceIn(0.01f, 0.20f)
                                        val scaledW = (width * pScale).toInt().coerceAtLeast(1)
                                        val scaledH = (height * pScale).toInt().coerceAtLeast(1)
                                        
                                        // Create scaled down version
                                        val small = android.graphics.Bitmap.createScaledBitmap(imageBitmap, scaledW, scaledH, false)
                                        
                                        // Apply color mode processing
                                        val processed = when (uiState.mosaicColorMode) {
                                            com.moshitech.workmate.feature.imagestudio.viewmodel.MosaicColorMode.AVERAGE -> small
                                            com.moshitech.workmate.feature.imagestudio.viewmodel.MosaicColorMode.DOMINANT -> {
                                                // Apply dominant color algorithm (Simplified for stability in refactor)
                                                // Note: Ideally moving this to a helper function
                                                small 
                                            }
                                            com.moshitech.workmate.feature.imagestudio.viewmodel.MosaicColorMode.POSTERIZE -> {
                                                val result = small.copy(small.config ?: android.graphics.Bitmap.Config.ARGB_8888, true)
                                                val step = 255 / (uiState.posterizeLevels - 1)
                                                for (y in 0 until small.height) {
                                                    for (x in 0 until small.width) {
                                                        val color = small.getPixel(x, y)
                                                        val r = (android.graphics.Color.red(color) / step) * step
                                                        val g = (android.graphics.Color.green(color) / step) * step
                                                        val b = (android.graphics.Color.blue(color) / step) * step
                                                        val a = android.graphics.Color.alpha(color)
                                                        result.setPixel(x, y, android.graphics.Color.argb(a, r, g, b))
                                                    }
                                                }
                                                result
                                            }
                                        }
                                        
                                        // Scale back up
                                        android.graphics.Bitmap.createScaledBitmap(processed, width, height, false)
                                    }
                                    
                                    val mosaicShader = remember(pixelatedBitmap) {
                                        android.graphics.BitmapShader(pixelatedBitmap, android.graphics.Shader.TileMode.CLAMP, android.graphics.Shader.TileMode.CLAMP)
                                    }
                                    
                                    Canvas(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .pointerInput(isDrawMode, uiState.activeDrawMode) {
                                                if (isDrawMode && uiState.activeDrawMode == com.moshitech.workmate.feature.imagestudio.viewmodel.DrawMode.PAINT) {
                                                    awaitEachGesture {
                                                        val down = awaitFirstDown(requireUnconsumed = false)
                                                        var dragPtrId = down.id
                                                        var isMultiTouch = false
                                                        
                                                        // Initial Point (Already in Local/Bitmap Coordinates due to ContentBox scaling)
                                                        val startPos = down.position
                                                        val rawNormX = startPos.x
                                                        val rawNormY = startPos.y
                                                        
                                                        if (rawNormX in 0f..imageBitmap.width.toFloat() && rawNormY in 0f..imageBitmap.height.toFloat()) {
                                                            currentDrawPath = listOf(Offset(rawNormX, rawNormY))
                                                        }

                                                        var pathPoints = currentDrawPath
                                                        
                                                        do {
                                                            val event = awaitPointerEvent()
                                                            if (event.changes.size > 1) {
                                                                isMultiTouch = true
                                                                currentDrawPath = null
                                                                break 
                                                            }
                                                            
                                                            val change = event.changes.find { it.id == dragPtrId }
                                                            if (change != null && change.pressed) {
                                                                if (currentDrawPath != null) {
                                                                    val newPos = change.position
                                                                    val clampedNormX = newPos.x.coerceIn(0f, imageBitmap.width.toFloat())
                                                                    val clampedNormY = newPos.y.coerceIn(0f, imageBitmap.height.toFloat())
                                                                    
                                                                    if (uiState.selectedDrawTool == com.moshitech.workmate.feature.imagestudio.viewmodel.DrawTool.BRUSH || 
                                                                        uiState.selectedDrawTool == com.moshitech.workmate.feature.imagestudio.viewmodel.DrawTool.NEON ||
                                                                        uiState.selectedDrawTool == com.moshitech.workmate.feature.imagestudio.viewmodel.DrawTool.MOSAIC ||
                                                                        uiState.selectedDrawTool == com.moshitech.workmate.feature.imagestudio.viewmodel.DrawTool.ERASER ||
                                                                        uiState.selectedDrawTool == com.moshitech.workmate.feature.imagestudio.viewmodel.DrawTool.HIGHLIGHTER) {
                                                                            
                                                                            currentDrawPath = currentDrawPath?.plus(Offset(clampedNormX, clampedNormY))
                                                                            pathPoints = currentDrawPath
                                                                    }
                                                                }
                                                                change.consume()
                                                            }
                                                        } while (event.changes.any { it.pressed })
                                                        
                                                        if (!isMultiTouch && pathPoints != null && pathPoints!!.size > 1) {
                                                            if (uiState.selectedDrawTool == com.moshitech.workmate.feature.imagestudio.viewmodel.DrawTool.BRUSH || 
                                                                uiState.selectedDrawTool == com.moshitech.workmate.feature.imagestudio.viewmodel.DrawTool.NEON ||
                                                                uiState.selectedDrawTool == com.moshitech.workmate.feature.imagestudio.viewmodel.DrawTool.MOSAIC ||
                                                                uiState.selectedDrawTool == com.moshitech.workmate.feature.imagestudio.viewmodel.DrawTool.ERASER ||
                                                                uiState.selectedDrawTool == com.moshitech.workmate.feature.imagestudio.viewmodel.DrawTool.HIGHLIGHTER) {
                                                                    
                                                                    viewModel.addDrawAction(
                                                                        com.moshitech.workmate.feature.imagestudio.viewmodel.DrawAction.Path(
                                                                            com.moshitech.workmate.feature.imagestudio.viewmodel.DrawPath(
                                                                                points = pathPoints!!,
                                                                                color = uiState.currentDrawColor,
                                                                                // Store Unscaled Width (relative to Bitmap)
                                                                                strokeWidth = uiState.currentStrokeWidth / bitScale,
                                                                                isEraser = uiState.selectedDrawTool == com.moshitech.workmate.feature.imagestudio.viewmodel.DrawTool.ERASER,
                                                                                isHighlighter = uiState.selectedDrawTool == com.moshitech.workmate.feature.imagestudio.viewmodel.DrawTool.HIGHLIGHTER,
                                                                                isNeon = uiState.selectedDrawTool == com.moshitech.workmate.feature.imagestudio.viewmodel.DrawTool.NEON,
                                                                                isMosaic = uiState.selectedDrawTool == com.moshitech.workmate.feature.imagestudio.viewmodel.DrawTool.MOSAIC
                                                                            )
                                                                        )
                                                                    )
                                                            }
                                                        }
                                                        currentDrawPath = null
                                                    }
                                                }
                                            }
                                    ) {
                                        // Render Actions
                                        uiState.drawActions.forEach { action ->
                                            when (action) {
                                                is com.moshitech.workmate.feature.imagestudio.viewmodel.DrawAction.Path -> {
                                                    val path = action.path
                                                    val pathObj = androidx.compose.ui.graphics.Path().apply {
                                                        if (path.points.isNotEmpty()) {
                                                            // Use points directly (Bitmap Coords)
                                                            val start = path.points.first()
                                                            moveTo(start.x, start.y)
                                                            path.points.drop(1).forEach { pt ->
                                                                lineTo(pt.x, pt.y)
                                                            }
                                                        }
                                                    }
                                                    
                                                    // Effective Stroke Width (Rendered in Box with bitScale)
                                                    // Stored as Unscaled. We need to render with Unscaled width so that:
                                                    // VisualWidth = UnscaledWidth * bitScale = (Slider/bitScale) * bitScale = Slider.
                                                    val effectiveStrokeWidth = path.strokeWidth
                                                    
                                                    if (path.isMosaic) {
                                                        drawIntoCanvas { canvas ->
                                                            val paint = androidx.compose.ui.graphics.Paint().asFrameworkPaint()
                                                            paint.shader = mosaicShader
                                                            paint.style = android.graphics.Paint.Style.STROKE
                                                            paint.strokeWidth = effectiveStrokeWidth
                                                            paint.alpha = android.graphics.Color.alpha(path.color)
                                                            paint.strokeCap = android.graphics.Paint.Cap.ROUND
                                                            paint.strokeJoin = android.graphics.Paint.Join.ROUND
                                                            paint.isAntiAlias = true
                                                            canvas.nativeCanvas.drawPath(pathObj.asAndroidPath(), paint)
                                                        }
                                                    } else if (path.isNeon) {
                                                        drawIntoCanvas { canvas ->
                                                            val paint = androidx.compose.ui.graphics.Paint().asFrameworkPaint()
                                                            paint.color = path.color
                                                            paint.style = android.graphics.Paint.Style.STROKE
                                                            paint.strokeWidth = effectiveStrokeWidth
                                                            paint.strokeCap = android.graphics.Paint.Cap.ROUND
                                                            paint.strokeJoin = android.graphics.Paint.Join.ROUND
                                                            paint.isAntiAlias = true
                                                            paint.setShadowLayer(15f, 0f, 0f, path.color)
                                                            canvas.nativeCanvas.drawPath(pathObj.asAndroidPath(), paint)
                                                            // Core
                                                            paint.setShadowLayer(0f,0f,0f,0)
                                                            paint.color = android.graphics.Color.WHITE
                                                            paint.strokeWidth = effectiveStrokeWidth / 3f
                                                            canvas.nativeCanvas.drawPath(pathObj.asAndroidPath(), paint)
                                                        }
                                                    } else {
                                                        val isEraser = path.isEraser
                                                        val isHighlighter = path.isHighlighter
                                                        
                                                        val strokeColor = when {
                                                            isHighlighter -> Color(path.color)
                                                            isEraser -> Color.White.copy(alpha = 0.5f) 
                                                            else -> Color(path.color)
                                                        }
                                                        
                                                        val strokeCap = if (isHighlighter) androidx.compose.ui.graphics.StrokeCap.Square else androidx.compose.ui.graphics.StrokeCap.Round
                                                        val blendMode = if (isEraser) androidx.compose.ui.graphics.BlendMode.Clear else if (isHighlighter) androidx.compose.ui.graphics.BlendMode.Multiply else androidx.compose.ui.graphics.BlendMode.SrcOver
                                                        
                                                        // Layered Transparency for Eraser logic (using Clear BlendMode on a layer if needed, 
                                                        // but here we draw directly. If Clear, it clears to Transparent, revealing Checkerboard behind. Correct.)
                                                        
                                                        drawPath(
                                                            path = pathObj,
                                                            color = strokeColor,
                                                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                                                width = effectiveStrokeWidth,
                                                                cap = strokeCap,
                                                                join = androidx.compose.ui.graphics.StrokeJoin.Round
                                                            ),
                                                            blendMode = blendMode
                                                        )
                                                    }
                                                }
                                                is com.moshitech.workmate.feature.imagestudio.viewmodel.DrawAction.Shape -> {
                                                     // Shape logic. SImplified coordinate mapping.
                                                     val shape = action.shape
                                                         val color = Color(shape.color)
                                                         val strokeWidth = shape.strokeWidth
                                                         val stroke = androidx.compose.ui.graphics.drawscope.Stroke(
                                                             width = strokeWidth,
                                                             cap = androidx.compose.ui.graphics.StrokeCap.Round
                                                         )
                                                         val fill = androidx.compose.ui.graphics.drawscope.Fill
                                                         
                                                         when (shape) {
                                                             is com.moshitech.workmate.feature.imagestudio.viewmodel.Shape.Line -> {
                                                                 drawLine(color, shape.start, shape.end, strokeWidth = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                                                             }
                                                             is com.moshitech.workmate.feature.imagestudio.viewmodel.Shape.Rectangle -> {
                                                                 drawRect(color, topLeft = shape.topLeft, size = shape.size, style = if(shape.filled) fill else stroke)
                                                             }
                                                             is com.moshitech.workmate.feature.imagestudio.viewmodel.Shape.Circle -> {
                                                                 drawCircle(color, center = shape.center, radius = shape.radius, style = if(shape.filled) fill else stroke)
                                                             }
                                                         }
                                                }
                                            }
                                        }

                                        // Live Preview of Current Path (Duplicate logic)
                                        if (currentDrawPath != null && isDrawMode) {
                                            // ... (Same logic: use path directly, no transforms) ...
                                            val pathObj = androidx.compose.ui.graphics.Path().apply {
                                                val start = currentDrawPath!!.first()
                                                moveTo(start.x, start.y)
                                                currentDrawPath!!.drop(1).forEach { pt -> lineTo(pt.x, pt.y) }
                                            }
                                            // Preview styling...
                                            val isEraser = uiState.selectedDrawTool == com.moshitech.workmate.feature.imagestudio.viewmodel.DrawTool.ERASER
                                            val isHighlighter = uiState.selectedDrawTool == com.moshitech.workmate.feature.imagestudio.viewmodel.DrawTool.HIGHLIGHTER
                                            
                                            val strokeColor = when {
                                                isHighlighter -> Color(uiState.currentDrawColor)
                                                isEraser -> Color.White.copy(alpha = 0.5f)
                                                else -> Color(uiState.currentDrawColor)
                                            }
                                            val strokeCap = if (isHighlighter) androidx.compose.ui.graphics.StrokeCap.Square else androidx.compose.ui.graphics.StrokeCap.Round
                                            
                                            drawPath(
                                                path = pathObj,
                                                color = strokeColor,
                                                style = androidx.compose.ui.graphics.drawscope.Stroke(
                                                    width = uiState.currentStrokeWidth / bitScale, // Preview uses unscaled
                                                    cap = strokeCap,
                                                    join = androidx.compose.ui.graphics.StrokeJoin.Round
                                                )
                                            )
                                            
                                            if (isEraser) {
                                                // Eraser Cursor
                                                drawPath(
                                                    path = pathObj,
                                                    color = Color.Black.copy(alpha = 0.2f),
                                                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                                                        width = 2.dp.toPx() / bitScale, // Scale outline too!
                                                        cap = strokeCap,
                                                        join = androidx.compose.ui.graphics.StrokeJoin.Round
                                                    )
                                                )
                                            }
                                        }
                                    }

                                    // 4. Overlays
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
                                                onTransformEnd = { viewModel.saveToHistory() }, 
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
                                               onTransformEnd = { viewModel.saveToHistory() },
                                               onDelete = { viewModel.removeSticker(it) },
                                               onFlip = { viewModel.flipSticker(it) } 
                                           )
                                       }
                                    }
                                }
                            }
                        } else {
                            // Empty Image State - Clickable Placeholder to add image
                            Box(
                                modifier = Modifier
                                        .fillMaxSize()
                                        .padding(32.dp)
                                        .background(Color(0xFF1E293B), RoundedCornerShape(16.dp)) // Secondary Dark
                                        .border(2.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
                                        .clickable { 
                                            galleryLauncher.launch(androidx.activity.result.PickVisualMediaRequest(androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly)) 
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(
                                            modifier = Modifier
                                                .size(80.dp)
                                                .background(Color(0xFF334155), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.Add,
                                                contentDescription = "Add Image",
                                                tint = Color.White,
                                                modifier = Modifier.size(40.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            "Select an Image to Edit",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Color.White,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            "Tap to browse gallery",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.Gray
                                        )
                                    }
                                }
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
                 
                        // Layers moved to ContentBox


                        // ADD NEW TEXT FAB (Top-Right Corner, below header)
                        if (currentTab == EditorTab.TEXT) {
                            FloatingActionButton(
                                onClick = { viewModel.createTextBoxAtCenter() },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 68.dp, end = 12.dp)
                                    .size(48.dp)
                                    .graphicsLayer {
                                        shadowElevation = 12f
                                        shape = CircleShape
                                        clip = true
                                    },
                                containerColor = Color(0xFF007AFF),
                                contentColor = Color.White
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Add Text",
                                    modifier = Modifier.size(24.dp),
                                    tint = Color.White
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
                                // Show toolbar if there are any text layers, not just when one is selected
                                val layerToEdit = if (uiState.selectedTextLayerId != null) {
                                    uiState.textLayers.find { it.id == uiState.selectedTextLayerId }
                                } else {
                                    uiState.textLayers.lastOrNull()
                                }
                                
                                if (layerToEdit != null) {
                                    Box(Modifier.fillMaxSize()) {
                                        com.moshitech.workmate.feature.imagestudio.components.TextEditorToolbar(
                                            layer = layerToEdit,
                                            visible = true,
                                            onUpdate = { updated, save -> viewModel.updateTextProperty(layerToEdit.id, save) { updated } },
                                            onSave = { viewModel.saveToHistory() },
                                            onRequestEyedropper = { callback -> eyedropperCallback = callback },
                                            onRequestTexturePick = { texturePickerLauncher.launch("image/*") },
                                            modifier = Modifier.fillMaxSize()
                                        )
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
        
        // Exit Confirmation Dialog
        if (showExitConfirmDialog) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showExitConfirmDialog = false },
                title = { Text("Unsaved Changes") },
                text = { Text("You have unsaved changes. Do you want to save before exiting?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showExitConfirmDialog = false
                            viewModel.showSaveDialog()
                        }
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(
                            onClick = {
                                showExitConfirmDialog = false
                                navController.popBackStack()
                            }
                        ) {
                            Text("Discard")
                        }
                        TextButton(
                            onClick = { showExitConfirmDialog = false }
                        ) {
                            Text("Cancel")
                        }
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
        var showResetDialog by remember { mutableStateOf(false) }

        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                title = { Text("Reset Adjustments?") },
                text = { Text("This will reset all brightness, contrast, and other adjustments to default values.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onReset()
                            showResetDialog = false
                        }
                    ) { Text("Reset", color = androidx.compose.material3.MaterialTheme.colorScheme.error) }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDialog = false }) { Text("Cancel") }
                }
            )
        }

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
            
            TextButton(onClick = { showResetDialog = true }) {
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

        CompactModernSlider(
            label = "Brightness",
            value = brightness,
            onValueChange = onBrightnessChange,
            valueRange = -1f..1f,
            unit = "%"
        )

        CompactModernSlider(
            label = "Contrast",
            value = contrast * 100f,
            onValueChange = { onContrastChange(it / 100f) },
            valueRange = 0f..200f,
            unit = "%"
        )

        CompactModernSlider(
            label = "Saturation",
            value = saturation * 100f,
            onValueChange = { onSaturationChange(it / 100f) },
            valueRange = 0f..200f,
            unit = "%"
        )

        CompactModernSlider(
            label = "Hue",
            value = hue,
            onValueChange = onHueChange,
            valueRange = -180f..180f,
            unit = "°"
        )
        
        CompactModernSlider(
            label = "Temp",
            value = temperature * 100f,
            onValueChange = { onTemperatureChange(it / 100f) },
            valueRange = -100f..100f,
            unit = "%"
        )
        
        CompactModernSlider(
            label = "Tint",
            value = tint * 100f,
            onValueChange = { onTintChange(it / 100f) },
            valueRange = -100f..100f,
            unit = "%"
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
        val animatedValue by animateFloatAsState(targetValue = value, label = "SliderAnimation")
        
        Slider(
            value = animatedValue,
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

