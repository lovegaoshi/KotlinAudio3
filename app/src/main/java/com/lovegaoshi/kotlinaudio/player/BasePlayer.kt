@file: OptIn(UnstableApi::class) package com.lovegaoshi.kotlinaudio.player

import android.content.Context
import android.media.AudioManager
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Metadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Player.Listener
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.ExoPlayer
import com.lovegaoshi.kotlinaudio.event.PlayerEventHolder
import com.lovegaoshi.kotlinaudio.models.AudioItem
import com.lovegaoshi.kotlinaudio.models.AudioItemTransitionReason
import com.lovegaoshi.kotlinaudio.models.AudioPlayerState
import com.lovegaoshi.kotlinaudio.models.PlayWhenReadyChangeData
import com.lovegaoshi.kotlinaudio.models.PlaybackError
import com.lovegaoshi.kotlinaudio.models.PositionChangedReason
import com.lovegaoshi.kotlinaudio.MediaFactory
import com.lovegaoshi.kotlinaudio.models.PlayerOptions
import com.lovegaoshi.kotlinaudio.models.setContentType
import com.lovegaoshi.kotlinaudio.models.setWakeMode
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import java.util.Locale

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
        get() = exoPlayer.currentMediaItem?.localConfiguration?.tag as AudioItem?

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
        exoPlayer.addListener(PlayerListener())
        playerEventHolder.updateAudioPlayerState(AudioPlayerState.IDLE)

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

    inner class PlayerListener : Listener {

        /**
         * Called when there is metadata associated with the current playback time.
         */
        override fun onMetadata(metadata: Metadata) {
            playerEventHolder.updateOnTimedMetadata(metadata)
        }

        override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
            playerEventHolder.updateOnCommonMetadata(mediaMetadata)
        }

        /**
         * A position discontinuity occurs when the playing period changes, the playback position
         * jumps within the period currently being played, or when the playing period has been
         * skipped or removed.
         */
        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int
        ) {
            this@BasePlayer.oldPosition = oldPosition.positionMs

            when (reason) {
                Player.DISCONTINUITY_REASON_AUTO_TRANSITION -> playerEventHolder.updatePositionChangedReason(
                    PositionChangedReason.AUTO(oldPosition.positionMs, newPosition.positionMs)
                )
                Player.DISCONTINUITY_REASON_SEEK -> playerEventHolder.updatePositionChangedReason(
                    PositionChangedReason.SEEK(oldPosition.positionMs, newPosition.positionMs)
                )
                Player.DISCONTINUITY_REASON_SEEK_ADJUSTMENT -> playerEventHolder.updatePositionChangedReason(
                    PositionChangedReason.SEEK_FAILED(
                        oldPosition.positionMs,
                        newPosition.positionMs
                    )
                )
                Player.DISCONTINUITY_REASON_REMOVE -> playerEventHolder.updatePositionChangedReason(
                    PositionChangedReason.QUEUE_CHANGED(
                        oldPosition.positionMs,
                        newPosition.positionMs
                    )
                )
                Player.DISCONTINUITY_REASON_SKIP -> playerEventHolder.updatePositionChangedReason(
                    PositionChangedReason.SKIPPED_PERIOD(
                        oldPosition.positionMs,
                        newPosition.positionMs
                    )
                )
                Player.DISCONTINUITY_REASON_INTERNAL -> playerEventHolder.updatePositionChangedReason(
                    PositionChangedReason.UNKNOWN(oldPosition.positionMs, newPosition.positionMs)
                )
            }
        }

        /**
         * Called when playback transitions to a media item or starts repeating a media item
         * according to the current repeat mode. Note that this callback is also called when the
         * playlist becomes non-empty or empty as a consequence of a playlist change.
         */
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            when (reason) {
                Player.MEDIA_ITEM_TRANSITION_REASON_AUTO -> playerEventHolder.updateAudioItemTransition(
                    AudioItemTransitionReason.AUTO(oldPosition)
                )
                Player.MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED -> playerEventHolder.updateAudioItemTransition(
                    AudioItemTransitionReason.QUEUE_CHANGED(oldPosition)
                )
                Player.MEDIA_ITEM_TRANSITION_REASON_REPEAT -> playerEventHolder.updateAudioItemTransition(
                    AudioItemTransitionReason.REPEAT(oldPosition)
                )
                Player.MEDIA_ITEM_TRANSITION_REASON_SEEK -> playerEventHolder.updateAudioItemTransition(
                    AudioItemTransitionReason.SEEK_TO_ANOTHER_AUDIO_ITEM(oldPosition)
                )
            }
        }

        /**
         * Called when the value returned from Player.getPlayWhenReady() changes.
         */
        override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
            val pausedBecauseReachedEnd = reason == Player.PLAY_WHEN_READY_CHANGE_REASON_END_OF_MEDIA_ITEM
            playerEventHolder.updatePlayWhenReadyChange(PlayWhenReadyChangeData(playWhenReady, pausedBecauseReachedEnd))
        }

        /**
         * The generic onEvents callback provides access to the Player object and specifies the set
         * of events that occurred together. It’s always called after the callbacks that correspond
         * to the individual events.
         */
        override fun onEvents(player: Player, events: Player.Events) {
            // Note that it is necessary to set `playerState` in order, since each mutation fires an
            // event.
            for (i in 0 until events.size()) {
                when (events[i]) {
                    Player.EVENT_PLAYBACK_STATE_CHANGED -> {
                        val state = when (player.playbackState) {
                            Player.STATE_BUFFERING -> AudioPlayerState.BUFFERING
                            Player.STATE_READY -> AudioPlayerState.READY
                            Player.STATE_IDLE ->
                                // Avoid transitioning to idle from error or stopped
                                if (
                                    playerState == AudioPlayerState.ERROR ||
                                    playerState == AudioPlayerState.STOPPED
                                )
                                    null
                                else
                                    AudioPlayerState.IDLE
                            Player.STATE_ENDED ->
                                if (player.mediaItemCount > 0) AudioPlayerState.ENDED
                                else AudioPlayerState.IDLE
                            else -> null // noop
                        }
                        if (state != null && state != playerState) {
                            playerState = state
                        }
                    }
                    Player.EVENT_MEDIA_ITEM_TRANSITION -> {
                        playbackError = null
                        if (currentItem != null) {
                            playerState = AudioPlayerState.LOADING
                            if (isPlaying) {
                                playerState = AudioPlayerState.READY
                                playerState = AudioPlayerState.PLAYING
                            }
                        }
                    }
                    Player.EVENT_PLAY_WHEN_READY_CHANGED -> {
                        if (!player.playWhenReady && playerState != AudioPlayerState.STOPPED) {
                            playerState = AudioPlayerState.PAUSED
                        }
                    }
                    Player.EVENT_IS_PLAYING_CHANGED -> {
                        if (player.isPlaying) {
                            playerState = AudioPlayerState.PLAYING
                        }
                    }
                }
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            val _playbackError = PlaybackError(
                error.errorCodeName
                    .replace("ERROR_CODE_", "")
                    .lowercase(Locale.getDefault())
                    .replace("_", "-"),
                error.message
            )
            playerEventHolder.updatePlaybackError(_playbackError)
            playbackError = _playbackError
            playerState = AudioPlayerState.ERROR
        }
    }
}
