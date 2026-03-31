package com.humber.artfinder.ui.viewmodel

import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.humber.artfinder.data.model.Artwork
import com.humber.artfinder.data.model.UserProfile
import com.humber.artfinder.data.model.VisitedArtwork
import com.humber.artfinder.data.repository.AuthRepository
import com.humber.artfinder.data.repository.UserRepository
import kotlinx.coroutines.launch

sealed class UserState {
    object Idle : UserState()
    object Loading : UserState()
    object Success : UserState()
    data class Error(val message: String) : UserState()
}

class UserViewModel(
    private val authRepo: AuthRepository,
    private val userRepo: UserRepository
) : ViewModel() {

    private val _userState = mutableStateOf<UserState>(UserState.Idle)
    val userState: State<UserState> = _userState

    private val _userProfile = mutableStateOf<UserProfile?>(null)
    val userProfile: State<UserProfile?> = _userProfile

    private val _visitedArtworks = mutableStateOf<List<VisitedArtwork>>(emptyList())
    val visitedArtworks: State<List<VisitedArtwork>> = _visitedArtworks

    val prebuiltAvatars = listOf(
        "https://api.dicebear.com/7.x/avataaars/svg?seed=Felix",
        "https://api.dicebear.com/7.x/avataaars/svg?seed=Aneka",
        "https://api.dicebear.com/7.x/avataaars/svg?seed=Jack",
        "https://api.dicebear.com/7.x/avataaars/svg?seed=James",
        "https://api.dicebear.com/7.x/avataaars/svg?seed=Milo",
        "https://api.dicebear.com/7.x/avataaars/svg?seed=Aria",
        "https://api.dicebear.com/7.x/avataaars/svg?seed=Luna",
        "https://api.dicebear.com/7.x/avataaars/svg?seed=Toby"
    )

    init {
        if (authRepo.currentUser != null) {
            fetchProfile()
            fetchVisitedArtworks()
        }
    }

    fun fetchProfile() {
        val uid = authRepo.currentUser?.uid ?: return
        _userState.value = UserState.Loading
        viewModelScope.launch {
            val result = userRepo.getUserProfile(uid)
            if (result.isSuccess) {
                val profile = result.getOrNull()
                _userProfile.value = profile
                
                // Milestone 3: Recalculate badges based on total accumulated points
                profile?.let { updateBadgesBasedOnPoints(uid, it.points) }
                
                _userState.value = UserState.Success
            } else {
                _userState.value = UserState.Error("Failed to fetch profile.")
            }
        }
    }

    private fun updateBadgesBasedOnPoints(uid: String, totalPoints: Int) {
        viewModelScope.launch {
            // Milestone 3 Official Badge Tiers:
            // 1. Explorer badge : 0-100 points
            // 2. Curator badge : 101 – 250 points
            // 3. Archivist badge : 251 – 500 points
            
            val badgesToAward = mutableListOf<String>()
            
            // Logic: Award Explorer always as the starting badge
            badgesToAward.add("Explorer")
            
            // Add higher badges if thresholds met
            if (totalPoints >= 101) {
                badgesToAward.add("Curator")
            }
            if (totalPoints >= 251) {
                badgesToAward.add("Archivist")
            }
            
            // Sync badges with Firestore (overwrite/update to avoid duplicates)
            val currentProfile = _userProfile.value ?: return@launch
            if (currentProfile.badges != badgesToAward) {
                userRepo.updateUserProfile(currentProfile.copy(badges = badgesToAward))
                // Refresh local state
                _userProfile.value = currentProfile.copy(badges = badgesToAward)
            }
        }
    }

    fun uploadPhotos(artworkId: Int, imageUris: List<Uri>) {
        val uid = authRepo.currentUser?.uid ?: return
        _userState.value = UserState.Loading
        viewModelScope.launch {
            var successCount = 0
            imageUris.forEach { uri ->
                if (userRepo.uploadArtworkPhoto(uid, artworkId, uri).isSuccess) {
                    successCount++
                }
            }

            if (successCount > 0) {
                // Re-calculate all points from scratch to ensure accuracy with professor's tiers
                calculatePointsFromScratch(uid)
                fetchVisitedArtworks()
                fetchProfile()
                _userState.value = UserState.Success
            } else {
                _userState.value = UserState.Error("Failed to upload photos.")
            }
        }
    }

    private suspend fun calculatePointsFromScratch(uid: String) {
        val visited = userRepo.getVisitedArtworks(uid).getOrNull() ?: return
        var totalPoints = 0
        visited.forEach { art ->
            val count = art.photos.size
            // Tier 1: 1-5 photos = 10 points
            // Tier 2: 6-10 photos = 20 points
            if (count >= 6) {
                totalPoints += 20
            } else if (count >= 1) {
                totalPoints += 10
            }
        }
        
        val currentProfile = userRepo.getUserProfile(uid).getOrNull()
        currentProfile?.let {
            userRepo.updateUserProfile(it.copy(points = totalPoints))
        }
    }

    fun uploadPhoto(artworkId: Int, imageUri: Uri) {
        uploadPhotos(artworkId, listOf(imageUri))
    }

    fun fetchVisitedArtworks() {
        val uid = authRepo.currentUser?.uid ?: return
        viewModelScope.launch {
            val result = userRepo.getVisitedArtworks(uid)
            if (result.isSuccess) {
                _visitedArtworks.value = result.getOrDefault(emptyList())
            }
        }
    }

    fun toggleVisited(artwork: Artwork) {
        val uid = authRepo.currentUser?.uid ?: return
        val existing = _visitedArtworks.value.find { it.id == artwork.id }
        viewModelScope.launch {
            if (existing != null) {
                userRepo.removeVisitedArtwork(uid, artwork.id)
            } else {
                val visited = VisitedArtwork(
                    id = artwork.id, title = artwork.title, artistDisplay = artwork.artistDisplay,
                    imageId = artwork.imageId, latitude = artwork.latitude, longitude = artwork.longitude,
                    visitedAt = Timestamp.now(), artworkType = artwork.artworkType, mediumDisplay = artwork.mediumDisplay
                )
                userRepo.addVisitedArtwork(uid, visited)
            }
            calculatePointsFromScratch(uid) // Re-calculate in case an artwork with photos was toggled
            fetchVisitedArtworks()
            fetchProfile()
        }
    }

    fun removeVisitedById(artworkId: Int) {
        val uid = authRepo.currentUser?.uid ?: return
        viewModelScope.launch {
            userRepo.removeVisitedArtwork(uid, artworkId)
            calculatePointsFromScratch(uid)
            fetchVisitedArtworks()
            fetchProfile()
        }
    }

    fun isVisited(artworkId: Int): Boolean = _visitedArtworks.value.any { it.id == artworkId }
    fun getVisitedArtworkById(id: Int): VisitedArtwork? = _visitedArtworks.value.find { it.id == id }
    fun clearProfile() {
        _userProfile.value = null
        _visitedArtworks.value = emptyList()
        _userState.value = UserState.Idle
    }
    fun updateProfile(displayName: String, email: String, avatarUrl: String) {
        val currentProfile = _userProfile.value ?: return
        _userState.value = UserState.Loading
        viewModelScope.launch {
            if (email != currentProfile.email) authRepo.updateEmail(email)
            val updatedProfile = currentProfile.copy(displayName = displayName, email = email, profileImageUrl = avatarUrl)
            if (userRepo.updateUserProfile(updatedProfile).isSuccess) {
                _userProfile.value = updatedProfile
                _userState.value = UserState.Success
            }
        }
    }
    fun createInitialProfile(uid: String, email: String, displayName: String) {
        _userState.value = UserState.Loading
        viewModelScope.launch {
            val profile = UserProfile(uid = uid, email = email, displayName = displayName, profileImageUrl = prebuiltAvatars.first())
            val result = userRepo.createUserProfile(profile)
            if (result.isSuccess) {
                _userProfile.value = profile
                _userState.value = UserState.Success
            }
        }
    }
}

class UserVMFactory(private val authRepo: AuthRepository, private val userRepo: UserRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return UserViewModel(authRepo, userRepo) as T
    }
}
