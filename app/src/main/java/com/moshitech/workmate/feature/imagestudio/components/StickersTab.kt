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
fun StickersTab(
    onStickerSelected: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    
    val allStickers = remember {
        listOf(
            StickerData("😀", listOf("smile", "happy", "face")),
            StickerData("😂", listOf("joy", "laugh", "happy", "tears")),
            StickerData("🥰", listOf("love", "hearts", "happy")),
            StickerData("😎", listOf("cool", "sunglasses", "happy")),
            StickerData("🤔", listOf("thinking", "hmm", "face")),
            StickerData("😭", listOf("cry", "sad", "tears")),
            StickerData("🤯", listOf("mindblown", "shock", "face")),
            StickerData("😱", listOf("scream", "shock", "scared")),
            StickerData("👍", listOf("thumbs", "up", "like", "good")),
            StickerData("👎", listOf("thumbs", "down", "dislike", "bad")),
            StickerData("👋", listOf("wave", "hello", "hand")),
            StickerData("🙌", listOf("hands", "celebrate", "up")),
            StickerData("🔥", listOf("fire", "hot", "lit")),
            StickerData("✨", listOf("sparkles", "shine", "stars")),
            StickerData("❤️", listOf("heart", "love", "red")),
            StickerData("💯", listOf("100", "score", "perfect")),
            StickerData("🎉", listOf("party", "celebrate", "popper")),
            StickerData("🌟", listOf("star", "glow", "shine")),
            StickerData("💡", listOf("idea", "light", "bulb")),
            StickerData("🚀", listOf("rocket", "space", "fly")),
            StickerData("🍕", listOf("pizza", "food", "slice")),
            StickerData("🍔", listOf("burger", "food", "fast")),
            StickerData("🍦", listOf("ice", "cream", "dessert")),
            StickerData("🍺", listOf("beer", "drink", "alcohol")),
            StickerData("🐶", listOf("dog", "puppy", "animal")),
            StickerData("🐱", listOf("cat", "kitten", "animal")),
            StickerData("🦄", listOf("unicorn", "fantasy", "horse")),
            StickerData("🌈", listOf("rainbow", "color", "sky")),
            StickerData("☀️", listOf("sun", "weather", "hot")),
            StickerData("🌙", listOf("moon", "night", "dark")),
            StickerData("🎵", listOf("music", "note", "sound")),
            StickerData("📷", listOf("camera", "photo", "picture")),
            StickerData("⚽", listOf("soccer", "ball", "sport")),
            StickerData("🏀", listOf("basketball", "ball", "sport")),
            StickerData("🎮", listOf("game", "play", "controller")),
            StickerData("🚗", listOf("car", "vehicle", "drive")),
            StickerData("✈️", listOf("plane", "fly", "travel")),
            StickerData("⌚", listOf("watch", "time", "clock")),
            StickerData("📱", listOf("phone", "mobile", "tech")),
            StickerData("💻", listOf("laptop", "computer", "tech")),
            StickerData("🕶️", listOf("glasses", "fashion", "cool")),
            StickerData("👑", listOf("crown", "king", "queen", "royal")),
            StickerData("👻", listOf("ghost", "scary", "halloween")),
            StickerData("👽", listOf("alien", "space", "ufo")),
            StickerData("🤖", listOf("robot", "tech", "bot")),
            StickerData("💩", listOf("poop", "funny", "piler")),
            StickerData("💀", listOf("skull", "death", "skeleton")),
            StickerData("🤡", listOf("clown", "funny", "circus"))
        )
    }

    val filteredStickers = if (searchQuery.isBlank()) {
        allStickers
    } else {
        allStickers.filter { sticker ->
            sticker.tags.any { tag -> tag.contains(searchQuery, ignoreCase = true) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize() // Fill the parent resizable panel
            .background(Color(0xFF1E1E1E))
            .padding(top = 8.dp)
    ) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .height(50.dp),
            placeholder = { Text("Search stickers...", color = Color.Gray, fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray) },
            singleLine = true,
            textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
            colors = OutlinedTextFieldDefaults.colors(
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
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF2C2C2C))
                        .clickable { onStickerSelected(sticker.emoji) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = sticker.emoji,
                        fontSize = 32.sp
                    )
                }
            }
        }
    }
}
