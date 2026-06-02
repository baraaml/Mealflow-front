package com.example.mealflow.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class SearchInfo(
    @SerialName("searchType")
    val search_type: String? = null,
    @SerialName("ingredientMode")
    val ingredient_mode: String? = null,
    @SerialName("filtersApplied")
    val filters_applied: FiltersApplied? = null
)

@Serializable
data class FiltersApplied(
    val creator: Boolean = false,
    val community: Boolean = false,
    val region: Boolean = false,
    @SerialName("subRegion")
    val sub_region: Boolean = false,
    val dietary: Boolean = false,
    val calories: Boolean = false,
    val time: Boolean = false
)

@Serializable
data class RegionsResponse(
//    val success: Boolean = true,
    @SerialName("items")
    val regions: List<String> = emptyList(),
    val pagination: MealPagination? = null
)

@Serializable
data class SubRegionsResponse(
//    val success: Boolean = true,
    @SerialName("items")
    val subRegions: List<String> = emptyList(),
    val pagination: MealPagination? = null
)

@Serializable
data class DietaryTagsResponse(
//    val success: Boolean = true,
    @SerialName("items")
    val dietaryTags: List<String?> = emptyList(),
    val pagination: MealPagination? = null
)

@Serializable
data class IngredientsResponse(
//    val success: Boolean = true,
    @SerialName("items")
    val ingredients: List<SearchIngredient> = emptyList(),
    val pagination: MealPagination? = null
)

@Serializable
data class ContinentsResponse(
//    val success: Boolean = true,
    @SerialName("items")
    val continents: List<String> = emptyList(),
    val pagination: MealPagination? = null
)

@Serializable
data class SearchIngredient(
    val id: String,
    val name: String,
    @SerialName("genericName")
    val generic_name: String,
    @SerialName("flavorCategory")
    val flavor_category: String? = null,
    @SerialName("dietCategory")
    val diet_category: String? = null,
    @SerialName("rawIngredient")
    val raw_ingredient: String? = null,
    val frequency: Int? = null,
    @SerialName("wikiLink")
    val wiki_link: String? = null,
    @SerialName("wikiImage")
    val wiki_image: String? = null,
    val nutrition: Map<String, JsonElement> = emptyMap()
)

@Serializable
data class Region(
    val name: String
)

@Serializable
data class SubRegion(
    val name: String
)

@Serializable
data class DietaryTag(
    val name: String
)

@Serializable
data class Continent(
    val name: String
)