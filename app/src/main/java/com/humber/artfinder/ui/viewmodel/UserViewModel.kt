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
                
                // Recalculate badge based on points whenever profile is fetched
                profile?.let { updateBadgesBasedOnPoints(uid, it.points) }
                
                _userState.value = UserState.Success
            } else {
                _userState.value = UserState.Error("Failed to fetch profile.")
            }
        }
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
                
                // Milestone 3: Points for visiting are handled by professor's photo logic instead
                // but we keep some base points if desired. Requirement focuses on photos.
            }
            fetchVisitedArtworks()
        }
    }

    fun uploadPhoto(artworkId: Int, imageUri: Uri) {
        val uid = authRepo.currentUser?.uid ?: return
        _userState.value = UserState.Loading
        viewModelScope.launch {
            val uploadResult = userRepo.uploadArtworkPhoto(uid, artworkId, imageUri)
            if (uploadResult.isSuccess) {
                val updatedVisited = userRepo.getVisitedArtworks(uid).getOrNull()
                val artwork = updatedVisited?.find { it.id == artworkId }
                
                artwork?.let {
                    val photoCount = it.photos.size
                    // Point Logic: 1-5 photos -> 10 pts, 6-10 photos -> 20 pts total
                    if (photoCount == 1) {
                        userRepo.addPoints(uid, 10)
                    } else if (photoCount == 6) {
                        userRepo.addPoints(uid, 10) // Total becomes 20
                    }
                }
                
                fetchProfile()
                fetchVisitedArtworks()
                _userState.value = UserState.Success
            } else {
                _userState.value = UserState.Error("Upload failed.")
            }
        }
    }

    private fun updateBadgesBasedOnPoints(uid: String, totalPoints: Int) {
        viewModelScope.launch {
            // Logic for badges
            if (totalPoints >= 251) {
                userRepo.addBadge(uid, "Archivist")
            } else if (totalPoints >= 101) {
                userRepo.addBadge(uid, "Curator")
            } else {
                userRepo.addBadge(uid, "Explorer")
            }
        }
    }

    fun removeVisitedById(artworkId: Int) {
        val uid = authRepo.currentUser?.uid ?: return
        viewModelScope.launch {
            userRepo.removeVisitedArtwork(uid, artworkId)
            fetchVisitedArtworks()
        }
    }

    fun isVisited(artworkId: Int): Boolean = _visitedArtworks.value.any { it.id == artworkId }

    fun getVisitedArtworkById(id: Int): VisitedArtwork? = _visitedArtworks.value.find { it.id == id }

    fun clearProfile() {
        _userProfile.value = null
        _visitedArtworks.value = emptyList()
        _userState.value = UserState.Idle
    }

    // Pass through methods for profile editing
    fun updateProfile(displayName: String, email: String, avatarUrl: String) {
        val currentProfile = _userProfile.value ?: return
        _userState.value = UserState.Loading
        viewModelScope.launch {
            if (email != currentProfile.email) {
                authRepo.updateEmail(email)
            }
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
