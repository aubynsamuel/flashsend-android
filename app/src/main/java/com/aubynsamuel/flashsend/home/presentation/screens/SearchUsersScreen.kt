package com.aubynsamuel.flashsend.home.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.aubynsamuel.flashsend.core.domain.logger
import com.aubynsamuel.flashsend.core.model.User
import com.aubynsamuel.flashsend.home.presentation.components.SearchedUserItem
import com.aubynsamuel.flashsend.navigation.Screen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.net.URLEncoder

@Composable
fun SearchUsersScreen(
    navController: NavController,
) {
    val auth = FirebaseAuth.getInstance()
    val tag = "SearchUsersScreen"
    val currentUsername by remember { mutableStateOf(auth.currentUser?.uid) }

    logger(tag, currentUsername.toString())

    var searchText by remember { mutableStateOf(("")) }
    var filteredUsers by remember { mutableStateOf<List<User>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("Search users") }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    val handleSearch = { text: String ->
        isLoading = true
        searchText = text

        if (text.trim().isEmpty()) {
            isLoading = false
            filteredUsers = emptyList()
            errorMessage = "Search Users"
        }

        val usersRef = Firebase.firestore.collection("users")
        usersRef.whereNotEqualTo("userId", currentUsername)
            .whereGreaterThanOrEqualTo("username", text)
            .whereLessThanOrEqualTo("username", text + "\uf8ff").get()
            .addOnSuccessListener { querySnapshot ->
                val userData = querySnapshot.documents.map { doc ->
                    User(
                        userId = doc.id,
                        username = doc.getString("username") ?: "",
                        profileUrl = doc.getString("profileUrl") ?: "",
                        deviceToken = doc.getString("deviceToken") ?: "",
                    )
                }
                filteredUsers = userData
                isLoading = false
                if (userData.isEmpty()) {
                    errorMessage = "No users found"
                }
            }.addOnFailureListener { error ->
                isLoading = false
                errorMessage = "An error occurred\nPlease check your internet connection"
                println("Error searching users: $error")
            }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            BasicTextField(value = searchText,
                singleLine = true,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
                textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground),
                onValueChange = { handleSearch(it) },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp)
                    .padding(end = 15.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                    .focusRequester(focusRequester),
                decorationBox = { innerTextField ->
                    Box(
                        contentAlignment = Alignment.CenterStart,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .padding(horizontal = 10.dp)
                    ) {
                        if (searchText.isEmpty()) {
                            Text(
                                text = "Search...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        innerTextField()
                    }
                })
        }

        // User List or Loading Indicator
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onBackground)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                if (filteredUsers.isNotEmpty()) {
                    items(filteredUsers) { user ->
                        SearchedUserItem(user = user, onClick = {
                            val encodedUsername = URLEncoder.encode(user.username, "UTF-8")
                            val encodedProfileUrl = URLEncoder.encode(user.profileUrl, "UTF-8")
                            navController.navigate(
                                Screen.ChatRoom.createRoute(
                                    username = encodedUsername,
                                    userId = user.userId,
                                    deviceToken = user.deviceToken,
                                    profileUrl = encodedProfileUrl,
                                )
                            )
                        })
                    }
                } else {
                    item {
                        Box(
                            modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = errorMessage, color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }
        }
    }
}