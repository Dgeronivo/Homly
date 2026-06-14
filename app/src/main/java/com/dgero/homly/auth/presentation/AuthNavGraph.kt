package com.dgero.homly.auth.presentation

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.dgero.homly.auth.domain.repository.SessionRepository
import com.dgero.homly.auth.domain.repository.UserRepository
import com.dgero.homly.auth.domain.usecase.LoginUserUseCase
import com.dgero.homly.auth.domain.usecase.RegisterUserUseCase
import com.dgero.homly.auth.domain.validation.LoginValidator
import com.dgero.homly.auth.domain.validation.PasswordValidator
import com.dgero.homly.auth.presentation.login.LoginScreen
import com.dgero.homly.auth.presentation.login.LoginViewModel
import com.dgero.homly.auth.presentation.register.RegisterScreen
import com.dgero.homly.auth.presentation.register.RegisterViewModel

fun NavGraphBuilder.authGraph(
    navController: NavHostController,
    userRepository: UserRepository,
    sessionRepository: SessionRepository,
) {
    navigation(startDestination = "auth/login", route = "auth") {
        composable("auth/login") {
            val vm: LoginViewModel = viewModel(
                factory = LoginViewModel.Factory(
                    loginUserUseCase = LoginUserUseCase(userRepository, sessionRepository),
                    loginValidator = LoginValidator(),
                    passwordValidator = PasswordValidator(),
                )
            )
            LoginScreen(
                viewModel = vm,
                onNavigateToRegister = { navController.navigate("auth/register") },
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("auth") { inclusive = true }
                    }
                },
            )
        }
        composable("auth/register") {
            val vm: RegisterViewModel = viewModel(
                factory = RegisterViewModel.Factory(
                    registerUserUseCase = RegisterUserUseCase(userRepository, sessionRepository),
                    loginValidator = LoginValidator(),
                    passwordValidator = PasswordValidator(),
                )
            )
            RegisterScreen(
                viewModel = vm,
                onNavigateToLogin = { navController.popBackStack() },
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("auth") { inclusive = true }
                    }
                },
            )
        }
    }
}
