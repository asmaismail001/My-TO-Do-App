package com.example.mytodoapp.repository

import androidx.room.*
import com.example.mytodoapp.model.Todo

@Dao
interface TodoDao {

    @Query("SELECT * FROM todos")
    suspend fun getAllTodos(): List<Todo>

    @Query("SELECT * FROM todos WHERE userId = :userId")
    suspend fun getAllTodosForUser(userId: String): List<Todo>

    @Query("UPDATE todos SET userId = :userId WHERE userId IS NULL")
    suspend fun assignOrphanTasksToUser(userId: String)

    @Query("SELECT * FROM todos WHERE id = :id LIMIT 1")
    suspend fun getTodoById(id: Int): Todo?

    @Insert
    suspend fun insertTodo(todo: Todo): Long

    @Update
    suspend fun updateTodo(todo: Todo)

    @Delete
    suspend fun deleteTodo(todo: Todo)
}