package com.lolha.learningapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lolha.learningapp.data.LearningRepository
import com.lolha.learningapp.data.RepositoryResult
import com.lolha.learningapp.data.local.AvailabilityExceptionEntity
import com.lolha.learningapp.data.local.AvailabilityRuleEntity
import com.lolha.learningapp.data.local.FocusSessionEntity
import com.lolha.learningapp.data.local.LearningTaskEntity
import com.lolha.learningapp.data.local.ScheduleItemEntity
import com.lolha.learningapp.data.local.SocialPublishingAssignmentEntity
import com.lolha.learningapp.data.local.UserProfileEntity
import com.lolha.learningapp.domain.AvailabilityValidator
import com.lolha.learningapp.ui.state.AiRequestKind
import com.lolha.learningapp.ui.state.AiRequestState
import com.lolha.learningapp.ui.state.AppTab
import com.lolha.learningapp.ui.state.MainUiState
import com.lolha.learningapp.ui.state.ScheduleMode
import com.lolha.learningapp.ui.state.proofKey
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

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
        viewModelScope.launch {
            repository.observeHomeworkDrafts().collectLatest { drafts ->
                val activeRemoteId = uiState.value.activeDraft?.remoteId
                val activeDraft = drafts.firstOrNull { it.remoteId == activeRemoteId } ?: uiState.value.activeDraft
                uiState.value = uiState.value.copy(drafts = drafts, activeDraft = activeDraft)
            }
        }
        viewModelScope.launch {
            repository.observeProfile().collectLatest { profile ->
                val value = profile ?: UserProfileEntity()
                uiState.value = uiState.value.copy(
                    profile = value,
                    profileNickname = value.nickname,
                    profileGoal = value.learningGoal,
                    profileTimezone = value.timezone,
                )
            }
        }
        viewModelScope.launch {
            repository.observeAvailabilityRules().collectLatest { rules ->
                uiState.value = uiState.value.copy(availabilityRules = rules)
            }
        }
        viewModelScope.launch {
            repository.observeAvailabilityExceptions().collectLatest { exceptions ->
                uiState.value = uiState.value.copy(availabilityExceptions = exceptions)
            }
        }
        viewModelScope.launch {
            repository.observeSocialAssignments().collectLatest { assignments ->
                uiState.value = uiState.value.copy(socialAssignments = assignments)
            }
        }
        viewModelScope.launch {
            repository.observeSocialProofs().collectLatest { proofs ->
                uiState.value = uiState.value.copy(socialProofs = proofs)
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
        if (uiState.value.loading) return
        sendTeacherRequest(
            kind = AiRequestKind.DailyTask,
            thinkingMessage = "AI teacher is preparing today's task...",
            visibleMessage = "請幫我佈置今日任務",
            modelPrompt = "請根據我目前的日文、英文、畫畫與健身訓練，佈置今日任務。每個任務都要有完成標準、建議時間與下一步行動。",
        )
    }

    fun requestDailySchedule() {
        if (uiState.value.loading) return
        uiState.value = uiState.value.copy(selectedTab = AppTab.Schedule, scheduleMode = ScheduleMode.Today)
        viewModelScope.launch {
            beginAiRequest(AiRequestKind.DailySchedule, "AI teacher is building today's schedule...")
            try {
                applySyncResult(repository.requestDailySchedule())
            } catch (error: Throwable) {
                failAiRequest(error.message ?: "AI request failed")
            } finally {
                endAiRequest()
            }
        }
    }

    fun requestWeeklySchedule() {
        if (uiState.value.loading) return
        uiState.value = uiState.value.copy(selectedTab = AppTab.Schedule, scheduleMode = ScheduleMode.Week)
        viewModelScope.launch {
            beginAiRequest(AiRequestKind.WeeklySchedule, "AI teacher is building this week's schedule...")
            try {
                applySyncResult(repository.requestWeeklySchedule())
            } catch (error: Throwable) {
                failAiRequest(error.message ?: "AI request failed")
            } finally {
                endAiRequest()
            }
        }
    }

    fun sendCurrentInput() {
        if (uiState.value.loading) return
        val input = uiState.value.input.trim().ifEmpty {
            if (attachedImageBase64 != null) "請批改這份圖片作業。" else ""
        }
        if (input.isEmpty()) return
        uiState.value = uiState.value.copy(input = "")
        sendTeacherRequest(
            kind = AiRequestKind.Chat,
            thinkingMessage = "AI teacher is thinking...",
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

    fun openHomeworkForSchedule(item: ScheduleItemEntity) {
        viewModelScope.launch {
            val draft = repository.openDraftForSchedule(item)
            uiState.value = uiState.value.copy(
                selectedTab = AppTab.Homework,
                activeDraft = draft,
                draftText = draft.draftText,
                syncError = null,
            )
        }
    }

    fun openHomeworkForTask(task: LearningTaskEntity) {
        viewModelScope.launch {
            val draft = repository.openDraftForTask(task)
            uiState.value = uiState.value.copy(
                selectedTab = AppTab.Homework,
                activeDraft = draft,
                draftText = draft.draftText,
                syncError = null,
            )
        }
    }

    fun updateDraftText(text: String) {
        uiState.value = uiState.value.copy(draftText = text)
    }

    fun saveActiveDraft() {
        val draft = uiState.value.activeDraft ?: return
        viewModelScope.launch {
            applySyncResult(repository.saveDraft(draft.remoteId, uiState.value.draftText))
        }
    }

    fun submitActiveDraft() {
        if (uiState.value.loading) return
        val draft = uiState.value.activeDraft ?: return
        viewModelScope.launch {
            beginAiRequest(AiRequestKind.HomeworkGrading, "AI teacher is grading your homework...")
            try {
                applySyncResult(repository.submitDraft(draft.remoteId, uiState.value.draftText))
                uiState.value = uiState.value.copy(selectedTab = AppTab.Journal)
            } catch (error: Throwable) {
                failAiRequest(error.message ?: "AI grading failed")
            } finally {
                endAiRequest()
            }
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

    fun updateProfileNickname(value: String) {
        uiState.value = uiState.value.copy(profileNickname = value)
    }

    fun updateProfileGoal(value: String) {
        uiState.value = uiState.value.copy(profileGoal = value)
    }

    fun updateProfileTimezone(value: String) {
        uiState.value = uiState.value.copy(profileTimezone = value)
    }

    fun saveProfile() {
        val current = uiState.value
        viewModelScope.launch {
            applySyncResult(
                repository.saveProfile(
                    current.profile.copy(
                        nickname = current.profileNickname.trim(),
                        learningGoal = current.profileGoal.trim(),
                        timezone = current.profileTimezone.trim().ifBlank { "Asia/Kuala_Lumpur" },
                    ),
                ),
            )
        }
    }

    fun updateRuleWeekday(value: String) {
        uiState.value = uiState.value.copy(ruleWeekday = value)
    }

    fun updateRuleStartTime(value: String) {
        uiState.value = uiState.value.copy(ruleStartTime = value)
    }

    fun updateRuleEndTime(value: String) {
        uiState.value = uiState.value.copy(ruleEndTime = value)
    }

    fun updateRuleLabel(value: String) {
        uiState.value = uiState.value.copy(ruleLabel = value)
    }

    fun updateRuleType(value: String) {
        uiState.value = uiState.value.copy(ruleType = value)
    }

    fun addAvailabilityRule() {
        val current = uiState.value
        val validationError = AvailabilityValidator.validateWeeklyRule(
            weekday = current.ruleWeekday,
            startTime = current.ruleStartTime,
            endTime = current.ruleEndTime,
        )
        if (validationError != null) {
            uiState.value = current.copy(error = validationError)
            return
        }
        viewModelScope.launch {
            applySyncResult(
                repository.addAvailabilityRule(
                    AvailabilityRuleEntity(
                        weekday = current.ruleWeekday.trim().ifBlank { "Mon" },
                        startTime = current.ruleStartTime.trim().ifBlank { "09:00" },
                        endTime = current.ruleEndTime.trim().ifBlank { "18:00" },
                        label = current.ruleLabel.trim().ifBlank { "Work" },
                        ruleType = current.ruleType.trim().ifBlank { "work" },
                    ),
                ),
            )
        }
    }

    fun deleteAvailabilityRule(remoteId: String) {
        viewModelScope.launch {
            applySyncResult(repository.deleteAvailabilityRule(remoteId))
        }
    }

    fun updateExceptionDate(value: String) {
        uiState.value = uiState.value.copy(exceptionDate = value)
    }

    fun updateExceptionStartTime(value: String) {
        uiState.value = uiState.value.copy(exceptionStartTime = value)
    }

    fun updateExceptionEndTime(value: String) {
        uiState.value = uiState.value.copy(exceptionEndTime = value)
    }

    fun updateExceptionLabel(value: String) {
        uiState.value = uiState.value.copy(exceptionLabel = value)
    }

    fun updateExceptionType(value: String) {
        uiState.value = uiState.value.copy(exceptionType = value)
    }

    fun addAvailabilityException() {
        val current = uiState.value
        if (current.exceptionDate.isBlank()) {
            uiState.value = current.copy(error = "Date must use YYYY-MM-DD format.")
            return
        }
        val validationError = AvailabilityValidator.validateException(
            date = current.exceptionDate,
            startTime = current.exceptionStartTime,
            endTime = current.exceptionEndTime,
        )
        if (validationError != null) {
            uiState.value = current.copy(error = validationError)
            return
        }
        viewModelScope.launch {
            applySyncResult(
                repository.addAvailabilityException(
                    AvailabilityExceptionEntity(
                        date = current.exceptionDate.trim(),
                        startTime = current.exceptionStartTime.trim().ifBlank { "09:00" },
                        endTime = current.exceptionEndTime.trim().ifBlank { "18:00" },
                        label = current.exceptionLabel.trim().ifBlank { "Unavailable" },
                        ruleType = current.exceptionType.trim().ifBlank { "unavailable" },
                    ),
                ),
            )
        }
    }

    fun deleteAvailabilityException(remoteId: String) {
        viewModelScope.launch {
            applySyncResult(repository.deleteAvailabilityException(remoteId))
        }
    }

    fun createMonthlySocialAssignment() {
        viewModelScope.launch {
            applySyncResult(repository.createMonthlySocialAssignment())
        }
    }

    fun updateProofInput(key: String, value: String) {
        uiState.value = uiState.value.copy(proofInputs = uiState.value.proofInputs + (key to value))
    }

    fun submitSocialProof(assignment: SocialPublishingAssignmentEntity, platform: String) {
        if (uiState.value.loading) return
        val key = proofKey(assignment.remoteId, platform)
        val url = uiState.value.proofInputs[key].orEmpty()
        if (url.isBlank()) {
            uiState.value = uiState.value.copy(error = "Please paste a public $platform URL first.")
            return
        }
        viewModelScope.launch {
            beginAiRequest(AiRequestKind.SocialProof, "AI teacher is verifying your public proof link...")
            try {
                applySyncResult(repository.submitSocialProof(assignment, platform, url))
                uiState.value = uiState.value.copy(proofInputs = uiState.value.proofInputs - key)
            } catch (error: Throwable) {
                failAiRequest(error.message ?: "Proof verification failed")
            } finally {
                endAiRequest()
            }
        }
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
        kind: AiRequestKind,
        thinkingMessage: String,
        visibleMessage: String,
        modelPrompt: String,
        imageBase64: String? = null,
        imageMimeType: String? = null,
    ) {
        viewModelScope.launch {
            beginAiRequest(kind, thinkingMessage)
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
                failAiRequest(error.message ?: "AI request failed")
            } finally {
                endAiRequest()
            }
        }
    }

    private fun beginAiRequest(kind: AiRequestKind, message: String) {
        uiState.value = uiState.value.copy(
            aiRequestState = AiRequestState.Thinking(kind, message),
            error = null,
            syncError = null,
        )
    }

    private fun failAiRequest(message: String) {
        uiState.value = uiState.value.copy(
            aiRequestState = AiRequestState.Failed(message),
            error = message,
        )
    }

    private fun endAiRequest() {
        if (uiState.value.aiRequestState is AiRequestState.Thinking) {
            uiState.value = uiState.value.copy(aiRequestState = AiRequestState.Idle)
        }
    }

    private fun applySyncResult(result: RepositoryResult) {
        uiState.value = uiState.value.copy(syncError = result.syncError)
    }
}
