package ru.variiix.afisha.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import ru.variiix.afisha.models.AuthResponse

interface AuthApi {
    @FormUrlEncoded
    @POST("auth/login")
    suspend fun login(
        @Field("login") login: String,  // universal: username or email
        @Field("password") password: String
    ): AuthResponse

    @Multipart
    @POST("auth/register")
    suspend fun register(
        @Part("username") username: RequestBody,
        @Part("password") password: RequestBody,
        @Part("email") email: RequestBody,
        @Part avatar: MultipartBody.Part? = null
    ): AuthResponse
}
