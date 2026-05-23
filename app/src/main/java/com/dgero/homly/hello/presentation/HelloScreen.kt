package com.dgero.homly.hello.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun HelloScreen(
    modifier: Modifier = Modifier,
    viewModel: HelloViewModel = viewModel()
) {
    val nameInput by viewModel.nameInput.collectAsStateWithLifecycle()
    val submittedName by viewModel.submittedName.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = nameInput,
            onValueChange = viewModel::onNameChange,
            label = { Text("Enter your name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = viewModel::onSubmit,
            enabled = nameInput.isNotBlank()
        ) {
            Text("Submit")
        }
        if (submittedName != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Hello $submittedName!",
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}
