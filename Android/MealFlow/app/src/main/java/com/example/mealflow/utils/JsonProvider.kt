package com.example.mealflow.utils

import kotlinx.serialization.json.Json

object JsonProvider {
    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }
}