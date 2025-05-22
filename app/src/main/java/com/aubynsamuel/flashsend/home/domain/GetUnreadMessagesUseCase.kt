package com.aubynsamuel.flashsend.home.domain

import com.aubynsamuel.flashsend.home.data.HomeRepository
import javax.inject.Inject

class GetUnreadMessagesUseCase @Inject constructor(
    private val homeRepository: HomeRepository,
) {
    operator fun invoke(
        roomId: String,
        otherUserId: String,
        callBack: (value: Int) -> Unit,
    ) {
        homeRepository.getUnreadMessages(roomId, otherUserId, callBack)
    }
}