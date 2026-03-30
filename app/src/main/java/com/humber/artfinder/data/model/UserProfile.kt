package com.humber.artfinder.data.model

data class UserProfile(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val profileImageUrl: String? = null,
    val points: Int = 0, // Added for Milestone 3
    val badges: List<String> = emptyList() // Added for Milestone 3
)
