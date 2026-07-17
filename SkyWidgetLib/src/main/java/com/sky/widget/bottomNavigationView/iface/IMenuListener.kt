package com.sky.widget.bottomNavigationView.iface

import android.view.MenuItem

/**
 * 菜单项选择监听器接口
 *
 * 用于监听 BottomNavigationView 的菜单项选择事件。
 */
interface IMenuListener {
    /**
     * 菜单项被选中时回调
     *
     * @param position 菜单项位置
     * @param menu 被选中的 MenuItem
     * @param isReSelected 是否为重复选中
     * @return true 允许切换，false 阻止切换
     */
    fun onNavigationItemSelected(position: Int, menu: MenuItem, isReSelected: Boolean): Boolean
}

/**
 * 菜单监听器抽象基类
 *
 * 提供默认实现，子类只需关注 [onEmptyItemClick] 即可。
 * 空菜单项的点击事件会通过此抽象类单独回调。
 */
abstract class AbsMenuListener : IMenuListener {
    override fun onNavigationItemSelected(
        position: Int,
        menu: MenuItem,
        isReSelected: Boolean
    ): Boolean {
        return true
    }

    /** 空菜单项（占位项）被点击时回调 */
    abstract fun onEmptyItemClick(position: Int, menu: MenuItem)
}

/**
 * 菜单项双击监听器接口
 *
 * 用于监听 BottomNavigationView 菜单项的双击事件。
 */
interface IMenuDoubleClickListener {
    /**
     * 菜单项被双击 时回调
     *
     * @param position 菜单项位置（已过滤空菜单项）
     * @param menu 被双击的 MenuItem
     */
    fun onDoubleClick(position: Int, menu: MenuItem)
}
