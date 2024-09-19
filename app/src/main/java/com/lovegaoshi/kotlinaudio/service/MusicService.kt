@file: OptIn(UnstableApi::class) package com.lovegaoshi.kotlinaudio.service

import android.content.Intent
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import androidx.annotation.OptIn
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import com.lovegaoshi.kotlinaudio.Player
import com.lovegaoshi.kotlinaudio.models.CustomButton

class MusicService : MediaLibraryService() {
    private val binder = MusicBinder()
    lateinit var mKAPlayer: com.lovegaoshi.kotlinaudio.Player
    lateinit var mediaSession: MediaLibrarySession

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession =
        mediaSession

    // Create your player and media session in the onCreate lifecycle event
    override fun onCreate() {
        super.onCreate()
        // Service is immediately set up for the KA example.
        setupService()
    }

    fun setupService(customActions: List<CustomButton> = arrayListOf()) {
        val player = ExoPlayer.Builder(this).build()

        val forwardingPlayer = object : ForwardingPlayer(player) {
            override fun play() {
                // Add custom logic
                super.play()
            }

            override fun setPlayWhenReady(playWhenReady: Boolean) {
                // Add custom logic
                super.setPlayWhenReady(playWhenReady)
            }
        }
        mKAPlayer = Player()
        mKAPlayer.setupPlayer()
        mediaSession = MediaLibrarySession.Builder(this, mKAPlayer.player, object : MediaLibrarySession.Callback {})
            .setCustomLayout(customActions.filter { v -> v.onLayout }.map{ v -> v.commandButton})
            .build()
    }

    // The user dismissed the app from the recent tasks
    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession.player
        if (!player.playWhenReady || player.mediaItemCount == 0) {
            // Stop the service if not playing, continue playing in the background
            // otherwise.
            stopSelf()
        }
    }

    // Remember to release the player and media session in onDestroy
    override fun onDestroy() {
        mediaSession.run {
            player.release()
            release()
            mediaSession.release()
        }
        super.onDestroy()
    }

    @UnstableApi private inner class CustomMediaSessionCallback(
        val customActions: List<CustomButton>
    ) : MediaLibrarySession.Callback {
        // Configure commands available to the controller in onConnect()
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            var sessionCommands = MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS.buildUpon()
            customActions.forEach{
                v -> sessionCommands = sessionCommands.add(SessionCommand(v.sessionCommand, Bundle.EMPTY))
            }
            return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                .setAvailableSessionCommands(sessionCommands.build())
                .build()
        }
    }

    inner class MusicBinder : Binder() {
        val service = this@MusicService
    }

    override fun onBind(intent: Intent?): IBinder? {
        val intentAction = intent?.action
        return if (intentAction != null) {
            super.onBind(intent)
        } else {
            binder
        }
    }
}