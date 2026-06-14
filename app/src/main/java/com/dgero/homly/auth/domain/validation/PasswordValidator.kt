package com.dgero.homly.auth.domain.validation

import com.dgero.homly.auth.domain.model.AuthError

class PasswordValidator {

    private val validCharsRegex = Regex("""^[a-zA-Z0-9!@#${'$'}%^&*()\-_=+\[\]{};:'",.<>?/\\| ]+$""")

    fun validate(password: String): AuthError? {
        return when {
            password.isEmpty() -> AuthError.EmptyPassword
            password.length < 4 -> AuthError.PasswordTooShort
            !password.matches(validCharsRegex) -> AuthError.InvalidPasswordChars
            else -> null
        }
    }

    fun hasInvalidChars(password: String): Boolean {
        return password.isNotEmpty() && !password.matches(validCharsRegex)
    }
}
