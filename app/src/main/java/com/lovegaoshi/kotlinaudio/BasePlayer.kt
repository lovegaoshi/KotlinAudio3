@file: OptIn(UnstableApi::class) package com.lovegaoshi.kotlinaudio

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.ExoPlayer
import com.lovegaoshi.kotlinaudio.models.PlayerOptions
import com.lovegaoshi.kotlinaudio.models.setContentType
import com.lovegaoshi.kotlinaudio.models.setWakeMode
import com.lovegaoshi.kotlinaudio.player.initCache
import com.lovegaoshi.kotlinaudio.player.setupBuffer

class BasePlayer (
    private val context: Context,
    val options: PlayerOptions = PlayerOptions()) {
    lateinit var exoPlayer: ExoPlayer
    lateinit var player: ForwardingPlayer
    private var cache: SimpleCache? = null

    val currentItem: MediaItem?
        get() = exoPlayer.currentMediaItem

    val playerState: Player.State? = null

    fun setupPlayer() {

        if (options.cacheSize > 0) {
            cache = initCache(context, options.cacheSize)
        }
        exoPlayer = ExoPlayer
            .Builder(context)
            .setHandleAudioBecomingNoisy(options.handleAudioBecomingNoisy)
            .setMediaSourceFactory(MediaFactory(context, cache))
            .setWakeMode(setWakeMode(options.wakeMode))
            .apply {
                setLoadControl(setupBuffer(options.bufferOptions))
            }
            .build()

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(setContentType(options.audioContentType))
            .build();
        exoPlayer.setAudioAttributes(audioAttributes, options.handleAudioFocus);

        player = object : ForwardingPlayer(exoPlayer) {


            override fun isCommandAvailable(command: Int): Boolean {
                if (options.alwaysShowNext) {
                    return when (command) {
                        COMMAND_SEEK_TO_NEXT_MEDIA_ITEM -> true
                        COMMAND_SEEK_TO_NEXT -> true
                        else -> super.isCommandAvailable(command)
                    }
                }
                return super.isCommandAvailable(command)
            }

            override fun getAvailableCommands(): Player.Commands {
                if (options.alwaysShowNext) {
                    return super.getAvailableCommands().buildUpon()
                        .add(COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
                        .add(COMMAND_SEEK_TO_NEXT)
                        .build()
                }
                return super.getAvailableCommands()
            }

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
