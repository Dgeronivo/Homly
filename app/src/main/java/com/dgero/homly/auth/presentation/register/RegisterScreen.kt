package com.dgero.homly.auth.presentation.register

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dgero.homly.ui.theme.HomlyTheme

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.navigateToHome.collect { onNavigateToHome() }
    }

    RegisterContent(
        uiState = uiState,
        onLoginChange = viewModel::onLoginChange,
        onPasswordChange = viewModel::onPasswordChange,
        onRegisterClick = viewModel::onRegisterClick,
        onLoginClick = onNavigateToLogin,
    )
}

@Composable
private fun RegisterContent(
    uiState: RegisterUiState,
    onLoginChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRegisterClick: () -> Unit,
    onLoginClick: () -> Unit,
) {
    val isFormValid = uiState.login.trim().length >= 3
        && uiState.password.length >= 4
        && uiState.loginError == null
        && uiState.passwordError == null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = "Create Account", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = uiState.login,
            onValueChange = onLoginChange,
            label = { Text("Login") },
            singleLine = true,
            isError = uiState.loginError != null,
            supportingText = {
                Text(uiState.loginError ?: "Only English letters and digits, min 3 characters")
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading,
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = uiState.password,
            onValueChange = onPasswordChange,
            label = { Text("Password") },
            singleLine = true,
            isError = uiState.passwordError != null,
            supportingText = {
                Text(uiState.passwordError ?: "Min 4 characters, letters, digits and special characters")
            },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { if (isFormValid) onRegisterClick() }),
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading,
        )
        if (uiState.authError != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = uiState.authError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Spacer(Modifier.height(20.dp))
        if (uiState.isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = onRegisterClick,
                enabled = isFormValid,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Register")
            }
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onLoginClick, modifier = Modifier.fillMaxWidth()) {
                Text("Login")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RegisterContentPreview() {
    HomlyTheme {
        RegisterContent(
            uiState = RegisterUiState(),
            onLoginChange = {},
            onPasswordChange = {},
            onRegisterClick = {},
            onLoginClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RegisterContentWithErrorPreview() {
    HomlyTheme {
        RegisterContent(
            uiState = RegisterUiState(
                login = "кирилиця",
                loginError = "Login can only contain letters and digits",
            ),
            onLoginChange = {},
            onPasswordChange = {},
            onRegisterClick = {},
            onLoginClick = {},
        )
    }
}
