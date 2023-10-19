package com.quixicon.presentation.fragments.draw.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import timber.log.Timber

// Stroke width for the the paint in DP.
private const val STROKE_WIDTH = 8

/**
 * Custom view that follows touch events to draw on a canvas.
 */

class DrawCanvasView @JvmOverloads constructor(
    context: Context,
    private val backgroundColor: Int,
    private val drawColor: Int,
    val initBitmap: Bitmap? = null,
    attrs: AttributeSet? = null,
) : View(context, attrs), LifecycleOwner {

    override val lifecycle: Lifecycle = LifecycleRegistry(this)

    // Holds the path you are currently drawing.
    private var path = Path()

    private lateinit var extraCanvas: Canvas
    lateinit var extraBitmap: Bitmap

    private var onTouchUpListener: OnTouchUp? = null

    // Set up the paint with which to draw.
    private val paint = Paint().apply {
        color = drawColor
        // Smooths out edges of what is drawn without affecting shape.
        isAntiAlias = true
        // Dithering affects how colors with higher-precision than the device are down-sampled.
        isDither = true
        style = Paint.Style.STROKE // default: FILL
        strokeJoin = Paint.Join.ROUND // default: MITER
        strokeCap = Paint.Cap.ROUND // default: BUTT
        strokeWidth = STROKE_WIDTH * resources.displayMetrics.density
    }

    /**
     * Don't draw every single pixel.
     * If the finger has has moved less than this distance, don't draw. scaledTouchSlop, returns
     * the distance in pixels a touch can wander before we think the user is scrolling.
     */
    // private val touchTolerance = ViewConfiguration.get(context).scaledTouchSlop
    private val touchTolerance = 0

    private var currentX = 0f
    private var currentY = 0f

    private var motionTouchEventX = 0f
    private var motionTouchEventY = 0f

    /**
     * Called whenever the view changes size.
     * Since the view starts out with no size, this is also called after
     * the view has been inflated and has a valid size.
     */
    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)

        Timber.e("On Size Change: $width $height, $oldWidth, $oldHeight")

        if (::extraBitmap.isInitialized) extraBitmap.recycle()

        initBitmap?.let {
            extraBitmap = it.copy(Bitmap.Config.ARGB_8888, true)
            extraCanvas = Canvas(extraBitmap)
        } ?: run {
            extraBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            extraCanvas = Canvas(extraBitmap)
            extraCanvas.drawColor(backgroundColor)
        }
    }

    override fun onDraw(canvas: Canvas) {
        // Draw the bitmap that has the saved path.
        canvas.drawBitmap(extraBitmap, 0f, 0f, null)
    }

    /**
     * No need to call and implement MyCanvasView#performClick, custom view
     * does not handle click actions.
     */

    override fun onTouchEvent(event: MotionEvent): Boolean {
        motionTouchEventX = event.x
        motionTouchEventY = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> touchStart()
            MotionEvent.ACTION_MOVE -> touchMove()
            MotionEvent.ACTION_UP -> touchUp()
        }
        return true
    }

    /**
     * The following methods factor out what happens for different touch events,
     * as determined by the onTouchEvent() when statement.
     * This keeps the when conditional block
     * concise and makes it easier to change what happens for each event.
     * No need to call invalidate because we are not drawing anything.
     */
    private fun touchStart() {
        path.reset()
        path.moveTo(motionTouchEventX, motionTouchEventY)
        currentX = motionTouchEventX
        currentY = motionTouchEventY
    }

    private fun touchMove() {
        val dx = Math.abs(motionTouchEventX - currentX)
        val dy = Math.abs(motionTouchEventY - currentY)
        if (dx >= touchTolerance || dy >= touchTolerance) {
            // QuadTo() adds a quadratic bezier from the last point,
            // approaching control point (x1,y1), and ending at (x2,y2).
            path.quadTo(currentX, currentY, (motionTouchEventX + currentX) / 2, (motionTouchEventY + currentY) / 2)
            currentX = motionTouchEventX
            currentY = motionTouchEventY
            // Draw the path in the extra bitmap to save it.
            extraCanvas.drawPath(path, paint)
        }
        // Invalidate() is inside the touchMove() under ACTION_MOVE because there are many other
        // types of motion events passed into this listener, and we don't want to invalidate the
        // view for those.
        invalidate()
    }

    private fun touchUp() {
        // Reset the path so it doesn't get drawn again.
        path.reset()
        Timber.e("Touch Up")
        onTouchUpListener?.onTouchUp()
    }

    fun clearCanvas() {
        extraCanvas.drawColor(backgroundColor)
        invalidate()
        onTouchUpListener?.onTouchUp()
    }

    fun setOnTouchUpListener(listener: OnTouchUp) {
        onTouchUpListener = listener
    }

    interface OnTouchUp {
        fun onTouchUp()
    }
}
