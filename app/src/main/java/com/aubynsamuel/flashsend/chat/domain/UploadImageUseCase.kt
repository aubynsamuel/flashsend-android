package com.aubynsamuel.flashsend.chat.domain

import android.net.Uri
import com.aubynsamuel.flashsend.chat.data.repository.FirebaseStorageRepository
import javax.inject.Inject


class UploadImageUseCase @Inject constructor(
    private val firebaseStorageRepository: FirebaseStorageRepository,
) {
    suspend operator fun invoke(imageUri: Uri, username: String): String? {
        return firebaseStorageRepository.uploadImage(imageUri, username)
    }
}
