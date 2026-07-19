package com.dgero.homly.todolist.domain.usecase.port

interface GetTodoItemCountUseCase {
    suspend operator fun invoke(userId: Long): Int
}
