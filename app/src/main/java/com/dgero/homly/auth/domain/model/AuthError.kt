package com.dgero.homly.auth.domain.model

sealed class AuthError : Exception() {
    object EmptyLogin : AuthError()
    object EmptyPassword : AuthError()
    object LoginTooShort : AuthError()
    object PasswordTooShort : AuthError()
    object InvalidLoginChars : AuthError()
    object InvalidPasswordChars : AuthError()
    object DuplicateAccount : AuthError()
    object InvalidCredentials : AuthError()
    data class Unknown(override val cause: Throwable) : AuthError()
}
