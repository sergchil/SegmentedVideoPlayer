package com.chilisoft.segmentedexoplayer

import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.ProgressBar
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.net.toUri
import androidx.core.view.doOnLayout
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.dummy_controller.view.*
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.max

/**
 * Created by Sergey Chilingaryan on 2019-08-26.
 */
class SegmentedVideoPlayer(private val player: SimpleExoPlayer, private val playerView: PlayerView, private val progressBarContainer: LinearLayoutCompat, var autoPlay: Boolean = true) {

    private val playbackEndCalled = AtomicBoolean(false) // detect player end and invoke callback only once
    private val internalSegments = mutableListOf<Segment>()
    private val dataSourceFactory: DefaultDataSourceFactory
    private val progressUpdateListener: PlayerControlView.ProgressUpdateListener by lazy {
        PlayerControlView.ProgressUpdateListener { position, _ -> onUpdateProgress(position) }
    }
    private val delayedPauseRunnable = Runnable { pause() }

    var scaleMode: ScaleMode = ScaleMode.SCALE_MODE_FIT
        set(value) {
            field = value
            playerView.resizeMode = value.value
        }
    var pauseDelay = 150L
    var segments = mutableListOf<Int>() // eg [5,15,10] in SECONDS
        set(value) {
            field = value

            // convert from [5,15,10] -> [{0,5000}, {5000,20000}, {20000,30000}]
            progressBarContainer.removeAllViews()
            segments.mapIndexedTo(internalSegments) { index: Int, d: Int ->
                val duration = TimeUnit.SECONDS.toMillis(d.toLong())
                val start = if (index == 0) 0L else internalSegments[index - 1].end
                val end = start + duration

                // add progress bar to container and set max to duration (millis)
                val progressBar = LayoutInflater.from(progressBarContainer.context).inflate(R.layout.progress_bar_item, progressBarContainer, false) as ProgressBar
                progressBar.max = duration.toInt()
                progressBarContainer.addView(progressBar)

                Segment(start, end, duration, progressBar, index)
            }

        }

    var videoUrl = ""
        set(value) {
            field = value
            player.prepare(createHlsMediaSource(value))
        }

    // Listeners
    var onPlaybackEnd: (() -> Unit)? = null
    var onReady: (() -> Unit)? = null
    var onError: (() -> Unit)? = null
    var onPause: (() -> Unit)? = null
    var onPlay: (() -> Unit)? = null
    var onRewind: (() -> Unit)? = null
    var onForward: (() -> Unit)? = null

    init {
        val context = playerView.context
        dataSourceFactory = DefaultDataSourceFactory(context, Util.getUserAgent(context, "ExoTest"))

        player.apply {
            playerView.player = this
            (playerView.exo_controller as? PlayerControlView)?.setProgressUpdateListener(progressUpdateListener)
//            (playerView.exo_controller as? PlayerControlView)?.setTimeBarMinUpdateInterval(1000) // HIGH CPU USAGE if value is small !!!

            prepare(createHlsMediaSource(videoUrl))
            playWhenReady = autoPlay

            onReady {
                // do something if needed
                onReady?.invoke()
            }

            onError {
                onError?.invoke()
            }

            onPlaybackEnd {
                playbackEnd()
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

    // called once
    private fun playbackEnd() {
        if (playbackEndCalled.get()) return

        internalSegments.forEach(Segment::completed)
        onPlaybackEnd?.let {
            it.invoke()
            playbackEndCalled.set(true)
        }
    }

    // cleanup
    fun release() {
        pause()
        (playerView.exo_controller as? PlayerControlView)?.setProgressUpdateListener(null)
        playerView.player = null
        playerView.setOnTouchListener(null)
        player.release()
        internalSegments.clear()
        segments.clear()
        onPlaybackEnd = null
        onReady = null
        onError = null
        onPause = null
        onPlay = null
        onRewind = null
        onForward = null
    }

    private fun createHlsMediaSource(url: String): MediaSource {
        return HlsMediaSource.Factory(dataSourceFactory).createMediaSource(url.toUri())
    }

    private fun findSegment(playerPosition: Long): Segment? = internalSegments.firstOrNull { playerPosition in it.start until it.end }

    private fun onUpdateProgress(position: Long) {
        if (player.duration == C.TIME_UNSET) return

        if (position >= player.duration) {
            playbackEnd()
            return
        }

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

        playbackEndCalled.set(false)
    }

    internal fun pause() {
        player.playWhenReady = false
        onPause?.invoke()
    }

    internal fun play() {
        player.playWhenReady = true
        onPlay?.invoke()
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
        onForward?.invoke()

    }

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
        onRewind?.invoke()

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

    private fun SimpleExoPlayer.onError(listener: SimpleExoPlayer.() -> Unit) {

        this.addListener(object : Player.EventListener {
            override fun onPlayerError(error: ExoPlaybackException?) {
                super.onPlayerError(error)
                listener()
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
}
