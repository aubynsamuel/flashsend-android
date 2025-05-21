package com.aubynsamuel.flashsend.auth.data

import com.aubynsamuel.flashsend.core.model.NewUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthUserRepository @Inject constructor(
    private val firebase: FirebaseFirestore,
) {
    suspend fun getUserProfile(userId: String): Result<NewUser?> {
        return try {
            val document = firebase.collection("users").document(userId).get().await()
            val userDataMap = document.data
            if (userDataMap != null) {
                val user = NewUser(
                    userId = userId,
                    username = userDataMap["username"] as? String ?: "",
                    profileUrl = userDataMap["profileUrl"] as? String ?: "",
                    deviceToken = userDataMap["deviceToken"] as? String ?: "",
                    email = userDataMap["email"] as? String ?: ""
                )
                Result.success(user)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}