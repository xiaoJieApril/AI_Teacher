package com.lolha.learningapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LearningDao {
    @Query("SELECT * FROM learning_tasks ORDER BY createdAt DESC")
    fun observeTasks(): Flow<List<LearningTaskEntity>>

    @Query("SELECT * FROM chat_messages ORDER BY createdAt ASC")
    fun observeMessages(): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM homework_submissions ORDER BY createdAt DESC")
    fun observeSubmissions(): Flow<List<HomeworkSubmissionEntity>>

    @Query("SELECT * FROM schedule_items ORDER BY date ASC, startTime ASC, createdAt ASC")
    fun observeScheduleItems(): Flow<List<ScheduleItemEntity>>

    @Query("SELECT * FROM homework_drafts ORDER BY updatedAt DESC")
    fun observeHomeworkDrafts(): Flow<List<HomeworkDraftEntity>>

    @Query("SELECT * FROM user_profiles WHERE id = 1 LIMIT 1")
    fun observeProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM availability_rules ORDER BY weekday ASC, startTime ASC")
    fun observeAvailabilityRules(): Flow<List<AvailabilityRuleEntity>>

    @Query("SELECT * FROM availability_exceptions ORDER BY date ASC, startTime ASC")
    fun observeAvailabilityExceptions(): Flow<List<AvailabilityExceptionEntity>>

    @Query("SELECT * FROM social_publishing_assignments ORDER BY dueDate ASC")
    fun observeSocialAssignments(): Flow<List<SocialPublishingAssignmentEntity>>

    @Query("SELECT * FROM social_post_proofs ORDER BY submittedAt DESC")
    fun observeSocialProofs(): Flow<List<SocialPostProofEntity>>

    @Query("SELECT * FROM deletion_audits ORDER BY deletedAt DESC")
    fun observeDeletionAudits(): Flow<List<DeletionAuditEntity>>

    @Query("SELECT * FROM schedule_items WHERE date IN (:dates)")
    suspend fun getScheduleItemsForDates(dates: List<String>): List<ScheduleItemEntity>

    @Query("SELECT * FROM learning_tasks WHERE id = :taskId LIMIT 1")
    suspend fun getTask(taskId: Long): LearningTaskEntity?

    @Query("SELECT * FROM learning_tasks WHERE sourceType = :sourceType AND sourceRemoteId = :sourceRemoteId")
    suspend fun getTasksForSource(sourceType: String, sourceRemoteId: String): List<LearningTaskEntity>

    @Query("SELECT * FROM social_publishing_assignments WHERE month = :month AND status = 'active' LIMIT 1")
    suspend fun getActiveSocialAssignmentForMonth(month: String): SocialPublishingAssignmentEntity?

    @Query("SELECT * FROM homework_drafts WHERE sourceType = :sourceType AND sourceRemoteId = :sourceRemoteId LIMIT 1")
    suspend fun getDraftForSource(sourceType: String, sourceRemoteId: String): HomeworkDraftEntity?

    @Query("SELECT * FROM homework_drafts WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getDraft(remoteId: String): HomeworkDraftEntity?

    @Query("SELECT * FROM user_profiles WHERE id = 1 LIMIT 1")
    suspend fun getProfile(): UserProfileEntity?

    @Query("SELECT * FROM availability_rules ORDER BY weekday ASC, startTime ASC")
    suspend fun getAvailabilityRules(): List<AvailabilityRuleEntity>

    @Query("SELECT * FROM availability_exceptions ORDER BY date ASC, startTime ASC")
    suspend fun getAvailabilityExceptions(): List<AvailabilityExceptionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: LearningTaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<LearningTaskEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDraft(draft: HomeworkDraftEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfileEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAvailabilityRule(rule: AvailabilityRuleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAvailabilityException(exception: AvailabilityExceptionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSocialAssignment(assignment: SocialPublishingAssignmentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSocialProof(proof: SocialPostProofEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeletionAudit(audit: DeletionAuditEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduleItems(items: List<ScheduleItemEntity>)

    @Query("DELETE FROM schedule_items WHERE date IN (:dates)")
    suspend fun deleteScheduleItemsForDates(dates: List<String>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity): Long

    @Query("DELETE FROM chat_messages WHERE remoteId = :remoteId")
    suspend fun deleteChatMessage(remoteId: String)

    @Query("DELETE FROM chat_messages")
    suspend fun clearChatMessages()

    @Query("DELETE FROM schedule_items WHERE remoteId = :remoteId")
    suspend fun deleteScheduleItem(remoteId: String)

    @Query("DELETE FROM learning_tasks WHERE remoteId = :remoteId")
    suspend fun deleteTask(remoteId: String)

    @Query("DELETE FROM availability_rules WHERE remoteId = :remoteId")
    suspend fun deleteAvailabilityRule(remoteId: String)

    @Query("DELETE FROM availability_exceptions WHERE remoteId = :remoteId")
    suspend fun deleteAvailabilityException(remoteId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubmission(submission: HomeworkSubmissionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFocusSession(session: FocusSessionEntity): Long

    @Query("UPDATE learning_tasks SET status = :status WHERE id = :taskId")
    suspend fun updateTaskStatus(taskId: Long, status: String)

    @Query("UPDATE schedule_items SET status = :status WHERE id = :itemId")
    suspend fun updateScheduleStatus(itemId: Long, status: String)

    @Query("UPDATE homework_drafts SET draftText = :draftText, updatedAt = :updatedAt WHERE remoteId = :remoteId")
    suspend fun updateDraftText(remoteId: String, draftText: String, updatedAt: Long)

    @Query("UPDATE homework_drafts SET status = :status, updatedAt = :updatedAt WHERE remoteId = :remoteId")
    suspend fun updateDraftStatus(remoteId: String, status: String, updatedAt: Long)
}
