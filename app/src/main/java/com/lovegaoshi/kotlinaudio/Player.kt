@file: OptIn(UnstableApi::class) package com.lovegaoshi.kotlinaudio

import androidx.annotation.OptIn
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.util.UnstableApi

class Player {
    lateinit var player: ForwardingPlayer

    fun setupPlayer() {
        player = object : ForwardingPlayer(player) {
            override fun play() {
                // Add custom logic
                super.play()
            }
            override fun setPlayWhenReady(playWhenReady: Boolean) {
                // Add custom logic
                super.setPlayWhenReady(playWhenReady)
            }
        }
    }

}