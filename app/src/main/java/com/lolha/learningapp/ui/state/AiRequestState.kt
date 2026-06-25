package com.lolha.learningapp.ui.state

sealed interface AiRequestState {
    data object Idle : AiRequestState

    data class Thinking(
        val kind: AiRequestKind,
        val message: String,
    ) : AiRequestState

    data class Failed(
        val message: String,
    ) : AiRequestState
}

enum class AiRequestKind {
    Chat,
    DailyTask,
    DailySchedule,
    WeeklySchedule,
    HomeworkGrading,
    SocialProof,
}

val AiRequestState.isThinking: Boolean
    get() = this is AiRequestState.Thinking

fun AiRequestState.thinkingMessageOrNull(): String? =
    (this as? AiRequestState.Thinking)?.message

