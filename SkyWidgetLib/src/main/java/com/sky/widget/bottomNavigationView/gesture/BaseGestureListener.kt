package com.sky.widget.bottomNavigationView.gesture

import android.os.Build
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.annotation.RestrictTo

/**
 * 基础手势监听器
 *
 * 封装了常见手势回调的默认实现，通过 [enableAll] 控制是否消费所有手势事件。
 * 子类可按需覆盖特定手势方法。
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
open class BaseGestureListener(protected val enableAll: Boolean, protected val view: View) :
    GestureDetector.SimpleOnGestureListener() {

    override fun onDown(e: MotionEvent): Boolean = enableAll

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        if (enableAll) {
            view.performClick()
        }
        return enableAll
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean = enableAll

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean = enableAll

    override fun onSingleTapConfirmed(e: MotionEvent): Boolean = enableAll

    override fun onDoubleTap(e: MotionEvent): Boolean = enableAll

    override fun onDoubleTapEvent(e: MotionEvent): Boolean = enableAll

    override fun onContextClick(e: MotionEvent): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && view.isContextClickable) {
            if (enableAll) {
                if (null != e && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    view.performContextClick(e.x, e.y)
                } else {
                    view.performContextClick()
                }
            }
        }
        return enableAll
    }

    override fun onLongPress(e: MotionEvent) {
        if (enableAll && view.isLongClickable) {
            if (null != e && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                view.performLongClick(e.x, e.y)
            } else {
                view.performLongClick()
            }
        }
    }
}
