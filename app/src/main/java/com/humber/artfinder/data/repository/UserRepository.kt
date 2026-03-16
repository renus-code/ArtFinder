package com.humber.artfinder.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.humber.artfinder.data.model.UserProfile
import com.humber.artfinder.data.model.VisitedArtwork
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val usersCollection = firestore.collection("users")

    suspend fun createUserProfile(profile: UserProfile): Result<Unit> {
        return try {
            usersCollection.document(profile.uid).set(profile).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserProfile(uid: String): Result<UserProfile?> {
        return try {
            val snapshot = usersCollection.document(uid).get().await()
            val profile = snapshot.toObject(UserProfile::class.java)
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserProfile(profile: UserProfile): Result<Unit> {
        return try {
            usersCollection.document(profile.uid).set(profile).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Visited Artworks Methods ---

    suspend fun addVisitedArtwork(uid: String, artwork: VisitedArtwork): Result<Unit> {
        return try {
            usersCollection.document(uid)
                .collection("visited_artworks")
                .document(artwork.id.toString())
                .set(artwork)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getVisitedArtworks(uid: String): Result<List<VisitedArtwork>> {
        return try {
            val snapshot = usersCollection.document(uid)
                .collection("visited_artworks")
                .orderBy("visitedAt", Query.Direction.DESCENDING)
                .get()
                .await()
            val artworks = snapshot.toObjects(VisitedArtwork::class.java)
            Result.success(artworks)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeVisitedArtwork(uid: String, artworkId: Int): Result<Unit> {
        return try {
            usersCollection.document(uid)
                .collection("visited_artworks")
                .document(artworkId.toString())
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
