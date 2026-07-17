package com.sky.widget.bottomNavigationView.helper

import androidx.annotation.CallSuper
import java.lang.ref.WeakReference

/** ViewPager 页面变化回调类型别名 */
typealias OnPageChangeCallback = (position: Int) -> Unit

/**
 * ViewPager 辅助类抽象基类
 *
 * 封装了 ViewPager/ViewPager2 与 BottomNavigation 联动的通用逻辑：
 * - 使用弱引用持有 ViewPager 实例，避免内存泄漏
 * - 区分「用户滑动」和「导航点击」两种页面切换来源，避免循环触发
 *
 * @param VP ViewPager 类型
 * @param vp ViewPager 实例
 * @param smoothScroll 导航点击切换页面时是否使用平滑滚动
 */
abstract class AbsViewPagerHelper<VP>(vp: VP, val smoothScroll: Boolean) {

    private var vpRef: WeakReference<VP> = WeakReference(vp)
    /** 标记当前是否由导航栏点击触发的页面切换 */
    private var isNavigationItemClicking: Boolean = false

    private var onPageChangeCallback: OnPageChangeCallback? = null

    /** 设置页面变化回调，由 ViewPager 滑动触发 */
    fun setOnPageChangeCallback(callback: OnPageChangeCallback) {
        onPageChangeCallback = callback
    }

    /** 获取 ViewPager 实例（弱引用） */
    protected val vp: VP? get() = vpRef.get()

    /** 释放资源，清除 ViewPager 引用 */
    @CallSuper
    open fun release() {
        vpRef.clear()
    }

    /** 通知页面变化（仅在非导航点击触发时回调） */
    protected fun notifyPageChanged(position: Int) {
        if (!isNavigationItemClicking) {
            onPageChangeCallback?.invoke(position)
        }
    }

    /** 由导航栏调用，切换到指定页面 */
    fun updatePosition(position: Int) {
        isNavigationItemClicking = true
        changeVPPosition(position)
        isNavigationItemClicking = false
    }

    /** 切换 ViewPager 到指定页面，由子类实现 */
    abstract fun changeVPPosition(position: Int)
}
