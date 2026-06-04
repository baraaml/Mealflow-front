package com.example.mealflow.feature.auth.presentation.otp

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mealflow.R
import com.example.mealflow.core.presentation.util.ObserveAsEvents
import org.koin.androidx.compose.koinViewModel

@Composable
fun OtpRoot(
    onVerificationSuccess: () -> Unit,
    viewModel: OtpViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is OtpEvent.VerificationSuccess -> onVerificationSuccess()
            is OtpEvent.Error -> {
                // Show snackbar
            }
        }
    }

    OtpScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
fun OtpScreen(
    state: OtpState,
    onAction: (OtpAction) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.HeaderOtp),
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(text = stringResource(id = R.string.check_email), fontSize = 20.sp)
            
            Spacer(modifier = Modifier.height(16.dp))

            OTPInputField(onOtpEntered = { onAction(OtpAction.OnOtpChange(it)) })

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                modifier = Modifier
                    .width(150.dp)
                    .padding(20.dp)
                    .height(50.dp)
                    .border(2.dp, Color.Black, RoundedCornerShape(50.dp)),
                shape = RoundedCornerShape(50.dp),
                onClick = { onAction(OtpAction.OnVerifyClick) },
                colors = ButtonDefaults.buttonColors(
                    contentColor = Color.White,
                    disabledContainerColor = Color.Gray.copy(alpha = 0.5f),
                    disabledContentColor = Color.White.copy(alpha = 0.7f)
                ),
                enabled = !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = stringResource(id = R.string.Verification),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (state.canResend) {
                Text(
                    text = "Resend OTP",
                    style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp),
                    color = Color.Blue,
                    modifier = Modifier.clickable { onAction(OtpAction.OnResendClick) }
                )
            } else {
                Text(
                    text = "Resend after: ${state.timer} seconds",
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun OTPInputField(
    otpLength: Int = 6,
    onOtpEntered: (String) -> Unit
) {
    var otpValues by remember { mutableStateOf(List(otpLength) { "" }) }
    val focusRequesters = List(otpLength) { remember { FocusRequester() } }

    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        otpValues.forEachIndexed { index, value ->
            OutlinedTextField(
                value = value,
                onValueChange = { newValue ->
                    if (index == 0 && newValue.length > 1 && newValue.all { it.isDigit() }) {
                        val pastedOtp = newValue.take(otpLength)
                        val newOtpValues = List(otpLength) { i ->
                            if (i < pastedOtp.length) pastedOtp[i].toString() else ""
                        }
                        otpValues = newOtpValues
                        if (pastedOtp.length < otpLength) {
                            focusRequesters[pastedOtp.length].requestFocus()
                        }
                        if (otpValues.joinToString("").length == otpLength) {
                            onOtpEntered(otpValues.joinToString(""))
                        }
                    } else if (newValue.length <= 1 && newValue.all { it.isDigit() }) {
                        val newOtpValues = otpValues.toMutableList()
                        newOtpValues[index] = newValue
                        otpValues = newOtpValues
                        if (newValue.isNotEmpty() && index < otpLength - 1) {
                            focusRequesters[index + 1].requestFocus()
                        }
                        if (otpValues.joinToString("").length == otpLength) {
                            onOtpEntered(otpValues.joinToString(""))
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .width(50.dp)
                    .height(55.dp)
                    .focusRequester(focusRequesters[index])
                    .padding(4.dp)
                    .onKeyEvent { keyEvent ->
                        if (keyEvent.key == Key.Backspace && value.isEmpty() && index > 0) {
                            focusRequesters[index - 1].requestFocus()
                            true
                        } else {
                            false
                        }
                    },
                textStyle = TextStyle(
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun OtpScreenPreview() {
    OtpScreen(
        state = OtpState(),
        onAction = {}
    )
}
