package com.dgero.homly.auth.presentation.login

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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dgero.homly.R
import com.dgero.homly.ui.theme.HomlyTheme

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onNavigateToRegister: () -> Unit,
    onNavigateToHome: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.navigateToHome.collect { onNavigateToHome() }
    }

    LoginContent(
        uiState = uiState,
        onLoginChange = viewModel::onLoginChange,
        onPasswordChange = viewModel::onPasswordChange,
        onLoginClick = viewModel::onLoginClick,
        onRegisterClick = onNavigateToRegister,
    )
}

@Composable
private fun LoginContent(
    uiState: LoginUiState,
    onLoginChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
) {
    val isFormValid = uiState.login.trim().length >= 3
        && uiState.password.length >= 4
        && uiState.loginError == null
        && uiState.passwordError == null

    var isPasswordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = stringResource(R.string.sign_in), style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = uiState.login,
            onValueChange = onLoginChange,
            label = { Text(stringResource(R.string.login)) },
            singleLine = true,
            isError = uiState.loginError != null,
            supportingText = {
                Text(uiState.loginError ?: stringResource(R.string.login_hint))
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading,
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = uiState.password,
            onValueChange = onPasswordChange,
            label = { Text(stringResource(R.string.password)) },
            singleLine = true,
            isError = uiState.passwordError != null,
            supportingText = {
                Text(uiState.passwordError ?: stringResource(R.string.password_hint))
            },
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Text(if (isPasswordVisible) "🙈" else "👁")
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { if (isFormValid) onLoginClick() }),
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
                onClick = onLoginClick,
                enabled = isFormValid,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.login))
            }
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onRegisterClick, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.register))
            }
        }
    }
}

@Preview(showBackground = true, locale = "uk")
@Composable
private fun LoginContentPreview() {
    HomlyTheme {
        LoginContent(
            uiState = LoginUiState(),
            onLoginChange = {},
            onPasswordChange = {},
            onLoginClick = {},
            onRegisterClick = {},
        )
    }
}

@Preview(showBackground = true, locale = "uk")
@Composable
private fun LoginContentWithErrorPreview() {
    HomlyTheme {
        LoginContent(
            uiState = LoginUiState(
                login = "кирилиця",
                loginError = stringResource(R.string.login_error),
            ),
            onLoginChange = {},
            onPasswordChange = {},
            onLoginClick = {},
            onRegisterClick = {},
        )
    }
}
