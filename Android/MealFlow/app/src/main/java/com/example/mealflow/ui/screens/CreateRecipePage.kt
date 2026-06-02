//package com.example.mealflow.ui.screens
//
//import android.content.Context
//import android.net.Uri
//import androidx.activity.compose.rememberLauncherForActivityResult
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.text.BasicTextField
//import androidx.compose.foundation.text.KeyboardOptions
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Close
//import androidx.compose.material.icons.filled.Tag
//import androidx.compose.material.icons.outlined.Add
//import androidx.compose.material.icons.outlined.Close
//import androidx.compose.material.icons.outlined.Image
//import androidx.compose.material3.Button
//import androidx.compose.material3.ButtonDefaults
//import androidx.compose.material3.Card
//import androidx.compose.material3.CardDefaults
//import androidx.compose.material3.Checkbox
//import androidx.compose.material3.CheckboxDefaults
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.Text
//import androidx.compose.material3.TextButton
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.SolidColor
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.text.TextStyle
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.input.KeyboardType
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.compose.ui.window.Dialog
//import androidx.navigation.NavController
//import androidx.navigation.compose.rememberNavController
//import coil.compose.rememberAsyncImagePainter
//import com.example.mealflow.network.Ingredient
//import com.example.mealflow.network.createRecipeApi
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun RecipeCreationPage(
//    navController: NavController,
//    context: Context,
//    communityId: String? = null
//) {
//    val darkBackground = Color(0xFF121212)
//    val blueText = Color(0xFF4E79E3)
//    val darkGray = Color(0xFF1E1E1E)
//    val scrollState = rememberScrollState()
//
//    var recipeTitle by remember { mutableStateOf("") }
//    var recipeDescription by remember { mutableStateOf("") }
//    var region by remember { mutableStateOf("") }
//    var subRegion by remember { mutableStateOf("") }
//    var continent by remember { mutableStateOf("") }
//    var cookTime by remember { mutableStateOf("") }
//    var prepTime by remember { mutableStateOf("") }
//    var servings by remember { mutableStateOf("") }
//    var calories by remember { mutableStateOf("") }
//
//    var showDietaryTagsDialog by remember { mutableStateOf(false) }
//    var selectedDietaryTags by remember { mutableStateOf<List<String>>(emptyList()) }
//
//    var ingredients by remember { mutableStateOf<List<Ingredient>>(listOf(Ingredient("", 0.0, ""))) }
//    var instructions by remember { mutableStateOf<List<String>>(listOf("")) }
//
//    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
//
//
//    val imagePickerLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.GetContent()
//    ) { uri ->
//        selectedImageUri = uri
//    }
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(darkBackground)
//    ) {
//        // Top bar
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(12.dp),
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            // Close button
//            IconButton(onClick = { /* Close action */ }) {
//                Icon(
//                    imageVector = Icons.Default.Close,
//                    contentDescription = "Close",
//                    tint = Color.White
//                )
//            }
//
//            // Create button
//            Button(
//                onClick = {
//                    createRecipeApi(
//                        context = context,
//                        title = recipeTitle,
//                        description = recipeDescription,
//                        region = region,
//                        subRegion = subRegion,
//                        continent = continent,
//                        imageUri = selectedImageUri,
//                        cookTime = cookTime.toIntOrNull() ?: 0,
//                        prepTime = prepTime.toIntOrNull() ?: 0,
//                        servings = servings.toIntOrNull() ?: 0,
//                        calories = calories.toIntOrNull() ?: 0,
//                        dietaryTags = selectedDietaryTags,
//                        communityId = communityId,
//                        ingredients = ingredients,
//                        instructions = instructions.filter { it.isNotBlank() },
//                        navController = navController,
//                    )
//                },
//                modifier = Modifier.padding(end = 8.dp),
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = Color.Gray.copy(alpha = 0.3f),
//                    contentColor = Color.White,
//                    disabledContainerColor = Color.Gray.copy(alpha = 0.1f),
//                    disabledContentColor = Color.Gray
//                )
//            ) {
//                Text(text = "Create Recipe")
//            }
//        }
//
//        // Recipe content area
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(start = 16.dp, end = 16.dp, top = 72.dp, bottom = 16.dp)
//                .verticalScroll(scrollState)
//        ) {
//            // Recipe Title
//            Text(
//                text = "Recipe Title",
//                color = Color.White,
//                style = TextStyle(fontWeight = FontWeight.Medium, fontSize = 18.sp)
//            )
//
//            Spacer(modifier = Modifier.height(4.dp))
//
//            // Title input field
//            BasicTextField(
//                value = recipeTitle,
//                onValueChange = { recipeTitle = it },
//                textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
//                cursorBrush = SolidColor(Color.White),
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .background(darkGray, RoundedCornerShape(8.dp))
//                    .padding(12.dp)
//            )
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            // Description
//            Text(
//                text = "Description",
//                color = Color.White,
//                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium)
//            )
//
//            Spacer(modifier = Modifier.height(4.dp))
//
//            BasicTextField(
//                value = recipeDescription,
//                onValueChange = { recipeDescription = it },
//                textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
//                cursorBrush = SolidColor(Color.White),
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(100.dp)
//                    .background(darkGray, RoundedCornerShape(8.dp))
//                    .padding(12.dp)
//            )
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            // Origin section
//            Text(
//                text = "Origin",
//                color = Color.White,
//                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium)
//            )
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            // Region
//            Text(
//                text = "Region",
//                color = Color.White,
//                style = TextStyle(fontSize = 14.sp)
//            )
//
//            Spacer(modifier = Modifier.height(4.dp))
//
//            BasicTextField(
//                value = region,
//                onValueChange = { region = it },
//                textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
//                cursorBrush = SolidColor(Color.White),
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .background(darkGray, RoundedCornerShape(8.dp))
//                    .padding(12.dp)
//            )
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            // Sub-region
//            Text(
//                text = "Sub-region",
//                color = Color.White,
//                style = TextStyle(fontSize = 14.sp)
//            )
//
//            Spacer(modifier = Modifier.height(4.dp))
//
//            BasicTextField(
//                value = subRegion,
//                onValueChange = { subRegion = it },
//                textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
//                cursorBrush = SolidColor(Color.White),
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .background(darkGray, RoundedCornerShape(8.dp))
//                    .padding(12.dp)
//            )
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            // Continent
//            Text(
//                text = "Continent",
//                color = Color.White,
//                style = TextStyle(fontSize = 14.sp)
//            )
//
//            Spacer(modifier = Modifier.height(4.dp))
//
//            BasicTextField(
//                value = continent,
//                onValueChange = { continent = it },
//                textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
//                cursorBrush = SolidColor(Color.White),
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .background(darkGray, RoundedCornerShape(8.dp))
//                    .padding(12.dp)
//            )
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            // Recipe details section
//            Text(
//                text = "Recipe Details",
//                color = Color.White,
//                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium)
//            )
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            // Recipe details in a grid
//            Row(modifier = Modifier.fillMaxWidth()) {
//                // Cook time
//                Column(modifier = Modifier.weight(1f)) {
//                    Text(
//                        text = "Cook Time (min)",
//                        color = Color.White,
//                        style = TextStyle(fontSize = 14.sp)
//                    )
//
//                    Spacer(modifier = Modifier.height(4.dp))
//
//                    BasicTextField(
//                        value = cookTime,
//                        onValueChange = { cookTime = it },
//                        textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
//                        cursorBrush = SolidColor(Color.White),
//                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .background(darkGray, RoundedCornerShape(8.dp))
//                            .padding(12.dp)
//                    )
//                }
//
//                Spacer(modifier = Modifier.width(8.dp))
//
//                // Prep time
//                Column(modifier = Modifier.weight(1f)) {
//                    Text(
//                        text = "Prep Time (min)",
//                        color = Color.White,
//                        style = TextStyle(fontSize = 14.sp)
//                    )
//
//                    Spacer(modifier = Modifier.height(4.dp))
//
//                    BasicTextField(
//                        value = prepTime,
//                        onValueChange = { prepTime = it },
//                        textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
//                        cursorBrush = SolidColor(Color.White),
//                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .background(darkGray, RoundedCornerShape(8.dp))
//                            .padding(12.dp)
//                    )
//                }
//            }
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            Row(modifier = Modifier.fillMaxWidth()) {
//                // Servings
//                Column(modifier = Modifier.weight(1f)) {
//                    Text(
//                        text = "Servings",
//                        color = Color.White,
//                        style = TextStyle(fontSize = 14.sp)
//                    )
//
//                    Spacer(modifier = Modifier.height(4.dp))
//
//                    BasicTextField(
//                        value = servings,
//                        onValueChange = { servings = it },
//                        textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
//                        cursorBrush = SolidColor(Color.White),
//                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .background(darkGray, RoundedCornerShape(8.dp))
//                            .padding(12.dp)
//                    )
//                }
//
//                Spacer(modifier = Modifier.width(8.dp))
//
//                // Calories
//                Column(modifier = Modifier.weight(1f)) {
//                    Text(
//                        text = "Calories",
//                        color = Color.White,
//                        style = TextStyle(fontSize = 14.sp)
//                    )
//
//                    Spacer(modifier = Modifier.height(4.dp))
//
//                    BasicTextField(
//                        value = calories,
//                        onValueChange = { calories = it },
//                        textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
//                        cursorBrush = SolidColor(Color.White),
//                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .background(darkGray, RoundedCornerShape(8.dp))
//                            .padding(12.dp)
//                    )
//                }
//            }
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            // Dietary Tags
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .clip(RoundedCornerShape(8.dp))
//                    .background(darkGray)
//                    .clickable { showDietaryTagsDialog = true }
//                    .padding(12.dp),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Icon(
//                    imageVector = Icons.Default.Tag,
//                    contentDescription = "Dietary Tags",
//                    tint = Color.Gray
//                )
//
//                Spacer(modifier = Modifier.width(8.dp))
//
//                Text(
//                    text = if (selectedDietaryTags.isEmpty()) "Add dietary tags" else selectedDietaryTags.joinToString(", "),
//                    color = if (selectedDietaryTags.isEmpty()) Color.Gray else Color.White,
//                    style = TextStyle(fontSize = 14.sp)
//                )
//            }
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            // Ingredients section
//            Text(
//                text = "Ingredients",
//                color = Color.White,
//                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium)
//            )
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            ingredients.forEachIndexed { index, ingredient ->
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(vertical = 4.dp),
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    // Name - with custom placeholder implementation
//                    Box(
//                        modifier = Modifier
//                            .weight(2f)
//                            .background(darkGray, RoundedCornerShape(8.dp))
//                    ) {
//                        BasicTextField(
//                            value = ingredient.name,
//                            onValueChange = {
//                                val newIngredients = ingredients.toMutableList()
//                                newIngredients[index] = ingredient.copy(name = it)
//                                ingredients = newIngredients
//                            },
//                            textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
//                            cursorBrush = SolidColor(Color.White),
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(8.dp)
//                        )
//
//                        if (ingredient.name.isEmpty()) {
//                            Text(
//                                text = "Ingredient",
//                                color = Color.Gray,
//                                fontSize = 14.sp,
//                                modifier = Modifier.padding(8.dp)
//                            )
//                        }
//                    }
//
//                    Spacer(modifier = Modifier.width(4.dp))
//
//                    // Quantity - with optimized update logic for Double type
//                    Box(
//                        modifier = Modifier
//                            .weight(1f)
//                            .background(darkGray, RoundedCornerShape(8.dp))
//                    ) {
//                        // Convert Double to String for display in TextField
//                        var quantityText by remember(ingredient.quantity) {
//                            mutableStateOf(if (ingredient.quantity == 0.0) "" else ingredient.quantity.toString())
//                        }
//
//                        BasicTextField(
//                            value = quantityText,
//                            onValueChange = { newText ->
//                                quantityText = newText
//                                // Try to parse to Double or default to 0.0
//                                val quantityValue = newText.toDoubleOrNull() ?: 0.0
//                                // Only update the main list when text changes
//                                val newIngredients = ingredients.toMutableList()
//                                newIngredients[index] = ingredient.copy(quantity = quantityValue)
//                                ingredients = newIngredients
//                            },
//                            textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
//                            cursorBrush = SolidColor(Color.White),
//                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(8.dp)
//                        )
//
//                        if (quantityText.isEmpty()) {
//                            Text(
//                                text = "Qty",
//                                color = Color.Gray,
//                                fontSize = 14.sp,
//                                modifier = Modifier.padding(8.dp)
//                            )
//                        }
//                    }
//
//                    Spacer(modifier = Modifier.width(4.dp))
//
//                    // Unit - with custom placeholder implementation
//                    Box(
//                        modifier = Modifier
//                            .weight(1f)
//                            .background(darkGray, RoundedCornerShape(8.dp))
//                    ) {
//                        BasicTextField(
//                            value = ingredient.unit,
//                            onValueChange = {
//                                val newIngredients = ingredients.toMutableList()
//                                newIngredients[index] = ingredient.copy(unit = it)
//                                ingredients = newIngredients
//                            },
//                            textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
//                            cursorBrush = SolidColor(Color.White),
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(8.dp)
//                        )
//
//                        if (ingredient.unit.isEmpty()) {
//                            Text(
//                                text = "Unit",
//                                color = Color.Gray,
//                                fontSize = 14.sp,
//                                modifier = Modifier.padding(8.dp)
//                            )
//                        }
//                    }
//
//                    Spacer(modifier = Modifier.width(4.dp))
//
//                    // Delete button (except for first ingredient)
//                    if (ingredients.size > 1) {
//                        IconButton(
//                            onClick = {
//                                ingredients = ingredients.filterIndexed { i, _ -> i != index }
//                            },
//                            modifier = Modifier.size(35.dp)
//                        ) {
//                            Icon(
//                                imageVector = Icons.Outlined.Close,
//                                contentDescription = "Remove ingredient",
//                                tint = Color.Gray,
//                                modifier = Modifier.size(20.dp)
//                            )
//                        }
//                    } else {
//                        Spacer(modifier = Modifier.size(35.dp))
//                    }
//                }
//            }
//
//            // Add ingredient button
//            TextButton(
//                onClick = {
//                    ingredients = ingredients + Ingredient("", 0.0, "")
//                },
//                modifier = Modifier.align(Alignment.End)
//            ) {
//                Icon(
//                    imageVector = Icons.Outlined.Add,
//                    contentDescription = "Add ingredient",
//                    tint = blueText
//                )
//                Spacer(modifier = Modifier.width(4.dp))
//                Text(
//                    text = "Add Ingredient",
//                    color = blueText
//                )
//            }
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            // Instructions section
//            Text(
//                text = "Instructions",
//                color = Color.White,
//                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium)
//            )
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            instructions.forEachIndexed { index, instruction ->
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(vertical = 4.dp),
//                    verticalAlignment = Alignment.Top
//                ) {
//                    // Step number
//                    Text(
//                        text = "${index + 1}.",
//                        color = Color.White,
//                        modifier = Modifier.padding(top = 8.dp, end = 8.dp),
//                        style = TextStyle(fontSize = 14.sp)
//                    )
//
//                    // Instruction text - with custom placeholder implementation
//                    Box(
//                        modifier = Modifier
//                            .weight(1f)
//                            .background(darkGray, RoundedCornerShape(8.dp))
//                    ) {
//                        BasicTextField(
//                            value = instruction,
//                            onValueChange = {
//                                val newInstructions = instructions.toMutableList()
//                                newInstructions[index] = it
//                                instructions = newInstructions
//                            },
//                            textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
//                            cursorBrush = SolidColor(Color.White),
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(8.dp)
//                        )
//
//                        if (instruction.isEmpty()) {
//                            Text(
//                                text = "Step instruction",
//                                color = Color.Gray,
//                                fontSize = 14.sp,
//                                modifier = Modifier.padding(8.dp)
//                            )
//                        }
//                    }
//
//                    Spacer(modifier = Modifier.width(4.dp))
//
//                    // Delete button (except for first instruction)
//                    if (instructions.size > 1) {
//                        IconButton(
//                            onClick = {
//                                instructions = instructions.filterIndexed { i, _ -> i != index }
//                            },
//                            modifier = Modifier.size(35.dp)
//                        ) {
//                            Icon(
//                                imageVector = Icons.Outlined.Close,
//                                contentDescription = "Remove step",
//                                tint = Color.Gray,
//                                modifier = Modifier.size(20.dp)
//                            )
//                        }
//                    } else {
//                        Spacer(modifier = Modifier.size(35.dp))
//                    }
//                }
//            }
//
//            // Add instruction button
//            TextButton(
//                onClick = {
//                    instructions = instructions + ""
//                },
//                modifier = Modifier.align(Alignment.End)
//            ) {
//                Icon(
//                    imageVector = Icons.Outlined.Add,
//                    contentDescription = "Add step",
//                    tint = blueText
//                )
//                Spacer(modifier = Modifier.width(4.dp))
//                Text(
//                    text = "Add Step",
//                    color = blueText
//                )
//            }
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            // Image section
//            Text(
//                text = "Recipe Image",
//                color = Color.White,
//                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium)
//            )
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            selectedImageUri?.let { uri ->
//                Image(
//                    painter = rememberAsyncImagePainter(uri),
//                    contentDescription = null,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(200.dp)
//                        .clip(RoundedCornerShape(8.dp)),
//                    contentScale = ContentScale.Crop
//                )
//            } ?: Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(150.dp)
//                    .clip(RoundedCornerShape(8.dp))
//                    .background(darkGray)
//                    .clickable { imagePickerLauncher.launch("image/*") }
//                    .padding(16.dp),
//                contentAlignment = Alignment.Center
//            ) {
//                Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                    Icon(
//                        imageVector = Icons.Outlined.Image,
//                        contentDescription = "Add Image",
//                        tint = Color.Gray,
//                        modifier = Modifier.size(48.dp)
//                    )
//
//                    Spacer(modifier = Modifier.height(8.dp))
//
//                    Text(
//                        text = "Add Recipe Image",
//                        color = Color.Gray
//                    )
//                }
//            }
//        }
//
//        // Dietary Tags Dialog
//        if (showDietaryTagsDialog) {
//            Dialog(onDismissRequest = { showDietaryTagsDialog = false }) {
//                Card(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(16.dp),
//                    colors = CardDefaults.cardColors(containerColor = darkBackground)
//                ) {
//                    Column(
//                        modifier = Modifier.padding(16.dp)
//                    ) {
//                        Text(
//                            text = "Select Dietary Tags",
//                            color = Color.White,
//                            style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp)
//                        )
//
//                        Spacer(modifier = Modifier.height(16.dp))
//
//                        val availableTags = listOf(
//                            "vegetarian", "vegan", "gluten-free", "dairy-free",
//                            "low-carb", "keto", "high-protein", "low-fat",
//                            "low-sodium", "paleo", "whole30", "sugar-free"
//                        )
//
//                        availableTags.forEach { tag ->
//                            val isSelected = selectedDietaryTags.contains(tag)
//                            Row(
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .padding(vertical = 8.dp)
//                                    .clickable {
//                                        selectedDietaryTags = if (isSelected) {
//                                            selectedDietaryTags - tag
//                                        } else {
//                                            selectedDietaryTags + tag
//                                        }
//                                    },
//                                verticalAlignment = Alignment.CenterVertically
//                            ) {
//                                Checkbox(
//                                    checked = isSelected,
//                                    onCheckedChange = null,
//                                    colors = CheckboxDefaults.colors(
//                                        checkedColor = blueText,
//                                        uncheckedColor = Color.Gray
//                                    )
//                                )
//
//                                Spacer(modifier = Modifier.width(8.dp))
//
//                                Text(
//                                    text = tag,
//                                    color = Color.White,
//                                    style = TextStyle(fontSize = 16.sp)
//                                )
//                            }
//                        }
//
//                        Spacer(modifier = Modifier.height(16.dp))
//
//                        Row(
//                            modifier = Modifier.fillMaxWidth(),
//                            horizontalArrangement = Arrangement.End
//                        ) {
//                            TextButton(onClick = { showDietaryTagsDialog = false }) {
//                                Text(
//                                    text = "Cancel",
//                                    color = Color.Gray
//                                )
//                            }
//
//                            Spacer(modifier = Modifier.width(8.dp))
//
//                            Button(
//                                onClick = { showDietaryTagsDialog = false },
//                                colors = ButtonDefaults.buttonColors(
//                                    containerColor = blueText
//                                )
//                            ) {
//                                Text("Apply")
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Preview(showBackground = true)
//@Composable
//fun RecipeCreationScreenPreview() {
//    RecipeCreationPage(
//        navController = rememberNavController(),
//        context = LocalContext.current
//    )
//}
package com.example.mealflow.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.mealflow.network.Ingredient
import com.example.mealflow.network.createRecipeApi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeCreationPage(
    navController: NavController,
    context: Context,
    communityId: String? = null
) {
    val darkBackground = Color(0xFF121212)
    val blueText = Color(0xFF4E79E3)
    val darkGray = Color(0xFF1E1E1E)
    val scrollState = rememberScrollState()

    var recipeTitle by remember { mutableStateOf("") }
    var recipeDescription by remember { mutableStateOf("") }
    var region by remember { mutableStateOf("") }
    var subRegion by remember { mutableStateOf("") }
    var continent by remember { mutableStateOf("") }
    var cookTime by remember { mutableStateOf("") }
    var prepTime by remember { mutableStateOf("") }
    var servings by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }

    var showDietaryTagsDialog by remember { mutableStateOf(false) }
    var selectedDietaryTags by remember { mutableStateOf<List<String>>(emptyList()) }

    var ingredients by remember { mutableStateOf<List<Ingredient>>(listOf(Ingredient("", 0.0,"g"))) }
    var instructions by remember { mutableStateOf<List<String>>(listOf("")) }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }


    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedImageUri = uri
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBackground)
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Close button
            IconButton(onClick = { /* Close action */ }) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White
                )
            }

            // Create button
            Button(
                onClick = {
                    createRecipeApi(
                        context = context,
                        title = recipeTitle,
                        region = region,
                        subRegion = subRegion,
                        imageUri = selectedImageUri,
                        cookTime = cookTime.toIntOrNull() ?: 0,
                        prepTime = prepTime.toIntOrNull() ?: 0,
                        servings = servings.toIntOrNull() ?: 0,
                        calories = calories.toIntOrNull() ?: 0,
                        dietaryTags = selectedDietaryTags,
                        communityId = communityId,
                        ingredients = ingredients,
                        instructions = instructions.filter { it.isNotBlank() },
                        navController = navController,
                    )
                },
                modifier = Modifier.padding(end = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Gray.copy(alpha = 0.3f),
                    contentColor = Color.White,
                    disabledContainerColor = Color.Gray.copy(alpha = 0.1f),
                    disabledContentColor = Color.Gray
                )
            ) {
                Text(text = "Create Recipe")
            }
        }

        // Recipe content area
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, top = 72.dp, bottom = 16.dp)
                .verticalScroll(scrollState)
        ) {
            // Recipe Title
            Text(
                text = "Recipe Title",
                color = Color.White,
                style = TextStyle(fontWeight = FontWeight.Medium, fontSize = 18.sp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Title input field
            BasicTextField(
                value = recipeTitle,
                onValueChange = { recipeTitle = it },
                textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                cursorBrush = SolidColor(Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(darkGray, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

//            // Description
//            Text(
//                text = "Description",
//                color = Color.White,
//                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium)
//            )
//
//            Spacer(modifier = Modifier.height(4.dp))
//
//            BasicTextField(
//                value = recipeDescription,
//                onValueChange = { recipeDescription = it },
//                textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
//                cursorBrush = SolidColor(Color.White),
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(100.dp)
//                    .background(darkGray, RoundedCornerShape(8.dp))
//                    .padding(12.dp)
//            )

            Spacer(modifier = Modifier.height(16.dp))

            // Origin section
            Text(
                text = "Origin",
                color = Color.White,
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Region
            Text(
                text = "Region",
                color = Color.White,
                style = TextStyle(fontSize = 14.sp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            BasicTextField(
                value = region,
                onValueChange = { region = it },
                textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                cursorBrush = SolidColor(Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(darkGray, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Sub-region
            Text(
                text = "Sub-region",
                color = Color.White,
                style = TextStyle(fontSize = 14.sp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            BasicTextField(
                value = subRegion,
                onValueChange = { subRegion = it },
                textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                cursorBrush = SolidColor(Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(darkGray, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

//            // Continent
//            Text(
//                text = "Continent",
//                color = Color.White,
//                style = TextStyle(fontSize = 14.sp)
//            )
//
//            Spacer(modifier = Modifier.height(4.dp))
//
//            BasicTextField(
//                value = continent,
//                onValueChange = { continent = it },
//                textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
//                cursorBrush = SolidColor(Color.White),
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .background(darkGray, RoundedCornerShape(8.dp))
//                    .padding(12.dp)
//            )

            Spacer(modifier = Modifier.height(16.dp))

            // Recipe details section
            Text(
                text = "Recipe Details",
                color = Color.White,
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Recipe details in a grid
            Row(modifier = Modifier.fillMaxWidth()) {
                // Cook time
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Cook Time (min)",
                        color = Color.White,
                        style = TextStyle(fontSize = 14.sp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    BasicTextField(
                        value = cookTime,
                        onValueChange = { cookTime = it },
                        textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                        cursorBrush = SolidColor(Color.White),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(darkGray, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Prep time
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Prep Time (min)",
                        color = Color.White,
                        style = TextStyle(fontSize = 14.sp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    BasicTextField(
                        value = prepTime,
                        onValueChange = { prepTime = it },
                        textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                        cursorBrush = SolidColor(Color.White),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(darkGray, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                // Servings
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Servings",
                        color = Color.White,
                        style = TextStyle(fontSize = 14.sp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    BasicTextField(
                        value = servings,
                        onValueChange = { servings = it },
                        textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                        cursorBrush = SolidColor(Color.White),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(darkGray, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Calories
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Calories",
                        color = Color.White,
                        style = TextStyle(fontSize = 14.sp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    BasicTextField(
                        value = calories,
                        onValueChange = { calories = it },
                        textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                        cursorBrush = SolidColor(Color.White),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(darkGray, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dietary Tags
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(darkGray)
                    .clickable { showDietaryTagsDialog = true }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Tag,
                    contentDescription = "Dietary Tags",
                    tint = Color.Gray
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = if (selectedDietaryTags.isEmpty()) "Add dietary tags" else selectedDietaryTags.joinToString(", "),
                    color = if (selectedDietaryTags.isEmpty()) Color.Gray else Color.White,
                    style = TextStyle(fontSize = 14.sp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Ingredients section
            Text(
                text = "Ingredients",
                color = Color.White,
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium)
            )

            Spacer(modifier = Modifier.height(8.dp))

            ingredients.forEachIndexed { index, ingredient ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Name - with custom placeholder implementation
                    Box(
                        modifier = Modifier
                            .weight(2f)
                            .background(darkGray, RoundedCornerShape(8.dp))
                    ) {
                        BasicTextField(
                            value = ingredient.name,
                            onValueChange = {
                                val newIngredients = ingredients.toMutableList()
                                newIngredients[index] = ingredient.copy(name = it)
                                ingredients = newIngredients
                            },
                            textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                            cursorBrush = SolidColor(Color.White),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        )

                        if (ingredient.name.isEmpty()) {
                            Text(
                                text = "Ingredient",
                                color = Color.Gray,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Quantity - with optimized update logic for Double type
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(darkGray, RoundedCornerShape(8.dp))
                    ) {
                        // Convert Double to String for display in TextField
                        var quantityText by remember(ingredient.quantity) {
                            mutableStateOf(if (ingredient.quantity == 0.0) "" else ingredient.quantity.toString())
                        }

                        BasicTextField(
                            value = quantityText,
                            onValueChange = { newText ->
                                quantityText = newText
                                // Try to parse to Double or default to 0.0
                                val quantityValue = newText.toDoubleOrNull() ?: 0.0
                                // Only update the main list when text changes
                                val newIngredients = ingredients.toMutableList()
                                newIngredients[index] = ingredient.copy(quantity = quantityValue)
                                ingredients = newIngredients
                            },
                            textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                            cursorBrush = SolidColor(Color.White),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        )

                        if (quantityText.isEmpty()) {
                            Text(
                                text = "Qty",
                                color = Color.Gray,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

//                    // Unit - with custom placeholder implementation
//                    Box(
//                        modifier = Modifier
//                            .weight(1f)
//                            .background(darkGray, RoundedCornerShape(8.dp))
//                    ) {
//                        BasicTextField(
//                            value = ingredient.unit,
//                            onValueChange = {
//                                val newIngredients = ingredients.toMutableList()
//                                newIngredients[index] = ingredient.copy(unit = it)
//                                ingredients = newIngredients
//                            },
//                            textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
//                            cursorBrush = SolidColor(Color.White),
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(8.dp)
//                        )
//
//                        if (ingredient.unit.isEmpty()) {
//                            Text(
//                                text = "Unit",
//                                color = Color.Gray,
//                                fontSize = 14.sp,
//                                modifier = Modifier.padding(8.dp)
//                            )
//                        }
//                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Delete button (except for first ingredient)
                    if (ingredients.size > 1) {
                        IconButton(
                            onClick = {
                                ingredients = ingredients.filterIndexed { i, _ -> i != index }
                            },
                            modifier = Modifier.size(35.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Remove ingredient",
                                tint = Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.size(35.dp))
                    }
                }
            }

            // Add ingredient button
            TextButton(
                onClick = {
                    ingredients = ingredients + Ingredient("", 0.0,"h")
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "Add ingredient",
                    tint = blueText
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Add Ingredient",
                    color = blueText
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Instructions section
            Text(
                text = "Instructions",
                color = Color.White,
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium)
            )

            Spacer(modifier = Modifier.height(8.dp))

            instructions.forEachIndexed { index, instruction ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Step number
                    Text(
                        text = "${index + 1}.",
                        color = Color.White,
                        modifier = Modifier.padding(top = 8.dp, end = 8.dp),
                        style = TextStyle(fontSize = 14.sp)
                    )

                    // Instruction text - with custom placeholder implementation
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(darkGray, RoundedCornerShape(8.dp))
                    ) {
                        BasicTextField(
                            value = instruction,
                            onValueChange = {
                                val newInstructions = instructions.toMutableList()
                                newInstructions[index] = it
                                instructions = newInstructions
                            },
                            textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                            cursorBrush = SolidColor(Color.White),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        )

                        if (instruction.isEmpty()) {
                            Text(
                                text = "Step instruction",
                                color = Color.Gray,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Delete button (except for first instruction)
                    if (instructions.size > 1) {
                        IconButton(
                            onClick = {
                                instructions = instructions.filterIndexed { i, _ -> i != index }
                            },
                            modifier = Modifier.size(35.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Remove step",
                                tint = Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.size(35.dp))
                    }
                }
            }

            // Add instruction button
            TextButton(
                onClick = {
                    instructions = instructions + ""
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "Add step",
                    tint = blueText
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Add Step",
                    color = blueText
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Image section
            Text(
                text = "Recipe Image",
                color = Color.White,
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium)
            )

            Spacer(modifier = Modifier.height(8.dp))

            selectedImageUri?.let { uri ->
                Image(
                    painter = rememberAsyncImagePainter(uri),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } ?: Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(darkGray)
                    .clickable { imagePickerLauncher.launch("image/*") }
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.Image,
                        contentDescription = "Add Image",
                        tint = Color.Gray,
                        modifier = Modifier.size(48.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Add Recipe Image",
                        color = Color.Gray
                    )
                }
            }
        }

        // Dietary Tags Dialog
        if (showDietaryTagsDialog) {
            Dialog(onDismissRequest = { showDietaryTagsDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = darkBackground)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Select Dietary Tags",
                            color = Color.White,
                            style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        val availableTags = listOf(
                            "vegetarian", "vegan", "gluten-free", "dairy-free",
                            "low-carb", "keto", "high-protein", "low-fat",
                            "low-sodium", "paleo", "whole30", "sugar-free"
                        )

                        availableTags.forEach { tag ->
                            val isSelected = selectedDietaryTags.contains(tag)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .clickable {
                                        selectedDietaryTags = if (isSelected) {
                                            selectedDietaryTags - tag
                                        } else {
                                            selectedDietaryTags + tag
                                        }
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = null,
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = blueText,
                                        uncheckedColor = Color.Gray
                                    )
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = tag,
                                    color = Color.White,
                                    style = TextStyle(fontSize = 16.sp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showDietaryTagsDialog = false }) {
                                Text(
                                    text = "Cancel",
                                    color = Color.Gray
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = { showDietaryTagsDialog = false },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = blueText
                                )
                            ) {
                                Text("Apply")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RecipeCreationScreenPreview() {
    RecipeCreationPage(
        navController = rememberNavController(),
        context = LocalContext.current
    )
}