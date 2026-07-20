package com.dgero.homly.calendar.presentation

import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import com.dgero.homly.R
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
import kotlinx.coroutines.flow.drop

private val MONTH_HEADER_FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy")
private val TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm")
private const val GRID_WEEKS = 6
internal const val MONTH_PAGER_TEST_TAG = "monthPager"

// "Rounded Squares" shape language: soft rounded squares replace circles across the
// today-badge, day-selection highlight, event dot, FAB and event rows.
private val TodayBadgeShape = RoundedCornerShape(9.dp)
private val SelectedDayShape = RoundedCornerShape(10.dp)
private val EventDotShape = RoundedCornerShape(1.dp)
private val FabShape = RoundedCornerShape(16.dp)
private val EventCardShape = RoundedCornerShape(12.dp)
private const val SELECTED_NOT_TODAY_ALPHA = 0.35f

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
    val eventLimitMessage = stringResource(R.string.event_limit_reached, CalendarLimits.MAX_EVENTS)
    LaunchedEffect(Unit) {
        viewModel.eventLimitReached.collect {
            snackbarHostState.showSnackbar(eventLimitMessage)
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
internal fun CalendarContent(
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

    // Fixed once per screen instance: the reference point the pager's huge page range is
    // measured from (PAGER_ANCHOR_PAGE == this month). Must not track uiState.currentYearMonth,
    // or every swipe would shift the anchor and break the page<->month mapping.
    val anchorYearMonth = remember { uiState.currentYearMonth }
    val pagerState = rememberPagerState(
        initialPage = yearMonthToPage(uiState.currentYearMonth, anchorYearMonth),
        pageCount = { PAGER_PAGE_COUNT },
    )

    // Swipe settles on a new page -> tell the ViewModel. animateScrollToPage below also settles
    // on its target page, so this fires for programmatic jumps too; onMonthChanged is idempotent
    // for an unchanged month, so that's harmless. drop(1) skips the initial emission of the
    // starting page, which would otherwise re-trigger a load of the already-current month.
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.drop(1).collect { page ->
            onMonthChanged(pageToYearMonth(page, anchorYearMonth))
        }
    }
    // "Сьогодні" / month-year picker changed the month -> scroll the pager to match.
    LaunchedEffect(uiState.currentYearMonth) {
        val targetPage = yearMonthToPage(uiState.currentYearMonth, anchorYearMonth)
        if (pagerState.currentPage != targetPage) pagerState.animateScrollToPage(targetPage)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = onFabClick, shape = FabShape) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_event))
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
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.testTag(MONTH_PAGER_TEST_TAG),
            ) { page ->
                val pageYearMonth = pageToYearMonth(page, anchorYearMonth)
                MonthGrid(
                    yearMonth = pageYearMonth,
                    selectedDate = uiState.selectedDate,
                    daysWithEvents = if (pageYearMonth == uiState.currentYearMonth) {
                        uiState.daysWithEvents
                    } else {
                        emptySet()
                    },
                    onDateSelected = onDateSelected,
                )
            }
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
            TodayButton(onClick = onTodayClick)
            IconButton(onClick = onPickerClick) {
                Icon(Icons.Default.DateRange, contentDescription = stringResource(R.string.pick_month_year))
            }
        }
    }
}

/** Circular "jump to today" shortcut showing today's day-of-month number, like a mini date badge. */
@Composable
private fun TodayButton(onClick: () -> Unit) {
    val todayContentDesc = stringResource(R.string.today_button)
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(TodayBadgeShape)
            .background(MaterialTheme.colorScheme.primary)
            .clickable(onClick = onClick)
            .semantics { contentDescription = todayContentDesc },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = LocalDate.now().dayOfMonth.toString(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimary,
        )
    }
}

@Preview(showBackground = true, locale = "uk")
@Composable
private fun TodayButtonPreview() {
    HomlyTheme {
        TodayButton(onClick = {})
    }
}

@Preview(showBackground = true, locale = "uk")
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
        title = { Text(stringResource(R.string.select_month_year)) },
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
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
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
    val today = remember { LocalDate.now() }

    Column(Modifier.fillMaxWidth()) {
        WeekdayHeaderRow()
        weeks.forEach { week ->
            Row(Modifier.fillMaxWidth()) {
                week.forEach { day ->
                    DayCell(
                        day = day,
                        isCurrentMonth = YearMonth.from(day) == yearMonth,
                        isSelected = day == selectedDate,
                        isToday = day == today,
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
    val weekdayLabels = listOf(
        stringResource(R.string.mon),
        stringResource(R.string.tue),
        stringResource(R.string.wed),
        stringResource(R.string.thu),
        stringResource(R.string.fri),
        stringResource(R.string.sat),
        stringResource(R.string.sun),
    )
    Row(Modifier.fillMaxWidth()) {
        weekdayLabels.forEach { label ->
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
    isToday: Boolean,
    hasEvents: Boolean,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val textColor = when {
        !isCurrentMonth -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
        day.dayOfWeek == DayOfWeek.SUNDAY -> MaterialTheme.colorScheme.error
        isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }
    // The selected day is highlighted opaque only when it's today; selecting any other
    // day highlights it with the same shape/color but semi-transparent, so "today" stays
    // visually distinct from an arbitrary selection.
    val selectionModifier = when {
        isSelected && isToday -> Modifier
            .clip(SelectedDayShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
        isSelected -> Modifier
            .clip(SelectedDayShape)
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = SELECTED_NOT_TODAY_ALPHA))
        else -> Modifier
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
                        .size(5.dp)
                        .clip(EventDotShape)
                        .background(
                            if (isSelected) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                        ),
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
                text = stringResource(R.string.no_events_day),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
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
            title = { Text(stringResource(R.string.delete_this_event)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteEvent(eventPendingDeletion.id)
                        pendingDeleteEvent = null
                    },
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteEvent = null }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}

@Composable
private fun EventRow(event: CalendarEvent, onClick: () -> Unit, onDeleteClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = EventCardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
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
}

private fun formatEventTime(event: CalendarEvent): String {
    // Note: This function is called outside Composable context, so we can't use stringResource here
    // "All day" will be handled in the UI layer instead
    val start = event.startTime?.format(TIME_FORMATTER) ?: "--:--"
    val end = event.endTime?.format(TIME_FORMATTER) ?: "--:--"
    return if (event.isAllDay) "All day" else "$start–$end"
}

/** Builds [GRID_WEEKS] weeks (Monday-first) covering [yearMonth], including adjacent-month fill days. */
private fun buildMonthWeeks(yearMonth: YearMonth): List<List<LocalDate>> {
    val firstOfMonth = yearMonth.atDay(1)
    val leadingDays = firstOfMonth.dayOfWeek.value - DayOfWeek.MONDAY.value
    val gridStart = firstOfMonth.minusDays(leadingDays.toLong())
    val totalCells = GRID_WEEKS * 7
    return (0 until totalCells).map { gridStart.plusDays(it.toLong()) }.chunked(7)
}

@Preview(showBackground = true, locale = "uk")
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

@Preview(showBackground = true, locale = "uk")
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

/** Today is the selected day -> opaque highlight. */
@Preview(showBackground = true, locale = "uk")
@Composable
private fun CalendarContentTodaySelectedPreview() {
    HomlyTheme {
        CalendarContent(
            uiState = CalendarUiState(
                currentYearMonth = YearMonth.now(),
                selectedDate = LocalDate.now(),
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

/** A day other than today is selected -> translucent highlight, so "today" stays distinct. */
@Preview(showBackground = true, locale = "uk")
@Composable
private fun CalendarContentOtherDaySelectedPreview() {
    HomlyTheme {
        val yearMonth = YearMonth.now()
        val otherDay = if (LocalDate.now().dayOfMonth == 1) yearMonth.atDay(2) else yearMonth.atDay(1)
        CalendarContent(
            uiState = CalendarUiState(
                currentYearMonth = yearMonth,
                selectedDate = otherDay,
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

@Preview(showBackground = true, locale = "uk")
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
