package net.exoad.cursorvfx.styles

import net.exoad.cursorvfx.*
import java.awt.Color
import java.awt.Graphics2D
import java.awt.RadialGradientPaint
import kotlin.math.roundToInt

object CometStyle : TrailStyleSpec {
    override val id = TrailStyle.COMET
    override val ghostCount = 8
    override val trailSampleGap = 4
    override val trailHistorySize = 84
    override val cursorSmoothing = 0.4
    override val moveThresholdPx = 0.3
    override val idleFadeFrames = 14
    override val fadeInLerp = 0.21
    override val fadeOutLerp = 0.33

    override fun activeLerp(index: Int): Double {
        return (0.37 - index * 0.03).coerceAtLeast(0.12)
    }

    override fun idleLerp(index: Int): Double {
        return (0.26 - index * 0.02).coerceAtLeast(0.08)
    }

    override fun ghostSize(index: Int): Int {
        return (38 - index * 2).coerceAtLeast(8)
    }

    override fun ghostBaseAlpha(index: Int): Float {
        return (1.0f - index * 0.1f).coerceAtLeast(0.2f)
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
            cometColor(index, state)
        )
    }

    override fun paintGhost(g2: Graphics2D, context: GhostPaintContext) {
        val cx = context.width / 2f
        val cy = context.height / 2f
        val radius = context.size / 2f
        val alphaScale = context.alphaScale
        val tailH = (radius * 0.45f).roundToInt().coerceAtLeast(2)
        with(g2) {
            color = Color(
                context.color.red,
                context.color.green,
                context.color.blue,
                (95f * alphaScale).toInt().coerceIn(0, 255)
            )
            fillRoundRect(
                (cx - radius * 1.0f).roundToInt(),
                (cy - radius * 0.22f).roundToInt(),
                (radius * 1.7f).roundToInt().coerceAtLeast(2),
                tailH,
                tailH,
                tailH
            )
            paint = RadialGradientPaint(
                cx,
                cy,
                radius,
                floatArrayOf(0.0f, 0.58f, 1.0f),
                arrayOf(
                    Color(255, 255, 255, (225f * alphaScale).toInt().coerceIn(0, 255)),
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
        }
    }

    private fun cometColor(index: Int, state: TrailFrameState): Color {
        return TrailMath.gradientColor(
            listOf(
                0.0 to Color(30, 36, 66),
                0.28 to Color(82, 106, 155),
                0.55 to Color(201, 214, 228),
                0.78 to Color(255, 229, 174),
                1.0 to Color(255, 170, 86)
            ),
            ((state.speed / 16.0).coerceIn(0.0, 1.0) * 0.7 + (1.0 - (index.toDouble() / (ghostCount - 1))).coerceIn(
                0.0,
                1.0
            ) * 0.3) * (1.0 - state.idleProgress * 0.35)
        )
    }
}
