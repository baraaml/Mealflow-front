package com.example.mealflow.utils

import android.util.Log
import com.example.mealflow.data.model.MealIngredient
import com.example.mealflow.data.model.EnhancedShoppingListItem
import kotlin.math.abs

/**
 * Utility class for parsing and normalizing ingredients for shopping lists
 */
object IngredientParser {

    // Common unit conversions for combining similar ingredients
    private val unitConversions = mapOf(
        // Volume conversions
        "ml" to mapOf("liter" to 0.001, "l" to 0.001, "milliliter" to 1.0, "milliliters" to 1.0),
        "milliliter" to mapOf("ml" to 1.0, "liter" to 0.001, "l" to 0.001, "milliliters" to 1.0),
        "milliliters" to mapOf("ml" to 1.0, "liter" to 0.001, "l" to 0.001, "milliliter" to 1.0),
        "liter" to mapOf("ml" to 1000.0, "l" to 1.0, "milliliter" to 1000.0, "milliliters" to 1000.0),
        "l" to mapOf("ml" to 1000.0, "liter" to 1.0, "milliliter" to 1000.0, "milliliters" to 1000.0),
        "cup" to mapOf("cups" to 1.0, "ml" to 236.588, "liter" to 0.236588, "l" to 0.236588),
        "cups" to mapOf("cup" to 1.0, "ml" to 236.588, "liter" to 0.236588, "l" to 0.236588),
        "tbsp" to mapOf("tablespoon" to 1.0, "tablespoons" to 1.0, "tsp" to 3.0, "teaspoon" to 3.0, "teaspoons" to 3.0, "ml" to 14.7868),
        "tablespoon" to mapOf("tbsp" to 1.0, "tablespoons" to 1.0, "tsp" to 3.0, "teaspoon" to 3.0, "teaspoons" to 3.0, "ml" to 14.7868),
        "tablespoons" to mapOf("tbsp" to 1.0, "tablespoon" to 1.0, "tsp" to 3.0, "teaspoon" to 3.0, "teaspoons" to 3.0, "ml" to 14.7868),
        "tsp" to mapOf("teaspoon" to 1.0, "teaspoons" to 1.0, "tbsp" to 0.333, "tablespoon" to 0.333, "tablespoons" to 0.333, "ml" to 4.92892),
        "teaspoon" to mapOf("tsp" to 1.0, "teaspoons" to 1.0, "tbsp" to 0.333, "tablespoon" to 0.333, "tablespoons" to 0.333, "ml" to 4.92892),
        "teaspoons" to mapOf("tsp" to 1.0, "teaspoon" to 1.0, "tbsp" to 0.333, "tablespoon" to 0.333, "tablespoons" to 0.333, "ml" to 4.92892),
        "oz" to mapOf("ounce" to 1.0, "ounces" to 1.0, "fluid ounce" to 1.0, "fluid ounces" to 1.0, "ml" to 29.5735),
        "ounce" to mapOf("oz" to 1.0, "ounces" to 1.0, "fluid ounce" to 1.0, "fluid ounces" to 1.0, "ml" to 29.5735),
        "ounces" to mapOf("oz" to 1.0, "ounce" to 1.0, "fluid ounce" to 1.0, "fluid ounces" to 1.0, "ml" to 29.5735),
        "fluid ounce" to mapOf("oz" to 1.0, "ounce" to 1.0, "ounces" to 1.0, "fluid ounces" to 1.0, "ml" to 29.5735),
        "fluid ounces" to mapOf("oz" to 1.0, "ounce" to 1.0, "ounces" to 1.0, "fluid ounce" to 1.0, "ml" to 29.5735),
        
        // Weight conversions
        "g" to mapOf("gram" to 1.0, "grams" to 1.0, "kg" to 0.001, "kilogram" to 0.001, "kilograms" to 0.001),
        "gram" to mapOf("g" to 1.0, "grams" to 1.0, "kg" to 0.001, "kilogram" to 0.001, "kilograms" to 0.001),
        "grams" to mapOf("g" to 1.0, "gram" to 1.0, "kg" to 0.001, "kilogram" to 0.001, "kilograms" to 0.001),
        "kg" to mapOf("g" to 1000.0, "gram" to 1000.0, "grams" to 1000.0, "kilogram" to 1.0, "kilograms" to 1.0),
        "kilogram" to mapOf("g" to 1000.0, "gram" to 1000.0, "grams" to 1000.0, "kg" to 1.0, "kilograms" to 1.0),
        "kilograms" to mapOf("g" to 1000.0, "gram" to 1000.0, "grams" to 1000.0, "kg" to 1.0, "kilogram" to 1.0),
        "lb" to mapOf("pound" to 1.0, "pounds" to 1.0, "g" to 453.592, "kg" to 0.453592),
        "pound" to mapOf("lb" to 1.0, "pounds" to 1.0, "g" to 453.592, "kg" to 0.453592),
        "pounds" to mapOf("lb" to 1.0, "pound" to 1.0, "g" to 453.592, "kg" to 0.453592)
    )
    
    // Common ingredient name variations for consolidation
    private val ingredientNameVariations = mapOf(
        "onion" to listOf("onions", "yellow onion", "white onion", "red onion"),
        "garlic" to listOf("garlic clove", "garlic cloves", "minced garlic"),
        "tomato" to listOf("tomatoes", "roma tomato", "roma tomatoes", "cherry tomato", "cherry tomatoes"),
        "potato" to listOf("potatoes", "russet potato", "russet potatoes", "yukon gold potato", "yukon gold potatoes"),
        "carrot" to listOf("carrots", "baby carrot", "baby carrots"),
        "bell pepper" to listOf("bell peppers", "red bell pepper", "green bell pepper", "yellow bell pepper"),
        "olive oil" to listOf("extra virgin olive oil", "evoo"),
        "vegetable oil" to listOf("canola oil"),
        "rice" to listOf("white rice", "brown rice", "jasmine rice", "basmati rice"),
        "flour" to listOf("all-purpose flour", "white flour", "whole wheat flour", "bread flour"),
        "sugar" to listOf("white sugar", "granulated sugar", "brown sugar", "cane sugar"),
        "salt" to listOf("sea salt", "kosher salt", "table salt", "iodized salt"),
        "pepper" to listOf("black pepper", "ground black pepper", "white pepper", "ground pepper"),
        "chicken breast" to listOf("chicken breasts", "boneless chicken breast", "skinless chicken breast", "boneless skinless chicken breast"),
        "ground beef" to listOf("minced beef", "beef mince", "hamburger meat")
    )
    
    /**
     * Normalize an ingredient unit to a standard form
     */
    fun normalizeUnit(unit: String?): String {
        if (unit == null || unit.isBlank()) return "unit"
        
        val cleanUnit = unit.trim().lowercase()
        
        // Direct mappings for unit normalization
        return when(cleanUnit) {
            // Volume units
            "milliliter", "milliliters", "ml", "cc" -> "ml"
            "liter", "liters", "litre", "litres", "l" -> "l"
            "cup", "cups", "c" -> "cup"
            "tablespoon", "tablespoons", "tbsp", "tbs", "tb" -> "tbsp"
            "teaspoon", "teaspoons", "tsp", "ts" -> "tsp"
            "fluid ounce", "fluid ounces", "fl oz", "fl. oz." -> "fl oz"
            
            // Weight units
            "gram", "grams", "g", "gr" -> "g"
            "kilogram", "kilograms", "kilo", "kilos", "kg" -> "kg"
            "pound", "pounds", "lb", "lbs", "#" -> "lb"
            "ounce", "ounces", "oz" -> "oz"
            
            // Count units
            "piece", "pieces", "pc", "pcs" -> "piece"
            "slice", "slices" -> "slice"
            "clove", "cloves" -> "clove"
            "bunch", "bunches" -> "bunch"
            "package", "packages", "pkg", "pkgs" -> "package"
            "can", "cans" -> "can"
            "jar", "jars" -> "jar"
            "bottle", "bottles" -> "bottle"
            "box", "boxes" -> "box"
            
            // Default case
            else -> cleanUnit
        }
    }
    
    /**
     * Normalize an ingredient name to a standard form
     */
    fun normalizeIngredientName(name: String?): String {
        if (name == null || name.isBlank()) return "unknown ingredient"
        
        val cleanName = name.trim().lowercase()
        
        // Check if this is a variation of a standard ingredient name
        for ((standardName, variations) in ingredientNameVariations) {
            if (cleanName == standardName || variations.any { cleanName.contains(it) }) {
                return standardName
            }
        }
        
        return cleanName
    }
    
    /**
     * Attempt to convert between units if possible
     */
    fun convertUnits(quantity: Double, fromUnit: String, toUnit: String): Double? {
        val normalizedFromUnit = normalizeUnit(fromUnit)
        val normalizedToUnit = normalizeUnit(toUnit)
        
        // Same unit, no conversion needed
        if (normalizedFromUnit == normalizedToUnit) {
            return quantity
        }
        
        // Look up conversion factor
        return unitConversions[normalizedFromUnit]?.get(normalizedToUnit)?.let { conversionFactor ->
            quantity * conversionFactor
        }
    }
    
    /**
     * Check if two ingredients can be combined (same ingredient, compatible units)
     */
    fun canCombineIngredients(item1: EnhancedShoppingListItem, item2: EnhancedShoppingListItem): Boolean {
        // Only combine items for the same shopping day
        if (item1.shoppingDayIndex != item2.shoppingDayIndex) {
            return false
        }
        
        val name1 = normalizeIngredientName(item1.name)
        val name2 = normalizeIngredientName(item2.name)
        
        // Different ingredients can't be combined
        if (name1 != name2) {
            return false
        }
        
        val unit1 = normalizeUnit(item1.unit)
        val unit2 = normalizeUnit(item2.unit)
        
        // Same units can always be combined
        if (unit1 == unit2) {
            return true
        }
        
        // Different units need conversion check
        return unitConversions[unit1]?.containsKey(unit2) == true || 
               unitConversions[unit2]?.containsKey(unit1) == true
    }
    
    /**
     * Combine two compatible ingredients, using the first item's unit as the target
     */
    fun combineIngredients(item1: EnhancedShoppingListItem, item2: EnhancedShoppingListItem): EnhancedShoppingListItem {
        if (!canCombineIngredients(item1, item2)) {
            throw IllegalArgumentException("Cannot combine incompatible ingredients: ${item1.name}(${item1.unit}) and ${item2.name}(${item2.unit})")
        }
        
        val combinedQuantity = if (item1.unit == item2.unit) {
            // Same units, simple addition
            item1.quantity + item2.quantity
        } else {
            // Different units, try to convert
            val unit1 = normalizeUnit(item1.unit)
            val unit2 = normalizeUnit(item2.unit)
            
            val convertedQuantity = convertUnits(item2.quantity, unit2, unit1)
            if (convertedQuantity != null) {
                item1.quantity + convertedQuantity
            } else {
                // If conversion failed, just add as-is and log warning
                Log.w("IngredientParser", "Failed to convert between units: ${item2.unit} to ${item1.unit}")
                item1.quantity + item2.quantity
            }
        }
        
        // Use the first item as the base, but update the quantity
        return item1.copy(
            quantity = combinedQuantity,
            // Only mark as purchased if both were purchased
            isPurchased = item1.isPurchased && item2.isPurchased
        )
    }
    
    /**
     * Group and combine similar ingredients in a shopping list
     */
    fun consolidateShoppingList(items: List<EnhancedShoppingListItem>): List<EnhancedShoppingListItem> {
        if (items.isEmpty()) return items
        
        // Use a map to group items by a composite key: name+unit+shoppingDayIndex
        val itemGroups = mutableMapOf<String, MutableList<EnhancedShoppingListItem>>()
        
        // Group items by their identifying characteristics
        for (item in items) {
            val key = "${normalizeIngredientName(item.name).lowercase()}_${normalizeUnit(item.unit).lowercase()}_${item.shoppingDayIndex}"
            if (!itemGroups.containsKey(key)) {
                itemGroups[key] = mutableListOf()
            }
            itemGroups[key]?.add(item)
        }
        
        // Combine items in each group
        val result = mutableListOf<EnhancedShoppingListItem>()
        
        for ((_, groupItems) in itemGroups) {
            if (groupItems.isEmpty()) continue
            
            if (groupItems.size == 1) {
                // Single item, no need to combine
                result.add(groupItems.first())
            } else {
                // Multiple items to combine
                var combinedItem = groupItems.first()
                
                for (i in 1 until groupItems.size) {
                    combinedItem = combineIngredients(combinedItem, groupItems[i])
                }
                
                result.add(combinedItem)
            }
        }
        
        // Sort by shopping day and then by ingredient name
        return result.sortedWith(compareBy({ it.shoppingDayIndex }, { normalizeIngredientName(it.name) }))
    }
    
    /**
     * Group and combine similar ingredients in a shopping list while preserving the earliest meal date
     */
    fun consolidateShoppingListWithDates(items: List<EnhancedShoppingListItem>): List<EnhancedShoppingListItem> {
        if (items.isEmpty()) return items
        
        // Use a map to group items by a composite key: name+unit+shoppingDayIndex
        val itemGroups = mutableMapOf<String, MutableList<EnhancedShoppingListItem>>()
        
        // Group items by their identifying characteristics
        for (item in items) {
            val key = "${normalizeIngredientName(item.name).lowercase()}_${normalizeUnit(item.unit).lowercase()}_${item.shoppingDayIndex}"
            if (!itemGroups.containsKey(key)) {
                itemGroups[key] = mutableListOf()
            }
            itemGroups[key]?.add(item)
        }
        
        // Combine items in each group while preserving the earliest meal date
        val result = mutableListOf<EnhancedShoppingListItem>()
        
        for ((_, groupItems) in itemGroups) {
            if (groupItems.isEmpty()) continue
            
            if (groupItems.size == 1) {
                // Single item, no need to combine
                result.add(groupItems.first())
            } else {
                // Multiple items to combine
                var combinedItem = groupItems.first()
                var earliestMealDate = combinedItem.mealDate
                
                for (i in 1 until groupItems.size) {
                    combinedItem = combineIngredients(combinedItem, groupItems[i])
                    
                    // Keep the earliest meal date
                    val nextItemDate = groupItems[i].mealDate
                    if (nextItemDate != null && (earliestMealDate == null || 
                        (DateUtils.parseLocalDate(nextItemDate)?.isBefore(DateUtils.parseLocalDate(earliestMealDate)) == true))) {
                        earliestMealDate = nextItemDate
                    }
                }
                
                // Set the earliest meal date on the combined item
                result.add(combinedItem.copy(mealDate = earliestMealDate))
            }
        }
        
        // First sort by shopping day
        val sortedByShoppingDay = result.sortedBy { it.shoppingDayIndex }
        
        // Then within each shopping day, sort by meal date (earliest first)
        return sortedByShoppingDay.groupBy { it.shoppingDayIndex }
            .flatMap { (_, dayItems) -> 
                dayItems.sortedWith(
                    compareBy<EnhancedShoppingListItem> { item -> 
                        item.mealDate?.let { DateUtils.parseLocalDate(it) } 
                    }.thenBy { 
                        normalizeIngredientName(it.name) 
                    }
                )
            }
    }
    
    /**
     * Process a meal ingredient for shopping list
     */
    fun processIngredientForShoppingList(
        ingredient: MealIngredient, 
        shoppingDayIndex: Int
    ): EnhancedShoppingListItem {
        val normalizedName = normalizeIngredientName(ingredient.phrase.toString())
        val normalizedUnit = normalizeUnit(ingredient.unit)
        
        return EnhancedShoppingListItem(
            ingredientId = ingredient.id,
            name = normalizedName,
            quantity = ingredient.quantity ?: 0.0,
            unit = normalizedUnit,
            shoppingDayIndex = shoppingDayIndex
        )
    }
    
    /**
     * Suggest better unit if the quantity is excessive
     * For example, convert 1000ml to 1L
     */
    fun suggestBetterUnit(quantity: Double, unit: String): Pair<Double, String>? {
        val normalizedUnit = normalizeUnit(unit)
        
        // Conversions for better readability
        return when {
            // Volume optimizations
            normalizedUnit == "ml" && quantity >= 1000 -> Pair(quantity / 1000, "l")
            normalizedUnit == "ml" && quantity >= 15 && quantity < 30 -> Pair(quantity / 15, "tbsp")
            normalizedUnit == "ml" && quantity < 15 && quantity >= 5 -> Pair(quantity / 5, "tsp")
            normalizedUnit == "tsp" && quantity >= 3 -> Pair(quantity / 3, "tbsp")
            normalizedUnit == "tbsp" && quantity >= 16 -> Pair(quantity / 16, "cup")
            
            // Weight optimizations
            normalizedUnit == "g" && quantity >= 1000 -> Pair(quantity / 1000, "kg")
            normalizedUnit == "oz" && quantity >= 16 -> Pair(quantity / 16, "lb")
            
            // No optimization needed
            else -> null
        }
    }
    
    /**
     * Format quantity nicely for display
     */
    fun formatQuantity(quantity: Double): String {
        // For whole numbers, display as integers
        if (quantity == quantity.toInt().toDouble()) {
            return quantity.toInt().toString()
        }
        
        // For fractions, try to display as common fractions
        val nearestFraction = findNearestFraction(quantity)
        if (nearestFraction != null) {
            val wholePart = nearestFraction.first
            val num = nearestFraction.second
            val den = nearestFraction.third
            
            return if (wholePart > 0) {
                if (num > 0) "$wholePart $num/$den" else wholePart.toString()
            } else {
                "$num/$den"
            }
        }
        
        // Otherwise use decimal with 2 places
        return "%.2f".format(quantity).replace(Regex("\\.00$"), "")
    }
    
    /**
     * Find the nearest common fraction for a decimal
     */
    private fun findNearestFraction(value: Double): Triple<Int, Int, Int>? {
        val commonFractions = listOf(
            Triple(0, 1, 4),   // 1/4
            Triple(0, 1, 3),   // 1/3
            Triple(0, 1, 2),   // 1/2
            Triple(0, 2, 3),   // 2/3
            Triple(0, 3, 4)    // 3/4
        )
        
        val wholePart = value.toInt()
        val decimalPart = value - wholePart
        
        if (decimalPart == 0.0) {
            return Triple(wholePart, 0, 1)
        }
        
        // Find the closest common fraction
        var closestFraction: Triple<Int, Int, Int>? = null
        var smallestDifference = Double.MAX_VALUE
        
        for (fraction in commonFractions) {
            val fractionValue = fraction.second.toDouble() / fraction.third.toDouble()
            val difference = abs(decimalPart - fractionValue)
            
            if (difference < smallestDifference && difference < 0.05) {
                smallestDifference = difference
                closestFraction = Triple(wholePart, fraction.second, fraction.third)
            }
        }
        
        return closestFraction
    }
} 