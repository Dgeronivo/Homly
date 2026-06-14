package com.dgero.homly.auth.domain.validation

import com.dgero.homly.auth.domain.model.AuthError

class LoginValidator {

    private val validCharsRegex = Regex("^[a-zA-Z0-9]+$")

    fun validate(login: String): AuthError? {
        val trimmed = login.trim()
        return when {
            trimmed.isEmpty() -> AuthError.EmptyLogin
            trimmed.length < 3 -> AuthError.LoginTooShort
            !trimmed.matches(validCharsRegex) -> AuthError.InvalidLoginChars
            else -> null
        }
    }

    fun hasInvalidChars(login: String): Boolean {
        val trimmed = login.trim()
        return trimmed.isNotEmpty() && !trimmed.matches(validCharsRegex)
    }
}
