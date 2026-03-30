package com.humber.artfinder.data.model

import com.google.firebase.Timestamp

data class VisitedArtwork(
    val id: Int = 0,
    val title: String = "",
    val artistDisplay: String? = null,
    val imageId: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val visitedAt: Timestamp = Timestamp.now(),
    val artworkType: String? = null,
    val mediumDisplay: String? = null,
    val photos: List<String> = emptyList() // Added for Milestone 3
)
