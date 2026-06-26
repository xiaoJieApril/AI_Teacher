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
    val sourceType: String? = null,
    val sourceRemoteId: String? = null,
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

@Entity(tableName = "homework_drafts")
data class HomeworkDraftEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val remoteId: String = UUID.randomUUID().toString(),
    val sourceType: String,
    val sourceRemoteId: String,
    val subject: String,
    val title: String,
    val prompt: String,
    val completionStandard: String = "",
    val draftText: String = "",
    val status: String = "draft",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey val id: Long = 1,
    val remoteId: String = UUID.randomUUID().toString(),
    val nickname: String = "",
    val learningGoal: String = "",
    val timezone: String = "Asia/Kuala_Lumpur",
    val updatedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "availability_rules")
data class AvailabilityRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val remoteId: String = UUID.randomUUID().toString(),
    val weekday: String,
    val startTime: String,
    val endTime: String,
    val label: String,
    val ruleType: String = "work",
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "availability_exceptions")
data class AvailabilityExceptionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val remoteId: String = UUID.randomUUID().toString(),
    val date: String,
    val startTime: String,
    val endTime: String,
    val label: String,
    val ruleType: String = "unavailable",
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "social_publishing_assignments")
data class SocialPublishingAssignmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val remoteId: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val month: String,
    val dueDate: String,
    val requiredPlatforms: String = "X,Pixiv",
    val artworkNotes: String = "",
    val status: String = "active",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "social_post_proofs")
data class SocialPostProofEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val remoteId: String = UUID.randomUUID().toString(),
    val assignmentRemoteId: String,
    val platform: String,
    val url: String,
    val verificationStatus: String = "pending",
    val aiFeedback: String = "",
    val submittedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "deletion_audits")
data class DeletionAuditEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val remoteId: String = UUID.randomUUID().toString(),
    val itemType: String,
    val itemRemoteId: String,
    val itemTitle: String,
    val reasonCategory: String,
    val reasonDetail: String,
    val deletedAt: Long = System.currentTimeMillis(),
)
