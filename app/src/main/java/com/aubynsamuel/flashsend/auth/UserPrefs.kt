package com.aubynsamuel.flashsend.auth

// UserPreferences.kt
import android.content.Context
import com.aubynsamuel.flashsend.functions.NewUser
import com.google.gson.Gson

class UserPreferences(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun getUserData(): NewUser? {
        return try {
            gson.fromJson(
                sharedPreferences.getString(USER_DATA_KEY, null),
                NewUser::class.java
            )
        } catch (e: Exception) {
            null
        }
    }

    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(IS_LOGGED_IN_KEY, false)
    }

    companion object {
        private const val USER_DATA_KEY = "user_data"
        private const val IS_LOGGED_IN_KEY = "is_logged_in"
    }
}