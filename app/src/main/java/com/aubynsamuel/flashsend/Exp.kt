package com.aubynsamuel.flashsend

import android.annotation.SuppressLint
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun Exp(navController: NavController) {
    var expanded by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .clickable { expanded = !expanded }
    ) {
        val transition = updateTransition(targetState = expanded, label = "expandTransition")
        val parentHeight = maxHeight

        // Animation values
        val imageSize by transition.animateDp(
            transitionSpec = { tween(300) },
            label = "imageSize"
        ) { if (it) 300.dp else 100.dp }

        val verticalOffset by transition.animateDp(
            transitionSpec = { tween(300) },
            label = "verticalOffset"
        ) { if (it) 0.dp else (parentHeight / 2 - imageSize / 2) }

        val contentAlpha by transition.animateFloat(
            transitionSpec = { tween(300) },
            label = "contentAlpha"
        ) { if (it) 1f else 0f }

        // Shared image element
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "Expandable Image",
            modifier = Modifier
                .size(imageSize)
                .offset(y = verticalOffset)
                .align(Alignment.TopCenter)
        )

        // Expanded content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(contentAlpha)
        ) {
            Column(
                modifier = Modifier
                    .padding(top = imageSize + 32.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    "Expanded View",
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Text(
                    "Additional content here...",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}