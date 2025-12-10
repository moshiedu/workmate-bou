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
import androidx.compose.material.icons.outlined.Info
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BatchConverterScreen(
    navController: NavController,
    incomingUris: String? = null,
    viewModel: BatchConverterViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    
    var showInfoModal by remember { androidx.compose.runtime.mutableStateOf(false) }
    var imageDetails by remember { androidx.compose.runtime.mutableStateOf<com.moshitech.workmate.feature.imagestudio.viewmodel.BatchConverterViewModel.ImageDetails?>(null) }
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    val multiplePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris -> viewModel.onImagesSelected(uris) }
    )

    // Load Incoming URIs
    LaunchedEffect(incomingUris) {
        if (!incomingUris.isNullOrEmpty()) {
            val type = object : com.google.gson.reflect.TypeToken<List<String>>() {}.type
            // Try parsing as JSON first if possible, or fall back to comma split if simple
            // But since we joined with comma in source, we split by comma
            try {
                val uris = incomingUris.split(",").map { Uri.parse(Uri.decode(it)) }
                viewModel.onImagesSelected(uris)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // State for Preview Caption
    var previewDetails by remember { androidx.compose.runtime.mutableStateOf<com.moshitech.workmate.feature.imagestudio.viewmodel.BatchConverterViewModel.ImageDetails?>(null) }

    LaunchedEffect(uiState.selectedImages) {
        if (uiState.selectedImages.isNotEmpty()) {
            previewDetails = viewModel.getImageDetails(uiState.selectedImages.first())
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                if (uiState.selectedImages.isNotEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(uiState.selectedImages.first())
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
                                    imageDetails = viewModel.getImageDetails(uiState.selectedImages.first())
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
                    Box(modifier = Modifier.size(60.dp)) {
                         AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current).data(uri).build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp))
                        )
                        // Remove button
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .clickable { viewModel.removeImage(uri) }
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
                        .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Batch Conversion: ${uiState.selectedImages.size} images selected",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }

            // 4. Output Settings
            Text("Output Format", color = Color(0xFF94A3B8), fontSize = 14.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CompressFormat.values().forEach { format ->
                    val isSelected = uiState.format == format
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Color(0xFF007AFF) else Color(0xFF334155))
                            .clickable { viewModel.updateFormat(format) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(format.name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
            

            
            // Dimensions Section
            Text("Dimensions", color = Color(0xFF94A3B8), fontSize = 14.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Width Input
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text("Width", color = Color(0xFF64748B), fontSize = 10.sp)
                        androidx.compose.foundation.text.BasicTextField(
                            value = uiState.width,
                            onValueChange = viewModel::updateWidth,
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 16.sp),
                            singleLine = true,
                            cursorBrush = androidx.compose.ui.graphics.SolidColor(Color(0xFF007AFF)),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Text("px", color = Color(0xFF64748B), fontSize = 12.sp, modifier = Modifier.align(Alignment.BottomEnd))
                }

                // Height Input
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text("Height", color = Color(0xFF64748B), fontSize = 10.sp)
                        androidx.compose.foundation.text.BasicTextField(
                            value = uiState.height,
                            onValueChange = viewModel::updateHeight,
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 16.sp),
                            singleLine = true,
                            cursorBrush = androidx.compose.ui.graphics.SolidColor(Color(0xFF007AFF)),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Text("px", color = Color(0xFF64748B), fontSize = 12.sp, modifier = Modifier.align(Alignment.BottomEnd))
                }

                // Link/Constraint Toggle
                Box(
                    modifier = Modifier
                        .size(56.dp)
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

            Text("Target File Size (Optional)", color = Color(0xFF94A3B8), fontSize = 14.sp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(Color(0xFF1E293B), RoundedCornerShape(8.dp)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.foundation.text.BasicTextField(
                    value = uiState.targetSize,
                    onValueChange = viewModel::updateTargetSize,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 16.sp),
                    singleLine = true,
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(Color(0xFF007AFF)),
                    decorationBox = { innerTextField ->
                        if (uiState.targetSize.isEmpty()) {
                             Text("Max Size", color = Color(0xFF475569))
                        }
                        innerTextField()
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                
                // Unit Toggle
                Row(
                   modifier = Modifier
                       .padding(4.dp)
                       .background(Color(0xFF334155), RoundedCornerShape(6.dp)) 
                ) {
                    Box(
                        modifier = Modifier
                            .background(if (!uiState.isTargetSizeInMb) Color(0xFF007AFF) else Color.Transparent, RoundedCornerShape(6.dp))
                            .clickable { if (uiState.isTargetSizeInMb) viewModel.toggleTargetSizeUnit() }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                         Text("KB", color = if (!uiState.isTargetSizeInMb) Color.White else Color(0xFF94A3B8), fontSize = 12.sp)
                    }
                    Box(
                        modifier = Modifier
                            .background(if (uiState.isTargetSizeInMb) Color(0xFF007AFF) else Color.Transparent, RoundedCornerShape(6.dp))
                            .clickable { if (!uiState.isTargetSizeInMb) viewModel.toggleTargetSizeUnit() }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                         Text("MB", color = if (uiState.isTargetSizeInMb) Color.White else Color(0xFF94A3B8), fontSize = 12.sp)
                    }
                }
            }
            
            Text("Overrides Quality slider below.", color = Color(0xFF64748B), fontSize = 12.sp)

            Spacer(modifier = Modifier.height(16.dp))

            Spacer(modifier = Modifier.height(16.dp))

            // Quality Slider
            val isQualityDisabled = uiState.targetSize.isNotBlank()
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Quality", color = if (isQualityDisabled) Color(0xFF64748B) else Color(0xFF94A3B8), fontSize = 14.sp)
                    Text("${uiState.quality}%", color = if (isQualityDisabled) Color(0xFF64748B) else Color.White, fontSize = 14.sp)
                }
                
                Slider(
                    value = uiState.quality.toFloat(),
                    onValueChange = { viewModel.updateQuality(it.toInt()) },
                    valueRange = 0f..100f,
                    enabled = !isQualityDisabled,
                    modifier = Modifier.padding(vertical = 4.dp), // Small vertical padding for thumb clearance
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
