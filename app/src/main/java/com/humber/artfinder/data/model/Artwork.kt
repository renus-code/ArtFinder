package com.humber.artfinder.data.model

import com.google.gson.annotations.SerializedName

data class ArtworkResponse(
    val data: List<Artwork>,
    val pagination: Pagination,
    val config: Config
)

data class Artwork(
    val id: Int,
    val title: String,
    @SerializedName("artist_display")
    val artistDisplay: String?,
    @SerializedName("artist_title")
    val artistTitle: String?,
    @SerializedName("image_id")
    val imageId: String?,
    @SerializedName("is_public_domain")
    val isPublicDomain: Boolean,
    @SerializedName("artwork_type_title")
    val artworkType: String?,
    @SerializedName("place_of_origin")
    val placeOfOrigin: String?,
    @SerializedName("dimensions")
    val dimensions: String?,
    @SerializedName("medium_display")
    val mediumDisplay: String?,
    @SerializedName("gallery_title")
    val galleryTitle: String?,
    @SerializedName("is_on_view")
    val isOnView: Boolean,
    @SerializedName("date_display")
    val dateDisplay: String?,
    @SerializedName("short_description")
    val shortDescription: String?,
    val latitude: Double?,
    val longitude: Double?,
    @SerializedName("department_title")
    val departmentTitle: String?
)

data class Pagination(
    @SerializedName("total_pages")
    val totalPages: Int,
    @SerializedName("current_page")
    val currentPage: Int
)

data class Config(
    @SerializedName("iiif_url")
    val iiifUrl: String
)
