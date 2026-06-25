package com.lolha.learningapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lolha.learningapp.data.local.AvailabilityExceptionEntity
import com.lolha.learningapp.data.local.AvailabilityRuleEntity
import com.lolha.learningapp.ui.components.ErrorText
import com.lolha.learningapp.ui.state.MainUiState

@Composable
fun ProfileScreen(
    state: MainUiState,
    onNicknameChanged: (String) -> Unit,
    onGoalChanged: (String) -> Unit,
    onTimezoneChanged: (String) -> Unit,
    onSaveProfile: () -> Unit,
    onRuleWeekdayChanged: (String) -> Unit,
    onRuleStartChanged: (String) -> Unit,
    onRuleEndChanged: (String) -> Unit,
    onRuleLabelChanged: (String) -> Unit,
    onRuleTypeChanged: (String) -> Unit,
    onAddRule: () -> Unit,
    onDeleteRule: (String) -> Unit,
    onExceptionDateChanged: (String) -> Unit,
    onExceptionStartChanged: (String) -> Unit,
    onExceptionEndChanged: (String) -> Unit,
    onExceptionLabelChanged: (String) -> Unit,
    onExceptionTypeChanged: (String) -> Unit,
    onAddException: () -> Unit,
    onDeleteException: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        state.error?.let { error ->
            item {
                ErrorText(error)
            }
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Profile", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    OutlinedTextField(
                        value = state.profileNickname,
                        onValueChange = onNicknameChanged,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Nickname") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = state.profileGoal,
                        onValueChange = onGoalChanged,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Learning goal") },
                    )
                    OutlinedTextField(
                        value = state.profileTimezone,
                        onValueChange = onTimezoneChanged,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Timezone") },
                        singleLine = true,
                    )
                    Button(onClick = onSaveProfile) {
                        Text("Save Profile")
                    }
                }
            }
        }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Weekly Availability", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text("work = No tasks allowed", color = Color(0xFFB45309), fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = state.ruleWeekday,
                            onValueChange = onRuleWeekdayChanged,
                            modifier = Modifier.weight(1f),
                            label = { Text("Day") },
                            singleLine = true,
                        )
                        OutlinedTextField(
                            value = state.ruleType,
                            onValueChange = onRuleTypeChanged,
                            modifier = Modifier.weight(1f),
                            label = { Text("Type") },
                            singleLine = true,
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = state.ruleStartTime,
                            onValueChange = onRuleStartChanged,
                            modifier = Modifier.weight(1f),
                            label = { Text("Start") },
                            singleLine = true,
                        )
                        OutlinedTextField(
                            value = state.ruleEndTime,
                            onValueChange = onRuleEndChanged,
                            modifier = Modifier.weight(1f),
                            label = { Text("End") },
                            singleLine = true,
                        )
                    }
                    OutlinedTextField(
                        value = state.ruleLabel,
                        onValueChange = onRuleLabelChanged,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Label") },
                        singleLine = true,
                    )
                    Button(onClick = onAddRule) {
                        Text("Add Weekly Rule")
                    }
                }
            }
        }

        items(state.availabilityRules) { rule ->
            AvailabilityRuleRow(rule = rule, onDelete = onDeleteRule)
        }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Special Date", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = state.exceptionDate,
                            onValueChange = onExceptionDateChanged,
                            modifier = Modifier.weight(1f),
                            label = { Text("YYYY-MM-DD") },
                            singleLine = true,
                        )
                        OutlinedTextField(
                            value = state.exceptionType,
                            onValueChange = onExceptionTypeChanged,
                            modifier = Modifier.weight(1f),
                            label = { Text("Type") },
                            singleLine = true,
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = state.exceptionStartTime,
                            onValueChange = onExceptionStartChanged,
                            modifier = Modifier.weight(1f),
                            label = { Text("Start") },
                            singleLine = true,
                        )
                        OutlinedTextField(
                            value = state.exceptionEndTime,
                            onValueChange = onExceptionEndChanged,
                            modifier = Modifier.weight(1f),
                            label = { Text("End") },
                            singleLine = true,
                        )
                    }
                    OutlinedTextField(
                        value = state.exceptionLabel,
                        onValueChange = onExceptionLabelChanged,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Label") },
                        singleLine = true,
                    )
                    Button(onClick = onAddException) {
                        Text("Add Special Date")
                    }
                }
            }
        }

        items(state.availabilityExceptions) { exception ->
            AvailabilityExceptionRow(exception = exception, onDelete = onDeleteException)
        }
    }
}

@Composable
private fun AvailabilityRuleRow(rule: AvailabilityRuleEntity, onDelete: (String) -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("${rule.weekday} ${rule.startTime}-${rule.endTime}", fontWeight = FontWeight.Bold)
                Text("${rule.ruleType} · ${rule.label}")
            }
            IconButton(onClick = { onDelete(rule.remoteId) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete rule")
            }
        }
    }
}

@Composable
private fun AvailabilityExceptionRow(exception: AvailabilityExceptionEntity, onDelete: (String) -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("${exception.date} ${exception.startTime}-${exception.endTime}", fontWeight = FontWeight.Bold)
                Text("${exception.ruleType} · ${exception.label}")
            }
            IconButton(onClick = { onDelete(exception.remoteId) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete exception")
            }
        }
    }
}

