package com.lovegaoshi.kotlinAudio

import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.media3.session.MediaBrowser
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import com.lovegaoshi.kotlinAudio.models.KAMediaItem
import com.lovegaoshi.kotlinAudio.ui.theme.KotlinAudioTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var browser: MediaBrowser

    override fun onStart() {
        super.onStart()
        val intent = Intent(this, MusicService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        val sessionToken =
            SessionToken(this, ComponentName(this, MusicService::class.java))
        val mediaItems = listOf(
            KAMediaItem(
                uri = "https://rntp.dev/example/Longing.mp3",
                title = "Longing",
                artist = "David Chavez",
                artworkUri = "https://rntp.dev/example/Longing.jpeg"
            ).mediaItem,
            KAMediaItem(
                uri = "https://rntp.dev/example/Soul%20Searching.mp3",
                title = "LSoul Searching (Demo)",
                artist = "David Chavez",
                artworkUri = "https://rntp.dev/example/Soul%20Searching.jpeg"
            ).mediaItem,
            KAMediaItem(
                uri = "https://rntp.dev/example/hls/whip/playlist.m3u8",
                title = "Whip",
                artworkUri = "https://rntp.dev/example/hls/whip/whip.jpeg"
            ).mediaItem,
            KAMediaItem(
                uri = "https://ais-sa5.cdnstream1.com/b75154_128mp3",
                title = "Smooth Jazz 24/7",
                artist = "David Chavez",
                artworkUri = "https://rntp.dev/example/smooth-jazz-24-7.jpeg"
            ).mediaItem,
        )
        val browserFuture = MediaBrowser.Builder(this, sessionToken).buildAsync()
        browserFuture.addListener({
            // MediaController is available here with controllerFuture.get()
            browserFuture.get().setMediaItems(mediaItems)
            browserFuture.get().prepare()
            browserFuture.get().play()
        }, MoreExecutors.directExecutor())

        val scope = CoroutineScope(Dispatchers.Main)
        scope.launch {
            browser = browserFuture.await()
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KotlinAudioTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("Android")
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        browser.release()
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    KotlinAudioTheme {
        Greeting("Android")
    }
}