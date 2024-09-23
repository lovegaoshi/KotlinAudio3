package com.lovegaoshi.kotlinaudio.models

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import java.util.UUID

interface AudioItem {
    var audioUrl: String
    val type: MediaType
    var artist: String?
    var title: String?
    var albumTitle: String?
    val artwork: String?
    val duration: Long?
    val options: AudioItemOptions?
    val mediaId: String?
}

data class AudioItemOptions(
    val headers: HashMap<String, String>? = null,
    val userAgent: String? = null,
    val resourceId: Int? = null
)

enum class MediaType(val value: String) {
    /**
     * The default media type. Should be used for streams over HTTP or files
     */
    DEFAULT("default"),

    /**
     * The DASH media type for adaptive streams. Should be used with DASH manifests.
     */
    DASH("dash"),

    /**
     * The HLS media type for adaptive streams. Should be used with HLS playlists.
     */
    HLS("hls"),

    /**
     * The SmoothStreaming media type for adaptive streams. Should be used with SmoothStreaming manifests.
     */
    SMOOTH_STREAMING("smoothstreaming");
}

data class DefaultAudioItem(
    override var audioUrl: String,

    /**
     * Set to [MediaType.DEFAULT] by default.
     */
    override val type: MediaType = MediaType.DEFAULT,

    override var artist: String? = null,
    override var title: String? = null,
    override var albumTitle: String? = null,
    override var artwork: String? = null,
    override val duration: Long? = null,
    override val options: AudioItemOptions? = null,
    override val mediaId: String? = null,
) : AudioItem

class AudioItemHolder(
    var audioItem: AudioItem
) {
    var artworkBitmap: Bitmap? = null
}

fun AudioItem2MediaItem(audioItem: AudioItem): MediaItem {
    return MediaItem.Builder()
        .setMediaId(audioItem.mediaId ?: UUID.randomUUID().toString())
        .setUri(audioItem.audioUrl)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(audioItem.title)
                .setArtist(audioItem.artist)
                .setArtworkUri(Uri.parse(audioItem.artwork))
                .setExtras(Bundle().apply {
                    audioItem.options?.headers?.let {
                        putSerializable("headers", audioItem.options!!.headers)
                    }
                    audioItem.options?.userAgent?.let {
                        putString("user-agent", it)
                    }
                    audioItem.options?.resourceId?.let {
                        putInt("resource-id", it)
                    }
                    putString("type", audioItem.type.toString())
                    putString("uri", audioItem.audioUrl)
                })
                .build()
        )
        .setTag(audioItem)
        .build()
}

fun MediaItem2AudioItem(item: MediaItem?): AudioItem? {
    return item?.localConfiguration?.tag as AudioItem?
}