package com.chilisoft.exoplayertest

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.chilisoft.segmentedexoplayer.SegmentedVideoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {
    private lateinit var player: SimpleExoPlayer
    private lateinit var segmentedVideoPlayer: SegmentedVideoPlayer


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val segments = mutableListOf(5,15,10)

        player = ExoPlayerFactory.newSimpleInstance(this)
        segmentedVideoPlayer = SegmentedVideoPlayer(player, playerView, progress_container, lifecycle)
        segmentedVideoPlayer.segments = segments
        segmentedVideoPlayer.videoUri = "http://videotest.idealmatch.com/welcome/welcome_v2.m3u8"
        segmentedVideoPlayer.autoPlay = true


        // init from outside


    }
}
