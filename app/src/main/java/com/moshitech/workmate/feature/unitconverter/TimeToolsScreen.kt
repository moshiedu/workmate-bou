package com.moshitech.workmate.feature.unitconverter


import android.app.DatePickerDialog
import android.os.Build
import android.widget.DatePicker
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeToolsScreen(
    navController: NavController,
    initialCategory: String = "TIME",
    viewModel: TimeToolsViewModel = viewModel(),
    unitViewModel: UnitConverterViewModel = viewModel()
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val backgroundColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF8F9FA)
    val textColor = if (isDark) Color.White else Color(0xFF111827)
    val primaryBlue = Color(0xFF1976D2)

    val initialIndex = when (initialCategory) {
        "TIME" -> 0
        "TIME_DATE_CALC" -> 1
        "TIME_DIFFERENCE" -> 2
        "TIME_TIMESTAMP" -> 3
        "TIME_ZONES" -> 4
        "TIME_BIZ_DAYS" -> 5
        "TIME_AGE" -> 6
        else -> 0
    }

    var selectedTabIndex by remember { mutableStateOf(initialIndex) }
    val tabs = listOf("Converter", "Date Calc", "Difference", "Timestamp", "Zones", "Biz Days", "Age")

    val isFav by if (selectedTabIndex == 0) {
        unitViewModel.isCurrentFavorite.collectAsState()
    } else {
        viewModel.isFavoriteForTab(selectedTabIndex).collectAsState(initial = false)
    }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = { Text("Time Tools", fontWeight = FontWeight.Bold, color = textColor) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = textColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor),
                actions = {
                    IconButton(onClick = {
                        if (selectedTabIndex == 0) unitViewModel.toggleFavorite()
                        else viewModel.toggleFavoriteForTab(selectedTabIndex)
                    }) {
                        Icon(
                            imageVector = if (isFav) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Favorite",
                            tint = if (isFav) primaryBlue else textColor
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {

            // Category Selector
            val secondaryTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF6B7280)
            val cardColor = if (isDark) Color(0xFF1E293B) else Color.White
            val borderColor = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
            var categoryExpanded by remember { mutableStateOf(false) }
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clickable { categoryExpanded = true },
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
                        Text("Time", color = textColor, fontWeight = FontWeight.Bold)
                        Icon(androidx.compose.material.icons.Icons.Default.ArrowDropDown, contentDescription = null, tint = textColor)
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
                                        tint = if (category == UnitCategory.TIME) primaryBlue else textColor,
                                        modifier = androidx.compose.ui.Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = androidx.compose.ui.Modifier.width(12.dp))
                                    Text(
                                        text = category.title,
                                        color = if (category == UnitCategory.TIME) primaryBlue else textColor
                                    )
                                }
                            },
                            onClick = {
                                categoryExpanded = false
                                if (category != UnitCategory.TIME) {
                                    navController.navigate("unit_conversion_details/${category.name}") {
                                        popUpTo("unit_conversion_details/{categoryName}") { inclusive = true }
                                    }
                                }
                            }
                        )
                    }
                }
            }

            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = backgroundColor,
                contentColor = primaryBlue,
                edgePadding = 0.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = primaryBlue
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title, maxLines = 1, style = MaterialTheme.typography.bodySmall) }
                    )
                }
            }

            Box(modifier = Modifier.padding(16.dp)) {
                when (selectedTabIndex) {
                    0 -> UnitConverterTab(navController, unitViewModel)
                    1 -> DateCalculatorTab(viewModel, isDark)
                    2 -> TimeDifferenceTab(viewModel, isDark)
                    3 -> TimestampTab(viewModel, isDark)
                    4 -> TimeZoneTab(viewModel, isDark)
                    5 -> BusinessDayTab(viewModel, isDark)
                    6 -> AgeCalculatorTab(viewModel, isDark)
                }
            }
        }
    }
}

@Composable
fun UnitConverterTab(navController: NavController, viewModel: UnitConverterViewModel) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val textColor = if (isDark) Color.White else Color(0xFF111827)
    val secondaryTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF6B7280)
    
    // Ensure we are in TIME category
    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.selectCategory(UnitCategory.TIME)
    }

    val inputValue by viewModel.inputValue.collectAsState()
    val resultValue by viewModel.resultValue.collectAsState()
    val sourceUnit by viewModel.sourceUnit.collectAsState()
    val targetUnit by viewModel.targetUnit.collectAsState()
    val availableUnits by viewModel.availableUnits.collectAsState()

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Time Unit Converter", color = textColor, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        // From Section
        Text("From", color = secondaryTextColor, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = inputValue,
                onValueChange = { viewModel.onInputValueChanged(it) },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    focusedBorderColor = Color(0xFF1976D2),
                    unfocusedBorderColor = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier.weight(1f)) {
                var expanded by remember { mutableStateOf(false) }
                OutlinedTextField(
                    value = sourceUnit?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                    modifier = Modifier.fillMaxWidth().clickable { expanded = true },
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = textColor,
                        disabledBorderColor = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0),
                        disabledLabelColor = secondaryTextColor,
                        disabledTrailingIconColor = textColor
                    )
                )
                Box(modifier = Modifier.matchParentSize().clickable { expanded = true })
                
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    availableUnits.forEach { unit ->
                        DropdownMenuItem(
                            text = { Text(unit.name) },
                            onClick = { 
                                viewModel.onSourceUnitChanged(unit)
                                expanded = false 
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Swap Button
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            IconButton(
                onClick = { viewModel.swapUnits() },
                modifier = Modifier
                    .background(Color(0xFF1976D2).copy(alpha = 0.1f), androidx.compose.foundation.shape.CircleShape)
            ) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.CompareArrows,
                    contentDescription = "Swap",
                    tint = Color(0xFF1976D2)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // To Section
        Text("To", color = secondaryTextColor, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            // Result is read-only
            OutlinedTextField(
                value = resultValue,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    focusedBorderColor = Color(0xFF1976D2),
                    unfocusedBorderColor = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier.weight(1f)) {
                var expanded by remember { mutableStateOf(false) }
                OutlinedTextField(
                    value = targetUnit?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                    modifier = Modifier.fillMaxWidth().clickable { expanded = true },
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = textColor,
                        disabledBorderColor = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0),
                        disabledLabelColor = secondaryTextColor,
                        disabledTrailingIconColor = textColor
                    )
                )
                Box(modifier = Modifier.matchParentSize().clickable { expanded = true })
                
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    availableUnits.forEach { unit ->
                        DropdownMenuItem(
                            text = { Text(unit.name) },
                            onClick = { 
                                viewModel.onTargetUnitChanged(unit)
                                expanded = false 
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        ResultCard(
            title = "Result",
            result = "$resultValue ${targetUnit?.symbol ?: ""}",
            icon = Icons.Default.Schedule,
            isDark = isDark,
            onSave = { /* Auto-saved or manual save not needed for standard converter as it's not history-heavy? 
                         Actually standard converter usually saves on every calculation or debounced.
                         Let's leave empty or implement manual save if desired. */ }
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DateCalculatorTab(viewModel: TimeToolsViewModel, isDark: Boolean) {
    val startDate by viewModel.calcStartDate.collectAsState()
    val years by viewModel.calcYears.collectAsState()
    val months by viewModel.calcMonths.collectAsState()
    val days by viewModel.calcDays.collectAsState()
    val operation by viewModel.calcOperation.collectAsState()
    val resultDate by viewModel.calcResultDate.collectAsState()
    
    val textColor = if (isDark) Color.White else Color(0xFF111827)
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Date Calculator", color = textColor, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        
        DateSelector(label = "Start Date", date = startDate) { viewModel.onCalcStartDateChanged(it) }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(
                onClick = { viewModel.onCalcOperationChanged(DateOperation.ADD) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (operation == DateOperation.ADD) Color(0xFF1976D2) else Color.Gray
                )
            ) { Text("Add") }
            Button(
                onClick = { viewModel.onCalcOperationChanged(DateOperation.SUBTRACT) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (operation == DateOperation.SUBTRACT) Color(0xFF1976D2) else Color.Gray
                )
            ) { Text("Subtract") }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = years, onValueChange = { viewModel.onCalcDurationChanged(it, months, days) },
                label = { Text("Years") }, modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                value = months, onValueChange = { viewModel.onCalcDurationChanged(years, it, days) },
                label = { Text("Months") }, modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                value = days, onValueChange = { viewModel.onCalcDurationChanged(years, months, it) },
                label = { Text("Days") }, modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        ResultCard(
            title = "Result Date",
            result = resultDate.format(DateTimeFormatter.ofPattern("EEE, MMM dd, yyyy")),
            icon = Icons.Default.CalendarToday,
            isDark = isDark,
            onSave = { viewModel.saveDateCalcHistory() }
        )
    }
}

@Composable
fun TimeDifferenceTab(viewModel: TimeToolsViewModel, isDark: Boolean) {
    val startDate by viewModel.diffStartDate.collectAsState()
    val endDate by viewModel.diffEndDate.collectAsState()
    val result by viewModel.diffResult.collectAsState()
    
    val textColor = if (isDark) Color.White else Color(0xFF111827)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Time Difference", color = textColor, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        
        DateSelector(label = "Start Date", date = startDate) { viewModel.onDiffStartDateChanged(it) }
        Spacer(modifier = Modifier.height(8.dp))
        DateSelector(label = "End Date", date = endDate) { viewModel.onDiffEndDateChanged(it) }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        ResultCard(
            title = "Time Difference",
            result = result,
            icon = androidx.compose.material.icons.Icons.Default.History,
            isDark = isDark,
            onSave = { viewModel.saveDiffHistory() }
        )
    }
}

@Composable
fun TimestampTab(viewModel: TimeToolsViewModel, isDark: Boolean) {
    val input by viewModel.timestampInput.collectAsState()
    val result by viewModel.timestampResult.collectAsState()
    val isToDate by viewModel.isTimestampToDate.collectAsState()
    val format by viewModel.timestampFormat.collectAsState()
    val zoneId by viewModel.timestampZoneId.collectAsState()
    
    val textColor = if (isDark) Color.White else Color(0xFF111827)
    val primaryBlue = Color(0xFF1976D2)
    
    // Common Timezones
    val commonZones = listOf(
        "UTC", "GMT", "America/New_York", "America/Los_Angeles", "Europe/London", 
        "Europe/Paris", "Asia/Tokyo", "Asia/Dhaka", "Australia/Sydney"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Timestamp Converter", color = textColor, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        
        // Direction Toggle
        Row(verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.material3.RadioButton(
                selected = isToDate,
                onClick = { viewModel.onTimestampDirectionChanged(true) },
                colors = androidx.compose.material3.RadioButtonDefaults.colors(selectedColor = primaryBlue)
            )
            Text("Timestamp → Date", color = textColor, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.width(12.dp))
            androidx.compose.material3.RadioButton(
                selected = !isToDate,
                onClick = { viewModel.onTimestampDirectionChanged(false) },
                colors = androidx.compose.material3.RadioButtonDefaults.colors(selectedColor = primaryBlue)
            )
            Text("Date → Timestamp", color = textColor, style = MaterialTheme.typography.bodyMedium)
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Format Selector
        var formatExpanded by remember { mutableStateOf(false) }
        val formats = listOf(
            "yyyy-MM-dd HH:mm:ss",
            "dd/MM/yyyy HH:mm:ss",
            "MM/dd/yyyy HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "EEE, dd MMM yyyy HH:mm:ss z"
        )
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = format,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Format") },
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                    modifier = Modifier.fillMaxWidth().clickable { formatExpanded = true },
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = textColor,
                        disabledBorderColor = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0),
                        disabledLabelColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF6B7280),
                        disabledTrailingIconColor = textColor
                    )
                )
                Box(modifier = Modifier.matchParentSize().clickable { formatExpanded = true })
                
                DropdownMenu(expanded = formatExpanded, onDismissRequest = { formatExpanded = false }) {
                    formats.forEach { f ->
                        DropdownMenuItem(
                            text = { Text(f) },
                            onClick = { 
                                viewModel.onTimestampFormatChanged(f)
                                formatExpanded = false 
                            }
                        )
                    }
                }
            }
            
            // Timezone Selector
            Box(modifier = Modifier.weight(1f)) {
                 var zoneExpanded by remember { mutableStateOf(false) }
                 OutlinedTextField(
                    value = zoneId,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Timezone") },
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                    modifier = Modifier.fillMaxWidth().clickable { zoneExpanded = true },
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = textColor,
                        disabledBorderColor = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0),
                        disabledLabelColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF6B7280),
                        disabledTrailingIconColor = textColor
                    )
                )
                Box(modifier = Modifier.matchParentSize().clickable { zoneExpanded = true })
                
                DropdownMenu(expanded = zoneExpanded, onDismissRequest = { zoneExpanded = false }) {
                    commonZones.forEach { z ->
                        DropdownMenuItem(
                            text = { Text(z) },
                            onClick = { 
                                viewModel.onTimestampZoneIdChanged(z)
                                zoneExpanded = false 
                            }
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        if (isToDate) {
            OutlinedTextField(
                value = input,
                onValueChange = { viewModel.onTimestampInputChanged(it) },
                label = { Text("Unix Timestamp (ms or sec)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        } else {
            // Date -> Timestamp Input
            val context = LocalContext.current
            val calendar = java.util.Calendar.getInstance()
            
            val datePickerDialog = DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    val timePickerDialog = android.app.TimePickerDialog(
                        context,
                        { _, hourOfDay, minute ->
                            val selectedDateTime = java.time.LocalDateTime.of(year, month + 1, dayOfMonth, hourOfDay, minute)
                            val formatter = java.time.format.DateTimeFormatter.ofPattern(format)
                            try {
                                viewModel.onTimestampInputChanged(selectedDateTime.format(formatter))
                            } catch (e: Exception) {
                                viewModel.onTimestampInputChanged(selectedDateTime.toString())
                            }
                        },
                        calendar.get(java.util.Calendar.HOUR_OF_DAY),
                        calendar.get(java.util.Calendar.MINUTE),
                        false 
                    )
                    timePickerDialog.show()
                },
                calendar.get(java.util.Calendar.YEAR),
                calendar.get(java.util.Calendar.MONTH),
                calendar.get(java.util.Calendar.DAY_OF_MONTH)
            )

            OutlinedTextField(
                value = input,
                onValueChange = { viewModel.onTimestampInputChanged(it) },
                label = { Text("Date String") },
                modifier = Modifier.fillMaxWidth().clickable { datePickerDialog.show() },
                enabled = false, 
                trailingIcon = { 
                    IconButton(onClick = { datePickerDialog.show() }) {
                        Icon(Icons.Default.CalendarToday, "Select Date")
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = textColor,
                    disabledBorderColor = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0),
                    disabledLabelColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF6B7280),
                    disabledTrailingIconColor = textColor
                )
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        ResultCard(
            title = if (isToDate) "Formatted Date" else "Timestamp (ms)",
            result = result,
            icon = androidx.compose.material.icons.Icons.Default.CalendarToday,
            isDark = isDark,
            onSave = { viewModel.saveTimestampHistory() }
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = { 
                if (isToDate) {
                    viewModel.onTimestampInputChanged(System.currentTimeMillis().toString()) 
                } else {
                    val formatter = java.time.format.DateTimeFormatter.ofPattern(format)
                    viewModel.onTimestampInputChanged(java.time.LocalDateTime.now().format(formatter))
                }
            },
            modifier = Modifier.align(Alignment.CenterHorizontally),
            colors = ButtonDefaults.buttonColors(containerColor = primaryBlue)
        ) {
            Text("Set to Now")
        }
    }
}

@Composable
fun TimeZoneTab(viewModel: TimeToolsViewModel, isDark: Boolean) {
    val sourceTime by viewModel.timeZoneSourceTime.collectAsState()
    val sourceId by viewModel.timeZoneSourceId.collectAsState()
    val targetId by viewModel.timeZoneTargetId.collectAsState()
    val result by viewModel.timeZoneResult.collectAsState()
    
    val textColor = if (isDark) Color.White else Color(0xFF111827)
    
    // Simplified Zone List for Demo
    val commonZones = listOf("UTC", "America/New_York", "Europe/London", "Asia/Tokyo", "Australia/Sydney", "Asia/Dhaka", "Europe/Paris")

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Time Zone Converter", color = textColor, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        
        // Source Time (Just Date Selector for now, ideally TimePicker too)
        DateSelector(label = "Source Date", date = sourceTime.toLocalDate()) { 
            viewModel.onTimeZoneSourceTimeChanged(it.atTime(sourceTime.toLocalTime()))
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text("Source Zone", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
        ZoneDropdown(selected = sourceId, zones = commonZones) { viewModel.onTimeZoneSourceIdChanged(it) }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text("Target Zone", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
        ZoneDropdown(selected = targetId, zones = commonZones) { viewModel.onTimeZoneTargetIdChanged(it) }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        ResultCard(
            title = "Converted Time",
            result = result,
            icon = androidx.compose.material.icons.Icons.Default.CalendarToday,
            isDark = isDark,
            onSave = { viewModel.saveTimeZoneHistory() }
        )
    }
}

@Composable
fun BusinessDayTab(viewModel: TimeToolsViewModel, isDark: Boolean) {
    val startDate by viewModel.bizStartDate.collectAsState()
    val days by viewModel.bizDays.collectAsState()
    val operation by viewModel.bizOperation.collectAsState()
    val resultDate by viewModel.bizResultDate.collectAsState()
    
    val textColor = if (isDark) Color.White else Color(0xFF111827)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Business Day Calculator", color = textColor, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        
        DateSelector(label = "Start Date", date = startDate) { viewModel.onBizStartDateChanged(it) }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(
                onClick = { viewModel.onBizOperationChanged(DateOperation.ADD) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (operation == DateOperation.ADD) Color(0xFF1976D2) else Color.Gray
                )
            ) { Text("Add") }
            Button(
                onClick = { viewModel.onBizOperationChanged(DateOperation.SUBTRACT) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (operation == DateOperation.SUBTRACT) Color(0xFF1976D2) else Color.Gray
                )
            ) { Text("Subtract") }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedTextField(
            value = days,
            onValueChange = { viewModel.onBizDaysChanged(it) },
            label = { Text("Business Days") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        ResultCard(
            title = "Result Date (Excluding Weekends)",
            result = resultDate.format(DateTimeFormatter.ofPattern("EEE, MMM dd, yyyy")),
            icon = Icons.Default.CalendarToday,
            isDark = isDark,
            onSave = { viewModel.saveBizDaysHistory() }
        )
    }
}

@Composable
fun AgeCalculatorTab(viewModel: TimeToolsViewModel, isDark: Boolean) {
    val birthDate by viewModel.ageBirthDate.collectAsState()
    val result by viewModel.ageResult.collectAsState()
    
    val textColor = if (isDark) Color.White else Color(0xFF111827)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Age Calculator", color = textColor, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        
        DateSelector(label = "Birth Date", date = birthDate) { viewModel.onAgeBirthDateChanged(it) }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        ResultCard(
            title = "Your Age",
            result = result,
            icon = androidx.compose.material.icons.Icons.Default.CalendarToday,
            isDark = isDark,
            onSave = { viewModel.saveAgeHistory() }
        )
    }
}

@Composable
fun ZoneDropdown(selected: String, zones: List<String>, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth().clickable { expanded = true }.background(Color.Transparent).padding(8.dp)) {
        Text(selected, style = MaterialTheme.typography.bodyLarge)
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            zones.forEach { zone ->
                DropdownMenuItem(text = { Text(zone) }, onClick = { onSelected(zone); expanded = false })
            }
        }
    }
}

@Composable
fun DateSelector(label: String, date: LocalDate, onDateSelected: (LocalDate) -> Unit) {
    val context = LocalContext.current
    val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
    
    val dialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, day: Int ->
            onDateSelected(LocalDate.of(year, month + 1, day))
        },
        date.year, date.monthValue - 1, date.dayOfMonth
    )
    
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = date.format(formatter),
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            enabled = false, // Disable typing, but clickable modifier handles click
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        // Hack to make the disabled text field clickable
        Box(modifier = Modifier.matchParentSize().clickable { dialog.show() })
    }
}

@Composable
fun ResultCard(
    title: String,
    result: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isDark: Boolean,
    onSave: () -> Unit
) {
    val cardColor = if (isDark) Color(0xFF1E293B) else Color.White
    val textColor = if (isDark) Color.White else Color(0xFF111827)
    val primaryBlue = Color(0xFF1976D2)
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = primaryBlue,
                    modifier = Modifier.size(32.dp)
                )
                
                Row {
                    IconButton(onClick = {
                        clipboardManager.setText(AnnotatedString(result))
                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = primaryBlue
                        )
                    }
                    IconButton(onClick = {
                        val sendIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, result)
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, null)
                        context.startActivity(shareIntent)
                    }) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Share,
                            contentDescription = "Share",
                            tint = primaryBlue
                        )
                    }
                    IconButton(onClick = {
                        onSave()
                        Toast.makeText(context, "Saved to History", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.History,
                            contentDescription = "Save to History",
                            tint = primaryBlue
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                title,
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = result,
                style = MaterialTheme.typography.headlineSmall,
                color = textColor,
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
