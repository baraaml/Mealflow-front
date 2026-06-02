package com.example.mealflow.viewModel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.example.mealflow.network.Ingredient
import com.example.mealflow.network.createRecipeApi

class RecipeViewModel : ViewModel() {
    // UI State
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    // Recipe Data
    var title by mutableStateOf("")
    var description by mutableStateOf("")
    var region by mutableStateOf("")
    var subRegion by mutableStateOf("")
    var continent by mutableStateOf("")
    var imageUri by mutableStateOf<Uri?>(null)
    var cookTime by mutableIntStateOf(0)
    var prepTime by mutableIntStateOf(0)
    var servings by mutableIntStateOf(0)
    var calories by mutableIntStateOf(0)
    var dietaryTags by mutableStateOf<List<String>>(emptyList())
    var communityId by mutableStateOf<String?>(null)
    var ingredients by mutableStateOf<List<Ingredient>>(emptyList())
    var instructions by mutableStateOf<List<String>>(emptyList())

    // Reset all states
    fun resetState() {
        isLoading = false
        errorMessage = null
        title = ""
        description = ""
        region = ""
        subRegion = ""
        continent = ""
        imageUri = null
        cookTime = 0
        prepTime = 0
        servings = 0
        calories = 0
        dietaryTags = emptyList()
        communityId = null
        ingredients = emptyList()
        instructions = emptyList()
    }

    // Function to create a new recipe
    fun createRecipe(
        context: Context,
        navController: NavController
    ) {
        // Validate required fields
        if (title.isBlank() || description.isBlank() || ingredients.isEmpty() || instructions.isEmpty()) {
            errorMessage = "Please fill in all required fields including title, description, ingredients, and instructions"
            return
        }

        // Set loading state
        isLoading = true
        errorMessage = null

        try {
            createRecipeApi(
                context = context,
                title = title,
                region = region,
                subRegion = subRegion,
                imageUri = imageUri,
                cookTime = cookTime,
                prepTime = prepTime,
                servings = servings,
                calories = calories,
                dietaryTags = dietaryTags,
                communityId = communityId,
                ingredients = ingredients,
                instructions = instructions,
                navController = navController
            )
            // Note: The API function will handle its own loading and error states
        } catch (e: Exception) {
            // Only handle exceptions that occur before the API call
            isLoading = false
            errorMessage = "Error: ${e.message}"
        }
    }

    // Function to update the image
    fun setImage(uri: Uri?) {
        imageUri = uri
    }

    // Function to clear selected image
    fun clearImage() {
        imageUri = null
    }

    // Function to add an ingredient
    fun addIngredient(name: String, quantity: Double, unit: String) {
        if (name.isBlank() || quantity <= 0.0 || unit.isBlank()) {
            errorMessage = "Please fill in all ingredient fields"
            return
        }

        val newIngredient = Ingredient(name, quantity,unit)
        ingredients = ingredients + newIngredient
        errorMessage = null
    }

    // Function to remove an ingredient
    fun removeIngredient(index: Int) {
        if (index in ingredients.indices) {
            ingredients = ingredients.toMutableList().apply {
                removeAt(index)
            }
        }
    }

    // Function to add an instruction
    fun addInstruction(instruction: String) {
        if (instruction.isBlank()) {
            errorMessage = "Instruction cannot be empty"
            return
        }

        instructions = instructions + instruction
        errorMessage = null
    }

    // Function to remove an instruction
    fun removeInstruction(index: Int) {
        if (index in instructions.indices) {
            instructions = instructions.toMutableList().apply {
                removeAt(index)
            }
        }
    }

    // Function to update dietary tags
    fun updateDietaryTags(tags: List<String>) {
        dietaryTags = tags
    }

    // Function to toggle a dietary tag
    fun toggleDietaryTag(tag: String) {
        dietaryTags = if (dietaryTags.contains(tag)) {
            dietaryTags - tag
        } else {
            dietaryTags + tag
        }
    }

    // Function to update an ingredient
    fun updateIngredient(index: Int, name: String, quantity: Double, unit: String) {
        if (index in ingredients.indices) {
            val updatedIngredient = Ingredient(name, quantity,unit)
            ingredients = ingredients.toMutableList().apply {
                set(index, updatedIngredient)
            }
        }
    }

    // Function to update an instruction
    fun updateInstruction(index: Int, instruction: String) {
        if (index in instructions.indices && instruction.isNotBlank()) {
            instructions = instructions.toMutableList().apply {
                set(index, instruction)
            }
        }
    }
}