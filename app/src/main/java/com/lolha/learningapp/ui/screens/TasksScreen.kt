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
import com.lolha.learningapp.ui.components.CompactButtonHeight
import com.lolha.learningapp.ui.components.CompactCardPadding
import com.lolha.learningapp.ui.components.CompactListGap
import com.lolha.learningapp.ui.components.CompactPagePadding
import com.lolha.learningapp.ui.components.EmptyState
import com.lolha.learningapp.ui.components.RecommendedMaterials

@Composable
fun TasksScreen(
    tasks: List<LearningTaskEntity>,
    onTaskDone: (Long) -> Unit,
    onTaskHomework: (LearningTaskEntity) -> Unit,
    onOpenUrl: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(CompactPagePadding),
        verticalArrangement = Arrangement.spacedBy(CompactListGap),
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
                Column(modifier = Modifier.padding(CompactCardPadding)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(task.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("${task.subject} · ${task.suggestedMinutes} min · ${task.status}", fontSize = 12.sp)
                        }
                        if (task.status != "done") {
                            IconButton(onClick = { onTaskDone(task.id) }) {
                                Icon(Icons.Default.Check, contentDescription = "Mark done")
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(task.description)
                    if (task.completionStandard.isNotBlank()) {
                        Spacer(Modifier.height(6.dp))
                        Text("Completion", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(task.completionStandard)
                    }
                    if (task.nextActionInstruction.isNotBlank()) {
                        Spacer(Modifier.height(6.dp))
                        Text("Next", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(task.nextActionInstruction)
                    }
                    RecommendedMaterials(subject = task.subject, onOpenUrl = onOpenUrl)
                    Spacer(Modifier.height(CompactListGap))
                    OutlinedButton(onClick = { onTaskHomework(task) }, modifier = Modifier.height(CompactButtonHeight)) {
                        Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = null)
                        Text(" Homework")
                    }
                }
            }
        }
    }
}
