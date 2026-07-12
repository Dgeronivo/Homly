package com.dgero.homly.calendar.domain.error

sealed class CalendarError : Exception() {
    object EmptyTitle : CalendarError()
    object TitleTooLong : CalendarError()
    object EndNotAfterStart : CalendarError()
    object EventLimitReached : CalendarError()
}
