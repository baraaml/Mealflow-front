// File: app/src/main/java/com/example/mealflow/ui/components/SegmentedButton.kt
package com.example.mealflow.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SegmentedButton(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        options.forEachIndexed { index, option ->
            val shape = when (index) {
                0 -> RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp, topEnd = 0.dp, bottomEnd = 0.dp)
                options.lastIndex -> RoundedCornerShape(topStart = 0.dp, bottomStart = 0.dp, topEnd = 8.dp, bottomEnd = 8.dp)
                else -> RoundedCornerShape(0.dp)
            }
            OutlinedButton(
                onClick = { onOptionSelected(option) },
                shape = shape,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                colors = if (selectedOption == option) {
                    ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier.weight(1f)
                    .padding(horizontal = if (index != 0 && index != options.lastIndex) 0.dp else 0.dp) // No internal horizontal padding
            ) {
                Text(text = option)
            }
        }
    }
}