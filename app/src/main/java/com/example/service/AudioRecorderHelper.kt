package com.example.service

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File

object AudioRecorderHelper {
    private const val TAG = "AudioRecorderHelper"
    private var mediaRecorder: MediaRecorder? = null
    private var currentOutputFile: File? = null
    private var isRecording = false

    fun startRecording(context: Context): Result<File> {
        return try {
            stopRecording() // Clean up any previous session

            val audioFile = File(context.cacheDir, "dictation_${System.currentTimeMillis()}.m4a")
            currentOutputFile = audioFile

            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(audioFile.absolutePath)
                prepare()
                start()
            }

            mediaRecorder = recorder
            isRecording = true
            Log.d(TAG, "Audio recording started: ${audioFile.absolutePath}")
            Result.success(audioFile)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start audio recording", e)
            isRecording = false
            mediaRecorder = null
            Result.failure(e)
        }
    }

    fun stopRecording(): File? {
        if (!isRecording && mediaRecorder == null) {
            return currentOutputFile?.takeIf { it.exists() && it.length() > 0 }
        }

        return try {
            mediaRecorder?.apply {
                stop()
                reset()
                release()
            }
            mediaRecorder = null
            isRecording = false
            Log.d(TAG, "Audio recording stopped successfully")
            currentOutputFile?.takeIf { it.exists() && it.length() > 0 }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping MediaRecorder", e)
            mediaRecorder = null
            isRecording = false
            currentOutputFile?.takeIf { it.exists() && it.length() > 0 }
        }
    }

    fun isCurrentlyRecording(): Boolean = isRecording
}
