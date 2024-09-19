package com.lovegaoshi.kotlinaudio.models

import android.net.Uri
import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import java.util.UUID

data class AudioItemOptions(
    val headers: HashMap<String, String>? = null,
    val userAgent: String? = null,
    val resourceId: Int? = null,
    val type: String? = null
)


class AudioItem(
    uri: String,
    mediaId: String = UUID.randomUUID().toString(),
    title: String = "",
    artist: String = "",
    artworkUri: String = "",
    private val options: AudioItemOptions? = null
    ) {

    val mediaItem = MediaItem.Builder()
        .setMediaId(mediaId)
        .setUri(uri)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(artist)
                .setArtworkUri(Uri.parse(artworkUri))
                .setExtras(Bundle().apply {
                    options?.headers?.let {
                        putSerializable("headers", options.headers)
                    }
                    options?.userAgent?.let {
                        putString("user-agent", it)
                    }
                    options?.resourceId?.let {
                        putInt("resource-id", it)
                    }
                    options?.type?.let {
                        putString("type", it)
                    }
                    putString("uri", uri)
                })
                .build()
        )
        .setTag(options)
        .build()
}
