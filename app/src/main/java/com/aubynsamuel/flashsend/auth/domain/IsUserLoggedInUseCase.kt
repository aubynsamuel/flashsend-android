package com.aubynsamuel.flashsend.auth.domain

import com.aubynsamuel.flashsend.auth.data.AuthRepository
import javax.inject.Inject

class IsUserLoggedInUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    operator fun invoke(): Boolean {
        return authRepository.isUserLoggedIn()
    }
}