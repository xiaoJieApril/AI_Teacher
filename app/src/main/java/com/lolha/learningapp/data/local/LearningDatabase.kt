package com.lolha.learningapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        LearningTaskEntity::class,
        ChatMessageEntity::class,
        HomeworkSubmissionEntity::class,
        FocusSessionEntity::class,
        ScheduleItemEntity::class,
        HomeworkDraftEntity::class,
        UserProfileEntity::class,
        AvailabilityRuleEntity::class,
        AvailabilityExceptionEntity::class,
        SocialPublishingAssignmentEntity::class,
        SocialPostProofEntity::class,
    ],
    version = 5,
)
abstract class LearningDatabase : RoomDatabase() {
    abstract fun learningDao(): LearningDao

    companion object {
        fun create(context: Context): LearningDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                LearningDatabase::class.java,
                "learning-app.db",
            )
                .fallbackToDestructiveMigration()
                .build()
    }
}
