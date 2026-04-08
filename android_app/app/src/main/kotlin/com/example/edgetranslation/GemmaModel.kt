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

    fun translate(text: String, from: String, to: String): String {
        val prompt = "Translate the following from $from to $to: $text"
        val conversation = engine?.createConversation()
        val response = conversation?.sendMessage(prompt)
        return response?.text ?: ""
    }
}
