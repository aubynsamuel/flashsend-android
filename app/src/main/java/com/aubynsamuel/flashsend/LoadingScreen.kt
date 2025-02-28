package com.aubynsamuel.flashsend

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.airbnb.lottie.compose.*
import com.aubynsamuel.flashsend.auth.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun LoadingScreen(navController: NavHostController, authViewModel: AuthViewModel) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.loading_animation))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        speed = 2.0f
    )

    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        delay(500)
        if (authState) {
            navController.navigate("main") {
                popUpTo("loadingScreen") { inclusive = true }
            }
        } else {
            navController.navigate("auth") {
                popUpTo("loadingScreen") { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.size(200.dp)
            )

            // App Logo/Title
            Text(
                text = "Flash Send",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontSize = 32.sp
            )

            // Progress text
            Text(
                text = "Getting things ready...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            AnimatedDots()
        }
    }
}

@Composable
fun AnimatedDots() {
    val dotCount = 3
    val transition = rememberInfiniteTransition()

    val delays = listOf(0, 200, 400)
    val scales = delays.map { delay ->
        transition.animateFloat(
            initialValue = 1f,
            targetValue = 1.5f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 1200
                    1f at delay
                    1.5f at delay + 300
                    1f at delay + 600
                }
            )
        )
    }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(dotCount) { index ->
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.extraLarge
                    )
                    .scale(scales[index].value)
            )
        }
    }
}