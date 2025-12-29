package com.moshitech.workmate.feature.imagestudio.ui

import android.graphics.Bitmap
import kotlinx.coroutines.launch
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Brush
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Add
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import com.moshitech.workmate.feature.imagestudio.components.DrawAndShapesToolbar
import com.moshitech.workmate.feature.imagestudio.viewmodel.ShapeType
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.RotateLeft
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ArrowDownward
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
import androidx.compose.material3.TabRow
import androidx.compose.material3.Tab
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.key
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo

import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.runtime.key
import androidx.compose.ui.geometry.Offset
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Size
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.moshitech.workmate.feature.imagestudio.components.AdContainer
import com.moshitech.workmate.feature.imagestudio.components.TextEditorBottomToolbar
import com.moshitech.workmate.feature.imagestudio.components.TextEditorToolbar
import com.moshitech.workmate.feature.imagestudio.components.CompactModernSlider
import com.moshitech.workmate.feature.imagestudio.viewmodel.PhotoEditorViewModel
import com.moshitech.workmate.feature.imagestudio.viewmodel.DrawAction
import com.moshitech.workmate.feature.imagestudio.viewmodel.EditorTab
import kotlin.math.roundToInt




@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun PhotoEditorScreen(
    navController: NavController,
    imageUri: Uri?,
    viewModel: PhotoEditorViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val activeTool = uiState.activeTool
    var showOriginal by remember { mutableStateOf(false) }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var showExitConfirmDialog by remember { mutableStateOf(false) }
    var showLayerPanel by remember { mutableStateOf(false) }
    var currentBitScale by remember { mutableStateOf(1f) } // Track UI scale for consistent saving
    val snackbarHostState = remember { SnackbarHostState() }
    val context = androidx.compose.ui.platform.LocalContext.current

    // Eyedropper State (Lifted for Toolbar access)
    var eyedropperCallback by remember { mutableStateOf<((Color) -> Unit)?>(null) }
    val isEyedropperActive = eyedropperCallback != null
    
    // Deselect layers when switching tabs/tools
    LaunchedEffect(activeTool) {
        when (activeTool) {
            com.moshitech.workmate.feature.imagestudio.viewmodel.EditorTab.TEXT -> {
                // Deselect shapes and stickers when on Text tab
                viewModel.deselectShape()
                viewModel.deselectSticker()
            }
            com.moshitech.workmate.feature.imagestudio.viewmodel.EditorTab.DRAW -> {
                // Deselect text and stickers when on Draw tab
                viewModel.deselectText()
                viewModel.deselectSticker()
            }
            else -> {
                // Deselect all layers when on other tabs (Adjust, Filters, etc.)
                // Or when exiting tool (activeTool == null)
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
    // Back Press Handler - Show confirmation if there are unsaved changes
    androidx.activity.compose.BackHandler(enabled = true) {
        if (activeTool != null) {
            viewModel.cancelTool()
        } else {
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
         // currentTab = EditorTab.TEXT  <-- Deprecated with Modal Logic. 
         // If we stay in NONE (activeTool == null), user sees main menu. Correct.
     }

    AdContainer(modifier = Modifier.fillMaxSize()) {
        // Root Layout (Manual Vertical Stack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF121212))
        ) {

            // 1. TOP BAR (Fixed)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0D121F)) // Deep dark blue/black
                    .statusBarsPadding()
                    .height(56.dp)
                    .padding(horizontal = 8.dp)
            ) {
                if (activeTool != null) {
                    // TOOL MODE TOP BAR
                    // Left: Cancel
                    Row(modifier = Modifier.align(Alignment.CenterStart)) {
                        IconButton(onClick = { viewModel.cancelTool() }) {
                            Icon(Icons.Default.Close, "Cancel", tint = Color.White)
                        }
                        IconButton(onClick = { viewModel.undo() }, enabled = uiState.canUndo) {
                            Icon(Icons.Default.Undo, "Undo", tint = if (uiState.canUndo) Color.White else Color.Gray)
                        }
                        IconButton(onClick = { viewModel.redo() }, enabled = uiState.canRedo) {
                            Icon(Icons.AutoMirrored.Filled.Redo, "Redo", tint = if (uiState.canRedo) Color.White else Color.Gray)
                        }
                    }
                    
                    // Center: Title
                    Text(
                        text = activeTool.name.replace("_", " ").toLowerCase().capitalize(java.util.Locale.ROOT),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                    
                    // Right: Apply
                    // Right: Download + Apply
                    Row(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.showSaveDialog() },
                            enabled = !uiState.isLoading && !uiState.isSaving
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                            } else {
                                Icon(
                                    imageVector = Icons.Default.ArrowDownward,
                                    contentDescription = "Save",
                                    tint = Color(0xFF007AFF) // iOS Blue
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        IconButton(
                            onClick = { viewModel.applyTool() }
                        ) {
                            Icon(Icons.Default.Check, "Apply", tint = Color(0xFF007AFF))
                        }
                    }
                } else {
                    // STANDARD TOP BAR
                    // Left: Close
                    IconButton(
                        onClick = { 
                             // Show exit dialog if changes exist? Checked in BackHandler logic
                             if (uiState.textLayers.isNotEmpty()) showExitConfirmDialog = true else navController.popBackStack()
                        },
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
                                imageVector = Icons.AutoMirrored.Filled.Redo,
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
                        Spacer(modifier = Modifier.width(16.dp))
                        // Layers button
                        BadgedBox(
                            badge = {
                                if (viewModel.getAllLayers().isNotEmpty()) {
                                    Badge { Text("${viewModel.getAllLayers().size}") }
                                }
                            }
                        ) {
                            IconButton(
                                onClick = { showLayerPanel = !showLayerPanel }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Layers,
                                    contentDescription = "Layers",
                                    tint = if (showLayerPanel) Color(0xFF3B82F6) else Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
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
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = "Save",
                                tint = Color(0xFF007AFF) // iOS Blue
                            )
                        }
                    }
                }
            }

            // 2. CANVAS AREA (Weighted, Middle, Clipped)
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f) // Fills all remaining vertical space
                    .fillMaxWidth()
                    .background(Color(0xFF0D121F))
                    .clipToBounds() // Strictly prevents image overflow
            ) {
                val boxWidth = constraints.maxWidth.toFloat()
                val boxHeight = constraints.maxHeight.toFloat()
                
                // Calculate Image Fit Params using Preview if available (handles rotation dimensions)
                val activeBitmap = uiState.previewBitmap ?: uiState.originalBitmap
                var bitScale = 1f
                var finalDisplayWidth = 0f
                var finalDisplayHeight = 0f
                val density = LocalDensity.current.density
                
                if (activeBitmap != null && activeBitmap.width > 0) {
                    val bmpW = activeBitmap.width.toFloat()
                    val bmpH = activeBitmap.height.toFloat()
                    val scaleToFit = minOf(boxWidth / bmpW, boxHeight / bmpH)
                    finalDisplayWidth = bmpW * scaleToFit
                    finalDisplayHeight = bmpH * scaleToFit
                    bitScale = scaleToFit
                    
                    // Update local state if needed (removed VM call)
                    LaunchedEffect(bitScale) {
                        /* if (currentBitScale != bitScale) {
                            currentBitScale = bitScale
                        } */
                    }
                }

                // CHECKERBOARD BACKGROUND (Integrated)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawBehind {
                            val checkerSize = 20f
                            val rows = (size.height / checkerSize).toInt() + 1
                            val cols = (size.width / checkerSize).toInt() + 1

                            for (row in 0 until rows) {
                                for (col in 0 until cols) {
                                    val color = if ((row + col) % 2 == 0) Color(0xFF202020) else Color(0xFF303030)
                                    drawRect(
                                        color = color,
                                        topLeft = Offset(col * checkerSize, row * checkerSize),
                                        size = Size(checkerSize, checkerSize)
                                    )
                                }
                            }
                        }
                )

                // IMAGE AND LAYERS
                val scope = rememberCoroutineScope()
                val offsetX = remember { androidx.compose.animation.core.Animatable(0f) }
                val offsetY = remember { androidx.compose.animation.core.Animatable(0f) }
                val scale = remember { androidx.compose.animation.core.Animatable(1f) }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        // DESELECTION TAP
                        .clickable(
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null
                        ) {
                             viewModel.deselectShapeLayer()
                             viewModel.deselectSticker()
                             viewModel.deselectText()
                        }
                        // ZOOM/PAN GESTURES
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                val newScale = (scale.value * zoom).coerceIn(0.5f, 5f)
                                scope.launch { scale.snapTo(newScale) }
                                
                                val newOffset = if (scale.value > 1f) {
                                    val maxX = (boxWidth * scale.value - boxWidth) / 2
                                    val maxY = (boxHeight * scale.value - boxHeight) / 2
                                    Offset(
                                        x = (offsetX.value + pan.x * scale.value).coerceIn(-maxX, maxX),
                                        y = (offsetY.value + pan.y * scale.value).coerceIn(-maxY, maxY)
                                    )
                                } else {
                                    Offset(
                                        x = offsetX.value + pan.x,
                                        y = offsetY.value + pan.y
                                    )
                                }
                                scope.launch { offsetX.snapTo(newOffset.x) }
                                scope.launch { offsetY.snapTo(newOffset.y) }
                            }
                        }
                        .graphicsLayer(
                            scaleX = scale.value * (if (uiState.flipX) -1f else 1f),
                            scaleY = scale.value * (if (uiState.flipY) -1f else 1f),
                            rotationZ = uiState.rotationAngle,
                            translationX = offsetX.value,
                            translationY = offsetY.value
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // MAIN IMAGE
                    if (activeBitmap != null) {
                        Image(
                            bitmap = activeBitmap.asImageBitmap(),
                            contentDescription = "Editor Image",
                            modifier = Modifier.requiredSize(
                                width = (finalDisplayWidth / density).dp,
                                height = (finalDisplayHeight / density).dp
                            ),
                            contentScale = ContentScale.FillBounds
                        )
                        val densityUnused = LocalDensity.current
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .aspectRatio(0.8f)
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(Color(0xFF1E1E1E), Color(0xFF252525))
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    brush = Brush.linearGradient(
                                        colors = listOf(Color(0xFF404040), Color(0xFF202020))
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = Color(0x33FFFFFF),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable { showImageSourceDialog = true },
                             contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .background(
                                            brush = Brush.linearGradient(
                                                colors = listOf(Color(0xFF2C2C2E), Color(0xFF1C1C1E))
                                            ),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AddPhotoAlternate,
                                        contentDescription = "Add Image",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Select an Image to Edit",
                                    color = Color(0xFFEEEEEE),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // LAYERS (Text, Stickers, Shapes)
                    // Render layers on top of image, responding to same transforms
                    // Need to verify layer rendering logic - assuming Standard Layer Rendering Here
                    // Based on previous code, Layers were rendered in a Box.
                    
                    // Calculate correct offset for Center-aligned parent
                    val centeredOffset = Offset(-finalDisplayWidth / 2f, -finalDisplayHeight / 2f)

                    // Sticker Layers
                    uiState.stickerLayers.forEach { layer ->
                        if (layer.isVisible) {
                            key(layer.id) {
                                com.moshitech.workmate.feature.imagestudio.components.StickerBoxComposable(
                                    layer = layer,
                                    isSelected = uiState.selectedStickerLayerId == layer.id,
                                    bitmapScale = bitScale,
                                    bitmapOffset = centeredOffset,
                                    onSelect = { viewModel.selectSticker(it) },
                                    onTransform = { id, offset, scale, rotation ->
                                        viewModel.updateStickerTransform(id, offset, scale, rotation)
                                    },
                                    onTransformEnd = { viewModel.saveToHistory() },
                                    onDelete = { viewModel.removeSticker(it) },
                                    onFlip = { viewModel.flipSticker(it) }
                                )
                            }
                        }
                    }

                     // Text Layers
                    uiState.textLayers.forEach { layer ->
                        if (layer.isVisible) {
                            key(layer.id) {
                                com.moshitech.workmate.feature.imagestudio.components.TextBoxComposable(
                                    layer = layer,
                                    isSelected = uiState.selectedTextLayerId == layer.id,
                                    isEditing = uiState.editingTextLayerId == layer.id,
                                    bitmapScale = bitScale,
                                    bitmapOffset = centeredOffset,
                                    onSelect = { viewModel.selectTextLayer(it) },
                                    onEdit = { viewModel.enterTextEditMode(it) },
                                    onTransform = { id, offset, scale, rotation -> 
                                        viewModel.updateTextLayerTransform(id, offset, scale, rotation) 
                                    },
                                    onTransformEnd = { viewModel.saveToHistory() },
                                    onTextChange = { id, txt -> viewModel.updateTextInline(id, txt) },
                                    onWidthChange = { id, w -> viewModel.updateTextLayerWidth(id, w) },
                                    onDuplicate = { viewModel.duplicateTextLayer(it) },
                                    onDelete = { viewModel.removeTextLayer(it) }
                                )
                            }
                        }
                    }
                    
                    // Shape Layers and Draw Canvas (DrawCanvas missing from components, commenting out)
                    // Shape Layers
                    uiState.shapeLayers.forEach { layer ->
                        if (layer.isVisible) {
                            key(layer.id) {
                                com.moshitech.workmate.feature.imagestudio.components.ShapeBoxComposable(
                                    layer = layer,
                                    isSelected = uiState.selectedShapeLayerId == layer.id,
                                    bitmapScale = bitScale,
                                    bitmapOffset = centeredOffset,
                                    onSelect = { viewModel.selectShapeLayer(it) },
                                    onTransform = { id, offset, scale, rotation ->
                                        viewModel.updateShapeTransform(id, offset, scale, rotation)
                                    },
                                    onResize = { id, w, h, dx, dy ->
                                        viewModel.updateShapeSize(id, w, h, dx, dy)
                                    },
                                    onTransformEnd = { viewModel.saveToHistory() },
                                    onDelete = { viewModel.deleteShapeLayer(it) }
                                )
                            }
                        }
                    }
                    
                    if (activeTool == EditorTab.DRAW || activeTool == EditorTab.SHAPES) {
                        /*
                        // DrawCanvas Missing
                        com.moshitech.workmate.feature.imagestudio.components.DrawCanvas(
                             modifier = Modifier.fillMaxSize(),
                             viewModel = viewModel,
                             imageBitmap = uiState.originalBitmap
                        )
                        */
                    }

                } // End Transformable Box
                
                // Snap-Back Animation triggers
                LaunchedEffect(scale.isRunning) {
                    if (!scale.isRunning && scale.value < 1f) {
                         launch { scale.animateTo(1f, spring(stiffness = Spring.StiffnessLow)) }
                         launch { offsetX.animateTo(0f, spring(stiffness = Spring.StiffnessLow)) }
                         launch { offsetY.animateTo(0f, spring(stiffness = Spring.StiffnessLow)) }
                    }
                }
                
                // Add Text FAB
                if (activeTool == EditorTab.TEXT) {
                     FloatingActionButton(
                        onClick = { viewModel.createTextBoxAtCenter() },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 16.dp, end = 16.dp)
                            .size(48.dp),
                        containerColor = Color(0xFF007AFF),
                        contentColor = Color.White
                    ) {
                        Icon(Icons.Default.Add, "Add Text")
                    }
                }

            } // END CANVAS BOX

            // 3. TOOLBAR PANEL (Dynamic Height)
            // Show only if activeTool is not null
            if (activeTool != null) {
                val isLargePanel = activeTool == com.moshitech.workmate.feature.imagestudio.viewmodel.EditorTab.STICKERS || 
                                  activeTool == com.moshitech.workmate.feature.imagestudio.viewmodel.EditorTab.TEXT || 
                                  activeTool == com.moshitech.workmate.feature.imagestudio.viewmodel.EditorTab.SHAPES
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0D121F))
                        .then(
                            if (isLargePanel) {
                                Modifier.height(300.dp)
                            } else {
                                Modifier.wrapContentHeight().heightIn(max = 320.dp)
                            }
                        )
                ) {
                     Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0D121F))
                     ) {
                         AnimatedContent(
                                targetState = activeTool,
                                transitionSpec = {
                                    (slideInVertically { height -> height } + fadeIn() togetherWith
                                            slideOutVertically { height -> height } + fadeOut())
                                        .using(SizeTransform(clip = false))
                                },
                                label = "ToolbarTransition"
                         ) { targetTab ->
                          when (targetTab) {
                                EditorTab.CROP -> {} // Handled elsewhere
                                EditorTab.ADJUST -> {
                                    AdjustTab(
                                        brightness = uiState.brightness,
                                        contrast = uiState.contrast,
                                        saturation = uiState.saturation,
                                        hue = uiState.hue,
                                        temperature = uiState.temperature,
                                        tint = uiState.tint,
                                        onBrightnessChange = { viewModel.updateBrightness(it) },
                                        onContrastChange = { viewModel.updateContrast(it) },
                                        onSaturationChange = { viewModel.updateSaturation(it) },
                                        onHueChange = { viewModel.setHue(it) },
                                        onTemperatureChange = { viewModel.setTemperature(it) },
                                        onTintChange = { viewModel.setTint(it) },

                                        onReset = { viewModel.resetAdjustments() },
                                        onSaveHistory = { viewModel.saveToHistory() }
                                    )
                                }
                                EditorTab.FILTERS -> {
                                    com.moshitech.workmate.feature.imagestudio.components.FiltersTab(
                                        activeFilterId = uiState.activeFilterId,
                                        previewBitmap = uiState.filterPreviewBitmap ?: uiState.originalBitmap,
                                        onFilterSelected = { filterId, matrix -> viewModel.applyFilter(filterId, matrix) },
                                        onClearFilter = { viewModel.clearFilter() }
                                    )
                                }
                                EditorTab.STICKERS -> {
                                    com.moshitech.workmate.feature.imagestudio.components.StickersTab(
                                        onStickerSelected = { emoji -> viewModel.addSticker(text = emoji) }
                                    )
                                }
                                EditorTab.TEXT -> {
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
                                    com.moshitech.workmate.feature.imagestudio.components.ShapesToolbar(uiState, viewModel)
                                }
                                EditorTab.DRAW -> {
                                    com.moshitech.workmate.feature.imagestudio.components.DrawAndShapesToolbar(uiState, viewModel)
                                }
                                 EditorTab.ROTATE -> {
                                     Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 24.dp), // Replaced fillMaxSize with wrapContent implied by default column behavior or just fillMaxWidth
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(16.dp) // Reduced spacing from 24.dp
                                     ) {
                                         // 1. Icon Row (Circular Buttons)
                                         Row(
                                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                         ) {
                                             val buttonModifier = Modifier
                                                 .size(42.dp)
                                                 .background(Color(0xFF2C2C2E), CircleShape)
                                                 .clip(CircleShape)

                                             // Turn Left
                                             Box(modifier = buttonModifier.clickable { viewModel.rotate90CCW() }, contentAlignment = Alignment.Center) {
                                                 Icon(Icons.Default.RotateLeft, "Rotate Left", tint = Color.White)
                                             }
                                             Spacer(Modifier.width(12.dp))

                                             // Turn Right
                                             Box(modifier = buttonModifier.clickable { viewModel.rotate90CW() }, contentAlignment = Alignment.Center) {
                                                 Icon(Icons.Default.RotateRight, "Rotate Right", tint = Color.White)
                                             }
                                             Spacer(Modifier.width(12.dp))

                                             // Flip Horizontal
                                             Box(modifier = buttonModifier.clickable { viewModel.flipHorizontal() }, contentAlignment = Alignment.Center) {
                                                 Icon(Icons.Default.SwapHoriz, "Flip Horizontal", tint = Color.White)
                                             }
                                             Spacer(Modifier.width(12.dp))

                                             // Flip Vertical
                                             Box(modifier = buttonModifier.clickable { viewModel.flipVertical() }, contentAlignment = Alignment.Center) {
                                                 Icon(Icons.Default.SwapHoriz, "Flip Vertical", tint = Color.White, modifier = Modifier.graphicsLayer(rotationZ = 90f))
                                             }
                                             Spacer(Modifier.width(12.dp))
                                             
                                             // Reset
                                             Box(modifier = buttonModifier.clickable { 
                                                 viewModel.resetRotationChanges() 
                                                 // Don't save history here as 'pills' usually restore state. 
                                                 // If user wants to 'Apply' this reset, they click check.
                                             }, contentAlignment = Alignment.Center) {
                                                 Icon(Icons.Default.Restore, "Reset", tint = Color.White, modifier = Modifier.size(20.dp))
                                             }
                                         }

                                         // 2. Slider Controls
                                         Column(
                                             modifier = Modifier.fillMaxWidth(),
                                             horizontalAlignment = Alignment.CenterHorizontally
                                         ) {
                                              com.moshitech.workmate.feature.imagestudio.components.CompactModernSlider(
                                                value = uiState.rotationAngle,
                                                onValueChange = { viewModel.setRotationAngle(it) },
                                                valueRange = -45f..45f,
                                                onValueChangeFinished = { viewModel.saveToHistory() },
                                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                                            )
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                text = "${uiState.rotationAngle.toInt()}°",
                                                color = Color.Gray,
                                                fontSize = 12.sp
                                            )
                                         }
                                     }
                                 }
                                else -> {}
                          }
                     }
                 }
            } // End Box & Tool Panel Column (Original brace was Column)
            } // End if(activeTool != null)

            // 4. BOTTOM NAVIGATION (Fixed)

            if (activeTool == null) {
                // MAIN TOOLBAR (Bottom)
                com.moshitech.workmate.feature.imagestudio.components.PhotoEditorBottomNav(
                    selectedTool = com.moshitech.workmate.feature.imagestudio.components.EditorTool.NONE,
                    onToolSelected = { tool ->
                         // Map EditorTool to EditorTab
                         val targetTab = when (tool) {
                             com.moshitech.workmate.feature.imagestudio.components.EditorTool.CROP -> com.moshitech.workmate.feature.imagestudio.viewmodel.EditorTab.CROP
                             com.moshitech.workmate.feature.imagestudio.components.EditorTool.FILTERS -> com.moshitech.workmate.feature.imagestudio.viewmodel.EditorTab.FILTERS
                             com.moshitech.workmate.feature.imagestudio.components.EditorTool.ADJUST -> com.moshitech.workmate.feature.imagestudio.viewmodel.EditorTab.ADJUST
                             com.moshitech.workmate.feature.imagestudio.components.EditorTool.STICKERS -> com.moshitech.workmate.feature.imagestudio.viewmodel.EditorTab.STICKERS 
                             com.moshitech.workmate.feature.imagestudio.components.EditorTool.TEXT -> com.moshitech.workmate.feature.imagestudio.viewmodel.EditorTab.TEXT
                             // OVERLAY not defined? Check PhotoEditorBottomNav. Assuming it exists or mapping to NONE
                             // com.moshitech.workmate.feature.imagestudio.components.EditorTool.OVERLAY -> com.moshitech.workmate.feature.imagestudio.viewmodel.EditorTab.OVERLAY
                             com.moshitech.workmate.feature.imagestudio.components.EditorTool.SHAPES -> com.moshitech.workmate.feature.imagestudio.viewmodel.EditorTab.SHAPES
                             com.moshitech.workmate.feature.imagestudio.components.EditorTool.DRAW -> com.moshitech.workmate.feature.imagestudio.viewmodel.EditorTab.DRAW 
                             else -> com.moshitech.workmate.feature.imagestudio.viewmodel.EditorTab.NONE
                         }

                         if (targetTab != com.moshitech.workmate.feature.imagestudio.viewmodel.EditorTab.NONE) {
                            if (targetTab == com.moshitech.workmate.feature.imagestudio.viewmodel.EditorTab.CROP) {
                                if (imageUri != null) {
                                    val options = com.canhub.cropper.CropImageContractOptions(uri = imageUri, cropImageOptions = com.canhub.cropper.CropImageOptions())
                                    cropImageLauncher.launch(options)
                                }
                            } else if (targetTab == com.moshitech.workmate.feature.imagestudio.viewmodel.EditorTab.TEXT) {
                                viewModel.addTextLayer("Text")
                                viewModel.enterTool(targetTab) // Also enter the tool to show the text editor
                            } else if (targetTab == com.moshitech.workmate.feature.imagestudio.viewmodel.EditorTab.STICKERS) {
                                showLayerPanel = false
                                viewModel.enterTool(targetTab)
                            } else if (targetTab == com.moshitech.workmate.feature.imagestudio.viewmodel.EditorTab.SHAPES) {
                                viewModel.setDrawMode(com.moshitech.workmate.feature.imagestudio.viewmodel.DrawMode.SHAPES)
                                showLayerPanel = false
                                if (uiState.selectedShapeLayerId == null) {
                                    viewModel.addShapeLayer(com.moshitech.workmate.feature.imagestudio.viewmodel.ShapeType.CIRCLE)
                                }
                                viewModel.enterTool(targetTab)
                            } else if (targetTab == com.moshitech.workmate.feature.imagestudio.viewmodel.EditorTab.DRAW) {
                                viewModel.setDrawMode(com.moshitech.workmate.feature.imagestudio.viewmodel.DrawMode.PAINT)
                                showLayerPanel = false
                                viewModel.enterTool(targetTab)
                            } else {
                                viewModel.enterTool(targetTab)
                            }
                         }
                    },
                    modifier = Modifier.navigationBarsPadding() // Handle bottom inset
                )
            }
            }
        
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
                            viewModel.saveImage(uiState.saveFilename, currentBitScale) {
                                // Stay in screen, do not pop
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
        
        // NEW: Layer Panel (slides in from right)
        androidx.compose.animation.AnimatedVisibility(
            visible = showLayerPanel,
            enter = androidx.compose.animation.slideInHorizontally(initialOffsetX = { it }),
            exit = androidx.compose.animation.slideOutHorizontally(targetOffsetX = { it })
        ) {
            // NEW: Background dimmer that dismisses panel when clicked
            if (showLayerPanel) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable { showLayerPanel = false } // Close when clicking outside
                )
            }

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.CenterEnd
            ) {
                // The Panel itself (clicks on panel should not close it)
                com.moshitech.workmate.feature.imagestudio.ui.components.LayerPanel(
                    layers = viewModel.getAllLayers(),
                    selectedLayerId = uiState.selectedTextLayerId ?: uiState.selectedStickerLayerId,
                    onLayerSelected = { layerId ->
                        // Find layer type and select it
                        val layer = viewModel.getAllLayers().find { it.id == layerId }
                        layer?.let {
                            when (it.type) {
                                com.moshitech.workmate.feature.imagestudio.data.LayerType.TEXT -> {
                                    viewModel.selectTextLayer(layerId)
                                }
                                // Sticker and Shape selection not implemented yet
                                else -> {}
                            }
                        }
                    },
                    onVisibilityToggle = { layerId ->
                        val layer = viewModel.getAllLayers().find { it.id == layerId }
                        layer?.let {
                            viewModel.toggleLayerVisibility(layerId, it.type)
                        }
                    },
                    onLayerReorder = { layerId, targetZIndex ->
                        // Find the target layer with this z-index
                        val targetLayer = viewModel.getAllLayers().find { it.zIndex == targetZIndex }
                        if (targetLayer != null && targetLayer.id != layerId) {
                            // Swap z-indices between dragged and target layers
                            viewModel.swapLayerZIndices(layerId, targetLayer.id)
                        } else {
                            // Fallback: just set the z-index directly
                            val layer = viewModel.getAllLayers().find { it.id == layerId }
                            layer?.let {
                                viewModel.updateLayerZIndex(layerId, it.type, targetZIndex)
                            }
                        }
                    },
                    onLayerRename = { layerId, newName ->
                        val layer = viewModel.getAllLayers().find { it.id == layerId }
                        layer?.let {
                            viewModel.renameLayer(layerId, it.type, newName)
                        }
                    },
                    onLayerDelete = { layerId ->
                        // Delete functionality not implemented yet
                        // TODO: Add delete functions to ViewModel
                    },
                    onClose = { showLayerPanel = false }, // Close button callback
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(320.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                        ) { 
                            // Consume clicks so they don't propagate to the background dismisser
                        }
                )
            }
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


    onReset: () -> Unit, // New Callback
    onSaveHistory: () -> Unit // Callback for slider release
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
            value = brightness * 100f,
            onValueChange = { onBrightnessChange(it / 100f) },
            onValueChangeFinished = onSaveHistory,
            valueRange = -100f..100f,
            unit = "%"
        )

        CompactModernSlider(
            label = "Contrast",
            value = contrast * 100f,
            onValueChange = { onContrastChange(it / 100f) },
            onValueChangeFinished = onSaveHistory,
            valueRange = 0f..200f,
            unit = "%"
        )

        CompactModernSlider(
            label = "Saturation",
            value = saturation * 100f,
            onValueChange = { onSaturationChange(it / 100f) },
            onValueChangeFinished = onSaveHistory,
            valueRange = 0f..200f,
            unit = "%"
        )

        CompactModernSlider(
            label = "Hue",
            value = hue,
            onValueChange = onHueChange,
            onValueChangeFinished = onSaveHistory,
            valueRange = -180f..180f,
            unit = "\u00B0"
        )
        
        CompactModernSlider(
            label = "Temp",
            value = temperature * 100f,
            onValueChange = { onTemperatureChange(it / 100f) },
            onValueChangeFinished = onSaveHistory,
            valueRange = -100f..100f,
            unit = "%"
        )
        
        CompactModernSlider(
            label = "Tint",
            value = tint * 100f,
            onValueChange = { onTintChange(it / 100f) },
            onValueChangeFinished = onSaveHistory,
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

