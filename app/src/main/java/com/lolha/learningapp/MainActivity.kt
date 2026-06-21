package com.lolha.learningapp

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Base64
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lolha.learningapp.data.local.ChatMessageEntity
import com.lolha.learningapp.data.local.AvailabilityExceptionEntity
import com.lolha.learningapp.data.local.AvailabilityRuleEntity
import com.lolha.learningapp.data.local.HomeworkDraftEntity
import com.lolha.learningapp.data.local.HomeworkSubmissionEntity
import com.lolha.learningapp.data.local.LearningTaskEntity
import com.lolha.learningapp.data.local.ScheduleItemEntity
import com.lolha.learningapp.data.local.SocialPostProofEntity
import com.lolha.learningapp.data.local.SocialPublishingAssignmentEntity
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.util.Locale
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pickImage = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            uri?.let(::attachImageForGrading)
        }
        val takePhoto = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            bitmap?.let(::attachBitmapForGrading)
        }
        val speechInput = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val spoken = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
                .orEmpty()
            if (spoken.isNotBlank()) {
                val current = viewModel.uiState.value.input
                viewModel.updateInput(listOf(current, spoken).filter { it.isNotBlank() }.joinToString(" "))
            }
        }
        setContent {
            LearningTheme {
                LearningAppScreen(
                    state = viewModel.uiState.value,
                    onTabSelected = viewModel::selectTab,
                    onInputChanged = viewModel::updateInput,
                    onPickImage = {
                        pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    onTakePhoto = { takePhoto.launch(null) },
                    onSpeechInput = {
                        speechInput.launch(
                            Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your answer")
                            },
                        )
                    },
                    onClearAttachment = viewModel::clearAttachment,
                    onSend = viewModel::sendCurrentInput,
                    onDeleteChatMessage = viewModel::deleteChatMessage,
                    onClearChat = viewModel::clearChat,
                    onDailyTask = viewModel::requestDailyTask,
                    onDailySchedule = viewModel::requestDailySchedule,
                    onWeeklySchedule = viewModel::requestWeeklySchedule,
                    onScheduleModeSelected = viewModel::selectScheduleMode,
                    onScheduleDone = viewModel::markScheduleDone,
                    onScheduleDelete = viewModel::deleteScheduleItem,
                    onScheduleFocus = viewModel::startScheduleFocus,
                    onScheduleHomework = viewModel::openHomeworkForSchedule,
                    onTaskDone = viewModel::markTaskDone,
                    onTaskHomework = viewModel::openHomeworkForTask,
                    onDraftTextChanged = viewModel::updateDraftText,
                    onSaveDraft = viewModel::saveActiveDraft,
                    onSubmitDraft = viewModel::submitActiveDraft,
                    onProfileNicknameChanged = viewModel::updateProfileNickname,
                    onProfileGoalChanged = viewModel::updateProfileGoal,
                    onProfileTimezoneChanged = viewModel::updateProfileTimezone,
                    onSaveProfile = viewModel::saveProfile,
                    onRuleWeekdayChanged = viewModel::updateRuleWeekday,
                    onRuleStartChanged = viewModel::updateRuleStartTime,
                    onRuleEndChanged = viewModel::updateRuleEndTime,
                    onRuleLabelChanged = viewModel::updateRuleLabel,
                    onRuleTypeChanged = viewModel::updateRuleType,
                    onAddRule = viewModel::addAvailabilityRule,
                    onDeleteRule = viewModel::deleteAvailabilityRule,
                    onExceptionDateChanged = viewModel::updateExceptionDate,
                    onExceptionStartChanged = viewModel::updateExceptionStartTime,
                    onExceptionEndChanged = viewModel::updateExceptionEndTime,
                    onExceptionLabelChanged = viewModel::updateExceptionLabel,
                    onExceptionTypeChanged = viewModel::updateExceptionType,
                    onAddException = viewModel::addAvailabilityException,
                    onDeleteException = viewModel::deleteAvailabilityException,
                    onCreateSocialAssignment = viewModel::createMonthlySocialAssignment,
                    onProofInputChanged = viewModel::updateProofInput,
                    onSubmitProof = viewModel::submitSocialProof,
                    onFocusMinutesChanged = viewModel::setFocusMinutes,
                    onStartFocus = {
                        requestScreenPinning()
                        viewModel.startFocus()
                    },
                    onStopFocus = viewModel::stopFocus,
                )
            }
        }
    }

    private fun requestScreenPinning() {
        try {
            (this as Activity).startLockTask()
        } catch (_: IllegalStateException) {
            // Device policy or system settings may prevent pinning.
        }
    }

    private fun attachImageForGrading(uri: Uri) {
        val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
        val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return
        val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
        viewModel.attachImage(base64, mimeType, "Image homework attached")
    }

    private fun attachBitmapForGrading(bitmap: Bitmap) {
        val bytes = ByteArrayOutputStream().use { output ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, output)
            output.toByteArray()
        }
        val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
        viewModel.attachImage(base64, "image/jpeg", "Camera homework attached")
    }
}

@Composable
private fun LearningTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF0F766E),
            secondary = Color(0xFF334155),
            tertiary = Color(0xFFB45309),
            background = Color(0xFFF8FAFC),
            surface = Color.White,
            onPrimary = Color.White,
            onSecondary = Color.White,
            onSurface = Color(0xFF0F172A),
        ),
        typography = MaterialTheme.typography,
        content = content,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LearningAppScreen(
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
    onScheduleDelete: (String) -> Unit,
    onScheduleFocus: (ScheduleItemEntity) -> Unit,
    onScheduleHomework: (ScheduleItemEntity) -> Unit,
    onTaskDone: (Long) -> Unit,
    onTaskHomework: (LearningTaskEntity) -> Unit,
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
) {
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
                    IconButton(onClick = onDailyTask) {
                        Icon(Icons.Default.Assignment, contentDescription = "Daily task")
                    }
                },
            )
        },
        bottomBar = {
            NavigationBar {
                AppTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = state.selectedTab == tab,
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
                )
                AppTab.Tasks -> TasksScreen(state.tasks, onTaskDone, onTaskHomework)
                AppTab.Homework -> HomeworkScreen(
                    activeDraft = state.activeDraft,
                    draftText = state.draftText,
                    loading = state.loading,
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
    }

private val AppTab.icon: ImageVector
    get() = when (this) {
        AppTab.Chat -> Icons.Default.Chat
        AppTab.Schedule -> Icons.Default.Timer
        AppTab.Tasks -> Icons.Default.Assignment
        AppTab.Homework -> Icons.Default.Assignment
        AppTab.Focus -> Icons.Default.Timer
        AppTab.Journal -> Icons.Default.History
        AppTab.Profile -> Icons.Default.Person
        AppTab.Social -> Icons.Default.Image
    }

@Composable
private fun ChatScreen(
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
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.lastIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (state.messages.isEmpty()) {
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
        }

        state.error?.let {
            Text(it, color = Color(0xFFB91C1C), modifier = Modifier.padding(bottom = 8.dp))
        }
        state.syncError?.let {
            Text("Sync warning: $it", color = Color(0xFFB45309), modifier = Modifier.padding(bottom = 8.dp))
        }

        state.attachmentLabel?.let { label ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Default.Image, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(label, modifier = Modifier.weight(1f).padding(start = 8.dp))
                IconButton(onClick = onClearAttachment) {
                    Icon(Icons.Default.Close, contentDescription = "Remove image")
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onDailyTask, modifier = Modifier.height(56.dp)) {
                Icon(Icons.Default.Assignment, contentDescription = null)
            }
            OutlinedButton(onClick = onPickImage, modifier = Modifier.height(56.dp)) {
                Icon(Icons.Default.Image, contentDescription = "Attach image")
            }
            OutlinedButton(onClick = onTakePhoto, modifier = Modifier.height(56.dp)) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Take photo")
            }
            OutlinedButton(onClick = onClearChat, modifier = Modifier.height(56.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Clear chat")
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onDailySchedule, modifier = Modifier.height(48.dp)) {
                Text("Today")
            }
            OutlinedButton(onClick = onWeeklySchedule, modifier = Modifier.height(48.dp)) {
                Text("Week")
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = state.input,
                onValueChange = onInputChanged,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Submit work or ask the teacher") },
                singleLine = true,
            )
            OutlinedButton(onClick = onSpeechInput, modifier = Modifier.height(56.dp)) {
                Icon(Icons.Default.Mic, contentDescription = "Speech input")
            }
            Button(onClick = onSend, enabled = !state.loading, modifier = Modifier.height(56.dp)) {
                Icon(Icons.Default.Send, contentDescription = "Send")
            }
        }
    }
}

@Composable
private fun ScheduleScreen(
    state: MainUiState,
    onModeSelected: (ScheduleMode) -> Unit,
    onDailySchedule: () -> Unit,
    onWeeklySchedule: () -> Unit,
    onDone: (ScheduleItemEntity) -> Unit,
    onDelete: (String) -> Unit,
    onFocus: (ScheduleItemEntity) -> Unit,
    onHomework: (ScheduleItemEntity) -> Unit,
) {
    val today = LocalDate.now().toString()
    val visibleItems = when (state.scheduleMode) {
        ScheduleMode.Today -> state.scheduleItems.filter { it.date == today }
        ScheduleMode.Week -> state.scheduleItems
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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

        Spacer(Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onDailySchedule, modifier = Modifier.weight(1f)) {
                Text("Generate Today")
            }
            OutlinedButton(onClick = onWeeklySchedule, modifier = Modifier.weight(1f)) {
                Text("Generate Week")
            }
        }

        Spacer(Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (visibleItems.isEmpty()) {
                item {
                    EmptyState(
                        title = "No schedule yet",
                        body = "Ask the teacher for today's or this week's schedule.",
                    )
                }
            }

            val groupedItems = visibleItems.groupBy { it.date }
            groupedItems.forEach { (date, itemsForDate) ->
                item {
                    Text(date, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                items(itemsForDate) { item ->
                    ScheduleCard(
                        item = item,
                        onDone = onDone,
                        onDelete = onDelete,
                        onFocus = onFocus,
                        onHomework = onHomework,
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
    onDone: (ScheduleItemEntity) -> Unit,
    onDelete: (String) -> Unit,
    onFocus: (ScheduleItemEntity) -> Unit,
    onHomework: (ScheduleItemEntity) -> Unit,
) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("${item.startTime}-${item.endTime}", fontWeight = FontWeight.Bold)
                    Text(item.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("${item.subject} · ${item.suggestedMinutes} min · ${item.status}")
                }
                if (item.status != "done") {
                    IconButton(onClick = { onDone(item) }) {
                        Icon(Icons.Default.Check, contentDescription = "Mark done")
                    }
                }
                IconButton(onClick = { onDelete(item.remoteId) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete schedule")
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(item.description)
            if (item.completionStandard.isNotBlank()) {
                Spacer(Modifier.height(10.dp))
                Text("Completion", fontWeight = FontWeight.Bold)
                Text(item.completionStandard)
            }
            if (item.requiresFocusTimer) {
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { onFocus(item) }) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Text(" Start")
                    }
                    OutlinedButton(onClick = { onHomework(item) }) {
                        Icon(Icons.Default.Assignment, contentDescription = null)
                        Text(" Homework")
                    }
                }
            } else {
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = { onHomework(item) }) {
                    Icon(Icons.Default.Assignment, contentDescription = null)
                    Text(" Homework")
                }
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
            Column(modifier = Modifier.padding(14.dp)) {
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

@Composable
private fun TasksScreen(
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
                        Icon(Icons.Default.Assignment, contentDescription = null)
                        Text(" Homework")
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeworkScreen(
    activeDraft: HomeworkDraftEntity?,
    draftText: String,
    loading: Boolean,
    error: String?,
    onDraftTextChanged: (String) -> Unit,
    onSaveDraft: () -> Unit,
    onSubmitDraft: () -> Unit,
) {
    if (activeDraft == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
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
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(activeDraft.title, fontWeight = FontWeight.Bold, fontSize = 22.sp)
        Text("${activeDraft.subject} · ${activeDraft.status}")
        Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Task", fontWeight = FontWeight.Bold)
                Text(activeDraft.prompt)
                if (activeDraft.completionStandard.isNotBlank()) {
                    Spacer(Modifier.height(10.dp))
                    Text("Completion", fontWeight = FontWeight.Bold)
                    Text(activeDraft.completionStandard)
                }
            }
        }
        OutlinedTextField(
            value = draftText,
            onValueChange = onDraftTextChanged,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            placeholder = { Text("Write your Japanese or English homework here") },
        )
        error?.let {
            Text(it, color = Color(0xFFB91C1C))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onSaveDraft, modifier = Modifier.weight(1f)) {
                Text("Save Draft")
            }
            Button(onClick = onSubmitDraft, enabled = !loading && draftText.isNotBlank(), modifier = Modifier.weight(1f)) {
                Text("Submit to AI")
            }
        }
    }
}

@Composable
private fun ProfileScreen(
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
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
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

@Composable
private fun SocialScreen(
    state: MainUiState,
    onCreateAssignment: () -> Unit,
    onProofInputChanged: (String, String) -> Unit,
    onSubmitProof: (SocialPublishingAssignmentEntity, String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Button(onClick = onCreateAssignment, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Image, contentDescription = null)
                Text(" Create Monthly Art Assignment")
            }
        }
        if (state.socialAssignments.isEmpty()) {
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
                        modifier = Modifier.weight(1f),
                        label = { Text("$platform public URL") },
                        singleLine = true,
                    )
                    Button(onClick = { onSubmitProof(assignment, platform) }) {
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

@Composable
private fun FocusScreen(
    state: MainUiState,
    onMinutesChanged: (Int) -> Unit,
    onStart: () -> Unit,
    onStop: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(16.dp))
        Text(formatSeconds(state.remainingSeconds), fontSize = 56.sp, fontWeight = FontWeight.Bold)
        Text("Focus session with Android screen pinning")
        Spacer(Modifier.height(28.dp))
        Text("${state.focusMinutes} minutes", fontWeight = FontWeight.Bold)
        Slider(
            value = state.focusMinutes.toFloat(),
            onValueChange = { onMinutesChanged(it.roundToInt()) },
            valueRange = 5f..180f,
            steps = 34,
            enabled = !state.focusRunning,
        )
        Spacer(Modifier.height(16.dp))
        if (state.focusRunning) {
            Button(onClick = onStop) {
                Icon(Icons.Default.Stop, contentDescription = null)
                Text(" Stop")
            }
        } else {
            Button(onClick = onStart) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Text(" Start focus")
            }
        }
    }
}

@Composable
private fun JournalScreen(submissions: List<HomeworkSubmissionEntity>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
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
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("${submission.subject} · Score ${submission.score}", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
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

@Composable
private fun FeedbackSection(title: String, text: String) {
    if (text.isBlank()) return
    Spacer(Modifier.height(10.dp))
    Text(title, fontWeight = FontWeight.Bold)
    val lines = text.lines().filter { it.isNotBlank() }
    lines.forEach { item ->
        Text("- $item")
    }
}

@Composable
private fun EmptyState(title: String, body: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 36.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Spacer(Modifier.height(8.dp))
            Text(body, color = Color(0xFF475569))
        }
    }
}

private fun formatSeconds(seconds: Int): String {
    val minutes = seconds / 60
    val remaining = seconds % 60
    return "%02d:%02d".format(minutes, remaining)
}
