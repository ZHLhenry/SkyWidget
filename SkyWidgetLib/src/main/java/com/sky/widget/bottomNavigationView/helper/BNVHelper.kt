package com.sky.widget.bottomNavigationView.helper

import android.annotation.SuppressLint
import android.view.GestureDetector
import android.view.Menu
import android.view.MenuItem
import com.sky.widget.base.ext.emptyCountBeforeMenuItem
import com.sky.widget.base.ext.filterEmptyMenuIndex
import com.sky.widget.base.ext.indexOf
import com.sky.widget.bottomNavigationView.gesture.OnDoubleClickListener
import com.sky.widget.bottomNavigationView.iface.AbsMenuListener
import com.sky.widget.bottomNavigationView.iface.IBottomNavigationEx
import com.sky.widget.bottomNavigationView.iface.IMenuDoubleClickListener
import com.sky.widget.bottomNavigationView.iface.IMenuListener
import com.sky.widget.bottomNavigationView.internal.InnerListener
import java.lang.ref.WeakReference
import kotlin.concurrent.thread

/**
 * BottomNavigationView 核心辅助类
 *
 * 负责协调菜单项选择、空菜单项过滤、ViewPager 联动、双击监听等逻辑。
 * 通过 [InnerListener] 接收原始选择事件，处理后分发给 [IMenuListener] 和 [AbsViewPagerHelper]。
 */
class BNVHelper(bottomNavigationEx: IBottomNavigationEx<*, *, *>) {

    private val iBNERef = WeakReference(bottomNavigationEx)
    /** 上一次选中的菜单位置 */
    private var previousPosition: Int = -1
    /** 外部设置的菜单选择监听器 */
    private var menuListener: IMenuListener? = null

    /** ViewPager 联动辅助器 */
    private var viewPagerHelper: AbsViewPagerHelper<*>? = null
    /** 空菜单项 ID 列表，这些项不参与正常选择流程 */
    var emptyMenuIds: List<Int> = emptyList()

    /** 内部事件监听器，处理菜单选择和重选逻辑 */
    private val innerListener: InnerListener by lazy {
        object : InnerListener {
            override fun onNavigationItemSelected(menu: Menu, item: MenuItem): Boolean {
                if (emptyMenuIds.contains(item.itemId)) {
                    (menuListener as? AbsMenuListener)?.onEmptyItemClick(menu.indexOf(item), item)
                    return false
                }

                val position = menu.indexOf(item)
                if (previousPosition == position) {
                    return true
                }

                val result = menuListener?.onNavigationItemSelected(position, item, false) ?: true
                if (!result) {
                    return false
                }

                viewPagerHelper?.updatePosition(
                    position - menu.emptyCountBeforeMenuItem(
                        item,
                        emptyMenuIds
                    )
                )

                previousPosition = position
                return true
            }

            override fun onNavigationItemReselected(menu: Menu, item: MenuItem) {
                if (emptyMenuIds.contains(item.itemId)) {
                    return
                }

                val position = menu.indexOf(item)

                menuListener?.onNavigationItemSelected(position, item, false)

                viewPagerHelper?.updatePosition(
                    position - menu.emptyCountBeforeMenuItem(
                        item,
                        emptyMenuIds
                    )
                )

                previousPosition = position
            }
        }
    }

    init {
        bottomNavigationEx.setInnerListener(innerListener)
    }

    /** 获取当前菜单监听器 */
    fun getListener() = menuListener

    /** 设置菜单选择监听器 */
    fun setListener(menuListener: IMenuListener?) {
        this.menuListener = menuListener
    }

    /**
     * 设置 ViewPager 联动辅助器
     *
     * 会先释放旧的辅助器，再注册新的页面变化回调。
     * 当 ViewPager 滑动切换页面时，自动同步选中对应的导航项。
     */
    fun setupViewPagerHelper(absViewPagerHelper: AbsViewPagerHelper<*>) {
        viewPagerHelper?.release()
        viewPagerHelper = null
        viewPagerHelper = absViewPagerHelper
        viewPagerHelper?.setOnPageChangeCallback {
            val menuList = iBNERef.get()?.getMenuItems() ?: return@setOnPageChangeCallback

            var position = it
            menuList.forEachIndexed { index, item ->
                if (emptyMenuIds.contains(item.itemId)) {
                    position++
                }
                if (index == position) {
                    iBNERef.get()?.setCurrentItem(position)
                    previousPosition = position
                    return@setOnPageChangeCallback
                }
            }

            iBNERef.get()?.setCurrentItem(position)
            previousPosition = position
        }
    }

    /**
     * 设置菜单项双击监听器
     *
     * 在子线程中遍历所有菜单项，为每个非空菜单项注册 GestureDetector，
     * 双击时回调 [IMenuDoubleClickListener]。
     */
    @SuppressLint("ClickableViewAccessibility")
    fun setMenuDoubleClickListener(menuDoubleClickListener: IMenuDoubleClickListener) {
        thread {
            iBNERef.get()?.getAllBNItemView()?.runCatching {
                this.forEachIndexed { index, item ->
                    item.post {
                        val menuItem =
                            iBNERef.get()?.getMenuItems()?.getOrNull(index) ?: return@post
                        if (emptyMenuIds.contains(menuItem.itemId)) {
                            return@post
                        }

                        val menu = iBNERef.get()?.getMenu() ?: return@post
                        val gestureDetector = GestureDetector(
                            item.context,
                            OnDoubleClickListener(view = item, onDoubleClick = {
                                menuDoubleClickListener.onDoubleClick(
                                    menu.filterEmptyMenuIndex(menuItem, emptyMenuIds), menuItem
                                )
                            })
                        )
                        iBNERef.get()?.setItemOnTouchListener(menuItem) { _, event ->
                            gestureDetector.onTouchEvent(event)
                            // 避免拦截掉点击事件而导致点击后无反应
                            false
                        }
                    }
                }
            }?.onFailure {
                it.printStackTrace()
            }
        }
    }
}
