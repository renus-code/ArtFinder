package com.humber.artfinder.ui.viewmodel

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
                _userProfile.value = result.getOrNull()
                _userState.value = UserState.Success
            } else {
                _userState.value = UserState.Error("Failed to fetch profile.")
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
            fetchVisitedArtworks()
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
}

class UserVMFactory(private val authRepo: AuthRepository, private val userRepo: UserRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return UserViewModel(authRepo, userRepo) as T
    }
}
