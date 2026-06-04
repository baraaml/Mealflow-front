package com.example.mealflow.feature.auth.presentation.quick_login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mealflow.core.domain.util.onFailure
import com.example.mealflow.core.domain.util.onSuccess
import com.example.mealflow.core.presentation.util.toUiText
import com.example.mealflow.database.token.TokenManager
import com.example.mealflow.feature.auth.domain.AuthRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class QuickLoginViewModel(
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _state = MutableStateFlow(QuickLoginState())
    val state = _state.asStateFlow()

    private val _events = Channel<QuickLoginEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: QuickLoginAction) {
        when (action) {
            is QuickLoginAction.OnQuickLoginClick -> quickLogin()
        }
    }

    private fun quickLogin() {
        val refreshToken = tokenManager.getRefreshToken()
        if (refreshToken.isNullOrEmpty()) {
            // Should not happen if we are on this screen
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            authRepository.quickLogin(refreshToken)
                .onSuccess {
                    _state.update { it.copy(isLoading = false) }
                    _events.send(QuickLoginEvent.QuickLoginSuccess)
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false, error = error.toUiText()) }
                    _events.send(QuickLoginEvent.Error(error.toUiText()))
                }
        }
    }
}
