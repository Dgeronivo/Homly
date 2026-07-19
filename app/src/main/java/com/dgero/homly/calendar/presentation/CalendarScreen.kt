package com.dgero.homly.calendar.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dgero.homly.calendar.domain.CalendarLimits
import com.dgero.homly.calendar.domain.model.CalendarEvent
import com.dgero.homly.ui.theme.HomlyTheme
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.Month
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle

private val MONTH_HEADER_FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy")
private val TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm")
private val WEEKDAY_LABELS = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
private const val GRID_WEEKS = 6

/**
 * Month grid + selected day's event list (SAD Flow 1).
 *
 * The FAB ("+") checks the per-user event limit before navigating: [onAddEvent] fires (with the
 * currently selected day, AC-13) when under the limit, otherwise a Snackbar reports the limit
 * reached (SAD Flow 4, AC-07b). [onEventClick] opens an existing event in edit mode.
 */
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel,
    onAddEvent: (LocalDate) -> Unit = {},
    onEventClick: (Long) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.navigateToAddEvent.collect { onAddEvent(uiState.selectedDate) }
    }
    LaunchedEffect(Unit) {
        viewModel.eventLimitReached.collect {
            snackbarHostState.showSnackbar("Event limit reached (max ${CalendarLimits.MAX_EVENTS} events)")
        }
    }

    // Re-fetches the current month whenever this screen resumes — e.g. returning from
    // AddEditEventScreen after a create/edit/delete — since CalendarViewModel survives that
    // round-trip and would otherwise keep showing stale data (AC-05/AC-06).
    val lifecycleOwner = LocalLifecycleOwner.current
    val onResume by rememberUpdatedState(viewModel::refresh)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) onResume()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    CalendarContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onDateSelected = viewModel::onDateSelected,
        onMonthChanged = viewModel::onMonthChanged,
        onTodayClick = viewModel::onTodayClick,
        onDeleteEvent = viewModel::onDeleteEvent,
        onEventClick = onEventClick,
        onFabClick = viewModel::onAddEventClick,
    )
}

@Composable
private fun CalendarContent(
    uiState: CalendarUiState,
    snackbarHostState: SnackbarHostState,
    onDateSelected: (LocalDate) -> Unit,
    onMonthChanged: (YearMonth) -> Unit,
    onTodayClick: () -> Unit,
    onDeleteEvent: (Long) -> Unit,
    onEventClick: (Long) -> Unit,
    onFabClick: () -> Unit,
) {
    var showMonthYearPicker by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = onFabClick) {
                Icon(Icons.Default.Add, contentDescription = "Add event")
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
        ) {
            MonthHeader(
                yearMonth = uiState.currentYearMonth,
                onPickerClick = { showMonthYearPicker = true },
                onTodayClick = onTodayClick,
            )
            Spacer(Modifier.height(8.dp))
            MonthGrid(
                yearMonth = uiState.currentYearMonth,
                selectedDate = uiState.selectedDate,
                daysWithEvents = uiState.daysWithEvents,
                onDateSelected = onDateSelected,
            )
            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))
            SelectedDayEvents(
                events = uiState.selectedDayEvents,
                onDeleteEvent = onDeleteEvent,
                onEventClick = onEventClick,
                modifier = Modifier.weight(1f),
            )
        }
    }

    if (showMonthYearPicker) {
        MonthYearPickerDialog(
            initialYearMonth = uiState.currentYearMonth,
            onConfirm = { pickedYearMonth ->
                onMonthChanged(pickedYearMonth)
                showMonthYearPicker = false
            },
            onDismiss = { showMonthYearPicker = false },
        )
    }
}

@Composable
private fun MonthHeader(yearMonth: YearMonth, onPickerClick: () -> Unit, onTodayClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = yearMonth.format(MONTH_HEADER_FORMATTER),
            style = MaterialTheme.typography.headlineSmall,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onTodayClick) {
                Text("Сьогодні")
            }
            IconButton(onClick = onPickerClick) {
                Icon(Icons.Default.DateRange, contentDescription = "Pick month and year")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MonthHeaderPreview() {
    HomlyTheme {
        MonthHeader(
            yearMonth = YearMonth.of(2026, 5),
            onPickerClick = {},
            onTodayClick = {},
        )
    }
}

/**
 * Lets the user jump directly to any month + year (AC-11) — no incremental stepping through
 * intermediate months. [initialYearMonth] seeds the year stepper and month grid selection.
 */
@Composable
private fun MonthYearPickerDialog(
    initialYearMonth: YearMonth,
    onConfirm: (YearMonth) -> Unit,
    onDismiss: () -> Unit,
) {
    var selectedYear by remember { mutableIntStateOf(initialYearMonth.year) }
    var selectedMonth by remember { mutableStateOf(initialYearMonth.month) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select month & year") },
        text = {
            Column {
                YearStepper(
                    year = selectedYear,
                    onYearChange = { selectedYear = it },
                )
                Spacer(Modifier.height(8.dp))
                MonthPickerGrid(
                    selectedMonth = selectedMonth,
                    onMonthSelected = { selectedMonth = it },
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(YearMonth.of(selectedYear, selectedMonth)) }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Composable
private fun YearStepper(year: Int, onYearChange: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = { onYearChange(year - 1) }) {
            Text("‹", style = MaterialTheme.typography.headlineSmall)
        }
        Text(
            text = year.toString(),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 24.dp),
        )
        IconButton(onClick = { onYearChange(year + 1) }) {
            Text("›", style = MaterialTheme.typography.headlineSmall)
        }
    }
}

@Composable
private fun MonthPickerGrid(selectedMonth: Month, onMonthSelected: (Month) -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Month.entries.chunked(3).forEach { row ->
            Row(Modifier.fillMaxWidth()) {
                row.forEach { month ->
                    MonthCell(
                        month = month,
                        isSelected = month == selectedMonth,
                        onClick = { onMonthSelected(month) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthCell(
    month: Month,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundModifier = if (isSelected) {
        Modifier.background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
    } else {
        Modifier
    }

    val locale = LocalLocale.current.platformLocale

    Box(
        modifier = modifier
            .padding(4.dp)
            .then(backgroundModifier)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = month.getDisplayName(TextStyle.SHORT, locale),
            color = if (isSelected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        )
    }
}

@Composable
private fun MonthGrid(
    yearMonth: YearMonth,
    selectedDate: LocalDate,
    daysWithEvents: Set<LocalDate>,
    onDateSelected: (LocalDate) -> Unit,
) {
    val weeks = remember(yearMonth) { buildMonthWeeks(yearMonth) }

    Column(Modifier.fillMaxWidth()) {
        WeekdayHeaderRow()
        weeks.forEach { week ->
            Row(Modifier.fillMaxWidth()) {
                week.forEach { day ->
                    DayCell(
                        day = day,
                        isCurrentMonth = YearMonth.from(day) == yearMonth,
                        isSelected = day == selectedDate,
                        hasEvents = day in daysWithEvents,
                        onDateSelected = onDateSelected,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun WeekdayHeaderRow() {
    Row(Modifier.fillMaxWidth()) {
        WEEKDAY_LABELS.forEach { label ->
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DayCell(
    day: LocalDate,
    isCurrentMonth: Boolean,
    isSelected: Boolean,
    hasEvents: Boolean,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val textColor = when {
        !isCurrentMonth -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
        day.dayOfWeek == DayOfWeek.SUNDAY -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurface
    }
    val selectionModifier = if (isSelected) {
        Modifier.border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
    } else {
        Modifier
    }
    val clickModifier = if (isCurrentMonth) {
        Modifier.clickable { onDateSelected(day) }
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .then(selectionModifier)
            .then(clickModifier),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = day.dayOfMonth.toString(), color = textColor)
            if (hasEvents) {
                Box(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(4.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                )
            }
        }
    }
}

@Composable
private fun SelectedDayEvents(
    events: List<CalendarEvent>,
    onDeleteEvent: (Long) -> Unit,
    onEventClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var pendingDeleteEvent by remember { mutableStateOf<CalendarEvent?>(null) }

    if (events.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "No events for this day",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    } else {
        LazyColumn(modifier = modifier.fillMaxSize()) {
            items(events, key = { it.id }) { event ->
                EventRow(
                    event,
                    onClick = { onEventClick(event.id) },
                    onDeleteClick = { pendingDeleteEvent = event },
                )
            }
        }
    }

    val eventPendingDeletion = pendingDeleteEvent
    if (eventPendingDeletion != null) {
        AlertDialog(
            onDismissRequest = { pendingDeleteEvent = null },
            title = { Text("Delete this event?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteEvent(eventPendingDeletion.id)
                        pendingDeleteEvent = null
                    },
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteEvent = null }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
private fun EventRow(event: CalendarEvent, onClick: () -> Unit, onDeleteClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = event.title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = formatEventTime(event),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        IconButton(onClick = onDeleteClick) {
            Text("✕", style = MaterialTheme.typography.titleMedium)
        }
    }
}

private fun formatEventTime(event: CalendarEvent): String {
    if (event.isAllDay) return "All day"
    val start = event.startTime?.format(TIME_FORMATTER) ?: "--:--"
    val end = event.endTime?.format(TIME_FORMATTER) ?: "--:--"
    return "$start–$end"
}

/** Builds [GRID_WEEKS] weeks (Monday-first) covering [yearMonth], including adjacent-month fill days. */
private fun buildMonthWeeks(yearMonth: YearMonth): List<List<LocalDate>> {
    val firstOfMonth = yearMonth.atDay(1)
    val leadingDays = firstOfMonth.dayOfWeek.value - DayOfWeek.MONDAY.value
    val gridStart = firstOfMonth.minusDays(leadingDays.toLong())
    val totalCells = GRID_WEEKS * 7
    return (0 until totalCells).map { gridStart.plusDays(it.toLong()) }.chunked(7)
}

@Preview(showBackground = true)
@Composable
private fun CalendarContentPreview() {
    HomlyTheme {
        val yearMonth = YearMonth.of(2026, 5)
        CalendarContent(
            uiState = CalendarUiState(
                currentYearMonth = yearMonth,
                selectedDate = yearMonth.atDay(18),
                daysWithEvents = setOf(
                    yearMonth.atDay(1),
                    yearMonth.atDay(8),
                    yearMonth.atDay(9),
                    yearMonth.atDay(15),
                    yearMonth.atDay(20),
                    yearMonth.atDay(31),
                ),
                selectedDayEvents = listOf(
                    CalendarEvent(
                        id = 1,
                        userId = 1,
                        title = "Anniversary",
                        date = yearMonth.atDay(18),
                        isAllDay = true,
                        startTime = null,
                        endTime = null,
                    ),
                    CalendarEvent(
                        id = 2,
                        userId = 1,
                        title = "Doctor appointment",
                        date = yearMonth.atDay(18),
                        isAllDay = false,
                        startTime = LocalTime.of(14, 0),
                        endTime = LocalTime.of(15, 0),
                    ),
                ),
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onDateSelected = {},
            onMonthChanged = {},
            onTodayClick = {},
            onDeleteEvent = {},
            onEventClick = {},
            onFabClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CalendarContentEmptyDayPreview() {
    HomlyTheme {
        CalendarContent(
            uiState = CalendarUiState(),
            snackbarHostState = remember { SnackbarHostState() },
            onDateSelected = {},
            onMonthChanged = {},
            onTodayClick = {},
            onDeleteEvent = {},
            onEventClick = {},
            onFabClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MonthYearPickerDialogPreview() {
    HomlyTheme {
        MonthYearPickerDialog(
            initialYearMonth = YearMonth.of(2026, 5),
            onConfirm = {},
            onDismiss = {},
        )
    }
}
