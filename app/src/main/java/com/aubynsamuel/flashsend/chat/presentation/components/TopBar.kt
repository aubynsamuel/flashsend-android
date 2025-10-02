package com.aubynsamuel.flashsend.chat.presentation.components

import androidx.compose.animation.ExperimentalSharedTransitionApi
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
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.aubynsamuel.flashsend.R
import com.aubynsamuel.flashsend.core.domain.model.DropMenu
import com.aubynsamuel.flashsend.core.domain.model.User
import com.aubynsamuel.flashsend.core.presentation.components.PopUpMenu
import com.aubynsamuel.flashsend.core.presentation.navigation.OtherProfileScreenDC
import com.aubynsamuel.flashsend.navigation.LocalChatRoomAnimatedVisibilityScope
import com.aubynsamuel.flashsend.navigation.LocalSharedTransitionScope
import com.google.gson.Gson

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun HeaderBar(
    name: String,
    netActivity: String,
    pic: String?,
    goBack: () -> Unit,
    userData: User,
    navController: NavController,
    chatOptionsList: List<DropMenu>,
    onImageClick: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val sharedTransitionScope = LocalSharedTransitionScope.current

    Row(
        modifier = Modifier
            .height(80.dp)
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(top = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
//            Back button, profile pic and name/network status
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            with(sharedTransitionScope) {
                Icon(
                    Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = "back button",
                    modifier = Modifier
                        .padding(start = 10.dp)
                        .size(25.dp)
                        .clickable(onClick = goBack)
                        .sharedBounds(
                            sharedContentState = rememberSharedContentState(key = "icon/otherUserProfileToChatRoom"),
                            animatedVisibilityScope = LocalChatRoomAnimatedVisibilityScope.current
                        ),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.width(5.dp))
//                profile pic and name/network status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable(onClick = {
                        val userJson = Gson().toJson(userData)
                        navController.navigate(OtherProfileScreenDC(userJson))
                    })
                    .weight(1f)
            ) {
                with(sharedTransitionScope) {
                    AsyncImage(
                        model = pic,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .sharedBounds(
                                sharedContentState =
                                    rememberSharedContentState(key = "image/$pic"),
                                animatedVisibilityScope = LocalChatRoomAnimatedVisibilityScope.current
                            )
                            .clip(CircleShape)
                            .size(45.dp),
                        contentScale = ContentScale.Crop,
                        error = rememberAsyncImagePainter(R.drawable.person)
                    )
                }

                Column {
                    with(sharedTransitionScope) {
                        Text(
                            text = name,
                            fontSize = 18.sp,
                            modifier = Modifier
                                .padding(start = 10.dp)
                                .sharedBounds(
                                    sharedContentState = rememberSharedContentState(key = "text/${userData.username}"),
                                    animatedVisibilityScope = LocalChatRoomAnimatedVisibilityScope.current
                                ),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    if (netActivity.isNotEmpty()) {
                        Text(
                            text = netActivity,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(start = 11.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

        }

//          Call and more vert icon buttons
        Row(modifier = Modifier.padding(end = 12.dp)) {
            Icon(
                Icons.Outlined.CameraAlt,
                contentDescription = "",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.clickable(onClick = { onImageClick() })
            )
            Spacer(modifier = Modifier.width(15.dp))
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
                dropItems = chatOptionsList,
                reactions = {}
            )
        }
    }
}

