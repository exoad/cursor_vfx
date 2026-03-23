package net.exoad.cursorvfx.styles

import net.exoad.cursorvfx.*
import java.awt.*
import kotlin.math.roundToInt

object HaloStyle : TrailStyleSpec {
    override val id = TrailStyle.HALO
    override val ghostCount = 6
    override val trailSampleGap = 5
    override val trailHistorySize = 90
    override val cursorSmoothing = 0.45
    override val moveThresholdPx = 0.35
    override val idleFadeFrames = 28
    override val fadeInLerp = 0.16
    override val fadeOutLerp = 0.2

    override fun activeLerp(index: Int): Double {
        return (0.24 - index * 0.018).coerceAtLeast(0.1)
    }

    override fun idleLerp(index: Int): Double {
        return (0.18 - index * 0.013).coerceAtLeast(0.07)
    }

    override fun ghostSize(index: Int): Int {
        return (44 - index * 5).coerceAtLeast(12)
    }

    override fun ghostBaseAlpha(index: Int): Float {
        return (0.85f - index * 0.12f).coerceAtLeast(0.2f)
    }

    override fun targetFor(index: Int, state: TrailFrameState, history: TrailHistory): GhostTarget {
        val easedIdle = TrailMath.easeOutCubic(state.idleProgress)
        val hasHistory = history.size > 0
        val anchorIndex = ((index + 1) * trailSampleGap).coerceAtMost((history.size - 1).coerceAtLeast(0))
        val anchorX = if (hasHistory) history.xAt(anchorIndex).toDouble() else state.cursorX
        val anchorY = if (hasHistory) history.yAt(anchorIndex).toDouble() else state.cursorY
        return GhostTarget(
            TrailMath.lerp(anchorX, state.cursorX, easedIdle),
            TrailMath.lerp(anchorY, state.cursorY, easedIdle),
            if (state.moving) 1.0 else (1.0 - easedIdle),
            haloColor(index, state)
        )
    }

    override fun paintGhost(g2: Graphics2D, context: GhostPaintContext) {
        val cx = context.width / 2f
        val cy = context.height / 2f
        val alphaScale = context.alphaScale
        with(g2) {
            stroke = BasicStroke((1.6f + context.index * 0.03f).coerceAtMost(2.2f))
            color = Color(
                context.color.red,
                context.color.green,
                context.color.blue,
                (165f * alphaScale).toInt().coerceIn(0, 255)
            )
            drawOval(2, 2, context.size, context.size)
            color = Color(
                context.color.red,
                context.color.green,
                context.color.blue,
                (100f * alphaScale).toInt().coerceIn(0, 255)
            )
            drawOval(4, 4, (context.size - 4).coerceAtLeast(1), (context.size - 4).coerceAtLeast(1))
            paint = RadialGradientPaint(
                cx,
                cy,
                context.size / 2f * 0.7f,
                floatArrayOf(0.0f, 1.0f),
                arrayOf(
                    Color(255, 255, 255, (165f * alphaScale).toInt().coerceIn(0, 255)),
                    Color(context.color.red, context.color.green, context.color.blue, 0)
                )
            )
            val core = (context.size / 2f * 0.9f).roundToInt().coerceAtLeast(2)
            fillOval((cx - core / 2f).roundToInt(), (cy - core / 2f).roundToInt(), core, core)
        }
    }

    private fun haloColor(index: Int, state: TrailFrameState): Color {
        val speedRatio = (state.speed / 9.5).coerceIn(0.0, 1.0)
        return Color.getHSBColor(
            (0.48 + ((state.frame * 0.02) + index * 0.25) % 1.0 * 0.18 + speedRatio * 0.1).toFloat() % 1f,
            (0.35 + speedRatio * 0.45).toFloat().coerceIn(0f, 1f),
            (0.8 + (1.0 - state.idleProgress) * 0.18).toFloat().coerceIn(0f, 1f)
        )
    }
}
