package com.sky.widget.badge

import android.graphics.PointF
import android.graphics.drawable.Drawable
import android.view.View

/**
 * Badge 通用接口定义
 *
 * 定义了 Badge 的所有可配置属性，包括数字/文本显示、背景样式、拖拽行为、
 * 位置偏移等。所有 setter 方法均返回 [IBadge] 以支持链式调用。
 */
interface IBadge {

    /** 设置 Badge 显示的数字，0 时隐藏，负数显示圆点 */
    fun setBadgeNumber(badgeNum: Int): IBadge
    /** 获取当前 Badge 数字 */
    fun getBadgeNumber(): Int

    /** 设置 Badge 显示的文本，优先级高于数字 */
    fun setBadgeText(badgeText: String?): IBadge
    /** 获取当前 Badge 文本 */
    fun getBadgeText(): String?

    /** 设置是否精确模式，关闭后超过 99 显示为 "99+" */
    fun setExactMode(isExact: Boolean): IBadge
    /** 是否为精确模式 */
    fun isExactMode(): Boolean

    /** 设置是否显示阴影 */
    fun setShowShadow(showShadow: Boolean): IBadge
    /** 是否显示阴影 */
    fun isShowShadow(): Boolean

    /** 设置 Badge 背景颜色 */
    fun setBadgeBackgroundColor(color: Int): IBadge
    /** 设置 Badge 边框样式 */
    fun stroke(color: Int, width: Float, isDpValue: Boolean): IBadge
    /** 获取 Badge 背景颜色 */
    fun getBadgeBackgroundColor(): Int

    /** 设置 Badge 背景 Drawable */
    fun setBadgeBackground(drawable: Drawable?): IBadge
    /** 设置 Badge 背景 Drawable，是否裁剪为 Badge 形状 */
    fun setBadgeBackground(drawable: Drawable?, clip: Boolean): IBadge
    /** 获取 Badge 背景 Drawable */
    fun getBadgeBackground(): Drawable?

    /** 设置 Badge 文字颜色 */
    fun setBadgeTextColor(color: Int): IBadge
    /** 获取 Badge 文字颜色 */
    fun getBadgeTextColor(): Int

    /** 设置 Badge 文字大小 */
    fun setBadgeTextSize(size: Float, isSpValue: Boolean): IBadge
    /** 获取 Badge 文字大小 */
    fun getBadgeTextSize(isSpValue: Boolean): Float

    /** 设置 Badge 内边距 */
    fun setBadgePadding(padding: Float, isDpValue: Boolean): IBadge
    /** 获取 Badge 内边距 */
    fun getBadgePadding(isDpValue: Boolean): Float

    /** 是否可拖拽 */
    fun isDraggable(): Boolean

    /** 设置 Badge 对齐方式，支持九宫格方位 */
    fun setBadgeGravity(gravity: Int): IBadge
    /** 获取 Badge 对齐方式 */
    fun getBadgeGravity(): Int

    /** 设置 Badge 偏移量（X/Y 相同） */
    fun setGravityOffset(offset: Float, isDpValue: Boolean): IBadge
    /** 设置 Badge 偏移量（X/Y 分别指定） */
    fun setGravityOffset(offsetX: Float, offsetY: Float, isDpValue: Boolean): IBadge
    /** 获取 X 方向偏移量 */
    fun getGravityOffsetX(isDpValue: Boolean): Float
    /** 获取 Y 方向偏移量 */
    fun getGravityOffsetY(isDpValue: Boolean): Float

    /** 设置拖拽状态变化监听器，非空时自动开启拖拽 */
    fun setOnDragStateChangedListener(l: OnDragStateChangedListener?): IBadge
    /** 获取当前拖拽中心坐标，未在拖拽时返回 null */
    fun getDragCenter(): PointF?
    /** 将 Badge 绑定到目标 View */
    fun bindTarget(view: View?): IBadge
    /** 获取绑定的目标 View */
    fun getTargetView(): View?
    /** 隐藏 Badge，可选择是否播放爆炸动画 */
    fun hide(animate: Boolean)

    /** 拖拽状态变化监听器 */
    interface OnDragStateChangedListener {
        /**
         * 拖拽状态变化回调
         * @param dragState 当前拖拽状态，取值见 [Companion] 中的常量
         * @param badge 当前 Badge 实例
         * @param targetView 绑定的目标 View
         */
        fun onDragStateChanged(dragState: Int, badge: IBadge?, targetView: View?)

        companion object {
            /** 开始拖拽 */
            const val STATE_START = 1
            /** 拖拽中 */
            const val STATE_DRAGGING = 2
            /** 拖拽超出范围 */
            const val STATE_DRAGGING_OUT_OF_RANGE = 3
            /** 拖拽取消（未超出范围松手） */
            const val STATE_CANCELED = 4
            /** 拖拽成功（超出范围松手） */
            const val STATE_SUCCEED = 5
        }
    }
}
