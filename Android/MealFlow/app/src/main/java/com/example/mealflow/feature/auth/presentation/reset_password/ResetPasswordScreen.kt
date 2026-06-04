package com.example.mealflow.feature.auth.presentation.reset_password

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mealflow.R
import com.example.mealflow.buttons.BackButton
import com.example.mealflow.core.presentation.util.ObserveAsEvents
import com.example.mealflow.utils.Validator
import org.koin.androidx.compose.koinViewModel

@Composable
fun ResetPasswordRoot(
    onSuccess: () -> Unit,
    onBack: () -> Unit,
    viewModel: ResetPasswordViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is ResetPasswordEvent.Success -> onSuccess()
            is ResetPasswordEvent.Error -> {
                // Show snackbar
            }
        }
    }

    ResetPasswordScreen(
        state = state,
        onAction = viewModel::onAction,
        onBack = onBack
    )
}

@Composable
fun ResetPasswordScreen(
    state: ResetPasswordState,
    onAction: (ResetPasswordAction) -> Unit,
    onBack: () -> Unit
) {
    val passwordError = Validator.validatePassword(state.password)
    val passwordsMatch = state.password == state.confirmPassword && state.password.isNotEmpty()
    val isFormValid = state.password.isNotEmpty() && passwordError == null && passwordsMatch

    Column {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            BackButton(onClick = onBack)
        }

        Text(
            text = stringResource(id = R.string.ForgottenPassword),
            modifier = Modifier.padding(start = 20.dp, top = 80.dp, end = 20.dp),
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = state.password,
            onValueChange = { onAction(ResetPasswordAction.OnPasswordChange(it)) },
            label = { Text(stringResource(id = R.string.EnterPassword)) },
            trailingIcon = {
                Icon(
                    painter = painterResource(
                        id = if (state.isPasswordVisible) R.drawable.eye_view_icon else R.drawable.eye_closed_icon
                    ),
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { onAction(ResetPasswordAction.OnTogglePasswordVisibility) }
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (passwordError != null) Color.Red else Color.Blue,
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

        OutlinedTextField(
            value = state.confirmPassword,
            onValueChange = { onAction(ResetPasswordAction.OnConfirmPasswordChange(it)) },
            label = { Text("Re-Enter your password") },
            trailingIcon = {
                Icon(
                    painter = painterResource(
                        id = if (state.isPasswordVisible) R.drawable.eye_view_icon else R.drawable.eye_closed_icon
                    ),
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { onAction(ResetPasswordAction.OnTogglePasswordVisibility) }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, top = 20.dp, end = 20.dp),
            singleLine = true,
            textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground),
            visualTransformation = if (state.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password)
        )

        if (state.confirmPassword.isNotEmpty() && !passwordsMatch) {
            Text(
                text = "Passwords do not match",
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 5.dp),
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp
            )
        }

        Button(
            onClick = { onAction(ResetPasswordAction.OnSubmitClick) },
            modifier = Modifier
                .padding(start = 20.dp, end = 20.dp, top = 50.dp)
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
            enabled = isFormValid && !state.isLoading
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = stringResource(id = R.string.Submit),
                    color = Color.White
                )
            }
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun ResetPasswordScreenPreview() {
    ResetPasswordScreen(
        state = ResetPasswordState(),
        onAction = {},
        onBack = {}
    )
}
