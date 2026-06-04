package com.example.mealflow.feature.auth.presentation.register

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

class RegisterViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterState())
    val state = _state.asStateFlow()

    private val _events = Channel<RegisterEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: RegisterAction) {
        when (action) {
            is RegisterAction.OnUsernameChange -> {
                _state.update { it.copy(username = action.username) }
            }
            is RegisterAction.OnEmailChange -> {
                _state.update { it.copy(email = action.email) }
            }
            is RegisterAction.OnPasswordChange -> {
                _state.update { it.copy(password = action.password) }
            }
            is RegisterAction.OnTogglePasswordVisibility -> {
                _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            }
            is RegisterAction.OnRegisterClick -> register()
        }
    }

    private fun register() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            authRepository.register(
                state.value.username,
                state.value.email,
                state.value.password
            ).onSuccess {
                _state.update { it.copy(isLoading = false) }
                _events.send(RegisterEvent.RegisterSuccess(state.value.email))
            }.onFailure { error ->
                _state.update { it.copy(isLoading = false, error = error.toUiText()) }
                _events.send(RegisterEvent.Error(error.toUiText()))
            }
        }
    }
}
