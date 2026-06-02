package com.example.mealflow.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportBugPage(navController: NavController) {
    val context = LocalContext.current
    val developers = listOf(
        "baraa2104879@gmail.com",
        "abdulkareem.oe@gmail.com",
        "amrkhaled3303@gmail.com"
    )

    var selectedDeveloper by remember { mutableStateOf(developers[0]) }
    var bugTitle by remember { mutableStateOf("") }
    var bugDescription by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showDeveloperDialog by remember { mutableStateOf(false) }

    val getContent = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Report a Bug") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    sendBugReport(
                        context = context,
                        recipient = selectedDeveloper,
                        title = bugTitle,
                        description = bugDescription,
                        imageUri = selectedImageUri
                    )
                },
                containerColor = MaterialTheme.colorScheme.primary,
                icon = { Icon(Icons.Default.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.onPrimary) },
                text = { Text("Send Report", color = MaterialTheme.colorScheme.onPrimary) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(0.dp)) // Top padding

            // Developer selection
            Text("1. Select Recipient", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline,
                        RoundedCornerShape(8.dp)
                    )
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { showDeveloperDialog = true }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Developer",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        selectedDeveloper,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = "Select Developer"
                    )
                }
            }

            // Bug title
            Text("2. Summarize the Bug", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = bugTitle,
                onValueChange = { bugTitle = it },
                label = { Text("Bug Title") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g., App crashes on login") },
                leadingIcon = { Icon(Icons.Default.Title, contentDescription = null) },
                singleLine = true
            )

            // Bug description
            Text("3. Describe the Bug", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = bugDescription,
                onValueChange = { bugDescription = it },
                label = { Text("Bug Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                placeholder = { Text("Describe what happened and how to reproduce it.") },
                leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) }
            )

            // Screenshot attachment
            Text("4. Attach a Screenshot (Optional)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (selectedImageUri != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(12.dp))
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(
                                    ImageRequest.Builder(LocalContext.current)
                                        .data(data = selectedImageUri)
                                        .crossfade(true)
                                        .build()
                                ),
                                contentDescription = "Selected Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            IconButton(
                                onClick = { selectedImageUri = null },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                        shape = CircleShape
                                    )
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove Image",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    } else {
                        Button(
                            onClick = { getContent.launch("image/*") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add Screenshot")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp)) // Space for FAB
        }
    }

    if (showDeveloperDialog) {
        AlertDialog(
            onDismissRequest = { showDeveloperDialog = false },
            title = { Text("Select Developer") },
            text = {
                Column {
                    developers.forEach { email ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    selectedDeveloper = email
                                    showDeveloperDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (email == selectedDeveloper),
                                onClick = {
                                    selectedDeveloper = email
                                    showDeveloperDialog = false
                                }
                            )
                            Spacer(Modifier.width(16.dp))
                            Text(text = email)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDeveloperDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun sendBugReport(
    context: Context,
    recipient: String,
    title: String,
    description: String,
    imageUri: Uri?
) {
    if (title.isBlank() || description.isBlank()) {
        // Here you can show a Toast or a Snackbar
        android.widget.Toast.makeText(context, "Title and description cannot be empty.", android.widget.Toast.LENGTH_SHORT).show()
        return
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "message/rfc822" // Use this type for email clients
        putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
        putExtra(Intent.EXTRA_SUBJECT, "Bug Report: $title")
        putExtra(Intent.EXTRA_TEXT, description)

        if (imageUri != null) {
            // For including an image, it's better to use ACTION_SEND_MULTIPLE or handle it carefully
            // The simple ACTION_SEND with an image might not work on all email clients as expected.
            // A common approach is to set the type to the image type and put text in EXTRA_TEXT.
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, imageUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    try {
        context.startActivity(Intent.createChooser(intent, "Send Bug Report via..."))
    } catch (e: Exception) {
        android.widget.Toast.makeText(context, "No email app found.", android.widget.Toast.LENGTH_SHORT).show()
    }
}