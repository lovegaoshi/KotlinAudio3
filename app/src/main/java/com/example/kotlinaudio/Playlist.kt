package com.example.kotlinaudio

import com.doublesymmetry.kotlinaudio.models.AudioItem2MediaItem
import com.doublesymmetry.kotlinaudio.models.AudioItemOptions
import com.doublesymmetry.kotlinaudio.models.DefaultAudioItem
import com.doublesymmetry.kotlinaudio.models.MediaType

class Playlist {
    val playlist = listOf(
        AudioItem2MediaItem(DefaultAudioItem(
            audioUrl = "https://rntp.dev/example/Longing.mp3",
            title = "Longing",
            artist = "David Chavez",
            artwork = "https://rntp.dev/example/Longing.jpeg",
            options = AudioItemOptions(
                userAgent = "myuseragent",
                headers = hashMapOf("some-header" to "some-result")
            )
        )),
        AudioItem2MediaItem(DefaultAudioItem(
            audioUrl = "https://rntp.dev/example/Soul%20Searching.mp3",
            title = "LSoul Searching (Demo)",
            artist = "David Chavez",
            artwork = "https://rntp.dev/example/Soul%20Searching.jpeg"
        )),
        AudioItem2MediaItem(DefaultAudioItem(
            audioUrl = "https://rntp.dev/example/hls/whip/playlist.m3u8",
            title = "Whip",
            artwork = "https://rntp.dev/example/hls/whip/whip.jpeg",
            type = MediaType.HLS

        )),
            AudioItem2MediaItem(DefaultAudioItem(
                audioUrl = "https://ais-sa5.cdnstream1.com/b75154_128mp3",
            title = "Smooth Jazz 24/7",
            artist = "David Chavez",
            artwork = "https://rntp.dev/example/smooth-jazz-24-7.jpeg"
            )),)
}