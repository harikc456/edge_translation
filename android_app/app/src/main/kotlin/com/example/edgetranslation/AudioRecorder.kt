package com.example.edgetranslation

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.delay

class AudioRecorder {
    private val sampleRate = 16000
    private val bufferSize = AudioRecord.getMinBufferSize(
        sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
    )

    suspend fun record(durationMs: Long): FloatArray {
        val recorder = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )

        val audioData = mutableListOf<Short>()
        val buffer = ShortArray(bufferSize)

        recorder.startRecording()
        val startTime = System.currentTimeMillis()
        while (System.currentTimeMillis() - startTime < durationMs) {
            val read = recorder.read(buffer, 0, buffer.size)
            for (i in 0 until read) audioData.add(buffer[i])
            delay(10)
        }
        recorder.stop()
        recorder.release()

        // Convert Short (PCM 16-bit) to Float [-1.0, 1.0] for models
        return FloatArray(audioData.size) { audioData[it] / 32768.0f }
    }
}
