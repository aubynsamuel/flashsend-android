package com.aubynsamuel.flashsend.chatRoom

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun HeaderBar(name: String, pic: String?, goBack: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column(
//        modifier = Modifier.windowInsetsPadding(WindowInsets.ime)
    )
    {
        Row(
            modifier = Modifier
                .height(80.dp)
                .fillMaxWidth(1f)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(top = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = "back button",
                    modifier = Modifier
                        .padding(start = 5.dp)
                        .size(30.dp)
                        .clickable(onClick = goBack),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (pic?.length != 0) {
                    AsyncImage(
                        model = pic,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .clip(CircleShape)
                            .size(50.dp)
                            .graphicsLayer {
                                scaleX = 1.5f
                                scaleY = 1.5f
                            },
                    )
                } else {
                    Icon(
                        Icons.Default.AccountCircle,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .clip(CircleShape)
                            .size(55.dp)
                    )
                }

                Text(
                    text = name,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(start = 10.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

            }
            Row(modifier = Modifier.padding(end = 12.dp)) {
                Icon(
                    Icons.Outlined.Call,
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable(onClick = {})
                )
                Spacer(modifier = Modifier.width(15.dp))
                Icon(
                    Icons.Outlined.MoreVert,
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable(onClick = { expanded = !expanded })
                )
                PopUpMenu(
                    expanded = expanded, { expanded = !expanded },
                    modifier = Modifier,
                    dropItems = optionsList
                )
            }
        }
    }
}

@Preview
@Composable
fun PrevHeader() {
    HeaderBar(
        name = "User",
        pic = ""
    ) { }
}