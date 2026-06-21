package com.lolha.learningapp.data

import android.content.Context
import com.lolha.learningapp.data.ai.GeminiTeacherClient
import com.lolha.learningapp.data.ai.TeacherResponse
import com.lolha.learningapp.data.local.ChatMessageEntity
import com.lolha.learningapp.data.local.FocusSessionEntity
import com.lolha.learningapp.data.local.HomeworkSubmissionEntity
import com.lolha.learningapp.data.local.LearningDao
import com.lolha.learningapp.data.local.LearningTaskEntity
import com.lolha.learningapp.data.local.ScheduleItemEntity
import com.lolha.learningapp.data.remote.SupabaseSyncClient
import java.time.LocalDate
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
            modelPrompt = "請為 $today 建立今日時間表，包含日文、英文、畫畫與健身。請平衡學習、訓練與休息，輸出 create_schedule 和 schedule_items。",
        )
    }

    suspend fun requestWeeklySchedule(): RepositoryResult {
        val today = LocalDate.now()
        val endDate = today.plusDays(6)
        return sendChatMessage(
            visibleMessage = "請幫我安排本週時間表",
            modelPrompt = "請為 ${today} 到 ${endDate} 建立 7 天本週時間表，包含日文、英文、畫畫與健身。請平衡強度、休息與提交成果，輸出 create_schedule 和 schedule_items。",
        )
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

    private suspend fun persistTeacherResponse(prompt: String, response: TeacherResponse): String? {
        var syncError: String? = null
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

    private suspend fun syncDelete(table: String, remoteId: String): String? =
        supabase.delete(table, remoteId).exceptionOrNull()?.message

    private fun String?.combine(next: String?): String? = this ?: next
}
