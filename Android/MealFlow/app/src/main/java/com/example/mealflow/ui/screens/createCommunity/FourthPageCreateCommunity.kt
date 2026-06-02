//////package com.example.mealflow.ui.screens.createCommunity
//////
//////import android.util.Log
//////import androidx.compose.foundation.layout.Column
//////import androidx.compose.foundation.layout.Spacer
//////import androidx.compose.foundation.layout.fillMaxSize
//////import androidx.compose.foundation.layout.fillMaxWidth
//////import androidx.compose.foundation.layout.height
//////import androidx.compose.foundation.layout.navigationBarsPadding
//////import androidx.compose.foundation.layout.padding
//////import androidx.compose.foundation.layout.systemBarsPadding
//////import androidx.compose.material3.MaterialTheme
//////import androidx.compose.material3.Scaffold
//////import androidx.compose.material3.Snackbar
//////import androidx.compose.material3.SnackbarHost
//////import androidx.compose.material3.SnackbarHostState
//////import androidx.compose.material3.Text
//////import androidx.compose.runtime.Composable
//////import androidx.compose.runtime.livedata.observeAsState
//////import androidx.compose.runtime.remember
//////import androidx.compose.ui.Modifier
//////import androidx.compose.ui.platform.LocalContext
//////import androidx.compose.ui.text.font.FontWeight
//////import androidx.compose.ui.text.style.TextAlign
//////import androidx.compose.ui.tooling.preview.Preview
//////import androidx.compose.ui.unit.dp
//////import androidx.compose.ui.unit.sp
//////import androidx.lifecycle.viewmodel.compose.viewModel
//////import androidx.navigation.NavController
//////import androidx.navigation.compose.rememberNavController
//////import com.example.mealflow.database.token.TokenManager
//////import com.example.mealflow.network.createCommunityApi
//////import com.example.mealflow.ui.components.PrivacySettingsScreen
//////import com.example.mealflow.ui.components.TopBarCreateCommunity
//////import com.example.mealflow.viewModel.CreateCommunityViewModel
//////
//////@Composable
//////fun FourthStep(
//////    navController: NavController,
//////    viewModel: CreateCommunityViewModel = viewModel(),
//////) {
//////    // Existing state observations
//////    val communityName = viewModel.communityName.observeAsState("").value
//////    val communityDescription = viewModel.communityDescription.observeAsState("").value
//////    val recipeCreationPermission = viewModel.recipeCreationPermission.observeAsState("").value
//////    val categories = viewModel.categories.observeAsState(emptyList()).value
//////    val selectedImageUri = viewModel.selectedImageUri
//////
//////    // Create SnackbarHostState
//////    val snackbarHostState = remember { SnackbarHostState() }
//////
//////    val context = LocalContext.current
//////    val tokenManager = TokenManager(context)
//////    val accessToken = tokenManager.getAccessToken()
//////
//////    val isButtonEnabled = communityName.isNotBlank() &&
//////            communityDescription.isNotBlank()
//////
//////    // Wrap the entire content in a Scaffold to ensure SnackbarHost is visible
//////    Scaffold(
//////        topBar = {
//////            TopBarCreateCommunity(
//////                navController = navController,
//////                textNumber = "4 of 4",
//////                onClick = {
//////                    Log.d("FourthStep", "Creating Community")
//////                    Log.d("FourthStep", "Name: $communityName")
//////                    Log.d("FourthStep", "Description: $communityDescription")
//////                    Log.d("FourthStep", "Token: $accessToken")
//////                    Log.d("FourthStep", "Image URI: $selectedImageUri")
//////
//////                    accessToken?.let {
//////                        createCommunityApi(
//////                            context,
//////                            communityName,
//////                            communityDescription,
//////                            recipeCreationPermission,
//////                            it,
//////                            categories,
//////                            selectedImageUri,
//////                            navController = navController,
//////                        )
//////                    }
//////                },
//////                isButtonEnabled = isButtonEnabled,
//////                buttonText = "Create"
//////            )
//////        },
//////        snackbarHost = {
//////            SnackbarHost(hostState = snackbarHostState) { data ->
//////                Snackbar(
//////                    snackbarData = data,
//////                    modifier = Modifier.padding(16.dp),
//////                    containerColor = MaterialTheme.colorScheme.primaryContainer,
//////                    contentColor = MaterialTheme.colorScheme.onPrimary
//////                )
//////            }
//////        }
//////    ) { paddingValues ->
//////        Column(
//////            modifier = Modifier
//////                .fillMaxSize()
//////                .navigationBarsPadding()
//////                .systemBarsPadding()
//////                .padding(paddingValues)
//////                .padding(16.dp)
//////        ) {
//////            Spacer(modifier = Modifier.height(16.dp))
//////
//////            Text(
//////                text = "Select community type",
//////                modifier = Modifier.fillMaxWidth(),
//////                textAlign = TextAlign.Center,
//////                fontSize = 32.sp,
//////                fontWeight = FontWeight.Bold,
//////                color = MaterialTheme.colorScheme.primary
//////            )
//////
//////            Spacer(modifier = Modifier.height(8.dp))
//////
//////            Text(
//////                text = "Decide who can view and contribute in your community. Only public communities show up in search. Important: Once set, you can only change your community type with Reddit's approval.",
//////                modifier = Modifier.fillMaxWidth(),
//////                textAlign = TextAlign.Center,
//////                fontSize = 16.sp,
//////                color = MaterialTheme.colorScheme.onSurfaceVariant
//////            )
//////
//////            Spacer(modifier = Modifier.height(40.dp))
//////
//////            PrivacySettingsScreen(viewModel)
//////        }
//////    }
//////}
//////
//////
//////@Preview(showBackground = true, showSystemUi = true)
//////@Composable
//////fun PreviewFourthStep() {
//////    FourthStep(navController = rememberNavController())
//////}
////
////package com.example.mealflow.ui.screens.createCommunity
////
////import android.util.Log
////import androidx.compose.foundation.layout.Box
////import androidx.compose.foundation.layout.Column
////import androidx.compose.foundation.layout.Spacer
////import androidx.compose.foundation.layout.fillMaxSize
////import androidx.compose.foundation.layout.fillMaxWidth
////import androidx.compose.foundation.layout.height
////import androidx.compose.foundation.layout.navigationBarsPadding
////import androidx.compose.foundation.layout.padding
////import androidx.compose.foundation.layout.systemBarsPadding
////import androidx.compose.material3.CircularProgressIndicator
////import androidx.compose.material3.MaterialTheme
////import androidx.compose.material3.Scaffold
////import androidx.compose.material3.Snackbar
////import androidx.compose.material3.SnackbarHost
////import androidx.compose.material3.SnackbarHostState
////import androidx.compose.material3.Text
////import androidx.compose.runtime.Composable
////import androidx.compose.runtime.LaunchedEffect
////import androidx.compose.runtime.livedata.observeAsState
////import androidx.compose.runtime.remember
////import androidx.compose.ui.Alignment
////import androidx.compose.ui.Modifier
////import androidx.compose.ui.platform.LocalContext
////import androidx.compose.ui.text.font.FontWeight
////import androidx.compose.ui.text.style.TextAlign
////import androidx.compose.ui.tooling.preview.Preview
////import androidx.compose.ui.unit.dp
////import androidx.compose.ui.unit.sp
////import androidx.lifecycle.viewmodel.compose.viewModel
////import androidx.navigation.NavController
////import androidx.navigation.compose.rememberNavController
////import com.example.mealflow.database.token.TokenManager
////import com.example.mealflow.ui.components.PrivacySettingsScreen
////import com.example.mealflow.ui.components.TopBarCreateCommunity
////import com.example.mealflow.viewModel.CreateCommunityViewModel
////
////@Composable
////fun FourthStep(
////    navController: NavController,
////    viewModel: CreateCommunityViewModel = viewModel(),
////) {
////    // Existing state observations
////    val communityName = viewModel.communityName.observeAsState("").value
////    val communityDescription = viewModel.communityDescription.observeAsState("").value
////    val recipeCreationPermission = viewModel.recipeCreationPermission.observeAsState("").value
////    val categories = viewModel.categories.observeAsState(emptyList()).value
////    val selectedImageUri = viewModel.selectedImageUri
////
////    // New loading and error state observations
////    val isLoading = viewModel.isLoading
////    val errorMessage = viewModel.errorMessage
////
////    // Create SnackbarHostState
////    val snackbarHostState = remember { SnackbarHostState() }
////
////    val context = LocalContext.current
////    val tokenManager = TokenManager(context)
////    val accessToken = tokenManager.getAccessToken()
////
////    // Show error message in Snackbar if exists
////    LaunchedEffect(errorMessage) {
////        errorMessage?.let { message ->
////            snackbarHostState.showSnackbar(message)
////            viewModel.errorMessage = null // Clear the error after showing
////        }
////    }
////
////    val isButtonEnabled = communityName.isNotBlank() &&
////            communityDescription.isNotBlank() &&
////            !isLoading // Disable button while loading
////
////    // Wrap the entire content in a Scaffold to ensure SnackbarHost is visible
////    Scaffold(
////        topBar = {
////            TopBarCreateCommunity(
////                navController = navController,
////                textNumber = "4 of 4",
////                onClick = {
////                    Log.d("FourthStep", "Creating Community")
////                    Log.d("FourthStep", "Name: $communityName")
////                    Log.d("FourthStep", "Description: $communityDescription")
////                    Log.d("FourthStep", "Token: $accessToken")
////                    Log.d("FourthStep", "Image URI: $selectedImageUri")
////
////                    accessToken?.let {
////                        // Use the ViewModel's createCommunity function instead of direct API call
////                        viewModel.createCommunity(
////                            context = context,
////                            accessToken = it,
////                            navController = navController
////                        )
////                    } ?: run {
////                        viewModel.errorMessage = "Access token not found. Please login again."
////                    }
////                },
////                isButtonEnabled = isButtonEnabled,
////                buttonText = if (isLoading) "Creating..." else "Create"
////            )
////        },
////        snackbarHost = {
////            SnackbarHost(hostState = snackbarHostState) { data ->
////                Snackbar(
////                    snackbarData = data,
////                    modifier = Modifier.padding(16.dp),
////                    containerColor = MaterialTheme.colorScheme.errorContainer,
////                    contentColor = MaterialTheme.colorScheme.onErrorContainer
////                )
////            }
////        }
////    ) { paddingValues ->
////        Box(
////            modifier = Modifier
////                .fillMaxSize()
////                .navigationBarsPadding()
////                .systemBarsPadding()
////                .padding(paddingValues)
////                .padding(16.dp)
////        ) {
////            Column(
////                modifier = Modifier.fillMaxSize()
////            ) {
////                Spacer(modifier = Modifier.height(16.dp))
////
////                Text(
////                    text = "Select community type",
////                    modifier = Modifier.fillMaxWidth(),
////                    textAlign = TextAlign.Center,
////                    fontSize = 32.sp,
////                    fontWeight = FontWeight.Bold,
////                    color = MaterialTheme.colorScheme.primary
////                )
////
////                Spacer(modifier = Modifier.height(8.dp))
////
////                Text(
////                    text = "Decide who can view and contribute in your community. Only public communities show up in search. Important: Once set, you can only change your community type with Reddit's approval.",
////                    modifier = Modifier.fillMaxWidth(),
////                    textAlign = TextAlign.Center,
////                    fontSize = 16.sp,
////                    color = MaterialTheme.colorScheme.onSurfaceVariant
////                )
////
////                Spacer(modifier = Modifier.height(40.dp))
////
////                PrivacySettingsScreen(viewModel)
////            }
////
////            // Show loading indicator over the content when loading
////            if (isLoading) {
////                Box(
////                    modifier = Modifier
////                        .fillMaxSize(),
////                    contentAlignment = Alignment.Center
////                ) {
////                    CircularProgressIndicator(
////                        color = MaterialTheme.colorScheme.primary
////                    )
////                }
////            }
////        }
////    }
////}
////
////@Preview(showBackground = true, showSystemUi = true)
////@Composable
////fun PreviewFourthStep() {
////    FourthStep(navController = rememberNavController())
////}
//package com.example.mealflow.ui.screens.createCommunity
//
//import android.util.Log
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.navigationBarsPadding
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.systemBarsPadding
//import androidx.compose.material3.CircularProgressIndicator
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Snackbar
//import androidx.compose.material3.SnackbarHost
//import androidx.compose.material3.SnackbarHostState
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.livedata.observeAsState
//import androidx.compose.runtime.remember
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.lifecycle.viewmodel.compose.viewModel
//import androidx.navigation.NavController
//import androidx.navigation.compose.rememberNavController
//import com.example.mealflow.database.token.TokenManager
//import com.example.mealflow.ui.components.PrivacySettingsScreen
//import com.example.mealflow.ui.components.TopBarCreateCommunity
//import com.example.mealflow.viewModel.CreateCommunityViewModel
//
//@Composable
//fun FourthStep(
//    navController: NavController,
//    viewModel: CreateCommunityViewModel = viewModel(),
//) {
//    // Existing state observations
//    val communityName = viewModel.communityName.observeAsState("").value
//    val communityDescription = viewModel.communityDescription.observeAsState("").value
//    val recipeCreationPermission = viewModel.recipeCreationPermission.observeAsState("").value
//    val categories = viewModel.categories.observeAsState(emptyList()).value
//    val selectedImageUri = viewModel.selectedImageUri
//
//    // New loading and error state observations
//    val isLoading = viewModel.isLoading
//    val errorMessage = viewModel.errorMessage
//
//    // Create SnackbarHostState
//    val snackbarHostState = remember { SnackbarHostState() }
//
//    val context = LocalContext.current
//    val tokenManager = TokenManager(context)
//    val accessToken = tokenManager.getAccessToken()
//
//    // Show error message in Snackbar if exists
//    LaunchedEffect(errorMessage) {
//        errorMessage?.let { message ->
//            snackbarHostState.showSnackbar(message)
//            viewModel.errorMessage = null // Clear the error after showing
//        }
//    }
//
//    val isButtonEnabled = communityName.isNotBlank() &&
//            communityDescription.isNotBlank() &&
//            !isLoading // Disable button while loading
//
//    // Wrap the entire content in a Scaffold to ensure SnackbarHost is visible
//    Scaffold(
//        topBar = {
//            TopBarCreateCommunity(
//                navController = navController,
//                textNumber = "4 of 4",
//                onClick = {
//                    Log.d("FourthStep", "Creating Community")
//                    Log.d("FourthStep", "Name: $communityName")
//                    Log.d("FourthStep", "Description: $communityDescription")
//                    Log.d("FourthStep", "Token: $accessToken")
//                    Log.d("FourthStep", "Image URI: $selectedImageUri")
//
//                    accessToken?.let {
//                        // Use the ViewModel's createCommunity function instead of direct API call
//                        viewModel.createCommunity(
//                            context = context,
//                            accessToken = it,
//                            navController = navController
//                        )
//                    } ?: run {
//                        viewModel.errorMessage = "Access token not found. Please login again."
//                    }
//                },
//                isButtonEnabled = isButtonEnabled,
//                buttonText = "Create",
//                isLoading = isLoading // Pass loading state to TopBar
//            )
//        },
//        snackbarHost = {
//            SnackbarHost(hostState = snackbarHostState) { data ->
//                Snackbar(
//                    snackbarData = data,
//                    modifier = Modifier.padding(16.dp),
//                    containerColor = MaterialTheme.colorScheme.errorContainer,
//                    contentColor = MaterialTheme.colorScheme.onErrorContainer
//                )
//            }
//        }
//    ) { paddingValues ->
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .navigationBarsPadding()
//                .systemBarsPadding()
//                .padding(paddingValues)
//                .padding(16.dp)
//        ) {
//            Spacer(modifier = Modifier.height(16.dp))
//
//            Text(
//                text = "Select community type",
//                modifier = Modifier.fillMaxWidth(),
//                textAlign = TextAlign.Center,
//                fontSize = 32.sp,
//                fontWeight = FontWeight.Bold,
//                color = MaterialTheme.colorScheme.primary
//            )
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            Text(
//                text = "Decide who can view and contribute in your community. Only public communities show up in search. Important: Once set, you can only change your community type with Reddit's approval.",
//                modifier = Modifier.fillMaxWidth(),
//                textAlign = TextAlign.Center,
//                fontSize = 16.sp,
//                color = MaterialTheme.colorScheme.onSurfaceVariant
//            )
//
//            Spacer(modifier = Modifier.height(40.dp))
//
//            PrivacySettingsScreen(viewModel)
//        }
//    }
//}
//
//@Preview(showBackground = true, showSystemUi = true)
//@Composable
//fun PreviewFourthStep() {
//    FourthStep(navController = rememberNavController())
//}