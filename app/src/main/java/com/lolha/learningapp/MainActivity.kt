package com.lolha.learningapp

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.lolha.learningapp.ui.LearningAppScreen
import com.lolha.learningapp.ui.theme.LearningTheme
import java.io.ByteArrayOutputStream
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pickImage = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            uri?.let(::attachImageForGrading)
        }
        val takePhoto = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            bitmap?.let(::attachBitmapForGrading)
        }
        val speechInput = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val spoken = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
                .orEmpty()
            if (spoken.isNotBlank()) {
                val current = viewModel.uiState.value.input
                viewModel.updateInput(listOf(current, spoken).filter { it.isNotBlank() }.joinToString(" "))
            }
        }

        setContent {
            LearningTheme {
                LearningAppScreen(
                    state = viewModel.uiState.value,
                    onTabSelected = viewModel::selectTab,
                    onInputChanged = viewModel::updateInput,
                    onPickImage = {
                        pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    onTakePhoto = { takePhoto.launch(null) },
                    onSpeechInput = {
                        speechInput.launch(
                            Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your answer")
                            },
                        )
                    },
                    onClearAttachment = viewModel::clearAttachment,
                    onSend = viewModel::sendCurrentInput,
                    onDeleteChatMessage = viewModel::deleteChatMessage,
                    onClearChat = viewModel::clearChat,
                    onDailyTask = viewModel::requestDailyTask,
                    onDailySchedule = viewModel::requestDailySchedule,
                    onWeeklySchedule = viewModel::requestWeeklySchedule,
                    onScheduleModeSelected = viewModel::selectScheduleMode,
                    onScheduleDone = viewModel::markScheduleDone,
                    onScheduleDelete = viewModel::deleteScheduleItem,
                    onScheduleFocus = viewModel::startScheduleFocus,
                    onScheduleHomework = viewModel::openHomeworkForSchedule,
                    onTaskDone = viewModel::markTaskDone,
                    onTaskHomework = viewModel::openHomeworkForTask,
                    onDraftTextChanged = viewModel::updateDraftText,
                    onSaveDraft = viewModel::saveActiveDraft,
                    onSubmitDraft = viewModel::submitActiveDraft,
                    onProfileNicknameChanged = viewModel::updateProfileNickname,
                    onProfileGoalChanged = viewModel::updateProfileGoal,
                    onProfileTimezoneChanged = viewModel::updateProfileTimezone,
                    onSaveProfile = viewModel::saveProfile,
                    onRuleWeekdayChanged = viewModel::updateRuleWeekday,
                    onRuleStartChanged = viewModel::updateRuleStartTime,
                    onRuleEndChanged = viewModel::updateRuleEndTime,
                    onRuleLabelChanged = viewModel::updateRuleLabel,
                    onRuleTypeChanged = viewModel::updateRuleType,
                    onAddRule = viewModel::addAvailabilityRule,
                    onDeleteRule = viewModel::deleteAvailabilityRule,
                    onExceptionDateChanged = viewModel::updateExceptionDate,
                    onExceptionStartChanged = viewModel::updateExceptionStartTime,
                    onExceptionEndChanged = viewModel::updateExceptionEndTime,
                    onExceptionLabelChanged = viewModel::updateExceptionLabel,
                    onExceptionTypeChanged = viewModel::updateExceptionType,
                    onAddException = viewModel::addAvailabilityException,
                    onDeleteException = viewModel::deleteAvailabilityException,
                    onCreateSocialAssignment = viewModel::createMonthlySocialAssignment,
                    onProofInputChanged = viewModel::updateProofInput,
                    onSubmitProof = viewModel::submitSocialProof,
                    onFocusMinutesChanged = viewModel::setFocusMinutes,
                    onStartFocus = {
                        requestScreenPinning()
                        viewModel.startFocus()
                    },
                    onStopFocus = viewModel::stopFocus,
                    onOpenUrl = ::openUrl,
                )
            }
        }
    }

    private fun openUrl(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    private fun requestScreenPinning() {
        try {
            startLockTask()
        } catch (_: IllegalStateException) {
            // Device policy or system settings may prevent screen pinning.
        }
    }

    private fun attachImageForGrading(uri: Uri) {
        val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
        val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return
        val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
        viewModel.attachImage(base64, mimeType, "Image homework attached")
    }

    private fun attachBitmapForGrading(bitmap: Bitmap) {
        val bytes = ByteArrayOutputStream().use { output ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, output)
            output.toByteArray()
        }
        val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
        viewModel.attachImage(base64, "image/jpeg", "Camera homework attached")
    }
}
