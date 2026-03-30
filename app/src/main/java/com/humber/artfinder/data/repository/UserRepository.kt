package com.humber.artfinder.data.repository

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.humber.artfinder.data.model.UserProfile
import com.humber.artfinder.data.model.VisitedArtwork
import kotlinx.coroutines.tasks.await
import java.util.*

class UserRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
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

    // --- Milestone 3: Photo Upload & Point Rewards ---

    suspend fun uploadArtworkPhoto(uid: String, artworkId: Int, imageUri: Uri): Result<String> {
        return try {
            val fileName = UUID.randomUUID().toString()
            val storageRef = storage.reference.child("users/$uid/artworks/$artworkId/$fileName")
            
            storageRef.putFile(imageUri).await()
            val downloadUrl = storageRef.downloadUrl.await().toString()
            
            // Update the visited artwork with the new photo link
            val artworkRef = usersCollection.document(uid)
                .collection("visited_artworks")
                .document(artworkId.toString())
            
            val snapshot = artworkRef.get().await()
            val currentPhotos = snapshot.toObject(VisitedArtwork::class.java)?.photos ?: emptyList()
            
            artworkRef.update("photos", currentPhotos + downloadUrl).await()
            
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addPoints(uid: String, pointsToAdd: Int): Result<Int> {
        return try {
            val userRef = usersCollection.document(uid)
            val snapshot = userRef.get().await()
            val currentPoints = snapshot.getLong("points")?.toInt() ?: 0
            val newPoints = currentPoints + pointsToAdd
            
            userRef.update("points", newPoints).await()
            Result.success(newPoints)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addBadge(uid: String, badgeName: String): Result<Unit> {
        return try {
            val userRef = usersCollection.document(uid)
            val snapshot = userRef.get().await()
            val currentBadges = snapshot.toObject(UserProfile::class.java)?.badges ?: emptyList()
            
            if (!currentBadges.contains(badgeName)) {
                userRef.update("badges", currentBadges + badgeName).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
