package com.aubynsamuel.flashsend.auth

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AuthScreen(navController: NavController, authViewModel: AuthViewModel) {
    var isLogin by remember { mutableStateOf(true) }
    val title = if (isLogin) "Login" else "Sign Up"
    val authState by authViewModel.authState.collectAsState()
    val message by authViewModel.message.collectAsState()

    LaunchedEffect(authState) {
        if (authState) {
            if (isLogin) {
                navController.navigate("home") {
                    popUpTo("auth") { inclusive = true }
                }
            } else {
                navController.navigate("setUserDetails") {
                    popUpTo("auth") { inclusive = true }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                title,
                fontSize = 35.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(start = 5.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            AuthForm(
                isLogin,
                { isLogin = !isLogin },
                authViewModel
            )

            message?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
        }
    }
}


@Composable
fun AuthForm(
    isLogin: Boolean,
    onToggleMode: () -> Unit,
    authViewModel: AuthViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val isLoggingIn by authViewModel.isLoggingIn.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
                capitalization = KeyboardCapitalization.Sentences
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = if (isLogin) ImeAction.Done else ImeAction.Next,
                capitalization = KeyboardCapitalization.Sentences

            ),
            trailingIcon = {
                Text(text = if (passwordVisible) "Hide" else "Show",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { passwordVisible = !passwordVisible })
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        AnimatedVisibility(visible = !isLogin) {
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                    capitalization = KeyboardCapitalization.Sentences

                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            )
        }
        Text(
            text = "Forgot Password?",
            modifier = Modifier
                .align(Alignment.End)
                .clickable(onClick = {
                    if (email.isNotBlank()) {
                        authViewModel.resetPassword(email)
                        Toast.makeText(
                            context, "Reset link has been sent to your email", Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            context, "Please enter your email address", Toast.LENGTH_LONG
                        ).show()
                    }
                }),
            color = MaterialTheme.colorScheme.onBackground

        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if (email.isNotBlank() && password.isNotBlank()) {
                    if (isLogin) {
                        authViewModel.login(email, password)
                    } else if (password == confirmPassword) {
                        authViewModel.signUp(email, password)
                    }
                }
            }, modifier = Modifier
                .fillMaxWidth()
                .height(50.dp), shape = RoundedCornerShape(20.dp)
        ) {
            if (isLoggingIn) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(if (isLogin) "Login" else "Sign Up", fontSize = 25.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(text = if (isLogin) "Don't have an account? Sign Up" else "Already have an account? Login",
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable { onToggleMode() })
    }
}

@Preview
@Composable
fun PrevAuthForm() {
    AuthForm(
        isLogin = false, onToggleMode = {},
        authViewModel = viewModel {
            AuthViewModel(
                repository = AuthRepository(FirebaseAuth.getInstance())
            )
        },
    )
}