@file: OptIn(UnstableApi::class)
package com.lovegaoshi.kotlinaudio.models

import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi

data class PlayerOptions(
    val cacheSize: Long = 0,
    val audioContentType: Int = 0,
    val wakeMode: Int = 0,
    val handleAudioBecomingNoisy: Boolean = false,
    val alwaysShowNext: Boolean = true,
    val handleAudioFocus: Boolean = true,
    val bufferOptions: BufferOptions = BufferOptions(null, null, null, null)
)

data class BufferOptions (

    val minBuffer: Int?,
    val maxBuffer: Int?,
    val playBuffer: Int?,
    val backBuffer: Int?,

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

fun setWakeMode(type: Int = 0): Int {
    return when (type) {
        1 -> C.WAKE_MODE_LOCAL
        2 -> C.WAKE_MODE_NETWORK
        else -> C.WAKE_MODE_NONE
    }
}
