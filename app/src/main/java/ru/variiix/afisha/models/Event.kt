package ru.variiix.afisha.models

data class Event(
    val id: Int,

    val title: String,
    val imageUrl: String,
    val type: String,
    val price: String,

    val isFavorite: Boolean,
    val isTicketBought: Boolean
)
