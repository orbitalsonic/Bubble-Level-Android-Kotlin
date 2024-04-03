package com.orbitalsonic.bubblelevel.widget

import android.content.Context
import android.graphics.*
import android.util.TypedValue
import androidx.core.content.ContextCompat
import com.orbitalsonic.bubblelevel.R

class AccelerometerDrawer(context: Context, private val isSimple: Boolean) : ViewDrawer<PointF?> {
    private val pathPaint: Paint
    private val ballPaint: Paint
    private var path: Path? = null
    private var xPos = 0f
    private var yPos = 0f
    private val center: Point
    private var radius = 0
    override fun layout(width: Int, height: Int) {
        radius = width / 8
        center[width / 2] = height / 2

        if (path == null) {
            val radius = width / 2f - width * 0.03f
            path = Path()
            path!!.moveTo(center.x - radius, center.y.toFloat())
            path!!.lineTo(center.x + radius, center.y.toFloat())
            path!!.moveTo(center.x.toFloat(), center.y - radius)
            path!!.lineTo(center.x.toFloat(), center.y + radius)
            if (!isSimple) {
                path!!.addCircle(center.x.toFloat(), center.y.toFloat(), radius, Path.Direction.CCW)
            }
        }
    }

    override fun draw(canvas: Canvas?) {
        //Draw grid
        canvas!!.drawPath(path!!, pathPaint)
        //Draw ball
        canvas.drawCircle(center.x - xPos, center.y + yPos, radius.toFloat(), ballPaint)
    }

    override fun update(value: PointF?) {
        xPos = value!!.x
        yPos = value.y
    }

    init {
        val gridColor: Int = ContextCompat.getColor(context,R.color.dayNightColor)
        val ballColor: Int = ContextCompat.getColor(context,R.color.dayNightColor)
        pathPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        pathPaint.strokeWidth = 1f
        pathPaint.style = Paint.Style.STROKE
        pathPaint.color = gridColor
        ballPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        ballPaint.style = Paint.Style.FILL
        ballPaint.color = ballColor
        center = Point(0, 0)
    }
}