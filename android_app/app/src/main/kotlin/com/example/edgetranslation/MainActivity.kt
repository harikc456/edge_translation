package com.example.edgetranslation

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import android.util.Log

class MainActivity : AppCompatActivity() {
    private lateinit var pipeline: TranslationPipeline
    private lateinit var recorder: AudioRecorder
    private lateinit var statusText: TextView
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        pipeline = TranslationPipeline(this)
        recorder = AudioRecorder()
        statusText = findViewById(R.id.statusText)

        findViewById<Button>(R.id.btnRecord).setOnClickListener {
            startTranslation()
        }
    }

    private fun startTranslation() {
        scope.launch {
            try {
                statusText.text = "Listening..."
                val audio = recorder.record(5000)

                statusText.text = "Processing..."
                val resultAudio = withContext(Dispatchers.Default) {
                    pipeline.translateAudio(audio, "English", "Spanish")
                }

                if (resultAudio != null) {
                    statusText.text = "Playing translation..."
                    playAudio(resultAudio)
                } else {
                    statusText.text = "Translation failed."
                }
            } catch (e: Exception) {
                Log.e("UI", "Pipeline Error", e)
                statusText.text = "Error: ${e.message}"
            }
        }
    }

    private fun playAudio(data: FloatArray) {
        val sampleRate = 16000 // MMS standard
        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_FLOAT
        )

        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        audioTrack.play()
        audioTrack.write(data, 0, data.size, AudioTrack.WRITE_BLOCKING)
        
        // Use a coroutine to stop the track after playback
        scope.launch {
            val durationMs = (data.size.toFloat() / sampleRate * 1000).toLong()
            delay(durationMs + 100)
            audioTrack.stop()
            audioTrack.release()
            statusText.text = "Done."
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
