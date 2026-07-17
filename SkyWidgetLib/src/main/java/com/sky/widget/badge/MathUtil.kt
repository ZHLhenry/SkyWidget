package com.sky.widget.badge

import android.graphics.PointF
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Badge 相关数学计算工具
 *
 * 提供拖拽 Badge 时所需的几何计算，包括象限判断、弧度转换、
 * 两点距离、内切点计算等。
 */
object MathUtil {

    /** 象限弧度（π/2） */
    private const val QUADRANT_RADIAN = PI / 2
    /** 圆周弧度（2π） */
    val CIRCLE_RADIAN: Double = 2 * PI

    /**
     * 根据反正切值和象限计算实际弧度
     *
     * @param atan 反正切值
     * @param quadrant 所在象限（1~4）
     * @return 修正后的弧度值
     */
    fun getTanRadian(atan: Double, quadrant: Int): Double {
        var result = if (atan < 0) atan + QUADRANT_RADIAN else atan
        result += QUADRANT_RADIAN * (quadrant - 1)
        return result
    }

    /** 弧度转角度 */
    fun radianToAngle(radian: Double): Double {
        return 360 * (radian / CIRCLE_RADIAN)
    }

    /**
     * 判断点 p 相对于 center 所在的象限（1~4）
     *
     * @return 象限编号 1~4，在坐标轴上返回 -1
     */
    fun getQuadrant(p: PointF, center: PointF): Int {
        return when {
            p.x > center.x -> when {
                p.y > center.y -> 4
                p.y < center.y -> 1
                else -> -1
            }
            p.x < center.x -> when {
                p.y > center.y -> 3
                p.y < center.y -> 2
                else -> -1
            }
            else -> -1
        }
    }

    /** 计算两点之间的直线距离 */
    fun getPointDistance(p1: PointF, p2: PointF): Float {
        val dx = (p1.x - p2.x).toDouble()
        val dy = (p1.y - p2.y).toDouble()
        return sqrt(dx * dx + dy * dy).toFloat()
    }

    /**
     * 计算内切点
     *
     * @param circleCenter 圆心坐标
     * @param radius       半径
     * @param slopeLine    过中点的直线斜率
     */
    fun getInnertangentPoints(
        circleCenter: PointF,
        radius: Float,
        slopeLine: Double?,
        points: MutableList<PointF?>
    ) {
        val xOffset: Float
        val yOffset: Float
        if (slopeLine != null) {
            val radian = atan(slopeLine).toFloat()
            xOffset = (cos(radian.toDouble()) * radius).toFloat()
            yOffset = (sin(radian.toDouble()) * radius).toFloat()
        } else {
            xOffset = radius
            yOffset = 0f
        }
        points.add(PointF(circleCenter.x + xOffset, circleCenter.y + yOffset))
        points.add(PointF(circleCenter.x - xOffset, circleCenter.y - yOffset))
    }
}
