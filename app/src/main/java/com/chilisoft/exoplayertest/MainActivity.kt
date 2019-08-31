package com.chilisoft.exoplayertest

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        segmentedPlayer.videoUrl = "https://bitdash-a.akamaihd.net/content/MI201109210084_1/m3u8s/f08e80da-bf1d-4e3d-8899-f0f6155f6efa.m3u8"
        segmentedPlayer.segments = mutableListOf(
            TimeUnit.SECONDS.toSeconds(30).toInt(),
            TimeUnit.SECONDS.toSeconds(30).toInt(),
            TimeUnit.MINUTES.toSeconds(1).toInt(),
            TimeUnit.MINUTES.toSeconds(1).toInt(),
            TimeUnit.SECONDS.toSeconds(30).toInt()
        )
    }

}
