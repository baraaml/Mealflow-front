package com.example.mealflow.core.domain.model

data class User(
    val id: String,
    val username: String,
    val email: String,
    val name: String? = null,
    val profilePicture: String? = null,
    val isVerified: Boolean = false
)
