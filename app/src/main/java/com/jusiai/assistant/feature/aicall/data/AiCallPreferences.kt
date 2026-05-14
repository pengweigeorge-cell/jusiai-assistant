package com.jusiai.assistant.feature.aicall.data

import android.content.Context

class AiCallPreferences(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    var voiceIndex: Int
        get() = prefs.getInt(KEY_VOICE_INDEX, 0)
        set(value) {
            prefs.edit().putInt(KEY_VOICE_INDEX, value.coerceIn(0, 3)).apply()
        }

    var promptLabel: String?
        get() = prefs.getString(KEY_PROMPT_LABEL, null)
        set(value) {
            prefs.edit().apply {
                if (value.isNullOrBlank()) remove(KEY_PROMPT_LABEL) else putString(KEY_PROMPT_LABEL, value)
            }.apply()
        }

    companion object {
        private const val FILE_NAME = "ai_call_prefs"
        private const val KEY_VOICE_INDEX = "voice_index"
        private const val KEY_PROMPT_LABEL = "prompt_label"
    }
}
