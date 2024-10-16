package com.example.todolistapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todo_items")
data class TodoItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val task: String,
    var isCompleted: Boolean = false // Added this field
)