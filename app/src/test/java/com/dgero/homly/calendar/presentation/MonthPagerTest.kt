package com.dgero.homly.calendar.presentation

import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Test

class MonthPagerTest {

    private val anchor = YearMonth.of(2026, 7)

    @Test
    fun `anchor page maps back to the anchor month`() {
        assertEquals(anchor, pageToYearMonth(PAGER_ANCHOR_PAGE, anchor))
        assertEquals(PAGER_ANCHOR_PAGE, yearMonthToPage(anchor, anchor))
    }

    @Test
    fun `pages after the anchor map to later months`() {
        assertEquals(YearMonth.of(2026, 8), pageToYearMonth(PAGER_ANCHOR_PAGE + 1, anchor))
        assertEquals(YearMonth.of(2027, 1), pageToYearMonth(PAGER_ANCHOR_PAGE + 6, anchor))
    }

    @Test
    fun `pages before the anchor map to earlier months`() {
        assertEquals(YearMonth.of(2026, 6), pageToYearMonth(PAGER_ANCHOR_PAGE - 1, anchor))
        assertEquals(YearMonth.of(2025, 7), pageToYearMonth(PAGER_ANCHOR_PAGE - 12, anchor))
    }

    @Test
    fun `yearMonthToPage is the inverse of pageToYearMonth`() {
        val months = listOf(
            YearMonth.of(2025, 1),
            YearMonth.of(2026, 7),
            YearMonth.of(2030, 12),
        )
        for (month in months) {
            val page = yearMonthToPage(month, anchor)
            assertEquals(month, pageToYearMonth(page, anchor))
        }
    }
}
