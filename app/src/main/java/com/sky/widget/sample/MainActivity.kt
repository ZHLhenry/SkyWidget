package com.sky.widget.sample

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.view.Gravity
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.graphics.toColorInt
import com.sky.widget.badge.IBadge
import com.sky.widget.badge.SkyBadgeView
import com.sky.widget.base.ext.dp2px
import com.sky.widget.bottomNavigationView.SkyBottomNavigationView
import com.sky.widget.bottomNavigationView.iface.AbsMenuListener
import com.sky.widget.bottomNavigationView.iface.IMenuDoubleClickListener
import com.sky.widget.iconfont.SkyIconFontsLib

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: SkyBottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupBadge()
        setupIconFont()
        setupBottomNavigation()
    }

    /**
     * 演示 SkyBadgeView 的三种用法：
     * 1. 数字角标
     * 2. 文本角标
     * 3. 可拖拽消除角标
     */
    private fun setupBadge() {
        // 1. 数字角标
        SkyBadgeView(this)
            .bindTarget(findViewById(R.id.badgeTarget1))
            .setBadgeNumber(5)
            .setBadgeBackgroundColor(Color.RED)

        // 2. 文本角标
        SkyBadgeView(this)
            .bindTarget(findViewById(R.id.badgeTarget2))
            .setBadgeText("NEW")
            .setBadgeBackgroundColor("#FF6200EE".toColorInt())
            .setBadgeGravity(Gravity.END or Gravity.TOP)

        // 3. 可拖拽消除角标
        SkyBadgeView(this)
            .bindTarget(findViewById(R.id.badgeTarget3))
            .setBadgeNumber(99)
            .setExactMode(false) // >99 显示 "99+"
            .setBadgeBackgroundColor("#FF03DAC5".toColorInt())
            .setOnDragStateChangedListener(object : IBadge.OnDragStateChangedListener {
                override fun onDragStateChanged(dragState: Int, badge: IBadge?, targetView: android.view.View?) {
                    when (dragState) {
                        IBadge.OnDragStateChangedListener.STATE_SUCCEED -> showToast("拖拽消除成功！")
                        IBadge.OnDragStateChangedListener.STATE_CANCELED -> showToast("取消消除")
                    }
                }
            })
    }

    /**
     * 演示 SkyIconFontsLib 的两种用法：
     * 1. 在文本中嵌入图标
     * 2. 创建图标 Drawable
     */
    private fun setupIconFont() {
        // 1. 在文本中嵌入图标（使用 formattedName）
        val tvIconFont = findViewById<TextView>(R.id.tvIconFont)
        val iconNames = listOf("testSkyshouye", "testSkyguangchang", "testSkyxiangmu", "testSkywode")
        val iconText = iconNames.joinToString("  ") { name ->
            SkyIconFontsLib.getFormattedIconName(name)
        }
        tvIconFont.text = SkyIconFontsLib.style(SpannableString(iconText))

        // 2. 创建图标 Drawable 显示在 ImageView 中
        val ivIconFont = findViewById<android.widget.ImageView>(R.id.ivIconFont)
        ivIconFont.setImageDrawable(SkyIconFontsLib.drawable(this, "testSkyfenxiang"))
    }

    /**
     * 演示 SkyBottomNavigationView 的增强功能：
     * 1. 菜单选择监听
     * 2. 双击监听
     * 3. 图标/文字样式控制
     * 4. 中间突出图标 tab（setEmptyMenuIds + 自定义样式）
     */
    private fun setupBottomNavigation() {
        bottomNav = findViewById(R.id.bottomNav)

        // 将中间项 nav_add 标记为空菜单项（不参与正常选择，点击走 onEmptyItemClick）
        bottomNav.setEmptyMenuIds(listOf(R.id.nav_add))

        // 基础样式设置
        bottomNav
            .enableAnimation(true)
            .setIconSize(22f, 22f)
            .setTextSize(12f)

        // 自定义中间突出图标 tab（位置2：首页/发现/添加/项目/我的）
        setupCenterTab()

        // 菜单选择监听
        bottomNav.setMenuListener(object : AbsMenuListener() {
            override fun onNavigationItemSelected(
                position: Int,
                menu: android.view.MenuItem,
                isReSelected: Boolean
            ): Boolean {
                showToast("选中第 $position 项：${menu.title}")
                return true
            }

            override fun onEmptyItemClick(position: Int, menu: android.view.MenuItem) {
                showToast("点击了中间添加按钮")
            }
        })

        // 双击监听
        bottomNav.setMenuDoubleClickListener(object : IMenuDoubleClickListener {
            override fun onDoubleClick(position: Int, menu: android.view.MenuItem) {
                showToast("双击了第 $position 项：${menu.title}")
            }
        })
    }

    /**
     * 自定义中间突出图标 tab：
     * - 隐藏文字
     * - 放大图标
     * - 设置圆形背景
     * - 向上偏移突出显示
     */
    private fun setupCenterTab() {
        val centerPosition = 2 // 5个菜单项的中间位置
        val centerItemView = bottomNav.getBNItemView(centerPosition) ?: return

        // 隐藏中间项的文字
        bottomNav.enableBNItemViewLabelVisibility(centerPosition, false)

        // 放大中间项图标
        bottomNav.setIconSizeAt(centerPosition, 28f, 28f)

        // 设置圆形背景 + 向上偏移
        centerItemView.post {
            centerItemView.setBackgroundResource(R.drawable.bg_center_tab)
            // 向上偏移，让圆形图标突出导航栏
            val params = centerItemView.layoutParams
            if (params is android.view.ViewGroup.MarginLayoutParams) {
                params.topMargin = -dp2px(16f)
                params.bottomMargin = -dp2px(16f)
                centerItemView.layoutParams = params
            }
            // 添加阴影效果
            centerItemView.elevation = dp2px(8f).toFloat()
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
