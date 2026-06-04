package com.example.mealflow.feature.auth.presentation.forget_password

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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
fun ForgetPasswordRoot(
    onSuccess: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: ForgetPasswordViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is ForgetPasswordEvent.Success -> onSuccess(event.email)
            is ForgetPasswordEvent.Error -> {
                // Show snackbar
            }
        }
    }

    ForgetPasswordScreen(
        state = state,
        onAction = viewModel::onAction,
        onBack = onBack
    )
}

@Composable
fun ForgetPasswordScreen(
    state: ForgetPasswordState,
    onAction: (ForgetPasswordAction) -> Unit,
    onBack: () -> Unit
) {
    val emailError = Validator.validateEmail(state.email)

    Box(modifier = Modifier.padding(bottom = 24.dp)) {
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
                modifier = Modifier.padding(start = 20.dp, top = 50.dp, bottom = 10.dp),
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily(Font(R.font.sf_pro_rounded_heavy)),
                color = MaterialTheme.colorScheme.onBackground
            )

            OutlinedTextField(
                value = state.email,
                onValueChange = { onAction(ForgetPasswordAction.OnEmailChange(it)) },
                label = {
                    Text(
                        stringResource(id = R.string.EnterEmail),
                        fontFamily = FontFamily(Font(R.font.sf_reg_ita)),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                },
                singleLine = true,
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onBackground,
                    fontFamily = FontFamily(Font(R.font.sfmed))
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (emailError != null)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    cursorColor = MaterialTheme.colorScheme.primary
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
                    fontSize = 12.sp,
                    fontFamily = FontFamily(Font(R.font.sfmed))
                )
            }

            Button(
                onClick = { onAction(ForgetPasswordAction.OnSubmitClick) },
                modifier = Modifier
                    .padding(start = 20.dp, end = 20.dp, top = 50.dp)
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
                ),
                enabled = emailError == null && !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = stringResource(id = R.string.ResetPassword),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontFamily = FontFamily(Font(R.font.sfmed))
                    )
                }
            }
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun ForgetPasswordScreenPreview() {
    ForgetPasswordScreen(
        state = ForgetPasswordState(),
        onAction = {},
        onBack = {}
    )
}
