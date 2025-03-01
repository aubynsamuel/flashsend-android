package com.aubynsamuel.flashsend.notifications

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

object NotificationTokenManager {
    private const val PREFS_NAME = "notification_prefs"
    private const val TOKEN_KEY = "deviceToken"
    private const val TAG = "NotificationTokenManager"

    /**
     * Initializes and updates the token if it has changed.
     */
    fun initializeAndUpdateToken(context: Context, userId: String, newToken: String) {
        val cachedToken = getStoredToken(context)
        if (cachedToken == newToken) {
            Log.d(TAG, "Token has not changed, no updates needed.")
            return
        }
        updateUserToken(context, userId, newToken)
    }

    /**
     * Updates the user document in Firestore with the new FCM token and caches it locally.
     */
    fun updateUserToken(context: Context, userId: String, token: String) {
        if (userId.isEmpty() || token.isEmpty()) return

        val firestore = FirebaseFirestore.getInstance()
        val userDocRef = firestore.collection("users").document(userId)

        userDocRef.update("deviceToken", token)
            .addOnSuccessListener {
                Log.d(TAG, "FCM token updated successfully.")
                cacheToken(context, token)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error updating user token", e)
            }
    }

    /**
     * Retrieves the stored token from SharedPreferences.
     */
    fun getStoredToken(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(TOKEN_KEY, null)
    }

    /**
     * Caches the token in SharedPreferences.
     */
    fun cacheToken(context: Context, token: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(TOKEN_KEY, token).apply()
    }
}