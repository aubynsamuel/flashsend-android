package com.aubynsamuel.flashsend.home.domain.usecase

import com.aubynsamuel.flashsend.home.data.HomeRepository
import javax.inject.Inject

class GetFCMTokenUseCase @Inject constructor(
    private val homeRepository: HomeRepository,
) {
    operator fun invoke(
        callBack: (token: String) -> Unit,
    ) {
        homeRepository.getFCMToken(callBack)
    }
}