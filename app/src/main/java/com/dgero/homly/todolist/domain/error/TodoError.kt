package com.dgero.homly.todolist.domain.error

sealed class TodoError : Exception() {
    object EmptyTitle : TodoError()
    object TitleTooLong : TodoError()
    object LimitReached : TodoError()
    object Unauthorized : TodoError()
    data class Unknown(override val cause: Throwable) : TodoError()
}
