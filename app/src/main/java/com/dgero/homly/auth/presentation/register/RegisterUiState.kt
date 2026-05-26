package com.dgero.homly.auth.presentation.register

data class RegisterUiState(
    val login: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)
