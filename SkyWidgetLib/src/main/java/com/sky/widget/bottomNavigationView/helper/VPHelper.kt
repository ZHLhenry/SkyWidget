package com.sky.widget.bottomNavigationView.helper

import androidx.annotation.RestrictTo
import androidx.viewpager.widget.ViewPager

/**
 * ViewPager 联动辅助器
 *
 * 注册 [ViewPager.OnPageChangeListener] 监听页面滑动，
 * 并在导航栏点击时同步切换 ViewPager 页面。
 * 释放时自动清除监听器。
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class VPHelper(vp: ViewPager, smoothScroll: Boolean = false) :
    AbsViewPagerHelper<ViewPager>(vp, smoothScroll) {

    init {
        vp.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {}

            override fun onPageSelected(position: Int) {
                notifyPageChanged(position)
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
    }

    override fun release() {
        vp?.clearOnPageChangeListeners()
        super.release()
    }

    override fun changeVPPosition(position: Int) {
        vp?.setCurrentItem(position, smoothScroll)
    }
}
