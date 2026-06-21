package com.lolha.learningapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lolha.learningapp.data.LearningRepository
import com.lolha.learningapp.data.local.ChatMessageEntity
import com.lolha.learningapp.data.local.FocusSessionEntity
import com.lolha.learningapp.data.local.HomeworkSubmissionEntity
import com.lolha.learningapp.data.local.LearningTaskEntity
import com.lolha.learningapp.data.local.ScheduleItemEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

enum class AppTab {
    Chat,
    Schedule,
    Tasks,
    Focus,
    Journal,
}

enum class ScheduleMode {
    Today,
    Week,
}

data class MainUiState(
    val selectedTab: AppTab = AppTab.Chat,
    val scheduleMode: ScheduleMode = ScheduleMode.Today,
    val messages: List<ChatMessageEntity> = emptyList(),
    val scheduleItems: List<ScheduleItemEntity> = emptyList(),
    val tasks: List<LearningTaskEntity> = emptyList(),
    val submissions: List<HomeworkSubmissionEntity> = emptyList(),
    val input: String = "",
    val focusMinutes: Int = 45,
    val remainingSeconds: Int = 45 * 60,
    val focusRunning: Boolean = false,
    val attachmentLabel: String? = null,
    val loading: Boolean = false,
    val error: String? = null,
    val syncError: String? = null,
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = LearningRepository(
        context = application,
        dao = (application as LearningApp).database.learningDao(),
    )
    private var timerJob: Job? = null
    private var plannedFocusSeconds: Int = 45 * 60
    private var attachedImageBase64: String? = null
    private var attachedImageMimeType: String? = null

    var uiState = androidx.compose.runtime.mutableStateOf(MainUiState())
        private set

    init {
        viewModelScope.launch {
            repository.observeMessages().collectLatest { messages ->
                uiState.value = uiState.value.copy(messages = messages)
            }
        }
        viewModelScope.launch {
            repository.observeTasks().collectLatest { tasks ->
                uiState.value = uiState.value.copy(tasks = tasks)
            }
        }
        viewModelScope.launch {
            repository.observeSubmissions().collectLatest { submissions ->
                uiState.value = uiState.value.copy(submissions = submissions)
            }
        }
        viewModelScope.launch {
            repository.observeScheduleItems().collectLatest { scheduleItems ->
                uiState.value = uiState.value.copy(scheduleItems = scheduleItems)
            }
        }
    }

    fun selectTab(tab: AppTab) {
        uiState.value = uiState.value.copy(selectedTab = tab)
    }

    fun updateInput(input: String) {
        uiState.value = uiState.value.copy(input = input)
    }

    fun attachImage(base64: String, mimeType: String, label: String) {
        attachedImageBase64 = base64
        attachedImageMimeType = mimeType
        uiState.value = uiState.value.copy(attachmentLabel = label)
    }

    fun clearAttachment() {
        attachedImageBase64 = null
        attachedImageMimeType = null
        uiState.value = uiState.value.copy(attachmentLabel = null)
    }

    fun requestDailyTask() {
        sendTeacherRequest(
            visibleMessage = "請幫我佈置今日任務",
            modelPrompt = "請根據我目前的日文、英文、畫畫與健身訓練，佈置今日任務。每個任務都要有完成標準、建議時間與下一步行動。",
        )
    }

    fun requestDailySchedule() {
        uiState.value = uiState.value.copy(selectedTab = AppTab.Schedule, scheduleMode = ScheduleMode.Today)
        viewModelScope.launch {
            uiState.value = uiState.value.copy(loading = true, error = null, syncError = null)
            try {
                applySyncResult(repository.requestDailySchedule())
            } catch (error: Throwable) {
                uiState.value = uiState.value.copy(error = error.message ?: "AI request failed")
            } finally {
                uiState.value = uiState.value.copy(loading = false)
            }
        }
    }

    fun requestWeeklySchedule() {
        uiState.value = uiState.value.copy(selectedTab = AppTab.Schedule, scheduleMode = ScheduleMode.Week)
        viewModelScope.launch {
            uiState.value = uiState.value.copy(loading = true, error = null, syncError = null)
            try {
                applySyncResult(repository.requestWeeklySchedule())
            } catch (error: Throwable) {
                uiState.value = uiState.value.copy(error = error.message ?: "AI request failed")
            } finally {
                uiState.value = uiState.value.copy(loading = false)
            }
        }
    }

    fun sendCurrentInput() {
        val input = uiState.value.input.trim().ifEmpty {
            if (attachedImageBase64 != null) "請批改這份圖片作業。" else ""
        }
        if (input.isEmpty()) return
        uiState.value = uiState.value.copy(input = "")
        sendTeacherRequest(
            visibleMessage = input,
            modelPrompt = input,
            imageBase64 = attachedImageBase64,
            imageMimeType = attachedImageMimeType,
        )
        clearAttachment()
    }

    fun markTaskDone(taskId: Long) {
        viewModelScope.launch {
            applySyncResult(repository.markTaskDone(taskId))
        }
    }

    fun markScheduleDone(item: ScheduleItemEntity) {
        viewModelScope.launch {
            applySyncResult(repository.markScheduleDone(item))
        }
    }

    fun deleteChatMessage(remoteId: String) {
        viewModelScope.launch {
            applySyncResult(repository.deleteChatMessage(remoteId))
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            applySyncResult(repository.clearChat())
        }
    }

    fun deleteScheduleItem(remoteId: String) {
        viewModelScope.launch {
            applySyncResult(repository.deleteScheduleItem(remoteId))
        }
    }

    fun selectScheduleMode(mode: ScheduleMode) {
        uiState.value = uiState.value.copy(scheduleMode = mode, selectedTab = AppTab.Schedule)
    }

    fun startScheduleFocus(item: ScheduleItemEntity) {
        setFocusMinutes(item.suggestedMinutes)
        startFocus()
    }

    fun setFocusMinutes(minutes: Int) {
        val clamped = minutes.coerceIn(5, 180)
        plannedFocusSeconds = clamped * 60
        uiState.value = uiState.value.copy(
            focusMinutes = clamped,
            remainingSeconds = plannedFocusSeconds,
        )
    }

    fun startFocus() {
        timerJob?.cancel()
        plannedFocusSeconds = uiState.value.focusMinutes * 60
        uiState.value = uiState.value.copy(
            remainingSeconds = plannedFocusSeconds,
            focusRunning = true,
            selectedTab = AppTab.Focus,
        )
        timerJob = viewModelScope.launch {
            while (uiState.value.remainingSeconds > 0 && uiState.value.focusRunning) {
                delay(1_000)
                uiState.value = uiState.value.copy(
                    remainingSeconds = (uiState.value.remainingSeconds - 1).coerceAtLeast(0),
                )
            }
            if (uiState.value.remainingSeconds == 0) {
                applySyncResult(repository.insertFocusSession(
                    FocusSessionEntity(
                        plannedMinutes = uiState.value.focusMinutes,
                        completedSeconds = plannedFocusSeconds,
                        completed = true,
                    ),
                ))
                uiState.value = uiState.value.copy(focusRunning = false)
            }
        }
    }

    fun stopFocus() {
        timerJob?.cancel()
        val completed = plannedFocusSeconds - uiState.value.remainingSeconds
        viewModelScope.launch {
            applySyncResult(repository.insertFocusSession(
                FocusSessionEntity(
                    plannedMinutes = uiState.value.focusMinutes,
                    completedSeconds = completed.coerceAtLeast(0),
                    completed = false,
                ),
            ))
        }
        uiState.value = uiState.value.copy(focusRunning = false)
    }

    private fun sendTeacherRequest(
        visibleMessage: String,
        modelPrompt: String,
        imageBase64: String? = null,
        imageMimeType: String? = null,
    ) {
        viewModelScope.launch {
            uiState.value = uiState.value.copy(loading = true, error = null, syncError = null)
            try {
                applySyncResult(
                    repository.sendChatMessage(
                        visibleMessage = visibleMessage,
                        modelPrompt = modelPrompt,
                        imageBase64 = imageBase64,
                        imageMimeType = imageMimeType,
                    ),
                )
            } catch (error: Throwable) {
                uiState.value = uiState.value.copy(error = error.message ?: "AI request failed")
            } finally {
                uiState.value = uiState.value.copy(loading = false)
            }
        }
    }

    private fun applySyncResult(result: com.lolha.learningapp.data.RepositoryResult) {
        uiState.value = uiState.value.copy(syncError = result.syncError)
    }
}
