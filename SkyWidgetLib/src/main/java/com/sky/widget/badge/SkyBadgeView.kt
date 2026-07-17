package com.sky.widget.badge

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.os.Parcelable
import android.text.TextPaint
import android.text.TextUtils
import android.util.AttributeSet
import android.util.SparseArray
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.core.graphics.createBitmap
import com.sky.widget.badge.MathUtil.getPointDistance
import com.sky.widget.badge.MathUtil.getQuadrant
import com.sky.widget.badge.MathUtil.getInnertangentPoints
import com.sky.widget.badge.MathUtil.getTanRadian
import com.sky.widget.badge.MathUtil.radianToAngle
import com.sky.widget.base.ext.dp2px
import com.sky.widget.base.ext.px2dp
import kotlin.math.atan

/**
 * 自定义 Badge 视图，支持拖拽、爆炸动画、数字/文本显示
 *
 * 通过 [bindTarget] 绑定到目标 View 后，Badge 会自动叠加显示在目标 View 上。
 * 支持以下功能：
 * - 数字/文本显示，超过 99 可配置为 "99+" 或精确显示
 * - 拖拽消除，拖出范围后播放爆炸消散动画
 * - 自定义背景颜色、Drawable、边框、阴影
 * - 九宫格方位对齐，支持偏移量微调
 *
 * 使用示例：
 * ```
 * SkyBadgeView(context)
 *     .bindTarget(targetView)
 *     .setBadgeNumber(5)
 * ```
 */
class SkyBadgeView private constructor(
    context: Context?,
    attrs: AttributeSet?,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), IBadge {

    protected var mColorBackground: Int = 0
    protected var mColorBackgroundBorder: Int = 0
    protected var mColorBadgeText: Int = 0
    protected var mDrawableBackground: Drawable? = null
    protected var mBitmapClip: Bitmap? = null
    protected var mDrawableBackgroundClip: Boolean = false
    protected var mBackgroundBorderWidth: Float = 0f
    protected var mBadgeTextSize: Float = 0f
    protected var mBadgePadding: Float = 0f
    protected var mBadgeNumber: Int = 0
    protected var mBadgeText: String? = null
    protected var mDraggable: Boolean = false
    protected var mDragging: Boolean = false
    protected var mExact: Boolean = false
    protected var mShowShadow: Boolean = false
    protected var mBadgeGravity: Int = 0
    protected var mGravityOffsetX: Float = 0f
    protected var mGravityOffsetY: Float = 0f

    protected var mDefaultRadius: Float = 0f
    protected var mFinalDragDistance: Float = 0f
    protected var mDragQuadrant: Int = 0
    protected var mDragOutOfRange: Boolean = false

    protected var mBadgeTextRect: RectF? = null
    protected var mBadgeBackgroundRect: RectF? = null
    protected var mDragPath: Path? = null

    protected var mBadgeTextFontMetrics: Paint.FontMetrics? = null

    protected var mBadgeCenter: PointF? = null
    protected var mDragCenter: PointF? = null
    protected var mRowBadgeCenter: PointF? = null
    protected var mControlPoint: PointF? = null

    protected var mInnertangentPoints: MutableList<PointF?>? = null

    protected var mTargetView: View? = null

    protected var mWidth: Int = 0
    protected var mHeight: Int = 0

    protected var mBadgeTextPaint: TextPaint? = null
    protected var mBadgeBackgroundPaint: Paint? = null
    protected var mBadgeBackgroundBorderPaint: Paint? = null

    protected var mAnimator: BadgeAnimator? = null

    protected var mDragStateChangedListener: IBadge.OnDragStateChangedListener? = null

    protected var mActivityRoot: ViewGroup? = null

    constructor(context: Context?) : this(context, null)

    init {
        initBadge()
    }

    /** 初始化 Badge 默认属性和绘制组件 */
    private fun initBadge() {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        mBadgeTextRect = RectF()
        mBadgeBackgroundRect = RectF()
        mDragPath = Path()
        mBadgeCenter = PointF()
        mDragCenter = PointF()
        mRowBadgeCenter = PointF()
        mControlPoint = PointF()
        mInnertangentPoints = ArrayList()
        mBadgeTextPaint = TextPaint().apply {
            isAntiAlias = true
            isSubpixelText = true
            isFakeBoldText = true
            xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        }
        mBadgeBackgroundPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
        }
        mBadgeBackgroundBorderPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
        }
        mColorBackground = -0x17b1c0
        mColorBadgeText = -0x1
        mBadgeTextSize = context.dp2px(11f).toFloat()
        mBadgePadding = context.dp2px(5f).toFloat()
        mBadgeNumber = 0
        mBadgeGravity = Gravity.END or Gravity.TOP
        mGravityOffsetX = context.dp2px(1f).toFloat()
        mGravityOffsetY = context.dp2px(1f).toFloat()
        mFinalDragDistance = context.dp2px(90f).toFloat()
        mShowShadow = true
        mDrawableBackgroundClip = false
        translationZ = 1000f
    }

    /**
     * 将 Badge 绑定到目标 View
     *
     * 会在目标 View 的父布局中插入一个 [BadgeContainer]，
     * 使 Badge 叠加显示在目标 View 之上。
     */
    override fun bindTarget(view: View?): IBadge {
        requireNotNull(view) { "targetView can not be null" }
        if (parent != null) {
            (parent as ViewGroup).removeView(this)
        }
        val targetParent = view.parent
        if (targetParent is ViewGroup) {
            mTargetView = view
            if (targetParent is BadgeContainer) {
                targetParent.addView(this)
            } else {
                val index = targetParent.indexOfChild(view)
                val targetParams = view.layoutParams
                targetParent.removeView(view)
                val badgeContainer = BadgeContainer(context)
                if (targetParent is RelativeLayout) {
                    badgeContainer.id = view.id
                }
                targetParent.addView(badgeContainer, index, targetParams)
                badgeContainer.addView(view)
                badgeContainer.addView(this)
            }
        } else {
            throw IllegalStateException("targetView must have a parent")
        }
        return this
    }

    override fun getTargetView(): View {
        return mTargetView!!
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (mActivityRoot == null) findViewRoot(mTargetView!!)
    }

    /** 查找 Activity 根布局，用于拖拽时将 Badge 提升到全屏层级 */
    private fun findViewRoot(view: View) {
        mActivityRoot = view.rootView as? ViewGroup
        if (mActivityRoot == null) {
            findActivityRoot(view)
        }
    }

    private fun findActivityRoot(view: View) {
        if (view.parent is View) {
            findActivityRoot(view.parent as View)
        } else if (view is ViewGroup) {
            mActivityRoot = view
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                val x = event.x
                val y = event.y
                val rect = mBadgeBackgroundRect!!
                if (mDraggable && event.getPointerId(event.actionIndex) == 0
                    && x > rect.left && x < rect.right && y > rect.top && y < rect.bottom
                    && mBadgeText != null
                ) {
                    initRowBadgeCenter()
                    mDragging = true
                    notifyDragStateChanged(IBadge.OnDragStateChangedListener.STATE_START)
                    mDefaultRadius = context.dp2px(7f).toFloat()
                    parent.requestDisallowInterceptTouchEvent(true)
                    screenFromWindow(true)
                    mDragCenter!!.x = event.rawX
                    mDragCenter!!.y = event.rawY
                }
            }

            MotionEvent.ACTION_MOVE -> if (mDragging) {
                mDragCenter!!.x = event.rawX
                mDragCenter!!.y = event.rawY
                invalidate()
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL ->
                if (event.getPointerId(event.actionIndex) == 0 && mDragging) {
                    mDragging = false
                    onPointerUp()
                }
        }
        return mDragging || super.onTouchEvent(event)
    }

    private fun onPointerUp() {
        if (mDragOutOfRange) {
            animateHide(mDragCenter!!)
            notifyDragStateChanged(IBadge.OnDragStateChangedListener.STATE_SUCCEED)
        } else {
            reset()
            notifyDragStateChanged(IBadge.OnDragStateChangedListener.STATE_CANCELED)
        }
    }

    /** 创建当前 Badge 的截图位图，用于爆炸动画 */
    protected fun createBadgeBitmap(): Bitmap {
        val dp3 = context.dp2px(3f)
        val bitmap = createBitmap(
            mBadgeBackgroundRect!!.width().toInt() + dp3,
            mBadgeBackgroundRect!!.height().toInt() + dp3,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawBadge(
            canvas,
            PointF(canvas.width / 2f, canvas.height / 2f),
            getBadgeCircleRadius()
        )
        return bitmap
    }

    /**
     * 将 Badge 从父布局分离或重新绑定
     *
     * @param screen true 时将 Badge 添加到 Activity 根布局（全屏拖拽模式），
     *               false 时重新绑定到目标 View
     */
    protected fun screenFromWindow(screen: Boolean) {
        if (parent != null) {
            (parent as ViewGroup).removeView(this)
        }
        if (screen) {
            mActivityRoot!!.addView(
                this, FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            )
        } else {
            bindTarget(mTargetView!!)
        }
    }

    /** 根据拖拽方向调整阴影偏移 */
    private fun showShadowImpl(showShadow: Boolean) {
        var x = context.dp2px(1f)
        var y = context.dp2px(1.5f)
        when (mDragQuadrant) {
            1 -> { x = context.dp2px(1f); y = context.dp2px(-1.5f) }
            2 -> { x = context.dp2px(-1f); y = context.dp2px(-1.5f) }
            3 -> { x = context.dp2px(-1f); y = context.dp2px(1.5f) }
            4 -> { x = context.dp2px(1f); y = context.dp2px(1.5f) }
        }
        mBadgeBackgroundPaint!!.setShadowLayer(
            (if (showShadow) context.dp2px(2f) else 0).toFloat(),
            x.toFloat(), y.toFloat(), 0x33000000
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mWidth = w
        mHeight = h
    }

    override fun onDraw(canvas: Canvas) {
        if (mAnimator != null && mAnimator!!.isRunning) {
            mAnimator!!.draw(canvas)
            return
        }
        if (mBadgeText != null) {
            initPaints()
            val badgeRadius = getBadgeCircleRadius()
            val startCircleRadius = mDefaultRadius * (1 - getPointDistance(
                mRowBadgeCenter!!, mDragCenter!!
            ) / mFinalDragDistance)
            if (mDraggable && mDragging) {
                mDragQuadrant = getQuadrant(mDragCenter!!, mRowBadgeCenter!!)
                showShadowImpl(mShowShadow)
                mDragOutOfRange = startCircleRadius < context.dp2px(1.5f)
                if (mDragOutOfRange) {
                    notifyDragStateChanged(IBadge.OnDragStateChangedListener.STATE_DRAGGING_OUT_OF_RANGE)
                    drawBadge(canvas, mDragCenter!!, badgeRadius)
                } else {
                    notifyDragStateChanged(IBadge.OnDragStateChangedListener.STATE_DRAGGING)
                    drawDragging(canvas, startCircleRadius, badgeRadius)
                    drawBadge(canvas, mDragCenter!!, badgeRadius)
                }
            } else {
                findBadgeCenter()
                drawBadge(canvas, mBadgeCenter!!, badgeRadius)
            }
        }
    }

    /** 初始化画笔属性（颜色、对齐等） */
    private fun initPaints() {
        showShadowImpl(mShowShadow)
        mBadgeBackgroundPaint!!.color = mColorBackground
        mBadgeBackgroundBorderPaint!!.color = mColorBackgroundBorder
        mBadgeBackgroundBorderPaint!!.strokeWidth = mBackgroundBorderWidth
        mBadgeTextPaint!!.color = mColorBadgeText
        mBadgeTextPaint!!.textAlign = Paint.Align.CENTER
    }

    /** 绘制拖拽状态下的 Badge 和贝塞尔曲线连接线 */
    private fun drawDragging(canvas: Canvas, startRadius: Float, badgeRadius: Float) {
        val dy = mDragCenter!!.y - mRowBadgeCenter!!.y
        val dx = mDragCenter!!.x - mRowBadgeCenter!!.x
        mInnertangentPoints!!.clear()
        val points = mInnertangentPoints!!
        if (dx != 0f) {
            val k2 = -1 / (dy / dx).toDouble()
            getInnertangentPoints(mDragCenter!!, badgeRadius, k2, points)
            getInnertangentPoints(mRowBadgeCenter!!, startRadius, k2, points)
        } else {
            getInnertangentPoints(mDragCenter!!, badgeRadius, 0.0, points)
            getInnertangentPoints(mRowBadgeCenter!!, startRadius, 0.0, points)
        }
        val path = mDragPath!!
        path.reset()
        path.addCircle(
            mRowBadgeCenter!!.x, mRowBadgeCenter!!.y, startRadius,
            if (mDragQuadrant == 1 || mDragQuadrant == 2) Path.Direction.CCW else Path.Direction.CW
        )
        mControlPoint!!.x = (mRowBadgeCenter!!.x + mDragCenter!!.x) / 2f
        mControlPoint!!.y = (mRowBadgeCenter!!.y + mDragCenter!!.y) / 2f
        val cp = mControlPoint!!
        path.moveTo(points[2]!!.x, points[2]!!.y)
        path.quadTo(cp.x, cp.y, points[0]!!.x, points[0]!!.y)
        path.lineTo(points[1]!!.x, points[1]!!.y)
        path.quadTo(cp.x, cp.y, points[3]!!.x, points[3]!!.y)
        path.lineTo(points[2]!!.x, points[2]!!.y)
        path.close()
        canvas.drawPath(path, mBadgeBackgroundPaint!!)

        // draw dragging border
        if (mColorBackgroundBorder != 0 && mBackgroundBorderWidth > 0) {
            path.reset()
            path.moveTo(points[2]!!.x, points[2]!!.y)
            path.quadTo(cp.x, cp.y, points[0]!!.x, points[0]!!.y)
            path.moveTo(points[1]!!.x, points[1]!!.y)
            path.quadTo(cp.x, cp.y, points[3]!!.x, points[3]!!.y)

            val startX: Float
            val startY: Float
            if (mDragQuadrant == 1 || mDragQuadrant == 2) {
                startX = points[2]!!.x - mRowBadgeCenter!!.x
                startY = mRowBadgeCenter!!.y - points[2]!!.y
            } else {
                startX = points[3]!!.x - mRowBadgeCenter!!.x
                startY = mRowBadgeCenter!!.y - points[3]!!.y
            }
            val startAngle = 360 - radianToAngle(
                getTanRadian(
                    atan((startY / startX).toDouble()),
                    if (mDragQuadrant - 1 == 0) 4 else mDragQuadrant - 1
                )
            ).toFloat()
            val rx = mRowBadgeCenter!!.x
            val ry = mRowBadgeCenter!!.y
            path.addArc(rx - startRadius, ry - startRadius, rx + startRadius, ry + startRadius, startAngle, 180f)
            canvas.drawPath(path, mBadgeBackgroundBorderPaint!!)
        }
    }

    /** 绘制 Badge 背景（圆形/圆角矩形）和文字 */
    private fun drawBadge(canvas: Canvas, center: PointF, radius: Float) {
        if (center.x == -1000f && center.y == -1000f) return
        var drawRadius = radius
        val textRect = mBadgeTextRect!!
        val bgRect = mBadgeBackgroundRect!!
        val bgPaint = mBadgeBackgroundPaint!!
        val borderPaint = mBadgeBackgroundBorderPaint!!

        if (mBadgeText!!.isEmpty() || mBadgeText!!.length == 1) {
            val r = drawRadius.toInt()
            bgRect.left = center.x - r
            bgRect.top = center.y - r
            bgRect.right = center.x + r
            bgRect.bottom = center.y + r
            if (mDrawableBackground != null) {
                drawBadgeBackground(canvas)
            } else {
                canvas.drawCircle(center.x, center.y, drawRadius, bgPaint)
                if (mColorBackgroundBorder != 0 && mBackgroundBorderWidth > 0) {
                    canvas.drawCircle(center.x, center.y, drawRadius, borderPaint)
                }
            }
        } else {
            val halfW = textRect.width() / 2f + mBadgePadding
            val halfH = textRect.height() / 2f + mBadgePadding * 0.5f
            bgRect.left = center.x - halfW
            bgRect.top = center.y - halfH
            bgRect.right = center.x + halfW
            bgRect.bottom = center.y + halfH
            drawRadius = bgRect.height() / 2f
            if (mDrawableBackground != null) {
                drawBadgeBackground(canvas)
            } else {
                canvas.drawRoundRect(bgRect, drawRadius, drawRadius, bgPaint)
                if (mColorBackgroundBorder != 0 && mBackgroundBorderWidth > 0) {
                    canvas.drawRoundRect(bgRect, drawRadius, drawRadius, borderPaint)
                }
            }
        }
        if (mBadgeText!!.isNotEmpty()) {
            val fm = mBadgeTextFontMetrics!!
            val textY = (bgRect.bottom + bgRect.top - fm.bottom - fm.top) / 2f
            canvas.drawText(mBadgeText!!, center.x, textY, mBadgeTextPaint!!)
        }
    }

    /** 绘制 Drawable 背景，支持裁剪为 Badge 形状 */
    private fun drawBadgeBackground(canvas: Canvas) {
        val bgPaint = mBadgeBackgroundPaint!!
        bgPaint.setShadowLayer(0f, 0f, 0f, 0)
        val left = mBadgeBackgroundRect!!.left.toInt()
        val top = mBadgeBackgroundRect!!.top.toInt()
        var right = mBadgeBackgroundRect!!.right.toInt()
        var bottom = mBadgeBackgroundRect!!.bottom.toInt()
        if (mDrawableBackgroundClip) {
            right = left + mBitmapClip!!.width
            bottom = top + mBitmapClip!!.height
            @Suppress("DEPRECATION")
            canvas.saveLayer(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), null, Canvas.ALL_SAVE_FLAG)
        }
        mDrawableBackground!!.setBounds(left, top, right, bottom)
        mDrawableBackground!!.draw(canvas)
        if (mDrawableBackgroundClip) {
            bgPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
            canvas.drawBitmap(mBitmapClip!!, left.toFloat(), top.toFloat(), bgPaint)
            canvas.restore()
            bgPaint.xfermode = null
            val borderPaint = mBadgeBackgroundBorderPaint!!
            if (mBadgeText!!.isEmpty() || mBadgeText!!.length == 1) {
                canvas.drawCircle(
                    mBadgeBackgroundRect!!.centerX(), mBadgeBackgroundRect!!.centerY(),
                    mBadgeBackgroundRect!!.width() / 2f, borderPaint
                )
            } else {
                canvas.drawRoundRect(
                    mBadgeBackgroundRect!!,
                    mBadgeBackgroundRect!!.height() / 2, mBadgeBackgroundRect!!.height() / 2,
                    borderPaint
                )
            }
        } else {
            canvas.drawRect(mBadgeBackgroundRect!!, mBadgeBackgroundBorderPaint!!)
        }
    }

    /** 创建裁剪蒙版位图，用于将 Drawable 背景裁剪为 Badge 形状 */
    private fun createClipLayer() {
        if (mBadgeText == null || !mDrawableBackgroundClip) return
        if (mBitmapClip != null && !mBitmapClip!!.isRecycled) {
            mBitmapClip!!.recycle()
        }
        val radius = getBadgeCircleRadius()
        val bgPaint = mBadgeBackgroundPaint!!
        if (mBadgeText!!.isEmpty() || mBadgeText!!.length == 1) {
            mBitmapClip = createBitmap(radius.toInt() * 2, radius.toInt() * 2, Bitmap.Config.ARGB_4444)
            val srcCanvas = Canvas(mBitmapClip!!)
            val size = srcCanvas.width / 2f
            srcCanvas.drawCircle(size, size, size, bgPaint)
        } else {
            mBitmapClip = createBitmap(
                (mBadgeTextRect!!.width() + mBadgePadding * 2).toInt(),
                (mBadgeTextRect!!.height() + mBadgePadding).toInt(),
                Bitmap.Config.ARGB_4444
            )
            val srcCanvas = Canvas(mBitmapClip!!)
            val w = srcCanvas.width.toFloat()
            val h = srcCanvas.height.toFloat()
            srcCanvas.drawRoundRect(0f, 0f, w, h, h / 2f, h / 2f, bgPaint)
        }
    }

    /** 计算 Badge 圆形背景半径 */
    private fun getBadgeCircleRadius(): Float {
        val textRect = mBadgeTextRect!!
        return when {
            mBadgeText!!.isEmpty() -> mBadgePadding
            mBadgeText!!.length == 1 -> {
                if (textRect.height() > textRect.width()) textRect.height() / 2f + mBadgePadding * 0.5f
                else textRect.width() / 2f + mBadgePadding * 0.5f
            }
            else -> mBadgeBackgroundRect!!.height() / 2f
        }
    }

    /** 根据 gravity 计算 Badge 中心坐标 */
    private fun findBadgeCenter() {
        val textRect = mBadgeTextRect!!
        val rectWidth = if (textRect.height() > textRect.width()) textRect.height() else textRect.width()
        val center = mBadgeCenter!!
        when (mBadgeGravity) {
            Gravity.START or Gravity.TOP -> {
                center.x = mGravityOffsetX + mBadgePadding + rectWidth / 2f
                center.y = mGravityOffsetY + mBadgePadding + textRect.height() / 2f
            }
            Gravity.START or Gravity.BOTTOM -> {
                center.x = mGravityOffsetX + mBadgePadding + rectWidth / 2f
                center.y = mHeight - (mGravityOffsetY + mBadgePadding + textRect.height() / 2f)
            }
            Gravity.END or Gravity.TOP -> {
                center.x = mWidth - (mGravityOffsetX + mBadgePadding + rectWidth / 2f)
                center.y = mGravityOffsetY + mBadgePadding + textRect.height() / 2f
            }
            Gravity.END or Gravity.BOTTOM -> {
                center.x = mWidth - (mGravityOffsetX + mBadgePadding + rectWidth / 2f)
                center.y = mHeight - (mGravityOffsetY + mBadgePadding + textRect.height() / 2f)
            }
            Gravity.CENTER -> {
                center.x = mWidth / 2f
                center.y = mHeight / 2f
            }
            Gravity.CENTER or Gravity.TOP -> {
                center.x = mWidth / 2f
                center.y = mGravityOffsetY + mBadgePadding + textRect.height() / 2f
            }
            Gravity.CENTER or Gravity.BOTTOM -> {
                center.x = mWidth / 2f
                center.y = mHeight - (mGravityOffsetY + mBadgePadding + textRect.height() / 2f)
            }
            Gravity.CENTER or Gravity.START -> {
                center.x = mGravityOffsetX + mBadgePadding + rectWidth / 2f
                center.y = mHeight / 2f
            }
            Gravity.CENTER or Gravity.END -> {
                center.x = mWidth - (mGravityOffsetX + mBadgePadding + rectWidth / 2f)
                center.y = mHeight / 2f
            }
        }
        initRowBadgeCenter()
    }

    /** 测量文本宽高，更新裁剪蒙版 */
    private fun measureText() {
        val textRect = mBadgeTextRect!!
        textRect.left = 0f
        textRect.top = 0f
        if (TextUtils.isEmpty(mBadgeText)) {
            textRect.right = 0f
            textRect.bottom = 0f
        } else {
            mBadgeTextPaint!!.textSize = mBadgeTextSize
            textRect.right = mBadgeTextPaint!!.measureText(mBadgeText)
            mBadgeTextFontMetrics = mBadgeTextPaint!!.fontMetrics
            textRect.bottom = mBadgeTextFontMetrics!!.descent - mBadgeTextFontMetrics!!.ascent
        }
        createClipLayer()
    }

    /** 将 Badge 中心坐标转换为屏幕绝对坐标 */
    private fun initRowBadgeCenter() {
        val screenPoint = IntArray(2)
        getLocationOnScreen(screenPoint)
        mRowBadgeCenter!!.x = mBadgeCenter!!.x + screenPoint[0]
        mRowBadgeCenter!!.y = mBadgeCenter!!.y + screenPoint[1]
    }

    /** 播放爆炸消散动画 */
    protected fun animateHide(center: PointF) {
        if (mBadgeText == null) return
        if (mAnimator == null || !mAnimator!!.isRunning) {
            screenFromWindow(true)
            mAnimator = BadgeAnimator(createBadgeBitmap(), center, this)
            mAnimator!!.start()
            setBadgeNumber(0)
        }
    }

    /** 重置拖拽状态，恢复 Badge 到初始位置 */
    fun reset() {
        mDragCenter!!.x = -1000f
        mDragCenter!!.y = -1000f
        mDragQuadrant = 4
        screenFromWindow(false)
        parent.requestDisallowInterceptTouchEvent(false)
        invalidate()
    }

    override fun hide(animate: Boolean) {
        if (animate && mActivityRoot != null) {
            initRowBadgeCenter()
            animateHide(mRowBadgeCenter!!)
        } else {
            setBadgeNumber(0)
        }
    }

    /**
     * @param badgeNum equal to zero badge will be hidden, less than zero show dot
     */
    override fun setBadgeNumber(badgeNum: Int): IBadge {
        mBadgeNumber = badgeNum
        mBadgeText = when {
            mBadgeNumber < 0 -> ""
            mBadgeNumber > 99 -> if (mExact) mBadgeNumber.toString() else "99+"
            mBadgeNumber in 1..99 -> mBadgeNumber.toString()
            else -> null
        }
        measureText()
        invalidate()
        return this
    }

    override fun getBadgeNumber(): Int = mBadgeNumber

    override fun setBadgeText(badgeText: String?): IBadge {
        mBadgeText = badgeText
        mBadgeNumber = 1
        measureText()
        invalidate()
        return this
    }

    override fun getBadgeText(): String? = mBadgeText

    override fun setExactMode(isExact: Boolean): IBadge {
        mExact = isExact
        if (mBadgeNumber > 99) setBadgeNumber(mBadgeNumber)
        return this
    }

    override fun isExactMode(): Boolean = mExact

    override fun setShowShadow(showShadow: Boolean): IBadge {
        mShowShadow = showShadow
        invalidate()
        return this
    }

    override fun isShowShadow(): Boolean = mShowShadow

    override fun setBadgeBackgroundColor(color: Int): IBadge {
        mColorBackground = color
        mBadgeTextPaint!!.xfermode = if (color == Color.TRANSPARENT) null else PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        invalidate()
        return this
    }

    override fun stroke(color: Int, width: Float, isDpValue: Boolean): IBadge {
        mColorBackgroundBorder = color
        mBackgroundBorderWidth = if (isDpValue) context.dp2px(width).toFloat() else width
        invalidate()
        return this
    }

    override fun getBadgeBackgroundColor(): Int = mColorBackground

    override fun setBadgeBackground(drawable: Drawable?): IBadge = setBadgeBackground(drawable, false)

    override fun setBadgeBackground(drawable: Drawable?, clip: Boolean): IBadge {
        mDrawableBackgroundClip = clip
        mDrawableBackground = drawable
        createClipLayer()
        invalidate()
        return this
    }

    override fun getBadgeBackground(): Drawable? = mDrawableBackground

    override fun setBadgeTextColor(color: Int): IBadge {
        mColorBadgeText = color
        invalidate()
        return this
    }

    override fun getBadgeTextColor(): Int = mColorBadgeText

    override fun setBadgeTextSize(size: Float, isSpValue: Boolean): IBadge {
        mBadgeTextSize = if (isSpValue) context.dp2px(size).toFloat() else size
        measureText()
        invalidate()
        return this
    }

    override fun getBadgeTextSize(isSpValue: Boolean): Float {
        return if (isSpValue) context.px2dp(mBadgeTextSize).toFloat() else mBadgeTextSize
    }

    override fun setBadgePadding(padding: Float, isDpValue: Boolean): IBadge {
        mBadgePadding = if (isDpValue) context.dp2px(padding).toFloat() else padding
        createClipLayer()
        invalidate()
        return this
    }

    override fun getBadgePadding(isDpValue: Boolean): Float {
        return if (isDpValue) context.px2dp(mBadgePadding).toFloat() else mBadgePadding
    }

    override fun isDraggable(): Boolean = mDraggable

    /**
     * @param gravity only support Gravity.START | Gravity.TOP , Gravity.END | Gravity.TOP ,
     * Gravity.START | Gravity.BOTTOM , Gravity.END | Gravity.BOTTOM ,
     * Gravity.CENTER , Gravity.CENTER | Gravity.TOP , Gravity.CENTER | Gravity.BOTTOM ,
     * Gravity.CENTER | Gravity.START , Gravity.CENTER | Gravity.END
     */
    override fun setBadgeGravity(gravity: Int): IBadge {
        val supportedGravities = setOf(
            Gravity.START or Gravity.TOP, Gravity.END or Gravity.TOP,
            Gravity.START or Gravity.BOTTOM, Gravity.END or Gravity.BOTTOM,
            Gravity.CENTER, Gravity.CENTER or Gravity.TOP, Gravity.CENTER or Gravity.BOTTOM,
            Gravity.CENTER or Gravity.START, Gravity.CENTER or Gravity.END
        )
        if (gravity in supportedGravities) {
            mBadgeGravity = gravity
            invalidate()
        } else {
            throw IllegalStateException(
                "only support Gravity.START | Gravity.TOP , Gravity.END | Gravity.TOP , " +
                    "Gravity.START | Gravity.BOTTOM , Gravity.END | Gravity.BOTTOM , Gravity.CENTER" +
                    " , Gravity.CENTER | Gravity.TOP , Gravity.CENTER | Gravity.BOTTOM ," +
                    "Gravity.CENTER | Gravity.START , Gravity.CENTER | Gravity.END"
            )
        }
        return this
    }

    override fun getBadgeGravity(): Int = mBadgeGravity

    override fun setGravityOffset(offset: Float, isDpValue: Boolean): IBadge {
        return setGravityOffset(offset, offset, isDpValue)
    }

    override fun setGravityOffset(offsetX: Float, offsetY: Float, isDpValue: Boolean): IBadge {
        mGravityOffsetX = if (isDpValue) context.dp2px(offsetX).toFloat() else offsetX
        mGravityOffsetY = if (isDpValue) context.dp2px(offsetY).toFloat() else offsetY
        invalidate()
        return this
    }

    override fun getGravityOffsetX(isDpValue: Boolean): Float {
        return if (isDpValue) context.px2dp(mGravityOffsetX).toFloat() else mGravityOffsetX
    }

    override fun getGravityOffsetY(isDpValue: Boolean): Float {
        return if (isDpValue) context.px2dp(mGravityOffsetY).toFloat() else mGravityOffsetY
    }

    /** 通知拖拽状态变化 */
    private fun notifyDragStateChanged(state: Int) {
        mDragStateChangedListener?.onDragStateChanged(state, this, mTargetView)
    }

    override fun setOnDragStateChangedListener(l: IBadge.OnDragStateChangedListener?): IBadge {
        mDraggable = l != null
        mDragStateChangedListener = l
        return this
    }

    override fun getDragCenter(): PointF? {
        return if (mDraggable && mDragging) mDragCenter else null
    }

    /** Badge 容器布局，用于包裹目标 View 和 Badge，实现叠加显示 */
    private inner class BadgeContainer(context: Context?) : ViewGroup(context) {
        override fun dispatchRestoreInstanceState(container: SparseArray<Parcelable?>?) {
            if (parent !is RelativeLayout) {
                super.dispatchRestoreInstanceState(container)
            }
        }

        override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
            for (i in 0 until childCount) {
                val child = getChildAt(i)
                child.layout(0, 0, child.measuredWidth, child.measuredHeight)
            }
        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            var targetView: View? = null
            var badgeView: View? = null
            for (i in 0 until childCount) {
                val child = getChildAt(i)
                if (child !is SkyBadgeView) {
                    targetView = child
                } else {
                    badgeView = child
                }
            }
            if (targetView == null) {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            } else {
                targetView.measure(widthMeasureSpec, heightMeasureSpec)
                badgeView?.measure(
                    MeasureSpec.makeMeasureSpec(targetView.measuredWidth, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(targetView.measuredHeight, MeasureSpec.EXACTLY)
                )
                setMeasuredDimension(targetView.measuredWidth, targetView.measuredHeight)
            }
        }
    }
}
