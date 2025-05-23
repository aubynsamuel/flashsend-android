package com.aubynsamuel.flashsend.chatRoom.data.repository

import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.File
import javax.inject.Inject

class FirebaseStorageRepository @Inject constructor(
    private val firebaseStorage: FirebaseStorage,
) {
    private val tag = "MessageRepository"
    suspend fun uploadImage(imageUri: Uri, username: String): String? {
        return try {
            val storageRef =
                firebaseStorage.reference.child("chatMedia/${username}_${System.currentTimeMillis()}.jpg")
            storageRef.putFile(imageUri).await()
            storageRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            Log.e(tag, "Error uploading image", e)
            null
        }
    }

    suspend fun uploadAudio(file: File?): String? {
        return try {
            if (file == null) return null

            val storageRef = firebaseStorage.reference.child("chatAudio/${file.name}")
            storageRef.putFile(Uri.fromFile(file)).await()
            storageRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            Log.e(tag, "Error uploading audio", e)
            null
        }
    }
}