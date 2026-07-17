package com.sky.widget.bottomNavigationView.helper

import androidx.viewpager2.widget.ViewPager2

/**
 * ViewPager2 联动辅助器
 *
 * 注册 [ViewPager2.OnPageChangeCallback] 监听页面滑动，
 * 并在导航栏点击时同步切换 ViewPager2 页面。
 */
class VP2Helper(vp2: ViewPager2, smoothScroll: Boolean = false) :
    AbsViewPagerHelper<ViewPager2>(vp2, smoothScroll) {

    init {
        vp2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                notifyPageChanged(position)
            }
        })
    }

    override fun changeVPPosition(position: Int) {
        vp?.setCurrentItem(position, smoothScroll)
    }
}
