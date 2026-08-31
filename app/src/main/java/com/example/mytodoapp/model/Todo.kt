package com.example.mytodoapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Priority { HIGH, MEDIUM, LOW }

@Entity(tableName = "todos")
data class Todo(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String = "",
    val priority: Priority = Priority.MEDIUM,
    val completed: Boolean,
    val createdAt: Long = System.currentTimeMillis(),
    val dueTimeMillis: Long? = null,
    val endTimeMillis: Long? = null,
    val attachmentUri: String? = null,
    val notificationEnabled: Boolean = false,
    val notificationMinutesBefore: Int = 10,
    val userId: String? = null
)