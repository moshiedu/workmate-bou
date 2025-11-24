package com.moshitech.workmate.feature.unitconverter


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitConverterScreen(
    navController: NavController,
    viewModel: UnitConverterViewModel = viewModel()
) {
    val isDark = isSystemInDarkTheme()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val favorites by viewModel.favorites.collectAsState()

    // Colors matching the design
    val backgroundColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF8F9FA)
    val textColor = if (isDark) Color.White else Color(0xFF111827)
    val secondaryTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF6B7280)
    val cardColor = if (isDark) Color(0xFF1E293B) else Color.White
    val borderColor = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
    val primaryBlue = Color(0xFF1976D2) // Or the specific blue from the image

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Unit Conversion", fontWeight = FontWeight.Bold, color = textColor)
                    }
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
                actions = {
                    IconButton(onClick = { /* TODO: Favorites/Star action */ }) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Favorites",
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
                .padding(horizontal = 16.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                placeholder = { Text("Search units or categories", color = secondaryTextColor) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = secondaryTextColor) },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = if (isDark) Color(0xFF1E293B) else Color.White,
                    unfocusedContainerColor = if (isDark) Color(0xFF1E293B) else Color.White,
                    focusedBorderColor = borderColor,
                    unfocusedBorderColor = borderColor,
                    cursorColor = primaryBlue,
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Favorites Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = primaryBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Favorites",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = textColor
                    )
                }
                Text(
                    text = "Manage",
                    style = MaterialTheme.typography.bodyMedium,
                    color = primaryBlue,
                    modifier = Modifier.clickable { navController.navigate("manage_favorites") }
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(favorites) { favorite ->
                    Card(
                        modifier = Modifier
                            .width(160.dp)
                            .height(80.dp)
                            .clickable { navController.navigate("unit_conversion_details/${favorite.category.name}") },
                        colors = CardDefaults.cardColors(containerColor = cardColor),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Icon(
                                    imageVector = favorite.category.icon,
                                    contentDescription = null,
                                    tint = secondaryTextColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = primaryBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text(
                                text = "${favorite.fromUnit} to ${favorite.toUnit}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = textColor,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Recent Conversions Section
            val history by viewModel.history.collectAsState()
            
            if (history.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.History,
                            contentDescription = null,
                            tint = primaryBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Recent Conversions",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = textColor
                        )
                    }
                    Text(
                        text = "Clear",
                        style = MaterialTheme.typography.bodyMedium,
                        color = primaryBlue,
                        modifier = Modifier.clickable { viewModel.clearHistory() }
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(history.take(10)) { item ->
                        Card(
                            modifier = Modifier
                                .width(180.dp)
                                .height(90.dp)
                                .clickable { 
                                    navController.navigate("unit_conversion_details/${item.category}")
                                },
                            colors = CardDefaults.cardColors(containerColor = cardColor),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = item.category.replace("_", " "),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = secondaryTextColor,
                                    maxLines = 1
                                )
                                Text(
                                    text = "${item.inputValue} ${item.fromUnit}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    color = textColor,
                                    maxLines = 1
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = androidx.compose.material.icons.Icons.Default.ArrowForward,
                                        contentDescription = null,
                                        tint = primaryBlue,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${item.resultValue} ${item.toUnit}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = primaryBlue,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // All Categories
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Category,
                    contentDescription = null,
                    tint = primaryBlue,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "All Categories",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = textColor
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(categories) { category ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .clickable { navController.navigate("unit_conversion_details/${category.name}") },
                        colors = CardDefaults.cardColors(containerColor = cardColor),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = category.icon,
                                contentDescription = null,
                                tint = secondaryTextColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = category.title,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = textColor
                            )
                        }
                    }
                }
            }
        }
    }
}
