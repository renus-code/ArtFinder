package com.humber.artfinder.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.humber.artfinder.data.model.UserProfile
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

    // Prebuilt avatars using DiceBear API
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
        // Automatically fetch profile if user is logged in
        fetchProfile()
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

    fun updateProfile(displayName: String, email: String, avatarUrl: String) {
        val currentProfile = _userProfile.value ?: return
        val uid = authRepo.currentUser?.uid ?: return

        _userState.value = UserState.Loading
        viewModelScope.launch {
            // 1. Update email in Firebase Auth if it changed
            if (email != currentProfile.email) {
                val emailResult = authRepo.updateEmail(email)
                if (emailResult.isFailure) {
                    _userState.value = UserState.Error("Failed to update email in Auth. Re-login may be required.")
                    return@launch
                }
            }

            // 2. Update profile in Firestore
            val updatedProfile = currentProfile.copy(
                displayName = displayName,
                email = email,
                profileImageUrl = avatarUrl
            )
            val result = userRepo.updateUserProfile(updatedProfile)

            if (result.isSuccess) {
                _userProfile.value = updatedProfile
                _userState.value = UserState.Success
            } else {
                _userState.value = UserState.Error("Failed to update profile data.")
            }
        }
    }

    fun clearProfile() {
        _userProfile.value = null
        _userState.value = UserState.Idle
    }
}

class UserVMFactory(
    private val authRepo: AuthRepository,
    private val userRepo: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserViewModel(authRepo, userRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
