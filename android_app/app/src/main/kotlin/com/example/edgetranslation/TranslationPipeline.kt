package com.example.edgetranslation

import android.content.Context
import android.util.Log

class TranslationPipeline(private val context: Context) {
    private val whisper = WhisperModel(context)
    private val gemma = GemmaModel(context)
    private val mms = MMSModel(context)

    suspend fun translateAudio(audioData: FloatArray, sourceLang: String, targetLang: String): FloatArray? {
        Log.d("Pipeline", "Starting STT (Whisper)...")
        val transcript = whisper.transcribe(audioData) ?: return null
        Log.d("Pipeline", "Transcribed: $transcript")

        Log.d("Pipeline", "Starting Translation (Gemma 4)...")
        val translation = gemma.translate(transcript, sourceLang, targetLang)
        Log.d("Pipeline", "Translated: $translation")

        Log.d("Pipeline", "Starting TTS (MMS)...")
        return mms.synthesize(translation, targetLang)
    }
}
