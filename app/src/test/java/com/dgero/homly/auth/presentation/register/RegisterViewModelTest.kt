package com.dgero.homly.auth.presentation.register

import com.dgero.homly.auth.domain.model.AuthError
import com.dgero.homly.auth.domain.model.User
import com.dgero.homly.auth.domain.repository.SessionRepository
import com.dgero.homly.auth.domain.repository.UserRepository
import com.dgero.homly.auth.domain.usecase.RegisterUserUseCase
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

    private fun makeViewModelWithRealUseCase(
        registerResult: Result<User> = Result.success(User(1, "alice")),
    ): RegisterViewModel {
        val useCase = RegisterUserUseCase(
            userRepository = FakeUserRepository(registerResult),
            sessionRepository = FakeSessionRepository(),
        )
        return RegisterViewModel(useCase)
    }

    @Test
    fun `empty login shows error message`() = runTest {
        val vm = makeViewModelWithRealUseCase()
        vm.onLoginChange("")
        vm.onPasswordChange("pass1234")
        vm.onRegisterClick()
        advanceUntilIdle()
        assertEquals("Login cannot be empty", vm.uiState.value.errorMessage)
    }

    @Test
    fun `login too short shows error message`() = runTest {
        val vm = makeViewModelWithRealUseCase()
        vm.onLoginChange("ab")
        vm.onPasswordChange("pass1234")
        vm.onRegisterClick()
        advanceUntilIdle()
        assertEquals("Login must be at least 3 characters", vm.uiState.value.errorMessage)
    }

    @Test
    fun `invalid login chars shows error message`() = runTest {
        val vm = makeViewModelWithRealUseCase()
        vm.onLoginChange("al!ce")
        vm.onPasswordChange("pass1234")
        vm.onRegisterClick()
        advanceUntilIdle()
        assertEquals("Login can only contain letters and digits", vm.uiState.value.errorMessage)
    }

    @Test
    fun `empty password shows error message`() = runTest {
        val vm = makeViewModelWithRealUseCase()
        vm.onLoginChange("alice")
        vm.onPasswordChange("")
        vm.onRegisterClick()
        advanceUntilIdle()
        assertEquals("Password cannot be empty", vm.uiState.value.errorMessage)
    }

    @Test
    fun `password too short shows error message`() = runTest {
        val vm = makeViewModelWithRealUseCase()
        vm.onLoginChange("alice")
        vm.onPasswordChange("abc")
        vm.onRegisterClick()
        advanceUntilIdle()
        assertEquals("Password must be at least 4 characters", vm.uiState.value.errorMessage)
    }

    @Test
    fun `duplicate account shows error message`() = runTest {
        val vm = makeViewModelWithRealUseCase(
            registerResult = Result.failure(AuthError.DuplicateAccount),
        )
        vm.onLoginChange("alice")
        vm.onPasswordChange("pass1234")
        vm.onRegisterClick()
        advanceUntilIdle()
        assertEquals("Account already exists", vm.uiState.value.errorMessage)
    }

    @Test
    fun `successful register emits navigateToHome event`() = runTest {
        val useCase = RegisterUserUseCase(
            userRepository = FakeUserRepository(Result.success(User(1, "alice"))),
            sessionRepository = FakeSessionRepository(),
        )
        val vm = RegisterViewModel(useCase)
        vm.onLoginChange("alice")
        vm.onPasswordChange("pass1234")
        vm.onRegisterClick()
        advanceUntilIdle()
        val event = vm.navigateToHome.first()
        assertNotNull(event)
    }
}
