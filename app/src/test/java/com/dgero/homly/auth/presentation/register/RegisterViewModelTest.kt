package com.dgero.homly.auth.presentation.register

import com.dgero.homly.auth.domain.model.AuthError
import com.dgero.homly.auth.domain.model.User
import com.dgero.homly.auth.domain.repository.SessionRepository
import com.dgero.homly.auth.domain.repository.UserRepository
import com.dgero.homly.auth.domain.usecase.RegisterUserUseCase
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
    private val registerResult: Result<User> = Result.success(User(1, "alice")),
) : UserRepository {
    override suspend fun register(login: String, password: String): Result<User> = registerResult

    override suspend fun login(login: String, password: String): Result<User> =
        Result.success(User(1, login))

    override suspend fun getUserById(id: Long): User? = null
}

private class FakeSessionRepository : SessionRepository {
    override val currentUserId: Flow<Long?> = flowOf(null)
    override suspend fun setSession(userId: Long) {}
    override suspend fun clear() {}
}

// ---------------------------------------------------------------------------
// Tests
// ---------------------------------------------------------------------------

@OptIn(ExperimentalCoroutinesApi::class)
class RegisterViewModelTest {

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
        registerResult: Result<User> = Result.success(User(1, "alice")),
    ): RegisterViewModel {
        val useCase = RegisterUserUseCase(
            userRepository = FakeUserRepository(registerResult),
            sessionRepository = FakeSessionRepository(),
        )
        return RegisterViewModel(useCase, LoginValidator(), PasswordValidator())
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
        vm.onLoginChange("alice")
        assertNull(vm.uiState.value.loginError)
    }

    @Test
    fun `typing invalid password chars sets passwordError immediately`() = runTest {
        val vm = makeViewModel()
        vm.onPasswordChange("пароль")
        assertEquals("Password contains invalid characters", vm.uiState.value.passwordError)
    }

    // --- Submit validation ---

    @Test
    fun `empty login on submit sets loginError`() = runTest {
        val vm = makeViewModel()
        vm.onLoginChange("")
        vm.onPasswordChange("pass1234")
        vm.onRegisterClick()
        advanceUntilIdle()
        assertEquals("Login cannot be empty", vm.uiState.value.loginError)
    }

    @Test
    fun `login too short on submit sets loginError`() = runTest {
        val vm = makeViewModel()
        vm.onLoginChange("ab")
        vm.onPasswordChange("pass1234")
        vm.onRegisterClick()
        advanceUntilIdle()
        assertEquals("Login must be at least 3 characters", vm.uiState.value.loginError)
    }

    @Test
    fun `invalid login chars on submit sets loginError`() = runTest {
        val vm = makeViewModel()
        vm.onLoginChange("al!ce")
        vm.onPasswordChange("pass1234")
        vm.onRegisterClick()
        advanceUntilIdle()
        assertEquals("Login can only contain English letters and digits", vm.uiState.value.loginError)
    }

    @Test
    fun `empty password on submit sets passwordError`() = runTest {
        val vm = makeViewModel()
        vm.onLoginChange("alice")
        vm.onPasswordChange("")
        vm.onRegisterClick()
        advanceUntilIdle()
        assertEquals("Password cannot be empty", vm.uiState.value.passwordError)
    }

    @Test
    fun `password too short on submit sets passwordError`() = runTest {
        val vm = makeViewModel()
        vm.onLoginChange("alice")
        vm.onPasswordChange("abc")
        vm.onRegisterClick()
        advanceUntilIdle()
        assertEquals("Password must be at least 4 characters", vm.uiState.value.passwordError)
    }

    @Test
    fun `duplicate account sets authError`() = runTest {
        val vm = makeViewModel(registerResult = Result.failure(AuthError.DuplicateAccount))
        vm.onLoginChange("alice")
        vm.onPasswordChange("pass1234")
        vm.onRegisterClick()
        advanceUntilIdle()
        assertEquals("Account already exists", vm.uiState.value.authError)
    }

    @Test
    fun `successful register emits navigateToHome event`() = runTest {
        val useCase = RegisterUserUseCase(
            userRepository = FakeUserRepository(Result.success(User(1, "alice"))),
            sessionRepository = FakeSessionRepository(),
        )
        val vm = RegisterViewModel(useCase, LoginValidator(), PasswordValidator())
        vm.onLoginChange("alice")
        vm.onPasswordChange("pass1234")
        vm.onRegisterClick()
        advanceUntilIdle()
        val event = vm.navigateToHome.first()
        assertNotNull(event)
    }
}
