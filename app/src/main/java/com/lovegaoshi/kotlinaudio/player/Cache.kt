package com.lovegaoshi.kotlinaudio.player

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.DatabaseProvider
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import java.io.File

@UnstableApi fun initCache(context: Context, size: Long): SimpleCache {
    val db: DatabaseProvider = StandaloneDatabaseProvider(context)
    return SimpleCache(
        File(context.cacheDir, "APM"),
        LeastRecentlyUsedCacheEvictor(size),
        db
    )
}