package com.abhi.voicesearch.main

import androidx.fragment.app.FragmentActivity
import com.abhi.voicesearch.data.App

interface SpeechRecognizerandler {
    fun selectedAppForSpeech(item: App, requireActivity: FragmentActivity)
}