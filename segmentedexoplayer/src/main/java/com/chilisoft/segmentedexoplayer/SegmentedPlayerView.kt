package com.chilisoft.segmentedexoplayer


import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.children
import com.chilisoft.segmentedexoplayer.player.IrisPlayerView
import com.chilisoft.segmentedexoplayer.player.SegmentedVideoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import kotlinx.android.synthetic.main.player_with_progress.view.*

class SegmentedPlayerView : FrameLayout {

    private lateinit var player: SimpleExoPlayer
    private lateinit var playerView: IrisPlayerView
    private lateinit var segmentedVideoPlayer: SegmentedVideoPlayer
    private lateinit var progressContainer: LinearLayoutCompat

    private var _progressColor: Int = Color.WHITE
    private var progressColor: Int
        get() = _progressColor
        set(value) {
            _progressColor = value
            updateProgress()
        }

    private var _progressBackgroundColor: Int = Color.RED
    private var progressBackgroundColor: Int
        get() = _progressBackgroundColor
        set(value) {
            _progressBackgroundColor = value
            updateProgress()
        }

    private var _progressHeight: Int = 4.dp
    private var progressHeight: Int
        get() = _progressHeight
        set(value) {
            _progressHeight = value
            updateProgress()
        }
    private var _progressPaddingLeft: Int = 0
    private var progressPaddingLeft: Int
        get() = _progressPaddingLeft
        set(value) {
            _progressPaddingLeft = value
            updateProgress()
        }
    private var _progressPaddingTop: Int = 0
    private var progressPaddingTop: Int
        get() = _progressPaddingTop
        set(value) {
            _progressPaddingTop = value
            updateProgress()
        }
    private var _progressPaddingRight: Int = 0
    private var progressPaddingRight: Int
        get() = _progressPaddingRight
        set(value) {
            _progressPaddingRight = value
            updateProgress()
        }
    private var _progressPaddingBottom: Int = 0
    private var progressPaddingBottom: Int
        get() = _progressPaddingBottom
        set(value) {
            _progressPaddingBottom = value
            updateProgress()
        }
    private var _progressDividerPadding: Int = 0
    private var progressDividerPadding: Int
        get() = _progressDividerPadding
        set(value) {
            _progressDividerPadding = value
            updateProgress()
        }
    private var _progressCornerRadius: Int = 4.dp
    private var progressCornerRadius: Int
        get() = _progressCornerRadius
        set(value) {
            _progressCornerRadius = value
            updateProgress()
        }
    private var _autoPlay: Boolean = true
    private var autoPlay: Boolean
        get() = _autoPlay
        set(value) {
            _autoPlay = value
            segmentedVideoPlayer.autoPlay = value
        }

    var videoUrl: String = ""
        set(value) {
            field = value
            segmentedVideoPlayer.videoUrl = value
        }

    var segments = mutableListOf<Int>()
        set(value) {
            field = value
            segmentedVideoPlayer.segments = value
            updateProgress()
        }

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        // Load attributes
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.SegmentedPlayerView, defStyle, 0
        )

        val view = inflate(R.layout.player_with_progress, true) as FrameLayout
        progressContainer = view.progress_container
        playerView = view.player_view


        _progressColor = a.getColor(R.styleable.SegmentedPlayerView_progressColor, progressColor)
        _progressBackgroundColor = a.getColor(R.styleable.SegmentedPlayerView_progressBackgroundColor, progressBackgroundColor)

        _progressHeight = a.getDimensionPixelSize(R.styleable.SegmentedPlayerView_progressHeight, progressHeight)
        _progressPaddingLeft = a.getDimensionPixelSize(R.styleable.SegmentedPlayerView_progressPaddingLeft, progressPaddingLeft)
        _progressPaddingTop = a.getDimensionPixelSize(R.styleable.SegmentedPlayerView_progressPaddingTop, progressPaddingTop)
        _progressPaddingRight = a.getDimensionPixelSize(R.styleable.SegmentedPlayerView_progressPaddingRight, progressPaddingRight)
        _progressPaddingBottom = a.getDimensionPixelSize(R.styleable.SegmentedPlayerView_progressPaddingBottom, progressPaddingBottom)
        _progressDividerPadding = a.getDimensionPixelSize(R.styleable.SegmentedPlayerView_progressDividerPadding, progressDividerPadding)
        _progressCornerRadius = a.getDimensionPixelSize(R.styleable.SegmentedPlayerView_progressCornerRadius, progressCornerRadius)
        _autoPlay = a.getBoolean(R.styleable.SegmentedPlayerView_autoPlay, autoPlay)

        a.recycle()

        player = ExoPlayerFactory.newSimpleInstance(context)
        segmentedVideoPlayer = SegmentedVideoPlayer(player, playerView, progress_container, _autoPlay)
        updateProgress()
    }


    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        segmentedVideoPlayer.release()
    }


    private fun updateProgress() {
        // progress bars
        progressContainer.children.map { it as ProgressBar }
            .forEach {
                it.customize(
                    _progressColor,
                    _progressBackgroundColor,
                    _progressHeight,
                    _progressCornerRadius
                )
            }

        // progress container
        progressContainer.customize(
            _progressDividerPadding,
            _progressPaddingLeft,
            _progressPaddingTop,
            _progressPaddingRight,
            _progressPaddingBottom
        )

    }

}
