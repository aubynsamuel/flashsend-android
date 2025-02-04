package com.aubynsamuel.flashsend.chatRoom

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aubynsamuel.flashsend.R
import com.aubynsamuel.flashsend.ui.theme.FlashSendTheme
import kotlin.collections.minus
import kotlin.collections.plus

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!", modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FlashSendTheme {
        Greeting("Android")
    }
    Image(
        painter = painterResource(id = R.drawable.ic_launcher_foreground), contentDescription = ""
    )
    LazyColumn(
        Modifier
            .padding(20.dp)
            .fillMaxSize()

    ) {
        items(1000) { i ->
            Row {
                Text(
                    text = i.toString(),
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .background(color = Color.Red)
                )
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_background),
                    contentDescription = "Image",
                    Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun MyList() {
    LazyColumn(
        modifier = Modifier
            .padding(20.dp)
            .fillMaxSize()
    ) {
        items(1000) { i ->
            Box(modifier = Modifier.padding(20.dp)) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_background),
                    contentDescription = "Image",
                    Modifier
                        .height(200.dp)
                        .width(200.dp)
                )
                Text(
                    text = i.toString(),
                    modifier = Modifier
                        .width(200.dp)
                        .height(200.dp)
                        .background(color = determineColor(i))
                )
                Spacer(modifier = Modifier.height(20.dp))
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Nothing")
            }
        }
    }
}

fun determineColor(number: Int): Color {
    return if (number % 8 == 0) {
        Color.Red
    } else if (number % 6 == 0) {
        Color.Blue
    } else if (number % 4 == 0) {
        Color.Green
    } else if (number % 2 == 0) {
        Color.Black
    } else Color.Yellow

    return Color.Yellow
}

data class ListItem(
    val id: Int,
    val name: String,
    val imageResId: Int  // Resource ID for drawable
)

@Composable
fun AnimatedImageList(items: List<ListItem>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp), reverseLayout = true,
        horizontalAlignment = Alignment.End
    ) {
        items(items) { item ->
            AnimatedListItem(item)
        }
    }
}

@Composable
fun AnimatedListItem(item: ListItem) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = "${item.name} $isExpanded",
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Image(
                painter = painterResource(id = item.imageResId),
                contentDescription = "Image for ${item.name}",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isExpanded) 300.dp else 150.dp)
                    .clickable { isExpanded = !isExpanded },
                contentScale = if (isExpanded) ContentScale.Fit else ContentScale.Crop
            )
        }
    }
}

// Example usage
@Composable
fun MainScreen() {
    // Example assuming you have drawable resources named img_1, img_2, etc.
    val items = List(1000) { index ->
        ListItem(
            id = index,
            name = "Item $index",
            imageResId = R.drawable.ic_launcher_foreground // You'll need to implement this based on your resources
        )
    }

    AnimatedImageList(items)
}

data class ListObject(
    val name: String,
    val checked: Boolean = false
)

@Composable
fun TODO() {
    var value by remember { mutableStateOf("") }
    var items by remember { mutableStateOf(listOf<String>()) }
    var checked by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Bottom
    ) {
        Text(text = "To do app")
        LazyColumn {
            items(items) { item ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = item)
                    Checkbox(checked = checked, onCheckedChange = { b ->
                        !b
                        if (b == true) {
                            items -= item
                        }
                    })
                }
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 10.dp)
        ) {
            TextField(
                value = value,
                onValueChange = { text: String -> value = text },
                modifier = Modifier.fillMaxWidth(0.8f),
            )
            Button(onClick = {
                if (value.isNotBlank()) {
                    items += value
                    value = ""
                }
            }) {
                Text(text = "Add")
            }
        }
    }
}
