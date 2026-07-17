package com.sky.widget.bottomNavigationView.internal

import android.view.Menu
import android.view.MenuItem
import androidx.annotation.RestrictTo

/**
 * BottomNavigationView 内部事件监听器
 *
 * 用于 [SkyBottomNavigationView] 与 [BNVHelper] 之间的事件传递，
 * 接收原始的菜单项选择和重选事件。
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface InnerListener {
    /** 菜单项被选中 */
    fun onNavigationItemSelected(menu: Menu, item: MenuItem): Boolean
    /** 菜单项被重复选中 */
    fun onNavigationItemReselected(menu: Menu, item: MenuItem)
}
