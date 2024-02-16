@file: OptIn(UnstableApi::class) package com.lovegaoshi.kotlinAudio

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaLibraryService.MediaLibrarySession

class Player {
    lateinit var mediaSession: MediaLibrarySession

    fun setupPlayer(
        context: Context,
        service: MediaLibraryService,
        player: ForwardingPlayer = object : ForwardingPlayer(ExoPlayer.Builder(context).build()) {},
        callback: MediaLibrarySession.Callback = object : MediaLibrarySession.Callback {},
        layout: List<CommandButton> = arrayListOf()
    ) {
        mediaSession = MediaLibrarySession.Builder(service, player, callback)
            .setCustomLayout(layout)
            .build()
    }

}