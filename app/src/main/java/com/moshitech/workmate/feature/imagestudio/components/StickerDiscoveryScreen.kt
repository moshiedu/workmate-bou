package com.moshitech.workmate.feature.imagestudio.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

// Mock Data Models
data class StickerCategory(val id: String, val name: String)
data class StickerItem(val id: String, val emoji: String, val categoryId: String, val tags: List<String>, val isPremium: Boolean = false)

@Composable
fun StickerDiscoveryScreen(
    onDismiss: () -> Unit,
    onStickerSelected: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    
    val categories = remember {
        listOf(
            StickerCategory("All", "All"),
            StickerCategory("Recent", "Recent"),
            StickerCategory("Emoji", "Emoji"),
            StickerCategory("Love", "Love"),
            StickerCategory("Cool", "Cool"),
            StickerCategory("Shape", "Shape"),
            StickerCategory("Text", "Text")
        )
    }

    val stickers = remember {
        listOf(
            StickerItem("1", "😀", "Emoji", listOf("smile")),
            StickerItem("2", "😂", "Emoji", listOf("laugh")),
            StickerItem("3", "❤️", "Love", listOf("heart")),
            StickerItem("4", "🔥", "Cool", listOf("fire")),
            StickerItem("5", "✨", "Cool", listOf("star")),
            StickerItem("6", "🎉", "Party", listOf("celebrate")),
            StickerItem("7", "😎", "Cool", listOf("sunglasses")),
            StickerItem("8", "👍", "Emoji", listOf("thumbs")),
            StickerItem("9", "🌹", "Love", listOf("flower")),
            StickerItem("10", "💯", "Cool", listOf("100")),
            StickerItem("11", "🍕", "Food", listOf("pizza")),
            StickerItem("12", "🐶", "Animal", listOf("dog")),
            StickerItem("13", "🚀", "Cool", listOf("rocket")),
            StickerItem("14", "🌈", "Nature", listOf("rainbow")),
            StickerItem("15", "⭐", "Shape", listOf("star")),
            StickerItem("16", "💡", "Object", listOf("idea")),
            StickerItem("17", "💪", "Emoji", listOf("muscle")),
            StickerItem("18", "👻", "Spooky", listOf("ghost")),
            StickerItem("19", "👑", "Cool", listOf("crown")),
            StickerItem("20", "💎", "Cool", listOf("gem"))
        )
    }

    val filteredStickers = remember(searchQuery, selectedCategory) {
        stickers.filter { sticker ->
            val matchesCategory = selectedCategory == "All" || sticker.categoryId == selectedCategory || (selectedCategory == "Emoji" && true) // Simplified
            val matchesSearch = searchQuery.isBlank() || sticker.tags.any { it.contains(searchQuery, ignoreCase = true) }
            matchesCategory && matchesSearch
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Scaffold(
            containerColor = Color.Black,
            topBar = {
                Column(modifier = Modifier.background(Color.Black)) {
                    // Header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(16.dp)
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.align(Alignment.CenterStart)
                        ) {
                            Icon(Icons.Default.Close, "Close", tint = Color.White)
                        }
                        
                        Text(
                            text = "Discover",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.Center)
                        )
                        
                        IconButton(
                            onClick = { /* TODO: Search Focus */ },
                            modifier = Modifier.align(Alignment.CenterEnd)
                        ) {
                            Icon(Icons.Default.Search, "Search", tint = Color.White)
                        }
                    }
                    
                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .height(50.dp),
                        placeholder = { Text("Search stickers, artists...", color = Color.Gray) },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF1E1E1E),
                            unfocusedContainerColor = Color(0xFF1E1E1E),
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            cursorColor = Color(0xFF0096FF)
                        ),
                        shape = RoundedCornerShape(25.dp),
                        singleLine = true
                    )
                    
                    // Categories
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categories) { category ->
                            val isSelected = category.id == selectedCategory
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSelected) Color.White else Color(0xFF1E1E1E))
                                    .clickable { selectedCategory = category.id }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = category.name,
                                    color = if (isSelected) Color.Black else Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        ) { padding ->
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                contentPadding = PaddingValues(
                    top = padding.calculateTopPadding() + 8.dp,
                    bottom = 16.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize().background(Color.Black)
            ) {
                items(filteredStickers) { sticker ->
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1E1E1E))
                            .clickable { onStickerSelected(sticker.emoji) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = sticker.emoji,
                            fontSize = 40.sp
                        )
                        if (sticker.isPremium) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(4.dp)
                                    .background(Color(0xFFFFD700), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text("PLUS", fontSize = 8.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
