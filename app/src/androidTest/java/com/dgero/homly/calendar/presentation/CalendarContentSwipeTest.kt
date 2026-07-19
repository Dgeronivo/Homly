package com.dgero.homly.calendar.presentation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dgero.homly.ui.theme.HomlyTheme
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Verifies swiping the month grid ([MONTH_PAGER_TEST_TAG]) drives [CalendarContent]'s month state. */
@RunWith(AndroidJUnit4::class)
class CalendarContentSwipeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val startMonth = YearMonth.of(2026, 7)
    private var reportedYearMonth: YearMonth? = null

    private fun setContentAt(initialYearMonth: YearMonth) {
        composeTestRule.setContent {
            var uiState by remember { mutableStateOf(CalendarUiState(currentYearMonth = initialYearMonth)) }
            HomlyTheme {
                CalendarContent(
                    uiState = uiState,
                    snackbarHostState = remember { SnackbarHostState() },
                    onDateSelected = {},
                    onMonthChanged = { month ->
                        reportedYearMonth = month
                        uiState = uiState.copy(currentYearMonth = month, selectedDate = month.atDay(1))
                    },
                    onTodayClick = {},
                    onDeleteEvent = {},
                    onEventClick = {},
                    onFabClick = {},
                )
            }
        }
    }

    @Test
    fun swipingLeftAdvancesToNextMonth() {
        setContentAt(startMonth)

        composeTestRule.onNodeWithTag(MONTH_PAGER_TEST_TAG).performTouchInput { swipeLeft() }
        composeTestRule.waitForIdle()

        assertEquals(startMonth.plusMonths(1), reportedYearMonth)
    }

    @Test
    fun swipingRightGoesToPreviousMonth() {
        setContentAt(startMonth)

        composeTestRule.onNodeWithTag(MONTH_PAGER_TEST_TAG).performTouchInput { swipeRight() }
        composeTestRule.waitForIdle()

        assertEquals(startMonth.minusMonths(1), reportedYearMonth)
    }
}
