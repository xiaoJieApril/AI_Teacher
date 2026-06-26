package com.lolha.learningapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
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
import com.lolha.learningapp.ui.components.CompactButtonHeight
import com.lolha.learningapp.ui.components.CompactCardPadding
import com.lolha.learningapp.ui.components.CompactDropdownField
import com.lolha.learningapp.ui.components.CompactListGap
import com.lolha.learningapp.ui.components.CompactPagePadding
import com.lolha.learningapp.ui.components.ErrorText
import com.lolha.learningapp.ui.state.MainUiState

private val WeekdayOptions = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
private val AvailabilityTypeOptions = listOf("work", "unavailable", "preferred")
private val TimeOptions = List(48) { index ->
    "%02d:%02d".format(index / 2, if (index % 2 == 0) 0 else 30)
}

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
        modifier = Modifier.padding(CompactPagePadding),
        verticalArrangement = Arrangement.spacedBy(CompactListGap),
    ) {
        state.error?.let { error ->
            item {
                ErrorText(error)
            }
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(CompactCardPadding), verticalArrangement = Arrangement.spacedBy(CompactListGap)) {
                    Text("Profile", fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
                    Button(onClick = onSaveProfile, modifier = Modifier.height(CompactButtonHeight)) {
                        Text("Save Profile")
                    }
                }
            }
        }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(CompactCardPadding), verticalArrangement = Arrangement.spacedBy(CompactListGap)) {
                    Text("Weekly Availability", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("work = No tasks allowed", color = Color(0xFFB45309), fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CompactDropdownField(
                            value = state.ruleWeekday,
                            options = WeekdayOptions,
                            onValueSelected = onRuleWeekdayChanged,
                            modifier = Modifier.weight(1f),
                            label = "Day",
                        )
                        CompactDropdownField(
                            value = state.ruleType,
                            options = AvailabilityTypeOptions,
                            onValueSelected = onRuleTypeChanged,
                            modifier = Modifier.weight(1f),
                            label = "Type",
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CompactDropdownField(
                            value = state.ruleStartTime,
                            options = TimeOptions,
                            onValueSelected = onRuleStartChanged,
                            modifier = Modifier.weight(1f),
                            label = "Start",
                        )
                        CompactDropdownField(
                            value = state.ruleEndTime,
                            options = TimeOptions,
                            onValueSelected = onRuleEndChanged,
                            modifier = Modifier.weight(1f),
                            label = "End",
                        )
                    }
                    OutlinedTextField(
                        value = state.ruleLabel,
                        onValueChange = onRuleLabelChanged,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Label") },
                        singleLine = true,
                    )
                    Button(onClick = onAddRule, modifier = Modifier.height(CompactButtonHeight)) {
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
                Column(modifier = Modifier.padding(CompactCardPadding), verticalArrangement = Arrangement.spacedBy(CompactListGap)) {
                    Text("Special Date", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = state.exceptionDate,
                            onValueChange = onExceptionDateChanged,
                            modifier = Modifier.weight(1f),
                            label = { Text("YYYY-MM-DD") },
                            singleLine = true,
                        )
                        CompactDropdownField(
                            value = state.exceptionType,
                            options = AvailabilityTypeOptions,
                            onValueSelected = onExceptionTypeChanged,
                            modifier = Modifier.weight(1f),
                            label = "Type",
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CompactDropdownField(
                            value = state.exceptionStartTime,
                            options = TimeOptions,
                            onValueSelected = onExceptionStartChanged,
                            modifier = Modifier.weight(1f),
                            label = "Start",
                        )
                        CompactDropdownField(
                            value = state.exceptionEndTime,
                            options = TimeOptions,
                            onValueSelected = onExceptionEndChanged,
                            modifier = Modifier.weight(1f),
                            label = "End",
                        )
                    }
                    OutlinedTextField(
                        value = state.exceptionLabel,
                        onValueChange = onExceptionLabelChanged,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Label") },
                        singleLine = true,
                    )
                    Button(onClick = onAddException, modifier = Modifier.height(CompactButtonHeight)) {
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
            modifier = Modifier.padding(CompactCardPadding),
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
            modifier = Modifier.padding(CompactCardPadding),
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
