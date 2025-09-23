package ru.variiix.afisha.models

data class User(
    val id: String,
    val nickname: String,
    val email: String,
    val avatarUrl: String? = null
)