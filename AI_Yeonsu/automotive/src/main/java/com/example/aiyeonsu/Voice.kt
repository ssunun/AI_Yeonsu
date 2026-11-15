package com.example.aiyeonsu

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

// 연곰 아저씨의 한국어 음성 (기기에 한국어 음성이 없으면 소리 없이 자막만 나옵니다)
class Voice(context: Context) {
    private var tts: TextToSpeech? = null
    private var ready = false
    private var pending: String? = null
    var muted = false

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.setLanguage(Locale.KOREAN)
                ready = true
                pending?.let { speak(it) }
                pending = null
            }
        }
    }

    fun speak(text: String) {
        if (muted) return
        if (!ready) {
            pending = text
            return
        }
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "ayeonsu")
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
