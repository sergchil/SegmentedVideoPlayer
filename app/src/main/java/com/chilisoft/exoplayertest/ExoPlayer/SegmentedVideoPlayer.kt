package com.chilisoft.exoplayertest.ExoPlayer

import android.content.Context
import android.net.Uri
import android.widget.ProgressBar
import androidx.annotation.RawRes
import androidx.core.net.toUri
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
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.max

/**
 * Created by Sergey Chilingaryan on 2019-08-26.
 */
class SegmentedVideoPlayer(var player: SimpleExoPlayer, var playerView: IrisPlayerView) {
    val segments = mutableListOf<Int>()
    val progresItems = ArrayList<ProgressBar>()
    private val videoUri = "http://videotest.idealmatch.com/welcome/welcome_v2.m3u8"
    val segmentsCount = 1
    val dataSourceFactory : DefaultDataSourceFactory
    val progressUpdateListener: IrisPlayerControlView.ProgressUpdateListener by lazy {
        IrisPlayerControlView.ProgressUpdateListener(::onUpdateProgress)
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

        }

        player.onPlaybackEnd {

        }


    }



    fun createHlsMediaSource(url: String) :MediaSource{
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

    fun getMaxProgress(): Int {
        return 100
    }




    fun SimpleExoPlayer.onReady(listener:() -> Unit) {

        this.addListener(object : Player.EventListener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                super.onPlayerStateChanged(playWhenReady, playbackState)
                if (playbackState == Player.STATE_READY) {
                    listener()
                }
            }

        })

    }

    fun <T> SimpleExoPlayer.onPlaybackEnd(onPlaybackEnd: T.()->Unit) {

        this.addListener(object : Player.EventListener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                super.onPlayerStateChanged(playWhenReady, playbackState)
                if (playbackState == Player.STATE_ENDED) {
                    onPlaybackEnd.invoke(this@onPlaybackEnd)
                }
            }

        })

    }

    class EventListener : Player.EventListener {

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            super.onPlayerStateChanged(playWhenReady, playbackState)

            if (playbackState == Player.STATE_READY) {
                playerView.controller?.setTimeBarMinUpdateInterval(17) // HIGH CPU USAGE !!!
                playerView.controller?.setProgressUpdateListener(progressUpdateListener)

                val segmentDuration = player.segmentDuration(CHUNK_COUNT).toInt()
                seekPoints.clear()
                for (i in 0..CHUNK_COUNT) {
                    seekPoints.add(i * segmentDuration) // first is always 0 for rewind
                }

                progresItems.forEach { it.max = player.maxProgress() }
            }

            if (playbackState == Player.STATE_ENDED) {
                progresItems.forEach { it.progress = player.maxProgress() }
            }
        }
    }


}
