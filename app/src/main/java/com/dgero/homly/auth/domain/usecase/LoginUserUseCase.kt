package com.dgero.homly.auth.domain.usecase

import com.dgero.homly.auth.domain.model.AuthError
import com.dgero.homly.auth.domain.model.User
import com.dgero.homly.auth.domain.repository.SessionRepository
import com.dgero.homly.auth.domain.repository.UserRepository

class LoginUserUseCase(
    private val userRepository: UserRepository,
    private val sessionRepository: SessionRepository,
) {
    suspend operator fun invoke(login: String, password: String): Result<User> {
        val error = validate(login, password)
        if (error != null) return Result.failure(error)
        val normalizedLogin = login.trim().lowercase()
        return userRepository.login(normalizedLogin, password)
            .onSuccess { user -> sessionRepository.setSession(user.id) }
    }

    private fun validate(login: String, password: String): AuthError? {
        val trimmed = login.trim()
        return when {
            trimmed.isEmpty() -> AuthError.EmptyLogin
            trimmed.length < 3 -> AuthError.LoginTooShort
            !trimmed.matches(Regex("^[a-zA-Z0-9]+\$")) -> AuthError.InvalidLoginChars
            password.isEmpty() -> AuthError.EmptyPassword
            password.length < 4 -> AuthError.PasswordTooShort
            !password.matches(Regex("""^[a-zA-Z0-9!@#${'$'}%^&*()\-_=+\[\]{};:'",.<>?/\\| ]+${'$'}""")) -> AuthError.InvalidPasswordChars
            else -> null
        }
    }
}
