package com.aubynsamuel.flashsend.home.domain.usecase


import com.aubynsamuel.flashsend.core.model.User
import com.aubynsamuel.flashsend.home.data.SearchUsersRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchUsersUseCase @Inject constructor(
    private val userRepository: SearchUsersRepository,
) {
    operator fun invoke(query: String, currentUserId: String?): Flow<Result<List<User>>> {
        return userRepository.searchUsers(query, currentUserId)
    }
}