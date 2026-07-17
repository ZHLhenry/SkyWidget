package com.sky.widget.bottomNavigationView

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.view.menu.MenuBuilder
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.sky.widget.base.ext.getFieldValue
import com.sky.widget.base.ext.safeGetFieldValue
import com.sky.widget.base.ext.setFieldValue
import androidx.core.view.isGone
import kotlin.math.min

/**
 * 自定义 BottomNavigationMenuView，支持超过 5 个菜单项的均匀分布
 *
 * 当菜单项数量超过 [MAX_ITEM_COUNT] 时，自动按最大宽度均分；
 * 同时支持通过反射复制原始 MenuView 的属性，用于动态配置菜单场景。
 */

@SuppressLint("RestrictedApi")
class BottomNavigationMenuView2 : BottomNavigationMenuView {

    companion object {

        /** 最大均匀分布的菜单项数量 */
        private const val MAX_ITEM_COUNT = 5
    }

    private var menu: MenuBuilder? = null
    /** 临时存储子 View 宽度，用于测量计算 */
    private val tempChildWidths = ArrayList<Int>()

    constructor(context: Context) : super(context)

    /** 从原始 MenuView 复制属性，用于动态替换 MenuView 后保持一致的样式 */
    @Suppress("UsePropertyAccessSyntax")
    constructor(context: Context, originMenuView: BottomNavigationMenuView) : this(context) {
        setPresenter(originMenuView.getFieldValue("presenter"))
        setFieldValue("buttons", originMenuView.safeGetFieldValue("buttons"))
        setIconTintList(originMenuView.iconTintList)
        setItemIconSize(originMenuView.itemIconSize)
        setItemTextColor(originMenuView.itemTextColor)
        setItemTextAppearanceInactive(originMenuView.itemTextAppearanceInactive)
        setItemTextAppearanceActive(originMenuView.itemTextAppearanceActive)
        setItemBackgroundRes(originMenuView.itemBackgroundRes)
        setItemBackground(originMenuView.itemBackground)
        setLabelVisibilityMode(originMenuView.labelVisibilityMode)
        setItemHorizontalTranslationEnabled(originMenuView.isItemHorizontalTranslationEnabled)
    }

    override fun initialize(menu: MenuBuilder) {
        super.initialize(menu)
        this.menu = menu
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)

        val totalCount = childCount

        tempChildWidths.clear()

        val parentHeight = MeasureSpec.getSize(heightMeasureSpec)
        val heightSpec = MeasureSpec.makeMeasureSpec(parentHeight, MeasureSpec.EXACTLY)

        val activeItemMaxWidth: Int = getFieldValue("activeItemMaxWidth")

        var totalWidth = 0
        val itemWidth = if(totalCount > MAX_ITEM_COUNT) {
            min(activeItemMaxWidth, width / MAX_ITEM_COUNT)
        } else if(totalCount <= 1) {
            width
        } else {
            width / totalCount
        }
        for(i in 0 until totalCount) {
            val child = getChildAt(i)
            if(child.isGone) {
                continue
            }
            child.measure(
                MeasureSpec.makeMeasureSpec(itemWidth, MeasureSpec.EXACTLY), heightSpec
            )
            val params = child.layoutParams
            params.width = child.measuredWidth
            totalWidth += child.measuredWidth
        }

        setMeasuredDimension(
            resolveSizeAndState(
                totalWidth, MeasureSpec.makeMeasureSpec(totalWidth, MeasureSpec.UNSPECIFIED), 0
            ),
            resolveSizeAndState(parentHeight, heightMeasureSpec, 0)
        )
    }
}