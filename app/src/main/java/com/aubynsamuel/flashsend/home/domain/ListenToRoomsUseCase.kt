package com.aubynsamuel.flashsend.home.domain

import com.aubynsamuel.flashsend.core.model.RoomData
import com.aubynsamuel.flashsend.home.data.RoomRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ListenToRoomsUseCase @Inject constructor(
    private val roomRepository: RoomRepository,
) {
    operator fun invoke(userId: String): Flow<Result<List<RoomData>>> {
        return roomRepository.listenToRooms(userId)
    }
}