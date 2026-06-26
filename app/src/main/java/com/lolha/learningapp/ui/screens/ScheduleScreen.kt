package com.lolha.learningapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lolha.learningapp.data.local.ScheduleItemEntity
import com.lolha.learningapp.ui.components.CompactButtonHeight
import com.lolha.learningapp.ui.components.CompactCardPadding
import com.lolha.learningapp.ui.components.CompactListGap
import com.lolha.learningapp.ui.components.CompactPagePadding
import com.lolha.learningapp.ui.components.DeleteReasonDialog
import com.lolha.learningapp.ui.components.EmptyState
import com.lolha.learningapp.ui.components.RecommendedMaterials
import com.lolha.learningapp.ui.components.TeacherThinkingCard
import com.lolha.learningapp.ui.state.MainUiState
import com.lolha.learningapp.ui.state.ScheduleMode
import com.lolha.learningapp.ui.state.thinkingMessageOrNull
import java.time.LocalDate

@Composable
fun ScheduleScreen(
    state: MainUiState,
    onModeSelected: (ScheduleMode) -> Unit,
    onDailySchedule: () -> Unit,
    onWeeklySchedule: () -> Unit,
    onDone: (ScheduleItemEntity) -> Unit,
    onDelete: (ScheduleItemEntity, String, String) -> Unit,
    onFocus: (ScheduleItemEntity) -> Unit,
    onHomework: (ScheduleItemEntity) -> Unit,
    onOpenUrl: (String) -> Unit,
) {
    val today = LocalDate.now().toString()
    val visibleItems = when (state.scheduleMode) {
        ScheduleMode.Today -> state.scheduleItems.filter { it.date == today }
        ScheduleMode.Week -> state.scheduleItems
    }
    var pendingDelete by remember { mutableStateOf<ScheduleItemEntity?>(null) }
    var reasonCategory by remember { mutableStateOf("") }
    var reasonDetail by remember { mutableStateOf("") }

    pendingDelete?.let { item ->
        DeleteReasonDialog(
            title = "Delete schedule item",
            reasonCategory = reasonCategory,
            reasonDetail = reasonDetail,
            onReasonCategoryChanged = { reasonCategory = it },
            onReasonDetailChanged = { reasonDetail = it },
            onDismiss = {
                pendingDelete = null
                reasonCategory = ""
                reasonDetail = ""
            },
            onConfirm = {
                onDelete(item, reasonCategory, reasonDetail.trim())
                pendingDelete = null
                reasonCategory = ""
                reasonDetail = ""
            },
        )
    }

    Column(modifier = Modifier.padding(CompactPagePadding)) {
        Row(horizontalArrangement = Arrangement.spacedBy(CompactListGap)) {
            ScheduleModeButton(
                label = "Today",
                selected = state.scheduleMode == ScheduleMode.Today,
                onClick = { onModeSelected(ScheduleMode.Today) },
                modifier = Modifier.weight(1f),
            )
            ScheduleModeButton(
                label = "Week",
                selected = state.scheduleMode == ScheduleMode.Week,
                onClick = { onModeSelected(ScheduleMode.Week) },
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(CompactListGap))

        Row(horizontalArrangement = Arrangement.spacedBy(CompactListGap)) {
            OutlinedButton(
                onClick = onDailySchedule,
                enabled = !state.loading,
                modifier = Modifier.weight(1f).height(CompactButtonHeight),
            ) {
                Text("Generate Today")
            }
            OutlinedButton(
                onClick = onWeeklySchedule,
                enabled = !state.loading,
                modifier = Modifier.weight(1f).height(CompactButtonHeight),
            ) {
                Text("Generate Week")
            }
        }

        Spacer(Modifier.height(CompactListGap))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(CompactListGap)) {
            state.aiRequestState.thinkingMessageOrNull()?.let { message ->
                item {
                    TeacherThinkingCard(message)
                }
            }
            if (visibleItems.isEmpty() && !state.loading) {
                item {
                    EmptyState(
                        title = "No schedule yet",
                        body = "Ask the teacher for today's or this week's schedule.",
                    )
                }
            }

            visibleItems.groupBy { it.date }.forEach { (date, itemsForDate) ->
                item {
                    Text(date, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                items(itemsForDate) { item ->
                    ScheduleCard(
                        item = item,
                        busy = state.loading,
                        onDone = onDone,
                        onDelete = { deleteItem -> pendingDelete = deleteItem },
                        onFocus = onFocus,
                        onHomework = onHomework,
                        onOpenUrl = onOpenUrl,
                    )
                }
            }
        }
    }
}

@Composable
private fun ScheduleModeButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (selected) {
        Button(onClick = onClick, modifier = modifier) {
            Text(label)
        }
    } else {
        OutlinedButton(onClick = onClick, modifier = modifier) {
            Text(label)
        }
    }
}

@Composable
private fun ScheduleCard(
    item: ScheduleItemEntity,
    busy: Boolean,
    onDone: (ScheduleItemEntity) -> Unit,
    onDelete: (ScheduleItemEntity) -> Unit,
    onFocus: (ScheduleItemEntity) -> Unit,
    onHomework: (ScheduleItemEntity) -> Unit,
    onOpenUrl: (String) -> Unit,
) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.padding(CompactCardPadding)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("${item.startTime}-${item.endTime}", fontWeight = FontWeight.Bold)
                    Text(item.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("${item.subject} · ${item.suggestedMinutes} min · ${item.status}", fontSize = 12.sp)
                }
                if (item.status != "done") {
                    IconButton(onClick = { onDone(item) }, enabled = !busy) {
                        Icon(Icons.Default.Check, contentDescription = "Mark done")
                    }
                }
                IconButton(onClick = { onDelete(item) }, enabled = !busy) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete schedule")
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(item.description)
            if (item.completionStandard.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text("Completion", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(item.completionStandard)
            }
            RecommendedMaterials(subject = item.subject, onOpenUrl = onOpenUrl)
            Spacer(Modifier.height(CompactListGap))
            Row(horizontalArrangement = Arrangement.spacedBy(CompactListGap)) {
                if (item.requiresFocusTimer) {
                    Button(onClick = { onFocus(item) }, enabled = !busy, modifier = Modifier.height(CompactButtonHeight)) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Text(" Start")
                    }
                }
                OutlinedButton(onClick = { onHomework(item) }, enabled = !busy, modifier = Modifier.height(CompactButtonHeight)) {
                    Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = null)
                    Text(" Homework")
                }
            }
        }
    }
}
