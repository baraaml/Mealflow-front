package com.example.mealflow.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MealResponse(
    val success: Boolean = true,
    val count: Int = 0,
    @SerialName("meals")
    val data: List<Meal> = emptyList(),
    val pagination: MealPagination? = null,
    @SerialName("search_info")
    val searchInfo: SearchInfo? = null,
)

@Serializable
data class MealPagination(
    val total: Int = 0,
    @SerialName("totalPages")
    val total_pages: Int = 0,
    @SerialName("pageSize")
    val page_size: Int = 0,
    val offset: Int = 0,
    @SerialName("nextOffset")
    val next_offset: Int? = null // Optional: if backend provides next offset directly
)



@Serializable
data class Meal(
    @SerialName("id")
    val mealId: String = "",

    @SerialName("title")
    val name: String,

    @SerialName("description")
    val description: String? = null, // Nullable if not always present

    @SerialName("imageUrl")
    val imageUrl: String? = null,

    @SerialName("region")
    val region: String? = null,

    @SerialName("subRegion")
    val subRegion: String? = null,

    @SerialName("continent")
    val continent: String? = null,

    @SerialName("tags")
    val tags: List<String> = emptyList(),

    @SerialName("dietaryTags")
    val dietaryTags: List<String?> = emptyList(),

    @SerialName("preparationTime")
    val preparationTime: Int = 0,

    @SerialName("cookingTime")
    val cookingTime: Int = 0,

    @SerialName("totalTime")
    val totalTime: Int = 0,

    @SerialName("servings")
    val servings: Int = 0,

    @SerialName("calories")
    val caloriesPerServing: Double = 0.0,

    @SerialName("createdBy")
    val createdBy: User = User(),

    @SerialName("isLiked")
    val isLiked: Boolean = false,

    @SerialName("isDisliked")
    val isDisliked: Boolean = false,

    @SerialName("isFavorited")
    val isFavorited: Boolean = false,

    @SerialName("isSaved")
    val isSaved: Boolean = false,

    @SerialName("notes")
    val notes: List<Note> = emptyList(),

    @SerialName("interactions")
    val interactions: Interactions = Interactions(),

    @SerialName("createdAt")
    val createdAt: String = "",

    @SerialName("updatedAt")
    val updatedAt: String = "",

    val isPlanned: Boolean = false,
    val isCooked: Boolean = false,

    @SerialName("ingredients")
    val ingredients: List<MealIngredient> = emptyList(),

    @SerialName("instructions")
    val instructions: List<String> = emptyList(),

    val cookware: List<String> = emptyList(),

    @SerialName("rating")
    val rating: Double = 0.0,

    @SerialName("reviewsCount")
    val reviewsCount: Int = 0,

    @SerialName("commID")
    val communityId: Int? = null,

    @SerialName("matchingIngredients")
    val matchingIngredients: Int = 0,

    // Flags for data quality and loading state
    @SerialName("_isPartialData")
    val isPartialData: Boolean = false,

    @SerialName("_loadingError")
    val hasLoadingError: Boolean = false
)

@Serializable
data class MealIngredient(
    val id: String? = null,
    val size: String? = null,
    val unit: String? = null,
    val state: String? = null,
    val phrase: String? = null,
    val quantity: Double? = null
)

// User serialization
@Serializable
data class User(
    @SerialName("user_id")
    val userId: String = "",

    @SerialName("username")
    val username: String = "",

    @SerialName("profile_picture")
    val profilePicture: String? = null
)

// Note serialization
@Serializable
data class Note(
    @SerialName("note_id")
    val noteId: String = "",

    @SerialName("user")
    val user: User = User(),

    @SerialName("comment")
    val comment: String = "",

    @SerialName("image_url")
    val imageUrl: String? = "https://mealflow.ddns.net/static/default_profile_picture.png",

    @SerialName("tags")
    val tags: List<String> = emptyList(),

    @SerialName("did_cook")
    val didCook: Boolean = false,

    @SerialName("likes")
    val likes: Int = 0,

    @SerialName("dislikes")
    val dislikes: Int = 0
)

// Interactions serialization
@Serializable
data class Interactions(
    @SerialName("views")
    val views: Int = 0,

    @SerialName("likes")
    val likes: Int = 0,

    @SerialName("dislikes")
    val dislikes: Int = 0,

    @SerialName("shares")
    val shares: Int = 0
)

// MealType enum used by both Meal and PlannedMeal
enum class MealType(val id: Int, val title: String) {
    BREAKFAST(0, "Breakfast"),
    LUNCH(1, "Lunch"),
    DINNER(2, "Dinner"),
    SNACK(3, "Snack"),
    OTHER(4, "Other")
}