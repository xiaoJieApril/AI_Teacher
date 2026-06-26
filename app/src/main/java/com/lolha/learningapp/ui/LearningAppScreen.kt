package com.lolha.learningapp.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.lolha.learningapp.data.local.LearningTaskEntity
import com.lolha.learningapp.data.local.ScheduleItemEntity
import com.lolha.learningapp.data.local.SocialPublishingAssignmentEntity
import com.lolha.learningapp.ui.screens.ChatScreen
import com.lolha.learningapp.ui.screens.FocusScreen
import com.lolha.learningapp.ui.screens.HomeworkScreen
import com.lolha.learningapp.ui.screens.JournalScreen
import com.lolha.learningapp.ui.screens.MaterialsScreen
import com.lolha.learningapp.ui.screens.MoreScreen
import com.lolha.learningapp.ui.screens.ProfileScreen
import com.lolha.learningapp.ui.screens.ScheduleScreen
import com.lolha.learningapp.ui.screens.SocialScreen
import com.lolha.learningapp.ui.screens.TasksScreen
import com.lolha.learningapp.ui.state.AppTab
import com.lolha.learningapp.ui.state.MainUiState
import com.lolha.learningapp.ui.state.ScheduleMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearningAppScreen(
    state: MainUiState,
    onTabSelected: (AppTab) -> Unit,
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
    onScheduleModeSelected: (ScheduleMode) -> Unit,
    onScheduleDone: (ScheduleItemEntity) -> Unit,
    onScheduleDelete: (ScheduleItemEntity, String, String) -> Unit,
    onScheduleFocus: (ScheduleItemEntity) -> Unit,
    onScheduleHomework: (ScheduleItemEntity) -> Unit,
    onTaskDone: (Long) -> Unit,
    onTaskHomework: (LearningTaskEntity) -> Unit,
    onTaskDelete: (LearningTaskEntity, String, String) -> Unit,
    onDraftTextChanged: (String) -> Unit,
    onSaveDraft: () -> Unit,
    onSubmitDraft: () -> Unit,
    onProfileNicknameChanged: (String) -> Unit,
    onProfileGoalChanged: (String) -> Unit,
    onProfileTimezoneChanged: (String) -> Unit,
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
    onCreateSocialAssignment: () -> Unit,
    onProofInputChanged: (String, String) -> Unit,
    onSubmitProof: (SocialPublishingAssignmentEntity, String) -> Unit,
    onFocusMinutesChanged: (Int) -> Unit,
    onStartFocus: () -> Unit,
    onStopFocus: () -> Unit,
    onOpenUrl: (String) -> Unit,
) {
    val primaryTabs = listOf(AppTab.Chat, AppTab.Schedule, AppTab.Tasks, AppTab.Homework, AppTab.More)
    val selectedBottomTab = if (state.selectedTab in primaryTabs.dropLast(1)) state.selectedTab else AppTab.More

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("AI Teacher", fontWeight = FontWeight.Bold)
                        Text("Strict practice for language and drawing", fontSize = 12.sp)
                    }
                },
                actions = {
                    IconButton(onClick = onDailyTask, enabled = !state.loading) {
                        Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = "Daily task")
                    }
                },
            )
        },
        bottomBar = {
            NavigationBar {
                primaryTabs.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedBottomTab == tab,
                        onClick = { onTabSelected(tab) },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                    )
                }
            }
        },
    ) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            color = MaterialTheme.colorScheme.background,
        ) {
            when (state.selectedTab) {
                AppTab.Chat -> ChatScreen(
                    state = state,
                    onInputChanged = onInputChanged,
                    onPickImage = onPickImage,
                    onTakePhoto = onTakePhoto,
                    onSpeechInput = onSpeechInput,
                    onClearAttachment = onClearAttachment,
                    onSend = onSend,
                    onDeleteChatMessage = onDeleteChatMessage,
                    onClearChat = onClearChat,
                    onDailyTask = onDailyTask,
                    onDailySchedule = onDailySchedule,
                    onWeeklySchedule = onWeeklySchedule,
                )
                AppTab.Schedule -> ScheduleScreen(
                    state = state,
                    onModeSelected = onScheduleModeSelected,
                    onDailySchedule = onDailySchedule,
                    onWeeklySchedule = onWeeklySchedule,
                    onDone = onScheduleDone,
                    onDelete = onScheduleDelete,
                    onFocus = onScheduleFocus,
                    onHomework = onScheduleHomework,
                    onOpenUrl = onOpenUrl,
                )
                AppTab.Tasks -> TasksScreen(state.tasks, onTaskDone, onTaskHomework, onTaskDelete, onOpenUrl)
                AppTab.Homework -> HomeworkScreen(
                    activeDraft = state.activeDraft,
                    draftText = state.draftText,
                    aiRequestState = state.aiRequestState,
                    error = state.error,
                    onDraftTextChanged = onDraftTextChanged,
                    onSaveDraft = onSaveDraft,
                    onSubmitDraft = onSubmitDraft,
                )
                AppTab.Focus -> FocusScreen(state, onFocusMinutesChanged, onStartFocus, onStopFocus)
                AppTab.Journal -> JournalScreen(state.submissions)
                AppTab.Profile -> ProfileScreen(
                    state = state,
                    onNicknameChanged = onProfileNicknameChanged,
                    onGoalChanged = onProfileGoalChanged,
                    onTimezoneChanged = onProfileTimezoneChanged,
                    onSaveProfile = onSaveProfile,
                    onRuleWeekdayChanged = onRuleWeekdayChanged,
                    onRuleStartChanged = onRuleStartChanged,
                    onRuleEndChanged = onRuleEndChanged,
                    onRuleLabelChanged = onRuleLabelChanged,
                    onRuleTypeChanged = onRuleTypeChanged,
                    onAddRule = onAddRule,
                    onDeleteRule = onDeleteRule,
                    onExceptionDateChanged = onExceptionDateChanged,
                    onExceptionStartChanged = onExceptionStartChanged,
                    onExceptionEndChanged = onExceptionEndChanged,
                    onExceptionLabelChanged = onExceptionLabelChanged,
                    onExceptionTypeChanged = onExceptionTypeChanged,
                    onAddException = onAddException,
                    onDeleteException = onDeleteException,
                )
                AppTab.Social -> SocialScreen(
                    state = state,
                    onCreateAssignment = onCreateSocialAssignment,
                    onProofInputChanged = onProofInputChanged,
                    onSubmitProof = onSubmitProof,
                )
                AppTab.Materials -> MaterialsScreen(onOpenUrl = onOpenUrl)
                AppTab.More -> MoreScreen(
                    selectedTab = state.selectedTab,
                    onTabSelected = onTabSelected,
                )
            }
        }
    }
}

private val AppTab.label: String
    get() = when (this) {
        AppTab.Chat -> "Chat"
        AppTab.Schedule -> "Schedule"
        AppTab.Tasks -> "Tasks"
        AppTab.Homework -> "Homework"
        AppTab.Focus -> "Focus"
        AppTab.Journal -> "Journal"
        AppTab.Profile -> "Profile"
        AppTab.Social -> "Social"
        AppTab.Materials -> "Materials"
        AppTab.More -> "More"
    }

private val AppTab.icon: ImageVector
    get() = when (this) {
        AppTab.Chat -> Icons.AutoMirrored.Filled.Chat
        AppTab.Schedule -> Icons.Default.Timer
        AppTab.Tasks -> Icons.AutoMirrored.Filled.Assignment
        AppTab.Homework -> Icons.AutoMirrored.Filled.Assignment
        AppTab.Focus -> Icons.Default.Timer
        AppTab.Journal -> Icons.Default.History
        AppTab.Profile -> Icons.Default.Person
        AppTab.Social -> Icons.Default.Image
        AppTab.Materials -> Icons.AutoMirrored.Filled.MenuBook
        AppTab.More -> Icons.Default.MoreHoriz
    }
