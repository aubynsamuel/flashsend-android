package com.aubynsamuel.flashsend

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.aubynsamuel.flashsend.auth.AuthViewModel

@Composable
fun LoadingScreen(navController: NavHostController, authViewModel: AuthViewModel) {
    val authState by authViewModel.authState.collectAsState()
    LaunchedEffect(authState) {
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CircularProgressIndicator(
            modifier = Modifier.align(Alignment.Center),
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}