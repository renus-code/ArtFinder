package com.humber.artfinder.data.repository

import com.humber.artfinder.data.model.Artwork
import com.humber.artfinder.data.network.ArtApiService

class ArtRepository(private val apiService: ArtApiService = ArtApiService.create()) {

    suspend fun getArtworks(page: Int): Result<List<Artwork>> {
        return try {
            val response = apiService.getArtworks(page = page)
            Result.success(response.data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchArtworks(query: String, page: Int): Result<List<Artwork>> {
        return try {
            val response = apiService.searchArtworks(query = query, page = page)
            Result.success(response.data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
