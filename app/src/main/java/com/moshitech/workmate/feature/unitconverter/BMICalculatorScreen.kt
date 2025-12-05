package com.moshitech.workmate.feature.unitconverter

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlin.math.pow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BMICalculatorScreen(
    navController: NavController,
    viewModel: UnitConverterViewModel
) {
    val isFavorite by viewModel.isCurrentFavorite.collectAsState()
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val backgroundColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF8F9FA)
    val textColor = if (isDark) Color.White else Color(0xFF111827)
    val secondaryTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF6B7280)
    val cardColor = if (isDark) Color(0xFF1E293B) else Color.White
    val borderColor = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
    val primaryBlue = Color(0xFF1976D2)
    
    // Exact colors from reference image
    val colorUnderweight = Color(0xFF03A9F4) // Cyan/Blue
    val colorNormal = Color(0xFF76FF03)      // Bright Green
    val colorOverweight = Color(0xFFFF9800)  // Orange
    val colorObese = Color(0xFFF44336)       // Red
    val colorHub = Color(0xFFE1BEE7)         // Light Purple
    val colorBottomBar = Color(0xFF4A148C)   // Dark Purple

    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    
    // Unit States
    var weightUnit by remember { mutableStateOf("kg") } // "kg", "lb"
    var heightUnit by remember { mutableStateOf("cm") } // "cm", "m", "ft+in"
    
    var isMale by remember { mutableStateOf(true) }
    var heightFt by remember { mutableStateOf("") }
    var heightIn by remember { mutableStateOf("") }
    var showGuideDialog by remember { mutableStateOf(false) }
    
    var bmiResult by remember { mutableStateOf<Double?>(null) }

    fun calculateBMI() {
        val wRaw = weight.toDoubleOrNull()
        if (wRaw == null || wRaw <= 0) {
            bmiResult = null
            return
        }
        
        // Convert weight to kg
        val wKg = if (weightUnit == "lb") wRaw * 0.453592 else wRaw

        // Convert height to meters
        val hM = when (heightUnit) {
            "cm" -> height.toDoubleOrNull()?.div(100)
            "m" -> height.toDoubleOrNull()
            "ft+in" -> {
                val ft = heightFt.toDoubleOrNull() ?: 0.0
                val inch = heightIn.toDoubleOrNull() ?: 0.0
                if (ft == 0.0 && inch == 0.0) null else ((ft * 12) + inch) * 0.0254
            }
            else -> null
        }

        if (hM != null && hM > 0) {
            bmiResult = wKg / hM.pow(2)
        } else {
            bmiResult = null
        }
    }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = { Text("BMI Calculator", fontWeight = FontWeight.Bold, color = textColor) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = textColor)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleFavorite() }) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) primaryBlue else textColor
                        )
                    }
                    IconButton(onClick = { showGuideDialog = true }) {
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
                .verticalScroll(rememberScrollState()), // Make scrollable
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Gender Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { isMale = true }
                        .border(if (isMale) 2.dp else 0.dp, primaryBlue, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = if (isMale) primaryBlue.copy(alpha = 0.1f) else cardColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Male, null, tint = if (isMale) primaryBlue else secondaryTextColor, modifier = Modifier.size(32.dp))
                        Text("Male", color = if (isMale) primaryBlue else secondaryTextColor, fontWeight = FontWeight.Bold)
                    }
                }
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { isMale = false }
                        .border(if (!isMale) 2.dp else 0.dp, Color(0xFFE91E63), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = if (!isMale) Color(0xFFE91E63).copy(alpha = 0.1f) else cardColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Female, null, tint = if (!isMale) Color(0xFFE91E63) else secondaryTextColor, modifier = Modifier.size(32.dp))
                        Text("Female", color = if (!isMale) Color(0xFFE91E63) else secondaryTextColor, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Age Input
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, borderColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DateRange, null, tint = primaryBlue, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Age", color = secondaryTextColor, style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = age,
                        onValueChange = { if (it.all { char -> char.isDigit() }) age = it },
                        modifier = Modifier.fillMaxWidth(),
                        suffix = { Text("years") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedBorderColor = primaryBlue,
                            unfocusedBorderColor = borderColor
                        )
                    )
                    if (age.isNotEmpty() && (age.toIntOrNull() ?: 20) < 20) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Note: Standard BMI is for adults (20+). For children, consult a growth chart.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorOverweight.copy(alpha = 0.8f) // Warning color
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Unit Toggle
            // Removed Global Unit Toggle

            Spacer(modifier = Modifier.height(24.dp))

            // Weight Input
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, borderColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MonitorWeight, null, tint = primaryBlue, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Weight", color = secondaryTextColor, style = MaterialTheme.typography.bodyMedium)
                        }
                        
                        // Weight Unit Selector
                        Row(
                            modifier = Modifier
                                .background(primaryBlue.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf("kg", "lb").forEach { unit ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (weightUnit == unit) primaryBlue else Color.Transparent)
                                        .clickable { weightUnit = unit; calculateBMI() }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = unit,
                                        color = if (weightUnit == unit) Color.White else primaryBlue,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = weight,
                        onValueChange = { weight = it; calculateBMI() },
                        modifier = Modifier.fillMaxWidth(),
                        suffix = { Text(weightUnit) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedBorderColor = primaryBlue,
                            unfocusedBorderColor = borderColor
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Height Input
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, borderColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Height, null, tint = primaryBlue, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Height", color = secondaryTextColor, style = MaterialTheme.typography.bodyMedium)
                        }
                        
                        // Height Unit Selector
                        Row(
                            modifier = Modifier
                                .background(primaryBlue.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf("cm", "m", "ft+in").forEach { unit ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (heightUnit == unit) primaryBlue else Color.Transparent)
                                        .clickable { heightUnit = unit; calculateBMI() }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = unit,
                                        color = if (heightUnit == unit) Color.White else primaryBlue,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (heightUnit == "ft+in") {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = heightFt,
                                onValueChange = { heightFt = it; calculateBMI() },
                                modifier = Modifier.weight(1f),
                                suffix = { Text("ft") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedBorderColor = primaryBlue,
                                    unfocusedBorderColor = borderColor
                                )
                            )
                            OutlinedTextField(
                                value = heightIn,
                                onValueChange = { heightIn = it; calculateBMI() },
                                modifier = Modifier.weight(1f),
                                suffix = { Text("in") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedBorderColor = primaryBlue,
                                    unfocusedBorderColor = borderColor
                                )
                            )
                        }
                    } else {
                        OutlinedTextField(
                            value = height,
                            onValueChange = { height = it; calculateBMI() },
                            modifier = Modifier.fillMaxWidth(),
                            suffix = { Text(heightUnit) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedBorderColor = primaryBlue,
                                unfocusedBorderColor = borderColor
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Result
            bmiResult?.let { bmi ->
                val (category, categoryColor) = when {
                    bmi < 18.5 -> "Underweight" to colorUnderweight
                    bmi < 25.0 -> "Normal" to colorNormal
                    bmi < 30.0 -> "Overweight" to colorOverweight
                    else -> "Obese" to colorObese
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("BMI = %.1f kg/m²".format(bmi), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = textColor)
                        Text("($category)", style = MaterialTheme.typography.titleMedium, color = categoryColor, fontWeight = FontWeight.Bold)
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Copy and Share buttons
                        val ageText = if (age.isNotEmpty()) ", Age: $age" else ""
                        val resultText = "BMI: %.1f ($category)$ageText".format(bmi)
                        CalculatorActions(
                            resultText = resultText,
                            primaryColor = primaryBlue
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))

                        // Gauge Visualization
                        Box(contentAlignment = Alignment.Center) {
                            androidx.compose.foundation.Canvas(modifier = Modifier.size(320.dp, 200.dp)) {
                                val canvasWidth = size.width
                                val canvasHeight = size.height
                                val arcRadius = canvasWidth / 2
                                val center = androidx.compose.ui.geometry.Offset(canvasWidth / 2, canvasHeight - 40f) // Leave space for bottom bar
                                
                                // Draw Bottom Bar
                                drawRect(
                                    color = colorBottomBar,
                                    topLeft = androidx.compose.ui.geometry.Offset(0f, canvasHeight - 50f),
                                    size = androidx.compose.ui.geometry.Size(canvasWidth, 50f)
                                )
                                
                                // Draw "BMI" text in bottom bar
                                val paintBmi = android.graphics.Paint().apply {
                                    textSize = 60f
                                    textAlign = android.graphics.Paint.Align.CENTER
                                    color = android.graphics.Color.WHITE
                                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                                }
                                drawContext.canvas.nativeCanvas.drawText("BMI", center.x, canvasHeight - 15f, paintBmi)

                                // Draw Hub Background (Light Purple Semi-Circle)
                                drawArc(
                                    color = colorHub,
                                    startAngle = 180f,
                                    sweepAngle = 180f,
                                    useCenter = true,
                                    topLeft = androidx.compose.ui.geometry.Offset(center.x - arcRadius * 0.6f, center.y - arcRadius * 0.6f),
                                    size = androidx.compose.ui.geometry.Size(arcRadius * 1.2f, arcRadius * 1.2f)
                                )

                                // Draw Colored Arcs
                                val strokeWidth = arcRadius * 0.4f
                                val arcSize = androidx.compose.ui.geometry.Size((arcRadius - strokeWidth/2) * 2, (arcRadius - strokeWidth/2) * 2)
                                val arcTopLeft = androidx.compose.ui.geometry.Offset(strokeWidth/2, center.y - arcRadius + strokeWidth/2)

                                // Angles mapping (Total 180 degrees)
                                // Range: 0 to 40+ (Visual scale approx 10 to 40 for main zones, but let's map 0-180 deg to standard zones)
                                // Let's use fixed angles for the visual look from the image
                                // Underweight: 180 to 225 (45 deg)
                                // Normal: 225 to 270 (45 deg)
                                // Overweight: 270 to 315 (45 deg)
                                // Obese: 315 to 360 (45 deg)
                                
                                val segments = listOf(
                                    Triple(colorUnderweight, 180f, 45f),
                                    Triple(colorNormal, 225f, 45f),
                                    Triple(colorOverweight, 270f, 45f),
                                    Triple(colorObese, 315f, 45f)
                                )

                                segments.forEach { (color, start, sweep) ->
                                    drawArc(
                                        color = color,
                                        startAngle = start,
                                        sweepAngle = sweep,
                                        useCenter = false,
                                        topLeft = arcTopLeft,
                                        size = arcSize,
                                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
                                    )
                                }

                                // Draw Text Labels inside Arcs
                                val textRadius = arcRadius - strokeWidth / 2
                                val paintLabels = android.graphics.Paint().apply {
                                    textSize = 32f
                                    textAlign = android.graphics.Paint.Align.CENTER
                                    color = android.graphics.Color.WHITE
                                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                                }
                                val paintSubLabels = android.graphics.Paint().apply {
                                    textSize = 24f
                                    textAlign = android.graphics.Paint.Align.CENTER
                                    color = android.graphics.Color.WHITE
                                }

                                val labels = listOf(
                                    Triple("Underweight", "< 18.5", 202.5f),
                                    Triple("Normal", "18.5-24.9", 247.5f),
                                    Triple("Overweight", "25-29.9", 292.5f),
                                    Triple("Obese", "> 30", 337.5f)
                                )

                                drawContext.canvas.nativeCanvas.apply {
                                    labels.forEach { (title, subtitle, angle) ->
                                        save()
                                        // Calculate position
                                        val angleRad = Math.toRadians(angle.toDouble())
                                        val x = center.x + textRadius * kotlin.math.cos(angleRad).toFloat()
                                        val y = center.y + textRadius * kotlin.math.sin(angleRad).toFloat()
                                        
                                        // Rotate text to match arc
                                        rotate(angle + 90f, x, y)
                                        drawText(title, x, y - 10f, paintLabels)
                                        drawText(subtitle, x, y + 20f, paintSubLabels)
                                        restore()
                                    }
                                }
                                
                                // Needle
                                // Map BMI to Angle
                                // 0-18.5 -> 180-225
                                // 18.5-25 -> 225-270
                                // 25-30 -> 270-315
                                // 30+ -> 315-360
                                
                                val bmiVal = bmi.toFloat()
                                val needleAngle = when {
                                    bmiVal < 18.5f -> 180f + (bmiVal / 18.5f) * 45f
                                    bmiVal < 25f -> 225f + ((bmiVal - 18.5f) / (25f - 18.5f)) * 45f
                                    bmiVal < 30f -> 270f + ((bmiVal - 25f) / (30f - 25f)) * 45f
                                    else -> 315f + ((bmiVal - 30f) / 10f).coerceAtMost(1f) * 45f // Cap at 40 BMI for visual
                                }
                                
                                val needleLength = arcRadius - 20f
                                val needleRad = Math.toRadians(needleAngle.toDouble())
                                
                                val needleEnd = androidx.compose.ui.geometry.Offset(
                                    center.x + needleLength * kotlin.math.cos(needleRad).toFloat(),
                                    center.y + needleLength * kotlin.math.sin(needleRad).toFloat()
                                )
                                
                                // Tapered Needle Path
                                val needleBaseWidth = 15f
                                val baseLeft = androidx.compose.ui.geometry.Offset(
                                    center.x + needleBaseWidth * kotlin.math.cos(needleRad - Math.PI/2).toFloat(),
                                    center.y + needleBaseWidth * kotlin.math.sin(needleRad - Math.PI/2).toFloat()
                                )
                                val baseRight = androidx.compose.ui.geometry.Offset(
                                    center.x + needleBaseWidth * kotlin.math.cos(needleRad + Math.PI/2).toFloat(),
                                    center.y + needleBaseWidth * kotlin.math.sin(needleRad + Math.PI/2).toFloat()
                                )
                                
                                val path = androidx.compose.ui.graphics.Path().apply {
                                    moveTo(baseLeft.x, baseLeft.y)
                                    lineTo(needleEnd.x, needleEnd.y)
                                    lineTo(baseRight.x, baseRight.y)
                                    close()
                                }
                                
                                drawPath(path, Color.Black)
                                drawCircle(Color.Black, radius = 15f, center = center)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = { showGuideDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryBlue.copy(alpha = 0.1f), contentColor = primaryBlue),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Info, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("View BMI Guide & Charts")
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Detailed Stats
                        val heightInMeters = when (heightUnit) {
                            "cm" -> height.toDoubleOrNull()?.div(100)
                            "m" -> height.toDoubleOrNull()
                            "ft+in" -> {
                                val ft = heightFt.toDoubleOrNull() ?: 0.0
                                val inch = heightIn.toDoubleOrNull() ?: 0.0
                                ((ft * 12) + inch) * 0.0254
                            }
                            else -> 0.0
                        } ?: 0.0

                        if (heightInMeters > 0) {
                            val minHealthyWeight = 18.5 * heightInMeters.pow(2)
                            val maxHealthyWeight = 25.0 * heightInMeters.pow(2)
                            val weightInKg = if (weightUnit == "lb") (weight.toDoubleOrNull() ?: 0.0) * 0.453592 else (weight.toDoubleOrNull() ?: 0.0)
                            val ponderalIndex = weightInKg / heightInMeters.pow(3)
                            val bmiPrime = bmi / 25.0
                            
                            val displayMinWeight = if (weightUnit == "lb") minHealthyWeight * 2.20462 else minHealthyWeight
                            val displayMaxWeight = if (weightUnit == "lb") maxHealthyWeight * 2.20462 else maxHealthyWeight

                            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Divider(color = borderColor)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Healthy BMI Range:", style = MaterialTheme.typography.bodyMedium, color = secondaryTextColor)
                                    Text("18.5 - 25 kg/m²", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = textColor)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Healthy Weight:", style = MaterialTheme.typography.bodyMedium, color = secondaryTextColor)
                                    Text("%.1f - %.1f %s".format(displayMinWeight, displayMaxWeight, weightUnit), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = textColor)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("BMI Prime:", style = MaterialTheme.typography.bodyMedium, color = secondaryTextColor)
                                    Text("%.2f".format(bmiPrime), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = textColor)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Ponderal Index:", style = MaterialTheme.typography.bodyMedium, color = secondaryTextColor)
                                    Text("%.1f kg/m³".format(ponderalIndex), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = textColor)
                                }
                            }
                        }
                    }
                }
            }
        }
        
        if (showGuideDialog) {
            AlertDialog(
                onDismissRequest = { showGuideDialog = false },
                title = { Text("BMI Guide & Charts") },
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        // Medical Disclaimer
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = colorOverweight.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, colorOverweight.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = null,
                                        tint = colorOverweight,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Medical Disclaimer",
                                        fontWeight = FontWeight.Bold,
                                        color = colorOverweight,
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    "This BMI calculator is for informational purposes only and should not be used as a substitute for professional medical advice, diagnosis, or treatment. Always consult with a qualified healthcare provider regarding any health concerns.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = secondaryTextColor
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Standard BMI Categories (Adults)", fontWeight = FontWeight.Bold, color = textColor)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Chart Table
                        val rows = listOf(
                            "Category" to "BMI Range",
                            "Underweight" to "< 18.5",
                            "Normal" to "18.5 - 25",
                            "Overweight" to "25 - 30",
                            "Obese" to "≥ 30"
                        )
                        
                        rows.forEachIndexed { index, (cat, range) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (index == 0) primaryBlue.copy(alpha = 0.1f) else Color.Transparent)
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(cat, fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Normal, color = if (index == 0) primaryBlue else textColor)
                                Text(range, fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Normal, color = if (index == 0) primaryBlue else textColor)
                            }
                            Divider(color = borderColor)
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Age & BMI", fontWeight = FontWeight.Bold, color = textColor)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "• Adults (20+): The standard BMI categories apply to both men and women of all ages.\n" +
                            "• Children & Teens (2-19): BMI is interpreted differently. It's age- and sex-specific (BMI-for-age percentile). Consult a growth chart.",
                            style = MaterialTheme.typography.bodySmall,
                            color = secondaryTextColor
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Other Factors", fontWeight = FontWeight.Bold, color = textColor)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "• Muscle Mass: Athletes may have high BMI but low body fat.\n" +
                            "• Elderly: Higher BMI ranges may be protective against bone loss.",
                            style = MaterialTheme.typography.bodySmall,
                            color = secondaryTextColor
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showGuideDialog = false }) {
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
