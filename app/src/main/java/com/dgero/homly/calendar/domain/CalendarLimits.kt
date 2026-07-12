package com.dgero.homly.calendar.domain

/** Single source of truth for calendar business constraints. */
object CalendarLimits {
    /** Max events per user. */
    const val MAX_EVENTS = 100

    /** Max event title length, in characters. */
    const val MAX_TITLE_LENGTH = 100
}
