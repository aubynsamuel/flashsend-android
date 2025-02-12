package com.aubynsamuel.flashsend.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.aubynsamuel.flashsend.Screen
import com.aubynsamuel.flashsend.functions.User
import com.aubynsamuel.flashsend.functions.logger
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.net.URLEncoder

@Composable
fun SearchUsersScreen(
    navController: NavController,
) {
    val auth = FirebaseAuth.getInstance()

    val currentUsername by remember { mutableStateOf(auth.currentUser?.uid) }


    logger("homePack", currentUsername.toString())

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
                        UserItem(user = user, onClick = {
                            val encodedUsername = URLEncoder.encode(user.username, "UTF-8")
                            val encodedProfileUrl = URLEncoder.encode(user.profileUrl, "UTF-8")
                            navController.navigate(
                                Screen.ChatRoom.createRoute(
                                    username = encodedUsername,
                                    userId = user.userId,
                                    deviceToken = user.deviceToken,
                                    profileUrl = encodedProfileUrl,
                                    roomId = ""
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

@Composable
fun UserItem(
    user: User, onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 15.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (user.profileUrl.isNotEmpty()) {
            AsyncImage(
                model = user.profileUrl,
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .clip(CircleShape)
                    .size(50.dp)
                    .graphicsLayer {
                        scaleX = 1.5f
                        scaleY = 1.5f
                    },
            )
        } else {
            Icon(
                Icons.Default.AccountCircle,
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .clip(CircleShape)
                    .size(52.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.width(15.dp))
        Text(
            text = user.username,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Preview
@Composable
fun Screen() {
    SearchUsersScreen(navController = rememberNavController())
}