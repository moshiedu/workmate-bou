package com.moshitech.workmate.feature.imagestudio.ui

import android.R.attr.padding
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.Info
import java.util.ArrayList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.moshitech.workmate.feature.imagestudio.components.AdContainer
import com.moshitech.workmate.feature.imagestudio.viewmodel.BatchConverterViewModel
import com.moshitech.workmate.feature.imagestudio.data.CompressFormat
import kotlinx.coroutines.launch
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.composed
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.zIndex
import androidx.compose.foundation.isSystemInDarkTheme

private object BatchColors {
    fun background(isDark: Boolean): Color = if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9)
    
    fun surface(isDark: Boolean): Color = if (isDark) Color(0xFF1E293B) else Color.White
    
    fun surfaceContainer(isDark: Boolean): Color = if (isDark) Color(0xFF1E293B) else Color.White
    
    fun textPrimary(isDark: Boolean): Color = if (isDark) Color.White else Color(0xFF1E293B)

    fun textSecondary(isDark: Boolean): Color = if (isDark) Color(0xFF94A3B8) else Color(0xFF6B7280)
    
    fun primary(isDark: Boolean): Color = if (isDark) Color(0xFF3B82F6) else Color(0xFF3B82F6) // Consistent Blue

    fun outline(isDark: Boolean): Color = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
    
    fun scrim(isDark: Boolean): Color = if (isDark) Color(0xE60F172A) else Color(0xFFF1F5F9).copy(alpha=0.95f)

    fun chipBackground(isDark: Boolean, selected: Boolean): Color {
        if (selected) return primary(isDark)
        // Light mode uses dark chips (high contrast)
        return if (isDark) Color.Transparent else Color(0xFF1E293B)
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
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
    
    // Handle back press
    androidx.activity.compose.BackHandler(enabled = uiState.screenState != com.moshitech.workmate.feature.imagestudio.viewmodel.BatchScreenState.INPUT) {
        if (uiState.screenState == com.moshitech.workmate.feature.imagestudio.viewmodel.BatchScreenState.DETAIL) {
            viewModel.closeDetail()
        } else if (uiState.screenState == com.moshitech.workmate.feature.imagestudio.viewmodel.BatchScreenState.SUCCESS) {
            viewModel.resetState()
        }
    }

    // Load Incoming URIs
    LaunchedEffect(incomingUris) {
        if (!incomingUris.isNullOrEmpty()) {
            val type = object : com.google.gson.reflect.TypeToken<List<String>>() {}.type
            try {
                val uris = incomingUris.split(",").map { Uri.parse(Uri.decode(it)) }
                viewModel.onImagesSelected(uris)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (uiState.screenState) {
            com.moshitech.workmate.feature.imagestudio.viewmodel.BatchScreenState.INPUT -> {
                BatchInputScreen(navController, viewModel, uiState, isDark)
            }
            com.moshitech.workmate.feature.imagestudio.viewmodel.BatchScreenState.SUCCESS -> {
                BatchSuccessScreen(navController, viewModel, uiState, isDark)
            }
            com.moshitech.workmate.feature.imagestudio.viewmodel.BatchScreenState.DETAIL -> {
                BatchDetailScreen(viewModel, uiState, isDark)
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun BatchInputScreen(
    navController: NavController,
    viewModel: BatchConverterViewModel,
    uiState: com.moshitech.workmate.feature.imagestudio.viewmodel.BatchConverterUiState,
    isDark: Boolean
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    
    var showInfoModal by remember { androidx.compose.runtime.mutableStateOf(false) }
    var showUserGuide by remember { androidx.compose.runtime.mutableStateOf(false) }
    var showSavePresetDialog by remember { androidx.compose.runtime.mutableStateOf(false) }
    var newPresetName by remember { androidx.compose.runtime.mutableStateOf("") }
    var imageDetails by remember { androidx.compose.runtime.mutableStateOf<com.moshitech.workmate.feature.imagestudio.viewmodel.BatchConverterViewModel.ImageDetails?>(null) }
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    val multiplePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris -> viewModel.onImagesSelected(uris) }
    )



    // State for explicitly selected preview, defaults to first if null
    var selectedPreviewUri by remember { androidx.compose.runtime.mutableStateOf<Uri?>(null) }
    // Ensure we reset manual selection if the underlying list changes to empty or doesn't contain it
    // But for simplicity, just fallback to first if manual selection is null or invalid
    val activePreviewUri = if (selectedPreviewUri != null && uiState.selectedImages.contains(selectedPreviewUri)) {
        selectedPreviewUri
    } else {
        uiState.selectedImages.firstOrNull()
    }

    var previewDetails by remember { androidx.compose.runtime.mutableStateOf<com.moshitech.workmate.feature.imagestudio.viewmodel.BatchConverterViewModel.ImageDetails?>(null) }

    LaunchedEffect(activePreviewUri) {
         if (activePreviewUri != null) {
            previewDetails = viewModel.getImageDetails(activePreviewUri!!)
         } else {
            previewDetails = null
         }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Convert Image", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }
                },
                actions = {
                    // Guide Button
                    IconButton(onClick = { showUserGuide = true }) {
                        Icon(Icons.Outlined.Info, "User Guide", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BatchColors.background(isDark),
                    titleContentColor = BatchColors.textPrimary(isDark),
                    navigationIconContentColor = BatchColors.textPrimary(isDark),
                    actionIconContentColor = BatchColors.textPrimary(isDark)
                )
            )
        },
        bottomBar = {
            PhotoStudioBottomBar(
                currentTab = "Convert",
                isEditorEnabled = false, // Disable editor from here or check logic
                onTabSelected = { tab ->
                    when (tab) {
                        "Gallery" -> {
                             navController.navigate(com.moshitech.workmate.navigation.Screen.ImageStudio.route) {
                                 popUpTo(com.moshitech.workmate.navigation.Screen.ImageStudio.route) { inclusive = true }
                             }
                        }
                        "Editor" -> { /* No-op or handle if needed */ }
                        "Convert" -> { /* Current */ }
                        "Share" -> { /* Handle share */ }
                    }
                }
            )
        },
        containerColor = BatchColors.background(isDark)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Main Preview Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(BatchColors.surface(isDark)), // Darker placeholder
                contentAlignment = Alignment.Center
            ) {
                if (activePreviewUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(activePreviewUri)
                            .build(),
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
                    // Empty State Placeholder specialized for Converter
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
            
            // Short Info Caption (Size & Type)
            if (previewDetails != null) {
                Text(
                    text = "${previewDetails?.size} • ${previewDetails?.type}", 
                    color = BatchColors.textSecondary(isDark),
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    fontWeight = FontWeight.Medium
                )
            } else if (uiState.selectedImages.isNotEmpty()) {
                 Text(
                    text = "Loading info...",
                    color = BatchColors.textSecondary(isDark),
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            // 2. Horizontal Thumbnails List
            androidx.compose.foundation.lazy.LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.selectedImages.size) { index ->
                    val uri = uiState.selectedImages[index]
                    val isPreviewing = uri == activePreviewUri
                    
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                if (isPreviewing) 2.dp else 0.dp, 
                                if (isPreviewing) BatchColors.primary(isDark) else Color.Transparent, 
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedPreviewUri = uri }
                    ) {
                         AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current).data(uri).build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        // Remove button
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .clickable { 
                                    // If we remove the one currently previewed, handled by activePreviewUri logic next render
                                    viewModel.removeImage(uri) 
                                }
                                .padding(2.dp)
                                .background(Color.Black.copy(alpha=0.6f), CircleShape)
                        ) {
                             Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(12.dp))
                        }
                    }
                }
                
                // Add Button at end
                item {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(BatchColors.surface(isDark))
                            .border(1.dp, BatchColors.outline(isDark), RoundedCornerShape(8.dp)) // Dashed effect hard to do simply, solid for now
                            .clickable {
                                multiplePhotoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, null, tint = Color(0xFF94A3B8))
                    }
                }
            }

            // 3. Banner
            if (uiState.selectedImages.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                        .background(BatchColors.surface(isDark), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Batch Conversion: ${uiState.selectedImages.size} images selected",
                        color = Color.White,
                        fontSize = 11.sp
                    )
                    
                    IconButton(
                        onClick = { viewModel.toggleGuide() },
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = "Guide",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }


            // Presets Section
            Text("Presets", color = Color(0xFF94A3B8), fontSize = 11.sp)
            androidx.compose.foundation.lazy.LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Save New Button
                item {
                    Box(
                        modifier = Modifier
                            .height(32.dp)
                            .background(Color(0xFF334155), CircleShape)
                            .clickable { showSavePresetDialog = true }
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(12.dp))
                            Text("New", color = Color.White, fontSize = 11.sp)
                        }
                    }
                }
                
                // Existing Presets
                items(uiState.presets.size) { index ->
                    val preset = uiState.presets[index]
                    Box(
                        modifier = Modifier
                            .height(32.dp)
                            .background(BatchColors.surface(isDark), CircleShape)
                            .border(1.dp, BatchColors.outline(isDark), CircleShape)
                            // We need both click (load) and long click (delete)
                            .composed {
                                this.combinedClickable(
                                    onClick = { viewModel.loadPreset(preset) },
                                    onLongClick = { viewModel.deletePreset(preset.name) }
                                )
                            }
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(preset.name, color = Color(0xFF94A3B8), fontSize = 11.sp)
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val formats = remember {
                    val all = if (android.os.Build.VERSION.SDK_INT >= 28) CompressFormat.values().toList()
                              else CompressFormat.values().filter { it != CompressFormat.HEIF }
                    // Move ORIGINAL to front
                    val (originals, others) = all.partition { it == CompressFormat.ORIGINAL }
                    originals + others
                }
                formats.forEach { format ->
                    val isSelected = uiState.format == format
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(BatchColors.chipBackground(isDark, isSelected))
                            .border(1.dp, BatchColors.chipBorder(isDark, isSelected), RoundedCornerShape(8.dp))
                            .clickable { viewModel.updateFormat(format) },
                        contentAlignment = Alignment.Center
                    ) {
                        val label = if (format == CompressFormat.ORIGINAL) "Auto" else format.name
                        Text(label, color = BatchColors.chipContent(isDark, isSelected), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
            if (uiState.format == CompressFormat.ORIGINAL) {
                Text(
                    "Maintains original file type (e.g. PNG stays PNG) while reducing size.",
                    color = Color(0xFF64748B),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            

            
            
            // Dimensions Section
            Text("Dimensions", color = Color(0xFF94A3B8), fontSize = 12.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Width Input
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .background(BatchColors.surface(isDark), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text("Width", color = BatchColors.textSecondary(isDark), fontSize = 9.sp)
                        androidx.compose.foundation.text.BasicTextField(
                            value = uiState.width,
                            onValueChange = viewModel::updateWidth,
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 11.sp),
                            singleLine = true,
                            cursorBrush = androidx.compose.ui.graphics.SolidColor(Color(0xFF007AFF)),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Text("px", color = Color(0xFF64748B), fontSize = 11.sp, modifier = Modifier.align(Alignment.BottomEnd))
                }

                // Height Input
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .background(BatchColors.surface(isDark), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text("Height", color = BatchColors.textSecondary(isDark), fontSize = 9.sp)
                        androidx.compose.foundation.text.BasicTextField(
                            value = uiState.height,
                            onValueChange = viewModel::updateHeight,
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 11.sp),
                            singleLine = true,
                            cursorBrush = androidx.compose.ui.graphics.SolidColor(BatchColors.primary(isDark)),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Text("px", color = Color(0xFF64748B), fontSize = 11.sp, modifier = Modifier.align(Alignment.BottomEnd))
                }
                



                // Link/Constraint Toggle
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(BatchColors.surface(isDark), RoundedCornerShape(8.dp))
                        .clickable { viewModel.toggleAspectRatio() },
                    contentAlignment = Alignment.Center
                ) {
                    // Start of link icon - using raw resource or vector would be best, standard Link icon often works
                    // Using standard material Link icon, tint changes based on state
                    Icon(
                        if (uiState.maintainAspectRatio) androidx.compose.material.icons.Icons.Default.Link else androidx.compose.material.icons.Icons.Default.LinkOff,
                        contentDescription = "Toggle Aspect Ratio",
                        tint = if (uiState.maintainAspectRatio) BatchColors.primary(isDark) else BatchColors.textSecondary(isDark),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Upscaling Warning
            val widthInt = uiState.width.toIntOrNull() ?: 0
            val heightInt = uiState.height.toIntOrNull() ?: 0
            if ((widthInt > uiState.maxInputWidth && uiState.maxInputWidth > 0) || 
                (heightInt > uiState.maxInputHeight && uiState.maxInputHeight > 0)) {
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEAB308), modifier = Modifier.size(12.dp))
                    Text(
                        "Upscaling larger than original causes blurriness.",
                        color = Color(0xFFEAB308),
                        fontSize = 11.sp
                    )
                }
            }

            // PDF Mode Check
            val isPdfMode = uiState.format == CompressFormat.PDF
            
            // Target File Size
            Text("Target File Size (Optional)", color = if (isPdfMode) BatchColors.textSecondary(isDark).copy(alpha=0.5f) else BatchColors.textSecondary(isDark), fontSize = 11.sp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .background(if(isPdfMode) BatchColors.surface(isDark).copy(alpha=0.5f) else BatchColors.surface(isDark), RoundedCornerShape(8.dp)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.foundation.text.BasicTextField(
                    value = if(isPdfMode) "Not supported for PDF" else uiState.targetSize,
                    onValueChange = { if(!isPdfMode) viewModel.updateTargetSize(it) },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(color = if (isPdfMode) BatchColors.textSecondary(isDark) else BatchColors.textPrimary(isDark), fontSize = 11.sp),
                    singleLine = true,
                    enabled = !isPdfMode,
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(BatchColors.primary(isDark)),
                    decorationBox = { innerTextField ->
                        if (uiState.targetSize.isEmpty() && !isPdfMode) {
                             Text("Max Size", color = BatchColors.textSecondary(isDark), fontSize = 11.sp)
                        }
                        innerTextField()
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                // Clear Button
                if (uiState.targetSize.isNotEmpty() && !isPdfMode) {
                    Box(
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .size(24.dp)
                            .clip(CircleShape)
                            .clickable { viewModel.updateTargetSize("") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                
                // Unit Toggle
                Row(
                   modifier = Modifier
                       .padding(2.dp)
                       .background(if(isPdfMode) BatchColors.outline(isDark).copy(alpha=0.5f) else BatchColors.outline(isDark), RoundedCornerShape(6.dp)) 
                ) {
                    Box(
                        modifier = Modifier
                            .background(if (!uiState.isTargetSizeInMb && !isPdfMode) BatchColors.primary(isDark) else Color.Transparent, RoundedCornerShape(6.dp))
                            .clickable { if (uiState.isTargetSizeInMb && !isPdfMode) viewModel.toggleTargetSizeUnit() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                         Text("KB", color = if (!uiState.isTargetSizeInMb && !isPdfMode) Color.White else BatchColors.textSecondary(isDark), fontSize = 10.sp)
                    }
                    Box(
                        modifier = Modifier
                            .background(if (uiState.isTargetSizeInMb && !isPdfMode) BatchColors.primary(isDark) else Color.Transparent, RoundedCornerShape(6.dp))
                            .clickable { if (!uiState.isTargetSizeInMb && !isPdfMode) viewModel.toggleTargetSizeUnit() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                         Text("MB", color = if (uiState.isTargetSizeInMb && !isPdfMode) Color.White else BatchColors.textSecondary(isDark), fontSize = 10.sp)
                    }
                }
            }
            
            if (!isPdfMode) {
                Text("Overrides Quality slider below.", color = Color(0xFF64748B), fontSize = 11.sp)
            }
            if (uiState.targetSize.isNotBlank() && !isPdfMode) {
                Text(
                    "Note: Resolution will be reduced if necessary to meet target.",
                    color = Color(0xFFEAB308),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // Metadata Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Keep Metadata", color = if (isPdfMode) Color(0xFF64748B) else Color(0xFF94A3B8), fontSize = 11.sp)
                    Text("Preserve EXIF data (GPS, Date, Camera)", color = Color(0xFF64748B), fontSize = 10.sp)
                }
                androidx.compose.material3.Switch(
                    checked = uiState.keepMetadata && !isPdfMode,
                    onCheckedChange = { if (!isPdfMode) viewModel.toggleKeepMetadata() },
                    enabled = !isPdfMode,
                    colors = androidx.compose.material3.SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF007AFF),
                        uncheckedThumbColor = if (isPdfMode) Color(0xFF475569) else Color(0xFF94A3B8),
                        uncheckedTrackColor = Color(0xFF334155),
                        disabledCheckedTrackColor = Color(0xFF334155),
                        disabledUncheckedTrackColor = Color(0xFF1E293B)
                    ),
                    modifier = Modifier.scale(0.8f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Quality Slider
            val isQualityDisabled = uiState.targetSize.isNotBlank() || isPdfMode
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if(isPdfMode) MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f) else MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Quality", color = if (isQualityDisabled) Color(0xFF64748B) else Color(0xFF94A3B8), fontSize = 11.sp)
                    Text(if (isPdfMode) "Auto" else "${uiState.quality}%", color = if (isQualityDisabled) Color(0xFF64748B) else Color.White, fontSize = 11.sp)
                }
                
                Slider(
                    value = if(isPdfMode) 100f else uiState.quality.toFloat(),
                    onValueChange = { viewModel.updateQuality(it.toInt()) },
                    valueRange = 0f..100f,
                    enabled = !isQualityDisabled,
                    modifier = Modifier.height(20.dp), // Force reduced height for the slider component itself
                    thumb = {
                        // Custom Vertical Thumb (Compact)
                         Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(16.dp)
                                .background(if (isQualityDisabled) Color(0xFF475569) else Color(0xFF007AFF), CircleShape)
                        )
                    },
                    track = { sliderState ->
                        SliderDefaults.Track(
                            sliderState = sliderState,
                            colors = androidx.compose.material3.SliderDefaults.colors(
                                activeTrackColor = Color(0xFF007AFF),
                                inactiveTrackColor = Color(0xFF0F172A),
                                disabledActiveTrackColor = Color(0xFF475569),
                                disabledInactiveTrackColor = Color(0xFF0F172A)
                            ),
                            modifier = Modifier
                                .height(6.dp) // Reduced height
                                .clip(RoundedCornerShape(3.dp)),
                            thumbTrackGapSize = 0.dp
                        )
                    }
                )
            }
            
            if (isQualityDisabled && !isPdfMode) { // Only show specific message if not PDF (PDF has its own context)
                Text(
                    "Quality slider disabled by Target Size.",
                    color = Color(0xFF64748B),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                )
            }
            if (isPdfMode) {
                 Text(
                    "PDF uses automatic quality and compression.",
                    color = Color(0xFF64748B),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Button
            Button(
                onClick = { viewModel.convertImages() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = BatchColors.primary(isDark)),
                shape = RoundedCornerShape(12.dp),
                enabled = uiState.selectedImages.isNotEmpty() && !uiState.isConverting
            ) {
                if (uiState.isConverting) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text("Convert & Save", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
    
    if (uiState.showGuide) {
        com.moshitech.workmate.feature.imagestudio.ui.BatchGuideDialog(
            onDismiss = { viewModel.toggleGuide() }
        )
    }
    
    if (showSavePresetDialog) {
        AlertDialog(
            onDismissRequest = { showSavePresetDialog = false },
            title = { Text("Save Preset") },
            text = {
                Column {
                    Text("Enter a name for this preset:", fontSize = 14.sp, color = Color(0xFF94A3B8))
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPresetName,
                        onValueChange = { newPresetName = it },
                        singleLine = true,
                        textStyle = TextStyle(color = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newPresetName.isNotBlank()) {
                            viewModel.savePreset(newPresetName)
                            newPresetName = ""
                            showSavePresetDialog = false
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSavePresetDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = BatchColors.surfaceContainer(isDark),
            titleContentColor = BatchColors.textPrimary(isDark),
            textContentColor = BatchColors.textSecondary(isDark)
        )
    }

    if (showUserGuide) {
        BatchConverterUserGuide(onDismiss = { showUserGuide = false }, isDark = isDark)
    }

    if (showInfoModal && imageDetails != null) {
        AlertDialog(
            onDismissRequest = { showInfoModal = false },
            icon = { Icon(Icons.Outlined.Info, null, tint = Color(0xFF007AFF)) },
            title = { Text("Image Details", color = MaterialTheme.colorScheme.onSurface) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Name: ${imageDetails?.name}", color = Color(0xFF334155))
                    Text("Resolution: ${imageDetails?.resolution}", color = Color(0xFF334155))
                    Text("Size: ${imageDetails?.size}", color = Color(0xFF334155))
                    Text("Type: ${imageDetails?.type}", color = Color(0xFF334155))
                    androidx.compose.material3.HorizontalDivider()
                    Text("Path: ${imageDetails?.path}", color = Color(0xFF64748B), fontSize = 12.sp, lineHeight = 14.sp)
                    
                    if (imageDetails?.exifData?.isNotEmpty() == true) {
                        androidx.compose.material3.HorizontalDivider()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Metadata", color = Color(0xFF0F172A), fontWeight = FontWeight.SemiBold)
                            // Copy Button
                            val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                            androidx.compose.material3.TextButton(
                                onClick = {
                                    val text = imageDetails?.exifData?.entries?.joinToString("\n") { (k, v) -> "$k: $v" } ?: ""
                                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(text))
                                }
                            ) {
                                Text("Copy", fontSize = 12.sp, color = BatchColors.primary(isDark))
                            }
                        }
                        
                        imageDetails?.exifData?.forEach { (key, value) ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(
                                    key.substringAfter("TAG_").replace("_", " ").lowercase()
                                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() },
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
                                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                                )
                            }
                        }
                    } else {
                        androidx.compose.material3.HorizontalDivider()
                        Text("No metadata available", color = BatchColors.textSecondary(isDark), fontSize = 12.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                    }
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { showInfoModal = false }) {
                    Text("Close", color = BatchColors.primary(isDark))
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { 
                     // Try open in gallery
                     try {
                         val uri = uiState.selectedImages.firstOrNull() // Re-fetching isn't ideal but safe
                         if (uri != null) {
                             val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                 setDataAndType(uri, "image/*")
                                 addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                             }
                             context.startActivity(intent)
                         }
                     } catch (e: Exception) {
                         // Fallback or ignore
                     }
                }) {
                    Text("Open", color = BatchColors.primary(isDark))
                }
            },
            containerColor = BatchColors.surfaceContainer(isDark)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BatchSuccessScreen(
    navController: NavController,
    viewModel: BatchConverterViewModel,
    uiState: com.moshitech.workmate.feature.imagestudio.viewmodel.BatchConverterUiState,
    isDark: Boolean
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Folder Picker
    val saveLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            viewModel.saveAllToDevice(context, uri)
        }
    }
    
     // Show Snackbar msg
    LaunchedEffect(uiState.message) {
        if (uiState.message != null) {
            val result = snackbarHostState.showSnackbar(
                message = uiState.message!!,
                actionLabel = if (uiState.lastSavedLocation != null && uiState.message!!.contains("Saved")) "Open" else null,
                duration = androidx.compose.material3.SnackbarDuration.Short
            )
            
            if (result == androidx.compose.material3.SnackbarResult.ActionPerformed && uiState.lastSavedLocation != null) {
                try {
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                        setDataAndType(uiState.lastSavedLocation, "vnd.android.document/directory")
                        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        putExtra("android.provider.extra.INITIAL_URI", uiState.lastSavedLocation)
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    // Try fallback
                     try {
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                            setDataAndType(uiState.lastSavedLocation, "resource/folder")
                            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(intent)
                    } catch (e2: Exception) {
                         try {
                             // Last resort: standard chooser
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                setDataAndType(uiState.lastSavedLocation, "*/*")
                                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(android.content.Intent.createChooser(intent, "Open Folder"))
                        } catch (e3: Exception) {
                            android.widget.Toast.makeText(context, "Could not open folder", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            viewModel.clearMessage()
        }
    }
    
    Scaffold(
         snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Batch Conversion Complete", fontWeight = FontWeight.SemiBold, fontSize = 18.sp) },
                actions = {
                    androidx.compose.material3.TextButton(onClick = { viewModel.resetState() }) {
                        Text("Done", color = BatchColors.primary(isDark), fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BatchColors.background(isDark),
                    titleContentColor = BatchColors.textPrimary(isDark)
                )
            )
        },
        bottomBar = {
             // 
        },
        containerColor = BatchColors.background(isDark)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Horizontal Result List
            androidx.compose.foundation.lazy.LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.convertedImages.size) { index ->
                    val image = uiState.convertedImages[index]
                    val isPdf = image.type.contains("pdf", ignoreCase = true) || image.name.contains("pdf", ignoreCase = true)
                    
                    Box(
                        modifier = Modifier
                            .size(120.dp, 160.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(BatchColors.surface(isDark))
                            .clickable { viewModel.selectDetailImage(image) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isPdf) {
                            PdfPreview(
                                uri = image.uri,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current).data(image.uri).build(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
            
            Text("Conversion Summary", color = BatchColors.textPrimary(isDark), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            
            // Summary Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        val label = if (uiState.format == com.moshitech.workmate.feature.imagestudio.data.CompressFormat.PDF) "Images Merged" else "Total Images Converted"
                        val count = if (uiState.format == com.moshitech.workmate.feature.imagestudio.data.CompressFormat.PDF) "${uiState.processedCount}" else "${uiState.convertedImages.size}"
                        Text(label, color = BatchColors.textSecondary(isDark))
                        Text(count, color = BatchColors.textSecondary(isDark), fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Output Format", color = BatchColors.textSecondary(isDark))
                        Text(uiState.format.name, color = BatchColors.textSecondary(isDark), fontWeight = FontWeight.Bold)
                    }
                    // Avg Size placeholder
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Average File Size", color = BatchColors.textSecondary(isDark))
                         // Simple avg calc
                        // Avg Size calc
                        val avgSize = if (uiState.convertedImages.isNotEmpty()) {
                            val totalBytes = uiState.convertedImages.sumOf { it.sizeBytes }
                            val avgBytes = totalBytes / uiState.convertedImages.size
                            viewModel.formatFileSize(avgBytes)
                        } else "0 KB"
                        Text(avgSize, color = BatchColors.textSecondary(isDark), fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Actions
            Button(
                onClick = { 
                    if (uiState.convertedImages.isNotEmpty()) {
                        viewModel.selectDetailImage(uiState.convertedImages.first())
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = BatchColors.surface(isDark)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Filled.Visibility, null, tint = BatchColors.textSecondary(isDark), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("View Changes", color = BatchColors.textSecondary(isDark))
            }
            
            Button(
                onClick = { 
                    // Launch picker to choose folder with initial saved uri
                    saveLauncher.launch(uiState.savedFolderUri)
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = BatchColors.surface(isDark)),
                shape = RoundedCornerShape(8.dp)
            ) {
                 Icon(Icons.Filled.Save, null, tint = BatchColors.primary(isDark), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save All to Device", color = BatchColors.primary(isDark))
            }
            
            Button(
                onClick = { 
                    try {
                        val uris = ArrayList<Uri>()
                        uiState.convertedImages.forEach { image ->
                            val file = java.io.File(image.uri.path!!)
                            val contentUri = androidx.core.content.FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                file
                            )
                            uris.add(contentUri)
                        }
                        
                        val intent = android.content.Intent(android.content.Intent.ACTION_SEND_MULTIPLE).apply {
                            type = "image/*"
                            putParcelableArrayListExtra(android.content.Intent.EXTRA_STREAM, uris)
                            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(android.content.Intent.createChooser(intent, "Share images"))
                    } catch (e: Exception) {
                        e.printStackTrace()
                        // Fallback or show toast
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = BatchColors.primary(isDark)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Filled.Share, null, tint = Color.White, modifier = Modifier.size(20.dp))
                 Spacer(modifier = Modifier.width(8.dp))
                Text("Share All", color = Color.White)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BatchDetailScreen(
    viewModel: BatchConverterViewModel,
    uiState: com.moshitech.workmate.feature.imagestudio.viewmodel.BatchConverterUiState,
    isDark: Boolean
) {
    val image = uiState.selectedDetailImage ?: return
    var details by remember { androidx.compose.runtime.mutableStateOf<com.moshitech.workmate.feature.imagestudio.viewmodel.BatchConverterViewModel.ImageDetails?>(null) }
    var showUserGuide by remember { androidx.compose.runtime.mutableStateOf(false) }
    
    LaunchedEffect(image) {
        details = viewModel.getImageDetails(image.uri)
    }
    
    val isPdf = image.type.contains("pdf", ignoreCase = true) || image.name.contains("pdf", ignoreCase = true)
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("presets", color = BatchColors.textPrimary(isDark), fontWeight = FontWeight.SemiBold, fontSize = 16.sp, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.closeDetail() }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }
                },
                actions = {
                    // Guide Button
                    IconButton(onClick = { showUserGuide = true }) {
                        Icon(Icons.Outlined.Info, "User Guide", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BatchColors.background(isDark))
            )
        },
        floatingActionButton = {
            if (isPdf) {
                val context = LocalContext.current
                androidx.compose.material3.ExtendedFloatingActionButton(
                     onClick = {
                         try {
                              val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                  setDataAndType(image.uri, "application/pdf")
                                  addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                              }
                              context.startActivity(intent)
                         } catch (e: Exception) {
                             android.widget.Toast.makeText(context, "No PDF Viewer found", android.widget.Toast.LENGTH_SHORT).show()
                         }
                     },
                     text = { Text("Open PDF") },
                     icon = { Icon(Icons.Default.Visibility, null) },
                     containerColor = BatchColors.primary(isDark),
                     contentColor = Color.White
                 )
            }
        },
        bottomBar = {
             // Details Panel
             Column(
                 modifier = Modifier
                     .fillMaxWidth()
                     .background(BatchColors.background(isDark))
                     .navigationBarsPadding()
                     .heightIn(max = 400.dp)
                     .verticalScroll(rememberScrollState())
                     .padding(16.dp)
             ) {
                 // ... existing details implementation (abbreviated here for tool efficiency, but in practice I keep it)
                 // NOTE: The tool says "ReplacementContent". 
                 // I need to provide the FULL bottomBar content OR target carefully.
                 // Since bottomBar is huge, I should probably TARGET only the Scaffold definition and keep bottomBar as is?
                 // No, I can't target "Scaffold to bottomBar" easily without including bottomBar.
                 // I will target the opening of Scaffold up to bottomBar start.
                 
                 // Wait, I can target the top part of Scaffold and the FAB, and then the bottom bar start.
                 // Let's rely on valid replacement.
                 // I will provide the FULL Scaffold structure but reuse the existing bottomBar inside the replace block.
                 // Actually, simpler: I'll replace the Scaffold start up to `containerColor` line? No.
                 
                 // I will replace `Scaffold(` ... `floatingActionButton = { ... }` ... `bottomBar = {`
                 
                 // Let's do it in chunks.
             }
        },
        containerColor = BatchColors.background(isDark)
    ) { padding ->
        val scale = remember { androidx.compose.runtime.mutableStateOf(1f) }
        val offset = remember { androidx.compose.runtime.mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
        val state = rememberTransformableState { zoomChange, offsetChange, _ ->
            scale.value *= zoomChange
            // Limit min scale to 1f
            if (scale.value < 1f) scale.value = 1f
            // Simple offset accumulation
            offset.value += offsetChange
        }

        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color.Black)
                .clipToBounds() // Clip sticking out parts
                .transformable(state = state),
            contentAlignment = Alignment.Center
        ) {
             if (isPdf) {
                 PdfPreview(
                     uri = image.uri,
                     modifier = Modifier
                         .fillMaxSize()
                         .graphicsLayer(
                             scaleX = scale.value,
                             scaleY = scale.value,
                             translationX = offset.value.x,
                             translationY = offset.value.y
                         )
                 )
             } else {
                 AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current).data(image.uri).build(),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale.value,
                            scaleY = scale.value,
                            translationX = offset.value.x,
                            translationY = offset.value.y
                        )
                )
             }
        }
    }

    if (showUserGuide) {
        BatchConverterUserGuide(onDismiss = { showUserGuide = false }, isDark = isDark)
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
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Converting $processedCount of $totalCount",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
private fun PdfPreview(
    uri: Uri,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var bitmap by remember { androidx.compose.runtime.mutableStateOf<android.graphics.Bitmap?>(null) }
    var error by remember { androidx.compose.runtime.mutableStateOf<String?>(null) }
    
    LaunchedEffect(uri) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                // Debug log
                android.util.Log.d("PdfPreview", "Rendering $uri")
                context.contentResolver.openFileDescriptor(uri, "r")?.use { descriptor ->
                    val renderer = android.graphics.pdf.PdfRenderer(descriptor)
                    if (renderer.pageCount > 0) {
                        val page = renderer.openPage(0)
                        val w = (page.width * 2).coerceAtMost(2048)
                        val h = (page.height * 2).coerceAtMost(2048)
                        val bmp = android.graphics.Bitmap.createBitmap(w, h, android.graphics.Bitmap.Config.ARGB_8888)
                        page.render(bmp, null, null, android.graphics.pdf.PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        page.close()
                        bitmap = bmp
                    }
                    renderer.close()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                error = e.localizedMessage
            }
        }
    }

    if (bitmap != null) {
        androidx.compose.foundation.Image(
            bitmap = bitmap!!.asImageBitmap(),
            contentDescription = "PDF Preview",
            contentScale = ContentScale.Fit,
            modifier = modifier
        )
    } else {
         Box(modifier = modifier, contentAlignment = Alignment.Center) {
             Column(horizontalAlignment = Alignment.CenterHorizontally) {
                 CircularProgressIndicator(color = Color.White)
                 if (error != null) {
                     Text("Error: $error", color = Color.Red, modifier = Modifier.padding(16.dp))
                 }
                 Text("Loading Preview...", color = Color.Gray)
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
                
                androidx.compose.material3.HorizontalDivider(color = BatchColors.outline(isDark))

                // Quality Section
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Quality Control", color = BatchColors.textPrimary(isDark), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text("Adjust the slider from 1-100 to trade quality for file size. 80% is recommended for most uses.", fontSize = 13.sp, color = BatchColors.textSecondary(isDark))
                }

                androidx.compose.material3.HorizontalDivider(color = BatchColors.outline(isDark))

                // Resizing Section
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Resizing Options", color = BatchColors.textPrimary(isDark), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text("• Width/Height: Set exact pixel dimensions.", fontSize = 13.sp, color = BatchColors.textSecondary(isDark))
                    Text("• Target Size: Set a max file size (e.g., 500 KB). The app will automatically adjust quality to fit.", fontSize = 13.sp, color = BatchColors.textSecondary(isDark))
                }
                
                androidx.compose.material3.HorizontalDivider(color = BatchColors.outline(isDark))

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
