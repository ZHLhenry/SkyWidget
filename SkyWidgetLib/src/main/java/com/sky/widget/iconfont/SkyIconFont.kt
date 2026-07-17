package com.sky.widget.iconfont

import android.graphics.Typeface
import com.mikepenz.iconics.typeface.IIcon
import com.mikepenz.iconics.typeface.ITypeface

/**
 * sky-iconfont 图标实现类（库内部使用）
 * 图标名称与 codepoint 的映射由同名 JSON 文件（iconfont.cn 导出）自动解析，
 * 无需在此硬编码。
 *
 * @param iconName 图标名称（用于 XML 中 iiv_icon 属性引用，如 "sky_shouye"）
 * @param char 图标对应的 Unicode 字符
 * @param typeface 图标所属的字体类型
 */
internal class SkyIconFont(
    private val iconName: String,
    private val char: Char,
    override val typeface: ITypeface
) : IIcon {

    override val character: Char get() = char
    override val formattedName: String get() = "{$iconName}"
    override val name: String get() = iconName
}

/**
 * sky-iconfont 自定义字体类型实现（库内部使用）
 * 消费者不应直接使用此类，应通过 [SkyIconFontsLib] 统一操作。
 *
 * @param ttfFilePath TTF 字体文件在 assets 中的路径，默认为 "fonts/sky_iconfont.ttf"
 *                    传入自定义 TTF 时，必须在其同目录下放置同名 JSON 文件（如 "fonts/custom_iconfont.json"），
 *                    JSON 文件为 iconfont.cn 导出的标准格式，内部会自动解析
 * @param iconNameToCodepoint 可选的名称→codepoint 覆盖映射，优先级高于 JSON 文件。
 *                            一般不需要传入，仅在需要覆盖 JSON 中的部分映射时使用
 */
internal class SkyIconFontTypeface(
    private val ttfFilePath: String = "fonts/sky_iconfont.ttf",
    private val iconNameToCodepoint: Map<String, Char>? = null
) : ITypeface {

    companion object {
        /**
         * 当前激活的字体实例，由 [SkyIconFontsLib.initRegister] 自动设置
         */
        internal var default: SkyIconFontTypeface? = null
    }

    private var _typeface: Typeface? = null
    private var _characters: Map<String, Char>? = null
    private var _icons: List<String>? = null
    private var _jsonResult: IconfontParseResult? = null
    private var _jsonLoaded: Boolean = false

    override val fontName: String get() { ensureJsonLoaded(); return _jsonResult!!.name.ifEmpty { ttfFilePath } }
    override val version: String = "1.0.0"
    override val iconCount: Int get() = icons.size
    //它与 JSON 中的 css_prefix_text（图标名称前缀）是两个不同的概念，但必须取 prefix 的前3个字符才能匹配
    override val mappingPrefix: String get() { ensureJsonLoaded(); return _jsonResult!!.prefix.take(3) }
    override val author: String = "henry"
    override val url: String = ""
    override val description: String get() { ensureJsonLoaded(); return _jsonResult!!.description }
    override val license: String = ""
    override val licenseUrl: String = ""
    override val fontRes: Int = 0

    override val icons: List<String>
        get() {
            if (_icons == null) {
                ensureJsonLoaded()
                _icons = _jsonResult!!.iconMapping.keys.toList()
            }
            return _icons!!
        }

    override val characters: Map<String, Char>
        get() {
            if (_characters == null) {
                ensureJsonLoaded()
                val map = HashMap<String, Char>()
                // 优先级1: 消费者显式传入的覆盖映射
                if (iconNameToCodepoint != null) {
                    map.putAll(iconNameToCodepoint)
                } else {
                    // 优先级2: 从同名 JSON 文件自动解析的映射
                    map.putAll(_jsonResult!!.iconMapping)
                }
                _characters = map
            }
            return _characters!!
        }

    /**
     * 加载 TTF 对应的 JSON 映射文件
     * JSON 文件路径由 TTF 路径推导：将 .ttf 扩展名替换为 .json
     * 例如：fonts/sky_iconfont.ttf → fonts/sky_iconfont.json
     *       fonts/testSky_iconfont.ttf → fonts/testSky_iconfont.json
     */
    private fun ensureJsonLoaded() {
        if (_jsonLoaded) return
        _jsonLoaded = true

        val jsonFilePath = ttfFilePath.substringBeforeLast('.', ttfFilePath) + ".json"
        try {
            val context = com.mikepenz.iconics.Iconics.applicationContext
            val jsonContent = context.assets.open(jsonFilePath).bufferedReader().use { it.readText() }
            _jsonResult = IconfontJsonParser.parse(jsonContent)
        } catch (e: java.io.FileNotFoundException) {
            throw IllegalArgumentException(
                "TTF '$ttfFilePath' 需要同名的 JSON 映射文件 '$jsonFilePath'，\n" +
                "请在 assets 目录下放置 iconfont.cn 导出的标准 JSON 文件（含 css_prefix_text 和 glyphs 字段）"
            )
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "解析 iconfont.json 文件 '$jsonFilePath' 失败: ${e.message}\n" +
                "请确认是 iconfont.cn 导出的标准 JSON 格式",
                e
            )
        }
    }

    /** 加载 TTF 字体文件，懒加载并缓存 */
    override val rawTypeface: Typeface
        get() {
            if (_typeface == null) {
                _typeface = Typeface.createFromAsset(
                    com.mikepenz.iconics.Iconics.applicationContext.assets, ttfFilePath
                )
            }
            return _typeface!!
        }

    /** 根据图标名称获取图标实例 */
    override fun getIcon(key: String): IIcon {
        val char = characters[key]
            ?: throw IllegalArgumentException("Icon '$key' not found in '$ttfFilePath'")
        return SkyIconFont(key, char, this)
    }

    /**
     * 输出字体完整信息（JSON 格式），包含元数据和所有图标映射
     */
    fun getSkyIconFontInfoJson(): String {
        ensureJsonLoaded()
        val r = _jsonResult!!
        val sb = StringBuilder()
        sb.append("{\n")
        sb.append("  \"name\": \"${escapeJson(r.name)}\",\n")
        sb.append("  \"font_file\": \"${escapeJson(ttfFilePath)}\",\n")
        sb.append("  \"prefix\": \"${escapeJson(r.prefix)}\",\n")
        sb.append("  \"description\": \"${escapeJson(r.description)}\",\n")
        sb.append("  \"version\": \"$version\",\n")
        sb.append("  \"author\": \"${escapeJson(author)}\",\n")
        sb.append("  \"icon_count\": $iconCount,\n")
        sb.append("  \"icons\": {\n")
        val iconList = characters.entries.sortedBy { it.key }
        iconList.forEachIndexed { index, (name, char) ->
            val comma = if (index < iconList.size - 1) "," else ""
            sb.append("    \"${escapeJson(name)}\": \"U+${char.code.toString(16).padStart(4, '0')}\"$comma\n")
        }
        sb.append("  }\n}")
        return sb.toString()
    }

    /** JSON 字符串转义 */
    private fun escapeJson(value: String): String {
        return value.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }
}
