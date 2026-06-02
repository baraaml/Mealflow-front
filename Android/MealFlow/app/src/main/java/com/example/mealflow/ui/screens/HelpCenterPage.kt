package com.example.mealflow.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

data class DeveloperInfo(
    val name: String,
    val role: String,
    val phone: String,
    val email: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpCenterPage(navController: NavController) {
    val developers = listOf(
        DeveloperInfo(
            name = "Baraa Ahmed",
            role = "Android Developer",
            phone = "+20 1027514653",
            email = "baraa2104879@gmail.com"
        ),
        DeveloperInfo(
            name = "Abdo Shokry",
            role = "Android Developer",
            phone = "+20 1283522202"
        ),
        DeveloperInfo(
            name = "Abdulkareem Mousa",
            role = "Backend Developer",
            phone = "+20 1159258507",
            email = "abdulkareem.oe@gmail.com"
        ),
        DeveloperInfo(
            name = "Amr Khaled",
            role = "Backend Developer",
            phone = "+20 1222 725042",
            email = "amrkhaled3303@gmail.com"
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Help Center") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(0.dp))
            Text(
                "Contact Our Team",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                "If you need help or have any questions, feel free to reach out to our developers directly.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Divider(modifier = Modifier.padding(vertical = 8.dp))

            developers.forEach { dev ->
                DeveloperContactCard(dev)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}


@Composable
fun DeveloperContactCard(developer: DeveloperInfo) {
    val context = LocalContext.current
    OutlinedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(developer.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(developer.role, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            Divider(modifier = Modifier.padding(vertical = 12.dp))

            ContactInfoRow(
                icon = Icons.Default.Phone,
                text = developer.phone,
                onClick = {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${developer.phone}"))
                    context.startActivity(intent)
                }
            )
            developer.email?.let { email ->
                Spacer(modifier = Modifier.height(8.dp))
                ContactInfoRow(
                    icon = Icons.Default.Email,
                    text = email,
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:")
                            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                        }
                        context.startActivity(Intent.createChooser(intent, "Send Email"))
                    }
                )
            }
        }
    }
}

@Composable
fun ContactInfoRow(icon: ImageVector, text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Action",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}