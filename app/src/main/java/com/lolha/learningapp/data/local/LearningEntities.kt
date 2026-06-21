package com.lolha.learningapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "learning_tasks")
data class LearningTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val remoteId: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val subject: String,
    val suggestedMinutes: Int,
    val completionStandard: String = "",
    val nextActionType: String = "none",
    val nextActionInstruction: String = "",
    val status: String = "todo",
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val remoteId: String = UUID.randomUUID().toString(),
    val role: String,
    val message: String,
    val actionType: String = "general_chat",
    val subject: String = "none",
    val score: String = "N/A",
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "homework_submissions")
data class HomeworkSubmissionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val remoteId: String = UUID.randomUUID().toString(),
    val subject: String,
    val prompt: String,
    val feedback: String,
    val score: String,
    val strengths: String = "",
    val problems: String = "",
    val corrections: String = "",
    val nextActionInstruction: String = "",
    val imageUri: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val remoteId: String = UUID.randomUUID().toString(),
    val plannedMinutes: Int,
    val completedSeconds: Int,
    val completed: Boolean,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "schedule_items")
data class ScheduleItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val remoteId: String = UUID.randomUUID().toString(),
    val date: String,
    val startTime: String,
    val endTime: String,
    val subject: String,
    val title: String,
    val description: String,
    val suggestedMinutes: Int,
    val completionStandard: String,
    val requiresFocusTimer: Boolean = true,
    val status: String = "todo",
    val createdAt: Long = System.currentTimeMillis(),
)
