package com.lovegaoshi.kotlinaudio.models

import androidx.media3.common.C

data class PlayerOptions(
    val cacheSize: Long = 0,
    val audioContentType: Int = 0,
    val handleAudioFocus: Boolean = true
)

fun setContentType (type: Int = 0): Int {
    return when (type) {
        1 -> C.AUDIO_CONTENT_TYPE_SPEECH
        2 -> C.AUDIO_CONTENT_TYPE_SONIFICATION
        3 -> C.AUDIO_CONTENT_TYPE_MOVIE
        4 -> C.AUDIO_CONTENT_TYPE_UNKNOWN
        else -> C.AUDIO_CONTENT_TYPE_MUSIC
    }
}