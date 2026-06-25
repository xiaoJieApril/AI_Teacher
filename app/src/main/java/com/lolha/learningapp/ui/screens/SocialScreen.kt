package com.lolha.learningapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lolha.learningapp.data.local.SocialPostProofEntity
import com.lolha.learningapp.data.local.SocialPublishingAssignmentEntity
import com.lolha.learningapp.ui.components.EmptyState
import com.lolha.learningapp.ui.components.ErrorText
import com.lolha.learningapp.ui.components.TeacherThinkingCard
import com.lolha.learningapp.ui.state.MainUiState
import com.lolha.learningapp.ui.state.proofKey
import com.lolha.learningapp.ui.state.thinkingMessageOrNull

@Composable
fun SocialScreen(
    state: MainUiState,
    onCreateAssignment: () -> Unit,
    onProofInputChanged: (String, String) -> Unit,
    onSubmitProof: (SocialPublishingAssignmentEntity, String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Button(onClick = onCreateAssignment, enabled = !state.loading, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Image, contentDescription = null)
                Text(" Create Monthly Art Assignment")
            }
        }
        state.error?.let { error ->
            item { ErrorText(error) }
        }
        state.aiRequestState.thinkingMessageOrNull()?.let { message ->
            item { TeacherThinkingCard(message) }
        }
        if (state.socialAssignments.isEmpty() && !state.loading) {
            item {
                EmptyState(
                    title = "No social assignment",
                    body = "Create a monthly drawing post assignment, then submit X or Pixiv proof links.",
                )
            }
        }
        items(state.socialAssignments) { assignment ->
            SocialAssignmentCard(
                assignment = assignment,
                busy = state.loading,
                proofs = state.socialProofs.filter { it.assignmentRemoteId == assignment.remoteId },
                proofInputs = state.proofInputs,
                onProofInputChanged = onProofInputChanged,
                onSubmitProof = onSubmitProof,
            )
        }
    }
}

@Composable
private fun SocialAssignmentCard(
    assignment: SocialPublishingAssignmentEntity,
    busy: Boolean,
    proofs: List<SocialPostProofEntity>,
    proofInputs: Map<String, String>,
    onProofInputChanged: (String, String) -> Unit,
    onSubmitProof: (SocialPublishingAssignmentEntity, String) -> Unit,
) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(assignment.title, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text("Due ${assignment.dueDate} · ${assignment.status}")
            Text(assignment.description)
            if (assignment.artworkNotes.isNotBlank()) {
                Text(assignment.artworkNotes, color = Color(0xFF475569))
            }
            assignment.requiredPlatforms.split(",").map { it.trim() }.filter { it.isNotBlank() }.forEach { platform ->
                val key = proofKey(assignment.remoteId, platform)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = proofInputs[key].orEmpty(),
                        onValueChange = { onProofInputChanged(key, it) },
                        enabled = !busy,
                        modifier = Modifier.weight(1f),
                        label = { Text("$platform public URL") },
                        singleLine = true,
                    )
                    Button(
                        onClick = { onSubmitProof(assignment, platform) },
                        enabled = !busy && proofInputs[key].orEmpty().isNotBlank(),
                    ) {
                        Text("Submit")
                    }
                }
            }
            proofs.forEach { proof ->
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("${proof.platform} · ${proof.verificationStatus}", fontWeight = FontWeight.Bold)
                        Text(proof.url)
                        if (proof.aiFeedback.isNotBlank()) {
                            Spacer(Modifier.height(6.dp))
                            Text(proof.aiFeedback)
                        }
                    }
                }
            }
        }
    }
}

