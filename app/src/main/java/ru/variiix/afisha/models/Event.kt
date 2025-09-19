package ru.variiix.afisha.models

import com.google.gson.annotations.SerializedName

data class Event(
    val id: String,
    val title: String,
    @SerializedName("image_url") val imageUrl: String?,
    val rating: Float?,
    val price: String?,
    val details: String,
    @SerializedName("is_favorite") val isFavorite: Boolean,
    @SerializedName("is_ticket") val isTicket: Boolean
)
