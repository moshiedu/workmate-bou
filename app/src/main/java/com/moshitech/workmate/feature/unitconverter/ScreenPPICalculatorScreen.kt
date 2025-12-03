package com.moshitech.workmate.feature.unitconverter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenPPICalculatorScreen(
    navController: NavController
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val backgroundColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF8F9FA)
    val textColor = if (isDark) Color.White else Color(0xFF111827)
    val secondaryTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF6B7280)
    val cardColor = if (isDark) Color(0xFF1E293B) else Color.White
    val borderColor = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
    val primaryBlue = Color(0xFF1976D2)

    var diagonal by remember { mutableStateOf("") }
    var width by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var showInfoDialog by remember { mutableStateOf(false) }

    var ppiResult by remember { mutableStateOf<Double?>(null) }
    var aspectRatio by remember { mutableStateOf<String?>(null) }
    var totalPixels by remember { mutableStateOf<Double?>(null) }

    fun gcd(a: Int, b: Int): Int {
        return if (b == 0) a else gcd(b, a % b)
    }

    fun calculatePPI() {
        val d = diagonal.toDoubleOrNull()
        val w = width.toIntOrNull()
        val h = height.toIntOrNull()

        if (d == null || d <= 0 || w == null || w <= 0 || h == null || h <= 0) {
            ppiResult = null
            aspectRatio = null
            totalPixels = null
            return
        }

        // Calculate PPI
        val diagonalPixels = sqrt((w * w + h * h).toDouble())
        ppiResult = diagonalPixels / d

        // Calculate aspect ratio
        val divisor = gcd(w, h)
        aspectRatio = "${w / divisor}:${h / divisor}"

        // Calculate total pixels (in megapixels)
        totalPixels = (w * h) / 1_000_000.0
    }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Screen PPI Calculator",
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = textColor)
                    }
                },
                actions = {
                    IconButton(onClick = { showInfoDialog = true }) {
                        Icon(Icons.Default.Info, "Info", tint = textColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Diagonal Input
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, borderColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.PhoneAndroid,
                            null,
                            tint = primaryBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Diagonal Size",
                            color = secondaryTextColor,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = diagonal,
                        onValueChange = { diagonal = it; calculatePPI() },
                        modifier = Modifier.fillMaxWidth(),
                        suffix = { Text("inches") },
                        placeholder = { Text("e.g., 6.1, 15.6, 27") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedBorderColor = primaryBlue,
                            unfocusedBorderColor = borderColor
                        )
                    )

                }

                Spacer(modifier = Modifier.height(16.dp))

                // Resolution Width Input
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, borderColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Resolution Width",
                            color = secondaryTextColor,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = width,
                            onValueChange = {
                                if (it.all { char -> char.isDigit() }) {
                                    width = it; calculatePPI()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            suffix = { Text("px") },
                            placeholder = { Text("e.g., 1920, 2560, 3840") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedBorderColor = primaryBlue,
                                unfocusedBorderColor = borderColor
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Resolution Height Input
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = cardColor),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, borderColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Resolution Height",
                                color = secondaryTextColor,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = height,
                                onValueChange = {
                                    if (it.all { char -> char.isDigit() }) {
                                        height = it; calculatePPI()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                suffix = { Text("px") },
                                placeholder = { Text("e.g., 1080, 1440, 2160") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedBorderColor = primaryBlue,
                                    unfocusedBorderColor = borderColor
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Results
                        ppiResult?.let { ppi ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = cardColor),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(24.dp)
                                        .fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        "Screen Specifications",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = textColor
                                    )

                                    Spacer(modifier = Modifier.height(24.dp))

                                    // PPI
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            "PPI (Pixels Per Inch)",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = secondaryTextColor
                                        )
                                        Text(
                                            "%.1f".format(ppi),
                                            style = MaterialTheme.typography.displayMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = primaryBlue
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))
                                    Divider(color = borderColor)
                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Aspect Ratio and Total Pixels
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                "Aspect Ratio",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = secondaryTextColor
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                aspectRatio ?: "-",
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = textColor
                                            )
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                "Total Pixels",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = secondaryTextColor
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                "%.2f MP".format(totalPixels),
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = textColor
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))
                                    Divider(color = borderColor)
                                    Spacer(modifier = Modifier.height(16.dp))

                                    // PPI Category
                                    val ppiCategory = when {
                                        ppi < 100 -> "Low Density" to Color(0xFFFF9800)
                                        ppi < 200 -> "Medium Density" to Color(0xFF2196F3)
                                        ppi < 300 -> "High Density" to Color(0xFF4CAF50)
                                        ppi < 400 -> "Very High Density (Retina)" to Color(
                                            0xFF9C27B0
                                        )

                                        else -> "Ultra High Density" to Color(0xFFE91E63)
                                    }

                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = ppiCategory.second.copy(
                                                alpha = 0.1f
                                            )
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = ppiCategory.first,
                                            modifier = Modifier.padding(
                                                horizontal = 16.dp,
                                                vertical = 8.dp
                                            ),
                                            color = ppiCategory.second,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (showInfoDialog) {
                        AlertDialog(
                            onDismissRequest = { showInfoDialog = false },
                            title = { Text("About Screen PPI") },
                            text = {
                                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                    Text(
                                        "PPI (Pixels Per Inch) measures screen pixel density.",
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("• Low (<100): Older monitors, TVs\n• Medium (100-200): Standard displays\n• High (200-300): Modern laptops, tablets\n• Retina (300-400): High-end phones, MacBooks\n• Ultra (>400): Flagship smartphones")
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        "Formula: PPI = √(width² + height²) / diagonal",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = secondaryTextColor
                                    )
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = { showInfoDialog = false }) {
                                    Text("Close")
                                }
                            },
                            containerColor = cardColor,
                            titleContentColor = textColor,
                            textContentColor = secondaryTextColor
                        )
                    }
                }
            }
        }
    }
}