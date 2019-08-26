package com.chilisoft.exoplayertest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnLayout
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
import kotlin.math.max
import kotlin.math.min


class MainActivity : AppCompatActivity() {
    private lateinit var player: SimpleExoPlayer
    //    private val videoUri = "https://ak2.picdn.net/shutterstock/videos/30189112/preview/stock-footage-angry-businessman-looks-at-statistics-in-tablet-and-yells-at-employees-on-the-phone.webm"
    private val videoUri = "http://videotest.idealmatch.com/welcome/welcome_v2.m3u8"

    val seekPoints = mutableListOf<Int>()
    val CHUNK_COUNT = 3
    val PAUSE_DELAY = 150L
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rawDataSource = RawResourceDataSource(this)
        rawDataSource.open(DataSpec(RawResourceDataSource.buildRawResourceUri(R.raw.test_footage)))



        player = ExoPlayerFactory.newSimpleInstance(this)

        playerView.player = player
        val dataSourceFactory = DefaultDataSourceFactory(this, Util.getUserAgent(this, "ExoTest"))
        val videoSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(rawDataSource.uri)
//        val videoSource = HlsMediaSource.Factory(dataSourceFactory).createMediaSource(videoUri.toUri())


        player.prepare(videoSource)
        player.volume = 0f
        player.playWhenReady = true

        val progresItems = ArrayList<ProgressBar>()


        // add progress bars
        for (i in 0 until CHUNK_COUNT) {
            val progressItem = LayoutInflater.from(this).inflate(R.layout.progress_bar_item, progress_container, false) as ProgressBar
            progresItems.add(progressItem)
            (progress_container as ViewGroup).addView(progressItem)
        }

        player.addListener(object : Player.EventListener {

            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                super.onPlayerStateChanged(playWhenReady, playbackState)

                if (playbackState == STATE_READY) {
                    val chunk = player.duration.toInt() / CHUNK_COUNT
                    // todo find proper way to do this
                    seekPoints.clear()
                    seekPoints.add(0) // for rewind
                    seekPoints.add(chunk)
                    seekPoints.add(chunk * 2)
                    seekPoints.add(chunk * 3)
                    progresItems.forEach { it.max = chunk }
                }

                if (playbackState == STATE_ENDED) {
                    progresItems.forEach { it.progress = player.duration.toInt() / CHUNK_COUNT }
                }
            }
        })

        playerView.controller?.setTimeBarMinUpdateInterval(17)
        playerView.controller?.setProgressUpdateListener { position, bufferedPosition ->
            val chunk = player.duration.toInt() / CHUNK_COUNT

            if (chunk == 0) return@setProgressUpdateListener

            val index = position.toInt() / chunk
            val progress = position.toInt().rem(chunk)

            if (index > progresItems.size - 1) return@setProgressUpdateListener

            // if seeks forward from middle, set to max
            progresItems
                .take(index)
                .forEach { it.progress = chunk }

            // if seeks backward from middle, set to min
            progresItems
                .takeLast(max(progresItems.size - 1 - index, 0))
                .forEach { it.progress = 0 }

            progresItems[index].progress = progress
        }

        playerView.doOnLayout {
            val center = it.width / 2

            it.setOnTouchListener { view, motionEvent ->

                if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                    view.removeCallbacks(r)
                    view.postDelayed(r, PAUSE_DELAY) // pause after PAUSE_DELAY
                }

                if (motionEvent.action == MotionEvent.ACTION_UP) {
                    view.removeCallbacks(r)

                    val duration = motionEvent.eventTime - motionEvent.downTime

                    if (duration <= PAUSE_DELAY) {
                        if (motionEvent.x.toInt() in 0..center) rewind() else forward()
                    } else {
                        play()
                    }

                    return@setOnTouchListener true
                }

                true
            }
        }
    }

    // delayed
    val r = Runnable {
        pouse()
    }

    fun pouse() {
        player.playWhenReady = false
    }

    fun play() {
        player.playWhenReady = true
    }

    fun forward() {
        val chunk = player.duration.toInt() / CHUNK_COUNT
        val indexDirty = min(player.currentPosition.toInt() / chunk, seekPoints.size - 1).inc()
        val index = min(indexDirty, seekPoints.size - 1)
        player.seekTo(seekPoints[index].toLong())
    }

    fun rewind() {
        val chunk = player.duration.toInt() / CHUNK_COUNT
        var index = min(player.currentPosition.toInt() / chunk, seekPoints.size)
        index = max(0, index.dec())
        val progress = max(0, seekPoints[index].toLong())
        player.seekTo(progress)
    }
}
