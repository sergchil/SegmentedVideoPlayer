package com.chilisoft.segmentedexoplayer

enum class ScaleMode(val value:Int) {
    SCALE_MODE_FIT(0),
    SCALE_MODE_FIXED_WIDTH(1),
    SCALE_MODE_FIXED_HEIGHT(2),
    SCALE_MODE_FILL(3),
    SCALE_MODE_ZOOM(4);

    companion object {
        fun valueOf(value: Int): ScaleMode = values().first { it.value == value }
    }
}