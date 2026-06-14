package com.dgero.homly.auth.presentation.login

data class LoginUiState(
    val login: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val loginError: String? = null,
    val passwordError: String? = null,
    val authError: String? = null,
)
