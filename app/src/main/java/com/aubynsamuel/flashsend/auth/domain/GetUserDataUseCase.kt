package com.aubynsamuel.flashsend.auth.domain

import android.content.Context
import com.aubynsamuel.flashsend.auth.data.AuthUserRepository
import com.aubynsamuel.flashsend.core.domain.MediaCacheManager
import com.aubynsamuel.flashsend.core.model.NewUser
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class GetUserDataUseCase @Inject constructor(
    private val authUserRepository: AuthUserRepository,
    @ApplicationContext private val appContext: Context,
) {
    suspend operator fun invoke(userId: String?): Result<NewUser?> {
        if (userId == null) {
            return Result.success(null)
        }
        return authUserRepository.getUserProfile(userId).mapCatching { user ->
            user?.let {
                val cachedUri = MediaCacheManager.getMediaUri(appContext, it.profileUrl)
                it.copy(profileUrl = cachedUri.toString())
            }
        }
    }
}