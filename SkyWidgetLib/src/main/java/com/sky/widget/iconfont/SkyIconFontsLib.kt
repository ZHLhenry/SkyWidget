package com.sky.widget.iconfont

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Spanned
import com.mikepenz.iconics.Iconics
import com.mikepenz.iconics.IconicsDrawable

/**
 * 已注册字体的摘要信息
 *
 * @property fontName 字体名称（JSON 中的 name 字段）
 * @property mappingPrefix 图标前缀（css_prefix_text 前3个字符）
 * @property iconCount 该字体包含的图标数量
 */
data class FontInfo(
    val fontName: String,
    val mappingPrefix: String,
    val iconCount: Int
)

/**
 * Android-Iconics 三方库的统一封装入口
 * 消费者通过此类完成图标库初始化、图标查询和 Drawable 创建等操作，
 * 避免在业务代码中直接依赖 Iconics API 或 [SkyIconFontTypeface]。
 *
 * 所有公开方法的参数和返回值均为基础类型（String、Context、Drawable 等），
 * 消费者无需感知 Iconics 类型体系。
 */
object SkyIconFontsLib {

    private const val DEFAULT_TTF_PATH = "fonts/sky_iconfont.ttf"

    // region 初始化与注册

    /**
     * 初始化并注册自定义字体，应在 Application.onCreate 中调用。
     * 传入 TTF 文件在 assets 中的路径列表，内部自动创建 [SkyIconFontTypeface] 并完成注册。
     * 第一个注册的字体会被设为默认字体。
     * 未传入路径时，自动使用库内置的默认字体。
     *
     * 示例：
     * ```
     * // 使用默认字体
     * SkyIconFontsLib.initRegister(context)
     *
     * // 使用自定义字体
     * SkyIconFontsLib.initRegister(context, listOf("fonts/custom_iconfont.ttf"))
     * ```
     */
    fun initRegister(context: Context, ttfPaths: List<String> = listOf(DEFAULT_TTF_PATH)) {
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

    // region 字体查找

    /**
     * 获取所有已注册的字体摘要信息
     */
    fun getRegisteredFonts(): List<FontInfo> {
        return Iconics.getRegisteredFonts()
            .filterIsInstance<SkyIconFontTypeface>()
            .map { FontInfo(it.fontName, it.mappingPrefix, it.iconCount) }
    }

    /**
     * 根据字体名称解析字体实例
     *
     * 查找优先级：
     * 1. fontName 作为 fontName 精确匹配（JSON 中的 name 字段）
     * 2. fontName 作为 mappingPrefix 匹配（css_prefix_text 前3个字符）
     * 3. fontName 为 null 时，使用默认字体（第一个注册的字体）
     *
     * @param fontName 字体名称，为 null 时使用默认字体
     * @throws IllegalStateException 未找到匹配字体时抛出
     */
    private fun resolveFont(fontName: String?): SkyIconFontTypeface {
        if (fontName != null) {
            // 优先按 fontName 匹配
            val byName = Iconics.getRegisteredFonts()
                .filterIsInstance<SkyIconFontTypeface>()
                .firstOrNull { it.fontName == fontName }
            if (byName != null) return byName

            // 其次按 mappingPrefix 匹配
            val byPrefix = Iconics.findFont(fontName)
            if (byPrefix is SkyIconFontTypeface) return byPrefix

            throw IllegalStateException(
                "未找到字体 '$fontName'，已注册的字体: " +
                    Iconics.getRegisteredFonts()
                        .filterIsInstance<SkyIconFontTypeface>()
                        .joinToString { "${it.fontName}(prefix=${it.mappingPrefix})" }
            )
        }
        return SkyIconFontTypeface.default ?: throw IllegalStateException(
            "SkyIconFontsLib 尚未初始化，请先调用 SkyIconFontsLib.initRegister()"
        )
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
     * @param iconName 图标名称
     * @param fontName 可选，指定字体名称。不传则使用默认字体
     *
     * 示例：
     * ```
     * // 使用默认字体
     * val text = "${SkyIconFontsLib.getFormattedIconName("skyshouye")} 首页"
     *
     * // 指定字体
     * val text = "${SkyIconFontsLib.getFormattedIconName("fenxiang", "another-iconfont")} 分享"
     *
     * textView.text = SkyIconFontsLib.style(SpannableString(text))
     * ```
     */
    fun getFormattedIconName(iconName: String, fontName: String? = null): String {
        val font = resolveFont(fontName)
        return font.getIcon(iconName).formattedName
    }

    // endregion

    // region 样式

    /**
     * 对 [Spanned] 文本进行图标样式处理，
     * 将文本中的 {prefix_name} 格式标记替换为对应的图标字符。
     * 自动根据前缀路由到对应的已注册字体，支持多字体混用。
     */
    fun style(spanned: Spanned): Spanned {
        return Iconics.style(spanned)
    }

    // endregion

    // region Drawable

    /**
     * 根据图标名称创建图标 Drawable
     *
     * @param iconName 图标名称
     * @param fontName 可选，指定字体名称。不传则使用默认字体
     *
     * 示例：
     * ```
     * // 使用默认字体
     * imageView.setImageDrawable(SkyIconFontsLib.drawable(context, "skyshouye"))
     *
     * // 指定字体
     * imageView.setImageDrawable(SkyIconFontsLib.drawable(context, "fenxiang", "another-iconfont"))
     * ```
     */
    fun drawable(context: Context, iconName: String, fontName: String? = null): Drawable {
        val font = resolveFont(fontName)
        val icon = font.getIcon(iconName)
        val drawable = IconicsDrawable(context, icon)
        drawable.typeface = icon.typeface.rawTypeface
        return drawable
    }

    // endregion

    // region 信息导出

    /**
     * 输出指定字体的完整信息（JSON 格式），包含元数据和所有图标映射
     *
     * @param fontName 可选，指定字体名称。不传则使用默认字体
     */
    fun getSkyIconFontInfoJson(fontName: String? = null): String {
        val font = resolveFont(fontName)
        return font.getSkyIconFontInfoJson()
    }

    // endregion
}
