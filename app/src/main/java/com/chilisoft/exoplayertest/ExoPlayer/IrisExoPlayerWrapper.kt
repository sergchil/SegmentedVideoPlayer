package com.chilisoft.exoplayertest.ExoPlayer

import android.widget.ProgressBar
import com.google.android.exoplayer2.SimpleExoPlayer

/**
 * Created by Sergey Chilingaryan on 2019-08-26.
 */
class IrisExoPlayerWrapper(var exoPlayer: SimpleExoPlayer) {
    val segments = mutableListOf<Int>()
    val progresItems = ArrayList<ProgressBar>()
    private val videoUri = "http://videotest.idealmatch.com/welcome/welcome_v2.m3u8"
    val segmentsCount = 1


}
