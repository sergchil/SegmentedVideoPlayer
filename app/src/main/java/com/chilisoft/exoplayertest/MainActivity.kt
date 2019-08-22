package com.chilisoft.exoplayertest

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Player.STATE_ENDED
import com.google.android.exoplayer2.Player.STATE_READY
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.RawResourceDataSource
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.custom_ctrl.view.*
import kotlinx.android.synthetic.main.progress_item.view.*
import kotlin.math.max
import kotlin.math.min


class MainActivity : AppCompatActivity() {
    private lateinit var player: SimpleExoPlayer
    private val videoUri = "https://ak2.picdn.net/shutterstock/videos/30189112/preview/stock-footage-angry-businessman-looks-at-statistics-in-tablet-and-yells-at-employees-on-the-phone.webm"

    val seekPoints = mutableListOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rawDataSource = RawResourceDataSource(this)
        rawDataSource.open(DataSpec(RawResourceDataSource.buildRawResourceUri(R.raw.post_training)))


        player = ExoPlayerFactory.newSimpleInstance(this)
        playerView.player = player
        val dataSourceFactory = DefaultDataSourceFactory(this, Util.getUserAgent(this, "ExoTest"))
        val videoSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(rawDataSource.uri)


        player.prepare(videoSource)
        player.volume = 0f
        player.playWhenReady = true

        val progresItems = ArrayList<View>()
        val chunkCount = 3

        // add progress bars
        for (i in 0 until chunkCount) {
            val progressItem = LayoutInflater.from(this).inflate(R.layout.progress_item, progress_container, false)
            progressItem.progressbar.tag = i
            if (i == 0) {
                progressItem.space.visibility = View.GONE
            }
            progresItems.add(progressItem)

            (progress_container as ViewGroup).addView(progressItem)
        }

        player.addListener(object : Player.EventListener {

            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                super.onPlayerStateChanged(playWhenReady, playbackState)

                if (playbackState == STATE_READY) {
                    val chunk = player.duration.toInt() / chunkCount
                    // todo find proper way to do this
                    seekPoints.clear()
                    seekPoints.add(0) // for rewind
                    seekPoints.add(chunk)
                    seekPoints.add(chunk * 2)
                    seekPoints.add(chunk * 3)
                    progresItems.map { it.progressbar }.forEach { it.max = chunk }
                }

                if (playbackState == STATE_ENDED) {
                    progresItems.map { it.progressbar }.forEach { it.progress = player.duration.toInt() / chunkCount }
                }
            }
        })

        playerView.controller?.setProgressUpdateListener { position, bufferedPosition ->
            val chunk = player.duration.toInt() / chunkCount

            if (chunk == 0) return@setProgressUpdateListener

            val index = position.toInt() / chunk
            val progress = position.toInt().rem(chunk)

            if (index > progresItems.size - 1) return@setProgressUpdateListener

            // if seeks forward from middle, set to max
            progresItems
                .take(index)
                .map { it.progressbar }
                .forEach { it.progress = chunk }

            // if seeks backward from middle, set to min
            progresItems
                .takeLast(max(progresItems.size - 1 - index, 0))
                .mapNotNull { it.progressbar }
                .forEach { it.progress = 0 }

            progresItems[index].progressbar.progress = progress
        }

        playerView.exo_ffwd.setOnClickListener {
            val chunk = player.duration.toInt() / chunkCount
            val indexDirty = min(player.currentPosition.toInt() / chunk, seekPoints.size-1).inc()
            val index = min(indexDirty, seekPoints.size - 1)
            player.seekTo(seekPoints[index].toLong())
        }


        playerView.exo_rew.setOnClickListener {
            val chunk = player.duration.toInt() / chunkCount
            var index = min(player.currentPosition.toInt() / chunk, seekPoints.size)

            if (player.currentPosition in index * chunk..(index * chunk) + 1000) {
                index = max(0, index.dec())
            }

            val progress = max(0, seekPoints[index].toLong())
            player.seekTo(progress)
        }
    }


}
