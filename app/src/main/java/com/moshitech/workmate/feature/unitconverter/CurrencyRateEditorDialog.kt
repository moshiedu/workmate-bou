package com.moshitech.workmate.feature.unitconverter

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyRateEditorDialog(
    currencyRates: Map<String, Double>,
    onDismiss: () -> Unit,
    onUpdateRate: (String, Double) -> Unit,
    onAddCurrency: (String, Double) -> Unit,
    onResetToDefaults: () -> Unit,
    textColor: Color,
    secondaryTextColor: Color,
    cardColor: Color,
    borderColor: Color
) {
    var editedRates by remember { mutableStateOf(currencyRates.toMutableMap()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var newCurrencyCode by remember { mutableStateOf("") }
    var newCurrencyRate by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Edit Currency Rates", fontWeight = FontWeight.Bold)
                Icon(Icons.Default.Edit, null, tint = Color(0xFF4CAF50))
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    "Set exchange rates relative to USD (1 USD = ? Currency)",
                    style = MaterialTheme.typography.bodySmall,
                    color = secondaryTextColor
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // USD (base currency - not editable)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = cardColor.copy(alpha = 0.5f)),
                    shape = MaterialTheme.shapes.small
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("USD (Base)", fontWeight = FontWeight.Bold, color = textColor)
                        Text("1.0", color = secondaryTextColor)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Add Currency Button
                Button(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                ) {
                    Icon(androidx.compose.material.icons.Icons.Default.Add, "Add Currency", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Custom Currency")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Other currencies
                currencyRates.filter { it.key != "USD" }.entries.sortedBy { it.key }.forEach { (currency, rate) ->
                    var rateText by remember { mutableStateOf(editedRates[currency]?.toString() ?: rate.toString()) }
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = cardColor),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = currency,
                                fontWeight = FontWeight.Bold,
                                color = textColor,
                                modifier = Modifier.width(60.dp)
                            )
                            
                            OutlinedTextField(
                                value = rateText,
                                onValueChange = { newValue ->
                                    if (newValue.all { it.isDigit() || it == '.' }) {
                                        rateText = newValue
                                        newValue.toDoubleOrNull()?.let { newRate ->
                                            editedRates[currency] = newRate
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                textStyle = MaterialTheme.typography.bodyMedium,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedBorderColor = Color(0xFF4CAF50),
                                    unfocusedBorderColor = borderColor
                                )
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    editedRates.forEach { (currency, rate) ->
                        onUpdateRate(currency, rate)
                    }
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = {
                    onResetToDefaults()
                    onDismiss()
                }) {
                    Text("Reset to Defaults")
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        },
        containerColor = cardColor,
        titleContentColor = textColor,
        textContentColor = secondaryTextColor
    )
    
    // Add Currency Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { 
                showAddDialog = false
                newCurrencyCode = ""
                newCurrencyRate = ""
            },
            title = { Text("Add Custom Currency") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newCurrencyCode,
                        onValueChange = { if (it.length <= 10) newCurrencyCode = it.uppercase() },
                        label = { Text("Currency Code (e.g., BDT)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newCurrencyRate,
                        onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) newCurrencyRate = it },
                        label = { Text("Rate (1 USD = ?)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val code = newCurrencyCode.trim()
                        val rate = newCurrencyRate.toDoubleOrNull()
                        if (code.isNotEmpty() && rate != null && rate > 0) {
                            onAddCurrency(code, rate)
                            editedRates[code] = rate
                            showAddDialog = false
                            newCurrencyCode = ""
                            newCurrencyRate = ""
                        }
                    },
                    enabled = newCurrencyCode.trim().isNotEmpty() && newCurrencyRate.toDoubleOrNull() != null
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showAddDialog = false
                    newCurrencyCode = ""
                    newCurrencyRate = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}
