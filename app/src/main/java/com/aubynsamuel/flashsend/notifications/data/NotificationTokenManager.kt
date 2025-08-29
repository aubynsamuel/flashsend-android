package com.aubynsamuel.flashsend.notifications.data

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import com.google.firebase.firestore.FirebaseFirestore

class NotificationTokenManager {
    companion object {
        private const val PREFS_NAME = "notification_prefs"
        private const val TOKEN_KEY = "deviceToken"
        private const val USER_ID_KEY = "userIdKey"
        private const val TAG = "NotificationTokenManager"
    }

    /**
     * Initializes and updates the token if it has changed or userId has changed.
     */
    fun initializeAndUpdateToken(context: Context, newUserId: String, newToken: String) {
        val cachedToken = getStoredToken(context)
        val cachedUserId = getStoredUserId(context)
        if (cachedToken == newToken && cachedUserId == newUserId) {
            Log.d(TAG, "Token/UserId has not changed, no updates needed.")
            return
        }
        updateUserToken(context, newUserId, newToken)
    }

    /**
     * Updates the user document in Firestore with the new FCM token and caches it locally.
     */
    private fun updateUserToken(context: Context, userId: String, token: String) {
        if (userId.isEmpty() || token.isEmpty()) return

        val firestore = FirebaseFirestore.getInstance()
        val userDocRef = firestore.collection("users").document(userId)

        userDocRef.update("deviceToken", token)
            .addOnSuccessListener {
                Log.d(TAG, "FCM token updated successfully.")
                cacheToken(context, token)
                cacheUserId(context, userId)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error updating user token", e)
            }
    }

    /**
     * Retrieves the stored token from SharedPreferences.
     */
    private fun getStoredToken(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(TOKEN_KEY, null)
    }

    /**
     * Retrieves the stored userId from SharedPreferences.
     */
    private fun getStoredUserId(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(USER_ID_KEY, null)
    }

    /**
     * Caches the token in SharedPreferences.
     */
    private fun cacheToken(context: Context, token: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putString(TOKEN_KEY, token) }
    }

    /**
     * Caches userId in SharedPreferences.
     */
    private fun cacheUserId(context: Context, userId: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putString(USER_ID_KEY, userId) }
    }
}