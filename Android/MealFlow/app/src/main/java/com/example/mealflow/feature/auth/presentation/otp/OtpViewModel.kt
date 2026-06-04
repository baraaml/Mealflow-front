package com.example.mealflow.feature.auth.presentation.otp

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mealflow.core.domain.util.onFailure
import com.example.mealflow.core.domain.util.onSuccess
import com.example.mealflow.core.presentation.util.toUiText
import com.example.mealflow.feature.auth.domain.AuthRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OtpViewModel(
    private val authRepository: AuthRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(OtpState(
        email = savedStateHandle["email"] ?: ""
    ))
    val state = _state.asStateFlow()

    private val _events = Channel<OtpEvent>()
    val events = _events.receiveAsFlow()

    init {
        startTimer()
    }

    fun onAction(action: OtpAction) {
        when (action) {
            is OtpAction.OnOtpChange -> {
                _state.update { it.copy(otp = action.otp) }
            }
            is OtpAction.OnVerifyClick -> verifyOtp()
            is OtpAction.OnResendClick -> resendOtp()
        }
    }

    private fun verifyOtp() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            authRepository.verifyOtp(state.value.email, state.value.otp)
                .onSuccess {
                    _state.update { it.copy(isLoading = false) }
                    _events.send(OtpEvent.VerificationSuccess)
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false, error = error.toUiText()) }
                    _events.send(OtpEvent.Error(error.toUiText()))
                }
        }
    }

    private fun resendOtp() {
        viewModelScope.launch {
            _state.update { it.copy(timer = 60, canResend = false, error = null) }
            startTimer()
            authRepository.resendOtp(state.value.email)
                .onFailure { error ->
                    _events.send(OtpEvent.Error(error.toUiText()))
                }
        }
    }

    private fun startTimer() {
        viewModelScope.launch {
            while (_state.value.timer > 0) {
                delay(1000L)
                _state.update { it.copy(timer = it.timer - 1) }
            }
            _state.update { it.copy(canResend = true) }
        }
    }
}
