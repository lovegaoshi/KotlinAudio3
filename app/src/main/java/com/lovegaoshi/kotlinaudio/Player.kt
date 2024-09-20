@file: OptIn(UnstableApi::class) package com.lovegaoshi.kotlinaudio

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.DatabaseProvider
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultLoadControl.Builder
import androidx.media3.exoplayer.DefaultLoadControl.DEFAULT_BACK_BUFFER_DURATION_MS
import androidx.media3.exoplayer.DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS
import androidx.media3.exoplayer.DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_MS
import androidx.media3.exoplayer.DefaultLoadControl.DEFAULT_MAX_BUFFER_MS
import androidx.media3.exoplayer.DefaultLoadControl.DEFAULT_MIN_BUFFER_MS
import androidx.media3.exoplayer.ExoPlayer
import com.lovegaoshi.kotlinaudio.models.BufferOptions
import com.lovegaoshi.kotlinaudio.models.PlayerOptions
import com.lovegaoshi.kotlinaudio.models.setContentType
import com.lovegaoshi.kotlinaudio.models.setWakeMode
import java.io.File

class Player (
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
            val db: DatabaseProvider = StandaloneDatabaseProvider(context)
            cache = SimpleCache(
                File(context.cacheDir, "APM"),
                LeastRecentlyUsedCacheEvictor(options.cacheSize),
                db
            )
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

private fun setupBuffer(bufferConfig: BufferOptions): DefaultLoadControl {
    bufferConfig.apply {
        val multiplier =
            DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS / DEFAULT_BUFFER_FOR_PLAYBACK_MS
        val minBuffer =
            if (minBuffer != null && minBuffer != 0) minBuffer else DEFAULT_MIN_BUFFER_MS
        val maxBuffer =
            if (maxBuffer != null && maxBuffer != 0) maxBuffer else DEFAULT_MAX_BUFFER_MS
        val playBuffer =
            if (playBuffer != null && playBuffer != 0) playBuffer else DEFAULT_BUFFER_FOR_PLAYBACK_MS
        val backBuffer =
            if (backBuffer != null && backBuffer != 0) backBuffer else DEFAULT_BACK_BUFFER_DURATION_MS

        return Builder()
            .setBufferDurationsMs(minBuffer, maxBuffer, playBuffer, playBuffer * multiplier)
            .setBackBuffer(backBuffer, false)
            .build()
    }
}

