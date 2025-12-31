package com.moshitech.workmate.feature.imagestudio.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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

data class StickerItem(
        val id: String,
        val emoji: String,
        val categoryId: String,
        val tags: List<String>,
        val isPremium: Boolean = false
)

@Composable
fun StickerDiscoveryScreen(onDismiss: () -> Unit, onStickerSelected: (String) -> Unit) {
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
                // --- Emotions & Faces ---
                StickerItem("1", "😀", "Emoji", listOf("smile", "happy")),
                StickerItem("2", "😂", "Emoji", listOf("laugh", "joy")),
                StickerItem("3", "🥰", "Emoji", listOf("love", "hearts")),
                StickerItem("4", "😎", "Cool", listOf("sunglasses", "cool")),
                StickerItem("5", "🤔", "Emoji", listOf("thinking")),
                StickerItem("6", "😭", "Emoji", listOf("cry", "sad")),
                StickerItem("7", "🤯", "Emoji", listOf("mindblown")),
                StickerItem("8", "😱", "Emoji", listOf("scream", "shock")),
                StickerItem("9", "🤬", "Emoji", listOf("angry", "mad")),
                StickerItem("10", "🤡", "Emoji", listOf("clown", "funny")),
                StickerItem("11", "👻", "Spooky", listOf("ghost", "halloween")),
                StickerItem("12", "💀", "Spooky", listOf("skull", "death")),
                StickerItem("13", "👽", "Space", listOf("alien", "ufo")),
                StickerItem("14", "🤖", "Cool", listOf("robot", "tech")),
                StickerItem("15", "💩", "Emoji", listOf("poop", "funny")),

                // --- Love & Hearts ---
                StickerItem("16", "❤️", "Love", listOf("heart", "red")),
                StickerItem("17", "🧡", "Love", listOf("heart", "orange")),
                StickerItem("18", "💛", "Love", listOf("heart", "yellow")),
                StickerItem("19", "💚", "Love", listOf("heart", "green")),
                StickerItem("20", "💙", "Love", listOf("heart", "blue")),
                StickerItem("21", "💜", "Love", listOf("heart", "purple")),
                StickerItem("22", "🖤", "Love", listOf("heart", "black")),
                StickerItem("23", "🤍", "Love", listOf("heart", "white")),
                StickerItem("24", "💔", "Love", listOf("heart", "break")),
                StickerItem("25", "💘", "Love", listOf("heart", "arrow")),

                // --- Celestial & Cool ---
                StickerItem("26", "✨", "Cool", listOf("sparkles", "shine")),
                StickerItem("27", "🌟", "Cool", listOf("star", "glow")),
                StickerItem("28", "💫", "Cool", listOf("dizzy", "star")),
                StickerItem("29", "🌙", "Nature", listOf("moon", "night")),
                StickerItem("30", "☀️", "Nature", listOf("sun", "day")),
                StickerItem("31", "⚡", "Cool", listOf("bolt", "power")),
                StickerItem("32", "❄️", "Nature", listOf("snow", "ice")),
                StickerItem("33", "🔥", "Cool", listOf("fire", "hot")),
                StickerItem("34", "🌈", "Nature", listOf("rainbow", "color")),

                // --- Kaomoji ---
                StickerItem("35", "(^_^)", "Emoji", listOf("kaomoji", "happy")),
                StickerItem("36", "(>_<)", "Emoji", listOf("kaomoji", "upset")),
                StickerItem("37", "¯\\_(ツ)_/¯", "Emoji", listOf("kaomoji", "shrug")),
                StickerItem("38", "(•_•)", "Emoji", listOf("kaomoji", "neutral")),
                StickerItem("39", "(⌐■_■)", "Cool", listOf("kaomoji", "glasses")),
                StickerItem("40", "ʕ•ᴥ•ʔ", "Emoji", listOf("kaomoji", "bear")),

                // --- Hand Signs ---
                StickerItem("41", "👍", "Emoji", listOf("thumbs", "up")),
                StickerItem("42", "👎", "Emoji", listOf("thumbs", "down")),
                StickerItem("43", "👋", "Emoji", listOf("wave", "hello")),
                StickerItem("44", "🙌", "Party", listOf("hands", "celebrate")),
                StickerItem("45", "🫶", "Love", listOf("heart", "hands")),
                StickerItem("46", "✌️", "Cool", listOf("peace", "victory")),

                // --- Text Bubbles ---
                StickerItem("47", "💬", "Text", listOf("bubble", "speech")),
                StickerItem("48", "💭", "Text", listOf("bubble", "thought")),
                StickerItem("49", "🗯️", "Text", listOf("bubble", "shout")),
                StickerItem("50", "💤", "Text", listOf("sleep", "zzz")),
                StickerItem("51", "💢", "Text", listOf("anger", "vein")),
                StickerItem("52", "💥", "Cool", listOf("boom", "pow")),
                StickerItem("53", "💯", "Cool", listOf("100", "score")),

                // --- Party & Objects ---
                StickerItem("54", "🎉", "Party", listOf("celebrate", "popper")),
                StickerItem("55", "🎈", "Party", listOf("balloon")),
                StickerItem("56", "🎁", "Party", listOf("gift", "present")),
                StickerItem("57", "🎂", "Food", listOf("cake", "birthday")),
                StickerItem("58", "🏆", "Cool", listOf("trophy", "win")),
                StickerItem("59", "👑", "Cool", listOf("crown", "royal")),
                StickerItem("60", "💎", "Cool", listOf("gem", "rich")),
                StickerItem("61", "💍", "Love", listOf("ring", "wedding")),
                StickerItem("62", "💄", "Cool", listOf("makeup", "beauty")),
                StickerItem("63", "🕶️", "Cool", listOf("glasses", "fashion")),
                StickerItem("64", "📷", "Cool", listOf("camera", "photo")),
                StickerItem("65", "🎧", "Cool", listOf("headphones", "music")),
                StickerItem("66", "🎵", "Cool", listOf("music", "note")),
                StickerItem("67", "🎮", "Cool", listOf("game", "play")),
                StickerItem("68", "📱", "Cool", listOf("phone", "tech")),
                StickerItem("69", "💻", "Cool", listOf("laptop", "tech")),
                StickerItem("70", "💡", "Cool", listOf("idea", "light")),
                StickerItem("71", "🚀", "Cool", listOf("rocket", "space")),
                StickerItem("72", "🚗", "Cool", listOf("car", "drive")),
                StickerItem("73", "✈️", "Cool", listOf("plane", "travel")),

                // --- Animals ---
                StickerItem("74", "🐶", "Animal", listOf("dog", "puppy")),
                StickerItem("75", "🐱", "Animal", listOf("cat", "kitten")),
                StickerItem("76", "🐰", "Animal", listOf("rabbit", "bunny")),
                StickerItem("77", "🦊", "Animal", listOf("fox", "wild")),
                StickerItem("78", "🐻", "Animal", listOf("bear", "wild")),
                StickerItem("79", "🐼", "Animal", listOf("panda", "bear")),
                StickerItem("80", "🐯", "Animal", listOf("tiger", "cat")),
                StickerItem("81", "🦁", "Animal", listOf("lion", "cat")),
                StickerItem("82", "🐷", "Animal", listOf("pig", "farm")),
                StickerItem("83", "🦄", "Animal", listOf("unicorn", "magic")),
                StickerItem("84", "🦋", "Animal", listOf("butterfly", "pretty")),

                // --- Food ---
                StickerItem("85", "🍕", "Food", listOf("pizza")),
                StickerItem("86", "🍔", "Food", listOf("burger")),
                StickerItem("87", "🍟", "Food", listOf("fries")),
                StickerItem("88", "🍦", "Food", listOf("ice", "cream")),
                StickerItem("89", "🍩", "Food", listOf("donut")),
                StickerItem("90", "🍺", "Food", listOf("beer", "drink")),
                StickerItem("91", "☕", "Food", listOf("coffee"))
        )
    }

    val filteredStickers =
            remember(searchQuery, selectedCategory) {
                stickers.filter { sticker ->
                    val matchesCategory =
                            selectedCategory == "All" ||
                                    sticker.categoryId == selectedCategory ||
                                    (selectedCategory == "Emoji" && true) // Simplified
                    val matchesSearch =
                            searchQuery.isBlank() ||
                                    sticker.tags.any { it.contains(searchQuery, ignoreCase = true) }
                    matchesCategory && matchesSearch
                }
            }

    Dialog(
            onDismissRequest = onDismiss,
            properties =
                    DialogProperties(
                            usePlatformDefaultWidth = false,
                            decorFitsSystemWindows = false
                    )
    ) {
        Scaffold(
                containerColor = Color.Black,
                topBar = {
                    Column(modifier = Modifier.background(Color.Black)) {
                        // Header
                        Box(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(16.dp)) {
                            IconButton(
                                    onClick = onDismiss,
                                    modifier = Modifier.align(Alignment.CenterStart)
                            ) { Icon(Icons.Default.Close, "Close", tint = Color.White) }

                            Text(
                                    text = "Discover",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.align(Alignment.Center)
                            )

                            IconButton(
                                    onClick = { /* TODO: Search Focus */},
                                    modifier = Modifier.align(Alignment.CenterEnd)
                            ) { Icon(Icons.Default.Search, "Search", tint = Color.White) }
                        }

                        // Search Bar
                        OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier =
                                        Modifier.fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                                .height(50.dp),
                                placeholder = {
                                    Text("Search stickers, artists...", color = Color.Gray)
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Search, null, tint = Color.Gray)
                                },
                                colors =
                                        OutlinedTextFieldDefaults.colors(
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
                                        modifier =
                                                Modifier.clip(RoundedCornerShape(20.dp))
                                                        .background(
                                                                if (isSelected) Color.White
                                                                else Color(0xFF1E1E1E)
                                                        )
                                                        .clickable {
                                                            selectedCategory = category.id
                                                        }
                                                        .padding(
                                                                horizontal = 16.dp,
                                                                vertical = 8.dp
                                                        )
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
                    contentPadding =
                            PaddingValues(
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
                            modifier =
                                    Modifier.aspectRatio(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFF1E1E1E))
                                            .clickable { onStickerSelected(sticker.emoji) },
                            contentAlignment = Alignment.Center
                    ) {
                        Text(text = sticker.emoji, fontSize = 40.sp)
                        if (sticker.isPremium) {
                            Box(
                                    modifier =
                                            Modifier.align(Alignment.BottomEnd)
                                                    .padding(4.dp)
                                                    .background(
                                                            Color(0xFFFFD700),
                                                            RoundedCornerShape(4.dp)
                                                    )
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(
                                        "PLUS",
                                        fontSize = 8.sp,
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
