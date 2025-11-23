package com.moshitech.workmate.feature.unitconverter

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversionDetailsScreen(
    navController: NavController,
    categoryName: String,
    viewModel: UnitConverterViewModel = viewModel()
) {
    val isDark = isSystemInDarkTheme()
    
    // Colors
    val backgroundColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF8F9FA)
    val textColor = if (isDark) Color.White else Color(0xFF111827)
    val secondaryTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF6B7280)
    val cardColor = if (isDark) Color(0xFF1E293B) else Color.White
    val borderColor = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
    val primaryBlue = Color(0xFF1976D2)

    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val inputValue by viewModel.inputValue.collectAsState()
    val resultValue by viewModel.resultValue.collectAsState()
    val sourceUnit by viewModel.sourceUnit.collectAsState()
    val targetUnit by viewModel.targetUnit.collectAsState()
    val availableUnits by viewModel.availableUnits.collectAsState()
    val dpiValue by viewModel.dpiValue.collectAsState()

    LaunchedEffect(categoryName) {
        val category = UnitCategory.entries.find { it.name == categoryName } ?: UnitCategory.LENGTH
        viewModel.selectCategory(category)
    }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text("Unit Conversion", fontWeight = FontWeight.Bold, color = textColor)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = textColor
                        )
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Category Selector (Visual only for now, or could be functional)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Conversion Category", color = secondaryTextColor, style = MaterialTheme.typography.bodyMedium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(selectedCategory.title, color = textColor, fontWeight = FontWeight.Bold)
                        // Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = textColor)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            // DPI Input for Digital Image
            if (selectedCategory == UnitCategory.DIGITAL_IMAGE) {
                OutlinedTextField(
                    value = dpiValue,
                    onValueChange = { viewModel.onDpiValueChanged(it) },
                    label = { Text("DPI (Dots Per Inch)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = cardColor,
                        unfocusedContainerColor = cardColor,
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        focusedBorderColor = primaryBlue,
                        unfocusedBorderColor = borderColor
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // From Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("From", color = secondaryTextColor, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = inputValue,
                        onValueChange = { viewModel.onInputValueChanged(it) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedBorderColor = borderColor,
                            unfocusedBorderColor = borderColor,
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    UnitDropdown(
                        selectedUnit = sourceUnit,
                        availableUnits = availableUnits,
                        onUnitSelected = { viewModel.onSourceUnitChanged(it) },
                        textColor = textColor,
                        borderColor = borderColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Swap Button
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF1E293B), CircleShape) // Dark background for button
                    .border(1.dp, borderColor, CircleShape)
                    .clickable { viewModel.swapUnits() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SwapVert,
                    contentDescription = "Swap",
                    tint = primaryBlue
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // To Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("To", color = secondaryTextColor, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = resultValue,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = textColor,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    UnitDropdown(
                        selectedUnit = targetUnit,
                        availableUnits = availableUnits,
                        onUnitSelected = { viewModel.onTargetUnitChanged(it) },
                        textColor = textColor,
                        borderColor = borderColor
                    )
                }
            }
        }
    }
}

@Composable
fun UnitDropdown(
    selectedUnit: ConversionUnit?,
    availableUnits: List<ConversionUnit>,
    onUnitSelected: (ConversionUnit) -> Unit,
    textColor: Color,
    borderColor: Color
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable { expanded = true }
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedUnit?.name ?: "Select Unit",
                color = textColor,
                fontWeight = FontWeight.Medium
            )
            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = textColor)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            availableUnits.forEach { unit ->
                DropdownMenuItem(
                    text = { Text(unit.name) },
                    onClick = {
                        onUnitSelected(unit)
                        expanded = false
                    }
                )
            }
        }
    }
}
