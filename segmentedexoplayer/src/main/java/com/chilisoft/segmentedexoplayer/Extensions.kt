package com.chilisoft.segmentedexoplayer

/**
 * Created by Sergey Chilingaryan on 2019-08-29.
 */

import android.content.res.Resources
import android.graphics.drawable.ClipDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.LinearLayoutCompat


fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
}


fun ProgressBar.customize(progressColor: Int, backgroundColor: Int, height: Int, cornerRadius: Int) {
    val d = (progressDrawable.mutate() as LayerDrawable)

    val radiusArray = FloatArray(8) {
        cornerRadius.toFloat()
    }

    // CHANGE THE BACKGROUND COLOR
    val topRoundRect = RoundRectShape(radiusArray, null, null)
    val topShapeDrawable = ShapeDrawable(topRoundRect)
    topShapeDrawable.paint.color = backgroundColor
    d.setDrawableByLayerId(android.R.id.background, topShapeDrawable)

    // CHANGE THE FOREGROUND SINCE WE CANNOT CHANGE THE CLIP DRAWABLE'S COLOR
    val bottomRoundRect = RoundRectShape(radiusArray, null, null)
    val bottomShapeDrawable = ShapeDrawable(bottomRoundRect)
    bottomShapeDrawable.paint.color = progressColor
    val clip = ClipDrawable(bottomShapeDrawable, Gravity.LEFT, ClipDrawable.HORIZONTAL)
    d.setDrawableByLayerId(android.R.id.progress, clip)

    // APPLY CHANGES TO PROGRESS BAR
    progressDrawable = d

    (layoutParams as ViewGroup.LayoutParams).height = height

}

fun LinearLayoutCompat.customize(
    spaceBetweenElements: Int,
    paddingLeft: Int,
    paddingTop: Int,
    paddingRight: Int,
    paddingBottom: Int
) {

    setPaddingRelative(paddingLeft, paddingTop, paddingRight, paddingBottom)

    // set space
    val divider = (dividerDrawable.mutate() as GradientDrawable)
    divider.setSize(spaceBetweenElements, spaceBetweenElements)
    dividerDrawable = divider.constantState?.newDrawable()
}

// dp to pixels
val Int.dp: Int get() = (this * Resources.getSystem().displayMetrics.density + 0.5f).toInt()
