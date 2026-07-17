package com.sky.widget.iconfont

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Spanned
import com.mikepenz.iconics.Iconics
import com.mikepenz.iconics.IconicsDrawable

/**
 * Android-Iconics 三方库的统一封装入口
 * 消费者通过此类完成图标库初始化、图标查询和 Drawable 创建等操作，
 * 避免在业务代码中直接依赖 Iconics API 或 [SkyIconFontTypeface]。
 *
 * 所有公开方法的参数和返回值均为基础类型（String、Context、Drawable 等），
 * 消费者无需感知 Iconics 类型体系。
 */
object SkyIconFontsLib {

    // region 初始化与注册

    /**
     * 初始化并注册自定义字体，应在 Application.onCreate 中调用。
     * 传入 TTF 文件在 assets 中的路径，内部自动创建 [SkyIconFontTypeface] 并完成注册。
     * 第一个注册的字体会被设为默认字体。
     *
     * 示例：
     * ```
     * SkyIconFontsLib.initRegister(context, "fonts/sky_iconfont.ttf")
     * ```
     */
    fun initRegister(context: Context, vararg ttfPaths: String) {
        Iconics.init(context)
        ttfPaths.forEach { path ->
            val font = SkyIconFontTypeface(ttfFilePath = path)
            Iconics.registerFont(font)
            if (SkyIconFontTypeface.default == null) {
                SkyIconFontTypeface.default = font
            }
        }
    }

    /**
     * 是否已完成初始化
     */
    fun isInitDone(): Boolean {
        return Iconics.isInitDone()
    }

    // endregion

    // region 图标查询

    /**
     * 判断指定名称的图标是否存在
     */
    fun isIconExists(icon: String): Boolean {
        return Iconics.isIconExists(icon)
    }

    /**
     * 获取图标的 formattedName（如 "{skyshouye}"）
     * 用于 Iconics 文本解析模式，可直接嵌入 SpannableString 中
     *
     * 示例：
     * ```
     * val text = "${SkyIconFontsLib.getFormattedIconName("skyshouye")} 首页"
     * textView.text = SkyIconFontsLib.style(SpannableString(text))
     * ```
     */
    fun getFormattedIconName(iconName: String): String {
        val font = SkyIconFontTypeface.default ?: throw IllegalStateException(
            "SkyIconFontTypeface 尚未初始化，请先调用 SkyIconFontsLib.initRegister()"
        )
        return font.getIcon(iconName).formattedName
    }

    // endregion

    // region 样式

    /**
     * 对 [Spanned] 文本进行图标样式处理，
     * 将文本中的 {prefix_name} 格式标记替换为对应的图标字符
     */
    fun style(spanned: Spanned): Spanned {
        return Iconics.style(spanned)
    }

    // endregion

    // region Drawable

    /**
     * 根据图标名称创建图标 Drawable
     *
     * 示例：
     * ```
     * imageView.setImageDrawable(SkyIconFontsLib.drawable(context, "skyshouye"))
     * ```
     */
    fun drawable(context: Context, iconName: String): Drawable {
        val font = SkyIconFontTypeface.default ?: throw IllegalStateException(
            "SkyIconFontTypeface 尚未初始化，请先调用 SkyIconFontsLib.initRegister()"
        )
        val icon = font.getIcon(iconName)
        val drawable = IconicsDrawable(context, icon)
        drawable.typeface = icon.typeface.rawTypeface
        return drawable
    }

    // endregion

    // region 信息导出

    /**
     * 输出当前激活字体的完整信息（JSON 格式），包含元数据和所有图标映射
     */
    fun getSkyIconFontInfoJson(): String {
        val font = SkyIconFontTypeface.default ?: throw IllegalStateException(
            "SkyIconFontTypeface 尚未初始化，请先调用 SkyIconFontsLib.initRegister()"
        )
        return font.getSkyIconFontInfoJson()
    }

    // endregion
}
