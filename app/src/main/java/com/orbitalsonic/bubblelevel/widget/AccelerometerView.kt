package com.orbitalsonic.bubblelevel.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.PointF
import android.util.AttributeSet
import android.view.View
import com.orbitalsonic.bubblelevel.AndroidUtils
import com.orbitalsonic.bubblelevel.widget.AccelerometerDrawer
import kotlin.math.cos

class AccelerometerView : View {
    private var roll = 0f
    private var pitch = 0f
    private var point: PointF? = null
    private var maxAcceleration = 0
    private var maxMove = AndroidUtils.dpToPx(50).toInt() //dip

    private var k = (maxMove / (Math.PI / 2)).toFloat()
    private var drawer: AccelerometerDrawer? = null
    private val isSimple = false

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context)
    }

    private fun init(context: Context) {
        point = PointF(0F, 0F)
        drawer = AccelerometerDrawer(context, isSimple)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        var w = MeasureSpec.getSize(widthMeasureSpec)
        var h = MeasureSpec.getSize(heightMeasureSpec)
        if (w > h) {
            w = h
        } else {
            h = w
        }
        setMeasuredDimension(
            resolveSize(w, widthMeasureSpec),
            resolveSize(h, heightMeasureSpec)
        )
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        val width = width
        maxMove = width / 2
        k = (maxMove / (Math.PI / 2)).toFloat()
        maxAcceleration = width / 10
        drawer!!.layout(getWidth(), height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawer!!.draw(canvas)
    }

    fun updateOrientation(pitch: Float, roll: Float) {
        if (this.pitch.toInt() != pitch.toInt() || this.roll.toInt() != roll.toInt()) {
            this.pitch = pitch
            this.roll = roll
            point!![width * 0.37f * cos(Math.toRadians((90 - roll).toDouble()))
                .toFloat()] = width * 0.37f * cos(Math.toRadians((90 - pitch).toDouble()))
                .toFloat()
            drawer!!.update(point!!)
            invalidate()
        }
    }
}