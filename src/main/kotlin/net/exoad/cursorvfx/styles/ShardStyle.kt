package net.exoad.cursorvfx.styles

import net.exoad.cursorvfx.*
import java.awt.Color
import java.awt.Graphics2D
import java.awt.Polygon
import java.awt.RadialGradientPaint
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

object ShardStyle : TrailStyleSpec {
    override val id = TrailStyle.SHARD
    override val ghostCount = 15
    override val trailSampleGap = 2
    override val trailHistorySize = 60
    override val cursorSmoothing = 0.28
    override val moveThresholdPx = 0.2
    override val idleFadeFrames = 26
    override val fadeInLerp = 0.2
    override val fadeOutLerp = 0.22

    override fun activeLerp(index: Int): Double {
        return (0.33 - index * 0.015).coerceAtLeast(0.09)
    }

    override fun idleLerp(index: Int): Double {
        return (0.22 - index * 0.01).coerceAtLeast(0.07)
    }

    override fun ghostSize(index: Int): Int {
        return (28 - index).coerceAtLeast(7)
    }

    override fun ghostBaseAlpha(index: Int): Float {
        return (1.0f - index * 0.055f).coerceAtLeast(0.14f)
    }

    override fun targetFor(index: Int, state: TrailFrameState, history: TrailHistory): GhostTarget {
        val easedIdle = TrailMath.easeOutCubic(state.idleProgress)
        val hasHistory = history.size > 0
        val anchorIndex = ((index + 1) * trailSampleGap).coerceAtMost((history.size - 1).coerceAtLeast(0))
        return GhostTarget(
            TrailMath.lerp(
                if (hasHistory) history.xAt(anchorIndex).toDouble() else state.cursorX,
                state.cursorX,
                easedIdle
            ),
            TrailMath.lerp(
                if (hasHistory) history.yAt(anchorIndex).toDouble() else state.cursorY,
                state.cursorY,
                easedIdle
            ),
            if (state.moving) 1.0 else (1.0 - easedIdle),
            shardColor(index, state)
        )
    }

    override fun paintGhost(g2: Graphics2D, context: GhostPaintContext) {
        val cx = context.width / 2f
        val cy = context.height / 2f
        val radius = context.size / 2f
        val alphaScale = context.alphaScale
        with(g2) {
            paint = RadialGradientPaint(
                cx,
                cy,
                radius,
                floatArrayOf(0.0f, 1.0f),
                arrayOf(
                    Color(
                        context.color.red,
                        context.color.green,
                        context.color.blue,
                        (170f * alphaScale).toInt().coerceIn(0, 255)
                    ),
                    Color(context.color.red, context.color.green, context.color.blue, 0)
                )
            )
            fillOval(2, 2, context.size, context.size)
            color = Color(255, 255, 255, (190f * alphaScale).toInt().coerceIn(0, 255))
            fillPolygon(starPolygon(cx, cy, radius * 0.58f, radius * 0.2f, 6))
        }
    }

    private fun starPolygon(cx: Float, cy: Float, outer: Float, inner: Float, spikes: Int): Polygon {
        val points = spikes * 2
        val x = IntArray(points)
        val y = IntArray(points)
        for (i in 0 until points) {
            val angle = i * Math.PI / spikes - Math.PI / 2.0
            val radius = if (i % 2 == 0) outer else inner
            x[i] = (cx + cos(angle).toFloat() * radius).roundToInt()
            y[i] = (cy + sin(angle).toFloat() * radius).roundToInt()
        }
        return Polygon(x, y, points)
    }

    private fun shardColor(index: Int, state: TrailFrameState): Color {
        val speedRatio = (state.speed / 10.5).coerceIn(0.0, 1.0)
        return Color.getHSBColor(
            ((((0.52 + index * 0.08 + state.frame * 0.004 + speedRatio * 0.15) % 1.0).toFloat() * 6f).toInt() / 6f),
            (0.55f + speedRatio.toFloat() * 0.35f).coerceIn(0f, 1f),
            (0.7f + (1.0 - state.idleProgress).toFloat() * 0.25f).coerceIn(0f, 1f)
        )
    }
}
