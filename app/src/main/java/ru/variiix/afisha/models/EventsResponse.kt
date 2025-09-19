package ru.variiix.afisha.models

data class EventsResponse(
    val events: List<Event>,
    val total: Int,
    val offset: Int,
    val limit: Int
)