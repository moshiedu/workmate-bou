package com.moshitech.workmate.feature.scanner.ui.home

import android.app.Activity
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.mlkit.vision.documentscanner.GmsDocumentScanner
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.moshitech.workmate.feature.scanner.domain.model.ScannerConfig
import com.moshitech.workmate.feature.scanner.domain.model.ScannerFeature
import com.moshitech.workmate.feature.scanner.domain.model.ScannedDocument
import com.moshitech.workmate.feature.scanner.ui.components.*
import com.moshitech.workmate.feature.scanner.ui.theme.ScannerColors
import java.time.LocalDateTime

/**
 * Scanner home screen with ML Kit integration
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerHomeScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    
    // Scanner preferences
    val scannerPrefs = remember { 
        com.moshitech.workmate.feature.scanner.data.preferences.ScannerPreferences(context)
    }
    
    // State
    var searchQuery by remember { mutableStateOf("") }
    var showSettings by remember { mutableStateOf(false) }
    var showPostScanActions by remember { mutableStateOf(false) }
    var currentScannedDocument by remember { mutableStateOf<ScannedDocument?>(null) }
    var scannerConfig by remember { 
        mutableStateOf(scannerPrefs.loadConfig()) // Load saved settings
    }
    var showToast by remember { mutableStateOf<String?>(null) }
    
    val features = remember { ScannerFeature.getAllFeatures() }
    
    // Mock recent documents (will be replaced with real data)
    val recentDocuments = remember {
        mutableStateListOf<ScannedDocument>()
    }
    
    // ML Kit scanner launcher
    val scannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val scanningResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
            scanningResult?.let { scanResult ->
                // Get PDF
                val pdfUri = scanResult.pdf?.uri
                val pageCount = scanResult.pages?.size ?: 0
                
                // Create document entry
                val newDoc = ScannedDocument(
                    id = System.currentTimeMillis().toString(),
                    name = "Scan ${LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))}",
                    thumbnailPath = pdfUri?.toString(),
                    filePath = pdfUri?.toString() ?: "",
                    pageCount = pageCount,
                    createdAt = LocalDateTime.now(),
                    modifiedAt = LocalDateTime.now(),
                    sizeInBytes = 0 // Will be calculated later
                )
                
                // Show post-scan action sheet
                currentScannedDocument = newDoc
                showPostScanActions = true
            }
        }
    }
    
    // Launch ML Kit scanner
    fun launchScanner(config: ScannerConfig) {
        android.util.Log.d("ScannerHome", "Launching scanner with config: autoCapture=${config.autoCapture}, batchMode=${config.batchMode}")
        
        val scannerMode = if (config.autoCapture) {
            android.util.Log.d("ScannerHome", "Using SCANNER_MODE_FULL (auto-capture)")
            GmsDocumentScannerOptions.SCANNER_MODE_FULL
        } else {
            android.util.Log.d("ScannerHome", "Using SCANNER_MODE_BASE (manual)")
            GmsDocumentScannerOptions.SCANNER_MODE_BASE
        }
        
        val options = GmsDocumentScannerOptions.Builder()
            .setScannerMode(scannerMode)
            .setPageLimit(if (config.batchMode) 10 else 1)
            .setGalleryImportAllowed(true)
            .setResultFormats(
                GmsDocumentScannerOptions.RESULT_FORMAT_JPEG,
                GmsDocumentScannerOptions.RESULT_FORMAT_PDF
            )
            .build()
        
        val scanner = GmsDocumentScanning.getClient(options)
        scanner.getStartScanIntent(context as Activity)
            .addOnSuccessListener { intentSender ->
                scannerLauncher.launch(
                    IntentSenderRequest.Builder(intentSender).build()
                )
            }
            .addOnFailureListener { exception ->
                showToast = "Failed to start scanner: ${exception.message}"
            }
    }
    
    // Share document
    fun shareDocument(document: ScannedDocument) {
        try {
            val uri = android.net.Uri.parse(document.filePath)
            val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(
                android.content.Intent.createChooser(shareIntent, "Share Document")
            )
            showToast = "Opening share menu..."
        } catch (e: Exception) {
            showToast = "Failed to share: ${e.message}"
        }
    }
    
    // Export document to Downloads
    fun exportDocument(document: ScannedDocument) {
        try {
            val uri = android.net.Uri.parse(document.filePath)
            val contentResolver = context.contentResolver
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                // Android 10+ - Use MediaStore
                val values = android.content.ContentValues().apply {
                    put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, "${document.name}.pdf")
                    put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOWNLOADS)
                }
                
                val collection = android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI
                val itemUri = contentResolver.insert(collection, values)
                
                itemUri?.let { outputUri ->
                    contentResolver.openInputStream(uri)?.use { input ->
                        contentResolver.openOutputStream(outputUri)?.use { output ->
                            input.copyTo(output)
                        }
                    }
                    showToast = "Exported to Downloads/${document.name}.pdf"
                } ?: run {
                    showToast = "Failed to create file in Downloads"
                }
            } else {
                // Android 9 and below - Direct file access
                val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(
                    android.os.Environment.DIRECTORY_DOWNLOADS
                )
                val fileName = "${document.name}.pdf"
                val destFile = java.io.File(downloadsDir, fileName)
                
                contentResolver.openInputStream(uri)?.use { input ->
                    java.io.FileOutputStream(destFile).use { output ->
                        input.copyTo(output)
                    }
                }
                
                // Notify media scanner
                android.media.MediaScannerConnection.scanFile(
                    context,
                    arrayOf(destFile.absolutePath),
                    arrayOf("application/pdf"),
                    null
                )
                
                showToast = "Exported to Downloads/$fileName"
            }
        } catch (e: Exception) {
            showToast = "Export failed: ${e.message}"
            android.util.Log.e("ScannerHome", "Export error", e)
        }
    }
    
    // View document
    fun viewDocument(document: ScannedDocument) {
        try {
            val uri = android.net.Uri.parse(document.filePath)
            val viewIntent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(viewIntent)
        } catch (e: Exception) {
            showToast = "No PDF viewer found. Please install one."
        }
    }
    
    // Settings sheet
    if (showSettings) {
        ScannerSettingsSheet(
            config = scannerConfig,
            onConfigChange = { newConfig ->
                // Update config and save to preferences
                scannerConfig = newConfig
                scannerPrefs.saveConfig(newConfig)
            },
            onStartScan = {
                // Launch scanner when user clicks "Start Scanning"
                showSettings = false
                launchScanner(scannerConfig)
            },
            onDismiss = { showSettings = false }
        )
    }
    
    // Post-scan action sheet
    currentScannedDocument?.let { document ->
        if (showPostScanActions) {
            PostScanActionSheet(
                document = document,
                onView = { viewDocument(document) },
                onExport = { exportDocument(document) },
                onShare = { shareDocument(document) },
                onSaveToLibrary = {
                    // Add to recents list
                    recentDocuments.add(0, document)
                    showToast = "Document saved to library!"
                },
                onDismiss = {
                    showPostScanActions = false
                    currentScannedDocument = null
                }
            )
        }
    }
    
    // Toast message
    showToast?.let { message ->
        LaunchedEffect(message) {
            kotlinx.coroutines.delay(3000)
            showToast = null
        }
        
        Snackbar(
            modifier = Modifier.padding(16.dp),
            containerColor = ScannerColors.Success,
            contentColor = ScannerColors.OnPrimary
        ) {
            Text(message)
        }
    }
    
    Scaffold(
        containerColor = ScannerColors.Background,
        floatingActionButton = {
            // FAB with settings button
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Scanner settings button (smaller, above FAB)
                FloatingActionButton(
                    onClick = { showSettings = true },
                    containerColor = ScannerColors.SurfaceVariant,
                    contentColor = ScannerColors.Primary,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune, // Camera settings icon
                        contentDescription = "Camera Settings",
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                // Main scan button
                FloatingActionButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        launchScanner(scannerConfig)
                    },
                    containerColor = ScannerColors.Primary,
                    contentColor = ScannerColors.OnPrimary,
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Scan Document",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(ScannerColors.Background)
                .padding(paddingValues)
                .windowInsetsPadding(androidx.compose.foundation.layout.WindowInsets.systemBars),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Title
            item {
                Text(
                    text = "Document Scanner",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = ScannerColors.OnSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            // Search bar
            item {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it }
                )
            }
            
            // Feature grid
            item {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier.height(280.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(features) { feature ->
                        FeatureCard(
                            feature = feature,
                            onClick = {
                                if (feature is ScannerFeature.SmartScan) {
                                    // Launch scanner directly
                                    launchScanner(scannerConfig)
                                } else {
                                    feature.route?.let { navController.navigate(it) }
                                }
                            }
                        )
                    }
                }
            }
            
            // Recents section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recents",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = ScannerColors.OnSurface
                    )
                    
                    TextButton(onClick = { /* Navigate to all documents */ }) {
                        Text(
                            "View All",
                            color = ScannerColors.Primary
                        )
                    }
                }
            }
            
            // Recent documents or empty state
            if (recentDocuments.isEmpty()) {
                item {
                    EmptyState(
                        title = "No Documents Yet",
                        message = "Start scanning documents by tapping the camera button below",
                        actionLabel = "Start Scanning",
                        onActionClick = {
                            showSettings = true
                        }
                    )
                }
            } else {
                items(recentDocuments) { document ->
                    DocumentCard(
                        document = document,
                        onView = { viewDocument(document) },
                        onShare = { shareDocument(document) },
                        onExport = { exportDocument(document) }
                    )
                }
            }
        }
    }
}
