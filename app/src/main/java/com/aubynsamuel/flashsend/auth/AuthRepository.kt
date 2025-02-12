package com.aubynsamuel.flashsend.auth

import com.aubynsamuel.flashsend.functions.NewUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository(private val auth: FirebaseAuth) {
    val firebase = FirebaseFirestore.getInstance()

    suspend fun signUp(email: String, password: String): Result<String> {
        return try {
            auth.createUserWithEmailAndPassword(email, password).await()
            createUserDatabase(email)
            Result.success("Sign-up successful! Please complete your profile.")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createUserDatabase(email: String) {
        try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            firebase.collection("users")
                .document(userId)
                .set(
                    NewUser(
                        userId = userId,
                        username = "",
                        profileUrl = "",
                        deviceToken = "",
                        email = email
                    )
                )
                .await()
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun updateUserDocument(newData: Map<String, Any>): Result<String> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            firebase.collection("users")
                .document(userId)
                .update(newData)
                .await()
            Result.success("Profile updated successfully!")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun login(email: String, password: String): Result<String> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Result.success("Login successful!")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resetPassword(email: String): Result<String> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success("Reset link has been sent to your email")
        } catch (e: Exception) {
            var msg = e.message ?: "An error occurred"
            if (msg.contains("auth/invalid-email")) msg = "Invalid Email"
            else if (msg.contains("auth/invalid-credential")) msg = "Invalid Credentials"
            else if (msg.contains("auth/network-request-failed")) msg = "No internet connection"
            Result.failure(Exception(msg))
        }
    }

//    fun signInWithGoogle(): Result<String> {
//        return try {
//            Result.success("Google sign in successful!")
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun logout() {
        auth.signOut()
    }
}
