package com.humber.artfinder.data.network

import com.humber.artfinder.data.model.ArtworkResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface ArtApiService {
    @GET("artworks/search?query[term][is_public_domain]=true")
    suspend fun getArtworks(
        @Query("page") page: Int,
        @Query("limit") limit: Int = 20,
        @Query("fields") fields: String = "id,title,artist_display,artist_title,image_id,is_public_domain,artwork_type_title,date_display,place_of_origin,medium_display,dimensions,gallery_title,department_title,short_description"
    ): ArtworkResponse

    @GET("artworks/search?query[term][is_public_domain]=true")
    suspend fun searchArtworks(
        @Query("q") query: String,
        @Query("page") page: Int,
        @Query("limit") limit: Int = 20,
        @Query("fields") fields: String = "id,title,artist_display,artist_title,image_id,is_public_domain,artwork_type_title,date_display,place_of_origin,medium_display,dimensions,gallery_title,department_title,short_description"
    ): ArtworkResponse

    companion object {
        private const val BASE_URL = "https://api.artic.edu/api/v1/"

        fun create(): ArtApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ArtApiService::class.java)
        }
    }
}
