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

    @Query("SELECT * FROM schedule_items WHERE date IN (:dates)")
    suspend fun getScheduleItemsForDates(dates: List<String>): List<ScheduleItemEntity>

    @Query("SELECT * FROM learning_tasks WHERE id = :taskId LIMIT 1")
    suspend fun getTask(taskId: Long): LearningTaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: LearningTaskEntity): Long

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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubmission(submission: HomeworkSubmissionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFocusSession(session: FocusSessionEntity): Long

    @Query("UPDATE learning_tasks SET status = :status WHERE id = :taskId")
    suspend fun updateTaskStatus(taskId: Long, status: String)

    @Query("UPDATE schedule_items SET status = :status WHERE id = :itemId")
    suspend fun updateScheduleStatus(itemId: Long, status: String)
}
