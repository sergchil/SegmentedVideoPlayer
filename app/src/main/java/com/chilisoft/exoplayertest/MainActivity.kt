package com.chilisoft.exoplayertest

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Player.*
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.RawResourceDataSource
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.progress_item.view.*
import kotlin.math.max
import kotlin.math.min


class MainActivity : AppCompatActivity() {
    lateinit var runnable: Runnable
    val handler = Handler()
    private lateinit var player: SimpleExoPlayer
    //    private val videoUri = "https://skyfire.vimeocdn.com/1565888586-0x826fc0d986c8eab9e16a2cc96381d924c47f7282/353869590/sep/video/1438628557,1438628555,1438628554,1438628553,1438628551/master.mpd?base64_init=1"
    private val videoUri =
        "https://ak2.picdn.net/shutterstock/videos/30189112/preview/stock-footage-angry-businessman-looks-at-statistics-in-tablet-and-yells-at-employees-on-the-phone.webm"
//    private val videoUri = "https://manifest.googlevideo.com/api/manifest/dash/expire/1565904576/ei/YHpVXb-nJsfr7ATAsL34CA/ip/89.249.196.35/id/9b8ae4a95d5a3f4a/source/youtube/requiressl/yes/playback_host/r3---sn-boj5a45-x8oz.googlevideo.com/mm/31%2C29/mn/sn-boj5a45-x8oz%2Csn-n8v7snl7/ms/au%2Crdu/mv/m/mvi/2/pl/21/tx/23824522/txs/23824516%2C23824517%2C23824518%2C23824519%2C23824520%2C23824521%2C23824522%2C23824523%2C23824524%2C23824525%2C23824526/hfr/all/as/fmp4_audio_clear%2Cwebm_audio_clear%2Cwebm2_audio_clear%2Cfmp4_sd_hd_clear%2Cwebm2_sd_hd_clear/initcwndbps/1397500/mt/1565882895/fvip/18/itag/0/sparams/expire%2Cei%2Cip%2Cid%2Csource%2Crequiressl%2Ctx%2Ctxs%2Chfr%2Cas%2Citag/sig/ALgxI2wwRAIgejDaKZSTFm1ML_F1J0y0cWmEQLNsOO_cRyDC8w132vgCIGBYTJXilbVFZwtOGSmZJHU14ANdpv0QB9bVvsWqQTsu/lsparams/playback_host%2Cmm%2Cmn%2Cms%2Cmv%2Cmvi%2Cpl%2Cinitcwndbps/lsig/AHylml4wRQIgV9LwL8MFl7vRfQlgXbdVyBmptfBOd6RLIO_U0LAwFE8CIQDbVsoKfwxRkuiILNJ6x-cDrlh9nj_-LsIEcHVBMGAVbA%3D%3D"
//    private val videoUri = "http://www.streambox.fr/playlists/test_001/stream.m3u8"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rawDataSource = RawResourceDataSource(this)
        rawDataSource.open(DataSpec(RawResourceDataSource.buildRawResourceUri(R.raw.post_training)))


        player = ExoPlayerFactory.newSimpleInstance(this)
        playerView.player = player
        val dataSourceFactory = DefaultDataSourceFactory(this, Util.getUserAgent(this, "ExoTest"))
//        val videoSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(videoUri.toUri())
        val videoSource =
            ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(rawDataSource.uri)


        player.prepare(videoSource)
        player.volume = 0f
        player.playWhenReady = true


        val progresItems = ArrayList<View>()
        val chunkCount = 3


        for (i in 0 until chunkCount) {
            val progressItem = LayoutInflater.from(this).inflate(R.layout.progress_item,progress_container, false)
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

//                if (playbackState == STATE_IDLE) {
//                    progresItems.map { it.progressbar }.forEach { it.progress = player.duration.toInt()/chunkCount }
//                }

                if (playbackState == STATE_READY) {
                    progresItems.map { it.progressbar }.forEach { it.max = player.duration.toInt()/chunkCount }
                }

                if (playbackState == STATE_ENDED) {
                    progresItems.map { it.progressbar }.forEach { it.progress = player.duration.toInt()/chunkCount }
                }
            }
        })

        playerView.controller?.setProgressUpdateListener { position, bufferedPosition ->
            val chunk = player.duration.toInt()/chunkCount

            if (chunk == 0) return@setProgressUpdateListener


//            progresItems.map { it.progressbar }.forEach { it.max = chunk }
            val index = position.toInt()/chunk
            val progress = position.toInt().rem(chunk)

            if (index > progresItems.size - 1) return@setProgressUpdateListener

            progresItems
                .take(index)
                .map { it.progressbar }
                .forEach { it.progress = chunk }

            progresItems
                .takeLast(max(progresItems.size - 1 - index, 0))
                .mapNotNull { it.progressbar }
                .forEach { it.progress = 0 }

//            for (i in 0..index) {
//                progresItems[i].progressbar.progress = chunk
//            }
//
//            for (i in index until progresItems.size) {
//                progresItems[i].progressbar.progress = 0
//            }

            println("index = $index   progress = $chunk / $progress")

            progresItems[min(index, progresItems.size - 1)].progressbar.progress = progress
        }


//        runnable = Runnable {
//            progressbar.setProgress(((player.getCurrentPosition() * 1000 / player.getDuration()).toInt()))
//            handler.postDelayed(runnable, 10)
//        }
//
//        handler.postDelayed(runnable, 0)


    }


}
