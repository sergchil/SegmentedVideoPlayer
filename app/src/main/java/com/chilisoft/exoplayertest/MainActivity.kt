package com.chilisoft.exoplayertest

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.chilisoft.exoplayertest.ExoPlayer.SegmentedVideoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private lateinit var player: SimpleExoPlayer
    private lateinit var segmentedVideoPlayer: SegmentedVideoPlayer


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        player = ExoPlayerFactory.newSimpleInstance(this)
        segmentedVideoPlayer = SegmentedVideoPlayer(player, playerView, progress_container)
    }
}
