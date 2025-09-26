package ru.variiix.afisha.network

import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query
import ru.variiix.afisha.models.EventsResponse

interface EventsApi {
    @GET("events")
    suspend fun getEvents(
        @Query("rubric") rubric: String?,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int,
        @Query("id") id: String? = null,
        @Header("Authorization") token: String? = null
    ): EventsResponse
}
