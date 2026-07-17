/**
 * View 手势扩展方法
 *
 * 提供双击事件监听的便捷扩展函数。
 */
package com.sky.widget.base.ext

import android.annotation.SuppressLint
import android.view.GestureDetector
import android.view.View
import androidx.annotation.RestrictTo
import com.sky.widget.bottomNavigationView.gesture.OnDoubleClickListener

/**
 * 为 View 添加双击事件监听
 *
 * @param enableAll 是否启用所有手势事件消费
 * @param onDoubleClick 双击回调
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@JvmOverloads
@SuppressLint("ClickableViewAccessibility")
internal fun <T : View> T.onDoubleClick(enableAll: Boolean = true, onDoubleClick: () -> Unit) {
    val gestureDetector = GestureDetector(context,
        OnDoubleClickListener(enableAll, this, onDoubleClick)
    )
    setOnTouchListener { _, event -> gestureDetector.onTouchEvent(event) }
}
