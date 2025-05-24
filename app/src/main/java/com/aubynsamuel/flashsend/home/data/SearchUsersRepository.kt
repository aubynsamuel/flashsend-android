package com.aubynsamuel.flashsend.home.data

import com.aubynsamuel.flashsend.core.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class SearchUsersRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    fun searchUsers(query: String, currentUserId: String?): Flow<Result<List<User>>> =
        callbackFlow {
            val usersRef = firestore.collection("users")
            val listenerRegistration = usersRef
                .whereNotEqualTo("userId", currentUserId)
                .whereGreaterThanOrEqualTo("username", query)
                .whereLessThanOrEqualTo("username", query + "\uf8ff")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(Result.failure(error))
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val userData = snapshot.documents.map { doc ->
                            User(
                                userId = doc.id,
                                username = doc.getString("username") ?: "",
                                profileUrl = doc.getString("profileUrl") ?: "",
                                deviceToken = doc.getString("deviceToken") ?: "",
                            )
                        }
                        trySend(Result.success(userData))
                    }
                }
            awaitClose {
                listenerRegistration.remove()
            }
        }
}