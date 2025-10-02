package com.aubynsamuel.flashsend.auth.domain

import com.aubynsamuel.flashsend.auth.data.AuthRepository
import com.aubynsamuel.flashsend.core.domain.model.NewUser
import javax.inject.Inject

class SignUpUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(email: String, password: String): Result<String> {
        return try {
            val authResult = authRepository.createAuthUser(email, password)
            authResult.onSuccess { userId ->
                val newUser = NewUser(
                    userId = userId,
                    username = "",
                    profileUrl = "",
                    deviceToken = "",
                    email = email
                )
                authRepository.saveUserProfile(newUser)
            }
            authResult.map { "Sign-up successful! Please complete your profile." }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
