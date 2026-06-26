package com.lolha.learningapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lolha.learningapp.data.local.HomeworkSubmissionEntity
import com.lolha.learningapp.ui.components.CompactCardPadding
import com.lolha.learningapp.ui.components.CompactListGap
import com.lolha.learningapp.ui.components.CompactPagePadding
import com.lolha.learningapp.ui.components.EmptyState
import com.lolha.learningapp.ui.components.FeedbackSection

@Composable
fun JournalScreen(submissions: List<HomeworkSubmissionEntity>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(CompactPagePadding),
        verticalArrangement = Arrangement.spacedBy(CompactListGap),
    ) {
        if (submissions.isEmpty()) {
            item {
                EmptyState(
                    title = "No graded work",
                    body = "Homework feedback will appear here after the teacher grades it.",
                )
            }
        }
        items(submissions) { submission ->
            Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(CompactCardPadding)) {
                    Text("${submission.subject} · Score ${submission.score}", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(submission.feedback)
                    FeedbackSection("Strengths", submission.strengths)
                    FeedbackSection("Problems", submission.problems)
                    FeedbackSection("Corrections", submission.corrections)
                    FeedbackSection("Next", submission.nextActionInstruction)
                }
            }
        }
    }
}
