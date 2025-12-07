package com.moshitech.workmate.feature.scanner.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.moshitech.workmate.ui.theme.*

/**
 * Represents scanner features on home screen
 */
sealed class ScannerFeature(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val iconColor: Color,
    val route: String? = null
) {
    object SmartScan : ScannerFeature(
        id = "smart_scan",
        title = "Smart Scan",
        icon = Icons.Default.DocumentScanner,
        iconColor = Primary,
        route = "scanner/camera"
    )
    
    object PdfTools : ScannerFeature(
        id = "pdf_tools",
        title = "PDF Tools",
        icon = Icons.Default.PictureAsPdf,
        iconColor = Error,
        route = "scanner/pdf_tools"
    )
    
    object ImportImages : ScannerFeature(
        id = "import_images",
        title = "Import Images",
        icon = Icons.Default.Image,
        iconColor = Primary,
        route = "scanner/import_images"
    )
    
    object ImportFiles : ScannerFeature(
        id = "import_files",
        title = "Import Files",
        icon = Icons.Default.Folder,
        iconColor = Primary,
        route = "scanner/import_files"
    )
    
    object IdCards : ScannerFeature(
        id = "id_cards",
        title = "ID Cards",
        icon = Icons.Default.CreditCard,
        iconColor = Accent,
        route = "scanner/id_cards"
    )
    
    object ExtractText : ScannerFeature(
        id = "extract_text",
        title = "Extract Text",
        icon = Icons.Default.TextFields,
        iconColor = Accent,
        route = "scanner/ocr"
    )
    
    object SolverAI : ScannerFeature(
        id = "solver_ai",
        title = "Solver AI",
        icon = Icons.Default.AutoAwesome,
        iconColor = Primary,
        route = "scanner/ai_solver"
    )
    
    object All : ScannerFeature(
        id = "all",
        title = "All",
        icon = Icons.Default.Apps,
        iconColor = Info,
        route = "scanner/all_features"
    )
    
    companion object {
        fun getAllFeatures() = listOf(
            SmartScan,
            PdfTools,
            ImportImages,
            ImportFiles,
            IdCards,
            ExtractText,
            SolverAI,
            All
        )
    }
}
