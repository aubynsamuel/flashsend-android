package com.aubynsamuel.flashsend.auth.domain

import com.aubynsamuel.flashsend.auth.data.AuthRepository
import javax.inject.Inject

class UpdateUserDocumentUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(newData: Map<String, Any>): Result<String> {
        return authRepository.updateUserDocument(newData)
    }
}
