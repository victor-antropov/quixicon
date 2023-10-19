package com.quixicon.presentation.activities.cards.viewmodels

import android.content.Intent
import android.speech.tts.TextToSpeech
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class SpeechViewModel : ViewModel() {

    private var textToSpeechEngine: TextToSpeech? = null
    private lateinit var startForResult: ActivityResultLauncher<Intent>

    fun initial(engine: TextToSpeech, launcher: ActivityResultLauncher<Intent>) =
        viewModelScope.launch {
            textToSpeechEngine = engine
            startForResult = launcher
        }

    fun speak(text: String) = viewModelScope.launch {
        textToSpeechEngine?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
        textToSpeechEngine?.isSpeaking
    }

    fun destroy() {
        textToSpeechEngine?.run {
            stop()
            shutdown()
            textToSpeechEngine = null
        }
    }
}
