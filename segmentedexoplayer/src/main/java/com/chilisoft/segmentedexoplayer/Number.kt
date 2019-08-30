package com.chilisoft.segmentedexoplayer

import android.content.res.Resources

/**
 * Created by Sergey Chilingaryan on 2019-08-29.
 */

// dp to pixels
val Int.dp: Int get() = (this * Resources.getSystem().displayMetrics.density + 0.5f).toInt()
val Int.dp_f: Float get() = (this * Resources.getSystem().displayMetrics.density + 0.5f)

// float dp to pixels
val Float.dp: Int get() = (this * Resources.getSystem().displayMetrics.density + 0.5f).toInt()
val Float.dp_f: Float get() = (this * Resources.getSystem().displayMetrics.density + 0.5f)
