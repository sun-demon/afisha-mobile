package ru.variiix.afisha.models

import com.google.gson.annotations.SerializedName

data class User(
    val id: Int,
    val username: String,
    val email: String,
    @SerializedName("avatar_url") val avatarUrl: String? = null
)