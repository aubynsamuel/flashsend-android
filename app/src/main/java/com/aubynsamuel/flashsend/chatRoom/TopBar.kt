package com.aubynsamuel.flashsend.chatRoom

import android.net.Uri
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.aubynsamuel.flashsend.functions.User
import com.google.gson.Gson

@Composable
fun HeaderBar(
    name: String, netActivity: String, pic: String?, goBack: () -> Unit, userData: User,
    navController: NavController, chatOptionsList: List<DropMenu>
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Row(
            modifier = Modifier
                .height(80.dp)
                .fillMaxWidth(1f)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(top = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
//            Back button, profile pic and name/network status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable(onClick = {
                    val userJson = Uri.encode(Gson().toJson(userData))
                    navController.navigate("otherProfileScreen/$userJson")
                })
            ) {
                Icon(
                    Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = "back button",
                    modifier = Modifier
                        .padding(start = 5.dp)
                        .size(30.dp)
                        .clickable(onClick = goBack),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
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

                Column {
                    Text(
                        text = name,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(start = 10.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    if (netActivity.isNotEmpty()) {
                        Text(
                            text = netActivity,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(start = 15.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

            }
//          Call and more vert icon buttons
            Row(modifier = Modifier.padding(end = 12.dp)) {
                Icon(
                    Icons.Outlined.Call,
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.clickable(onClick = {})
                )
                Spacer(modifier = Modifier.width(15.dp))
                Icon(
                    Icons.Outlined.MoreVert,
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.clickable(onClick = { expanded = !expanded })
                )

                PopUpMenu(
                    expanded = expanded, { expanded = !expanded },
                    modifier = Modifier,
                    dropItems = chatOptionsList
                )
            }
        }
    }
}

//@Preview
//@Composable
//fun PrevHeader() {
//    HeaderBar(
//        name = "User",
//        pic = "",
//        netActivity = "",
//        goBack = {},
//        userData =
//    )
//}