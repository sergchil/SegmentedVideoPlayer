package com.chilisoft.exoplayertest.ExoPlayer

import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.annotation.RawRes
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.net.toUri
import androidx.core.view.doOnLayout
import com.chilisoft.exoplayertest.R
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.RawResourceDataSource
import com.google.android.exoplayer2.util.Util
import kotlin.math.max
import kotlin.math.min

/**
 * Created by Sergey Chilingaryan on 2019-08-26.
 */
class SegmentedVideoPlayer(var player: SimpleExoPlayer, playerView: IrisPlayerView, progressContainer: LinearLayoutCompat) {
    val PAUSE_DELAY = 150L

    private val segments = mutableListOf<Int>()
    private val progresItems = ArrayList<ProgressBar>()
    private val videoUri = "http://videotest.idealmatch.com/welcome/welcome_v2.m3u8"
    private val segmentsCount = 3
    private val dataSourceFactory: DefaultDataSourceFactory
    private val progressUpdateListener: IrisPlayerControlView.ProgressUpdateListener by lazy {
        IrisPlayerControlView.ProgressUpdateListener(::onUpdateProgress)
    }

    // delayed
    val delayedPauseRunnable = Runnable {
        pouse()
    }

    fun pouse() {
        player.playWhenReady = false
    }

    fun play() {
        player.playWhenReady = true
    }

    fun forward() {
        val chunk = player.duration.toInt() / segmentsCount
        val indexDirty = min(player.currentPosition.toInt() / chunk, segments.size - 1).inc()
        val index = min(indexDirty, segments.size - 1)
        player.seekTo(segments[index].toLong())
    }

    fun rewind() {
        val chunk = player.duration.toInt() / segmentsCount
        var index = min(player.currentPosition.toInt() / chunk, segments.size)
        index = max(0, index.dec())
        val progress = max(0, segments[index].toLong())
        player.seekTo(progress)
    }

    init {
        playerView.player = player
        val context = playerView.context

        dataSourceFactory = DefaultDataSourceFactory(context, Util.getUserAgent(context, "ExoTest"))

        val videoSource = if (true) {
            createRawMediaSource(context, R.raw.test_footage)
        } else {
            createHlsMediaSource(videoUri)
        }

        player.prepare(videoSource)
        player.playWhenReady = true


        player.onReady {
            playerView.controller?.setTimeBarMinUpdateInterval(17) // HIGH CPU USAGE !!!
            playerView.controller?.setProgressUpdateListener(progressUpdateListener)

            val segmentDuration = getSegmentDuration()
            segments.clear()
            for (i in 0..segmentsCount) {
                segments.add(i * segmentDuration) // first is always 0 for rewind
            }

            progresItems.forEach { it.max = getSegmentDuration() }
        }

        player.onPlaybackEnd {
            progresItems.forEach { it.progress = getSegmentDuration() }
        }


        playerView.doOnLayout {
            val center = it.width / 2

            it.setOnTouchListener { view, motionEvent ->

                if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                    view.removeCallbacks(delayedPauseRunnable)
                    view.postDelayed(delayedPauseRunnable, PAUSE_DELAY) // pause after PAUSE_DELAY
                }

                if (motionEvent.action == MotionEvent.ACTION_UP) {
                    view.removeCallbacks(delayedPauseRunnable)

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

        // add progress bars
        for (i in 0 until segmentsCount) {
            val progressItem = LayoutInflater.from(context).inflate(R.layout.progress_bar_item, progressContainer, false) as ProgressBar
            progresItems.add(progressItem)
            (progressContainer as ViewGroup).addView(progressItem)
        }


    }


    fun createHlsMediaSource(url: String): MediaSource {
        return HlsMediaSource.Factory(dataSourceFactory).createMediaSource(url.toUri())
    }

    fun createRawMediaSource(context: Context, @RawRes id: Int): MediaSource? {
        val rawDataSource = RawResourceDataSource(context)
        rawDataSource.open(DataSpec(RawResourceDataSource.buildRawResourceUri(R.raw.test_footage)))
        return ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(rawDataSource.uri ?: return null)
    }

    private fun onUpdateProgress(position: Long, bufferedPosition: Long) {
        val segmentDuration = getSegmentDuration()

        if (segmentDuration == 0) return

        val index = position.toInt() / segmentDuration
        val progress = position.toInt().rem(segmentDuration)

        if (index > progresItems.size - 1) return

        // if seeks forward from middle, set to max
        progresItems
            .take(index)
            .forEach { it.progress = segmentDuration }

        // if seeks backward from middle, set to min
        progresItems
            .takeLast(max(progresItems.size - 1 - index, 0))
            .forEach { it.progress = 0 }

        progresItems[index].progress = progress
    }

    fun getCurrentProgress(): Int {
        return (player.currentPosition * 100 / getSegmentDuration()).toInt()
    }

    fun getSegmentDuration(): Int {
        return player.duration.toInt() / segmentsCount
    }




    fun SimpleExoPlayer.onReady(listener: SimpleExoPlayer.() -> Unit) {

        this.addListener(object : Player.EventListener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                super.onPlayerStateChanged(playWhenReady, playbackState)
                if (playbackState == Player.STATE_READY) {
                    listener()
                }
            }

        })

    }

    fun SimpleExoPlayer.onPlaybackEnd(onPlaybackEnd: SimpleExoPlayer.() -> Unit) {

        this.addListener(object : Player.EventListener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                super.onPlayerStateChanged(playWhenReady, playbackState)
                if (playbackState == Player.STATE_ENDED) {
                    onPlaybackEnd.invoke(this@onPlaybackEnd)
                }
            }

        })

    }


}
