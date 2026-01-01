package com.moshitech.workmate.feature.imagestudio.components

import androidx.compose.foundation.Image
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
import androidx.compose.ui.semantics.Role.Companion.Image
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

// Mock Data Models
data class StickerCategory(val id: String, val name: String)

data class StickerItem(
        val id: String,
        val emoji: String?, // Nullable for Image based stickers
        val resId: Int? = null, // Resource ID for Image stickers
        val categoryId: String,
        val tags: List<String>,
        val isPremium: Boolean = false
)

@Composable
fun StickerDiscoveryScreen(onDismiss: () -> Unit, onStickerSelected: (String?, Int?) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = remember {
        listOf(
                StickerCategory("All", "All"),
                StickerCategory("Recent", "Recent"),
                StickerCategory("Realistic", "Realistic"), // New Category
                StickerCategory("Islamic", "Islamic"), // New Category
                StickerCategory("Emoji", "Emoji"),
                StickerCategory("Love", "Love"),
                StickerCategory("Cool", "Cool"),
                StickerCategory("Shape", "Shape"),
                StickerCategory("Text", "Text")
        )
    }

    val stickers = remember {
        listOf(
                // --- Realistic ---
                StickerItem("201", null, com.moshitech.workmate.R.drawable.sticker_real_flower, "Realistic", listOf("flower", "rose", "nature")),
                StickerItem("202", null, com.moshitech.workmate.R.drawable.sticker_real_cat, "Realistic", listOf("cat", "animal", "pet")),
                StickerItem("203", null, com.moshitech.workmate.R.drawable.sticker_real_car, "Realistic", listOf("car", "vehicle", "sport")),
                StickerItem("204", null, com.moshitech.workmate.R.drawable.sticker_real_pizza, "Realistic", listOf("pizza", "food", "yummy")),

                // --- Emotions & Faces ---
                StickerItem("1", "😀", null, "Emoji", listOf("smile", "happy")),
                StickerItem("2", "😂", null, "Emoji", listOf("laugh", "joy")),
                StickerItem("3", "🥰", null, "Emoji", listOf("love", "hearts")),

                // --- Islamic ---
                StickerItem("101", "🕌", null, "Islamic", listOf("mosque", "masjid", "islam", "prayer")),
                StickerItem("102", "🕋", null, "Islamic", listOf("kaaba", "mecca", "islam", "hajj")),
                StickerItem("103", "📿", null, "Islamic", listOf("tasbih", "beads", "worship", "dhikr")),
                StickerItem("104", "🤲", null, "Islamic", listOf("dua", "pray", "hands")),
                StickerItem("105", "🌙", null, "Islamic", listOf("moon", "crescent", "ramadan", "night")),
                StickerItem("106", "⭐", null, "Islamic", listOf("star", "light")),
                StickerItem("107", "🕯️", null, "Islamic", listOf("candle", "light")),
                StickerItem("108", "🛐", null, "Islamic", listOf("pray", "mat", "worship")),
                StickerItem("109", "🐫", null, "Islamic", listOf("camel", "desert")),
                StickerItem("110", "🌴", null, "Islamic", listOf("palm", "dates", "tree")),
                StickerItem("111", "⛺", null, "Islamic", listOf("tent", "desert")),
                StickerItem("112", "🏜️", null, "Islamic", listOf("desert", "sand")),
                StickerItem("113", "✨", null, "Islamic", listOf("sparkle", "light", "nur")),
                StickerItem("114", "🟢", null, "Islamic", listOf("green", "circle")),
                StickerItem("115", "📖", null, "Islamic", listOf("book", "quran", "read")),
                StickerItem("116", "﷽", null, "Islamic", listOf("bismillah", "calligraphy")),
                StickerItem("117", "ﷲ", null, "Islamic", listOf("allah", "god", "calligraphy")),
                StickerItem("118", "ﷻ", null, "Islamic", listOf("jalla", "jalaluhu", "calligraphy")),
                StickerItem("119", "ﷺ", null, "Islamic", listOf("pbuh", "prophet", "calligraphy")),
                StickerItem("120", "ﷴ", null, "Islamic", listOf("muhammad", "prophet", "calligraphy")),
                StickerItem("121", "۞", null, "Islamic", listOf("symbol", "star", "quran")),
                StickerItem("122", "☪️", null, "Islamic", listOf("star", "moon", "symbol")),
                StickerItem("123", "☝️", null, "Islamic", listOf("one", "tawhid", "finger")),
                StickerItem("124", "👳", null, "Islamic", listOf("man", "turban")),
                StickerItem("125", "🧕", null, "Islamic", listOf("woman", "hijab")),
                StickerItem("128", "🤝", null, "Islamic", listOf("salam", "shake", "peace")),
                StickerItem("129", "🌄", null, "Islamic", listOf("sunrise", "fajr")),
                StickerItem("130", "🌇", null, "Islamic", listOf("sunset", "maghrib")),
                StickerItem("132", "🥛", null, "Islamic", listOf("milk", "sunnah")),
                StickerItem("133", "🍵", null, "Islamic", listOf("tea", "chai")),
                StickerItem("134", "🌹", null, "Islamic", listOf("rose", "flower")),
                StickerItem("135", "💐", null, "Islamic", listOf("flowers", "bouquet")),

                StickerItem("4", "😎", null, "Cool", listOf("sunglasses", "cool")),
                StickerItem("5", "🤔", null, "Emoji", listOf("thinking")),
                StickerItem("6", "😭", null, "Emoji", listOf("cry", "sad")),
                StickerItem("7", "🤯", null, "Emoji", listOf("mindblown")),
                StickerItem("8", "😱", null, "Emoji", listOf("scream", "shock")),
                StickerItem("9", "🤬", null, "Emoji", listOf("angry", "mad")),
                StickerItem("10", "🤡", null, "Emoji", listOf("clown", "funny")),
                StickerItem("11", "👻", null, "Spooky", listOf("ghost", "halloween")),
                StickerItem("12", "💀", null, "Spooky", listOf("skull", "death")),
                StickerItem("13", "👽", null, "Space", listOf("alien", "ufo")),
                StickerItem("14", "🤖", null, "Cool", listOf("robot", "tech")),
                StickerItem("15", "💩", null, "Emoji", listOf("poop", "funny")),

                // --- Love & Hearts ---
                StickerItem("16", "❤️", null, "Love", listOf("heart", "red")),
                StickerItem("17", "🧡", null, "Love", listOf("heart", "orange")),
                StickerItem("18", "💛", null, "Love", listOf("heart", "yellow")),
                StickerItem("19", "💚", null, "Love", listOf("heart", "green")),
                StickerItem("20", "💙", null, "Love", listOf("heart", "blue")),
                StickerItem("21", "💜", null, "Love", listOf("heart", "purple")),
                StickerItem("22", "🖤", null, "Love", listOf("heart", "black")),
                StickerItem("23", "🤍", null, "Love", listOf("heart", "white")),
                StickerItem("24", "💔", null, "Love", listOf("heart", "break")),
                StickerItem("25", "💘", null, "Love", listOf("heart", "arrow")),

                // --- Celestial & Cool ---
                StickerItem("26", "✨", null, "Cool", listOf("sparkles", "shine")),
                StickerItem("27", "🌟", null, "Cool", listOf("star", "glow")),
                StickerItem("28", "💫", null, "Cool", listOf("dizzy", "star")),
                StickerItem("29", "🌙", null, "Nature", listOf("moon", "night")),
                StickerItem("30", "☀️", null, "Nature", listOf("sun", "day")),
                StickerItem("31", "⚡", null, "Cool", listOf("bolt", "power")),
                StickerItem("32", "❄️", null, "Nature", listOf("snow", "ice")),
                StickerItem("33", "🔥", null, "Cool", listOf("fire", "hot")),
                StickerItem("34", "🌈", null, "Nature", listOf("rainbow", "color")),

                // --- Kaomoji ---
                StickerItem("35", "(^_^)", null, "Emoji", listOf("kaomoji", "happy")),
                StickerItem("36", "(>_<)", null, "Emoji", listOf("kaomoji", "upset")),
                StickerItem("37", "¯\\_(ツ)_/¯", null, "Emoji", listOf("kaomoji", "shrug")),
                StickerItem("38", "(•_•)", null, "Emoji", listOf("kaomoji", "neutral")),
                StickerItem("39", "(⌐■_■)", null, "Cool", listOf("kaomoji", "glasses")),
                StickerItem("40", "ʕ•ᴥ•ʔ", null, "Emoji", listOf("kaomoji", "bear")),

                // --- Hand Signs ---
                StickerItem("41", "👍", null, "Emoji", listOf("thumbs", "up")),
                StickerItem("42", "👎", null, "Emoji", listOf("thumbs", "down")),
                StickerItem("43", "👋", null, "Emoji", listOf("wave", "hello")),
                StickerItem("44", "🙌", null, "Party", listOf("hands", "celebrate")),
                StickerItem("45", "🫶", null, "Love", listOf("heart", "hands")),
                StickerItem("46", "✌️", null, "Cool", listOf("peace", "victory")),

                // --- Text Bubbles ---
                StickerItem("47", "💬", null, "Text", listOf("bubble", "speech")),
                StickerItem("48", "💭", null, "Text", listOf("bubble", "thought")),
                StickerItem("49", "🗯️", null, "Text", listOf("bubble", "shout")),
                StickerItem("50", "💤", null, "Text", listOf("sleep", "zzz")),
                StickerItem("51", "💢", null, "Text", listOf("anger", "vein")),
                StickerItem("52", "💥", null, "Cool", listOf("boom", "pow")),
                StickerItem("53", "💯", null, "Cool", listOf("100", "score")),

                // --- Party & Objects ---
                StickerItem("54", "🎉", null, "Party", listOf("celebrate", "popper")),
                StickerItem("55", "🎈", null, "Party", listOf("balloon")),
                StickerItem("56", "🎁", null, "Party", listOf("gift", "present")),
                StickerItem("57", "🎂", null, "Food", listOf("cake", "birthday")),
                StickerItem("58", "🏆", null, "Cool", listOf("trophy", "win")),
                StickerItem("59", "👑", null, "Cool", listOf("crown", "royal")),
                StickerItem("60", "💎", null, "Cool", listOf("gem", "rich")),
                StickerItem("61", "💍", null, "Love", listOf("ring", "wedding")),
                StickerItem("62", "💄", null, "Cool", listOf("makeup", "beauty")),
                StickerItem("63", "🕶️", null, "Cool", listOf("glasses", "fashion")),
                StickerItem("64", "📷", null, "Cool", listOf("camera", "photo")),
                StickerItem("65", "🎧", null, "Cool", listOf("headphones", "music")),
                StickerItem("66", "🎵", null, "Cool", listOf("music", "note")),
                StickerItem("67", "🎮", null, "Cool", listOf("game", "play")),
                StickerItem("68", "📱", null, "Cool", listOf("phone", "tech")),
                StickerItem("69", "💻", null, "Cool", listOf("laptop", "tech")),
                StickerItem("70", "💡", null, "Cool", listOf("idea", "light")),
                StickerItem("71", "🚀", null, "Cool", listOf("rocket", "space")),
                StickerItem("72", "🚗", null, "Cool", listOf("car", "drive")),
                StickerItem("73", "✈️", null, "Cool", listOf("plane", "travel")),

                // --- Animals ---
                StickerItem("74", "🐶", null, "Animal", listOf("dog", "puppy")),
                StickerItem("75", "🐱", null, "Animal", listOf("cat", "kitten")),
                StickerItem("76", "🐰", null, "Animal", listOf("rabbit", "bunny")),
                StickerItem("77", "🦊", null, "Animal", listOf("fox", "wild")),
                StickerItem("78", "🐻", null, "Animal", listOf("bear", "wild")),
                StickerItem("79", "🐼", null, "Animal", listOf("panda", "bear")),
                StickerItem("80", "🐯", null, "Animal", listOf("tiger", "cat")),
                StickerItem("81", "🦁", null, "Animal", listOf("lion", "cat")),
                StickerItem("82", "🐷", null, "Animal", listOf("pig", "farm")),
                StickerItem("83", "🦄", null, "Animal", listOf("unicorn", "magic")),
                StickerItem("84", "🦋", null, "Animal", listOf("butterfly", "pretty")),

                // --- Food ---
                StickerItem("89", "🍩", null, "Food", listOf("donut")),
                StickerItem("90", "🍺", null, "Food", listOf("beer", "drink")),
                StickerItem("91", "☕", null, "Food", listOf("coffee"))
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
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF1E1E1E))
                                            .clickable { onStickerSelected(sticker.emoji, sticker.resId) }
                                            .padding(8.dp),
                            contentAlignment = Alignment.Center
                    ) {
                        if (sticker.resId != null) {
                            Image(
                                painter = androidx.compose.ui.res.painterResource(id = sticker.resId),
                                contentDescription = sticker.tags.firstOrNull(),
                                modifier = Modifier.size(64.dp)
                            )
                        } else if (sticker.emoji != null) {
                            Text(
                                text = sticker.emoji,
                                fontSize = 32.sp,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
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
