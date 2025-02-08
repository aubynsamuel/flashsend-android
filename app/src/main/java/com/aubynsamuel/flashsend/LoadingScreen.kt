package com.aubynsamuel.flashsend

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
    Scaffold(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background),
        content = { innerPadding ->
            Column(
                modifier = Modifier.padding(innerPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onBackground)
            }
        })
}