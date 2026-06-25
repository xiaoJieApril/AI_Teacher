package com.lolha.learningapp.ui.state

import com.lolha.learningapp.data.local.AvailabilityExceptionEntity
import com.lolha.learningapp.data.local.AvailabilityRuleEntity
import com.lolha.learningapp.data.local.ChatMessageEntity
import com.lolha.learningapp.data.local.HomeworkDraftEntity
import com.lolha.learningapp.data.local.HomeworkSubmissionEntity
import com.lolha.learningapp.data.local.LearningTaskEntity
import com.lolha.learningapp.data.local.ScheduleItemEntity
import com.lolha.learningapp.data.local.SocialPostProofEntity
import com.lolha.learningapp.data.local.SocialPublishingAssignmentEntity
import com.lolha.learningapp.data.local.UserProfileEntity

data class MainUiState(
    val selectedTab: AppTab = AppTab.Chat,
    val scheduleMode: ScheduleMode = ScheduleMode.Today,
    val messages: List<ChatMessageEntity> = emptyList(),
    val scheduleItems: List<ScheduleItemEntity> = emptyList(),
    val tasks: List<LearningTaskEntity> = emptyList(),
    val drafts: List<HomeworkDraftEntity> = emptyList(),
    val activeDraft: HomeworkDraftEntity? = null,
    val draftText: String = "",
    val submissions: List<HomeworkSubmissionEntity> = emptyList(),
    val profile: UserProfileEntity = UserProfileEntity(),
    val availabilityRules: List<AvailabilityRuleEntity> = emptyList(),
    val availabilityExceptions: List<AvailabilityExceptionEntity> = emptyList(),
    val socialAssignments: List<SocialPublishingAssignmentEntity> = emptyList(),
    val socialProofs: List<SocialPostProofEntity> = emptyList(),
    val profileNickname: String = "",
    val profileGoal: String = "",
    val profileTimezone: String = "Asia/Kuala_Lumpur",
    val ruleWeekday: String = "Mon",
    val ruleStartTime: String = "09:00",
    val ruleEndTime: String = "18:00",
    val ruleLabel: String = "Work",
    val ruleType: String = "work",
    val exceptionDate: String = "",
    val exceptionStartTime: String = "09:00",
    val exceptionEndTime: String = "18:00",
    val exceptionLabel: String = "Unavailable",
    val exceptionType: String = "unavailable",
    val proofInputs: Map<String, String> = emptyMap(),
    val input: String = "",
    val focusMinutes: Int = 45,
    val remainingSeconds: Int = 45 * 60,
    val focusRunning: Boolean = false,
    val attachmentLabel: String? = null,
    val aiRequestState: AiRequestState = AiRequestState.Idle,
    val error: String? = null,
    val syncError: String? = null,
) {
    val loading: Boolean
        get() = aiRequestState.isThinking
}

fun proofKey(assignmentRemoteId: String, platform: String): String = "$assignmentRemoteId:$platform"

