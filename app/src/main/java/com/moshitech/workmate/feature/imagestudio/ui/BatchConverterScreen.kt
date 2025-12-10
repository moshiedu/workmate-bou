package com.moshitech.workmate.feature.imagestudio.ui

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
import androidx.compose.ui.graphics.graphicsLayer

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BatchConverterScreen(
    navController: NavController,
    incomingUris: String? = null,
    viewModel: BatchConverterViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
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

    when (uiState.screenState) {
        com.moshitech.workmate.feature.imagestudio.viewmodel.BatchScreenState.INPUT -> {
            BatchInputScreen(navController, viewModel, uiState)
        }
        com.moshitech.workmate.feature.imagestudio.viewmodel.BatchScreenState.SUCCESS -> {
            BatchSuccessScreen(navController, viewModel, uiState)
        }
        com.moshitech.workmate.feature.imagestudio.viewmodel.BatchScreenState.DETAIL -> {
            BatchDetailScreen(viewModel, uiState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun BatchInputScreen(
    navController: NavController,
    viewModel: BatchConverterViewModel,
    uiState: com.moshitech.workmate.feature.imagestudio.viewmodel.BatchConverterUiState
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    
    var showInfoModal by remember { androidx.compose.runtime.mutableStateOf(false) }
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
                title = { Text("Convert Image", color = Color.White, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A)
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
        containerColor = Color(0xFF0F172A)
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
                    .background(Color(0xFF1E293B)), // Darker placeholder
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
                         Icon(Icons.Default.Add, null, tint = Color(0xFF007AFF), modifier = Modifier.size(48.dp))
                         Text("Tap to add images", color = Color(0xFF94A3B8))
                     }
                }
            }
            
            // Short Info Caption (Size & Type)
            if (previewDetails != null) {
                Text(
                    text = "${previewDetails?.size} • ${previewDetails?.type}", 
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    fontWeight = FontWeight.Medium
                )
            } else if (uiState.selectedImages.isNotEmpty()) {
                 Text(
                    text = "Loading info...",
                    color = Color(0xFF94A3B8),
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
                                if (isPreviewing) Color(0xFF007AFF) else Color.Transparent, 
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
                            .background(Color(0xFF1E293B))
                            .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp)) // Dashed effect hard to do simply, solid for now
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
                        .background(Color(0xFF1E293B), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Batch Conversion: ${uiState.selectedImages.size} images selected",
                        color = Color.White,
                        fontSize = 11.sp
                    )
                }
            }

            Text("Output Format", color = Color(0xFF94A3B8), fontSize = 12.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CompressFormat.values().forEach { format ->
                    val isSelected = uiState.format == format
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Color(0xFF007AFF) else Color(0xFF334155))
                            .clickable { viewModel.updateFormat(format) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(format.name, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                }
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
                        .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text("Width", color = Color(0xFF64748B), fontSize = 9.sp)
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
                        .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text("Height", color = Color(0xFF64748B), fontSize = 9.sp)
                        androidx.compose.foundation.text.BasicTextField(
                            value = uiState.height,
                            onValueChange = viewModel::updateHeight,
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 11.sp),
                            singleLine = true,
                            cursorBrush = androidx.compose.ui.graphics.SolidColor(Color(0xFF007AFF)),
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
                        .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
                        .clickable { viewModel.toggleAspectRatio() },
                    contentAlignment = Alignment.Center
                ) {
                    // Start of link icon - using raw resource or vector would be best, standard Link icon often works
                    // Using standard material Link icon, tint changes based on state
                    Icon(
                        if (uiState.maintainAspectRatio) androidx.compose.material.icons.Icons.Default.Link else androidx.compose.material.icons.Icons.Default.LinkOff,
                        contentDescription = "Toggle Aspect Ratio",
                        tint = if (uiState.maintainAspectRatio) Color(0xFF007AFF) else Color(0xFF64748B),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Text("Target File Size (Optional)", color = Color(0xFF94A3B8), fontSize = 11.sp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .background(Color(0xFF1E293B), RoundedCornerShape(8.dp)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.foundation.text.BasicTextField(
                    value = uiState.targetSize,
                    onValueChange = viewModel::updateTargetSize,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 11.sp),
                    singleLine = true,
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(Color(0xFF007AFF)),
                    decorationBox = { innerTextField ->
                        if (uiState.targetSize.isEmpty()) {
                             Text("Max Size", color = Color(0xFF475569), fontSize = 11.sp)
                        }
                        innerTextField()
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                
                // Unit Toggle
                Row(
                   modifier = Modifier
                       .padding(2.dp)
                       .background(Color(0xFF334155), RoundedCornerShape(6.dp)) 
                ) {
                    Box(
                        modifier = Modifier
                            .background(if (!uiState.isTargetSizeInMb) Color(0xFF007AFF) else Color.Transparent, RoundedCornerShape(6.dp))
                            .clickable { if (uiState.isTargetSizeInMb) viewModel.toggleTargetSizeUnit() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                         Text("KB", color = if (!uiState.isTargetSizeInMb) Color.White else Color(0xFF94A3B8), fontSize = 10.sp)
                    }
                    Box(
                        modifier = Modifier
                            .background(if (uiState.isTargetSizeInMb) Color(0xFF007AFF) else Color.Transparent, RoundedCornerShape(6.dp))
                            .clickable { if (!uiState.isTargetSizeInMb) viewModel.toggleTargetSizeUnit() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                         Text("MB", color = if (uiState.isTargetSizeInMb) Color.White else Color(0xFF94A3B8), fontSize = 10.sp)
                    }
                }
            }
            
            Text("Overrides Quality slider below.", color = Color(0xFF64748B), fontSize = 11.sp)

            Spacer(modifier = Modifier.height(4.dp))

            // Quality Slider
            val isQualityDisabled = uiState.targetSize.isNotBlank()
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Quality", color = if (isQualityDisabled) Color(0xFF64748B) else Color(0xFF94A3B8), fontSize = 11.sp)
                    Text("${uiState.quality}%", color = if (isQualityDisabled) Color(0xFF64748B) else Color.White, fontSize = 11.sp)
                }
                
                Slider(
                    value = uiState.quality.toFloat(),
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
                                .background(Color(0xFF007AFF), CircleShape)
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
            
            if (isQualityDisabled) {
                Text(
                    "Quality slider disabled by Target Size.",
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
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF)),
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
    
    if (showInfoModal && imageDetails != null) {
        AlertDialog(
            onDismissRequest = { showInfoModal = false },
            icon = { Icon(Icons.Outlined.Info, null, tint = Color(0xFF007AFF)) },
            title = { Text("Image Details", color = Color(0xFF0F172A)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Name: ${imageDetails?.name}", color = Color(0xFF334155))
                    Text("Resolution: ${imageDetails?.resolution}", color = Color(0xFF334155))
                    Text("Size: ${imageDetails?.size}", color = Color(0xFF334155))
                    Text("Type: ${imageDetails?.type}", color = Color(0xFF334155))
                    androidx.compose.material3.HorizontalDivider()
                    Text("Path: ${imageDetails?.path}", color = Color(0xFF64748B), fontSize = 12.sp, lineHeight = 14.sp)
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { showInfoModal = false }) {
                    Text("Close", color = Color(0xFF007AFF))
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
                    Text("Open", color = Color(0xFF007AFF))
                }
            },
            containerColor = Color.White
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BatchSuccessScreen(
    navController: NavController,
    viewModel: BatchConverterViewModel,
    uiState: com.moshitech.workmate.feature.imagestudio.viewmodel.BatchConverterUiState
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
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    // Try fallback
                     try {
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                            setDataAndType(uiState.lastSavedLocation, "*/*")
                            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(intent)
                    } catch (e2: Exception) {
                        android.widget.Toast.makeText(context, "Could not open folder", android.widget.Toast.LENGTH_SHORT).show()
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
                title = { Text("Batch Conversion Complete", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 18.sp) },
                actions = {
                    androidx.compose.material3.TextButton(onClick = { viewModel.resetState() }) {
                        Text("Done", color = Color(0xFF007AFF), fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F172A))
            )
        },
        bottomBar = {
             // 
        },
        containerColor = Color(0xFF0F172A)
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
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current).data(image.uri).build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(120.dp, 160.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1E293B))
                            .clickable { viewModel.selectDetailImage(image) }
                    )
                }
            }
            
            Text("Conversion Summary", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            
            // Summary Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Images Converted", color = Color(0xFF94A3B8))
                        Text("${uiState.convertedImages.size}", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Output Format", color = Color(0xFF94A3B8))
                        Text(uiState.format.name, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    // Avg Size placeholder
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Average File Size", color = Color(0xFF94A3B8))
                         // Simple avg calc
                        val avgSize = if (uiState.convertedImages.isNotEmpty()) {
                             uiState.convertedImages.first().size 
                        } else "0 KB"
                        Text(avgSize, color = Color.White, fontWeight = FontWeight.Bold)
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
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Filled.Visibility, null, tint = Color.White, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("View Changes", color = Color.White)
            }
            
            Button(
                onClick = { 
                    // Launch picker to choose folder with initial saved uri
                    saveLauncher.launch(uiState.savedFolderUri)
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(8.dp)
            ) {
                 Icon(Icons.Filled.Save, null, tint = Color(0xFF007AFF), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save All to Device", color = Color(0xFF007AFF))
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
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF)),
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
    uiState: com.moshitech.workmate.feature.imagestudio.viewmodel.BatchConverterUiState
) {
    val image = uiState.selectedDetailImage ?: return
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(image.name, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.closeDetail() }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F172A))
            )
        },
        bottomBar = {
             // Details Panel
             Column(
                 modifier = Modifier
                     .fillMaxWidth()
                     .background(Color(0xFF0F172A))
                     .navigationBarsPadding()
                     .heightIn(max = 400.dp)
                     .verticalScroll(rememberScrollState())
                     .padding(16.dp)
             ) {
                 Text("Details", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                 Spacer(modifier = Modifier.height(12.dp))
                 
                 Card(
                     colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                     shape = RoundedCornerShape(12.dp),
                     modifier = Modifier.fillMaxWidth()
                 ) {
                     Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                         // Original header
                         Text("ORIGINAL", color = Color(0xFF64748B), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                         Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                             Text("Size / Format", color = Color(0xFF94A3B8))
                             Text("${image.originalSize} • ${image.originalType.substringAfter("/")}", color = Color.White) 
                         }
                         Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                             Text("Resolution", color = Color(0xFF94A3B8))
                             Text(image.originalResolution, color = Color.White) 
                         }
                         
                         androidx.compose.material3.HorizontalDivider(color = Color(0xFF334155))
                         
                         // Converted header
                         Text("CONVERTED", color = Color(0xFF007AFF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                         Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                             Text("Size / Format", color = Color(0xFF94A3B8))
                             Text("${image.size} • ${image.type.substringAfter("/")}", color = Color.White, fontWeight = FontWeight.Bold) 
                         }
                          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                             Text("Resolution", color = Color(0xFF94A3B8))
                             Text(image.resolution, color = Color.White, fontWeight = FontWeight.Bold) 
                         }
                     }
                 }

                 
                 Spacer(modifier = Modifier.height(24.dp)) // Increased gap
            }
        },
        containerColor = Color(0xFF0F172A)
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
