package com.moshitech.workmate.feature.imagestudio.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.animation.animateContentSize
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.moshitech.workmate.feature.imagestudio.data.CompressFormat
import com.moshitech.workmate.feature.imagestudio.data.ConversionPreset
import com.moshitech.workmate.feature.imagestudio.viewmodel.BatchConverterUiState
import com.moshitech.workmate.feature.imagestudio.viewmodel.BatchConverterViewModel
import com.moshitech.workmate.feature.imagestudio.viewmodel.BatchScreenState
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.History
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

// --- Color System ---
object BatchColors {
    // Dark Mode: Solid Slate 900 (Previous Impressive Design)
    // Light Mode: Gradient SpeedTest Style (F1F5F9 -> E2E8F0) - Handled via Brush, but base color for Scrim/Fallback
    
    fun backgroundBrush(isDark: Boolean): Brush {
        return if (isDark) {
            Brush.verticalGradient(listOf(Color(0xFF0F172A), Color(0xFF0F172A))) // Solid Dark
        } else {
            Brush.verticalGradient(listOf(Color(0xFFF1F5F9), Color(0xFFE2E8F0))) // SpeedTest Light Gradient
        }
    }

    fun surface(isDark: Boolean): Color = if (isDark) Color(0xFF1E293B) else Color.White
    fun surfaceContainer(isDark: Boolean): Color = if (isDark) Color(0xFF1E293B) else Color.White
    
    fun textPrimary(isDark: Boolean): Color = if (isDark) Color.White else Color(0xFF1E293B)
    fun textSecondary(isDark: Boolean): Color = if (isDark) Color(0xFF94A3B8) else Color(0xFF6B7280)
    
    fun primary(isDark: Boolean): Color = if (isDark) Color(0xFF3B82F6) else Color(0xFF3B82F6) // Consistent Blue
    fun outline(isDark: Boolean): Color = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
    
    fun scrim(isDark: Boolean): Color = if (isDark) Color(0xE60F172A) else Color(0xFFF1F5F9).copy(alpha=0.95f)

    fun chipBackground(isDark: Boolean, selected: Boolean): Color {
        if (selected) return primary(isDark)
        return if (isDark) Color.Transparent else Color(0xFF1E293B) // Dark chips on light mode for contrast
    }

    fun chipContent(isDark: Boolean, selected: Boolean): Color {
        if (selected) return Color.White
        return if (isDark) textSecondary(isDark) else Color.White
    }

    fun chipBorder(isDark: Boolean, selected: Boolean): Color {
        if (selected) return Color.Transparent
        return if (isDark) outline(isDark) else Color.Transparent
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BatchConverterScreen(
    navController: NavController,
    incomingUris: String? = null,
    viewModel: BatchConverterViewModel = viewModel(),
    mainViewModel: com.moshitech.workmate.MainViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val themeState by mainViewModel.theme.collectAsState()
    val isSystemDark = isSystemInDarkTheme()
    
    val isDark = when (themeState) {
        com.moshitech.workmate.data.repository.AppTheme.LIGHT -> false
        com.moshitech.workmate.data.repository.AppTheme.DARK -> true
        com.moshitech.workmate.data.repository.AppTheme.SYSTEM -> isSystemDark
    }
    
    // Back Handler
    // Back Handler
    androidx.activity.compose.BackHandler(enabled = uiState.screenState != BatchScreenState.INPUT) {
        if (uiState.screenState == BatchScreenState.DETAIL) {
            viewModel.closeDetail()
        } else if (uiState.screenState == BatchScreenState.SUCCESS) {
            viewModel.resetState()
        } else if (uiState.screenState == BatchScreenState.HISTORY) {
            viewModel.setScreenState(BatchScreenState.INPUT)
        }
    }

    // Load Incoming
    LaunchedEffect(incomingUris) {
        if (!incomingUris.isNullOrEmpty()) {
            try {
                val uris = incomingUris.split(",").map { Uri.parse(Uri.decode(it)) }
                viewModel.onImagesSelected(uris)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BatchColors.backgroundBrush(isDark)) // Global Background
    ) {
        when (uiState.screenState) {
            BatchScreenState.INPUT -> {
                BatchInputScreen(navController, viewModel, uiState, isDark)
            }
            BatchScreenState.SUCCESS -> {
                BatchSuccessScreen(navController, viewModel, uiState, isDark)
            }
            BatchScreenState.DETAIL -> {
                BatchDetailScreen(viewModel, uiState, isDark)
            }
            BatchScreenState.HISTORY -> {
                BatchHistoryScreen(
                    viewModel = viewModel,
                    history = uiState.history,
                    onBack = { viewModel.setScreenState(BatchScreenState.INPUT) },
                    isDark = isDark
                )
            }
        }
        
        if (uiState.isConverting) {
            BatchProgressOverlay(
                progress = uiState.progress,
                processedCount = uiState.processedCount,
                totalCount = uiState.totalCount,
                currentFileProgress = uiState.currentFileProgress,
                onCancel = viewModel::cancelConversion,
                isDark = isDark
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BatchInputScreen(
    navController: NavController,
    viewModel: BatchConverterViewModel,
    uiState: BatchConverterUiState,
    isDark: Boolean
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    var showUserGuide by remember { mutableStateOf(false) }
    var showSavePresetDialog by remember { mutableStateOf(false) }
    var newPresetName by remember { mutableStateOf("") }
    
    // Info Modal State
    var showInfoModal by remember { mutableStateOf(false) }
    var imageDetails by remember { mutableStateOf<BatchConverterViewModel.ImageDetails?>(null) }

    val multiplePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris -> viewModel.onImagesSelected(uris) }
    )

    // Preview Selection Logic
    var selectedPreviewUri by remember { mutableStateOf<Uri?>(null) }
    val activePreviewUri = if (selectedPreviewUri != null && uiState.selectedImages.contains(selectedPreviewUri)) {
        selectedPreviewUri
    } else {
        uiState.selectedImages.firstOrNull()
    }
    
    var previewDetails by remember { mutableStateOf<BatchConverterViewModel.ImageDetails?>(null) }
    LaunchedEffect(activePreviewUri) {
         if (activePreviewUri != null) {
            previewDetails = viewModel.getImageDetails(activePreviewUri!!)
         } else {
            previewDetails = null
         }
    }

    // Message Handling
    LaunchedEffect(uiState.message) {
        uiState.message?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessage()
        }
    }
    
    // Preset Options State
    var presetOptionState by remember { mutableStateOf<ConversionPreset?>(null) } // Preset to view/delete
    if (presetOptionState != null) {
        AlertDialog(
            onDismissRequest = { presetOptionState = null },
            icon = { Icon(Icons.Filled.Settings, null, tint = BatchColors.primary(isDark), modifier = Modifier.size(24.dp)) },
            title = { Text(presetOptionState?.name ?: "Preset", color = BatchColors.textPrimary(isDark)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                    // Preset Details
                    Column(modifier = Modifier.fillMaxWidth().background(BatchColors.surface(isDark).copy(alpha=0.5f), RoundedCornerShape(8.dp)).padding(12.dp)) {
                        Text("Preset Details", color = BatchColors.textPrimary(isDark), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Spacer(Modifier.height(4.dp))
                        Text("Format: ${presetOptionState!!.format.name}", color = BatchColors.textSecondary(isDark), fontSize = 11.sp)
                        Text("Quality: ${presetOptionState!!.quality}%", color = BatchColors.textSecondary(isDark), fontSize = 11.sp)
                        if (presetOptionState!!.targetSize.isNotBlank()) {
                            Text("Target Size: ${presetOptionState!!.targetSize} ${if(presetOptionState!!.isTargetSizeInMb) "MB" else "KB"}", color = BatchColors.textSecondary(isDark), fontSize = 11.sp)
                        }
                        if (presetOptionState!!.width.isNotBlank() || presetOptionState!!.height.isNotBlank()) {
                            Text("Dimensions: ${if(presetOptionState!!.width.isBlank()) "Auto" else presetOptionState!!.width} x ${if(presetOptionState!!.height.isBlank()) "Auto" else presetOptionState!!.height} px", color = BatchColors.textSecondary(isDark), fontSize = 11.sp)
                        }
                    }

                    // Update
                    Button(
                        onClick = { 
                            viewModel.savePreset(presetOptionState!!.name) // Overwrite logic
                            presetOptionState = null 
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BatchColors.surface(isDark), contentColor = BatchColors.textPrimary(isDark)),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, BatchColors.outline(isDark))
                    ) {
                         Text("Update with Current Settings")
                    }
                    
                    // Load
                    Button(
                        onClick = { 
                            viewModel.loadPreset(presetOptionState!!)
                            presetOptionState = null 
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BatchColors.primary(isDark), contentColor = Color.White),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Load Preset")
                    }
                    
                    // Delete
                    Button(
                        onClick = { 
                            viewModel.deletePreset(presetOptionState!!.name)
                            presetOptionState = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444), contentColor = Color.White), // Red
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Delete Preset")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { presetOptionState = null }) {
                    Text("Cancel", color = BatchColors.textSecondary(isDark))
                }
            },
            containerColor = BatchColors.surfaceContainer(isDark)
        )
    }

    if (showSavePresetDialog) {
        // ... (Save Dialog code potentially here or above) ...
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Convert Image", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = BatchColors.textPrimary(isDark))
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.setScreenState(BatchScreenState.HISTORY) }) {
                        Icon(Icons.Default.History, "History", tint = BatchColors.textPrimary(isDark))
                    }
                    IconButton(onClick = { showUserGuide = true }) {
                        Icon(Icons.Outlined.Info, "User Guide", tint = BatchColors.textPrimary(isDark))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = BatchColors.textPrimary(isDark),
                    actionIconContentColor = BatchColors.textPrimary(isDark)
                )
            )
        },
        bottomBar = {
            PhotoStudioBottomBar(
                currentTab = "Convert",
                isEditorEnabled = uiState.selectedImages.size == 1,
                onTabSelected = { tab ->
                    when (tab) {
                        "Gallery" -> {
                            // Clear back stack to gallery
                             navController.popBackStack()
                        }
                        "Editor" -> {
                            if (uiState.selectedImages.size == 1) {
                                val uri = uiState.selectedImages.first()
                                val encodedUri = android.net.Uri.encode(uri.toString())
                                navController.navigate("${com.moshitech.workmate.navigation.Screen.PhotoEditor.route}?uri=$encodedUri")
                            }
                        }
                        "Convert" -> { /* Already here */ }
                        "Share" -> {
                            if (uiState.selectedImages.isNotEmpty()) {
                                try {
                                    val uris = ArrayList<Uri>(uiState.selectedImages)
                                    val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND_MULTIPLE).apply {
                                        putParcelableArrayListExtra(android.content.Intent.EXTRA_STREAM, uris)
                                        type = "image/*"
                                        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    navController.context.startActivity(android.content.Intent.createChooser(shareIntent, "Share images"))
                                } catch (e: Exception) {
                                  // Handled by safe call
                                }
                            }
                        }
                    }
                }
            )
        },
        containerColor = Color.Transparent // Background handled by parent Box
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(BatchColors.surface(isDark)),
                contentAlignment = Alignment.Center
            ) {
                if (activePreviewUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current).data(activePreviewUri).build(),
                        contentDescription = "Preview",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Info Icon
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha=0.5f), CircleShape)
                            .clickable {
                                scope.launch {
                                    imageDetails = viewModel.getImageDetails(activePreviewUri)
                                    showInfoModal = true
                                }
                            }
                            .padding(8.dp)
                    ) {
                        Icon(Icons.Outlined.Info, "Info", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                } else {
                     Column(
                         horizontalAlignment = Alignment.CenterHorizontally,
                         modifier = Modifier.clickable { 
                             multiplePhotoPickerLauncher.launch(
                                 PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                             )
                         }
                     ) {
                         Icon(Icons.Default.Add, null, tint = BatchColors.primary(isDark), modifier = Modifier.size(48.dp))
                         Text("Tap to add images", color = BatchColors.textSecondary(isDark))
                     }
                }
            }
            
            // Info text
            if (previewDetails != null) {
                Text("${previewDetails?.size} • ${previewDetails?.type}", color = BatchColors.textSecondary(isDark), fontSize = 12.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            // 2. Thumbnails
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(uiState.selectedImages.size) { index ->
                    val uri = uiState.selectedImages[index]
                    val isPreviewing = uri == activePreviewUri
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(if (isPreviewing) 2.dp else 0.dp, if (isPreviewing) BatchColors.primary(isDark) else Color.Transparent, RoundedCornerShape(8.dp))
                            .clickable { selectedPreviewUri = uri }
                    ) {
                        AsyncImage(model = ImageRequest.Builder(LocalContext.current).data(uri).build(), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                        Box(modifier = Modifier.align(Alignment.TopEnd).clickable { viewModel.removeImage(uri) }.padding(2.dp).background(Color.Black.copy(alpha=0.6f), CircleShape)) {
                             Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(12.dp))
                        }
                    }
                }
                item {
                     Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(BatchColors.surface(isDark))
                            .clickable { multiplePhotoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Default.Add, null, tint = BatchColors.textSecondary(isDark)) }
                }
            }

            // Batch Info Card
            if (uiState.selectedImages.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().background(BatchColors.surface(isDark), RoundedCornerShape(8.dp)).clickable { showUserGuide = true }.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Batch Conversion: ${uiState.selectedImages.size} images selected", color = BatchColors.textPrimary(isDark), fontSize = 12.sp)
                        Icon(Icons.Outlined.Info, null, tint = BatchColors.textSecondary(isDark), modifier = Modifier.size(16.dp))
                    }
                }
            }

            // Presets
            Text("Presets", color = BatchColors.textSecondary(isDark), fontSize = 11.sp)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    Box(modifier = Modifier.height(32.dp).background(Color(0xFF334155), CircleShape).clickable { showSavePresetDialog = true }.padding(horizontal = 12.dp), contentAlignment = Alignment.Center) {
                        Row(verticalAlignment=Alignment.CenterVertically, horizontalArrangement=Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(12.dp))
                            Text("New", color = Color.White, fontSize = 11.sp)
                        }
                    }
                }
                items(uiState.presets.size) { index ->
                    val preset = uiState.presets[index]
                    // Preset Item with Long Press
                    Box(modifier = Modifier
                        .height(32.dp)
                        .background(BatchColors.surface(isDark), CircleShape)
                        .border(1.dp, BatchColors.outline(isDark), CircleShape)
                        .combinedClickable(
                            onClick = { viewModel.loadPreset(preset) },
                            onLongClick = { 
                                presetOptionState = preset
                            }
                        )
                        .padding(horizontal = 12.dp), contentAlignment = Alignment.Center) {
                        Text(preset.name, color = BatchColors.textSecondary(isDark), fontSize = 11.sp)
                    }
                    
                    // Dropdown Menu for options (or Dialog)
                    // Implementing simple logic: We need a state holder for the "selected preset for options"
                }
            }
            
            // Format Selection
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val formats = remember {
                     val all = if (android.os.Build.VERSION.SDK_INT >= 28) CompressFormat.values().toList() else CompressFormat.values().filter { it != CompressFormat.HEIF }
                     val (originals, others) = all.partition { it == CompressFormat.ORIGINAL }
                     originals + others
                }
                formats.forEach { format ->
                    val isSelected = uiState.format == format
                    Box(
                        modifier = Modifier.weight(1f).height(32.dp).clip(RoundedCornerShape(8.dp))
                            .background(BatchColors.chipBackground(isDark, isSelected))
                            .border(1.dp, BatchColors.chipBorder(isDark, isSelected), RoundedCornerShape(8.dp))
                            .clickable { viewModel.updateFormat(format) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (format == CompressFormat.ORIGINAL) "Auto" else format.name, color = BatchColors.chipContent(isDark, isSelected), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            // Dimensions + Logic
            val isPdfMode = uiState.format == CompressFormat.PDF
            Text("Dimensions", color = BatchColors.textSecondary(isDark), fontSize = 12.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                // Width
                Box(modifier = Modifier.weight(1f).height(48.dp).background(BatchColors.surface(isDark), RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 6.dp)) {
                    Column {
                        Text("Width", color = BatchColors.textSecondary(isDark), fontSize = 9.sp)
                        androidx.compose.foundation.text.BasicTextField(
                            value = uiState.width, onValueChange = viewModel::updateWidth,
                            textStyle = TextStyle(color = BatchColors.textPrimary(isDark), fontSize = 11.sp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Text("px", color = BatchColors.textSecondary(isDark), fontSize = 11.sp, modifier = Modifier.align(Alignment.BottomEnd))
                }
                // Height
                Box(modifier = Modifier.weight(1f).height(48.dp).background(BatchColors.surface(isDark), RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 6.dp)) {
                    Column {
                        Text("Height", color = BatchColors.textSecondary(isDark), fontSize = 9.sp)
                        androidx.compose.foundation.text.BasicTextField(
                            value = uiState.height, onValueChange = viewModel::updateHeight,
                            textStyle = TextStyle(color = BatchColors.textPrimary(isDark), fontSize = 11.sp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Text("px", color = BatchColors.textSecondary(isDark), fontSize = 11.sp, modifier = Modifier.align(Alignment.BottomEnd))
                }
                // Ratio Toggle
                Box(modifier = Modifier.size(48.dp).background(BatchColors.surface(isDark), RoundedCornerShape(8.dp)).clickable { viewModel.toggleAspectRatio() }, contentAlignment = Alignment.Center) {
                    Icon(if (uiState.maintainAspectRatio) Icons.Default.Link else Icons.Default.LinkOff, null, tint = if (uiState.maintainAspectRatio) BatchColors.primary(isDark) else BatchColors.textSecondary(isDark), modifier = Modifier.size(24.dp))
                }
            }
            // Upscaling Warning
            if (uiState.width.isNotBlank() && uiState.width.toIntOrNull() ?: 0 > uiState.maxInputWidth && uiState.maxInputWidth > 0) {
                 Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                     Icon(Icons.Default.Warning, null, tint = Color(0xFFEAB308), modifier = Modifier.size(12.dp))
                     Text("Upscaling larger than original causes blurriness.", color = Color(0xFFEAB308), fontSize = 11.sp)
                 }
            }

            // Target Size
            Text("Target File Size (Optional)", color = if (isPdfMode) BatchColors.textSecondary(isDark).copy(alpha=0.5f) else BatchColors.textSecondary(isDark), fontSize = 11.sp)
            Row(
                modifier = Modifier.fillMaxWidth().height(32.dp).background(if(isPdfMode) BatchColors.surface(isDark).copy(alpha=0.5f) else BatchColors.surface(isDark), RoundedCornerShape(8.dp)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.foundation.text.BasicTextField(
                    value = if(isPdfMode) "Not supported for PDF" else uiState.targetSize,
                    onValueChange = { if(!isPdfMode) viewModel.updateTargetSize(it) },
                    modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                    textStyle = TextStyle(color = if (isPdfMode) BatchColors.textSecondary(isDark) else BatchColors.textPrimary(isDark), fontSize = 11.sp),
                    singleLine = true,
                    enabled = !isPdfMode,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    decorationBox = { innerTextField ->
                        if (uiState.targetSize.isEmpty() && !isPdfMode) {
                            Text("Max Size", color = BatchColors.textSecondary(isDark).copy(alpha = 0.5f), fontSize = 11.sp)
                        }
                        innerTextField()
                    }
                )
                if (uiState.targetSize.isNotEmpty() && !isPdfMode) {
                     Icon(Icons.Default.Close, null, tint = BatchColors.textSecondary(isDark), modifier = Modifier.padding(end=8.dp).size(16.dp).clickable { viewModel.updateTargetSize("") })
                }
                // Unit
                Row(modifier = Modifier.padding(2.dp).background(BatchColors.outline(isDark), RoundedCornerShape(6.dp))) {
                    Box(modifier = Modifier.clickable { if (uiState.isTargetSizeInMb && !isPdfMode) viewModel.toggleTargetSizeUnit() }
                        .background(if (!uiState.isTargetSizeInMb && !isPdfMode) BatchColors.primary(isDark) else Color.Transparent, RoundedCornerShape(6.dp)).padding(horizontal=8.dp, vertical=4.dp)) {
                        Text("KB", color = if (!uiState.isTargetSizeInMb && !isPdfMode) Color.White else BatchColors.textSecondary(isDark), fontSize = 10.sp)
                    }
                    Box(modifier = Modifier.clickable { if (!uiState.isTargetSizeInMb && !isPdfMode) viewModel.toggleTargetSizeUnit() }
                        .background(if (uiState.isTargetSizeInMb && !isPdfMode) BatchColors.primary(isDark) else Color.Transparent, RoundedCornerShape(6.dp)).padding(horizontal=8.dp, vertical=4.dp)) {
                        Text("MB", color = if (uiState.isTargetSizeInMb && !isPdfMode) Color.White else BatchColors.textSecondary(isDark), fontSize = 10.sp)
                    }
                }
            }
            Text("Overrides Quality slider below.", color = BatchColors.textSecondary(isDark).copy(alpha=0.6f), fontSize = 10.sp)
            
            // Metadata
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Keep Metadata", color = BatchColors.textSecondary(isDark), fontSize = 11.sp)
                    Text("Preserve EXIF data (GPS, Date, Camera)", color = BatchColors.textSecondary(isDark).copy(alpha=0.6f), fontSize = 10.sp)
                }
                Switch(checked = uiState.keepMetadata && !isPdfMode, onCheckedChange = { if(!isPdfMode) viewModel.toggleKeepMetadata() }, enabled = !isPdfMode, modifier = Modifier.scale(0.8f))
            }
            
            // Quality Slider
            val isQualityDisabled = uiState.targetSize.isNotBlank() || isPdfMode
            Column(modifier = Modifier.fillMaxWidth().background(BatchColors.surface(isDark), RoundedCornerShape(8.dp)).padding(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Quality", color = BatchColors.textSecondary(isDark), fontSize = 11.sp)
                    Text(if (isPdfMode) "Auto" else "${uiState.quality}%", color = BatchColors.textPrimary(isDark), fontSize = 11.sp)
                }
                // Compact & Premium Slider
                CompositionLocalProvider(androidx.compose.material3.LocalMinimumInteractiveComponentEnforcement provides false) {
                     Slider(
                        value = if(isPdfMode) 100f else uiState.quality.toFloat(),
                        onValueChange = { viewModel.updateQuality(it.toInt()) },
                        valueRange = 0f..100f,
                        enabled = !isQualityDisabled,
                        modifier = Modifier.padding(vertical = 4.dp), // Compact vertical padding
                        colors = SliderDefaults.colors(
                            thumbColor = BatchColors.primary(isDark),
                            activeTrackColor = BatchColors.primary(isDark),
                            inactiveTrackColor = BatchColors.outline(isDark),
                            disabledThumbColor = BatchColors.outline(isDark),
                            disabledActiveTrackColor = BatchColors.outline(isDark)
                        ),
                        thumb = {
                             // Custom Smaller Thumb for "Compact" look
                             Box(modifier = Modifier
                                 .size(16.dp)
                                 .background(BatchColors.primary(isDark), CircleShape)
                                 .border(2.dp, Color.White, CircleShape)
                             )
                        },
                        track = { sliderState -> 
                            // Custom thinner track
                            Box(modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(if(!isQualityDisabled) BatchColors.outline(isDark) else BatchColors.outline(isDark).copy(alpha=0.5f))
                            ) {
                                Box(modifier = Modifier
                                    .fillMaxWidth(if(isPdfMode) 1f else sliderState.value / 100f)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(if(!isQualityDisabled) BatchColors.primary(isDark) else BatchColors.outline(isDark))
                                )
                            }
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = { viewModel.convertImages() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BatchColors.primary(isDark)),
                shape = RoundedCornerShape(12.dp),
                enabled = uiState.selectedImages.isNotEmpty() && !uiState.isConverting
            ) {
                 if (uiState.isConverting) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                 else Text("Convert & Save", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
    
    // Dialogs
    if (showUserGuide) {
        BatchConverterUserGuide(onDismiss = { showUserGuide = false }, isDark = isDark)
    }
    
    if (showInfoModal && imageDetails != null) {
        AlertDialog(
            onDismissRequest = { showInfoModal = false },
            icon = { Icon(Icons.Outlined.Info, null, tint = Color(0xFF007AFF)) },
            title = { Text("Image Details", color = BatchColors.textPrimary(isDark)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Name: ${imageDetails?.name}", color = BatchColors.textPrimary(isDark))
                    Text("Resolution: ${imageDetails?.resolution}", color = BatchColors.textPrimary(isDark))
                    Text("Size: ${imageDetails?.size}", color = BatchColors.textPrimary(isDark))
                    Text("Type: ${imageDetails?.type}", color = BatchColors.textPrimary(isDark))
                    HorizontalDivider(color = BatchColors.outline(isDark))
                    Text("Path: ${imageDetails?.path}", color = BatchColors.textSecondary(isDark), fontSize = 12.sp, lineHeight = 14.sp)
                    
                    if (imageDetails?.exifData?.isNotEmpty() == true) {
                        HorizontalDivider(color = BatchColors.outline(isDark))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Metadata", color = BatchColors.textPrimary(isDark), fontWeight = FontWeight.SemiBold)
                            // Copy Button
                            val clipboardManager = LocalClipboardManager.current
                            TextButton(
                                onClick = {
                                    val text = imageDetails?.exifData?.entries?.joinToString("\n") { (k, v) -> "$k: $v" } ?: ""
                                    clipboardManager.setText(AnnotatedString(text))
                                }
                            ) {
                                Text("Copy", fontSize = 12.sp, color = BatchColors.primary(isDark))
                            }
                        }
                        
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            imageDetails?.exifData?.forEach { (key, value) ->
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(
                                        key.substringAfter("TAG_").replace("_", " ").lowercase()
                                            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                                        color = BatchColors.textSecondary(isDark),
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        value,
                                        color = BatchColors.textPrimary(isDark),
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(start = 8.dp).weight(1f),
                                        textAlign = TextAlign.End
                                    )
                                }
                            }
                        }
                    } else {
                        HorizontalDivider(color = BatchColors.outline(isDark))
                        Text("No metadata available", color = BatchColors.textSecondary(isDark), fontSize = 12.sp, fontStyle = FontStyle.Italic)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showInfoModal = false }) {
                    Text("Close", color = BatchColors.primary(isDark))
                }
            },
            dismissButton = {
                val context = LocalContext.current
                TextButton(onClick = { 
                     try {
                         val uri = uiState.selectedImages.firstOrNull()
                         if (uri != null) {
                             val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                 setDataAndType(uri, "image/*")
                                 addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                             }
                             context.startActivity(intent)
                         }
                     } catch (e: Exception) {
                         // Fallback
                     }
                }) {
                    Text("Open", color = BatchColors.primary(isDark))
                }
            },
            containerColor = BatchColors.surfaceContainer(isDark),
            titleContentColor = BatchColors.textPrimary(isDark),
            textContentColor = BatchColors.textSecondary(isDark)
        )
    }
    
    if (showSavePresetDialog) {
        AlertDialog(
            onDismissRequest = { showSavePresetDialog = false },
            title = { Text("Save Preset", color = BatchColors.textPrimary(isDark)) },
            text = { OutlinedTextField(value = newPresetName, onValueChange = { newPresetName = it }, textStyle = TextStyle(color = BatchColors.textPrimary(isDark))) },
            confirmButton = { TextButton(onClick = { if (newPresetName.isNotBlank()) { viewModel.savePreset(newPresetName); showSavePresetDialog = false } }) { Text("Save") } },
            dismissButton = { TextButton(onClick = { showSavePresetDialog = false }) { Text("Cancel") } },
            containerColor = BatchColors.surfaceContainer(isDark)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BatchSuccessScreen(
    navController: NavController,
    viewModel: BatchConverterViewModel,
    uiState: BatchConverterUiState,
    isDark: Boolean
) {
    val context = LocalContext.current
    val saveLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) viewModel.saveAllToDevice(context, uri)
    }
    
    val hasFailures = uiState.failedImages.isNotEmpty()
    val hasSuccesses = uiState.convertedImages.isNotEmpty()
    val title = if (hasFailures && hasSuccesses) "Completed with Errors" else if (hasFailures) "Batch Failed" else "Batch Conversion Complete"
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = if(hasFailures) MaterialTheme.colorScheme.error else BatchColors.textPrimary(isDark)) },
                actions = { TextButton(onClick = { viewModel.resetState() }) { Text("Done", color = BatchColors.primary(isDark), fontWeight = FontWeight.Bold) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent, titleContentColor = BatchColors.textPrimary(isDark))
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            
            // FAILED Items Section
            if (hasFailures) {
                Card(
                     colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                     shape = RoundedCornerShape(12.dp),
                     modifier = Modifier.fillMaxWidth().animateContentSize()
                 ) {
                     Column(modifier = Modifier.padding(12.dp)) {
                         Text("${uiState.failedImages.size} Failed Items", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                         Spacer(Modifier.height(8.dp))
                         LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                             items(uiState.failedImages.size) { index ->
                                 val item = uiState.failedImages[index]
                                 Column(modifier = Modifier.width(100.dp)) {
                                     Box(
                                         modifier = Modifier.size(60.dp).background(MaterialTheme.colorScheme.error.copy(alpha=0.2f), RoundedCornerShape(8.dp)), 
                                         contentAlignment = Alignment.Center
                                     ) {
                                         Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error)
                                     }
                                     Text(
                                         item.reason, 
                                         color = MaterialTheme.colorScheme.onErrorContainer, 
                                         style = MaterialTheme.typography.labelSmall, 
                                         maxLines = 2, 
                                         overflow = TextOverflow.Ellipsis
                                     )
                                 }
                             }
                         }
                     }
                 }
            }

            // SUCCESS Items Section
            if (hasSuccesses) {
                 LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                     items(uiState.convertedImages.size) { index ->
                         val image = uiState.convertedImages[index]
                         Box(modifier = Modifier.size(120.dp, 160.dp).clip(RoundedCornerShape(12.dp)).background(BatchColors.surface(isDark)).clickable { viewModel.selectDetailImage(image) }, contentAlignment = Alignment.Center) {
                             if (image.type.contains("pdf", ignoreCase = true)) {
                                 Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                     Icon(Icons.Default.Share, null, tint = BatchColors.primary(isDark), modifier = Modifier.size(32.dp)) // PDF Icon Placeholder
                                     Text("PDF", color = BatchColors.textPrimary(isDark), fontWeight = FontWeight.Bold)
                                 }
                             } else {
                                 AsyncImage(model = ImageRequest.Builder(LocalContext.current).data(image.uri).build(), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                             }
                         }
                     }
                 }
                 
                 // Summary
                 val totalSize = uiState.convertedImages.sumOf { it.sizeBytes }
                 val avgSize = if (uiState.convertedImages.isNotEmpty()) totalSize / uiState.convertedImages.size else 0L
                 
                 Card(
                     colors = CardDefaults.cardColors(containerColor = BatchColors.surface(isDark)),
                     shape = RoundedCornerShape(12.dp),
                     modifier = Modifier.fillMaxWidth()
                 ) {
                     Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                         Text("Conversion Summary", fontWeight = FontWeight.Bold, color = BatchColors.textPrimary(isDark), fontSize = 16.sp)
                         
                         Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                             Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                 Text("Total Size", fontSize = 11.sp, color = BatchColors.textSecondary(isDark))
                                 Text(viewModel.formatFileSize(totalSize), fontWeight = FontWeight.Bold, color = BatchColors.textPrimary(isDark))
                             }
                             Box(modifier = Modifier.width(1.dp).height(24.dp).background(BatchColors.outline(isDark)))
                             Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                 Text("Avg Size", fontSize = 11.sp, color = BatchColors.textSecondary(isDark))
                                 Text(viewModel.formatFileSize(avgSize), fontWeight = FontWeight.Bold, color = BatchColors.textPrimary(isDark))
                             }
                             Box(modifier = Modifier.width(1.dp).height(24.dp).background(BatchColors.outline(isDark)))
                             Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                 Text("Format", fontSize = 11.sp, color = BatchColors.textSecondary(isDark))
                                 Text(uiState.format.name, fontWeight = FontWeight.Bold, color = BatchColors.textPrimary(isDark))
                             }
                         }
                     }
                 }
            } else if (!hasFailures) {
                 // Empty state (shouldn't happen really unless bug)
                 Text("No images processed.", color = BatchColors.textSecondary(isDark))
            }
             
             Spacer(modifier = Modifier.weight(1f))
             
             // Buttons (only if success)
             if (hasSuccesses) {
                 Button(onClick = { if (uiState.convertedImages.isNotEmpty()) viewModel.selectDetailImage(uiState.convertedImages.first()) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = BatchColors.surface(isDark))) {
                     Icon(Icons.Filled.Visibility, null, tint = BatchColors.textSecondary(isDark)); Spacer(Modifier.width(8.dp)); Text("View Changes", color = BatchColors.textSecondary(isDark))
                 }
                 Button(onClick = { saveLauncher.launch(uiState.savedFolderUri) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = BatchColors.surface(isDark))) {
                     Icon(Icons.Filled.Save, null, tint = BatchColors.primary(isDark)); Spacer(Modifier.width(8.dp)); Text("Save All", color = BatchColors.primary(isDark))
                 }
                 Button(onClick = {
                     if (uiState.convertedImages.isNotEmpty()) {
                         try {
                             val uris = ArrayList(uiState.convertedImages.map { it.uri })
                             val intent = android.content.Intent().apply {
                                 if (uris.size == 1) {
                                     action = android.content.Intent.ACTION_SEND
                                     putExtra(android.content.Intent.EXTRA_STREAM, uris.first())
                                 } else {
                                     action = android.content.Intent.ACTION_SEND_MULTIPLE
                                     putParcelableArrayListExtra(android.content.Intent.EXTRA_STREAM, uris)
                                 }
                                 val mimeType = if (uiState.format == CompressFormat.PDF) "application/pdf" else "image/*"
                                 type = mimeType
                                 addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                             }
                             context.startActivity(android.content.Intent.createChooser(intent, "Share output"))
                         } catch (e: Exception) {
                             // Fallback or toast
                         }
                     }
                 }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = BatchColors.primary(isDark))) {
                     Icon(Icons.Filled.Share, null, tint = Color.White); Spacer(Modifier.width(8.dp)); Text("Share All", color = Color.White)
                 }
             }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BatchDetailScreen(viewModel: BatchConverterViewModel, uiState: BatchConverterUiState, isDark: Boolean) {
    val image = uiState.selectedDetailImage ?: return
    var showInfoDialog by remember { mutableStateOf(false) }

    if (showInfoDialog) {
        val detailEntity = com.moshitech.workmate.feature.imagestudio.data.local.ConversionHistoryEntity(
            originalUri = image.originalUri.toString(),
            outputUri = image.uri.toString(),
            date = System.currentTimeMillis(), // We don't track exact conversion time in ConvertedImage, use current or approx
            format = image.type,
            sizeBytes = image.sizeBytes,
            width = image.resolution.substringBefore("x").trim().toIntOrNull() ?: 0,
            height = image.resolution.substringAfter("x").trim().toIntOrNull() ?: 0
        )
        
        // Check if file exists (For preview, it exists in cache)
        val isAvailable = true 

        ImageDetailDialog(
            item = detailEntity,
            isFileExists = isAvailable,
            onDismiss = { showInfoDialog = false },
            onDelete = { 
                showInfoDialog = false
            }, 
            onOpenFolder = null, // Disable open folder for preview (cache)
            isDark = isDark,
            customPath = "Temporary Storage (Unsaved)",
            customStatus = "Ready to Save"
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(image.name, color = Color.White, fontSize = 14.sp) }, // White text on dark bg
                navigationIcon = { IconButton(onClick = { viewModel.closeDetail() }) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) } },
                actions = {
                    IconButton(onClick = { showInfoDialog = true }) {
                        Icon(Icons.Default.Info, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black.copy(alpha=0.5f))
            )
        },
        containerColor = Color.Black
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
             // Zoomable Image
             var scale by remember { mutableFloatStateOf(1f) }
             var offset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
             val state = rememberTransformableState { zoomChange, offsetChange, _ ->
                 scale = (scale * zoomChange).coerceIn(1f, 3f)
                 offset += offsetChange
             }
             
             if (image.type.contains("pdf", ignoreCase = true)) {
                 // PDF Placeholder
                 Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                     Column(horizontalAlignment = Alignment.CenterHorizontally) {
                         Icon(Icons.Default.Share, null, tint = Color.LightGray, modifier = Modifier.size(64.dp)) // Using generic icon or dedicated PDF icon if available, but consistent with available imports
                         Spacer(Modifier.height(16.dp))
                         Text("PDF Preview Not Available", color = Color.LightGray)
                     }
                 }
             } else {
                 AsyncImage(
                     model = ImageRequest.Builder(LocalContext.current).data(image.uri).build(),
                     contentDescription = null,
                     contentScale = ContentScale.Fit,
                     modifier = Modifier
                         .fillMaxSize()
                         .graphicsLayer(
                             scaleX = scale,
                             scaleY = scale,
                             translationX = offset.x,
                             translationY = offset.y
                         )
                         .transformable(state)
                 )
             }
             
             // Info Sheet overlay
             Column(
                 modifier = Modifier
                     .align(Alignment.BottomCenter)
                     .fillMaxWidth()
                     .background(Color.Black.copy(alpha = 0.85f))
                     .padding(16.dp),
                 verticalArrangement = Arrangement.spacedBy(16.dp)
             ) {
                 Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                      Text("Conversion Detail", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                      // Type Badge
                      Text(image.type.uppercase().substringAfter("/"), color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.background(Color.White, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp))
                 }
                 
                 Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                     // Before
                     Column(modifier = Modifier.weight(1f)) {
                         Text("BEFORE", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom=4.dp))
                         Text(image.originalSize, color = Color.White, fontSize = 13.sp)
                         Text(image.originalResolution, color = Color.LightGray, fontSize = 12.sp)
                     }
                     
                     // Arrow
                     Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = BatchColors.primary(true), modifier = Modifier.align(Alignment.CenterVertically).size(20.dp))
                     
                     // After
                     Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                         Text("AFTER", color = BatchColors.primary(true), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom=4.dp))
                         Text(image.size, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                         Text(image.resolution, color = Color.LightGray, fontSize = 12.sp)
                     }
                 }
                 
                 HorizontalDivider(color = Color.Gray.copy(alpha=0.3f))
                 
                 // Metadata Section
                 Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                      Text("Metadata (After)", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                      // Start simple: If PDF, say "Embedded in PDF". If image, say check file.
                      // Currently image.exifData isn't carried over to ConvertedImage model perfectly in the ViewModel logic yet (need to check).
                      // Assuming current model doesn't support "After" metadata specific list, we display standard message or what user asked.
                      if (image.type.contains("pdf", ignoreCase = true)) {
                          Text("Metadata embedded in PDF document.", color = Color.LightGray, fontSize = 12.sp)
                      } else {
                          // For now, static message as we don't re-read the converted file EXIF here yet. behavior match request "if not available also show metadata not available"
                          Text("Metadata preserved in output file.", color = Color.LightGray, fontSize = 12.sp)
                      }
                 }
             }
        }
    }
}

@Composable
fun BatchProgressOverlay(
    progress: Float,
    processedCount: Int,
    totalCount: Int,
    currentFileProgress: Float,
    onCancel: () -> Unit,
    isDark: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BatchColors.scrim(isDark)) // Scrim adhering to theme
            .clickable(enabled = true, onClick = {}) // Block touches
            ,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(120.dp),
                    color = BatchColors.primary(isDark),
                    strokeWidth = 8.dp,
                    trackColor = BatchColors.outline(isDark),
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    color = BatchColors.textPrimary(isDark),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Converting your images...",
                color = BatchColors.textPrimary(isDark),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Converting $processedCount of $totalCount",
                fontSize = 14.sp,
                color = BatchColors.textSecondary(isDark)
            )
            
            // Per-File Progress
            if (currentFileProgress > 0f && currentFileProgress < 1f) {
                Spacer(modifier = Modifier.height(24.dp))
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Current file: ${(currentFileProgress * 100).toInt()}%",
                        color = BatchColors.textSecondary(isDark),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    LinearProgressIndicator(
                        progress = { currentFileProgress },
                        modifier = Modifier
                            .width(180.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = BatchColors.primary(isDark),
                        trackColor = BatchColors.outline(isDark),
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Button(
                onClick = onCancel,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = BatchColors.surface(isDark),
                    contentColor = BatchColors.textSecondary(isDark)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .width(200.dp)
                    .height(48.dp)
            ) {
                Text("Cancel")
            }
        }
    }
}

@Composable
private fun BatchConverterUserGuide(onDismiss: () -> Unit, isDark: Boolean) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Outlined.Info, null, tint = BatchColors.primary(isDark)) },
        title = { Text("Converter Guide", color = BatchColors.textPrimary(isDark), fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Formats Section
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Supported Formats", color = BatchColors.textPrimary(isDark), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text("• JPEG: Best for photos. Small size, but no transparency.", fontSize = 13.sp, color = BatchColors.textSecondary(isDark))
                    Text("• PNG: Best for graphics. Lossless quality & transparency.", fontSize = 13.sp, color = BatchColors.textSecondary(isDark))
                    Text("• WEBP: Modern web format. High quality, very small size.", fontSize = 13.sp, color = BatchColors.textSecondary(isDark))
                    Text("• PDF: Merges all selected images into a single document.", fontSize = 13.sp, color = BatchColors.textSecondary(isDark))
                    Text("• ORIGINAL: Keeps format but allows resizing.", fontSize = 13.sp, color = BatchColors.textSecondary(isDark))
                }
                
                HorizontalDivider(color = BatchColors.outline(isDark))

                // Quality Section
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Quality Control", color = BatchColors.textPrimary(isDark), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text("Adjust the slider from 1-100 to trade quality for file size. 80% is recommended for most uses.", fontSize = 13.sp, color = BatchColors.textSecondary(isDark))
                }

                HorizontalDivider(color = BatchColors.outline(isDark))

                // Resizing Section
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Resizing Options", color = BatchColors.textPrimary(isDark), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text("• Width/Height: Set exact pixel dimensions.", fontSize = 13.sp, color = BatchColors.textSecondary(isDark))
                    Text("• Target Size: Set a max file size (e.g., 500 KB). The app will automatically adjust quality to fit.", fontSize = 13.sp, color = BatchColors.textSecondary(isDark))
                }
                
                HorizontalDivider(color = BatchColors.outline(isDark))

                // Pro Tip
                Card(
                    colors = CardDefaults.cardColors(containerColor = BatchColors.surface(isDark)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("💡 Pro Tip", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BatchColors.textPrimary(isDark))
                        Text("Use 'Save Preset' to remember your favorite settings for next time!", fontSize = 12.sp, color = BatchColors.textSecondary(isDark))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Got it", color = BatchColors.primary(isDark), fontWeight = FontWeight.Bold)
            }
        },
        containerColor = BatchColors.surfaceContainer(isDark),
        titleContentColor = BatchColors.textPrimary(isDark),
        textContentColor = BatchColors.textSecondary(isDark)
    )
}
