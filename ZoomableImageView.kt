package com.liufy.thermaldisplay

import android.content.Context
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.ImageView
import kotlin.math.min

/**
 * V19 display image view.
 * Rendering remains identical to the stable V19 build.
 * Gesture transforms can be applied either by the view itself or by a transparent
 * MediaGestureOverlay layered above it.
 */
class ZoomableImageView(
    context: Context,
    private val fitFraction: Float,
    private val maxUserScale: Float
) : ImageView(context), ScaleGestureDetector.OnScaleGestureListener {

    private val transform = Matrix()
    private val scaleDetector = ScaleGestureDetector(context, this)

    private var userScale = 1f
    private var lastX = 0f
    private var lastY = 0f
    private var firstDrawable = true

    init {
        scaleType = ScaleType.MATRIX
        isClickable = true
        isFocusable = true
    }

    override fun setImageDrawable(drawable: Drawable?) {
        val hadDrawable = this.drawable != null
        val saved = Matrix(imageMatrix)
        super.setImageDrawable(drawable)
        if (drawable == null) return
        post {
            if (!hadDrawable || firstDrawable) {
                firstDrawable = false
                resetTransform()
            } else {
                imageMatrix = saved
            }
        }
    }

    fun setImageDrawablePreserveTransform(drawable: Drawable?) {
        val saved = Matrix(imageMatrix)
        val initialized = this.drawable != null
        super.setImageDrawable(drawable)
        post {
            if (initialized) imageMatrix = saved else resetTransform()
        }
    }

    fun resetTransform() {
        val d = drawable ?: return
        if (width <= 0 || height <= 0 || d.intrinsicWidth <= 0 || d.intrinsicHeight <= 0) return
        val targetW = width * fitFraction
        val targetH = height * fitFraction
        val base = min(targetW / d.intrinsicWidth.toFloat(), targetH / d.intrinsicHeight.toFloat())
        val dw = d.intrinsicWidth * base
        val dh = d.intrinsicHeight * base
        val dx = (width - dw) / 2f
        val dy = (height - dh) / 2f
        transform.reset()
        transform.postScale(base, base)
        transform.postTranslate(dx, dy)
        userScale = 1f
        imageMatrix = transform
        invalidate()
    }

    /** Apply user zoom in local view coordinates. */
    fun zoomBy(rawFactor: Float, focusX: Float, focusY: Float): Boolean {
        if (!rawFactor.isFinite() || rawFactor <= 0f) return false
        val requested = (userScale * rawFactor).coerceIn(1f, maxUserScale)
        val factor = requested / userScale
        if (!factor.isFinite() || kotlin.math.abs(factor - 1f) < 0.0001f) return false
        transform.set(imageMatrix)
        transform.postScale(factor, factor, focusX, focusY)
        imageMatrix = transform
        userScale = requested
        invalidate()
        return true
    }

    /** Apply user panning while zoomed. */
    fun panBy(dx: Float, dy: Float): Boolean {
        if (userScale <= 1.001f) return false
        if (!dx.isFinite() || !dy.isFinite()) return false
        transform.set(imageMatrix)
        transform.postTranslate(dx, dy)
        imageMatrix = transform
        invalidate()
        return true
    }

    fun currentUserScale(): Float = userScale

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0 && (oldw == 0 || oldh == 0)) post { resetTransform() }
    }

    // Keep V19's original direct touch path as a fallback. The transparent overlay
    // normally receives media gestures first in the stable-touch build.
    override fun onTouchEvent(event: MotionEvent): Boolean {
        parent?.requestDisallowInterceptTouchEvent(true)
        scaleDetector.onTouchEvent(event)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.x
                lastY = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                if (!scaleDetector.isInProgress && event.pointerCount == 1 && userScale > 1.001f) {
                    val dx = event.x - lastX
                    val dy = event.y - lastY
                    panBy(dx, dy)
                }
                lastX = event.x
                lastY = event.y
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                parent?.requestDisallowInterceptTouchEvent(false)
            }
        }
        return true
    }

    override fun onScale(detector: ScaleGestureDetector): Boolean =
        zoomBy(detector.scaleFactor, detector.focusX, detector.focusY)

    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean = true
    override fun onScaleEnd(detector: ScaleGestureDetector) = Unit
}
