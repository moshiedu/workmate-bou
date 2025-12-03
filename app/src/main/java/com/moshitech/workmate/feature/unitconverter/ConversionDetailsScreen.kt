package com.moshitech.workmate.feature.unitconverter

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.luminance
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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Input
import androidx.compose.material.icons.filled.Output
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversionDetailsScreen(
    navController: NavController,
    categoryName: String,
    viewModel: UnitConverterViewModel = viewModel()
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    
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
    val isFavorite by viewModel.isCurrentFavorite.collectAsState()
    val currencyRates by viewModel.currencyRates.collectAsState()
    
    var showRateEditor by remember { mutableStateOf(false) }

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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor),
                actions = {
                    IconButton(onClick = { viewModel.toggleFavorite() }) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) primaryBlue else textColor
                        )
                    }
                }
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
            // Category Selector
            var categoryExpanded by remember { mutableStateOf(false) }
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { categoryExpanded = true },
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, borderColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Category", color = secondaryTextColor, style = MaterialTheme.typography.bodyMedium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(selectedCategory.title, color = textColor, fontWeight = FontWeight.SemiBold)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = textColor, modifier = Modifier.size(20.dp))
                    }
                }
                
                DropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    val internalCategories = setOf(
                        UnitCategory.TIME_DATE_CALC,
                        UnitCategory.TIME_DIFFERENCE,
                        UnitCategory.TIME_TIMESTAMP,
                        UnitCategory.TIME_ZONES,
                        UnitCategory.TIME_BIZ_DAYS,
                        UnitCategory.TIME_AGE,
                        UnitCategory.MORE
                    )
                    UnitCategory.values().filter { !internalCategories.contains(it) }.forEach { category ->
                        DropdownMenuItem(
                            text = { 
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = category.icon,
                                        contentDescription = null,
                                        tint = if (category == selectedCategory) primaryBlue else textColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = category.title,
                                        color = if (category == selectedCategory) primaryBlue else textColor
                                    )
                                }
                            },
                            onClick = {
                                categoryExpanded = false
                                if (category == UnitCategory.TIME) {
                                    // Navigate to TimeToolsScreen for TIME category
                                    navController.navigate("unit_conversion_details/TIME") {
                                        popUpTo("unit_conversion_details/{categoryName}") { inclusive = true }
                                    }
                                } else {
                                    viewModel.selectCategory(category)
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
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
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, borderColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Input,
                            contentDescription = null,
                            tint = primaryBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("From", color = secondaryTextColor, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                    }
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
                            focusedBorderColor = primaryBlue,
                            unfocusedBorderColor = borderColor.copy(alpha = 0.5f),
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    UnitDropdown(
                        selectedUnit = sourceUnit,
                        availableUnits = availableUnits,
                        onUnitSelected = { viewModel.onSourceUnitChanged(it) },
                        textColor = textColor,
                        borderColor = borderColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Swap Button
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(primaryBlue, CircleShape)
                    .clickable { viewModel.swapUnits() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SwapVert,
                    contentDescription = "Swap",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // To Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, borderColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Output,
                            contentDescription = null,
                            tint = primaryBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("To", color = secondaryTextColor, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                    ) {
                        Text(
                            text = resultValue,
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = primaryBlue,
                            modifier = Modifier.weight(1f)
                        )

                        val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                        val context = androidx.compose.ui.platform.LocalContext.current

                        IconButton(
                            onClick = {
                                val text = "$inputValue ${sourceUnit?.symbol} = $resultValue ${targetUnit?.symbol}"
                                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(text))
                                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = primaryBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                val text = "$inputValue ${sourceUnit?.symbol} = $resultValue ${targetUnit?.symbol}"
                                val sendIntent: Intent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, text)
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, null)
                                context.startActivity(shareIntent)
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = primaryBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitDropdown(
    selectedUnit: ConversionUnit?,
    availableUnits: List<ConversionUnit>,
    onUnitSelected: (ConversionUnit) -> Unit,
    textColor: Color,
    borderColor: Color
) {
    var showSheet by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable { showSheet = true }
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
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { 
                showSheet = false 
                searchQuery = ""
            },
            containerColor = if (isDark) Color(0xFF1E293B) else Color.White,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
            ) {
                Text(
                    text = "Select Unit",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    placeholder = { Text("Search unit...", color = Color.Gray, style = MaterialTheme.typography.bodyMedium) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp)) },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedBorderColor = Color(0xFF1976D2),
                        unfocusedBorderColor = borderColor,
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                val filteredUnits = availableUnits.filter { 
                    it.name.contains(searchQuery, ignoreCase = true) || 
                    it.symbol.contains(searchQuery, ignoreCase = true)
                }

                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier.fillMaxWidth().height(300.dp)
                ) {
                    items(filteredUnits.size) { index ->
                        val unit = filteredUnits[index]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onUnitSelected(unit)
                                    showSheet = false
                                    searchQuery = ""
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = unit == selectedUnit,
                                onClick = {
                                    onUnitSelected(unit)
                                    showSheet = false
                                    searchQuery = ""
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF1976D2))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = unit.name,
                                style = MaterialTheme.typography.bodyLarge,
                                color = textColor
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = unit.symbol,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                        HorizontalDivider(color = borderColor.copy(alpha = 0.5f))
                    }
                }
            }
        }
    }
}
