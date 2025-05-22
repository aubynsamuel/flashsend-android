package com.aubynsamuel.flashsend.auth.data

import android.content.Context
import android.credentials.CreateCredentialException
import android.credentials.GetCredentialException
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.credentials.CreatePasswordRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPasswordOption
import androidx.credentials.PasswordCredential
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
class AppCredentialsManager(private val context: Context) {
    private val credentialManager = CredentialManager.create(context)
    private val tag = "AppCredentialsManager"

    suspend fun registerPassword(username: String, password: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val createPasswordRequest = CreatePasswordRequest(
                    id = username,
                    password = password
                )

                val result = credentialManager.createCredential(
                    context = context,
                    request = createPasswordRequest
                )

                Log.d(tag, "Password credential successfully registered: $result")
                true
            } catch (_: CreateCredentialException) {
                false
            }
        }

    suspend fun getCredential(): Pair<String, String>? = withContext(Dispatchers.IO) {
        try {
            val getPasswordOption = GetPasswordOption()
            val getCredRequest = GetCredentialRequest(listOf(getPasswordOption))

            val result = credentialManager.getCredential(
                context = context,
                request = getCredRequest
            )

            if (result.credential is PasswordCredential) {
                val credential = result.credential as PasswordCredential
                Log.d(tag, "Successfully retrieved password credential for ID: ${credential.id}")
                Pair(credential.id, credential.password)
            } else {
                Log.d(tag, "Retrieved credential is not a password credential")
                null
            }
        } catch (e: GetCredentialException) {
            Log.d(tag, "No credentials available: ${e.message}")
            null
        }
    }
}