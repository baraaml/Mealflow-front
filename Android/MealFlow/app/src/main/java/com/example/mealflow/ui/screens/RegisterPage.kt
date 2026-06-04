package com.example.mealflow.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.example.mealflow.utils.googleSignIn.GoogleSignInViewModel
import com.example.mealflow.utils.OrDivider
import com.example.mealflow.utils.Validator
import com.example.mealflow.utils.googleSignIn.getGoogleSignInClientFromResources
import com.example.mealflow.utils.googleSignIn.handleGoogleSignInResult
import com.example.mealflow.viewModel.RegisterViewModel


// ----------------------- Register Page ---------------------------
@Composable
fun RegisterPage(
    navController: NavController,
    viewModel: RegisterViewModel,
    googleSignInViewModel: GoogleSignInViewModel = viewModel())
{
    // ----------------------- Variables ---------------------------
    val username by viewModel.username.observeAsState("")
    val email by viewModel.email.observeAsState("")
    val password by viewModel.password.observeAsState("")
    val repassword by viewModel.repassword.observeAsState("")
    val passwordVisible by viewModel.passwordVisible.observeAsState(false)
    val context = LocalContext.current

    // Validation errors
    val usernameError = Validator.validateUsername(username)
    val emailError = Validator.validateEmail(email)
    val passwordError = Validator.validatePassword(password)

    // Focus states
    var isFocusedUsername by remember { mutableStateOf(false) }
    var isFocusedEmail by remember { mutableStateOf(false) }
    var isFocusedPassword by remember { mutableStateOf(false) }
    var isFocusedRepassword by remember { mutableStateOf(false) }

    // ViewModel states
    val isLoading = viewModel.isLoading
    val errorMessage = viewModel.errorMessage

    // ViewModel states
    val isRegisterLoading = viewModel.isLoading
//    val isGoogleLoading by googleSignInViewModel.isLoading
    val isGoogleLoading = googleSignInViewModel.isLoading // Remove 'by' delegate since it's already a Compose state

    // No need to create client here as we create it on button click

    // المشغل (Launcher) يستدعي الآن الدالة المساعدة لمعالجة النتيجة
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        handleGoogleSignInResult(
            context = context,
            data = result.data,
            viewModel = googleSignInViewModel,
            navController = navController
        )
    }
    // snackbarHostState and CoroutineScope
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.ime) // Adjusts the position based on the keyboard height
        ) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues)
        ) {
            // ----------------------- Join Text -----------------------------
            Text(
                text = stringResource(id = R.string.join),
                Modifier.padding(start = 20.dp, top = 40.dp ,end = 20.dp),
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily(Font(R.font.sf_pro_rounded_heavy))
            )
            // ----------------------- InputFields ---------------------------
            // ---------------------------------------------------------------
            // Input field ------------------ UserName -----------------------
            OutlinedTextField(
                value = username,
                onValueChange = { viewModel.updateUsername(it) },
                label = {
                    Text(
                        text ="Enter username",
                        fontFamily = FontFamily(Font(R.font.sflightit))
                    )
                },
                singleLine = true,
                textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (usernameError != null) Color.Red else Color.Blue,  // Border color when focused
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, top = 20.dp, end = 20.dp)
                    .onFocusChanged { isFocusedUsername = it.isFocused }
            )
            // Text ------------------ Username Error -----------------------
            if (isFocusedUsername && usernameError != null)
            {
                Text(
                    text = usernameError,
                    modifier = Modifier
                        .padding(start = 24.dp, end = 24.dp, top = 5.dp),
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp
                )
            }

            // Input field ------------------ Email -----------------------
            OutlinedTextField(
                value = email,
                onValueChange = { viewModel.updateEmail(it) },
                label = {
                    Text(
                        text = "Add an Email",
                        fontFamily = FontFamily(Font(R.font.sflightit))
                    )
                },
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
            // Text ------------------ Email Error -----------------------
            if (isFocusedEmail && emailError != null)
            {
                Text(
                    text = emailError,
                    modifier = Modifier
                        .padding(start = 24.dp, end = 24.dp, top = 5.dp),
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp
                )
            }
            // Input field ------------------ Password -----------------------
            OutlinedTextField(
                value = password,
                onValueChange = { viewModel.updatePassword(it) },
                label = {
                    Text(
                        text = "Enter your password",
                        fontFamily = FontFamily(Font(R.font.sflightit))
                    ) },
                trailingIcon = {
                    Icon(
                        painter = painterResource(
                            id = if (passwordVisible) {
                                R.drawable.eye_view_icon
                            } else {
                                R.drawable.eye_closed_icon
                            }
                        ),
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier
                            .size(28.dp)
                            .clickable { viewModel.togglePasswordVisibility() }
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (passwordError != null) Color.Red else Color.Blue,  // Border color when focused
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
            // Text ------------------ Password Error -----------------------
            if (isFocusedPassword && passwordError != null)
            {
                Text(
                    text = passwordError,
                    modifier = Modifier
                        .padding(start = 24.dp, end = 24.dp, top = 5.dp),
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp
                )
            }

            // Button ------------------ Sign Up -----------------------
            // Sign Up Button
            Button(
                onClick = {
                    viewModel.register(context, navController)
                },
                Modifier
                    .padding(20.dp)
                    .align(alignment = Alignment.CenterHorizontally)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                ),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Sign up",
                        color = Color.White,
                        fontFamily = FontFamily(Font(R.font.sfmed))
                    )
                }
            }
            //----------------------- Button to go to the login page -----------------------
            Row(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = "You already have an account?",
                    modifier = Modifier.padding(end = 5.dp),
                    fontFamily = FontFamily(Font(R.font.sflightit))
                )
                Text(text = "Login",
                    Modifier
                        .clickable(onClick = {navController.navigate(Destination.Login)}
                        ),
                    color = MaterialTheme.colorScheme.tertiary,
                    fontFamily = FontFamily(Font(R.font.sfmed))
                )
            }
            // ----------------------- Line with text in the middle ---------------------------
            OrDivider("OR")
            // ----------------------- Button to sign in with google ---------------------------
            // Sign in with Google Button
            Button(
                onClick = {
                    // ابدأ التحميل ثم قم بتشغيل المشغل
                    googleSignInViewModel.setLoading(true)
                    val signInClient = getGoogleSignInClientFromResources(context)
                    launcher.launch(signInClient.signInIntent)
                },
                modifier = Modifier
                    .padding(start = 20.dp, end = 20.dp, bottom = 20.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                enabled = !isGoogleLoading && !isRegisterLoading
            ) {
                if (isGoogleLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.google_icon_icons_com_62736),
                        contentDescription = "Google Icon",
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .size(24.dp)
                            .padding(end = 10.dp)
                    )
                    Text("Sign up with Google", color = Color.White, fontFamily = FontFamily(Font(R.font.sfmed)))
                }
            }
//            Button(
//                onClick = { /*TODO*/ } ,
//                Modifier
//                    .padding(start = 20.dp, end = 20.dp, bottom = 20.dp)
//                    .align(alignment = Alignment.CenterHorizontally)
//                    .fillMaxWidth(),
//                shape = RoundedCornerShape(8.dp),
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = MaterialTheme.colorScheme.primary,
//                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
//                )
//            ) {
//                Icon(
//                    painter = painterResource(
//                        id = R.drawable.google_icon_icons_com_62736
//                    ),
//                    contentDescription = null,
//                    tint = Color.Unspecified,
//                    modifier = Modifier
//                        .size(24.dp)
//                        .padding(end = 10.dp)
//                )
//                Text(
//                    text = "Sign up using Google",
//                    color = Color.White,
//                    fontFamily = FontFamily(Font(R.font.sfmed))
//                )
//            }
        }
    }
}

// ----------------------- Function to preview RegisterPage ---------------------------
//---------------------------------------------------------------------------------
@Preview(showSystemUi = true, showBackground = true)
@Composable
fun PreviewRegisterPage()
{
    val registerViewModel: RegisterViewModel = viewModel()
    RegisterPage(navController = rememberNavController(),registerViewModel)
}
//---------------------------------------------------------------------------------
//---------------------------------------------------------------------------------