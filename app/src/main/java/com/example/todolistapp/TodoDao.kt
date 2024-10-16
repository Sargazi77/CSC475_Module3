package com.example.todolistapp

import androidx.room.*

@Dao
interface TodoDao {
    @Query("SELECT * FROM todo_items") // Retrieve all tasks
    fun getAll(): List<TodoItem>

    @Insert // Insert a new task into the database
    fun insert(todoItem: TodoItem)

    @Delete // Delete a task from the database
    fun delete(todoItem: TodoItem)

    @Update // Update a task (e.g., mark as completed)
    fun update(todoItem: TodoItem)

    @Query("UPDATE todo_items SET isCompleted = :isCompleted WHERE id = :id")
    fun updateCompletion(id: Int, isCompleted: Boolean) // Mark as completed
}