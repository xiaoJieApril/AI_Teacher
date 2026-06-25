package com.lolha.learningapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lolha.learningapp.data.local.LearningTaskEntity
import com.lolha.learningapp.ui.components.EmptyState

@Composable
fun TasksScreen(
    tasks: List<LearningTaskEntity>,
    onTaskDone: (Long) -> Unit,
    onTaskHomework: (LearningTaskEntity) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (tasks.isEmpty()) {
            item {
                EmptyState(
                    title = "No tasks yet",
                    body = "Use the assignment button to let the teacher build today's schedule.",
                )
            }
        }
        items(tasks) { task ->
            Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(task.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("${task.subject} · ${task.suggestedMinutes} min · ${task.status}")
                        }
                        if (task.status != "done") {
                            IconButton(onClick = { onTaskDone(task.id) }) {
                                Icon(Icons.Default.Check, contentDescription = "Mark done")
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(task.description)
                    if (task.completionStandard.isNotBlank()) {
                        Spacer(Modifier.height(10.dp))
                        Text("Completion", fontWeight = FontWeight.Bold)
                        Text(task.completionStandard)
                    }
                    if (task.nextActionInstruction.isNotBlank()) {
                        Spacer(Modifier.height(10.dp))
                        Text("Next", fontWeight = FontWeight.Bold)
                        Text(task.nextActionInstruction)
                    }
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(onClick = { onTaskHomework(task) }) {
                        Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = null)
                        Text(" Homework")
                    }
                }
            }
        }
    }
}

