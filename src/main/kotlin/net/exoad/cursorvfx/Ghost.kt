package net.exoad.cursorvfx

import java.awt.*
import java.awt.RenderingHints.KEY_ANTIALIASING
import java.awt.RenderingHints.VALUE_ANTIALIAS_ON
import javax.swing.JFrame
import javax.swing.JPanel
import kotlin.math.roundToInt

class GhostWindow(
    private val index: Int,
    private val styleSpec: TrailStyleSpec
) : JFrame() {
    var targetX: Double = 0.0
    var targetY: Double = 0.0
    private var currentX: Double = 0.0
    private var currentY: Double = 0.0
    private val ghostSize: Int = styleSpec.ghostSize(index)
    private val baseAlpha: Float = styleSpec.ghostBaseAlpha(index)
    private var opacityFactor: Double = 0.0
    var targetOpacity: Double = 0.0
    var dotColor: Color = Color.WHITE
    var frameCounter: Int = 0
    private var lastRenderX: Int = Int.MIN_VALUE
    private var lastRenderY: Int = Int.MIN_VALUE
    private var lastColor: Color = dotColor
    private var lastOpacity = -1.0
    private var lastFrame = -1

    init {
        isUndecorated = true
        isAlwaysOnTop = true
        background = Color(0, 0, 0, 0)
        size = Dimension(ghostSize + 4, ghostSize + 4)
        isFocusable = false
        focusableWindowState = false
        isEnabled = false
        type = Type.UTILITY
        contentPane = object : JPanel() {
            init {
                isOpaque = false
            }

            override fun paintComponent(g: Graphics) {
                super.paintComponent(g)
                with(g.create() as Graphics2D) {
                    setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON)
                    val context = GhostPaintContext(
                        index = index,
                        size = ghostSize,
                        color = dotColor,
                        alphaScale = (baseAlpha * opacityFactor.toFloat()).coerceIn(0f, 1f),
                        frame = frameCounter,
                        width = width,
                        height = height
                    )
                    styleSpec.paintGhost(this, context)
                    dispose()
                }
            }
        }
    }

    fun tick(lerpFactor: Double = 0.18, opacityLerp: Double = 0.2) {
        currentX += (targetX - currentX) * lerpFactor
        currentY += (targetY - currentY) * lerpFactor
        opacityFactor += (targetOpacity - opacityFactor) * opacityLerp
        frameCounter++
        val renderX = (currentX - ghostSize / 2.0).roundToInt()
        val renderY = (currentY - ghostSize / 2.0).roundToInt()
        if (renderX != lastRenderX || renderY != lastRenderY) {
            location = Point(renderX, renderY)
            lastRenderX = renderX
            lastRenderY = renderY
        }
        val needsPaint =
            dotColor != lastColor ||
                kotlin.math.abs(opacityFactor - lastOpacity) > 0.01 ||
                frameCounter - lastFrame >= 2
        if (needsPaint) {
            repaint()
            lastColor = dotColor
            lastOpacity = opacityFactor
            lastFrame = frameCounter
        }
    }

    fun initAt(x: Int, y: Int) {
        currentX = x.toDouble()
        currentY = y.toDouble()
        targetX = x.toDouble()
        targetY = y.toDouble()
        location = Point(x - ghostSize / 2, y - ghostSize / 2)
    }
}
