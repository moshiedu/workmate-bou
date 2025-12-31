package com.moshitech.workmate.feature.imagestudio.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class StickerData(val emoji: String, val tags: List<String>)

@Composable
fun StickersTab(onStickerSelected: (String) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }

    val allStickers = remember {
        listOf(
                // --- Emotions & Faces ---
                StickerData("😀", listOf("smile", "happy", "face")),
                StickerData("😂", listOf("joy", "laugh", "happy", "tears")),
                StickerData("🥰", listOf("love", "hearts", "happy")),
                StickerData("😎", listOf("cool", "sunglasses", "happy")),
                StickerData("🤔", listOf("thinking", "hmm", "face")),
                StickerData("😭", listOf("cry", "sad", "tears")),
                StickerData("🤯", listOf("mindblown", "shock", "face")),
                StickerData("😱", listOf("scream", "shock", "scared")),
                StickerData("🤬", listOf("angry", "curse", "mad")),
                StickerData("🤡", listOf("clown", "funny", "circus")),
                StickerData("👻", listOf("ghost", "scary", "halloween")),
                StickerData("💀", listOf("skull", "death", "skeleton")),
                StickerData("👽", listOf("alien", "space", "ufo")),
                StickerData("🤖", listOf("robot", "tech", "bot")),
                StickerData("💩", listOf("poop", "funny", "piler")),

                // --- Kaomoji / "Custom" Style ---
                StickerData("(^_^)", listOf("kaomoji", "happy", "cute")),
                StickerData("(>_<)", listOf("kaomoji", "upset", "cute")),
                StickerData("¯\\_(ツ)_/¯", listOf("kaomoji", "shrug", "dunno")),
                StickerData("(•_•)", listOf("kaomoji", "neutral", "face")),
                StickerData("(⌐■_■)", listOf("kaomoji", "cool", "glasses")),
                StickerData("ʕ•ᴥ•ʔ", listOf("kaomoji", "bear", "cute")),
                StickerData("(✿◠‿◠)", listOf("kaomoji", "happy", "flower")),

                // --- Hand Signs ---
                StickerData("👍", listOf("thumbs", "up", "like", "good")),
                StickerData("👎", listOf("thumbs", "down", "dislike", "bad")),
                StickerData("👋", listOf("wave", "hello", "hand")),
                StickerData("🙌", listOf("hands", "celebrate", "up")),
                StickerData("🫶", listOf("heart", "hands", "love")),
                StickerData("✌️", listOf("peace", "hand", "victory")),
                StickerData("🤞", listOf("fingers", "crossed", "luck")),

                // --- Celestial & Abstract ---
                StickerData("✨", listOf("sparkles", "shine", "stars")),
                StickerData("🌟", listOf("star", "glow", "shine")),
                StickerData("💫", listOf("dizzy", "star", "shoot")),
                StickerData("🌙", listOf("moon", "night", "dark")),
                StickerData("☀️", listOf("sun", "weather", "hot")),
                StickerData("⚡", listOf("bolt", "lightning", "power")),
                StickerData("❄️", listOf("snowflake", "ice", "cold")),
                StickerData("🔥", listOf("fire", "hot", "lit")),
                StickerData("🌈", listOf("rainbow", "color", "sky")),

                // --- Hearts & Love ---
                StickerData("❤️", listOf("heart", "love", "red")),
                StickerData("🧡", listOf("heart", "love", "orange")),
                StickerData("💛", listOf("heart", "love", "yellow")),
                StickerData("💚", listOf("heart", "love", "green")),
                StickerData("💙", listOf("heart", "love", "blue")),
                StickerData("💜", listOf("heart", "love", "purple")),
                StickerData("🖤", listOf("heart", "love", "black")),
                StickerData("🤍", listOf("heart", "love", "white")),
                StickerData("💔", listOf("heart", "break", "sad")),
                StickerData("💘", listOf("heart", "arrow", "love")),

                // --- Text / Bubbles ---
                StickerData("💬", listOf("bubble", "speech", "chat")),
                StickerData("💭", listOf("bubble", "thought", "cloud")),
                StickerData("🗯️", listOf("bubble", "shout", "anger")),
                StickerData("💤", listOf("sleep", "zzz", "tired")),
                StickerData("💢", listOf("anger", "vein", "mad")),
                StickerData("💥", listOf("boom", "explosion", "pow")),
                StickerData("💯", listOf("100", "score", "perfect")),

                // --- Objects & Activities ---
                StickerData("🎉", listOf("party", "celebrate", "popper")),
                StickerData("🎈", listOf("balloon", "party", "float")),
                StickerData("🎁", listOf("gift", "present", "box")),
                StickerData("🎂", listOf("cake", "birthday", "food")),
                StickerData("🏆", listOf("trophy", "win", "cup")),
                StickerData("🥇", listOf("medal", "first", "win")),
                StickerData("👑", listOf("crown", "king", "queen", "royal")),
                StickerData("💎", listOf("gem", "diamond", "jewelry")),
                StickerData("💍", listOf("ring", "wedding", "jewelry")),
                StickerData("💄", listOf("lipstick", "makeup", "beauty")),
                StickerData("🕶️", listOf("glasses", "fashion", "cool")),
                StickerData("📷", listOf("camera", "photo", "picture")),
                StickerData("🎥", listOf("movie", "camera", "film")),
                StickerData("🎧", listOf("headphones", "music", "sound")),
                StickerData("🎵", listOf("music", "note", "sound")),
                StickerData("🎤", listOf("mic", "sing", "karaoke")),
                StickerData("🎮", listOf("game", "play", "controller")),
                StickerData("📱", listOf("phone", "mobile", "tech")),
                StickerData("💻", listOf("laptop", "computer", "tech")),
                StickerData("💡", listOf("idea", "light", "bulb")),
                StickerData("🚀", listOf("rocket", "space", "fly")),
                StickerData("🚗", listOf("car", "vehicle", "drive")),
                StickerData("✈️", listOf("plane", "fly", "travel")),
                StickerData("🗺️", listOf("map", "travel", "world")),

                // --- Animals ---
                StickerData("🐶", listOf("dog", "puppy", "animal")),
                StickerData("🐱", listOf("cat", "kitten", "animal")),
                StickerData("🐭", listOf("mouse", "rat", "animal")),
                StickerData("🐹", listOf("hamster", "rodent", "animal")),
                StickerData("🐰", listOf("rabbit", "bunny", "animal")),
                StickerData("🦊", listOf("fox", "wild", "animal")),
                StickerData("🐻", listOf("bear", "wild", "animal")),
                StickerData("🐼", listOf("panda", "bear", "animal")),
                StickerData("🐨", listOf("koala", "bear", "animal")),
                StickerData("🐯", listOf("tiger", "cat", "wild")),
                StickerData("🦁", listOf("lion", "cat", "wild")),
                StickerData("🐮", listOf("cow", "farm", "animal")),
                StickerData("🐷", listOf("pig", "farm", "animal")),
                StickerData("🐸", listOf("frog", "green", "animal")),
                StickerData("🐵", listOf("monkey", "ape", "animal")),
                StickerData("🦄", listOf("unicorn", "fantasy", "horse")),
                StickerData("🦋", listOf("butterfly", "insect", "pretty")),

                // --- Food ---
                StickerData("🍎", listOf("apple", "fruit", "food")),
                StickerData("🍓", listOf("strawberry", "fruit", "food")),
                StickerData("🍒", listOf("cherry", "fruit", "food")),
                StickerData("🍑", listOf("peach", "fruit", "butt")),
                StickerData("🥑", listOf("avocado", "fruit", "food")),
                StickerData("🍕", listOf("pizza", "food", "slice")),
                StickerData("🍔", listOf("burger", "food", "fast")),
                StickerData("🍟", listOf("fries", "food", "fast")),
                StickerData("🌭", listOf("hotdog", "food", "fast")),
                StickerData("🍿", listOf("popcorn", "movie", "snack")),
                StickerData("🍩", listOf("donut", "sweet", "dessert")),
                StickerData("🍪", listOf("cookie", "sweet", "dessert")),
                StickerData("🍦", listOf("ice", "cream", "dessert")),
                StickerData("🍺", listOf("beer", "drink", "alcohol")),
                StickerData("🍷", listOf("wine", "drink", "alcohol")),
                StickerData("☕", listOf("coffee", "drink", "warm"))
        )
    }

    val filteredStickers =
            if (searchQuery.isBlank()) {
                allStickers
            } else {
                allStickers.filter { sticker ->
                    sticker.tags.any { tag -> tag.contains(searchQuery, ignoreCase = true) }
                }
            }

    Column(
            modifier =
                    Modifier.fillMaxSize() // Fill the parent resizable panel
                            .background(Color(0xFF1E1E1E))
                            .padding(top = 8.dp)
    ) {
        // Search Bar
        OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier =
                        Modifier.fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .height(50.dp),
                placeholder = { Text("Search stickers...", color = Color.Gray, fontSize = 14.sp) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray)
                },
                singleLine = true,
                textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                colors =
                        OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color.Gray,
                                cursorColor = MaterialTheme.colorScheme.primary,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                        ),
                shape = RoundedCornerShape(24.dp)
        )

        // Grid
        LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(filteredStickers) { sticker ->
                Box(
                        modifier =
                                Modifier.aspectRatio(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF2C2C2C))
                                        .clickable { onStickerSelected(sticker.emoji) },
                        contentAlignment = Alignment.Center
                ) { Text(text = sticker.emoji, fontSize = 32.sp) }
            }
        }
    }
}
