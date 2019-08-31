package com.chilisoft.exoplayertest

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        segmentedPlayer.videoUrl = "http://videotest.idealmatch.com/welcome/welcome_v2.m3u8"
        segmentedPlayer.segments = mutableListOf(10, 10, 10, 4)
    }

}
