package com.aubynsamuel.flashsend.chatRoom.domain

import android.content.Context
import android.media.MediaRecorder
import android.os.Environment
import android.util.Log
import com.aubynsamuel.flashsend.chatRoom.data.remote.FirebaseStorageRepository
import com.aubynsamuel.flashsend.chatRoom.data.remote.SendMessageRepository
import com.aubynsamuel.flashsend.chatRoom.presentation.components.formatAudioTime
import java.io.File
import javax.inject.Inject


class AudioRecordingUseCase @Inject constructor(
    private val sendMessageRepository: SendMessageRepository,
    private val firebaseStorageRepository: FirebaseStorageRepository,
    private val notificationUseCase: SendNotificationUseCase,
) {
    private val tag = "AudioRecordingUseCase"
    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null
    private var startTime: Long = 0L
    private var stopTime: Long = 0L

    val recordingStartTime: Long
        get() = startTime

    fun startRecording(context: Context) {
        try {
            val outputDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
            audioFile =
                File.createTempFile("audio_${System.currentTimeMillis()}", ".3gp", outputDir)

            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(audioFile?.absolutePath)
                prepare()
                start()
                startTime = System.currentTimeMillis()
            }
        } catch (e: Exception) {
            Log.e(tag, "Error starting recording", e)
        }
    }

    fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        stopTime = System.currentTimeMillis()
        mediaRecorder = null
    }

    suspend fun sendAudioMessage(
        roomId: String,
        senderId: String,
        senderName: String,
        otherUserId: String,
        profileUrl: String,
        recipientsToken: String,
    ) {
        try {
            val audioUrl = firebaseStorageRepository.uploadAudio(audioFile)
            val duration = stopTime - startTime
            val content = "🔊 ${formatAudioTime(duration)}"

            sendMessageRepository.sendAudioMessage(
                roomId = roomId,
                content = content,
                senderId = senderId,
                senderName = senderName,
                audioUrl = audioUrl,
                duration = duration
            )

            notificationUseCase(
                recipientsToken = recipientsToken,
                title = senderName,
                body = content,
                roomId = roomId,
                recipientsUserId = otherUserId,
                sendersUserId = senderId,
                profileUrl = profileUrl
            )

            // Clear the audio file reference
            audioFile = null
        } catch (e: Exception) {
            Log.e(tag, "Error sending audio message", e)
        }
    }

    fun reset() {
        if (mediaRecorder != null) {
            stopRecording()
        }
        audioFile = null
    }
}