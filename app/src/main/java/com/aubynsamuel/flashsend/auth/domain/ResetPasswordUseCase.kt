package com.aubynsamuel.flashsend.auth.domain

import com.aubynsamuel.flashsend.auth.data.AuthRepository
import javax.inject.Inject

class ResetPasswordUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(email: String): Result<String> {
        return authRepository.resetPassword(email)
    }
}