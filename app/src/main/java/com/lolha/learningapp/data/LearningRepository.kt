package com.lolha.learningapp.data

import android.content.Context
import com.lolha.learningapp.data.ai.GeminiTeacherClient
import com.lolha.learningapp.data.ai.TeacherResponse
import com.lolha.learningapp.data.local.AvailabilityExceptionEntity
import com.lolha.learningapp.data.local.AvailabilityRuleEntity
import com.lolha.learningapp.data.local.ChatMessageEntity
import com.lolha.learningapp.data.local.FocusSessionEntity
import com.lolha.learningapp.data.local.HomeworkDraftEntity
import com.lolha.learningapp.data.local.HomeworkSubmissionEntity
import com.lolha.learningapp.data.local.LearningDao
import com.lolha.learningapp.data.local.LearningTaskEntity
import com.lolha.learningapp.data.local.ScheduleItemEntity
import com.lolha.learningapp.data.local.SocialPostProofEntity
import com.lolha.learningapp.data.local.SocialPublishingAssignmentEntity
import com.lolha.learningapp.data.local.UserProfileEntity
import com.lolha.learningapp.data.remote.SupabaseSyncClient
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import org.json.JSONObject

data class RepositoryResult(val syncError: String? = null)

class LearningRepository(
    context: Context,
    private val dao: LearningDao,
    private val teacherClient: GeminiTeacherClient = GeminiTeacherClient(),
) {
    private val deviceId = context.getSharedPreferences("learning_app", Context.MODE_PRIVATE)
        .getString("device_id", null)
        ?: UUID.randomUUID().toString().also { generated ->
            context.getSharedPreferences("learning_app", Context.MODE_PRIVATE)
                .edit()
                .putString("device_id", generated)
                .apply()
        }
    private val supabase = SupabaseSyncClient(deviceId)

    fun observeMessages(): Flow<List<ChatMessageEntity>> = dao.observeMessages()
    fun observeTasks(): Flow<List<LearningTaskEntity>> = dao.observeTasks()
    fun observeSubmissions(): Flow<List<HomeworkSubmissionEntity>> = dao.observeSubmissions()
    fun observeScheduleItems(): Flow<List<ScheduleItemEntity>> = dao.observeScheduleItems()
    fun observeHomeworkDrafts(): Flow<List<HomeworkDraftEntity>> = dao.observeHomeworkDrafts()
    fun observeProfile(): Flow<UserProfileEntity?> = dao.observeProfile()
    fun observeAvailabilityRules(): Flow<List<AvailabilityRuleEntity>> = dao.observeAvailabilityRules()
    fun observeAvailabilityExceptions(): Flow<List<AvailabilityExceptionEntity>> = dao.observeAvailabilityExceptions()
    fun observeSocialAssignments(): Flow<List<SocialPublishingAssignmentEntity>> = dao.observeSocialAssignments()
    fun observeSocialProofs(): Flow<List<SocialPostProofEntity>> = dao.observeSocialProofs()

    suspend fun sendChatMessage(
        visibleMessage: String,
        modelPrompt: String,
        imageBase64: String? = null,
        imageMimeType: String? = null,
    ): RepositoryResult {
        var syncError: String? = null
        val studentMessage = ChatMessageEntity(role = "student", message = visibleMessage)
        dao.insertMessage(studentMessage)
        syncError = syncError.combine(syncChatMessage(studentMessage))

        val response = teacherClient.askTeacher(modelPrompt, imageBase64, imageMimeType)
        syncError = syncError.combine(persistTeacherResponse(modelPrompt, response))
        return RepositoryResult(syncError)
    }

    suspend fun requestDailySchedule(): RepositoryResult {
        val today = LocalDate.now().toString()
        return sendChatMessage(
            visibleMessage = "請幫我安排今日時間表",
            modelPrompt = """
                請為 $today 建立今日時間表，包含日文、英文、畫畫與健身。
                請平衡學習、訓練與休息，輸出 create_schedule 和 schedule_items。
                ${availabilityContext()}
            """.trimIndent(),
        )
    }

    suspend fun requestWeeklySchedule(): RepositoryResult {
        val today = LocalDate.now()
        val endDate = today.plusDays(6)
        return sendChatMessage(
            visibleMessage = "請幫我安排本週時間表",
            modelPrompt = """
                請為 ${today} 到 ${endDate} 建立 7 天本週時間表，包含日文、英文、畫畫與健身。
                請平衡強度、休息與提交成果，輸出 create_schedule 和 schedule_items。
                ${availabilityContext()}
            """.trimIndent(),
        )
    }

    suspend fun openDraftForSchedule(item: ScheduleItemEntity): HomeworkDraftEntity {
        val existing = dao.getDraftForSource("schedule", item.remoteId)
        if (existing != null) return existing
        val draft = HomeworkDraftEntity(
            sourceType = "schedule",
            sourceRemoteId = item.remoteId,
            subject = item.subject,
            title = item.title,
            prompt = item.description,
            completionStandard = item.completionStandard,
        )
        dao.insertDraft(draft)
        syncDraft(draft)
        return draft
    }

    suspend fun openDraftForTask(task: LearningTaskEntity): HomeworkDraftEntity {
        val existing = dao.getDraftForSource("task", task.remoteId)
        if (existing != null) return existing
        val draft = HomeworkDraftEntity(
            sourceType = "task",
            sourceRemoteId = task.remoteId,
            subject = task.subject,
            title = task.title,
            prompt = task.description,
            completionStandard = task.completionStandard,
        )
        dao.insertDraft(draft)
        syncDraft(draft)
        return draft
    }

    suspend fun saveDraft(remoteId: String, draftText: String): RepositoryResult {
        val updatedAt = System.currentTimeMillis()
        dao.updateDraftText(remoteId, draftText, updatedAt)
        val updated = dao.getDraft(remoteId)
        return RepositoryResult(updated?.let { syncDraft(it) })
    }

    suspend fun submitDraft(remoteId: String, draftText: String): RepositoryResult {
        var syncError = saveDraft(remoteId, draftText).syncError
        val draft = dao.getDraft(remoteId) ?: return RepositoryResult(syncError)
        val prompt = """
            請批改以下作業。
            科目：${draft.subject}
            作業標題：${draft.title}
            作業要求：${draft.prompt}
            完成標準：${draft.completionStandard}
            學生作答：
            ${draft.draftText}

            請輸出 action_type=grade_homework，並給出 score、strengths、problems、corrections、next_action。
        """.trimIndent()
        val response = teacherClient.askTeacher(prompt)
        syncError = syncError.combine(persistTeacherResponse(prompt, response))
        dao.updateDraftStatus(remoteId, "submitted", System.currentTimeMillis())
        dao.getDraft(remoteId)?.let { syncError = syncError.combine(syncDraft(it)) }
        return RepositoryResult(syncError)
    }

    suspend fun saveProfile(profile: UserProfileEntity): RepositoryResult {
        val updated = profile.copy(updatedAt = System.currentTimeMillis())
        dao.insertProfile(updated)
        return RepositoryResult(syncProfile(updated))
    }

    suspend fun addAvailabilityRule(rule: AvailabilityRuleEntity): RepositoryResult {
        dao.insertAvailabilityRule(rule)
        return RepositoryResult(syncAvailabilityRule(rule))
    }

    suspend fun deleteAvailabilityRule(remoteId: String): RepositoryResult {
        dao.deleteAvailabilityRule(remoteId)
        return RepositoryResult(syncDelete("availability_rules", remoteId))
    }

    suspend fun addAvailabilityException(exception: AvailabilityExceptionEntity): RepositoryResult {
        dao.insertAvailabilityException(exception)
        return RepositoryResult(syncAvailabilityException(exception))
    }

    suspend fun deleteAvailabilityException(remoteId: String): RepositoryResult {
        dao.deleteAvailabilityException(remoteId)
        return RepositoryResult(syncDelete("availability_exceptions", remoteId))
    }

    suspend fun createMonthlySocialAssignment(): RepositoryResult {
        val month = YearMonth.now()
        val assignment = SocialPublishingAssignmentEntity(
            title = "${month} 畫畫社群發佈任務",
            description = "完成一張本月代表作品，發佈到 X/Twitter 與 Pixiv，並回來提交公開作品連結。",
            month = month.toString(),
            dueDate = month.atEndOfMonth().toString(),
            requiredPlatforms = "X,Pixiv",
            artworkNotes = "AI 老師可將本任務拆成草稿、線稿、上色、發佈與 proof 驗證階段。",
        )
        dao.insertSocialAssignment(assignment)
        return RepositoryResult(syncSocialAssignment(assignment))
    }

    suspend fun submitSocialProof(
        assignment: SocialPublishingAssignmentEntity,
        platform: String,
        url: String,
    ): RepositoryResult {
        val trimmedUrl = url.trim()
        val platformMatches = when (platform.lowercase()) {
            "x" -> trimmedUrl.contains("x.com") || trimmedUrl.contains("twitter.com")
            "pixiv" -> trimmedUrl.contains("pixiv.net")
            else -> false
        }
        val prompt = """
            請檢查這個畫畫社群發佈 proof 是否符合任務。
            任務：${assignment.title}
            任務要求：${assignment.description}
            平台：$platform
            使用者提交連結：$trimmedUrl
            注意：App 第一版不登入社群帳號，也不抓私人資料。若連結無法公開檢查，請要求學生補充截圖或作品圖片。
            請用簡短 teacher_dialogue 回覆，指出 verified 或 needs_fix 的理由。
        """.trimIndent()
        val response = teacherClient.askTeacher(prompt)
        val status = if (platformMatches && !response.teacherDialogue.contains("needs_fix", ignoreCase = true)) {
            "verified"
        } else {
            "needs_fix"
        }
        val proof = SocialPostProofEntity(
            assignmentRemoteId = assignment.remoteId,
            platform = platform,
            url = trimmedUrl,
            verificationStatus = status,
            aiFeedback = response.teacherDialogue,
        )
        dao.insertSocialProof(proof)
        return RepositoryResult(syncSocialProof(proof))
    }

    suspend fun deleteChatMessage(remoteId: String): RepositoryResult {
        dao.deleteChatMessage(remoteId)
        return RepositoryResult(syncDelete("chat_messages", remoteId))
    }

    suspend fun clearChat(): RepositoryResult {
        dao.clearChatMessages()
        return RepositoryResult(
            supabase.deleteByDevice("chat_messages").exceptionOrNull()?.message,
        )
    }

    suspend fun deleteScheduleItem(remoteId: String): RepositoryResult {
        dao.deleteScheduleItem(remoteId)
        return RepositoryResult(syncDelete("schedule_items", remoteId))
    }

    suspend fun markScheduleDone(item: ScheduleItemEntity): RepositoryResult {
        val updated = item.copy(status = "done")
        dao.updateScheduleStatus(item.id, updated.status)
        return RepositoryResult(syncScheduleItem(updated))
    }

    suspend fun markTaskDone(taskId: Long): RepositoryResult {
        dao.updateTaskStatus(taskId, "done")
        val updated = dao.getTask(taskId)?.copy(status = "done")
        return RepositoryResult(updated?.let { syncTask(it) })
    }

    suspend fun insertFocusSession(session: FocusSessionEntity): RepositoryResult {
        dao.insertFocusSession(session)
        return RepositoryResult(syncFocusSession(session))
    }

    private suspend fun availabilityContext(): String {
        val profile = dao.getProfile()
        val rules = dao.getAvailabilityRules()
        val exceptions = dao.getAvailabilityExceptions()
        return buildString {
            appendLine("Profile availability rules:")
            appendLine("- Timezone: ${profile?.timezone ?: "Asia/Kuala_Lumpur"}")
            if (profile?.learningGoal?.isNotBlank() == true) {
                appendLine("- Student goals: ${profile.learningGoal}")
            }
            appendLine("- work 時段是硬性禁止，絕對不可安排任何 schedule item。")
            appendLine("- unavailable/preferred 是強偏好，盡量避開；若真的需要使用，必須在 teacher_dialogue 說明原因。")
            if (rules.isEmpty()) {
                appendLine("- Weekly rules: none")
            } else {
                appendLine("- Weekly rules:")
                rules.forEach { rule ->
                    appendLine("  ${rule.weekday} ${rule.startTime}-${rule.endTime} ${rule.ruleType} ${rule.label}")
                }
            }
            if (exceptions.isEmpty()) {
                appendLine("- Date exceptions: none")
            } else {
                appendLine("- Date exceptions:")
                exceptions.forEach { exception ->
                    appendLine("  ${exception.date} ${exception.startTime}-${exception.endTime} ${exception.ruleType} ${exception.label}")
                }
            }
        }
    }

    private suspend fun persistTeacherResponse(prompt: String, response: TeacherResponse): String? {
        var syncError: String? = null
        // One teacher JSON response can fan out into chat, task, schedule, and journal rows.
        val teacherMessage = ChatMessageEntity(
            role = "teacher",
            message = response.teacherDialogue,
            actionType = response.actionType,
            subject = response.subject,
            score = response.score,
        )
        dao.insertMessage(teacherMessage)
        syncError = syncError.combine(syncChatMessage(teacherMessage))

        if (response.actionType == "assign_task" && response.taskDetails.title.isNotBlank()) {
            val task = LearningTaskEntity(
                title = response.taskDetails.title,
                description = response.taskDetails.description,
                subject = response.subject,
                suggestedMinutes = response.taskDetails.suggestedMinutes,
                completionStandard = response.taskDetails.completionStandard,
                nextActionType = response.nextAction.type,
                nextActionInstruction = response.nextAction.instruction,
            )
            dao.insertTask(task)
            syncError = syncError.combine(syncTask(task))
        }

        if (response.actionType == "create_schedule" && response.scheduleItems.isNotEmpty()) {
            val items = response.scheduleItems.map { item ->
                ScheduleItemEntity(
                    date = item.date,
                    startTime = item.startTime,
                    endTime = item.endTime,
                    subject = item.subject,
                    title = item.title,
                    description = item.description,
                    suggestedMinutes = item.suggestedMinutes,
                    completionStandard = item.completionStandard,
                    requiresFocusTimer = item.requiresFocusTimer,
                )
            }
            val dates = items.map { it.date }.distinct()
            // Schedule generation owns the requested dates, so stale items for those dates are replaced.
            dao.getScheduleItemsForDates(dates).forEach { oldItem ->
                syncError = syncError.combine(syncDelete("schedule_items", oldItem.remoteId))
            }
            dao.deleteScheduleItemsForDates(dates)
            dao.insertScheduleItems(items)
            items.forEach { item ->
                syncError = syncError.combine(syncScheduleItem(item))
            }
        }

        if (response.actionType == "grade_homework") {
            val submission = HomeworkSubmissionEntity(
                subject = response.subject,
                prompt = prompt,
                feedback = response.teacherDialogue,
                score = response.score,
                strengths = response.grading.strengths.joinToString("\n"),
                problems = response.grading.problems.joinToString("\n"),
                corrections = response.grading.corrections.joinToString("\n"),
                nextActionInstruction = response.nextAction.instruction,
            )
            dao.insertSubmission(submission)
            syncError = syncError.combine(syncSubmission(submission))
        }
        return syncError
    }

    private suspend fun syncChatMessage(message: ChatMessageEntity): String? =
        supabase.upsert(
            "chat_messages",
            JSONObject()
                .put("remote_id", message.remoteId)
                .put("role", message.role)
                .put("message", message.message)
                .put("action_type", message.actionType)
                .put("subject", message.subject)
                .put("score", message.score)
                .put("created_at", message.createdAt),
        ).exceptionOrNull()?.message

    private suspend fun syncTask(task: LearningTaskEntity): String? =
        supabase.upsert(
            "learning_tasks",
            JSONObject()
                .put("remote_id", task.remoteId)
                .put("title", task.title)
                .put("description", task.description)
                .put("subject", task.subject)
                .put("suggested_minutes", task.suggestedMinutes)
                .put("completion_standard", task.completionStandard)
                .put("next_action_type", task.nextActionType)
                .put("next_action_instruction", task.nextActionInstruction)
                .put("status", task.status)
                .put("created_at", task.createdAt),
        ).exceptionOrNull()?.message

    private suspend fun syncScheduleItem(item: ScheduleItemEntity): String? =
        supabase.upsert(
            "schedule_items",
            JSONObject()
                .put("remote_id", item.remoteId)
                .put("date", item.date)
                .put("start_time", item.startTime)
                .put("end_time", item.endTime)
                .put("subject", item.subject)
                .put("title", item.title)
                .put("description", item.description)
                .put("suggested_minutes", item.suggestedMinutes)
                .put("completion_standard", item.completionStandard)
                .put("requires_focus_timer", item.requiresFocusTimer)
                .put("status", item.status)
                .put("created_at", item.createdAt),
        ).exceptionOrNull()?.message

    private suspend fun syncSubmission(submission: HomeworkSubmissionEntity): String? =
        supabase.upsert(
            "homework_submissions",
            JSONObject()
                .put("remote_id", submission.remoteId)
                .put("subject", submission.subject)
                .put("prompt", submission.prompt)
                .put("feedback", submission.feedback)
                .put("score", submission.score)
                .put("strengths", submission.strengths)
                .put("problems", submission.problems)
                .put("corrections", submission.corrections)
                .put("next_action_instruction", submission.nextActionInstruction)
                .put("image_uri", submission.imageUri)
                .put("created_at", submission.createdAt),
        ).exceptionOrNull()?.message

    private suspend fun syncFocusSession(session: FocusSessionEntity): String? =
        supabase.upsert(
            "focus_sessions",
            JSONObject()
                .put("remote_id", session.remoteId)
                .put("planned_minutes", session.plannedMinutes)
                .put("completed_seconds", session.completedSeconds)
                .put("completed", session.completed)
                .put("created_at", session.createdAt),
        ).exceptionOrNull()?.message

    private suspend fun syncDraft(draft: HomeworkDraftEntity): String? =
        supabase.upsert(
            "homework_drafts",
            JSONObject()
                .put("remote_id", draft.remoteId)
                .put("source_type", draft.sourceType)
                .put("source_remote_id", draft.sourceRemoteId)
                .put("subject", draft.subject)
                .put("title", draft.title)
                .put("prompt", draft.prompt)
                .put("completion_standard", draft.completionStandard)
                .put("draft_text", draft.draftText)
                .put("status", draft.status)
                .put("created_at", draft.createdAt)
                .put("updated_at_ms", draft.updatedAt),
        ).exceptionOrNull()?.message

    private suspend fun syncProfile(profile: UserProfileEntity): String? =
        supabase.upsert(
            "user_profiles",
            JSONObject()
                .put("remote_id", profile.remoteId)
                .put("nickname", profile.nickname)
                .put("learning_goal", profile.learningGoal)
                .put("timezone", profile.timezone)
                .put("updated_at_ms", profile.updatedAt),
        ).exceptionOrNull()?.message

    private suspend fun syncAvailabilityRule(rule: AvailabilityRuleEntity): String? =
        supabase.upsert(
            "availability_rules",
            JSONObject()
                .put("remote_id", rule.remoteId)
                .put("weekday", rule.weekday)
                .put("start_time", rule.startTime)
                .put("end_time", rule.endTime)
                .put("label", rule.label)
                .put("rule_type", rule.ruleType)
                .put("created_at", rule.createdAt),
        ).exceptionOrNull()?.message

    private suspend fun syncAvailabilityException(exception: AvailabilityExceptionEntity): String? =
        supabase.upsert(
            "availability_exceptions",
            JSONObject()
                .put("remote_id", exception.remoteId)
                .put("date", exception.date)
                .put("start_time", exception.startTime)
                .put("end_time", exception.endTime)
                .put("label", exception.label)
                .put("rule_type", exception.ruleType)
                .put("created_at", exception.createdAt),
        ).exceptionOrNull()?.message

    private suspend fun syncSocialAssignment(assignment: SocialPublishingAssignmentEntity): String? =
        supabase.upsert(
            "social_publishing_assignments",
            JSONObject()
                .put("remote_id", assignment.remoteId)
                .put("title", assignment.title)
                .put("description", assignment.description)
                .put("month", assignment.month)
                .put("due_date", assignment.dueDate)
                .put("required_platforms", assignment.requiredPlatforms)
                .put("artwork_notes", assignment.artworkNotes)
                .put("status", assignment.status)
                .put("created_at", assignment.createdAt)
                .put("updated_at_ms", assignment.updatedAt),
        ).exceptionOrNull()?.message

    private suspend fun syncSocialProof(proof: SocialPostProofEntity): String? =
        supabase.upsert(
            "social_post_proofs",
            JSONObject()
                .put("remote_id", proof.remoteId)
                .put("assignment_remote_id", proof.assignmentRemoteId)
                .put("platform", proof.platform)
                .put("url", proof.url)
                .put("verification_status", proof.verificationStatus)
                .put("ai_feedback", proof.aiFeedback)
                .put("submitted_at", proof.submittedAt),
        ).exceptionOrNull()?.message

    private suspend fun syncDelete(table: String, remoteId: String): String? =
        supabase.delete(table, remoteId).exceptionOrNull()?.message

    private fun String?.combine(next: String?): String? = this ?: next
}
