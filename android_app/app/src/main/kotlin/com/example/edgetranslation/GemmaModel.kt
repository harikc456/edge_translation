package com.example.edgetranslation

import android.content.Context
import com.google.ai.edge.litert.lm.Engine
import com.google.ai.edge.litert.lm.EngineConfig

class GemmaModel(context: Context) {
    private var engine: Engine? = null

    init {
        val config = EngineConfig.builder()
            .setModelPath("/data/local/tmp/gemma-4-e2b.litertlm")
            .setBackend(EngineConfig.Backend.GPU)
            .build()
        engine = Engine(context, config)
        engine?.initialize()
    }

    fun translate(text: String): String {
        val prompt = """
            You are a translation assistant. The user will give you a request like 'Translate to French: Hello' or 'How are you in Spanish?'.
            1. Identify the target language. If not specified, default to Spanish.
            2. Translate the message.
            3. Respond ONLY in this format: 'Language: [Language] | Translation: [Translated Text]'
            
            User request: $text
        """.trimIndent()
        val conversation = engine?.createConversation()
        val response = conversation?.sendMessage(prompt)
        return response?.text ?: ""
    }
}
