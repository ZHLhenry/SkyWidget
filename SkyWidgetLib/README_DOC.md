# SkyWidgetLib 技术文档

> Android 自定义组件库，提供 Badge 角标、增强型 BottomNavigationView、IconFont 图标字体三大核心功能模块。

---

## 目录

- [1. 项目概览](#1-项目概览)
- [2. 技术栈与版本](#2-技术栈与版本)
- [3. 工程结构](#3-工程结构)
- [4. 构建配置](#4-构建配置)
- [5. 模块详解](#5-模块详解)
  - [5.1 Badge 角标模块](#51-badge-角标模块)
  - [5.2 BottomNavigationView 增强模块](#52-bottomnavigationview-增强模块)
  - [5.3 IconFont 图标字体模块](#53-iconfont-图标字体模块)
  - [5.4 Base 基础扩展](#54-base-基础扩展)
- [6. 快速接入指南](#6-快速接入指南)
- [7. API 参考](#7-api-参考)
- [8. 注意事项](#8-注意事项)

---

## 1. 项目概览

SkyWidget 是一个 Android 自定义组件库项目，包含两个 Gradle 模块：

| 模块 | 说明 |
|------|------|
| `app` | 示例应用（`com.sky.widget.sample`），演示各组件用法 |
| `SkyWidgetLib` | 核心组件库（`com.sky.widget`），供业务方集成使用 |

**核心功能：**

- **SkyBadgeView** — 自定义 Badge 角标，支持数字/文本显示、拖拽消除、爆炸消散动画
- **SkyBottomNavigationView** — 增强型底部导航栏，突破原生 5 个菜单限制，支持图标/文字精细控制、ViewPager 联动、双击监听
- **SkyIconFontsLib** — IconFont 图标字体统一管理入口，封装 [Android-Iconics](https://github.com/mikepenz/Android-Iconics) 三方库，支持 iconfont.cn 导出的字体文件自动解析

---

## 2. 技术栈与版本

### 2.1 开发环境

| 项目 | 版本 |
|------|------|
| Android Studio | Quail 2 (2026.1.2) |
| JDK | JetBrains Runtime 21 (jbr-21) |
| Kotlin | 2.2.10 |
| Gradle | 配合 AGP 9.x |

### 2.2 编译配置

| 配置项 | 值 |
|--------|-----|
| compileSdk | 36 |
| minSdk | 24 (Android 7.0) |
| targetSdk | 35 |
| namespace | `com.sky.widget` |
| versionCode | 100 |
| versionName | 1.0.0 |

### 2.3 核心依赖

| 依赖 | 版本 | 用途 |
|------|------|------|
| `androidx.core:core-ktx` | 1.10.1 | Android KTX 扩展 |
| `androidx.appcompat:appcompat` | 1.6.1 | 兼容性支持 |
| `com.google.android.material:material` | 1.10.0 | Material Design 组件（BottomNavigationView 来源） |
| `com.mikepenz:iconics-core` | 5.5.0 | 图标字体框架（IconFont 模块基础） |

### 2.4 构建插件

| 插件 | 版本 | 说明 |
|------|------|------|
| `sky.android.library` | 1.2.0 | 内部 Android Library 构建插件 |
| `sky.android.publish` | 1.2.0 | 内部发布插件 |
| `com.android.library` | 9.2.1 | Android Library 官方插件 |

### 2.5 Gradle 配置

```properties
# gradle.properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
org.gradle.configuration-cache=true
kotlin.code.style=official
```

---

## 3. 工程结构

```
SkyWidgetLib/src/main/java/com/sky/widget/
├── badge/                              # Badge 角标模块
│   ├── BadgeAnimator.kt               #   爆炸消散动画
│   ├── IBadge.kt                      #   Badge 通用接口
│   ├── MathUtil.kt                    #   数学计算工具（象限、弧度、内切点）
│   └── SkyBadgeView.kt               #   核心 Badge 视图
│
├── base/ext/                          # 基础扩展
│   ├── ExtContext.kt                  #   dp/px 单位转换
│   ├── ExtMenu.kt                     #   Menu 索引计算扩展
│   ├── ExtReflect.kt                  #   反射安全访问工具
│   └── ExtViewGesture.kt            #   View 双击手势扩展
│
├── bottomNavigationView/              # BottomNavigationView 增强模块
│   ├── SkyBottomNavigationView.kt    #   核心增强视图
│   ├── BottomNavigationMenuView2.kt  #   自定义 MenuView（支持 >5 个菜单）
│   ├── gesture/                       #   手势监听
│   │   ├── BaseGestureListener.kt     #     基础手势监听器
│   │   └── OnDoubleClickListener.kt   #     双击监听器
│   ├── helper/                        #   联动辅助
│   │   ├── AbsViewPagerHelper.kt      #     ViewPager 辅助抽象基类
│   │   ├── BNVHelper.kt              #     核心业务逻辑辅助
│   │   ├── VPHelper.kt               #     ViewPager (v1) 适配
│   │   └── VP2Helper.kt              #     ViewPager2 适配
│   ├── iface/                         #   接口定义
│   │   ├── IBottomNavigationEx.kt     #     扩展功能接口
│   │   └── IMenuListener.kt           #     菜单监听接口
│   └── internal/
│       └── InnerListener.kt           #     内部事件监听
│
└── iconfont/                          # IconFont 图标字体模块
    ├── IconfontJsonParser.kt          #   iconfont.cn JSON 解析器
    ├── SkyIconFont.kt                 #   字体类型实现
    └── SkyIconFontsLib.kt            #   对外统一 API 入口
```

---

## 4. 构建配置

### 4.1 版本目录 (Version Catalog)

项目使用 `gradle/libs.versions.toml` 统一管理依赖版本：

```toml
[versions]
agp = "9.3.0"
kotlinGradlePlugin = "2.2.10"
coreKtx = "1.10.1"
appcompat = "1.6.1"
material = "1.10.0"
iconics = "5.5.0"
buildLogic = "1.2.0"
```

### 4.2 SkyWidgetLib 构建脚本

```kotlin
plugins {
    alias(libs.plugins.sky.android.library)
    alias(libs.plugins.sky.android.publish)
}

android {
    namespace = "com.sky.widget"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.iconics.core)
}
```

### 4.3 仓库源

- Google Maven
- Maven Central
- JitPack
- 阿里云镜像（central / google / public）
- 阿里云私有仓库（SkyMVVM / SkyBuildLogic）

---

## 5. 模块详解

### 5.1 Badge 角标模块

#### 5.1.1 架构设计

```
IBadge (接口)
  └── SkyBadgeView (实现)
        ├── BadgeAnimator (爆炸动画)
        └── MathUtil (几何计算)
```

#### 5.1.2 IBadge 接口

定义 Badge 的所有可配置属性，所有 setter 返回 `IBadge` 支持链式调用：

| 方法 | 说明 |
|------|------|
| `setBadgeNumber(Int)` | 设置数字，0 隐藏，负数显示圆点 |
| `setBadgeText(String?)` | 设置文本，优先级高于数字 |
| `setExactMode(Boolean)` | 精确模式，关闭后 >99 显示 "99+" |
| `setShowShadow(Boolean)` | 是否显示阴影 |
| `setBadgeBackgroundColor(Int)` | 设置背景颜色 |
| `stroke(Int, Float, Boolean)` | 设置边框样式 |
| `setBadgeBackground(Drawable?)` | 设置 Drawable 背景 |
| `setBadgeTextColor(Int)` | 设置文字颜色 |
| `setBadgeTextSize(Float, Boolean)` | 设置文字大小 |
| `setBadgePadding(Float, Boolean)` | 设置内边距 |
| `setBadgeGravity(Int)` | 设置对齐方式（九宫格方位） |
| `setGravityOffset(Float, Float, Boolean)` | 设置偏移量 |
| `setOnDragStateChangedListener(...)` | 设置拖拽监听，非空时自动开启拖拽 |
| `bindTarget(View?)` | 绑定到目标 View |
| `hide(Boolean)` | 隐藏 Badge（可选爆炸动画） |

**拖拽状态常量：**

| 常量 | 值 | 说明 |
|------|-----|------|
| `STATE_START` | 1 | 开始拖拽 |
| `STATE_DRAGGING` | 2 | 拖拽中 |
| `STATE_DRAGGING_OUT_OF_RANGE` | 3 | 拖拽超出范围 |
| `STATE_CANCELED` | 4 | 拖拽取消 |
| `STATE_SUCCEED` | 5 | 拖拽成功 |

#### 5.1.3 SkyBadgeView

自定义 View，继承 `View` 并实现 `IBadge` 接口。

**核心特性：**
- 软件渲染层（`LAYER_TYPE_SOFTWARE`），确保 `PorterDuffXfermode` 混合模式正常工作
- 通过 `BadgeContainer`（内部 `ViewGroup`）实现与目标 View 的叠加显示
- 拖拽时自动提升到 Activity 根布局全屏层级
- 支持贝塞尔曲线连接拖拽位置与原始位置

**支持的 Gravity 方位：**
- `START | TOP`、`END | TOP`、`START | BOTTOM`、`END | BOTTOM`
- `CENTER`、`CENTER | TOP`、`CENTER | BOTTOM`
- `CENTER | START`、`CENTER | END`

#### 5.1.4 BadgeAnimator

爆炸消散动画实现：

- 将 Badge 位图按 `min(width, height) / 6` 尺寸切割为碎片数组
- 通过 `ValueAnimator`（500ms）驱动碎片随机偏移并逐渐缩小
- 使用 `WeakReference<SkyBadgeView>` 避免内存泄漏
- 动画结束自动回调 `SkyBadgeView.reset()` 重置状态

#### 5.1.5 MathUtil

几何计算工具（`object` 单例）：

| 方法 | 说明 |
|------|------|
| `getTanRadian(atan, quadrant)` | 根据反正切值和象限计算实际弧度 |
| `radianToAngle(radian)` | 弧度转角度 |
| `getQuadrant(point, center)` | 判断点相对中心的象限（1~4） |
| `getPointDistance(p1, p2)` | 两点间距离 |
| `getInnertangentPoints(center, radius, slope, points)` | 计算圆的内切点 |

---

### 5.2 BottomNavigationView 增强模块

#### 5.2.1 架构设计

```
SkyBottomNavigationView (核心视图)
  ├── implements IBottomNavigationEx (扩展接口)
  ├── BottomNavigationMenuView2 (自定义 MenuView，支持 >5 个菜单)
  └── BNVHelper (核心业务辅助)
        ├── AbsViewPagerHelper (ViewPager 联动抽象)
        │     ├── VPHelper (ViewPager v1)
        │     └── VP2Helper (ViewPager2)
        ├── IMenuListener (菜单选择监听)
        ├── AbsMenuListener (含空菜单项监听)
        └── IMenuDoubleClickListener (双击监听)
```

#### 5.2.2 SkyBottomNavigationView

继承原生 `BottomNavigationView`，实现 `IBottomNavigationEx` 接口。

**增强功能一览：**

| 分类 | 功能 | 方法 |
|------|------|------|
| 图标控制 | 图标可见性 | `setIconVisibility(Boolean)` |
| | 图标尺寸 | `setIconSize(Float, Float)` / `setIconSizeAt(...)` |
| | 图标 Tint | `setIconTintList(...)` |
| | 图标边距 | `setIconsMarginTop(...)` / `setIconMarginTop(...)` |
| 文字控制 | 文字可见性 | `setTextVisibility(Boolean)` |
| | 文字大小 | `setSmallTextSize(...)` / `setLargeTextSize(...)` / `setTextSize(...)` |
| | 文字颜色 | `setTextTintList(...)` |
| | 字体样式 | `setTypeface(...)` |
| 动画 | 切换动画 | `enableAnimation(Boolean)` |
| | 标签可见性 | `enableLabelVisibility(Boolean)` |
| | 单项标签 | `enableBNItemViewLabelVisibility(...)` |
| 导航 | 当前选中 | `getCurrentIndex()` / `setCurrentItem(Int)` |
| | 菜单监听 | `setMenuListener(IMenuListener)` |
| | 双击监听 | `setMenuDoubleClickListener(...)` |
| 联动 | ViewPager | `setupWithViewPager(...)` |
| | ViewPager2 | `setupWithViewPager2(...)` |
| 高级 | 空菜单占位 | `setEmptyMenuIds(List<Int>)` |
| | 动态配置菜单 | `configDynamic(Int, generator)` |
| | 菜单最大数 | `getMenuMaxItemCount()` |
| | 高度控制 | `setBNMenuViewHeight(Int)` |
| | 水平平移 | `enableItemHorizontalTranslation(Boolean)` |
| | 状态恢复 | `saveInstanceState()` / `restoreInstanceState(...)` |
| 中心图标 | 空菜单占位 | `setEmptyMenuIds(List<Int>)` + `onEmptyItemClick` |
| | 单项标签隐藏 | `enableBNItemViewLabelVisibility(pos, false)` |
| | 单项图标放大 | `setIconSizeAt(pos, width, height)` |

#### 5.2.3 BottomNavigationMenuView2

自定义 `BottomNavigationMenuView`，解决原生最多 5 个菜单项的限制：

- 当菜单项 > 5 个时，自动按 `min(activeItemMaxWidth, width / 5)` 均分宽度
- 通过构造函数从原始 MenuView 反射复制所有样式属性
- 跳过 `GONE` 状态的子 View 不参与测量

#### 5.2.4 BNVHelper

核心业务逻辑协调器：

- **菜单选择处理**：过滤空菜单项、计算真实位置、防重复选中
- **ViewPager 联动**：页面滑动时同步导航栏选中，导航点击时同步页面
- **双击监听**：在子线程中为每个非空菜单项注册 `GestureDetector`

**空菜单项机制：**
`emptyMenuIds` 列表中的菜单项 ID 不参与正常选择流程，点击时回调 `AbsMenuListener.onEmptyItemClick()`，适用于添加特殊按钮（如中间 "+" 号）。

**中心图标 Tab：**
通过在菜单中间位置放置一个空菜单项（title 为空），配合以下 API 实现突出效果：
- `setEmptyMenuIds()` 标记空占位项
- `enableBNItemViewLabelVisibility(position, false)` 隐藏文字
- `setIconSizeAt(position, w, h)` 放大图标
- 自定义 View 背景（圆形）+ 负 `topMargin` 向上偏移 + `elevation` 阴影

#### 5.2.5 手势监听

| 类 | 说明 |
|------|------|
| `BaseGestureListener` | 基础手势监听器，通过 `enableAll` 控制事件消费 |
| `OnDoubleClickListener` | 双击监听器，继承 `BaseGestureListener`，始终消费 `onDown` |

#### 5.2.6 接口定义

**IBottomNavigationEx\<BNV, BNMV, BNIV\>**

泛型接口，定义了所有增强功能的方法签名。泛型参数：
- `BNV` — BottomNavigationView 类型
- `BNMV` — BottomNavigationMenuView 类型
- `BNIV` — BottomNavigationItemView 类型

**IMenuListener / AbsMenuListener**

```kotlin
// 基础菜单监听
interface IMenuListener {
    fun onNavigationItemSelected(position: Int, menu: MenuItem, isReSelected: Boolean): Boolean
}

// 含空菜单项监听（抽象类）
abstract class AbsMenuListener : IMenuListener {
    abstract fun onEmptyItemClick(position: Int, menu: MenuItem)
}

// 双击监听
interface IMenuDoubleClickListener {
    fun onDoubleClick(position: Int, menu: MenuItem)
}
```

---

### 5.3 IconFont 图标字体模块

#### 5.3.1 架构设计

```
SkyIconFontsLib (对外统一 API)
  └── SkyIconFontTypeface (ITypeface 实现)
        ├── IconfontJsonParser (JSON 解析)
        │     └── JsonTokenizer (轻量 JSON 分词器)
        └── SkyIconFont (IIcon 实现)
```

基于 [Android-Iconics](https://github.com/mikepenz/Android-Iconics) 5.5.0 构建，消费者无需感知 Iconics 类型体系。

#### 5.3.2 SkyIconFontsLib

对外统一 API 入口（`object` 单例），所有方法参数和返回值均为基础类型：

| 方法 | 说明 |
|------|------|
| `initRegister(Context, vararg String)` | 初始化并注册字体，应在 `Application.onCreate` 调用 |
| `isInitDone()` | 是否已完成初始化 |
| `isIconExists(String)` | 判断图标是否存在 |
| `getFormattedIconName(String)` | 获取 formattedName（如 `{skyshouye}`） |
| `style(Spanned)` | 对文本进行图标样式处理 |
| `drawable(Context, String)` | 根据名称创建图标 Drawable |
| `getSkyIconFontInfoJson()` | 输出当前字体的完整信息（JSON） |

#### 5.3.3 IconfontJsonParser

轻量级 JSON 解析器，不依赖第三方库：

- 解析 iconfont.cn 导出的标准 JSON 文件
- 提取 `name`、`css_prefix_text`、`description`、`glyphs` 字段
- 内部 `JsonTokenizer` 实现完整的 JSON 词法分析（对象、数组、字符串、数字、布尔、null、Unicode 转义）

**JSON 格式要求：**
```json
{
  "name": "testSky-iconfont",
  "css_prefix_text": "testSky",
  "description": "测试项目",
  "glyphs": [
    { "font_class": "fenxiang", "unicode": "e7fb" }
  ]
}
```

#### 5.3.4 SkyIconFontTypeface

`ITypeface` 实现，核心特性：

- **懒加载**：TTF 字体和 JSON 映射文件均在首次使用时加载
- **JSON 自动推导**：TTF 路径 `.ttf` → `.json`（如 `fonts/sky_iconfont.ttf` → `fonts/sky_iconfont.json`）
- **映射优先级**：显式传入的 `iconNameToCodepoint` > JSON 文件解析结果
 `mappingPrefix` 从 JSON 的 `css_prefix_text` 动态推导（取前 3 个字符），与 Iconics 框架的 `fontKey` 提取逻辑对齐

#### 5.3.5 SkyIconFont

`IIcon` 实现，单个图标的封装：

| 属性 | 说明 |
|------|------|
| `character` | 图标对应的 Unicode 字符 |
| `formattedName` | 格式化名称，如 `{sky_shouye}` |
| `name` | 图标名称 |
| `typeface` | 所属字体类型 |

---

### 5.4 Base 基础扩展

#### 5.4.1 ExtContext — dp/px 转换

```kotlin
fun Context.dp2px(dpValue: Number): Int  // dp → px（四舍五入）
fun Context.px2dp(pxValue: Number): Int  // px → dp（四舍五入）
```

#### 5.4.2 ExtMenu — Menu 索引扩展

| 方法 | 说明 |
|------|------|
| `Menu.filterEmptyMenuIndex(MenuItem, List<Int>)` | 过滤空菜单项后的真实索引 |
| `Menu.indexOf(MenuItem)` | 计算 MenuItem 在 Menu 中的下标 |
| `Menu.emptyCountBeforeMenuItem(MenuItem, List<Int>)` | 获取指定项之前的空菜单项数量 |

#### 5.4.3 ExtReflect — 反射工具

| 方法 | 说明 |
|------|------|
| `T.safeAccess(ignoreFinal, block)` | 安全访问，自动恢复 accessible 状态 |
| `T?.setFieldValue(fieldName, value)` | 设置字段值 |
| `T.getFieldValue(fieldName)` | 获取字段值（强转） |
| `T.getRawFieldValue(fieldName)` | 获取字段原始值（不强转） |
| `T.safeGetFieldValue(fieldName)` | 安全获取字段值（失败返回 null） |
| `T?.invokeMethod(funName, vararg args)` | 安全调用方法 |

**兼容性处理：**
- API > 21：直接修改 `Field.accessFlags`
- API ≤ 21：通过 `ArtField` 间接修改

#### 5.4.4 ExtViewGesture — 双击扩展

```kotlin
internal fun <T : View> T.onDoubleClick(enableAll: Boolean = true, onDoubleClick: () -> Unit)
```

为 View 添加双击手势监听，仅限库内部使用（`@RestrictTo(LIBRARY_GROUP)`）。

---

## 6. 快速接入指南

### 6.1 添加依赖

```kotlin
// app/build.gradle.kts
dependencies {
    implementation("com.sky.lib:SkyWidget:最新版本号")
}
```

### 6.2 使用 SkyBadgeView

```kotlin
// 绑定到目标 View 并显示数字
SkyBadgeView(context)
    .bindTarget(targetView)
    .setBadgeNumber(5)

// 显示文本
SkyBadgeView(context)
    .bindTarget(targetView)
    .setBadgeText("NEW")

// 自定义样式
SkyBadgeView(context)
    .bindTarget(targetView)
    .setBadgeNumber(99)
    .setBadgeBackgroundColor(Color.RED)
    .setBadgeTextColor(Color.WHITE)
    .setBadgeGravity(Gravity.END or Gravity.TOP)
    .setExactMode(false)  // >99 显示 "99+"

// 拖拽消除
SkyBadgeView(context)
    .bindTarget(targetView)
    .setBadgeNumber(3)
    .setOnDragStateChangedListener(object : IBadge.OnDragStateChangedListener {
        override fun onDragStateChanged(dragState: Int, badge: IBadge?, targetView: View?) {
            when (dragState) {
                STATE_SUCCEED -> Toast.makeText(context, "消除成功", Toast.LENGTH_SHORT).show()
            }
        }
    })

// 隐藏（带动画）
badgeView.hide(animate = true)
```

### 6.3 使用 SkyBottomNavigationView

```xml
<!-- 布局文件 -->
<com.sky.widget.bottomNavigationView.SkyBottomNavigationView
    android:id="@+id/bottomNav"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:menu="@menu/bottom_nav_menu" />
```

```kotlin
val bottomNav = findViewById<SkyBottomNavigationView>(R.id.bottomNav)

// 图标/文字控制
bottomNav
    .setIconVisibility(true)
    .setTextVisibility(true)
    .enableAnimation(true)
    .setIconSize(24f, 24f)
    .setTextSize(12f)

// 菜单监听
bottomNav.setMenuListener(object : AbsMenuListener() {
    override fun onNavigationItemSelected(position: Int, menu: MenuItem, isReSelected: Boolean): Boolean {
        // 处理选择
        return true
    }
    override fun onEmptyItemClick(position: Int, menu: MenuItem) {
        // 处理空菜单项点击
    }
})

// ViewPager2 联动
bottomNav.setupWithViewPager2(viewPager2, smoothScroll = true)

// 空菜单占位（如中间添加 "+" 按钮）
bottomNav.setEmptyMenuIds(listOf(R.id.nav_add))

// 中心图标 Tab（需配合菜单中间空 title 项使用）
val centerPos = 2
bottomNav.enableBNItemViewLabelVisibility(centerPos, false)  // 隐藏文字
bottomNav.setIconSizeAt(centerPos, 28f, 28f)                  // 放大图标
val centerItemView = bottomNav.getBNItemView(centerPos)
centerItemView?.setBackgroundResource(R.drawable.bg_center_tab) // 圆形背景
val params = centerItemView?.layoutParams as? MarginLayoutParams
params?.topMargin = -dp2px(16f)                                // 向上偏移
params?.bottomMargin = -dp2px(16f)
centerItemView?.layoutParams = params
centerItemView?.elevation = dp2px(8f).toFloat()               // 阴影

// 双击监听
bottomNav.setMenuDoubleClickListener(object : IMenuDoubleClickListener {
    override fun onDoubleClick(position: Int, menu: MenuItem) {
        // 处理双击
    }
})
```

### 6.4 使用 SkyIconFontsLib

```kotlin
// 1. Application.onCreate 中初始化
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // 传入 TTF 文件在 assets 中的路径
        // 自动在同目录查找同名 JSON（如 fonts/sky_iconfont.json）
        SkyIconFontsLib.initRegister(this, "fonts/sky_iconfont.ttf")
    }
}

// 2. 创建图标 Drawable
imageView.setImageDrawable(SkyIconFontsLib.drawable(context, "sky_shouye"))

// 3. 在文本中嵌入图标
val text = "${SkyIconFontsLib.getFormattedIconName("sky_shouye")} 首页"
textView.text = SkyIconFontsLib.style(SpannableString(text))

// 4. 检查图标是否存在
if (SkyIconFontsLib.isIconExists("sky_shouye")) { ... }

// 5. 导出字体完整信息
val json = SkyIconFontsLib.getSkyIconFontInfoJson()
```

**Assets 文件要求：**
```
assets/
└── fonts/
    ├── sky_iconfont.ttf      # TTF 字体文件
    └── sky_iconfont.json     # iconfont.cn 导出的同名 JSON
```

---

## 7. API 参考

### 7.1 类继承关系

```
// Badge 模块
View
  └── SkyBadgeView : IBadge

// BottomNavigationView 模块
BottomNavigationView
  └── SkyBottomNavigationView : IBottomNavigationEx<...>

BottomNavigationMenuView
  └── BottomNavigationMenuView2

// IconFont 模块
ITypeface
  └── SkyIconFontTypeface (internal)

IIcon
  └── SkyIconFont (internal)

// 手势
GestureDetector.SimpleOnGestureListener
  └── BaseGestureListener (abstract)
        └── OnDoubleClickListener

// ViewPager 联动
AbsViewPagerHelper<VP> (abstract)
  ├── VPHelper : AbsViewPagerHelper<ViewPager>
  └── VP2Helper : AbsViewPagerHelper<ViewPager2>
```

### 7.2 可见性说明

| 标记 | 说明 |
|------|------|
| `public` | 对外公开 API |
| `@RestrictTo(LIBRARY_GROUP)` | 仅限库内部使用 |
| `internal` | Kotlin 模块内部可见 |

---

## 8. 注意事项

### 8.1 Badge

- `SkyBadgeView` 使用软件渲染层，可能影响性能，避免在列表项中大量使用
- `bindTarget()` 会修改目标 View 的父布局结构（插入 `BadgeContainer`），需注意对布局层级的影响
- 拖拽消除功能需要将 Badge 提升到 Activity 根布局，可能与其他全屏 View 产生层级冲突
- `setBadgeGravity()` 仅支持文档中列出的 9 种方位组合，传入不支持的值会抛出 `IllegalStateException`

### 8.2 BottomNavigationView

- `configDynamic()` 会替换原生 `BottomNavigationMenuView` 为自定义的 `BottomNavigationMenuView2`，此操作不可逆
- 空菜单项（`emptyMenuIds`）不参与正常选择流程和位置计算
- 双击监听在子线程中设置，确保在 View 完成布局后执行
- 与 ViewPager 联动时，空菜单项会影响位置映射关系

### 8.3 IconFont

- `initRegister()` 必须在 `Application.onCreate` 中调用，否则后续 API 会抛出 `IllegalStateException`
- `mappingPrefix` 从 JSON 的 `css_prefix_text` 动态推导（取前 3 个字符）。Iconics 框架的 `placeFontIcon()` 固定取 `{` 后的前 3 个字符作为 `fontKey`，因此 `mappingPrefix` 必须与 `css_prefix_text` 的前 3 个字符一致，否则图标无法匹配渲染
- TTF 文件必须搭配同名 JSON 文件（仅扩展名不同），JSON 必须包含 `css_prefix_text` 和 `glyphs` 字段
- Unicode 值必须在 BMP 范围内（U+0001 ~ U+FFFF）

### 8.4 反射工具

- `ExtReflect` 中的反射操作依赖 Android 内部实现细节（`Field.accessFlags` / `ArtField`），在高版本 Android 上可能受限
- 建议仅在调试或特殊场景下使用，避免在核心业务逻辑中依赖
