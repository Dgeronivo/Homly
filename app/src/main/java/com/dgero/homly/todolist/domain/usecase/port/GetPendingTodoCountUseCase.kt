package com.dgero.homly.todolist.domain.usecase.port

interface GetPendingTodoCountUseCase {
    suspend operator fun invoke(userId: Long): Int
}
