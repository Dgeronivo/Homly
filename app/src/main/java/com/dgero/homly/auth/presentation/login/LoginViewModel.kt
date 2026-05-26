package com.dgero.homly.auth.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dgero.homly.auth.domain.model.AuthError
import com.dgero.homly.auth.domain.usecase.LoginUserUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(private val loginUserUseCase: LoginUserUseCase) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _navigateToHome = Channel<Unit>(Channel.BUFFERED)
    val navigateToHome = _navigateToHome.receiveAsFlow()

    fun onLoginChange(value: String) = _uiState.update { it.copy(login = value, errorMessage = null) }
    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value, errorMessage = null) }

    fun onLoginClick() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = loginUserUseCase(_uiState.value.login, _uiState.value.password)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false) }
                    _navigateToHome.send(Unit)
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = authErrorMessage(e)) }
                },
            )
        }
    }

    class Factory(private val loginUserUseCase: LoginUserUseCase) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            LoginViewModel(loginUserUseCase) as T
    }
}

internal fun authErrorMessage(e: Throwable): String = when (e) {
    is AuthError.EmptyLogin -> "Login cannot be empty"
    is AuthError.LoginTooShort -> "Login must be at least 3 characters"
    is AuthError.InvalidLoginChars -> "Login can only contain letters and digits"
    is AuthError.EmptyPassword -> "Password cannot be empty"
    is AuthError.PasswordTooShort -> "Password must be at least 4 characters"
    is AuthError.InvalidPasswordChars -> "Password contains invalid characters"
    is AuthError.InvalidCredentials -> "Wrong login or password"
    is AuthError.DuplicateAccount -> "Account already exists"
    else -> "Something went wrong"
}
