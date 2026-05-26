package com.dgero.homly.auth.presentation.login

import com.dgero.homly.auth.domain.model.User
import com.dgero.homly.auth.domain.repository.SessionRepository
import com.dgero.homly.auth.domain.repository.UserRepository
import com.dgero.homly.auth.domain.usecase.LoginUserUseCase
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

/**
 * A UserRepository that always returns [fixedResult] from login, bypassing any real logic.
 * Used together with LoginUserUseCase to stub the outcome after validation passes.
 */
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

    private fun makeViewModelWithRealUseCase(
        loginResult: Result<User> = Result.success(User(1, "alex")),
    ): LoginViewModel {
        val useCase = LoginUserUseCase(
            userRepository = FakeUserRepository(loginResult),
            sessionRepository = FakeSessionRepository(),
        )
        return LoginViewModel(useCase)
    }

    @Test
    fun `empty login shows error message`() = runTest {
        val vm = makeViewModelWithRealUseCase()
        vm.onLoginChange("")
        vm.onPasswordChange("pass1234")
        vm.onLoginClick()
        advanceUntilIdle()
        assertEquals("Login cannot be empty", vm.uiState.value.errorMessage)
    }

    @Test
    fun `login too short shows error message`() = runTest {
        val vm = makeViewModelWithRealUseCase()
        vm.onLoginChange("ab")
        vm.onPasswordChange("pass1234")
        vm.onLoginClick()
        advanceUntilIdle()
        assertEquals("Login must be at least 3 characters", vm.uiState.value.errorMessage)
    }

    @Test
    fun `invalid login chars shows error message`() = runTest {
        val vm = makeViewModelWithRealUseCase()
        vm.onLoginChange("al!ce")
        vm.onPasswordChange("pass1234")
        vm.onLoginClick()
        advanceUntilIdle()
        assertEquals("Login can only contain letters and digits", vm.uiState.value.errorMessage)
    }

    @Test
    fun `empty password shows error message`() = runTest {
        val vm = makeViewModelWithRealUseCase()
        vm.onLoginChange("alice")
        vm.onPasswordChange("")
        vm.onLoginClick()
        advanceUntilIdle()
        assertEquals("Password cannot be empty", vm.uiState.value.errorMessage)
    }

    @Test
    fun `password too short shows error message`() = runTest {
        val vm = makeViewModelWithRealUseCase()
        vm.onLoginChange("alice")
        vm.onPasswordChange("abc")
        vm.onLoginClick()
        advanceUntilIdle()
        assertEquals("Password must be at least 4 characters", vm.uiState.value.errorMessage)
    }

    @Test
    fun `successful login emits navigateToHome event`() = runTest {
        val useCase = LoginUserUseCase(
            userRepository = AlwaysSuccessUserRepository(Result.success(User(1, "alex"))),
            sessionRepository = FakeSessionRepository(),
        )
        val vm = LoginViewModel(useCase)
        vm.onLoginChange("alex")
        vm.onPasswordChange("pass1234")
        vm.onLoginClick()
        advanceUntilIdle()
        val event = vm.navigateToHome.first()
        assertNotNull(event)
    }
}
