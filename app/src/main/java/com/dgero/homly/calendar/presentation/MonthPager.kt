package com.dgero.homly.calendar.presentation

import java.time.YearMonth
import java.time.temporal.ChronoUnit

/**
 * [HorizontalPager][androidx.compose.foundation.pager.HorizontalPager] has no notion of "no
 * limit", so month swiping is modeled as a huge page range centered on [PAGER_ANCHOR_PAGE], which
 * always maps to whatever month was on screen when the pager was first created. Swiping left/right
 * from there just walks [YearMonth.plusMonths] in either direction.
 */
internal const val PAGER_ANCHOR_PAGE = Int.MAX_VALUE / 2
internal const val PAGER_PAGE_COUNT = Int.MAX_VALUE

internal fun pageToYearMonth(page: Int, anchorYearMonth: YearMonth): YearMonth =
    anchorYearMonth.plusMonths((page - PAGER_ANCHOR_PAGE).toLong())

internal fun yearMonthToPage(yearMonth: YearMonth, anchorYearMonth: YearMonth): Int =
    PAGER_ANCHOR_PAGE + anchorYearMonth.until(yearMonth, ChronoUnit.MONTHS).toInt()
