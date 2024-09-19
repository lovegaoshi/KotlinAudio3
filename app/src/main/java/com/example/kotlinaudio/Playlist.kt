package com.example.kotlinaudio

import com.lovegaoshi.kotlinaudio.models.AudioItem
import com.lovegaoshi.kotlinaudio.models.AudioItemOptions

class Playlist {
    val playlist = listOf(
        AudioItem(
            uri = "https://rntp.dev/example/Longing.mp3",
            title = "Longing",
            artist = "David Chavez",
            artworkUri = "https://rntp.dev/example/Longing.jpeg",
            options = AudioItemOptions(
                userAgent = "myuseragent",
                resourceId = 1,
                headers = hashMapOf("some-header" to "some-result")
            )
        ).mediaItem,
        AudioItem(
            uri = "https://rntp.dev/example/Soul%20Searching.mp3",
            title = "LSoul Searching (Demo)",
            artist = "David Chavez",
            artworkUri = "https://rntp.dev/example/Soul%20Searching.jpeg"
        ).mediaItem,
        AudioItem(
            uri = "https://rntp.dev/example/hls/whip/playlist.m3u8",
            title = "Whip",
            artworkUri = "https://rntp.dev/example/hls/whip/whip.jpeg"
        ).mediaItem,
        AudioItem(
            uri = "https://ais-sa5.cdnstream1.com/b75154_128mp3",
            title = "Smooth Jazz 24/7",
            artist = "David Chavez",
            artworkUri = "https://rntp.dev/example/smooth-jazz-24-7.jpeg"
        ).mediaItem)
}