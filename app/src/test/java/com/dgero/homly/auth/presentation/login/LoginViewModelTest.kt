package com.dgero.homly.auth.presentation.login

import com.dgero.homly.auth.domain.model.AuthError
import com.dgero.homly.auth.domain.model.User
import com.dgero.homly.auth.domain.repository.SessionRepository
import com.dgero.homly.auth.domain.repository.UserRepository
import com.dgero.homly.auth.domain.usecase.LoginUserUseCase
import com.dgero.homly.auth.domain.validation.LoginValidator
import com.dgero.homly.auth.domain.validation.PasswordValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

// ---------------------------------------------------------------------------
// Fakes
// ---------------------------------------------------------------------------

private class FakeUserRepository(
    private val loginResult: Result<User> = Result.success(User(1, "alex")),
) : UserRepository {
    override suspend fun register(login: String, password: String): Result<User> =
        Result.success(User(1, login))

    override suspend fun login(login: String, password: String): Result<User> = loginResult

    override suspend fun getUserById(id: Long): User? = null
}

private class FakeSessionRepository : SessionRepository {
    override val currentUserId: Flow<Long?> = flowOf(null)
    override suspend fun setSession(userId: Long) {}
    override suspend fun clear() {}
}

private class AlwaysSuccessUserRepository(
    private val fixedResult: Result<User>,
) : UserRepository {
    override suspend fun register(login: String, password: String): Result<User> = fixedResult
    override suspend fun login(login: String, password: String): Result<User> = fixedResult
    override suspend fun getUserById(id: Long): User? = null
}

// ---------------------------------------------------------------------------
// Tests
// ---------------------------------------------------------------------------

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun makeViewModel(
        loginResult: Result<User> = Result.success(User(1, "alex")),
    ): LoginViewModel {
        val useCase = LoginUserUseCase(
            userRepository = FakeUserRepository(loginResult),
            sessionRepository = FakeSessionRepository(),
        )
        return LoginViewModel(useCase, LoginValidator(), PasswordValidator())
    }

    // --- Inline validation ---

    @Test
    fun `typing invalid login chars sets loginError immediately`() = runTest {
        val vm = makeViewModel()
        vm.onLoginChange("кирилиця")
        assertEquals("Login can only contain English letters and digits", vm.uiState.value.loginError)
    }

    @Test
    fun `typing valid login chars after invalid clears loginError`() = runTest {
        val vm = makeViewModel()
        vm.onLoginChange("кирилиця")
        vm.onLoginChange("abc")
        assertNull(vm.uiState.value.loginError)
    }

    @Test
    fun `typing short valid login does not set loginError`() = runTest {
        val vm = makeViewModel()
        vm.onLoginChange("ab")
        assertNull(vm.uiState.value.loginError)
    }

    @Test
    fun `typing invalid password chars sets passwordError immediately`() = runTest {
        val vm = makeViewModel()
        vm.onPasswordChange("пароль")
        assertEquals("Password contains invalid characters", vm.uiState.value.passwordError)
    }

    @Test
    fun `typing valid password after invalid clears passwordError`() = runTest {
        val vm = makeViewModel()
        vm.onPasswordChange("пароль")
        vm.onPasswordChange("pass1234")
        assertNull(vm.uiState.value.passwordError)
    }

    // --- Submit validation ---

    @Test
    fun `empty login on submit sets loginError`() = runTest {
        val vm = makeViewModel()
        vm.onLoginChange("")
        vm.onPasswordChange("pass1234")
        vm.onLoginClick()
        advanceUntilIdle()
        assertEquals("Login cannot be empty", vm.uiState.value.loginError)
    }

    @Test
    fun `login too short on submit sets loginError`() = runTest {
        val vm = makeViewModel()
        vm.onLoginChange("ab")
        vm.onPasswordChange("pass1234")
        vm.onLoginClick()
        advanceUntilIdle()
        assertEquals("Login must be at least 3 characters", vm.uiState.value.loginError)
    }

    @Test
    fun `invalid login chars on submit sets loginError`() = runTest {
        val vm = makeViewModel()
        vm.onLoginChange("al!ce")
        vm.onPasswordChange("pass1234")
        vm.onLoginClick()
        advanceUntilIdle()
        assertEquals("Login can only contain English letters and digits", vm.uiState.value.loginError)
    }

    @Test
    fun `empty password on submit sets passwordError`() = runTest {
        val vm = makeViewModel()
        vm.onLoginChange("alice")
        vm.onPasswordChange("")
        vm.onLoginClick()
        advanceUntilIdle()
        assertEquals("Password cannot be empty", vm.uiState.value.passwordError)
    }

    @Test
    fun `password too short on submit sets passwordError`() = runTest {
        val vm = makeViewModel()
        vm.onLoginChange("alice")
        vm.onPasswordChange("abc")
        vm.onLoginClick()
        advanceUntilIdle()
        assertEquals("Password must be at least 4 characters", vm.uiState.value.passwordError)
    }

    @Test
    fun `invalid credentials sets authError`() = runTest {
        val vm = makeViewModel(loginResult = Result.failure(AuthError.InvalidCredentials))
        vm.onLoginChange("alice")
        vm.onPasswordChange("pass1234")
        vm.onLoginClick()
        advanceUntilIdle()
        assertEquals("Wrong login or password", vm.uiState.value.authError)
    }

    @Test
    fun `successful login emits navigateToHome event`() = runTest {
        val useCase = LoginUserUseCase(
            userRepository = AlwaysSuccessUserRepository(Result.success(User(1, "alex"))),
            sessionRepository = FakeSessionRepository(),
        )
        val vm = LoginViewModel(useCase, LoginValidator(), PasswordValidator())
        vm.onLoginChange("alex")
        vm.onPasswordChange("pass1234")
        vm.onLoginClick()
        advanceUntilIdle()
        val event = vm.navigateToHome.first()
        assertNotNull(event)
    }
}
