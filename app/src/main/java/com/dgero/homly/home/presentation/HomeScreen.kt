package com.dgero.homly.home.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dgero.homly.ui.theme.HomlyTheme

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onOpenShoppingList: () -> Unit,
    onLogout: () -> Unit,
) {
    val login by viewModel.login.collectAsStateWithLifecycle()

    HomeContent(
        login = login,
        onOpenShoppingList = onOpenShoppingList,
        onLogout = { viewModel.onLogout(onLogout) },
    )
}

@Composable
private fun HomeContent(
    login: String,
    onOpenShoppingList: () -> Unit,
    onLogout: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = "Hello $login", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onOpenShoppingList) {
            Text("Shopping list")
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = onLogout) {
            Text("Log out")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeContentPreview() {
    HomlyTheme {
        HomeContent(login = "alex", onOpenShoppingList = {}, onLogout = {})
    }
}
