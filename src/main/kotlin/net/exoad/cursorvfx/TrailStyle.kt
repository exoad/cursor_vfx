package net.exoad.cursorvfx

import net.exoad.cursorvfx.styles.CometStyle
import net.exoad.cursorvfx.styles.HaloStyle
import net.exoad.cursorvfx.styles.PlasmaStyle
import net.exoad.cursorvfx.styles.ShardStyle
import java.awt.Color
import java.awt.Graphics2D

enum class TrailStyle(val title: String) {
    PLASMA("Plasma"),
    COMET("Comet"),
    SHARD("Shard"),
    HALO("Halo");
}

data class TrailFrameState(
    val frame: Int,
    val cursorX: Double,
    val cursorY: Double,
    val speed: Double,
    val moving: Boolean,
    val idleProgress: Double
)

data class GhostTarget(
    val x: Double,
    val y: Double,
    val opacity: Double,
    val color: Color
)

data class GhostPaintContext(
    val index: Int,
    val size: Int,
    val color: Color,
    val alphaScale: Float,
    val frame: Int,
    val width: Int,
    val height: Int
)

interface TrailStyleSpec {
    val id: TrailStyle
    val ghostCount: Int
    val trailSampleGap: Int
    val trailHistorySize: Int
    val cursorSmoothing: Double
    val moveThresholdPx: Double
    val idleFadeFrames: Int
    val fadeInLerp: Double
    val fadeOutLerp: Double

    fun activeLerp(index: Int): Double
    fun idleLerp(index: Int): Double
    fun ghostSize(index: Int): Int
    fun ghostBaseAlpha(index: Int): Float
    fun targetFor(index: Int, state: TrailFrameState, history: TrailHistory): GhostTarget
    fun paintGhost(g2: Graphics2D, context: GhostPaintContext)
}

class TrailHistory(capacity: Int) {
    private var xs = IntArray(capacity.coerceAtLeast(1))
    private var ys = IntArray(capacity.coerceAtLeast(1))
    private var head = 0
    var size: Int = 0
        private set

    fun clear() {
        head = 0
        size = 0
    }

    fun resize(newCapacity: Int) {
        val target = newCapacity.coerceAtLeast(1)
        if (target == xs.size) {
            clear()
            return
        }
        xs = IntArray(target)
        ys = IntArray(target)
        clear()
    }

    fun add(x: Int, y: Int) {
        head = (head - 1).floorMod(xs.size)
        xs[head] = x
        ys[head] = y
        if (size < xs.size) {
            size++
        }
    }

    fun xAt(index: Int): Int {
        return xs[(head + index).floorMod(xs.size)]
    }

    fun yAt(index: Int): Int {
        return ys[(head + index).floorMod(ys.size)]
    }

    private fun Int.floorMod(mod: Int): Int {
        val result = this % mod
        return if (result < 0) result + mod else result
    }
}

object TrailStyles {
    val all = listOf(PlasmaStyle, CometStyle, ShardStyle, HaloStyle)
    val default: TrailStyleSpec = PlasmaStyle

    fun byId(id: TrailStyle): TrailStyleSpec {
        return all.firstOrNull { it.id == id } ?: default
    }
}

object TrailMath {
    fun lerp(start: Double, end: Double, t: Double): Double {
        return start + (end - start) * t
    }

    fun easeOutCubic(t: Double): Double {
        val inv = 1.0 - t.coerceIn(0.0, 1.0)
        return 1.0 - (inv * inv * inv)
    }

    fun gradientColor(stops: List<Pair<Double, Color>>, ratio: Double): Color {
        val clamped = ratio.coerceIn(0.0, 1.0)
        for (idx in 0 until stops.lastIndex) {
            val (startT, startColor) = stops[idx]
            val (endT, endColor) = stops[idx + 1]
            if (clamped <= endT) {
                val localT = ((clamped - startT) / (endT - startT)).coerceIn(0.0, 1.0)
                return Color(
                    lerp(startColor.red.toDouble(), endColor.red.toDouble(), localT).toInt(),
                    lerp(startColor.green.toDouble(), endColor.green.toDouble(), localT).toInt(),
                    lerp(startColor.blue.toDouble(), endColor.blue.toDouble(), localT).toInt()
                )
            }
        }
        return stops.last().second
    }
}
