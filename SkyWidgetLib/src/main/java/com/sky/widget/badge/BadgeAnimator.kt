package com.sky.widget.badge

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import androidx.core.graphics.get
import java.lang.ref.WeakReference
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/**
 * Badge 爆炸消散动画
 *
 * 将 Badge 位图拆分为多个碎片片段，通过 [ValueAnimator] 驱动碎片向外扩散并逐渐缩小，
 * 实现爆炸消散的视觉效果。动画结束后自动回调 [SkyBadgeView.reset] 重置状态。
 *
 * @param badgeBitmap Badge 截图位图，构造后会被 recycle
 * @param center Badge 中心坐标，作为碎片的起始扩散点
 * @param badge 关联的 SkyBadgeView，使用弱引用避免内存泄漏
 */
class BadgeAnimator(badgeBitmap: Bitmap, center: PointF, badge: SkyBadgeView?) : ValueAnimator() {

    /** 碎片二维数组，按行列排列 */
    private val fragments: Array<Array<BitmapFragment?>>
    /** SkyBadgeView 弱引用，防止动画期间 View 被回收导致异常 */
    private val weakBadge: WeakReference<SkyBadgeView?> = WeakReference(badge)

    init {
        setFloatValues(0f, 1f)
        duration = 500
        fragments = createFragments(badgeBitmap, center)
        addUpdateListener {
            val badgeView = weakBadge.get()
            if (badgeView == null || !badgeView.isShown) {
                cancel()
            } else {
                badgeView.invalidate()
            }
        }
        addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                weakBadge.get()?.reset()
            }
        })
    }

    /** 在 Canvas 上绘制所有碎片，根据当前动画进度更新碎片位置和大小 */
    fun draw(canvas: Canvas) {
        val value = animatedValue as Float
        for (row in fragments) {
            for (bf in row) {
                bf?.update(value, canvas)
            }
        }
    }

    /**
     * 将 Badge 位图切割为碎片数组
     *
     * 按固定尺寸（min(width, height) / 6）将位图均匀分割为 rows × cols 个碎片，
     * 每个碎片记录其颜色、初始位置和尺寸。切割完成后回收原始位图。
     */
    private fun createFragments(badgeBitmap: Bitmap, center: PointF): Array<Array<BitmapFragment?>> {
        val width = badgeBitmap.width
        val height = badgeBitmap.height
        val fragmentSize = min(width, height) / 6f
        val startX = center.x - width / 2f
        val startY = center.y - height / 2f
        val rows = (height / fragmentSize).toInt()
        val cols = (width / fragmentSize).toInt()
        val result = Array(rows) { arrayOfNulls<BitmapFragment>(cols) }
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                val bf = BitmapFragment()
                bf.color = badgeBitmap[(j * fragmentSize).toInt(), (i * fragmentSize).toInt()]
                bf.x = startX + j * fragmentSize
                bf.y = startY + i * fragmentSize
                bf.size = fragmentSize
                bf.maxSize = max(width, height)
                result[i][j] = bf
            }
        }
        badgeBitmap.recycle()
        return result
    }

    /** 单个碎片片段，记录颜色、位置和尺寸，每帧更新时随机偏移并缩小 */
    private inner class BitmapFragment {
        var x: Float = 0f
        var y: Float = 0f
        var size: Float = 0f
        var color: Int = 0
        var maxSize: Int = 0
        private val paint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
        }

        fun update(value: Float, canvas: Canvas) {
            paint.color = color
            x += 0.1f * Random.nextInt(maxSize) * (Random.nextFloat() - 0.5f)
            y += 0.1f * Random.nextInt(maxSize) * (Random.nextFloat() - 0.5f)
            canvas.drawCircle(x, y, size - value * size, paint)
        }
    }
}
