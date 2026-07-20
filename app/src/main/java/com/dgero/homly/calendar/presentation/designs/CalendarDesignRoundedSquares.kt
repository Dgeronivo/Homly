package com.dgero.homly.calendar.presentation.designs

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dgero.homly.R
import com.dgero.homly.calendar.domain.model.CalendarEvent
import com.dgero.homly.ui.theme.HomlyTheme
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter

/**
 * Design 1 — "Заокруглені квадрати": замість кіл (сьогодні-бейдж, вибраний день, крапка події)
 * скрізь використовуються м'яко заокруглені квадрати. Список подій — окремі картки.
 * Колірна палітра — без змін (HomlyTheme).
 */

private val MONTH_HEADER_FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy")
private val TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm")
private const val GRID_WEEKS = 6
private val CellShape = RoundedCornerShape(10.dp)
private val DotShape = RoundedCornerShape(1.dp)
private const val SELECTED_NOT_TODAY_ALPHA = 0.35f

@Composable
private fun RoundedSquaresCalendarContent(
    yearMonth: YearMonth,
    selectedDate: LocalDate,
    daysWithEvents: Set<LocalDate>,
    selectedDayEvents: List<CalendarEvent>,
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {}, shape = RoundedCornerShape(16.dp)) {
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
            RoundedSquaresMonthHeader(yearMonth = yearMonth)
            Spacer(Modifier.height(8.dp))
            RoundedSquaresMonthGrid(
                yearMonth = yearMonth,
                selectedDate = selectedDate,
                daysWithEvents = daysWithEvents,
            )
            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))
            RoundedSquaresEventList(events = selectedDayEvents, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun RoundedSquaresMonthHeader(yearMonth: YearMonth) {
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
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable {},
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = LocalDate.now().dayOfMonth.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
            IconButton(onClick = {}) {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = stringResource(R.string.pick_month_year),
                )
            }
        }
    }
}

@Composable
private fun RoundedSquaresMonthGrid(
    yearMonth: YearMonth,
    selectedDate: LocalDate,
    daysWithEvents: Set<LocalDate>,
) {
    val weeks = buildMonthWeeks(yearMonth)
    val today = LocalDate.now()
    val weekdayLabels = listOf(
        stringResource(R.string.mon),
        stringResource(R.string.tue),
        stringResource(R.string.wed),
        stringResource(R.string.thu),
        stringResource(R.string.fri),
        stringResource(R.string.sat),
        stringResource(R.string.sun),
    )
    Column(Modifier.fillMaxWidth()) {
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
        weeks.forEach { week ->
            Row(Modifier.fillMaxWidth()) {
                week.forEach { day ->
                    RoundedSquaresDayCell(
                        day = day,
                        isCurrentMonth = YearMonth.from(day) == yearMonth,
                        isSelected = day == selectedDate,
                        isToday = day == today,
                        hasEvents = day in daysWithEvents,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun RoundedSquaresDayCell(
    day: LocalDate,
    isCurrentMonth: Boolean,
    isSelected: Boolean,
    isToday: Boolean,
    hasEvents: Boolean,
    modifier: Modifier = Modifier,
) {
    val textColor = when {
        !isCurrentMonth -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
        day.dayOfWeek == DayOfWeek.SUNDAY -> MaterialTheme.colorScheme.error
        isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }
    val backgroundModifier = when {
        isSelected && isToday -> Modifier
            .clip(CellShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
        isSelected -> Modifier
            .clip(CellShape)
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = SELECTED_NOT_TODAY_ALPHA))
        else -> Modifier
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .then(backgroundModifier)
            .then(if (isCurrentMonth) Modifier.clickable {} else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = day.dayOfMonth.toString(), color = textColor)
            if (hasEvents) {
                Box(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(5.dp)
                        .clip(DotShape)
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
private fun RoundedSquaresEventList(events: List<CalendarEvent>, modifier: Modifier = Modifier) {
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
                RoundedSquaresEventCard(event)
            }
        }
    }
}

@Composable
private fun RoundedSquaresEventCard(event: CalendarEvent) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable {},
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
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
        }
    }
}

private fun formatEventTime(event: CalendarEvent): String {
    val start = event.startTime?.format(TIME_FORMATTER) ?: "--:--"
    val end = event.endTime?.format(TIME_FORMATTER) ?: "--:--"
    return if (event.isAllDay) "All day" else "$start–$end"
}

private fun buildMonthWeeks(yearMonth: YearMonth): List<List<LocalDate>> {
    val firstOfMonth = yearMonth.atDay(1)
    val leadingDays = firstOfMonth.dayOfWeek.value - DayOfWeek.MONDAY.value
    val gridStart = firstOfMonth.minusDays(leadingDays.toLong())
    val totalCells = GRID_WEEKS * 7
    return (0 until totalCells).map { gridStart.plusDays(it.toLong()) }.chunked(7)
}

@Preview(showBackground = true, locale = "uk")
@Composable
private fun RoundedSquaresCalendarPreview() {
    HomlyTheme {
        val yearMonth = YearMonth.of(2026, 5)
        RoundedSquaresCalendarContent(
            yearMonth = yearMonth,
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
        )
    }
}

@Preview(showBackground = true, locale = "uk")
@Composable
private fun RoundedSquaresCalendarEmptyDayPreview() {
    HomlyTheme {
        val yearMonth = YearMonth.of(2026, 5)
        RoundedSquaresCalendarContent(
            yearMonth = yearMonth,
            selectedDate = yearMonth.atDay(3),
            daysWithEvents = emptySet(),
            selectedDayEvents = emptyList(),
        )
    }
}

/** Today is the selected day -> opaque highlight. */
@Preview(showBackground = true, locale = "uk")
@Composable
private fun RoundedSquaresCalendarTodaySelectedPreview() {
    HomlyTheme {
        val yearMonth = YearMonth.now()
        RoundedSquaresCalendarContent(
            yearMonth = yearMonth,
            selectedDate = LocalDate.now(),
            daysWithEvents = emptySet(),
            selectedDayEvents = emptyList(),
        )
    }
}

/** A day other than today is selected -> translucent highlight, so "today" stays distinct. */
@Preview(showBackground = true, locale = "uk")
@Composable
private fun RoundedSquaresCalendarOtherDaySelectedPreview() {
    HomlyTheme {
        val yearMonth = YearMonth.now()
        val otherDay = if (LocalDate.now().dayOfMonth == 1) yearMonth.atDay(2) else yearMonth.atDay(1)
        RoundedSquaresCalendarContent(
            yearMonth = yearMonth,
            selectedDate = otherDay,
            daysWithEvents = emptySet(),
            selectedDayEvents = emptyList(),
        )
    }
}
