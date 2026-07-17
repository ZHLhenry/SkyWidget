package com.sky.widget.base.ext

import android.content.Context

/**
 * Context 扩展工具方法
 *
 * 提供 dp/px 单位转换的便捷扩展函数。
 */

/** dp 转 px（向上取整） */
fun Context.dp2px(dpValue: Number): Int {
    val scale = resources.displayMetrics.density
    return (dpValue.toFloat() * scale + 0.5f).toInt()
}

/** px 转 dp（向上取整） */
fun Context.px2dp(pxValue: Number): Int {
    val scale = resources.displayMetrics.density
    return (pxValue.toFloat() / scale + 0.5f).toInt()
}