package net.exoad.cursorvfx

import java.awt.*
import java.awt.RenderingHints.*
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.Timer

class StyleToastWindow : JFrame() {
    private var text: String = ""
    private var alpha: Float = 0f
    private var targetAlpha: Float = 0f
    private val fadeTimer = Timer(16) { evt ->
        alpha += (targetAlpha - alpha) * if (targetAlpha > alpha) 0.26f else 0.2f
        if (alpha < 0.02f && targetAlpha == 0f) {
            alpha = 0f
            isVisible = false
            repaint()
            (evt.source as Timer).stop()
        } else {
            repaint()
        }
    }.apply {
        isCoalesce = true
    }
    private val hideTimer = Timer(1300) {
        targetAlpha = 0f
    }.apply {
        isRepeats = false
    }
    private val titleFont = Font("Segoe UI", Font.BOLD, 42)

    init {
        isUndecorated = true
        isAlwaysOnTop = true
        background = Color(0, 0, 0, 0)
        size = Dimension(720, 160)
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
                val g2 = g.create() as Graphics2D
                with(g2) {
                    setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON)
                    setRenderingHint(KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_ON)
                    color = Color(0, 0, 0, (alpha * 170f).toInt().coerceIn(0, 255))
                    fillRoundRect(40, 32, width - 80, height - 64, 28, 28)
                    g2.font = titleFont
                    val fm: FontMetrics = fontMetrics
                    val x = (width - fm.stringWidth(text)) / 2
                    val y = (height + fm.ascent) / 2 - 8
                    color = Color(84, 184, 255, (alpha * 120f).toInt().coerceIn(0, 255))
                    drawString(text, x + 1, y + 1)
                    color = Color(255, 255, 255, (alpha * 255f).toInt().coerceIn(0, 255))
                    drawString(text, x, y)
                    dispose()
                }
            }
        }
    }

    fun showStyle(title: String) {
        text = title
        placeCentered()
        targetAlpha = 1f
        if (!isVisible) {
            isVisible = true
        }
        hideTimer.restart()
        if (!fadeTimer.isRunning) {
            fadeTimer.start()
        }
    }

    private fun placeCentered() {
        val screen = GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice.defaultConfiguration.bounds
        location = Point(
            screen.x + (screen.width - width) / 2,
            screen.y + (screen.height - height) / 2
        )
    }

    override fun dispose() {
        hideTimer.stop()
        fadeTimer.stop()
        super.dispose()
    }
}
