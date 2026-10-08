package com.liufy.thermaldisplay

import android.content.Context
import android.graphics.Color
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import kotlin.math.hypot

/**
 * Transparent touch surface placed directly above a media ImageView.
 *
 * This is intentionally separate from image rendering: animation frame refreshes and
 * thermal image swaps never replace the object that owns the touch stream. That keeps
 * V19's stable media/rendering path while making pinch input deterministic.
 */
class MediaGestureOverlay(
    context: Context,
    private val target: ZoomableImageView
) : View(context) {

    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean = true
        override fun onDoubleTap(e: MotionEvent): Boolean {
            target.resetTransform()
            return true
        }
    })

    private var lastSpan = 0f
    private var lastFocusX = 0f
    private var lastFocusY = 0f
    private var lastX = 0f
    private var lastY = 0f
    private var pinching = false

    private var maxPointersSeen = 0

    init {
        setBackgroundColor(Color.TRANSPARENT)
        isClickable = true
        isFocusable = true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        parent?.requestDisallowInterceptTouchEvent(true)
        gestureDetector.onTouchEvent(event)
        if (event.pointerCount > maxPointersSeen) maxPointersSeen = event.pointerCount

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                pinching = false
                lastSpan = 0f
                lastX = event.x
                lastY = event.y
                return true
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                if (event.pointerCount >= 2) {
                    pinching = true
                    lastSpan = span(event)
                    lastFocusX = focusX(event)
                    lastFocusY = focusY(event)
                }
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                if (event.pointerCount >= 2) {
                    val currentSpan = span(event)
                    val fx = focusX(event)
                    val fy = focusY(event)

                    if (!pinching || lastSpan <= 0f) {
                        pinching = true
                        lastSpan = currentSpan
                        lastFocusX = fx
                        lastFocusY = fy
                    } else if (currentSpan > 0f) {
                        // Zoom around the live two-finger midpoint.
                        target.zoomBy(currentSpan / lastSpan, fx, fy)
                        // Two-finger translation feels much more natural on large touch frames.
                        target.panBy(fx - lastFocusX, fy - lastFocusY)
                        lastSpan = currentSpan
                        lastFocusX = fx
                        lastFocusY = fy
                    }
                    return true
                }

                if (!pinching && event.pointerCount == 1) {
                    val x = event.x
                    val y = event.y
                    target.panBy(x - lastX, y - lastY)
                    lastX = x
                    lastY = y
                }
                return true
            }

            MotionEvent.ACTION_POINTER_UP -> {
                pinching = false
                lastSpan = 0f
                val lifted = event.actionIndex
                val remainIndex = if (lifted == 0 && event.pointerCount > 1) 1 else 0
                if (remainIndex < event.pointerCount) {
                    lastX = event.getX(remainIndex)
                    lastY = event.getY(remainIndex)
                }
                return true
            }

            MotionEvent.ACTION_UP -> {
                pinching = false
                lastSpan = 0f
                parent?.requestDisallowInterceptTouchEvent(false)
                performClick()
                return true
            }

            MotionEvent.ACTION_CANCEL -> {
                pinching = false
                lastSpan = 0f
                parent?.requestDisallowInterceptTouchEvent(false)
                return true
            }
        }
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    fun getMaxPointersSeen(): Int = maxPointersSeen

    private fun span(event: MotionEvent): Float {
        if (event.pointerCount < 2) return 0f
        return hypot(event.getX(1) - event.getX(0), event.getY(1) - event.getY(0))
    }

    private fun focusX(event: MotionEvent): Float = (event.getX(0) + event.getX(1)) * 0.5f
    private fun focusY(event: MotionEvent): Float = (event.getY(0) + event.getY(1)) * 0.5f
}
