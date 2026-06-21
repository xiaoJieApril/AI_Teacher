package com.lolha.learningapp.data.ai

import org.json.JSONObject

data class TaskDetails(
    val title: String = "",
    val description: String = "",
    val suggestedMinutes: Int = 30,
    val completionStandard: String = "",
)

data class GradingDetails(
    val score: String = "N/A",
    val strengths: List<String> = emptyList(),
    val problems: List<String> = emptyList(),
    val corrections: List<String> = emptyList(),
)

data class NextAction(
    val type: String = "none",
    val minutes: Int = 0,
    val instruction: String = "",
)

data class ScheduleItem(
    val date: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val subject: String = "none",
    val title: String = "",
    val description: String = "",
    val suggestedMinutes: Int = 30,
    val completionStandard: String = "",
    val requiresFocusTimer: Boolean = true,
)

data class TeacherResponse(
    val teacherDialogue: String,
    val actionType: String,
    val subject: String,
    val taskDetails: TaskDetails,
    val grading: GradingDetails,
    val nextAction: NextAction,
    val scheduleItems: List<ScheduleItem> = emptyList(),
) {
    val score: String
        get() = grading.score
    companion object {
        fun fromJson(raw: String): TeacherResponse {
            val json = JSONObject(raw.stripJsonFence())
            val task = json.optJSONObject("task_details") ?: JSONObject()
            val grading = json.optJSONObject("grading") ?: JSONObject()
            val nextAction = json.optJSONObject("next_action") ?: JSONObject()
            return TeacherResponse(
                teacherDialogue = json.optString("teacher_dialogue", "收到。下一步請更具體地提交你的學習內容。"),
                actionType = json.optString("action_type", "general_chat"),
                subject = json.optString("subject", "none"),
                taskDetails = TaskDetails(
                    title = task.optString("title"),
                    description = task.optString("description"),
                    suggestedMinutes = task.optInt("suggested_minutes", 30),
                    completionStandard = task.optString("completion_standard"),
                ),
                grading = GradingDetails(
                    score = grading.optString("score", json.optString("score", "N/A")),
                    strengths = grading.optStringList("strengths"),
                    problems = grading.optStringList("problems"),
                    corrections = grading.optStringList("corrections"),
                ),
                nextAction = NextAction(
                    type = nextAction.optString("type", "none"),
                    minutes = nextAction.optInt("minutes", 0),
                    instruction = nextAction.optString("instruction"),
                ),
                scheduleItems = json.optScheduleItems(),
            )
        }

        private fun JSONObject.optScheduleItems(): List<ScheduleItem> {
            val array = optJSONArray("schedule_items") ?: return emptyList()
            return List(array.length()) { index -> array.optJSONObject(index) ?: JSONObject() }
                .map { item ->
                    ScheduleItem(
                        date = item.optString("date"),
                        startTime = item.optString("start_time"),
                        endTime = item.optString("end_time"),
                        subject = item.optString("subject", "none"),
                        title = item.optString("title"),
                        description = item.optString("description"),
                        suggestedMinutes = item.optInt("suggested_minutes", 30),
                        completionStandard = item.optString("completion_standard"),
                        requiresFocusTimer = item.optBoolean("requires_focus_timer", true),
                    )
                }
                .filter { it.date.isNotBlank() && it.title.isNotBlank() }
        }

        private fun JSONObject.optStringList(name: String): List<String> {
            val array = optJSONArray(name) ?: return emptyList()
            return List(array.length()) { index -> array.optString(index) }
                .filter { it.isNotBlank() }
        }

        private fun String.stripJsonFence(): String =
            trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()
    }
}
