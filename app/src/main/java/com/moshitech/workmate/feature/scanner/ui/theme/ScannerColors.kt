package com.moshitech.workmate.feature.scanner.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Standard scanner app color palette
 * Based on industry standards (CamScanner, Adobe Scan, etc.)
 */
object ScannerColors {
    // Primary Colors (Teal/Cyan - Industry Standard)
    val Primary = Color(0xFF00BCD4)           // Teal
    val PrimaryDark = Color(0xFF0097A7)       // Dark Teal
    val PrimaryLight = Color(0xFF4DD0E1)      // Light Teal
    
    // Background & Surfaces
    val Background = Color(0xFF121212)        // Dark background
    val Surface = Color(0xFF1E1E1E)           // Card surfaces
    val SurfaceVariant = Color(0xFF2C2C2C)    // Elevated surfaces
    
    // Text Colors
    val OnPrimary = Color(0xFFFFFFFF)         // White on teal
    val OnBackground = Color(0xFFFFFFFF)      // White text
    val OnSurface = Color(0xFFFFFFFF)         // White on dark
    val OnSurfaceVariant = Color(0xFFB0B0B0) // Gray text
    
    // Detection States
    val DetectionActive = Color(0xFF00E676)   // Bright green (detected)
    val DetectionSearching = Color(0xFFFFC107) // Yellow (searching)
    val DetectionError = Color(0xFFFF5252)    // Red (error/no doc)
    
    // Functional Colors
    val Success = Color(0xFF4CAF50)           // Success green
    val Warning = Color(0xFFFF9800)           // Warning orange
    val Error = Color(0xFFFF5252)             // Error red
    val Info = Color(0xFF2196F3)              // Info blue
    
    // Filter Colors
    val FilterActive = Color(0xFF00BCD4)      // Active filter (teal)
    val FilterInactive = Color(0xFF757575)    // Inactive filter (gray)
    
    // Button States
    val ButtonPrimary = Color(0xFF00BCD4)     // Primary button
    val ButtonSecondary = Color(0xFF424242)   // Secondary button
    val ButtonDisabled = Color(0xFF616161)    // Disabled button
}
