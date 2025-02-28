package com.aubynsamuel.flashsend.chatRoom

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ReactionPicker(onReactionSelected: (String) -> Unit) {
    val frequentlyUsedEmojis = listOf("😀", "❤️", "😂", "😭", "👍")

    var showFullPicker by remember { mutableStateOf(false) }

    var animated by remember { mutableStateOf(false) }
    val transY = animateFloatAsState(
        targetValue = if (animated) -2f else 2f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
    )
    LaunchedEffect(Unit) {
        while (true) {
            delay(300)
            animated = !animated
        }
    }

    Row(
        modifier = Modifier
            .padding(8.dp)
            .graphicsLayer { translationY = transY.value },
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        frequentlyUsedEmojis.forEach { emoji ->
            Text(
                text = emoji,
                fontSize = 20.sp,
                modifier = Modifier
                    .clickable { onReactionSelected(emoji) }
                    .padding(4.dp)
            )
        }
        // Plus button to trigger the full picker
        Text(
            text = "+",
            fontSize = 20.sp,
            modifier = Modifier
                .clickable { showFullPicker = true }
                .padding(4.dp)
        )
    }

    if (showFullPicker) {
        EmojiPickerDialog(
            emojiCategories = emojiCategories,
            onEmojiSelected = { selectedEmoji ->
                onReactionSelected(selectedEmoji)
                showFullPicker = false
            },
            onDismiss = { showFullPicker = false }
        )
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun EmojiPickerDialog(
    emojiCategories: Map<String, List<String>>,
    onEmojiSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val categories = emojiCategories.keys.toList()
    val pagerState = rememberPagerState(initialPage = 0)
    val coroutineScope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = { Text(text = "Select Emoji", fontSize = 18.sp) },
        text = {
            Column {
                ScrollableTabRow(
                    selectedTabIndex = pagerState.currentPage,
                    edgePadding = 8.dp
                ) {
                    categories.forEachIndexed { index, category ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.scrollToPage(index)
                                }
                            },
                            text = {
                                Text(
                                    text = category,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        )
                    }
                }

                HorizontalPager(
                    count = categories.size,
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth()
                ) { page ->
                    val categoryName = categories[page]
                    val emojis = emojiCategories[categoryName] ?: emptyList()

                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 36.dp),
                        contentPadding = PaddingValues(8.dp),
                        modifier = Modifier.height(300.dp)
                    ) {
                        items(emojis.size) { index ->
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .padding(4.dp)
                                    .clip(CircleShape)
                                    .clickable { onEmojiSelected(emojis[index]) }
                            ) {
                                Text(
                                    text = emojis[index],
                                    fontSize = 24.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        properties = DialogProperties(
            dismissOnClickOutside = true,
            dismissOnBackPress = true
        )
    )
}


val emojiCategories = mapOf(
    "Smileys & Emotions" to listOf(
        "😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣", "😊", "😇",
        "😉", "😌", "😍", "🥰", "😘", "😗", "😙", "😚", "😐", "😑",
        "😶", "😏", "😒", "🙄", "😬", "😮", "😲", "😳", "🥺", "😦",
        "😧", "😨", "😰", "😥", "😢", "😭", "😱", "😖", "😣", "😞",
        "😓", "😩", "😫", "😤", "😡", "🤬", "😴", "😪", "🤤", "😷",
        "🤒", "🤕", "🤢", "🤮", "🤧", "🥵", "🥶", "🥴", "😵", "🤯",
        "😎", "🤓", "🧐", "😕", "🫤", "🫢", "🫣", "🤠",
    ),
    "Skulls & Creatures" to listOf(
        "💀", "☠️", "👻", "👽", "👾", "🤖", "💩", "🙊", "🙉", "🙈",
        "🦄", "🐉", "🐲", "🧌", "🦹‍♂️", "🦸‍♂️", "🧙‍♂️", "🧛‍♂️", "🧟‍♂️", "🧞‍♂️"
    ),
    "People & Body" to listOf(
        "👋", "🤚", "🖐️", "✋", "🖖", "👌", "🤌", "🤏", "✌️", "🤞",
        "🤟", "🤘", "🤙", "👈", "👉", "👆", "👇", "👍", "👎", "👏",
        "🙌", "👐", "🤲", "🤝", "🙏", "💪", "🦾", "🦿", "🖕", "✍️",
        "💅", "🦶", "🦵", "👂", "🦻", "👃", "🧠", "🫀", "🫁", "🦷",
        "👀", "👁️", "👅", "👄", "🫦", "🦴"
    ),
    "Animals & Nature" to listOf(
        "🐶", "🐱", "🐭", "🐹", "🐰", "🦊", "🐻", "🐼", "🐨", "🐯",
        "🦁", "🐮", "🐷", "🐸", "🐵", "🐔", "🐧", "🐦", "🦆", "🦅",
        "🦉", "🦇", "🐺", "🐗", "🐴", "🦄", "🐝", "🐛", "🦋", "🐌",
        "🐞", "🐜", "🪲", "🦟", "🪰", "🦠", "🐢", "🐍", "🦎", "🦖",
        "🦕", "🐙", "🦑", "🦞", "🦀", "🐡", "🐠", "🐟", "🐬", "🐳",
        "🦈", "🦭", "🐊", "🦦", "🦡", "🦥", "🦘", "🦨"
    ),
    "Celebrations" to listOf(
        "🎉", "🎊", "🥳", "🎂", "🎈", "🍾", "🥂", "🍰", "🪅", "🪄"
    ),
    "Food & Drink" to listOf(
        "🍏", "🍎", "🍐", "🍊", "🍋", "🍌", "🍉", "🍇", "🍓", "🍈",
        "🍒", "🍑", "🍍", "🥝", "🍅", "🍆", "🥑", "🥦", "🥬", "🥒",
        "🌶️", "🌽", "🥕", "🫑", "🥔", "🫘", "🥜", "🍞", "🥐", "🥖",
        "🥨", "🥞", "🧀", "🍗", "🍖", "🍕", "🌭", "🍔", "🍟", "🥗",
        "🍿", "🧂", "🥫", "🍩", "🍪", "🎂", "🍰", "🧁", "🥧", "🍫",
        "🍬", "🍭", "☕", "🫖", "🍵", "🥤", "🧃", "🍷", "🍸", "🍹",
        "🍺", "🍻", "🥂", "🥃", "🫗"
    ),
    "Activities & Sports" to listOf(
        "⚽", "🏀", "🏈", "⚾", "🥎", "🎾", "🏐", "🏉", "🥏", "🎱",
        "🪀", "🏓", "🏸", "🏒", "🏑", "🥍", "🏏", "🪃", "🥅", "⛳",
        "🏹", "🎣", "🤿", "🥊", "🥋", "🎽", "🛹", "🛼", "⛸️", "🥌",
        "🎿", "⛷️", "🏂", "🪂", "🏋️‍♂️", "🏋️‍♀️", "🤼‍♂️", "🤼‍♀️"
    ),
    "Objects & Symbols" to listOf(
        "📱", "💻", "⌨️", "🖥️", "🖨️", "🖱️", "🖲️", "💾", "💿", "📀",
        "🎥", "🎞️", "📽️", "📺", "📷", "📸", "📹", "📡", "🔋", "🔌",
        "💡", "🔦", "🕯️", "🪔", "📡", "🔑", "🗝️", "🔐", "🔒", "🔓",
        "🛑", "🚸", "🚫", "⛔", "❌", "✅", "⚠️", "🔰", "♻️", "🚷"
    ),
    "Transport & Travel" to listOf(
        "🚗", "🚕", "🚙", "🚌", "🚎", "🏎️", "🚓", "🚑", "🚒", "🚚",
        "🚛", "🚜", "🛵", "🏍️", "🛻", "🚄", "🚅", "🚆", "🚇", "🚉",
        "🚀", "🛸", "🚁", "🛶", "⛵", "🚤", "🛳️", "🛥️", "🛩️", "✈️"
    ),
    "Flags" to listOf(
        "🏳️", "🏴", "🏁", "🚩", "🇺🇸", "🇬🇧", "🇨🇦", "🇦🇺", "🇫🇷", "🇩🇪",
        "🇯🇵", "🇰🇷", "🇨🇳", "🇧🇷", "🇮🇳", "🇲🇽", "🇿🇦", "🇪🇸", "🇮🇹", "🇷🇺"
    )
)
