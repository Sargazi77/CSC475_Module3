package com.example.todolistapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.todolistapp.ui.theme.ToDoListAppTheme
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private lateinit var database: TodoDatabase // Database instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the database when the activity is created
        database = TodoDatabase.getDatabase(this)

        setContent {
            ToDoListAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ToDoListScreen() // Load the main to-do list screen
                }
            }
        }
    }

    // Composable function to manage the to-do list screen
    @Composable
    fun ToDoListScreen() {
        var taskText by remember { mutableStateOf("") } // State for task input
        val tasks = remember { mutableStateListOf<TodoItem>() } // List to hold tasks

        // Load tasks from the database on a background thread
        LaunchedEffect(Unit) {
            val loadedTasks = withContext(Dispatchers.IO) {
                database.todoDao().getAll() // Fetch tasks from the database
            }
            tasks.addAll(loadedTasks) // Add tasks to the UI list
        }

        // Main column layout to display input field, button, and task list
        Column(modifier = Modifier.padding(16.dp)) {
            // Row for input field and add button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Input field to enter new tasks
                BasicTextField(
                    value = taskText,
                    onValueChange = { taskText = it },
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                    decorationBox = { innerTextField ->
                        if (taskText.isEmpty()) {
                            Text("Enter task...", style = MaterialTheme.typography.bodyLarge)
                        }
                        innerTextField()
                    }
                )

                // Button to add a new task
                Button(onClick = {
                    if (taskText.isNotEmpty()) {
                        val newTask = TodoItem(task = taskText) // Create a new task object
                        lifecycleScope.launch {
                            withContext(Dispatchers.IO) {
                                database.todoDao().insert(newTask) // Insert task into database
                            }
                            tasks.add(newTask) // Add task to the UI list
                        }
                        taskText = "" // Clear the input field
                    }
                }) {
                    Text("Add Task")
                }
            }

            Spacer(modifier = Modifier.height(16.dp)) // Add space between rows

            // Loop through each task and display it in the UI
            tasks.forEach { task ->
                TaskItem(
                    task = task,
                    onTaskCompleted = { completed ->
                        // Update task completion status when checkbox is toggled
                        lifecycleScope.launch {
                            withContext(Dispatchers.IO) {
                                database.todoDao().updateCompletion(task.id, completed) // Update in DB
                            }
                            task.isCompleted = completed // Update in UI
                        }
                    },
                    onDelete = {
                        // Delete task when delete button is clicked
                        lifecycleScope.launch {
                            withContext(Dispatchers.IO) {
                                database.todoDao().delete(it) // Remove from DB
                            }
                            tasks.remove(it) // Remove from UI list
                        }
                    }
                )
            }
        }
    }

    // Composable function to display each task item
    @Composable
    fun TaskItem(
        task: TodoItem,
        onTaskCompleted: (Boolean) -> Unit, // Callback for task completion toggle
        onDelete: (TodoItem) -> Unit // Callback for task deletion
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Row with a checkbox and task name
            Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                // Checkbox to mark the task as completed or not
                Checkbox(
                    checked = task.isCompleted, // Checkbox reflects the task status
                    onCheckedChange = { completed ->
                        onTaskCompleted(completed) // Update the status when toggled
                    }
                )
                Text(
                    text = task.task, // Display the task description
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            // Button to delete the task
            Button(onClick = { onDelete(task) }) {
                Text("Delete")
            }
        }
    }

    // Preview function for the to-do list screen
    @Preview(showBackground = true)
    @Composable
    fun ToDoListScreenPreview() {
        ToDoListAppTheme {
            ToDoListScreen() // Preview the to-do list UI
        }
    }
}

//START Application
//
//INITIALIZE Room database
//
//DISPLAY main activity with:
//    - Task input field
//    - Add task button
//    - List of tasks with:
//        - Checkbox to mark tasks as completed
//        - Delete button to remove tasks
//
//FUNCTION Add Task:
//    IF input field is not empty:
//        CREATE a new task with "isCompleted" set to false
//        SAVE task to the Room database (in the background)
//        ADD task to the list
//
//FUNCTION Mark Task as Completed:
//    TOGGLE the "isCompleted" status of the selected task
//    UPDATE the task status in the Room database (in the background)
//
//FUNCTION Delete Task:
//    REMOVE the selected task from the database (in the background)
//    REMOVE the task from the list
//
//LOAD all tasks from the Room database when the app starts
//
//END Application