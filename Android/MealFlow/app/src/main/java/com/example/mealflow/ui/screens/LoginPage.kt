package com.example.mealflow.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.mealflow.navigation.Destination
import androidx.navigation.compose.rememberNavController
import com.example.mealflow.R
import com.example.mealflow.utils.*
import com.example.mealflow.utils.googleSignIn.GoogleSignInViewModel
import com.example.mealflow.utils.googleSignIn.getGoogleSignInClientFromResources
import com.example.mealflow.utils.googleSignIn.handleGoogleSignInResult
import com.example.mealflow.viewModel.LoginViewModel

@Composable
fun LoginPage(navController: NavController) {
    val viewModel: LoginViewModel = viewModel()
    val googleViewModel: GoogleSignInViewModel = viewModel()

    val email by viewModel.email.observeAsState("")
    val password by viewModel.password.observeAsState("")
    val passwordVisible by viewModel.passwordVisible.observeAsState(false)
    val emailError = Validator.validateEmail(email)
    val passwordError = Validator.validatePassword(password)

    var isFocusedEmail by remember { mutableStateOf(false) }
    var isFocusedPassword by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val navigateToHome by viewModel.navigateToHome.observeAsState(false)
//    val googleNavigateToHome by googleViewModel.navigateToHome.observeAsState(false)

    LaunchedEffect(navigateToHome) {
        if (navigateToHome) {
            navController.navigate(Destination.Home) {
                popUpTo(Destination.Start) { inclusive = true }
                launchSingleTop = true
            }
            viewModel.onHomeNavigationComplete()
        }
    }

//    LaunchedEffect(googleNavigateToHome) {
//        if (googleNavigateToHome) {
//            navController.navigate("Home Page") {
//                popUpTo("Start Page") { inclusive = true }
//                launchSingleTop = true
//            }
//            googleViewModel.onHomeNavigationComplete()
//        }
//    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        handleGoogleSignInResult(
            context = context,
            data = result.data,
            viewModel = googleViewModel,
            navController = navController
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState, Modifier.windowInsetsPadding(WindowInsets.ime)) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->

        Column(modifier = Modifier.padding(paddingValues)) {

            Text(
                text = stringResource(id = R.string.Welcome_back),
                Modifier.padding(start = 20.dp, top = 40.dp, end = 20.dp),
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily(Font(R.font.sf_pro_rounded_heavy))
            )

            // ----------------------- Email Field ---------------------------
            OutlinedTextField(
                value = email,
                onValueChange = { viewModel.updateEmail(it) },
                label = { Text(stringResource(id = R.string.EnterEmail), fontFamily = FontFamily(Font(R.font.sflightit))) },
                singleLine = true,
                textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (emailError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, top = 20.dp, end = 20.dp)
                    .onFocusChanged { isFocusedEmail = it.isFocused }
            )
            if (isFocusedEmail && emailError != null) {
                Text(
                    text = emailError,
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 5.dp),
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp
                )
            }

            // ----------------------- Password Field ---------------------------
            OutlinedTextField(
                value = password,
                onValueChange = { viewModel.updatePassword(it) },
                label = { Text(stringResource(id = R.string.EnterPassword), fontFamily = FontFamily(Font(R.font.sflightit))) },
                trailingIcon = {
                    Icon(
                        painter = painterResource(
                            id = if (passwordVisible) R.drawable.eye_view_icon else R.drawable.eye_closed_icon
                        ),
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(28.dp).clickable { viewModel.togglePasswordVisibility() }
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (passwordError != null) Color.Red else Color.Blue
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, top = 20.dp, end = 20.dp)
                    .onFocusChanged { isFocusedPassword = it.isFocused },
                singleLine = true,
                textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password)
            )
            if (isFocusedPassword && passwordError != null) {
                Text(
                    text = passwordError,
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 5.dp),
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp
                )
            }

            Row(
                Modifier.fillMaxWidth().padding(top = 20.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = stringResource(id = R.string.ForgotPassword),
                    Modifier.clickable { navController.navigate(Destination.ForgetPassword) }.padding(end = 25.dp),
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily(Font(R.font.sflight)),
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick = {
                    viewModel.loginUser(
                        context = context,
                        email = email,
                        password = password,
                        navController = navController,
                        snackbarHostState = snackbarHostState
                    )
                },
                modifier = Modifier.padding(20.dp).align(Alignment.CenterHorizontally).fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                ),
                enabled = !viewModel.isLoading
            ) {
                if (viewModel.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text(text = stringResource(id = R.string.Login), color = Color.White)
                }
            }

            Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text(
                    text = stringResource(id = R.string.DoNot_Account),
                    fontFamily = FontFamily(Font(R.font.sflightit)),
                    modifier = Modifier.padding(end = 5.dp)
                )
                Text(
                    text = stringResource(id = R.string.Register),
                    Modifier.clickable { navController.navigate(Destination.Register) },
                    color = MaterialTheme.colorScheme.tertiary,
                    fontFamily = FontFamily(Font(R.font.sfmed))
                )
            }

            OrDivider("OR")

            Button(
                onClick = {
                    val signInClient = getGoogleSignInClientFromResources(context)
                    val signInIntent = signInClient.signInIntent
                    launcher.launch(signInIntent)
                },
                Modifier.padding(start = 20.dp, end = 20.dp, bottom = 20.dp)
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.google_icon_icons_com_62736),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(24.dp).padding(end = 10.dp)
                )
                Text(
                    text = "Login with Google",
                    color = Color.White,
                    fontFamily = FontFamily(Font(R.font.sfmed))
                )
            }
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun PreviewLoginPage() {
    LoginPage(navController = rememberNavController())
}
