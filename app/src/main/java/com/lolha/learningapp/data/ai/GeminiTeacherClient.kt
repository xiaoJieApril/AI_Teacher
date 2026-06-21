package com.lolha.learningapp.data.ai

import com.lolha.learningapp.BuildConfig
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class GeminiTeacherClient(
    private val apiKey: String = BuildConfig.GEMINI_API_KEY,
    private val model: String = "gemini-3.5-flash",
) {
    suspend fun askTeacher(
        userMessage: String,
        imageBase64: String? = null,
        imageMimeType: String? = null,
    ): TeacherResponse = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) return@withContext offlineFallback(userMessage)

        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json")
            doOutput = true
            connectTimeout = 15_000
            readTimeout = 30_000
        }

        connection.outputStream.use { stream ->
            stream.write(buildRequest(userMessage, imageBase64, imageMimeType).toString().toByteArray())
        }

        val body = if (connection.responseCode in 200..299) {
            connection.inputStream.bufferedReader().use { it.readText() }
        } else {
            val error = connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
            throw IllegalStateException("Gemini request failed: ${connection.responseCode} $error")
        }

        val text = JSONObject(body)
            .getJSONArray("candidates")
            .getJSONObject(0)
            .getJSONObject("content")
            .getJSONArray("parts")
            .getJSONObject(0)
            .getString("text")

        TeacherResponse.fromJson(text)
    }

    private fun buildRequest(
        userMessage: String,
        imageBase64: String?,
        imageMimeType: String?,
    ): JSONObject {
        val parts = JSONArray().put(JSONObject().put("text", userMessage))
        if (!imageBase64.isNullOrBlank() && !imageMimeType.isNullOrBlank()) {
            parts.put(
                JSONObject().put(
                    "inline_data",
                    JSONObject()
                        .put("mime_type", imageMimeType)
                        .put("data", imageBase64),
                ),
            )
        }

        return JSONObject()
            .put(
                "system_instruction",
                JSONObject().put("parts", JSONArray().put(JSONObject().put("text", AiTeacherPrompt.systemInstruction))),
            )
            .put(
                "contents",
                JSONArray().put(
                    JSONObject()
                        .put("role", "user")
                        .put("parts", parts),
                ),
            )
            .put(
                "generationConfig",
                JSONObject()
                    .put("temperature", 0.35)
                    .put("response_mime_type", "application/json"),
            )
    }

    private fun offlineFallback(userMessage: String): TeacherResponse {
        val wantsTask = userMessage.contains("今日") || userMessage.contains("任務") || userMessage.contains("task", ignoreCase = true)
        val wantsSchedule = userMessage.contains("時間表") || userMessage.contains("schedule", ignoreCase = true)
        if (wantsSchedule) {
            val today = LocalDate.now()
            val weekly = userMessage.contains("7 天") || userMessage.contains("本週") || userMessage.contains("到 ${today.plusDays(6)}")
            val dates = if (weekly) List(7) { today.plusDays(it.toLong()) } else listOf(today)
            return TeacherResponse(
                teacherDialogue = "API key 尚未設定。這是一份離線示範時間表；設定 GEMINI_API_KEY 後，我會根據你的實際狀態重新安排。",
                actionType = "create_schedule",
                subject = "none",
                taskDetails = TaskDetails(),
                grading = GradingDetails(score = "N/A"),
                nextAction = NextAction(
                    type = "start_timer",
                    minutes = 30,
                    instruction = "選一個時間表項目並啟動專注計時。",
                ),
                scheduleItems = dates.flatMap { date ->
                    listOf(
                        ScheduleItem(
                            date = date.toString(),
                            startTime = "09:00",
                            endTime = "09:30",
                            subject = "japanese",
                            title = "日文輸入訓練",
                            description = "複習 10 個單字並各寫 1 句例句。",
                            suggestedMinutes = 30,
                            completionStandard = "提交 10 句日文例句。",
                        ),
                        ScheduleItem(
                            date = date.toString(),
                            startTime = "17:30",
                            endTime = "18:00",
                            subject = "fitness",
                            title = "低強度體能建立",
                            description = "5 分鐘熱身，接著做深蹲、伏地挺身或替代動作各 3 組。疼痛或暈眩就停止。",
                            suggestedMinutes = 30,
                            completionStandard = "記錄完成組數與身體感受。",
                        ),
                        ScheduleItem(
                            date = date.toString(),
                            startTime = "20:30",
                            endTime = "21:15",
                            subject = "drawing",
                            title = "手勢速寫",
                            description = "完成 10 張 30 秒手勢速寫，最後挑 2 張重畫。",
                            suggestedMinutes = 45,
                            completionStandard = "提交速寫照片並標出 2 個比例問題。",
                        ),
                    )
                },
            )
        }
        return if (wantsTask) {
            TeacherResponse(
                teacherDialogue = "API key 尚未設定。先完成這份離線任務：日文 20 分鐘、英文 15 分鐘、素描 25 分鐘。完成後再提交成果。",
                actionType = "assign_task",
                subject = "none",
                taskDetails = TaskDetails(
                    title = "離線基礎訓練",
                    description = "日文：整理 10 個 N3 單字例句。英文：寫 120 字短文。繪畫：臨摹一張手部結構並標出比例問題。",
                    suggestedMinutes = 60,
                    completionStandard = "提交單字例句、英文短文，以及一張標註比例問題的素描照片。",
                ),
                grading = GradingDetails(score = "N/A"),
                nextAction = NextAction(
                    type = "start_timer",
                    minutes = 60,
                    instruction = "啟動 60 分鐘專注計時，完成後提交文字與圖片作業。",
                ),
            )
        } else {
            TeacherResponse(
                teacherDialogue = "我收到你的訊息，但目前沒有 API key。請在 local.properties 設定 GEMINI_API_KEY，才能啟用嚴格批改。",
                actionType = "general_chat",
                subject = "none",
                taskDetails = TaskDetails(),
                grading = GradingDetails(score = "N/A"),
                nextAction = NextAction(
                    type = "continue_chat",
                    instruction = "設定 API key 後重新送出作業。",
                ),
            )
        }
    }
}
