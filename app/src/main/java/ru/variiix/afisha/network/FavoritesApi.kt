package ru.variiix.afisha.network

import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Header
import retrofit2.http.Query
import ru.variiix.afisha.models.EventsResponse

interface FavoritesApi {
    @GET("favorites")
    suspend fun getFavorites(
        @Query("rubric") rubric: String?,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int,
        @Header("Authorization") token: String
    ): EventsResponse

    @POST("favorites/{eventId}")
    suspend fun addFavorite(
        @Path("eventId") eventId: String,
        @Header("Authorization") token: String
    )

    @DELETE("favorites/{eventId}")
    suspend fun removeFavorite(
        @Path("eventId") eventId: String,
        @Header("Authorization") token: String
    )
}
