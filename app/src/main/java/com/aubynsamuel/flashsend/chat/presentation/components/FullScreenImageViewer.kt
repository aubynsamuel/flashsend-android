package com.aubynsamuel.flashsend.chat.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import coil.compose.AsyncImage
import com.aubynsamuel.flashsend.navigation.LocalSharedTransitionScope
import com.aubynsamuel.flashsend.ui.theme.LocalAppTheme
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun FullScreenImageViewer(
    imageUri: String,
    onDismiss: () -> Unit,
    animatedScope: AnimatedVisibilityScope,
) {
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var isUIVisible by remember { mutableStateOf(true) }
    val dragThreshold = 200f
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val computedAlpha by animateFloatAsState(
        targetValue = (1f - (abs(dragOffset) / dragThreshold)).coerceIn(0f, 1f)
    )
    val view = LocalView.current
    val window = (view.context as? androidx.activity.ComponentActivity)?.window
    val darkTheme = LocalAppTheme.current

    LaunchedEffect(isUIVisible) {
        if (window != null) {
            val controller = WindowCompat.getInsetsController(window, view)
            if (isUIVisible) {
                controller.show(WindowInsetsCompat.Type.systemBars())
                controller.isAppearanceLightStatusBars = !darkTheme
            } else {
                controller.hide(WindowInsetsCompat.Type.systemBars())
                WindowCompat.setDecorFitsSystemWindows(window, false)
            }
        }
    }

    DisposableEffect(Unit) {
        if (window != null) {
            val controller = WindowCompat.getInsetsController(window, view)
            controller.hide(WindowInsetsCompat.Type.systemBars())
            WindowCompat.setDecorFitsSystemWindows(window, false)
            onDispose {
                controller.show(WindowInsetsCompat.Type.systemBars())
            }
        } else {
            onDispose { }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colorScheme.background
                    .copy(alpha = computedAlpha)
            )
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onVerticalDrag = { _: PointerInputChange, dragAmount: Float ->
                        dragOffset += dragAmount
                    },
                    onDragEnd = {
                        if (abs(dragOffset) > dragThreshold)
                            onDismiss()
                        else
                            dragOffset = 0f
                    }
                )
            }
            .clickable(
                onClick = { isUIVisible = !isUIVisible },
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
            )
    ) {
        with(sharedTransitionScope) {
            AsyncImage(
                model = imageUri,
                contentDescription = "Expanded Image",
                modifier = Modifier
                    .offset { IntOffset(x = 0, y = dragOffset.roundToInt()) }
                    .sharedBounds(
                        sharedContentState = rememberSharedContentState(key = "image/$imageUri"),
                        animatedVisibilityScope = animatedScope
                    )
                    .fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        // Header bar with back button (animated visibility)
        AnimatedVisibility(
            visible = isUIVisible,
            enter = fadeIn(animationSpec = tween(500)),
            exit = fadeOut(animationSpec = tween(500)),
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(48.dp)
                        .alpha(computedAlpha)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}