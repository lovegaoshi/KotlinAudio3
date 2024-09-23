@file: OptIn(UnstableApi::class) package com.lovegaoshi.kotlinaudio.player

import android.content.Context
import android.media.AudioManager
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.ExoPlayer
import com.doublesymmetry.kotlinaudio.event.PlayerEventHolder
import com.doublesymmetry.kotlinaudio.models.AudioItem
import com.doublesymmetry.kotlinaudio.models.AudioPlayerState
import com.doublesymmetry.kotlinaudio.models.PlaybackError
import com.lovegaoshi.kotlinaudio.models.PlayerOptions
import com.lovegaoshi.kotlinaudio.models.setContentType
import com.lovegaoshi.kotlinaudio.models.setWakeMode
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay

class BasePlayer internal constructor(
    internal val context: Context,
    val options: PlayerOptions = PlayerOptions()
) : AudioManager.OnAudioFocusChangeListener {

    lateinit var exoPlayer: ExoPlayer
    lateinit var player: ForwardingPlayer
    private val scope = MainScope()
    private var cache: SimpleCache? = null
    private val playerEventHolder = PlayerEventHolder()
    private val focusManager = FocusManager(context, listener=this, options=options)

    open val currentItem: AudioItem?
        get() = exoPlayer.currentMediaItem?.localConfiguration?.tag as AudioItem

    var playbackError: PlaybackError? = null
    var playerState: AudioPlayerState = AudioPlayerState.IDLE
        private set(value) {
            if (value != field) {
                field = value
                playerEventHolder.updateAudioPlayerState(value)
                if (!options.handleAudioFocus) {
                    when (value) {
                        AudioPlayerState.IDLE,
                        AudioPlayerState.ERROR -> focusManager.abandonAudioFocusIfHeld()
                        AudioPlayerState.READY -> focusManager.requestAudioFocus()
                        else -> {}
                    }
                }
            }
        }

    var playWhenReady: Boolean
        get() = exoPlayer.playWhenReady
        set(value) {
            exoPlayer.playWhenReady = value
        }

    val duration: Long
        get() {
            return if (exoPlayer.duration == C.TIME_UNSET) 0
            else exoPlayer.duration
        }

    val isCurrentMediaItemLive: Boolean
        get() = exoPlayer.isCurrentMediaItemLive

    private var oldPosition = 0L

    val position: Long
        get() {
            return if (exoPlayer.currentPosition == C.POSITION_UNSET.toLong()) 0
            else exoPlayer.currentPosition
        }

    val bufferedPosition: Long
        get() {
            return if (exoPlayer.bufferedPosition == C.POSITION_UNSET.toLong()) 0
            else exoPlayer.bufferedPosition
        }

    private var volumeMultiplier = 1f
        private set(value) {
            field = value
            volume = volume
        }

    var volume: Float
        get() = exoPlayer.volume
        set(value) {
            exoPlayer.volume = value * volumeMultiplier
        }

    /**
     * fade volume of the current exoPlayer by a simple linear function.
     */
    fun fadeVolume(volume: Float = 1f, duration: Long = 500, interval: Long = 20L, callback: () -> Unit = { }): Deferred<Unit> {
        return scope.async {
            val volumeDiff = (volume - exoPlayer.volume) * interval / duration
            var fadeInDuration = duration
            while (fadeInDuration > 0) {
                fadeInDuration -= interval
                exoPlayer.volume += volumeDiff
                delay(interval)
            }
            exoPlayer.volume = volume
            callback()
            return@async
        }
    }

    var playbackSpeed: Float
        get() = exoPlayer.playbackParameters.speed
        set(value) {
            exoPlayer.setPlaybackSpeed(value)
        }

    val isPlaying
        get() = exoPlayer.isPlaying

    private var wasDucking = false

    init {

        if (options.cacheSize > 0) {
            cache = Cache.initCache(context, options.cacheSize)
        }
        exoPlayer = ExoPlayer
            .Builder(context)
            .setHandleAudioBecomingNoisy(options.handleAudioBecomingNoisy)
            .setMediaSourceFactory(com.lovegaoshi.kotlinaudio.MediaFactory(context, cache))
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

    override fun onAudioFocusChange(focusChange: Int) {
        Log.d("APM","Audio focus changed")
    }
}
