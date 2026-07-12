package com.dgero.homly.calendar.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dgero.homly.ui.theme.HomlyTheme
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val DATE_FORMATTER = DateTimeFormatter.ofPattern("d MMM yyyy")
private val TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm")

/**
 * Add/edit event form (SAD Flow 2, Flow 3).
 *
 * [onBack] is invoked both from the top-bar back arrow (cancel) and automatically once a save
 * completes successfully — actual navigation is wired by the caller (T15).
 */
@Composable
fun AddEditEventScreen(
    viewModel: AddEditEventViewModel,
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.saveCompleted) {
        if (uiState.saveCompleted) onBack()
    }

    AddEditEventContent(
        uiState = uiState,
        onBack = onBack,
        onTitleChange = viewModel::onTitleChange,
        onDateChange = viewModel::onDateChange,
        onAllDayToggle = viewModel::onAllDayToggle,
        onStartTimeChange = viewModel::onStartTimeChange,
        onEndTimeChange = viewModel::onEndTimeChange,
        onSave = viewModel::onSave,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditEventContent(
    uiState: AddEditEventUiState,
    onBack: () -> Unit,
    onTitleChange: (String) -> Unit,
    onDateChange: (LocalDate) -> Unit,
    onAllDayToggle: (Boolean) -> Unit,
    onStartTimeChange: (LocalTime) -> Unit,
    onEndTimeChange: (LocalTime) -> Unit,
    onSave: () -> Unit,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEditMode) "Edit event" else "Add event") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("‹", style = MaterialTheme.typography.headlineMedium)
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
        ) {
            OutlinedTextField(
                value = uiState.title,
                onValueChange = onTitleChange,
                label = { Text("Title") },
                isError = uiState.titleError != null,
                supportingText = { uiState.titleError?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))

            OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
                Text("Date: ${uiState.date.format(DATE_FORMATTER)}")
            }
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("All day", style = MaterialTheme.typography.bodyLarge)
                Switch(checked = uiState.isAllDay, onCheckedChange = onAllDayToggle)
            }

            if (!uiState.isAllDay) {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = { showStartTimePicker = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Start: ${uiState.startTime?.format(TIME_FORMATTER) ?: "--:--"}")
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = { showEndTimePicker = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("End: ${uiState.endTime?.format(TIME_FORMATTER) ?: "--:--"}")
                }
                if (uiState.timeError != null) {
                    Text(
                        text = uiState.timeError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }

            uiState.formError?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onSave,
                enabled = !uiState.isSaving,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Save")
            }
        }
    }

    if (showDatePicker) {
        EventDatePickerDialog(
            initialDate = uiState.date,
            onConfirm = {
                onDateChange(it)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false },
        )
    }
    if (showStartTimePicker) {
        EventTimePickerDialog(
            initialTime = uiState.startTime ?: LocalTime.of(9, 0),
            onConfirm = {
                onStartTimeChange(it)
                showStartTimePicker = false
            },
            onDismiss = { showStartTimePicker = false },
        )
    }
    if (showEndTimePicker) {
        EventTimePickerDialog(
            initialTime = uiState.endTime ?: LocalTime.of(10, 0),
            onConfirm = {
                onEndTimeChange(it)
                showEndTimePicker = false
            },
            onDismiss = { showEndTimePicker = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventDatePickerDialog(
    initialDate: LocalDate,
    onConfirm: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    val state = rememberDatePickerState(initialSelectedDateMillis = initialDate.toEpochUtcMillis())

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val millis = state.selectedDateMillis
                if (millis != null) onConfirm(millis.toLocalDateUtc()) else onDismiss()
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    ) {
        DatePicker(state = state)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventTimePickerDialog(
    initialTime: LocalTime,
    onConfirm: (LocalTime) -> Unit,
    onDismiss: () -> Unit,
) {
    val state = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = true,
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(LocalTime.of(state.hour, state.minute)) }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        text = { TimePicker(state = state) },
    )
}

private fun LocalDate.toEpochUtcMillis(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private fun Long.toLocalDateUtc(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

@Preview(showBackground = true)
@Composable
private fun AddEditEventContentCreatePreview() {
    HomlyTheme {
        AddEditEventContent(
            uiState = AddEditEventUiState(),
            onBack = {},
            onTitleChange = {},
            onDateChange = {},
            onAllDayToggle = {},
            onStartTimeChange = {},
            onEndTimeChange = {},
            onSave = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AddEditEventContentValidationErrorPreview() {
    HomlyTheme {
        AddEditEventContent(
            uiState = AddEditEventUiState(
                title = "Meeting",
                isEditMode = true,
                startTime = LocalTime.of(10, 0),
                endTime = LocalTime.of(10, 0),
                timeError = "End time must be after start time",
            ),
            onBack = {},
            onTitleChange = {},
            onDateChange = {},
            onAllDayToggle = {},
            onStartTimeChange = {},
            onEndTimeChange = {},
            onSave = {},
        )
    }
}
