package com.example.mealflow.core.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: String,
    val username: String? = "",
    val name: String? = "",
    val profilePicture: String? = "",
    val email: String,
    val isVerified: Boolean
)
