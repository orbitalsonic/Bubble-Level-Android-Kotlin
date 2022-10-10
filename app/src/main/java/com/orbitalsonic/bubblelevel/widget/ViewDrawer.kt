package com.orbitalsonic.bubblelevel.widget

import android.graphics.Canvas

interface ViewDrawer<T> {
    fun layout(width: Int, height: Int)
    fun draw(canvas: Canvas?)
    fun update(value: T)
}