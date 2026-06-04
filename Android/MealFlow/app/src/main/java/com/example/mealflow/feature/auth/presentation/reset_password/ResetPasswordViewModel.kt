package com.example.mealflow.feature.auth.presentation.reset_password

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mealflow.core.domain.util.onFailure
import com.example.mealflow.core.domain.util.onSuccess
import com.example.mealflow.core.presentation.util.toUiText
import com.example.mealflow.feature.auth.domain.AuthRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ResetPasswordViewModel(
    private val authRepository: AuthRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(ResetPasswordState(
        token = savedStateHandle["token"] ?: ""
    ))
    val state = _state.asStateFlow()

    private val _events = Channel<ResetPasswordEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: ResetPasswordAction) {
        when (action) {
            is ResetPasswordAction.OnPasswordChange -> {
                _state.update { it.copy(password = action.password) }
            }
            is ResetPasswordAction.OnConfirmPasswordChange -> {
                _state.update { it.copy(confirmPassword = action.confirmPassword) }
            }
            is ResetPasswordAction.OnTogglePasswordVisibility -> {
                _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            }
            is ResetPasswordAction.OnSubmitClick -> submit()
        }
    }

    private fun submit() {
        if (state.value.password != state.value.confirmPassword) {
            // Handle password mismatch
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            authRepository.resetPassword(state.value.token, state.value.password)
                .onSuccess {
                    _state.update { it.copy(isLoading = false) }
                    _events.send(ResetPasswordEvent.Success)
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false, error = error.toUiText()) }
                    _events.send(ResetPasswordEvent.Error(error.toUiText()))
                }
        }
    }
}
