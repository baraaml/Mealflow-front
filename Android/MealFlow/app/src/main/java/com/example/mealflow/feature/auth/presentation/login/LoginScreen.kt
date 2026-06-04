package com.example.mealflow.feature.auth.presentation.login

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mealflow.R
import com.example.mealflow.core.presentation.util.ObserveAsEvents
import com.example.mealflow.utils.OrDivider
import com.example.mealflow.utils.Validator
import com.example.mealflow.utils.googleSignIn.getGoogleSignInClientFromResources
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginRoot(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgetPassword: () -> Unit,
    viewModel: LoginViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is LoginEvent.LoginSuccess -> onLoginSuccess()
            is LoginEvent.Error -> {
                // Show snackbar or toast
            }
        }
    }

    LoginScreen(
        state = state,
        onAction = viewModel::onAction,
        onNavigateToRegister = onNavigateToRegister,
        onNavigateToForgetPassword = onNavigateToForgetPassword
    )
}

@Composable
fun LoginScreen(
    state: LoginState,
    onAction: (LoginAction) -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgetPassword: () -> Unit
) {
    val context = LocalContext.current
    val emailError = Validator.validateEmail(state.email)
    val passwordError = Validator.validatePassword(state.password)

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ ->
        // Google sign-in result handling should be moved to Root or ViewModel
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            Text(
                text = stringResource(id = R.string.Welcome_back),
                modifier = Modifier.padding(start = 20.dp, top = 40.dp, end = 20.dp),
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily(Font(R.font.sf_pro_rounded_heavy))
            )

            // ----------------------- Email Field ---------------------------
            OutlinedTextField(
                value = state.email,
                onValueChange = { onAction(LoginAction.OnEmailChange(it)) },
                label = { Text(stringResource(id = R.string.EnterEmail), fontFamily = FontFamily(Font(R.font.sflightit))) },
                singleLine = true,
                textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (emailError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, top = 20.dp, end = 20.dp)
            )
            if (emailError != null && state.email.isNotEmpty()) {
                Text(
                    text = emailError,
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 5.dp),
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp
                )
            }

            // ----------------------- Password Field ---------------------------
            OutlinedTextField(
                value = state.password,
                onValueChange = { onAction(LoginAction.OnPasswordChange(it)) },
                label = { Text(stringResource(id = R.string.EnterPassword), fontFamily = FontFamily(Font(R.font.sflightit))) },
                trailingIcon = {
                    Icon(
                        painter = painterResource(
                            id = if (state.isPasswordVisible) R.drawable.eye_view_icon else R.drawable.eye_closed_icon
                        ),
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier
                            .size(28.dp)
                            .clickable { onAction(LoginAction.OnTogglePasswordVisibility) }
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (passwordError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, top = 20.dp, end = 20.dp),
                singleLine = true,
                textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground),
                visualTransformation = if (state.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password)
            )
            if (passwordError != null && state.password.isNotEmpty()) {
                Text(
                    text = passwordError,
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 5.dp),
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = stringResource(id = R.string.ForgotPassword),
                    modifier = Modifier
                        .clickable { onNavigateToForgetPassword() }
                        .padding(end = 25.dp),
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily(Font(R.font.sflight)),
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick = { onAction(LoginAction.OnLoginClick) },
                modifier = Modifier
                    .padding(20.dp)
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                ),
                enabled = !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
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
                    modifier = Modifier.clickable { onNavigateToRegister() },
                    color = MaterialTheme.colorScheme.tertiary,
                    fontFamily = FontFamily(Font(R.font.sfmed))
                )
            }

            OrDivider("OR")

            Button(
                onClick = {
                    val signInClient = getGoogleSignInClientFromResources(context)
                    launcher.launch(signInClient.signInIntent)
                },
                modifier = Modifier
                    .padding(start = 20.dp, end = 20.dp, bottom = 20.dp)
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
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = 10.dp)
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
private fun LoginScreenPreview() {
    LoginScreen(
        state = LoginState(),
        onAction = {},
        onNavigateToRegister = {},
        onNavigateToForgetPassword = {}
    )
}
