package com.dgero.homly.auth.presentation.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dgero.homly.auth.domain.model.AuthError
import com.dgero.homly.auth.domain.usecase.RegisterUserUseCase
import com.dgero.homly.auth.domain.validation.LoginValidator
import com.dgero.homly.auth.domain.validation.PasswordValidator
import com.dgero.homly.auth.presentation.login.authErrorMessage
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val registerUserUseCase: RegisterUserUseCase,
    private val loginValidator: LoginValidator,
    private val passwordValidator: PasswordValidator,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private val _navigateToHome = Channel<Unit>(Channel.BUFFERED)
    val navigateToHome = _navigateToHome.receiveAsFlow()

    fun onLoginChange(value: String) {
        val error = if (loginValidator.hasInvalidChars(value)) "Login can only contain English letters and digits" else null
        _uiState.update { it.copy(login = value, loginError = error, authError = null) }
    }

    fun onPasswordChange(value: String) {
        val error = if (passwordValidator.hasInvalidChars(value)) "Password contains invalid characters" else null
        _uiState.update { it.copy(password = value, passwordError = error, authError = null) }
    }

    fun onRegisterClick() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, authError = null) }
            val result = registerUserUseCase(_uiState.value.login, _uiState.value.password)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false) }
                    _navigateToHome.send(Unit)
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false) }
                    when (e) {
                        is AuthError.EmptyLogin, is AuthError.LoginTooShort, is AuthError.InvalidLoginChars ->
                            _uiState.update { it.copy(loginError = authErrorMessage(e)) }
                        is AuthError.EmptyPassword, is AuthError.PasswordTooShort, is AuthError.InvalidPasswordChars ->
                            _uiState.update { it.copy(passwordError = authErrorMessage(e)) }
                        else ->
                            _uiState.update { it.copy(authError = authErrorMessage(e)) }
                    }
                },
            )
        }
    }

    class Factory(
        private val registerUserUseCase: RegisterUserUseCase,
        private val loginValidator: LoginValidator,
        private val passwordValidator: PasswordValidator,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            RegisterViewModel(registerUserUseCase, loginValidator, passwordValidator) as T
    }
}
