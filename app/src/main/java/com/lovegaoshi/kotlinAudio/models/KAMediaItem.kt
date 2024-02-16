package com.lovegaoshi.kotlinAudio.models

import android.net.Uri
import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import java.util.UUID

class KAMediaItem(
    uri: String,
    mediaId: String = UUID.randomUUID().toString(),
    extras: Bundle = Bundle.EMPTY,
    title: String = "",
    artist: String = "",
    artworkUri: String = ""
    ) {

    val mediaItem = MediaItem.Builder()
        .setMediaId(mediaId)
        .setUri(uri)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(artist)
                .setArtworkUri(Uri.parse(artworkUri))
                .setExtras(extras)
                .build()
        )
        .build()
}