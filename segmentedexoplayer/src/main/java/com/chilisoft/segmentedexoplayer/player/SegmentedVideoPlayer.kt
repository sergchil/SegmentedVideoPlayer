package com.chilisoft.segmentedexoplayer.player

import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.ProgressBar
import androidx.annotation.RawRes
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.net.toUri
import androidx.core.view.doOnLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.chilisoft.segmentedexoplayer.R
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.RawResourceDataSource
import com.google.android.exoplayer2.util.Util
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit
import kotlin.math.max

/**
 * Created by Sergey Chilingaryan on 2019-08-26.
 */
class SegmentedVideoPlayer(private val player: SimpleExoPlayer, playerView: IrisPlayerView, progressBarContainer: LinearLayoutCompat, lifecycle: Lifecycle) : LifecycleObserver {
    var pauseDelay = 150L
    var segments = mutableListOf<Int>() // eg [5,15,10] in SECONDS
    var videoUri = ""
    var autoPlay = true

    private val internalSegments = mutableListOf<Segment>()
    private val dataSourceFactory: DefaultDataSourceFactory
    private val progressUpdateListener: IrisPlayerControlView.ProgressUpdateListener by lazy {
        IrisPlayerControlView.ProgressUpdateListener { position, _ -> onUpdateProgress(position) }
    }
    private val delayedPauseRunnable = Runnable { pause() }

    init {
        lifecycle.addObserver(this) // start listeneing
        val context = playerView.context
        dataSourceFactory = DefaultDataSourceFactory(context, Util.getUserAgent(context, "ExoTest"))
        val layoutInflater = LayoutInflater.from(context)


        // convert from [5,15,10] -> [{0,5000}, {5000,20000}, {20000,30000}]
        progressBarContainer.removeAllViews()
        segments.mapIndexedTo(internalSegments) { index: Int, d: Int ->
            val duration = TimeUnit.SECONDS.toMillis(d.toLong())
            val start = if (index == 0) 0L else internalSegments[index - 1].end
            val end = start + duration

            // add progress bar to container and set max to duration (millis)
            val progressBar = layoutInflater.inflate(R.layout.progress_bar_item, progressBarContainer, false) as ProgressBar
            progressBar.max = duration.toInt()
            progressBarContainer.addView(progressBar)

            Segment(start, end, duration, progressBar, index)
        }


        val videoSource = if (true) {
            createRawMediaSource(context, R.raw.test_footage)
        } else {
            createHlsMediaSource(videoUri)
        }


        player.apply {
            playerView.player = this
            prepare(videoSource)
            playWhenReady = autoPlay

            onReady {
                playerView.controller?.setTimeBarMinUpdateInterval(1000) // HIGH CPU USAGE !!!
                playerView.controller?.setProgressUpdateListener(progressUpdateListener)
            }

            onPlaybackEnd {
                internalSegments.forEach(Segment::completed)
            }
        }

        // handle touches
        playerView.doOnLayout {
            val center = it.width / 2

            it.setOnTouchListener { view, motionEvent ->

                if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                    view.removeCallbacks(delayedPauseRunnable)
                    view.postDelayed(delayedPauseRunnable, pauseDelay) // pause after pauseDelay
                }

                if (motionEvent.action == MotionEvent.ACTION_UP) {
                    view.removeCallbacks(delayedPauseRunnable)

                    val duration = motionEvent.eventTime - motionEvent.downTime

                    val isInRewindArea = motionEvent.x.toInt() in 0..center // touch is close to the left edge of the screen

                    if (duration <= pauseDelay) {
                        if (isInRewindArea) rewind() else forward()
                    } else {
                        play()
                    }

                    return@setOnTouchListener true
                }

                true
            }
        }


    }

    // cleanup
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        pause()
        player.release()
    }

    private fun createHlsMediaSource(url: String): MediaSource {
        return HlsMediaSource.Factory(dataSourceFactory).createMediaSource(url.toUri())
    }

    private fun createRawMediaSource(context: Context, @RawRes id: Int): MediaSource? {
        val rawDataSource = RawResourceDataSource(context)
        rawDataSource.open(DataSpec(RawResourceDataSource.buildRawResourceUri(R.raw.test_footage)))
        return ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(rawDataSource.uri ?: return null)
    }

    private fun findSegment(playerPosition: Long): Segment? = internalSegments.firstOrNull { playerPosition in it.start until it.end }

    private fun onUpdateProgress(position: Long) {
        val segment = findSegment(position) ?: return
        val progress = position - segment.start

        // if seeks forward from middle, set to max
        internalSegments.subList(0, segment.index).forEach {
            it.completed()
        }

        internalSegments.subList(segment.index, internalSegments.size).forEach {
            it.notStarted()
        }

        segment.setProgress(progress)
    }

    private fun pause() {
        player.playWhenReady = false
    }

    private fun play() {
        player.playWhenReady = true
    }

    private fun forward() {
        val segment = findSegment(player.currentPosition) ?: return
        val nextIndex = segment.index + 1
        val nextSegment = internalSegments.getOrNull(nextIndex)
        // forward to last segment end
        nextSegment?.let {
            player.seekTo(it.start)
        } ?: run {
            player.seekTo(segment.end)
        }

    }

    // todo improve the logic of quick tapping
    private fun rewind() {
        val currentSegment = findSegment(player.currentPosition)

        // rewind to last segment start
        if (currentSegment == null) {
            player.seekTo(internalSegments.last().start)
            return
        }

        val previousSegmentIndex = max(0, currentSegment.index - 1)
        val previousSegment = internalSegments[previousSegmentIndex]

        player.seekTo(previousSegment.start)

    }

    private fun SimpleExoPlayer.onReady(listener: SimpleExoPlayer.() -> Unit) {

        this.addListener(object : Player.EventListener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                super.onPlayerStateChanged(playWhenReady, playbackState)
                if (playbackState == Player.STATE_READY) {
                    listener()
                }
            }
        })
    }

    private fun SimpleExoPlayer.onPlaybackEnd(onPlaybackEnd: SimpleExoPlayer.() -> Unit) {
        this.addListener(object : Player.EventListener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                super.onPlayerStateChanged(playWhenReady, playbackState)
                if (playbackState == Player.STATE_ENDED) {
                    onPlaybackEnd.invoke(this@onPlaybackEnd)
                }
            }
        })
    }

    private data class Segment(var start: Long, var end: Long, var duration: Long, var progressBar: ProgressBar, var index: Int) {

        fun completed() {
            progressBar.progress = duration.toInt()
        }

        fun notStarted() {
            progressBar.progress = 0
        }

        fun setProgress(progress: Long) {
            progressBar.progress = progress.toInt()
        }
    }

    fun <T> WeakReference<T>.safe(body: T.() -> Unit) {
        this.get()?.body()
    }

}
