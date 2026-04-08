package com.example.edgetranslation

import android.content.Context
import android.util.Log

class TranslationPipeline(private val context: Context) {
    private val whisper = WhisperModel(context)
    private val gemma = GemmaModel(context)
    private val mms = MMSModel(context)

    data class TranslationResult(
        val transcript: String,
        val translation: String,
        val audio: FloatArray?
    )

    suspend fun translateAudio(audioData: FloatArray): TranslationResult? {
        Log.d("Pipeline", "Starting STT (Whisper)...")
        val transcript = whisper.transcribe(audioData) ?: return null
        Log.d("Pipeline", "Transcribed: $transcript")

        Log.d("Pipeline", "Starting Translation (Gemma 4)...")
        val llmResponse = gemma.translate(transcript)
        Log.d("Pipeline", "LLM Response: $llmResponse")

        // Parse structured response: "Language: Spanish | Translation: Hola"
        var detectedLang = "Spanish" // Default
        var translation = llmResponse
        
        try {
            val parts = llmResponse.split("|")
            if (parts.size == 2) {
                detectedLang = parts[0].replace("Language:", "").trim()
                translation = parts[1].replace("Translation:", "").trim()
            }
        } catch (e: Exception) {
            Log.e("Pipeline", "Error parsing LLM response", e)
        }

        Log.d("Pipeline", "Detected Lang: $detectedLang, Translation: $translation")

        Log.d("Pipeline", "Starting TTS (MMS)...")
        val audio = mms.synthesize(translation, detectedLang)
        
        return TranslationResult(transcript, translation, audio)
    }
}
