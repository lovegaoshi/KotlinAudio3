package com.lovegaoshi.kotlinAudio

import android.content.ComponentName
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
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.lovegaoshi.kotlinAudio.ui.theme.KotlinAudioTheme

class MainActivity : ComponentActivity() {
    private lateinit var browserFuture: ListenableFuture<MediaBrowser>

    override fun onStart() {
        super.onStart()
        val sessionToken =
            SessionToken(this, ComponentName(this, MusicService::class.java))
        browserFuture = MediaBrowser.Builder(this, sessionToken).buildAsync()
        browserFuture.addListener({
            // MediaBrowser is available here with browserFuture.get()
        }, MoreExecutors.directExecutor())
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
        MediaController.releaseFuture(browserFuture)
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