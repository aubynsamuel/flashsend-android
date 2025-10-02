package com.aubynsamuel.flashsend.chat.presentation.screens

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.aubynsamuel.flashsend.R
import com.aubynsamuel.flashsend.core.domain.model.User
import com.aubynsamuel.flashsend.core.presentation.navigation.FullScreenImageViewerDC
import com.aubynsamuel.flashsend.home.presentation.components.ProfileDetailItem
import com.aubynsamuel.flashsend.navigation.LocalSharedTransitionScope
import com.aubynsamuel.flashsend.navigation.safePopBackStack
import kotlinx.coroutines.launch
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun OtherUserProfileScreen(
    navController: NavController,
    userData: User,
    animatedScope: AnimatedContentScope,
) {
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    val maxHeight = 220.dp
    val minHeight = 120.dp
    val maxHeightPx = with(density) { maxHeight.toPx() }
    val minHeightPx = with(density) { minHeight.toPx() }
    val headerHeight = remember { Animatable(maxHeightPx) }
    val headerHeightDp = with(density) { headerHeight.value.toDp() }

    val exitUntilCollapsedScrollBehavior = remember {
        object : NestedScrollConnection {
            private val snapThreshold = (maxHeightPx + minHeightPx) / 2
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                if (delta < 0f) { // Scroll Up (Collapse)
                    val previousHeight = headerHeight.value
                    // Calculate the new height, constrained by min/max
                    val newHeaderHeight =
                        (headerHeight.value + delta).coerceIn(minHeightPx, maxHeightPx)

                    // Consumed is the actual change in header height (must be negative)
                    val consumed = newHeaderHeight - previousHeight

                    if (abs(consumed) > 0.5f) { // Check to prevent tiny floats from consuming
                        coroutineScope.launch {
                            headerHeight.animateTo(newHeaderHeight)
                        }
                        return Offset(0f, consumed)
                    }
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                val delta = available.y
                if (delta > 0f) { // Scroll Down (Expand)
                    val previousHeight = headerHeight.value
                    // Calculate the new height, constrained by min/max
                    val newHeaderHeight =
                        (headerHeight.value + delta).coerceIn(minHeightPx, maxHeightPx)

                    // Consumed is the actual change in header height (must be positive)
                    val consumed = newHeaderHeight - previousHeight

                    if (abs(consumed) > 0.5f) {
                        coroutineScope.launch {
                            headerHeight.animateTo(newHeaderHeight)
                        }
                        return Offset(0f, consumed)
                    }
                }
                return Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                val currentHeight = headerHeight.value
                val targetHeight = if (currentHeight < snapThreshold) {
                    minHeightPx
                } else {
                    maxHeightPx
                }
                if (currentHeight != targetHeight) {
                    headerHeight.animateTo(targetHeight)
                }
                return available
            }
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(exitUntilCollapsedScrollBehavior),
        topBar = {
            TopAppBar(
                title = {
                    with(sharedTransitionScope) {
                        Text(
                            userData.username, fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .sharedBounds(
                                    sharedContentState = rememberSharedContentState(key = "text/${userData.username}"),
                                    animatedVisibilityScope = animatedScope
                                )
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors().copy(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                navigationIcon = {
                    with(sharedTransitionScope) {
                        Icon(
                            Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Back Button",
                            modifier = Modifier
                                .padding(start = 10.dp)
                                .size(25.dp)
                                .clickable(onClick = { navController.safePopBackStack() })
                                .sharedBounds(
                                    sharedContentState = rememberSharedContentState(key = "icon/otherUserProfileToChatRoom"),
                                    animatedVisibilityScope = animatedScope
                                )
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .height(headerHeightDp)
            ) {
                with(sharedTransitionScope) {
                    val imageUri = userData.profileUrl
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "Profile picture",
                        contentScale = ContentScale.Crop,
                        error = rememberAsyncImagePainter(R.drawable.person),
                        modifier = Modifier
                            .clickable(onClick = {
                                navController.navigate(
                                    FullScreenImageViewerDC(
                                        imageUri = imageUri
                                    )
                                )
                            })
                            .border(
                                2.dp,
                                MaterialTheme.colorScheme.primary,
                                CircleShape
                            )
                            .sharedBounds(
                                sharedContentState = rememberSharedContentState(key = "image/$imageUri"),
                                animatedVisibilityScope = animatedScope
                            )
                            .clip(CircleShape)
                            .size(headerHeightDp - 80.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = userData.username,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }

            // Details
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(modifier = Modifier.padding(6.dp)) {
                    ProfileDetailItem(
                        icon = Icons.Default.Person,
                        label = "Username",
                        value = userData.username
                    )
                }
            }
        }
    }
}