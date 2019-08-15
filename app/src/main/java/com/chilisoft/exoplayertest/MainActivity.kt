package com.chilisoft.exoplayertest

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private val videoUri = "https://ak2.picdn.net/shutterstock/videos/30189112/preview/stock-footage-angry-businessman-looks-at-statistics-in-tablet-and-yells-at-employees-on-the-phone.webm".toUri()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val player = ExoPlayerFactory.newSimpleInstance(this)
        playerView.player = player
        val dataSourceFactory = DefaultDataSourceFactory(this, Util.getUserAgent(this, "ExoTest"))
        val videoSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(videoUri)

        player.prepare(videoSource)
        player.playWhenReady = true
    }
}
