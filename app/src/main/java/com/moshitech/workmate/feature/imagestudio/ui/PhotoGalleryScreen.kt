package com.moshitech.workmate.feature.imagestudio.ui

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import com.moshitech.workmate.feature.imagestudio.viewmodel.MediaItem
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Transform
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.moshitech.workmate.feature.imagestudio.viewmodel.PhotoGalleryViewModel

// Colors from design
private val DarkBackground = Color(0xFF0F172A) // Dark Navy/Black like in screenshot
private val PrimaryBlue = Color(0xFF007AFF)
private val SecondaryDark = Color(0xFF1E293B)
private val TextWhite = Color.White
private val TextGrey = Color(0xFF94A3B8)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoGalleryScreen(
    navController: NavController,
    viewModel: PhotoGalleryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Check if essential permissions are granted
        val cameraGranted = permissions[Manifest.permission.CAMERA] == true
        val storageGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions[Manifest.permission.READ_MEDIA_IMAGES] == true
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true
        } else {
            // Pre-Q requires Write to save, taking photo requires write.
            permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] == true
        }
        
        viewModel.updatePermissionStatus(storageGranted) 
    }

    // Camera Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        viewModel.onCameraResult(success)
    }

    // Device Picker Launcher
    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
              val encodedUri = android.net.Uri.encode(uri.toString())
              navController.navigate("${com.moshitech.workmate.navigation.Screen.PhotoEditor.route}?uri=$encodedUri")
        }
    }

    // Permission Lifecycle Observer
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                        viewModel.updatePermissionStatus(true)
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                        viewModel.updatePermissionStatus(true)
                    }
                } else {
                     if (androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                        viewModel.updatePermissionStatus(true)
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        val permissionsToRequest = mutableListOf<String>()
        
        // Storage permissions logic (omitted for brevity, assume unchanged logic flow for this block)
        // Re-inserting the exact logic to be safe since it's a replace block
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES)
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        } else {
             if (androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
        
        // Camera
        if (androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.CAMERA)
        }

        if (permissionsToRequest.isEmpty()) {
            viewModel.updatePermissionStatus(true)
        } else {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    val editorEnabled = uiState.selectedImages.size == 1

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            if (uiState.isSelectionMode) {
                // ... (Selection Top Bar)
                TopAppBar(
                    title = { 
                        Text(
                            if (uiState.selectedImages.isEmpty()) "Select Images" else "${uiState.selectedImages.size} Selected", 
                            color = TextWhite, 
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp
                        ) 
                    },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.setSelectionMode(false) }) {
                            Icon(Icons.Default.Close, contentDescription = "Close Selection", tint = TextWhite)
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.showDeleteConfirmation() }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextWhite)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryBlue)
                )
            } else {
                // ... (Standard Top Bar)
                TopAppBar(
                    title = { Text("Photo Studio", color = TextWhite, fontWeight = FontWeight.SemiBold) },
                    actions = {
                        IconButton(onClick = { viewModel.setSelectionMode(true) }) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = "Select", tint = TextWhite)
                        }
                        IconButton(onClick = { 
                            if (androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                val uri = viewModel.createTempPictureUri()
                                if (uri != null) {
                                    cameraLauncher.launch(uri)
                                }
                            } else {
                                permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
                            }
                        }) {
                            Icon(Icons.Filled.CameraAlt, contentDescription = "Camera", tint = TextWhite)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
                )
            }
        },
        bottomBar = {
            PhotoStudioBottomBar(
                currentTab = "Gallery",
                isEditorEnabled = editorEnabled,
                onTabSelected = { tab ->
                     when (tab) {
                        "Editor" -> {
                             // This block will only be reachable if enabled (size == 1)
                             if (uiState.selectedImages.size == 1) {
                                 val uri = uiState.selectedImages.first()
                                 val encodedUri = android.net.Uri.encode(uri.toString())
                                 navController.navigate("${com.moshitech.workmate.navigation.Screen.PhotoEditor.route}?uri=$encodedUri")
                             }
                        }
                        "Convert" -> {
                             val route = if (uiState.selectedImages.isNotEmpty()) {
                                 val uris = uiState.selectedImages.joinToString(",") { android.net.Uri.encode(it.toString()) }
                                 "${com.moshitech.workmate.navigation.Screen.BatchConverter.route}?uris=$uris"
                             } else {
                                 com.moshitech.workmate.navigation.Screen.BatchConverter.route
                             }
                            navController.navigate(route)
                        }
                        "Share" -> {
                             if (uiState.selectedImages.isNotEmpty()) {
                                 val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND_MULTIPLE).apply {
                                     putParcelableArrayListExtra(android.content.Intent.EXTRA_STREAM, ArrayList(uiState.selectedImages))
                                     type = "image/*"
                                     addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                 }
                                 context.startActivity(android.content.Intent.createChooser(shareIntent, "Share images"))
                             } else {
                                 android.widget.Toast.makeText(context, "Select images to share", android.widget.Toast.LENGTH_SHORT).show()
                             }
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(DarkBackground)
        ) {
            if (!uiState.permissionGranted) {
                // Screen 3: Permission Denied / Access Request
                PermissionRequestState(
                    onAllowAccess = {
                        val permissions = mutableListOf<String>()
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                        } else {
                            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        }
                        permissions.add(Manifest.permission.CAMERA)
                        
                        // Just launch request. Logic for permanent denial handles via "Open Settings" button.
                        permissionLauncher.launch(permissions.toTypedArray())
                    }
                )
            } else if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally), color = PrimaryBlue)
            } else if (uiState.allImages.isEmpty() && !uiState.isLoading) {
                // Screen 2: Empty State
                EmptyGalleryState(
                    onTakePhoto = {
                         if (androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                            val uri = viewModel.createTempPictureUri()
                            if (uri != null) {
                                cameraLauncher.launch(uri)
                            }
                        } else {
                            permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
                        }
                    },
                    onBrowse = { pickerLauncher.launch(androidx.activity.result.PickVisualMediaRequest(androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                )
            } else {
                // Search & Filter Section
                if (uiState.allImages.isNotEmpty()) {
                    GallerySearchAndFilters(
                        query = uiState.searchQuery,
                        onQueryChange = viewModel::onSearchQueryChanged,
                        filters = uiState.availableFilters,
                        selectedFilter = uiState.selectedFilter,
                        onFilterSelected = viewModel::onFilterSelected,
                        onSortSelected = viewModel::onSortOptionSelected
                    )
                }
                
                // Screen 1 & 4: Gallery Grid
                GalleryGrid(
                    images = uiState.filteredImages,
                    selectedImages = uiState.selectedImages,
                    isSelectionMode = uiState.isSelectionMode,
                    onImageClick = { uri ->
                        if (uiState.isSelectionMode) {
                            viewModel.toggleSelection(uri)
                        } else {
                            val encodedUri = android.net.Uri.encode(uri.toString())
                            navController.navigate("${com.moshitech.workmate.navigation.Screen.PhotoEditor.route}?uri=$encodedUri")
                        }
                    },
                    onImageLongClick = { uri ->
                        viewModel.toggleSelection(uri)
                    }
                )
            }
        }
    }
    
    // Delete Confirmation Dialog
    if (uiState.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteDialog() },
            title = { Text("Delete ${uiState.selectedImages.size} images?", color = TextWhite) },
            text = { Text("Are you sure you want to delete these images? This action cannot be undone.", color = TextGrey) },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteSelectedImages() }) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDeleteDialog() }) {
                    Text("Cancel", color = TextGrey)
                }
            },
            containerColor = SecondaryDark,
            titleContentColor = TextWhite,
            textContentColor = TextGrey
        )
    }


}

@Composable
fun PermissionRequestState(onAllowAccess: () -> Unit) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Lock,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = PrimaryBlue
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Allow Photo & Camera Access",
            style = MaterialTheme.typography.headlineSmall,
            color = TextWhite,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "Workmate needs access to your photos and camera to edit and convert images.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextGrey,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onAllowAccess,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Allow Access", fontWeight = FontWeight.Medium)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Settings Fallback Button
        TextButton(
            onClick = {
                val intent = android.content.Intent(
                    android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    android.net.Uri.fromParts("package", context.packageName, null)
                )
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Open Settings", color = PrimaryBlue)
        }
    }
}



@Composable
fun EmptyGalleryState(
    onTakePhoto: () -> Unit,
    onBrowse: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.CameraAlt,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = PrimaryBlue
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Your Gallery is Empty",
            style = MaterialTheme.typography.headlineSmall,
            color = TextWhite,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Start by taking a new photo or importing one from your device.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextGrey,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(48.dp))
        
        // Take Photo Button
        Button(
            onClick = onTakePhoto,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.size(8.dp))
            Text("Take a Photo")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Browse Device Button
        Button(
            onClick = onBrowse,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SecondaryDark),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Outlined.FolderOpen, contentDescription = null, modifier = Modifier.size(20.dp), tint = PrimaryBlue)
            Spacer(modifier = Modifier.size(8.dp))
            Text("Browse Device", color = PrimaryBlue)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GallerySearchAndFilters(
    query: String,
    onQueryChange: (String) -> Unit,
    filters: List<String>,
    selectedFilter: String,
    onFilterSelected: (String) -> Unit,
    onSortSelected: (com.moshitech.workmate.feature.imagestudio.viewmodel.SortOption) -> Unit
) {
    var showSortMenu by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkBackground)
            .padding(bottom = 8.dp)
    ) {
        // Search Bar Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .background(SecondaryDark, RoundedCornerShape(8.dp))
                    .border(1.dp, SecondaryDark, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp),
                textStyle = TextStyle(color = TextWhite, fontSize = 13.sp),
                singleLine = true,
                cursorBrush = SolidColor(PrimaryBlue),
                decorationBox = { innerTextField ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            Icons.Default.Search, 
                            contentDescription = "Search", 
                            tint = TextGrey,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            if (query.isEmpty()) {
                                Text("Search photos...", color = TextGrey, fontSize = 13.sp)
                            }
                            innerTextField()
                        }
                    }
                }
            )
            
            Spacer(modifier = Modifier.size(8.dp))
            
            // Filter/Sort Icon Button
            Box {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(SecondaryDark, RoundedCornerShape(8.dp))
                        .border(1.dp, SecondaryDark, RoundedCornerShape(8.dp))
                        .clickable { showSortMenu = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Tune,
                        contentDescription = "Sort",
                        tint = TextGrey,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                androidx.compose.material3.DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false },
                    modifier = Modifier.background(SecondaryDark)
                ) {
                    com.moshitech.workmate.feature.imagestudio.viewmodel.SortOption.values().forEach { option ->
                        val label = when (option) {
                            com.moshitech.workmate.feature.imagestudio.viewmodel.SortOption.DATE_DESC -> "Date (Newest)"
                            com.moshitech.workmate.feature.imagestudio.viewmodel.SortOption.DATE_ASC -> "Date (Oldest)"
                            com.moshitech.workmate.feature.imagestudio.viewmodel.SortOption.NAME_ASC -> "Name (A-Z)"
                            com.moshitech.workmate.feature.imagestudio.viewmodel.SortOption.NAME_DESC -> "Name (Z-A)"
                        }
                        androidx.compose.material3.DropdownMenuItem(
                            text = { Text(label, color = TextWhite) },
                            onClick = {
                                onSortSelected(option)
                                showSortMenu = false
                            }
                        )
                    }
                }
            }
        }
        
        // Filter Chips Row
        LazyRow(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(filters) { filter ->
                val isSelected = filter == selectedFilter
                FilterChip(
                    selected = isSelected,
                    onClick = { onFilterSelected(filter) },
                    label = { Text(filter, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryBlue.copy(alpha = 0.2f),
                        selectedLabelColor = PrimaryBlue,
                        containerColor = SecondaryDark,
                        labelColor = TextGrey
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = Color.Transparent,
                        selectedBorderColor = PrimaryBlue,
                         enabled = true,
                         selected = isSelected
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(32.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GalleryGrid(
    images: List<MediaItem>,
    selectedImages: Set<Uri>,
    isSelectionMode: Boolean,
    onImageClick: (Uri) -> Unit,
    onImageLongClick: (Uri) -> Unit
) {
    if (images.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No photos found", color = TextGrey)
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(images, key = { it.uri.toString() }) { item ->
                val uri = item.uri
                val isSelected = selectedImages.contains(uri)
                Box(Modifier.aspectRatio(1f)) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            ImageRequest.Builder(LocalContext.current).data(uri).build()
                        ),
                        contentDescription = item.displayName, // Use actual name for better a11y
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .combinedClickable(
                                onClick = { onImageClick(uri) },
                                onLongClick = { onImageLongClick(uri) }
                            )
                    )
                    
                    // Selection Indicator (Same as before)
                    if (isSelectionMode) {
                         Box(
                             modifier = Modifier
                                 .fillMaxSize()
                                 .background(if (isSelected) PrimaryBlue.copy(alpha = 0.3f) else Color.Transparent)
                                 .padding(8.dp),
                             contentAlignment = Alignment.TopEnd
                         ) {
                             if (isSelected) {
                                 Icon(
                                     Icons.Filled.CheckCircle,
                                     contentDescription = "Selected",
                                     tint = PrimaryBlue,
                                     modifier = Modifier.background(Color.White, CircleShape).border(1.dp, Color.White, CircleShape)
                                 )
                             } else {
                                 Box(
                                     modifier = Modifier
                                         .size(24.dp)
                                         .border(2.dp, Color.White, CircleShape)
                                         .background(Color.Black.copy(alpha=0.3f), CircleShape)
                                 )
                             }
                         }
                    }
                }
            }
        }
    }
}

@Composable
fun PhotoStudioBottomBar(
    currentTab: String,
    isEditorEnabled: Boolean = true,
    onTabSelected: (String) -> Unit
) {
    NavigationBar(
        containerColor = DarkBackground,
        contentColor = TextGrey
    ) {
        NavigationBarItem(
            selected = currentTab == "Gallery",
            onClick = { onTabSelected("Gallery") },
            icon = { Icon(Icons.Filled.Collections, contentDescription = "Gallery") },
            label = { Text("Gallery") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryBlue,
                selectedTextColor = PrimaryBlue,
                unselectedIconColor = TextGrey,
                unselectedTextColor = TextGrey,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = currentTab == "Editor",
            enabled = isEditorEnabled,
            onClick = { onTabSelected("Editor") },
            icon = { Icon(Icons.Outlined.AutoAwesome, contentDescription = "Editor") },
            label = { Text("Editor") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryBlue,
                selectedTextColor = PrimaryBlue,
                unselectedIconColor = TextGrey,
                unselectedTextColor = TextGrey,
                disabledIconColor = TextGrey.copy(alpha = 0.4f),
                disabledTextColor = TextGrey.copy(alpha = 0.4f),
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = currentTab == "Convert",
            onClick = { onTabSelected("Convert") },
            icon = { Icon(Icons.Outlined.Transform, contentDescription = "Convert") },
            label = { Text("Convert") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryBlue,
                selectedTextColor = PrimaryBlue,
                unselectedIconColor = TextGrey,
                unselectedTextColor = TextGrey,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = currentTab == "Share",
            onClick = { onTabSelected("Share") },
            icon = { Icon(Icons.Outlined.Share, contentDescription = "Share") },
            label = { Text("Share") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryBlue,
                selectedTextColor = PrimaryBlue,
                unselectedIconColor = TextGrey,
                unselectedTextColor = TextGrey,
                indicatorColor = Color.Transparent
            )
        )
    }
}
