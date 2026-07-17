package com.sky.widget.bottomNavigationView

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Parcelable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.view.forEachIndexed
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.R
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.sky.widget.base.ext.dp2px
import com.sky.widget.base.ext.getFieldValue
import com.sky.widget.bottomNavigationView.helper.BNVHelper
import com.sky.widget.bottomNavigationView.iface.IBottomNavigationEx
import com.sky.widget.bottomNavigationView.internal.InnerListener
import kotlin.math.ceil
import kotlin.math.max
import androidx.core.view.get
import androidx.core.view.size
import com.sky.widget.base.ext.setFieldValue
import com.sky.widget.bottomNavigationView.helper.VP2Helper
import com.sky.widget.bottomNavigationView.helper.VPHelper
import com.sky.widget.bottomNavigationView.iface.IMenuDoubleClickListener
import com.sky.widget.bottomNavigationView.iface.IMenuListener

/**
 * 自定义 BottomNavigationView，扩展了原生 BottomNavigationView 的功能
 *
 * 支持以下增强功能：
 * - 图标/文字可见性控制、动画开关
 * - 图标尺寸、文字大小、字体样式自定义
 * - 图标/文字颜色独立设置
 * - 与 ViewPager / ViewPager2 联动
 * - 支持空菜单项占位、双击监听、动态配置菜单
 * - 支持超过 5 个菜单项的均匀分布
 */
@SuppressLint("RestrictedApi")
class SkyBottomNavigationView : BottomNavigationView,
    IBottomNavigationEx<BottomNavigationView, BottomNavigationMenuView, BottomNavigationItemView> {

    // region 动画相关状态记录
    private var labelSizeRecord = false
    private var largeLabelSize = 0f
    private var smallLabelSize = 0f
    private var visibilityHeightRecord = false
    private var itemHeight = 0
    private var textVisibility = true
    // endregion

    private val theBottomNavigationMenuView by lazy {
        menuView as BottomNavigationMenuView
    }
    private val theBottomNavigationItemViews: Array<BottomNavigationItemView>
        get() = (0 until theBottomNavigationMenuView.childCount)
            .map { theBottomNavigationMenuView.getChildAt(it) }
            .filterIsInstance<BottomNavigationItemView>()
            .toTypedArray()

    private var innerListener: InnerListener? = null
    private val bnvHelper: BNVHelper

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(
        context,
        attrs,
        R.attr.bottomNavigationStyle
    )

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(
        context,
        attrs,
        defStyleAttr,
        R.style.Widget_Design_BottomNavigationView
    )

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        bnvHelper = BNVHelper(this)
        setOnItemSelectedListener {
            return@setOnItemSelectedListener innerListener?.onNavigationItemSelected(menu, it)
                ?: false
        }
        setOnItemReselectedListener {
            innerListener?.onNavigationItemSelected(menu, it)
        }
    }

    override val realView: BottomNavigationView get() = this

    override fun getSuggestedMinimumHeight(): Int {
        return context.dp2px(56F)
    }

    override fun setLayoutParams(params: ViewGroup.LayoutParams?) {
        super.setLayoutParams(params)
        itemHeight = layoutParams.height + paddingTop + paddingBottom
    }

    override fun setIconVisibility(visibility: Boolean): SkyBottomNavigationView {
        for(b in theBottomNavigationItemViews) {
            val icon: ImageView = b.getFieldValue("icon")
            icon.visibility = if(visibility) {
                View.VISIBLE
            } else {
                View.INVISIBLE
            }
        }
        if(!visibility) {
            if(!visibilityHeightRecord) {
                visibilityHeightRecord = true
                itemHeight = getBNMenuViewHeight()
            }

            val button = theBottomNavigationItemViews.firstOrNull()
            if(null != button) {
                val icon: ImageView = button.getFieldValue("icon")
                icon.post {
                    setBNMenuViewHeight(itemHeight - icon.measuredHeight)
                }
            }
        } else {
            if(!visibilityHeightRecord) {
                return this
            }
            setBNMenuViewHeight(itemHeight)
        }
        return this
    }

    override fun setTextVisibility(visibility: Boolean): SkyBottomNavigationView {
        this.textVisibility = visibility

        for(b in theBottomNavigationItemViews) {
            val largeLabel: TextView = b.getFieldValue("largeLabel")
            val smallLabel: TextView = b.getFieldValue("smallLabel")

            if(visibility) {
                // if not record the font size, we need do nothing.
                if(!labelSizeRecord) {
                    return this
                }

                // restore it
                largeLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, largeLabelSize)
                smallLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, smallLabelSize)
            } else {
                // if not record the font size, record it
                if(!labelSizeRecord) {
                    largeLabelSize = largeLabel.textSize
                    smallLabelSize = smallLabel.textSize
                    labelSizeRecord = true
                }

                // if not visitable, set font size to 0
                largeLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, 0f)
                smallLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, 0f)
            }
        }

        // 4 change mItemHeight to only icon size in menuView
        if(visibility) {
            // if not record the mItemHeight, we need do nothing.
            if(!visibilityHeightRecord) {
                return this
            }
            // restore mItemHeight
            setBNMenuViewHeight(itemHeight)
        } else {
            // if not record mItemHeight
            if(!visibilityHeightRecord) {
                itemHeight = getBNMenuViewHeight()
                visibilityHeightRecord = true
            }

            // change mItemHeight to only icon size in menuView
            // private final int mItemHeight;
            setBNMenuViewHeight(itemHeight - getFontHeight(smallLabelSize))
        }
        theBottomNavigationMenuView.updateMenuView()
        return this

    }

    /** 获取指定字体大小对应的文本高度 */
    private fun getFontHeight(fontSize: Float): Int {
        val paint = Paint()
        paint.textSize = fontSize
        val fm = paint.fontMetrics
        return ceil((fm.descent - fm.top).toDouble()).toInt() + 2
    }

    override fun enableAnimation(enable: Boolean): SkyBottomNavigationView {
        for(b in theBottomNavigationItemViews) {
            val largeLabel: TextView = b.getFieldValue("largeLabel")
            val smallLabel: TextView = b.getFieldValue("smallLabel")

            if(!enable) {
                if(!labelSizeRecord) {
                    largeLabelSize = largeLabel.textSize
                    smallLabelSize = smallLabel.textSize
                    labelSizeRecord = true
                }
                // disable
                largeLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, smallLabelSize)

                // trigger calculateTextScaleFactors
                b.setTextAppearanceInactive(itemTextAppearanceInactive)
                b.setTextAppearanceActive(itemTextAppearanceInactive)
            } else {
                // haven't change the value. It means it was the first call this method. So nothing need to do.
                if(!labelSizeRecord) {
                    return this
                }
                // restore
                largeLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, largeLabelSize)

                // trigger calculateTextScaleFactors
                b.setTextAppearanceInactive(itemTextAppearanceInactive)
                b.setTextAppearanceActive(itemTextAppearanceActive)
            }
        }
        theBottomNavigationMenuView.updateMenuView()
        return this
    }

    override fun enableLabelVisibility(enable: Boolean): SkyBottomNavigationView {
        labelVisibilityMode = if(enable) LABEL_VISIBILITY_SELECTED else LABEL_VISIBILITY_LABELED
        return this
    }

    override fun enableBNItemViewLabelVisibility(position: Int, enable: Boolean): SkyBottomNavigationView {
        getBNItemView(position)?.setShifting(enable)
        return this
    }

    override fun enableItemHorizontalTranslation(enable: Boolean): SkyBottomNavigationView {
        isItemHorizontalTranslationEnabled = enable
        return this
    }

    override fun getCurrentIndex(): Int {
        menu.forEachIndexed { index, item ->
            if(item.isChecked) {
                return index
            }
        }
        return -1
    }

    override fun menuItemPositionAt(item: MenuItem): Int {
        menu.forEachIndexed { index, m ->
            if(m.itemId == item.itemId) {
                return index
            }
        }
        return -1
    }

    override fun setCurrentItem(index: Int): SkyBottomNavigationView {
        val target = menu[index]
        if(bnvHelper.emptyMenuIds.contains(target.itemId)) {
            if(index >= menu.size) {
                return this
            }
            for(pos in index + 1 until menu.size) {
                val m = menu[pos]
                if(!bnvHelper.emptyMenuIds.contains(m.itemId)) {
                    selectedItemId = target.itemId
                    return this
                }
            }
        } else {
            selectedItemId = target.itemId
        }
        return this
    }

    override fun getMenuListener(): IMenuListener? {
        return bnvHelper.getListener()
    }

    override fun setMenuListener(menuListener: IMenuListener): SkyBottomNavigationView {
        bnvHelper.setListener(menuListener)
        return this
    }

    override fun setMenuDoubleClickListener(menuDoubleClickListener: IMenuDoubleClickListener): SkyBottomNavigationView {
        bnvHelper.setMenuDoubleClickListener(menuDoubleClickListener)
        return this
    }

    override fun setInnerListener(listener: InnerListener) {
        this.innerListener = listener
    }

    override fun getBNMenuView(): BottomNavigationMenuView {
        return theBottomNavigationMenuView
    }

    override fun clearIconTintColor(): SkyBottomNavigationView {
        theBottomNavigationMenuView.iconTintList = null
        return this
    }

    override fun getAllBNItemView(): Array<BottomNavigationItemView> {
        return theBottomNavigationItemViews
    }

    override fun getBNItemView(position: Int): BottomNavigationItemView? {
        return theBottomNavigationItemViews.getOrNull(position)
    }

    override fun getIconAt(position: Int): ImageView? {
        return getBNItemView(position)?.getFieldValue("icon")
    }

    override fun getSmallLabelAt(position: Int): TextView? {
        return getBNItemView(position)?.getFieldValue("smallLabel")
    }

    override fun getLargeLabelAt(position: Int): TextView? {
        return getBNItemView(position)?.getFieldValue("largeLabel")
    }

    override fun getBNItemViewCount(): Int {
        return theBottomNavigationItemViews.size
    }

    override fun setSmallTextSize(sp: Float): SkyBottomNavigationView {
        val count = getBNItemViewCount()
        for(i in 0 until count) {
            getSmallLabelAt(i)?.textSize = sp
        }
        theBottomNavigationMenuView.updateMenuView()
        return this
    }

    override fun setLargeTextSize(sp: Float): SkyBottomNavigationView {
        val count = getBNItemViewCount()
        for(i in 0 until count) {
            getLargeLabelAt(i)?.textSize = sp
        }
        theBottomNavigationMenuView.updateMenuView()
        return this
    }

    override fun setTextSize(sp: Float): SkyBottomNavigationView {
        setLargeTextSize(sp)
        setSmallTextSize(sp)
        return this
    }

    override fun setIconSizeAt(
        position: Int,
        width: Float,
        height: Float
    ): SkyBottomNavigationView {
        val icon = getIconAt(position)
            ?: return this
        // update size
        val layoutParams = icon.layoutParams
        layoutParams.width = context.dp2px(width)
        layoutParams.height = context.dp2px(height)
        icon.layoutParams = layoutParams

        theBottomNavigationMenuView.updateMenuView()
        return this
    }

    override fun setIconSize(width: Float, height: Float): SkyBottomNavigationView {
        val count = getBNItemViewCount()
        for(i in 0 until count) {
            setIconSizeAt(i, width, height)
        }
        return this
    }

    override fun setIconSize(dpSize: Float): SkyBottomNavigationView {
        itemIconSize = context.dp2px(dpSize)
        return this
    }

    override fun setBNMenuViewHeight(height: Int): SkyBottomNavigationView {
        val lp = layoutParams
        lp.height = max(height, -2)
        layoutParams = lp
        requestLayout()
        theBottomNavigationMenuView.requestLayout()
        theBottomNavigationMenuView.updateMenuView()
        itemHeight = lp.height
        return this
    }

    override fun getBNMenuViewHeight(): Int {
        return itemHeight
    }

    override fun setTypeface(typeface: Typeface, style: Int): SkyBottomNavigationView {
        val count = getBNItemViewCount()
        for(i in 0 until count) {
            getLargeLabelAt(i)?.setTypeface(typeface, style)
            getSmallLabelAt(i)?.setTypeface(typeface, style)
        }
        theBottomNavigationMenuView.updateMenuView()
        return this
    }

    override fun setTypeface(typeface: Typeface): SkyBottomNavigationView {
        val count = getBNItemViewCount()
        for(i in 0 until count) {
            getLargeLabelAt(i)?.typeface = typeface
            getSmallLabelAt(i)?.typeface = typeface
        }
        theBottomNavigationMenuView.updateMenuView()
        return this
    }

    override fun setupWithViewPager(
        vp: ViewPager?
    ): SkyBottomNavigationView {
        return setupWithViewPager(vp, false)
    }

    override fun setupWithViewPager(
        vp: ViewPager?,
        smoothScroll: Boolean
    ): SkyBottomNavigationView {
        if(null == vp) {
            return this
        }
        bnvHelper.setupViewPagerHelper(VPHelper(vp, smoothScroll))
        return this
    }

    override fun setupWithViewPager2(
        vp2: ViewPager2?
    ): SkyBottomNavigationView {
        return setupWithViewPager2(vp2, false)
    }

    override fun setupWithViewPager2(
        vp2: ViewPager2?,
        smoothScroll: Boolean,
    ): SkyBottomNavigationView {
        if(null == vp2) {
            return this
        }
        bnvHelper.setupViewPagerHelper(VP2Helper(vp2, smoothScroll))
        return this
    }

    override fun setBNItemViewBackgroundRes(position: Int, background: Int): SkyBottomNavigationView {
        getBNItemView(position)?.setItemBackground(background)
        return this
    }

    override fun setIconTintList(tint: ColorStateList?): SkyBottomNavigationView {
        theBottomNavigationMenuView.iconTintList = tint
        return this
    }

    override fun setIconTintList(position: Int, tint: ColorStateList?): SkyBottomNavigationView {
        getBNItemView(position)?.setIconTintList(tint)
        return this
    }

    override fun setTextTintList(tint: ColorStateList?): SkyBottomNavigationView {
        theBottomNavigationMenuView.itemTextColor = tint
        return this
    }

    override fun setTextTintList(position: Int, tint: ColorStateList?): SkyBottomNavigationView {
        getBNItemView(position)?.setTextColor(tint)
        return this
    }

    override fun setIconsMarginTop(marginTop: Int): SkyBottomNavigationView {
        for(i in 0 until getBNItemViewCount()) {
            setIconMarginTop(i, marginTop)
        }
        return this
    }

    override fun setIconMarginTop(position: Int, marginTop: Int): SkyBottomNavigationView {
        if(getBNItemView(position).setFieldValue("itemPaddingTop", marginTop)) {
            theBottomNavigationMenuView.updateMenuView()
        }
        return this
    }

    override fun setEmptyMenuIds(emptyMenuIds: List<Int>): SkyBottomNavigationView {
        bnvHelper.emptyMenuIds = emptyMenuIds
        return this
    }

    override fun getMenuItems(): List<MenuItem> {
        val result = arrayListOf<MenuItem>()
        menu.forEachIndexed { index, item ->
            result.add(index, item)
        }
        return result.toList()
    }

    override fun configDynamic(count: Int, generator: (menu: Menu, index: Int) -> MenuItem): SkyBottomNavigationView {
        if(count > 0) {
            menu.setFieldValue("maxItemCount", count)
            val menu = configMenu()
            presenter.setUpdateSuspended(false)
            menu.clearAll()
            for(i in 1 .. count) {
                generator(menu, i)
            }
            menu.addMenuPresenter(presenter)
            presenter.setUpdateSuspended(false)
            presenter.updateMenuView(true)
        }
        return this
    }

    /** 动态配置菜单项，替换为自定义的 BottomNavigationMenuView2 以支持超过 5 个菜单 */
    private fun configMenu() : MenuBuilder {
        val menuBuilder: MenuBuilder = getMenu() as MenuBuilder
        if(menuView is BottomNavigationMenuView2) {
            return menuBuilder
        }

        val originMenuView = getBNMenuView()

        val params = LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.gravity = Gravity.START

        val newMenuView = BottomNavigationMenuView2(context, originMenuView)
        newMenuView.setLayoutParams(params)

        setFieldValue("menuView", newMenuView)

        presenter.setMenuView(newMenuView)

        presenter.initForMenu(context, menuBuilder)

        removeView(originMenuView)
        addView(newMenuView, params)
        return menuBuilder
    }

    override fun getMenuMaxItemCount(): Int {
        return menu.getFieldValue("maxItemCount")
    }

    override fun restoreInstanceState(state: Parcelable?) {
        onRestoreInstanceState(state)
    }

    override fun saveInstanceState(): Parcelable? {
        return onSaveInstanceState()
    }

    override fun setItemOnTouchListener(menuItem: MenuItem, onTouchListener: OnTouchListener) {
        super.setItemOnTouchListener(menuItem.itemId, onTouchListener)
    }
}