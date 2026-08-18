package com.example.mytodoapp.repository

import androidx.room.*
import com.example.mytodoapp.model.Todo

@Dao
interface TodoDao {

    @Query("SELECT * FROM todos")
    suspend fun getAllTodos(): List<Todo>

    @Insert
    suspend fun insertTodo(todo: Todo)

    @Update
    suspend fun updateTodo(todo: Todo)

    @Delete
    suspend fun deleteTodo(todo: Todo)

    @Query("SELECT COUNT(*) FROM todos")
    suspend fun getCount(): Int
}