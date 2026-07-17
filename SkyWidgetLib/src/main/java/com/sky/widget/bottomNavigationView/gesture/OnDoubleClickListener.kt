package com.sky.widget.bottomNavigationView.gesture

import android.view.MotionEvent
import android.view.View
import androidx.annotation.RestrictTo

/**
 * 双击事件监听器
 *
 * 继承 [BaseGestureListener]，仅关注双击手势，
 * 始终消费 [onDown] 以确保后续手势事件能正常传递。
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class OnDoubleClickListener @JvmOverloads constructor(
    enableAll: Boolean = true,
    view: View,
    private val onDoubleClick: () -> Unit
) : BaseGestureListener(enableAll, view) {

    override fun onDown(e: MotionEvent): Boolean {
        return true
    }

    override fun onDoubleTap(e: MotionEvent): Boolean {
        onDoubleClick.invoke()
        return true
    }
}
