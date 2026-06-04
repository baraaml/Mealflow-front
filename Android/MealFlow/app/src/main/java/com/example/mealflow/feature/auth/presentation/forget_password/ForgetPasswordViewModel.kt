package com.example.mealflow.feature.auth.presentation.forget_password

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

class ForgetPasswordViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ForgetPasswordState())
    val state = _state.asStateFlow()

    private val _events = Channel<ForgetPasswordEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: ForgetPasswordAction) {
        when (action) {
            is ForgetPasswordAction.OnEmailChange -> {
                _state.update { it.copy(email = action.email) }
            }
            is ForgetPasswordAction.OnSubmitClick -> submit()
        }
    }

    private fun submit() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            authRepository.forgetPassword(state.value.email)
                .onSuccess {
                    _state.update { it.copy(isLoading = false) }
                    _events.send(ForgetPasswordEvent.Success(state.value.email))
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false, error = error.toUiText()) }
                    _events.send(ForgetPasswordEvent.Error(error.toUiText()))
                }
        }
    }
}
