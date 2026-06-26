package com.lolha.learningapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lolha.learningapp.data.local.ChatMessageEntity
import com.lolha.learningapp.ui.components.CompactButtonHeight
import com.lolha.learningapp.ui.components.CompactCardPadding
import com.lolha.learningapp.ui.components.CompactListGap
import com.lolha.learningapp.ui.components.CompactPagePadding
import com.lolha.learningapp.ui.components.EmptyState
import com.lolha.learningapp.ui.components.ErrorText
import com.lolha.learningapp.ui.components.SyncWarningText
import com.lolha.learningapp.ui.components.TeacherThinkingCard
import com.lolha.learningapp.ui.state.MainUiState
import com.lolha.learningapp.ui.state.thinkingMessageOrNull

@Composable
fun ChatScreen(
    state: MainUiState,
    onInputChanged: (String) -> Unit,
    onPickImage: () -> Unit,
    onTakePhoto: () -> Unit,
    onSpeechInput: () -> Unit,
    onClearAttachment: () -> Unit,
    onSend: () -> Unit,
    onDeleteChatMessage: (String) -> Unit,
    onClearChat: () -> Unit,
    onDailyTask: () -> Unit,
    onDailySchedule: () -> Unit,
    onWeeklySchedule: () -> Unit,
) {
    val listState = rememberLazyListState()
    LaunchedEffect(state.messages.size, state.loading) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.lastIndex)
        }
    }

    Column(modifier = Modifier.padding(CompactPagePadding)) {
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(CompactListGap),
        ) {
            if (state.messages.isEmpty() && !state.loading) {
                item {
                    EmptyState(
                        title = "Ready for class",
                        body = "Ask for today's task or submit homework text for grading.",
                    )
                }
            }
            items(state.messages) { message ->
                ChatBubble(message = message, onDelete = onDeleteChatMessage)
            }
            state.aiRequestState.thinkingMessageOrNull()?.let { message ->
                item {
                    TeacherThinkingCard(message)
                }
            }
        }

        state.error?.let {
            ErrorText(it, modifier = Modifier.padding(bottom = CompactListGap))
        }
        state.syncError?.let {
            SyncWarningText(it, modifier = Modifier.padding(bottom = CompactListGap))
        }

        state.attachmentLabel?.let { label ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Default.Image, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(label, modifier = Modifier.weight(1f).padding(start = 8.dp))
                IconButton(onClick = onClearAttachment, enabled = !state.loading) {
                    Icon(Icons.Default.Close, contentDescription = "Remove image")
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(CompactListGap),
        ) {
            OutlinedButton(onClick = onDailyTask, enabled = !state.loading, modifier = Modifier.height(CompactButtonHeight)) {
                Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = "Daily task")
                Text(" Task")
            }
            OutlinedButton(onClick = onDailySchedule, enabled = !state.loading, modifier = Modifier.height(CompactButtonHeight)) {
                Text("Today")
            }
            OutlinedButton(onClick = onWeeklySchedule, enabled = !state.loading, modifier = Modifier.height(CompactButtonHeight)) {
                Text("Week")
            }
            OutlinedButton(onClick = onPickImage, enabled = !state.loading, modifier = Modifier.height(CompactButtonHeight)) {
                Icon(Icons.Default.Image, contentDescription = "Attach image")
            }
            OutlinedButton(onClick = onTakePhoto, enabled = !state.loading, modifier = Modifier.height(CompactButtonHeight)) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Take photo")
            }
            OutlinedButton(onClick = onClearChat, enabled = !state.loading, modifier = Modifier.height(CompactButtonHeight)) {
                Icon(Icons.Default.Delete, contentDescription = "Clear chat")
            }
        }

        Spacer(Modifier.height(CompactListGap))

        Row(horizontalArrangement = Arrangement.spacedBy(CompactListGap)) {
            OutlinedTextField(
                value = state.input,
                onValueChange = onInputChanged,
                enabled = !state.loading,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Submit work or ask the teacher") },
                singleLine = true,
            )
            OutlinedButton(onClick = onSpeechInput, enabled = !state.loading, modifier = Modifier.height(CompactButtonHeight)) {
                Icon(Icons.Default.Mic, contentDescription = "Speech input")
            }
            Button(onClick = onSend, enabled = !state.loading, modifier = Modifier.height(CompactButtonHeight)) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
            }
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessageEntity, onDelete: (String) -> Unit) {
    val isTeacher = message.role == "teacher"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isTeacher) Arrangement.Start else Arrangement.End,
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isTeacher) Color.White else Color(0xFFE0F2FE),
            ),
            modifier = Modifier.fillMaxWidth(0.86f),
        ) {
            Column(modifier = Modifier.padding(CompactCardPadding)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (isTeacher) "Teacher" else "You",
                        modifier = Modifier.weight(1f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    IconButton(onClick = { onDelete(message.remoteId) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete message")
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(message.message)
                if (message.score != "N/A") {
                    Spacer(Modifier.height(6.dp))
                    Text("Score: ${message.score}", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
