@file: OptIn(UnstableApi::class) package com.lovegaoshi.kotlinaudio

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer

class Player {
    lateinit var exoPlayer: ExoPlayer
    lateinit var player: ForwardingPlayer

    fun setupPlayer(context: Context) {

        exoPlayer = ExoPlayer
            .Builder(context)
            .setMediaSourceFactory(MediaFactory(context))
            .build()
        player = object : ForwardingPlayer(exoPlayer) {
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