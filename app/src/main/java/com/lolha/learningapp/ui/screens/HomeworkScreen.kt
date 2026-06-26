package com.lolha.learningapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import com.lolha.learningapp.data.local.HomeworkDraftEntity
import com.lolha.learningapp.ui.components.CompactButtonHeight
import com.lolha.learningapp.ui.components.CompactCardPadding
import com.lolha.learningapp.ui.components.CompactListGap
import com.lolha.learningapp.ui.components.CompactPagePadding
import com.lolha.learningapp.ui.components.EmptyState
import com.lolha.learningapp.ui.components.ErrorText
import com.lolha.learningapp.ui.components.TeacherThinkingCard
import com.lolha.learningapp.ui.state.AiRequestState
import com.lolha.learningapp.ui.state.isThinking
import com.lolha.learningapp.ui.state.thinkingMessageOrNull

@Composable
fun HomeworkScreen(
    activeDraft: HomeworkDraftEntity?,
    draftText: String,
    aiRequestState: AiRequestState,
    error: String?,
    onDraftTextChanged: (String) -> Unit,
    onSaveDraft: () -> Unit,
    onSubmitDraft: () -> Unit,
) {
    if (activeDraft == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(CompactPagePadding),
        ) {
            EmptyState(
                title = "No homework open",
                body = "Open a schedule item or task, then tap Homework.",
            )
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(CompactPagePadding),
        verticalArrangement = Arrangement.spacedBy(CompactListGap),
    ) {
        Text(activeDraft.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text("${activeDraft.subject} · ${activeDraft.status}")
        Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(modifier = Modifier.padding(CompactCardPadding)) {
                Text("Task", fontWeight = FontWeight.Bold)
                Text(activeDraft.prompt)
                if (activeDraft.completionStandard.isNotBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text("Completion", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(activeDraft.completionStandard)
                }
            }
        }
        aiRequestState.thinkingMessageOrNull()?.let { message ->
            TeacherThinkingCard(message)
        }
        OutlinedTextField(
            value = draftText,
            onValueChange = onDraftTextChanged,
            enabled = !aiRequestState.isThinking,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            placeholder = { Text("Write your Japanese or English homework here") },
        )
        error?.let {
            ErrorText(it)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = onSaveDraft,
                enabled = !aiRequestState.isThinking,
                modifier = Modifier.weight(1f).height(CompactButtonHeight),
            ) {
                Text("Save Draft")
            }
            Button(
                onClick = onSubmitDraft,
                enabled = !aiRequestState.isThinking && draftText.isNotBlank(),
                modifier = Modifier.weight(1f).height(CompactButtonHeight),
            ) {
                Text("Submit to AI")
            }
        }
    }
}
