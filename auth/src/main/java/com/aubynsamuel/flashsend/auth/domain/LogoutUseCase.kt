package com.aubynsamuel.flashsend.auth.domain

import com.aubynsamuel.flashsend.auth.data.AuthRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    operator fun invoke() {
        authRepository.logout()
    }
}