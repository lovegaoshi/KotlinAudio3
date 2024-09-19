@file: OptIn(UnstableApi::class) package com.lovegaoshi.kotlinaudio

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.DatabaseProvider
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.ExoPlayer
import com.lovegaoshi.kotlinaudio.models.PlayerOptions
import java.io.File

class Player {
    lateinit var exoPlayer: ExoPlayer
    lateinit var player: ForwardingPlayer
    private var cache: SimpleCache? = null

    fun setupPlayer(context: Context, options: PlayerOptions = PlayerOptions()) {

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
            .setMediaSourceFactory(MediaFactory(context, cache))
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