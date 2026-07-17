## [v1.1.1] - 2026-07-17
- IconFont 模块多字体支持改造
  - 新增 `FontInfo` 数据类，提供已注册字体的摘要信息（fontName、mappingPrefix、iconCount）
  - `initRegister()` 签名变更：`vararg ttfPaths: String` → `ttfPaths: List<String> = listOf(DEFAULT_TTF_PATH)`，支持传入字体路径列表，未传时自动加载库内置默认字体 `fonts/sky_iconfont.ttf`
  - 新增 `getRegisteredFonts(): List<FontInfo>` 方法，可获取所有已注册字体的摘要信息
  - `getFormattedIconName()`、`drawable()`、`getSkyIconFontInfoJson()` 均新增可选 `fontName` 参数，支持按字体名称或 mappingPrefix 指定字体
  - `style()` 方法支持多字体混用，自动根据 `{prefix_name}` 前缀路由到对应已注册字体
  - 内部新增 `resolveFont()` 字体查找逻辑：fontName 精确匹配 → mappingPrefix 匹配 → 默认字体

## [v1.1.0] - 2026-07-17
- SkyWidget包重磅首发
