package ru.variiix.afisha.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val BASE_URL = "https://afisha.ddns.net/api/"

    // common retrofit
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authApi: AuthApi by lazy {
        retrofit.create(AuthApi::class.java)
    }

    val eventsApi: EventsApi by lazy {
        retrofit.create(EventsApi::class.java)
    }

    val favoritesApi: FavoritesApi by lazy {
        retrofit.create(FavoritesApi::class.java)
    }

    val ticketsApi: TicketsApi by lazy {
        retrofit.create(TicketsApi::class.java)
    }
}
