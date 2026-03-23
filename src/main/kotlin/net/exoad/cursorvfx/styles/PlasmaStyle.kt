package net.exoad.cursorvfx.styles

import net.exoad.cursorvfx.*
import java.awt.Color
import java.awt.Graphics2D
import java.awt.RadialGradientPaint
import kotlin.math.roundToInt
import kotlin.math.sin

object PlasmaStyle : TrailStyleSpec {
    override val id = TrailStyle.PLASMA
    override val ghostCount = 22
    override val trailSampleGap = 1
    override val trailHistorySize = 96
    override val cursorSmoothing = 0.22
    override val moveThresholdPx = 0.12
    override val idleFadeFrames = 16
    override val fadeInLerp = 0.2
    override val fadeOutLerp = 0.3

    override fun activeLerp(index: Int): Double {
        return (0.34 - index * 0.012).coerceAtLeast(0.08)
    }

    override fun idleLerp(index: Int): Double {
        return (0.22 - index * 0.008).coerceAtLeast(0.06)
    }

    override fun ghostSize(index: Int): Int {
        return (34 - index).coerceAtLeast(5)
    }

    override fun ghostBaseAlpha(index: Int): Float {
        return (0.98f - index * 0.032f).coerceAtLeast(0.1f)
    }

    override fun targetFor(index: Int, state: TrailFrameState, history: TrailHistory): GhostTarget {
        val easedIdle = TrailMath.easeOutCubic(state.idleProgress)
        val hasHistory = history.size > 0
        val anchorIndex = ((index + 1) * trailSampleGap).coerceAtMost((history.size - 1).coerceAtLeast(0))
        val anchorX = if (hasHistory) history.xAt(anchorIndex).toDouble() else state.cursorX
        val anchorY = if (hasHistory) history.yAt(anchorIndex).toDouble() else state.cursorY
        val wobble = sin(state.frame * 0.12 + index * 0.7) * (3.2 * (1.0 - easedIdle))
        return GhostTarget(
            TrailMath.lerp(anchorX, state.cursorX, easedIdle) + wobble,
            TrailMath.lerp(anchorY, state.cursorY, easedIdle) - wobble * 0.5,
            if (state.moving) 1.0 else (1.0 - easedIdle),
            plasmaColor(index, state)
        )
    }

    override fun paintGhost(g2: Graphics2D, context: GhostPaintContext) {
        val cx = context.width / 2f
        val cy = context.height / 2f
        val alphaScale = context.alphaScale
        with(g2) {
            paint = RadialGradientPaint(
                cx,
                cy,
                context.size / 2f,
                floatArrayOf(0.0f, 0.65f, 1.0f),
                arrayOf(
                    Color(
                        context.color.red,
                        context.color.green,
                        context.color.blue,
                        (220f * alphaScale).toInt().coerceIn(0, 255)
                    ),
                    Color(
                        context.color.red,
                        context.color.green,
                        context.color.blue,
                        (110f * alphaScale).toInt().coerceIn(0, 255)
                    ),
                    Color(context.color.red, context.color.green, context.color.blue, 0)
                )
            )
            fillOval(2, 2, context.size, context.size)
            val half = context.size * 0.32f
            color = Color(context.color.red, context.color.green, 255, (170f * alphaScale).toInt().coerceIn(0, 255))
            fillPolygon(
                intArrayOf(cx.roundToInt(), (cx + half).roundToInt(), cx.roundToInt(), (cx - half).roundToInt()),
                intArrayOf((cy - half).roundToInt(), cy.roundToInt(), (cy + half).roundToInt(), cy.roundToInt()), 4
            )
        }
    }

    private fun plasmaColor(index: Int, state: TrailFrameState): Color {
        return TrailMath.gradientColor(
            listOf(
                0.0 to Color(32, 18, 112),
                0.2 to Color(96, 36, 196),
                0.45 to Color(44, 186, 255),
                0.72 to Color(120, 255, 182),
                1.0 to Color(240, 255, 245)
            ),
            ((state.speed / 11.0).coerceIn(
                0.0,
                1.0
            ) * 0.78 + ((sin(state.frame * 0.09 + index * 0.65) + 1.0) * 0.5) * 0.22).coerceIn(0.0, 1.0)
        )
    }
}
