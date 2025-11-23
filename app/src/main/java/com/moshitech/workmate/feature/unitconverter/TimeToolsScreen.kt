package com.moshitech.workmate.feature.unitconverter

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
    viewModel: TimeToolsViewModel = viewModel()
) {
    val isDark = isSystemInDarkTheme()
    val backgroundColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF8F9FA)
    val textColor = if (isDark) Color.White else Color(0xFF111827)
    val primaryBlue = Color(0xFF1976D2)

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Converter", "Date Calc", "Difference", "Timestamp")

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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor)
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = backgroundColor,
                contentColor = primaryBlue,
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
                    0 -> UnitConverterTab(navController) // Reuse existing logic
                    1 -> DateCalculatorTab(viewModel, isDark)
                    2 -> TimeDifferenceTab(viewModel, isDark)
                    3 -> TimestampTab(viewModel, isDark)
                }
            }
        }
    }
}

@Composable
fun UnitConverterTab(navController: NavController) {
    // We can reuse the ConversionDetailsScreen logic here, but since that expects navigation args,
    // we might need to instantiate the view model and UI directly or just embed a simplified version.
    // For simplicity, let's just call the existing screen logic but we need to pass "TIME" category.
    // However, ConversionDetailsScreen is a full screen with Scaffold. We should probably refactor or just
    // instantiate the content.
    // Let's instantiate a local ConversionDetailsContent if we refactored, but for now let's just
    // show a button to go to standard converter or re-implement simple UI.
    // Better: Re-use ConversionDetailsScreen but we can't embed Scaffold in Scaffold easily.
    // Let's just create a simplified version here using the same ViewModel logic.
    
    val viewModel: UnitConverterViewModel = viewModel()
    val isDark = isSystemInDarkTheme()
    val textColor = if (isDark) Color.White else Color(0xFF111827)
    
    // Force select TIME category
    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.selectCategory(UnitCategory.TIME)
    }
    
    // We need to replicate the UI from ConversionDetailsScreen without the Scaffold/TopBar
    // This is a bit of duplication but cleanest for now without major refactor.
    // ... (Simplified UI code)
    
    Column {
        Text("Standard Time Unit Conversion", color = textColor, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        // Re-using the core UI parts would be ideal. 
        // Let's just call the ConversionDetailsScreen content if we can extract it.
        // Since we didn't extract it, I'll just put a placeholder or basic implementation.
        
        // Basic Implementation reusing ViewModel
        val inputValue by viewModel.inputValue.collectAsState()
        val resultValue by viewModel.resultValue.collectAsState()
        val sourceUnit by viewModel.sourceUnit.collectAsState()
        val targetUnit by viewModel.targetUnit.collectAsState()
        val availableUnits by viewModel.availableUnits.collectAsState()
        
        OutlinedTextField(
            value = inputValue,
            onValueChange = { viewModel.onInputValueChanged(it) },
            label = { Text("Value") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(modifier = Modifier.height(8.dp))
        UnitDropdown(sourceUnit, availableUnits, { viewModel.onSourceUnitChanged(it) }, textColor, Color.Gray)
        Spacer(modifier = Modifier.height(8.dp))
        Text("=", color = textColor, style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text(resultValue, color = textColor, style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        UnitDropdown(targetUnit, availableUnits, { viewModel.onTargetUnitChanged(it) }, textColor, Color.Gray)
    }
}

@Composable
fun DateCalculatorTab(viewModel: TimeToolsViewModel, isDark: Boolean) {
    val startDate by viewModel.calcStartDate.collectAsState()
    val years by viewModel.calcYears.collectAsState()
    val months by viewModel.calcMonths.collectAsState()
    val days by viewModel.calcDays.collectAsState()
    val operation by viewModel.calcOperation.collectAsState()
    val resultDate by viewModel.calcResultDate.collectAsState()
    
    val textColor = if (isDark) Color.White else Color(0xFF111827)
    val cardColor = if (isDark) Color(0xFF1E293B) else Color.White

    Column {
        Text("Date Calculator", color = textColor, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        DateSelector(label = "Start Date", date = startDate) { viewModel.onCalcStartDateChanged(it) }
        
        Spacer(modifier = Modifier.height(16.dp))
        
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
        
        Spacer(modifier = Modifier.height(16.dp))
        
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
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = cardColor)
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Result Date", color = Color.Gray)
                Text(
                    text = resultDate.format(DateTimeFormatter.ofPattern("EEE, MMM dd, yyyy")),
                    style = MaterialTheme.typography.headlineMedium,
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun TimeDifferenceTab(viewModel: TimeToolsViewModel, isDark: Boolean) {
    val startDate by viewModel.diffStartDate.collectAsState()
    val endDate by viewModel.diffEndDate.collectAsState()
    val result by viewModel.diffResult.collectAsState()
    
    val textColor = if (isDark) Color.White else Color(0xFF111827)
    val cardColor = if (isDark) Color(0xFF1E293B) else Color.White

    Column {
        Text("Time Difference", color = textColor, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        DateSelector(label = "Start Date", date = startDate) { viewModel.onDiffStartDateChanged(it) }
        Spacer(modifier = Modifier.height(8.dp))
        DateSelector(label = "End Date", date = endDate) { viewModel.onDiffEndDateChanged(it) }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = cardColor)
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Difference", color = Color.Gray)
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
}

@Composable
fun TimestampTab(viewModel: TimeToolsViewModel, isDark: Boolean) {
    val input by viewModel.timestampInput.collectAsState()
    val result by viewModel.timestampResult.collectAsState()
    
    val textColor = if (isDark) Color.White else Color(0xFF111827)
    val cardColor = if (isDark) Color(0xFF1E293B) else Color.White

    Column {
        Text("Timestamp Converter", color = textColor, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = input,
            onValueChange = { viewModel.onTimestampInputChanged(it) },
            label = { Text("Unix Timestamp (ms or sec)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = cardColor)
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Formatted Date", color = Color.Gray)
                Text(
                    text = result,
                    style = MaterialTheme.typography.headlineMedium,
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { viewModel.onTimestampInputChanged(System.currentTimeMillis().toString()) },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Set to Now")
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
