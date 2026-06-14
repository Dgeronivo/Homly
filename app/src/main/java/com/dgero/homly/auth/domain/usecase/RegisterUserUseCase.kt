package com.dgero.homly.auth.domain.usecase

import com.dgero.homly.auth.domain.model.User
import com.dgero.homly.auth.domain.repository.SessionRepository
import com.dgero.homly.auth.domain.repository.UserRepository
import com.dgero.homly.auth.domain.validation.LoginValidator
import com.dgero.homly.auth.domain.validation.PasswordValidator

class RegisterUserUseCase(
    private val userRepository: UserRepository,
    private val sessionRepository: SessionRepository,
    private val loginValidator: LoginValidator = LoginValidator(),
    private val passwordValidator: PasswordValidator = PasswordValidator(),
) {
    suspend operator fun invoke(login: String, password: String): Result<User> {
        loginValidator.validate(login)?.let { return Result.failure(it) }
        passwordValidator.validate(password)?.let { return Result.failure(it) }
        val normalizedLogin = login.trim().lowercase()
        return userRepository.register(normalizedLogin, password)
            .onSuccess { user -> sessionRepository.setSession(user.id) }
    }
}
