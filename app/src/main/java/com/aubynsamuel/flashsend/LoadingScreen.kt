package com.aubynsamuel.flashsend

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.aubynsamuel.flashsend.auth.AuthViewModel

@Composable
fun LoadingScreen(navController: NavHostController, authViewModel: AuthViewModel) {
    // Observe the auth state
    val authState by authViewModel.authState.collectAsState()
    LaunchedEffect(authState) {
//        delay(2000)
        if (authState) {
            navController.navigate("home") {
                popUpTo("loadingScreen") { inclusive = true }
            }
        } else {
            navController.navigate("auth") {
                popUpTo("loadingScreen") { inclusive = true }
            }
        }
    }

    // Create an infinite transition for our animations.
    val infiniteTransition = rememberInfiniteTransition(label = "")

    // Animate a full rotation from 0f to 360f.
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = ""
    )

    // Animate the alpha for a pulsating effect.
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    // The loading screen UI
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // The rotating circle drawn on a Canvas.
        Canvas(
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.Center)
                .rotate(rotation)
        ) {
            drawCircle(
                color = Color.Black,
                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        // Pulsating "Loading..." text below the circle.
        Text(
            text = "Loading...",
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 150.dp)
                .alpha(alphaAnim),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
