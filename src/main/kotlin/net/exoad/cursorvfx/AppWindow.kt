package net.exoad.cursorvfx

import java.awt.Color
import java.awt.Dimension
import java.awt.KeyEventDispatcher
import java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager
import java.awt.MouseInfo.getPointerInfo
import java.awt.Point
import java.awt.event.KeyEvent.*
import javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW
import javax.swing.JFrame
import javax.swing.KeyStroke.getKeyStroke
import javax.swing.Timer
import kotlin.math.hypot
import kotlin.system.exitProcess

class AppWindow : JFrame() {
    private var activeStyleId: TrailStyle = TrailStyle.PLASMA
    private var styleSpec: TrailStyleSpec = TrailStyles.byId(activeStyleId)
    private val ghosts = mutableListOf<GhostWindow>()
    private val styleToast = StyleToastWindow()
    private var shiftDown = false
    private var rDown = false
    private var frame = 0
    private var idleFrames = 0
    private var smoothedCursorX = 0.0
    private var smoothedCursorY = 0.0
    private var previousSmoothedCursorX = 0.0
    private var previousSmoothedCursorY = 0.0
    private val trailHistory = TrailHistory(styleSpec.trailHistorySize)
    private var timer: Timer? = null

    private val styleDispatcher = KeyEventDispatcher { event ->
        when (event.id) {
            KEY_PRESSED -> {
                if (event.keyCode == VK_SHIFT) {
                    shiftDown = true
                }
                if (event.keyCode == VK_R) {
                    rDown = true
                }
                if (shiftDown && rDown) {
                    when (event.keyCode) {
                        VK_W -> cycleStyle(-1)
                        VK_E -> cycleStyle(1)
                        VK_Q -> shutdown()
                    }
                }
            }

            KEY_RELEASED -> {
                if (event.keyCode == VK_SHIFT) {
                    shiftDown = false
                }
                if (event.keyCode == VK_R) {
                    rDown = false
                }
            }
        }
        false
    }

    init {
        defaultCloseOperation = EXIT_ON_CLOSE
        isUndecorated = true
        isAlwaysOnTop = false
        background = Color(0, 0, 0, 0)
        size = Dimension(1, 1)
        location = Point(-10, -10)
        isFocusable = true
        opacity = 0.01f
        val startPos = getPointerInfo()?.location ?: Point(0, 0)
        smoothedCursorX = startPos.x.toDouble()
        smoothedCursorY = startPos.y.toDouble()
        previousSmoothedCursorX = smoothedCursorX
        previousSmoothedCursorY = smoothedCursorY
        rebuildGhosts(startPos)
        getCurrentKeyboardFocusManager().addKeyEventDispatcher(styleDispatcher)
        timer = Timer(16) { tickFrame() }.apply {
            isCoalesce = true
            start()
        }
        rootPane.registerKeyboardAction(
            {
                shutdown()
            },
            getKeyStroke(VK_ESCAPE, 0),
            WHEN_IN_FOCUSED_WINDOW
        )
        isVisible = true
    }

    private fun tickFrame() {
        val current = getPointerInfo()?.location ?: return
        previousSmoothedCursorX = smoothedCursorX
        previousSmoothedCursorY = smoothedCursorY
        smoothedCursorX += (current.x - smoothedCursorX) * styleSpec.cursorSmoothing
        smoothedCursorY += (current.y - smoothedCursorY) * styleSpec.cursorSmoothing
        val smoothedSpeed = hypot(
            smoothedCursorX - previousSmoothedCursorX,
            smoothedCursorY - previousSmoothedCursorY
        )
        val moving = smoothedSpeed > styleSpec.moveThresholdPx
        idleFrames = if (moving) {
            0
        } else {
            (idleFrames + 1).coerceAtMost(styleSpec.idleFadeFrames)
        }
        trailHistory.add(smoothedCursorX.toInt(), smoothedCursorY.toInt())
        val frameState = TrailFrameState(
            frame = frame,
            cursorX = smoothedCursorX,
            cursorY = smoothedCursorY,
            speed = smoothedSpeed,
            moving = moving,
            idleProgress = (idleFrames.toDouble() / styleSpec.idleFadeFrames).coerceIn(0.0, 1.0)
        )
        ghosts.forEachIndexed { index, ghost ->
            val target = styleSpec.targetFor(index, frameState, trailHistory)
            with(ghost) {
                targetX = target.x
                targetY = target.y
                targetOpacity = target.opacity
                dotColor = target.color
            }
        }
        frame++
        ghosts.forEachIndexed { index, ghost ->
            ghost.tick(
                lerpFactor = if (moving) styleSpec.activeLerp(index) else styleSpec.idleLerp(index),
                opacityLerp = if (moving) styleSpec.fadeInLerp else styleSpec.fadeOutLerp
            )
        }
    }

    private fun setStyle(style: TrailStyle) {
        if (activeStyleId == style) {
            return
        }
        activeStyleId = style
        styleSpec = TrailStyles.byId(style)
        idleFrames = 0
        trailHistory.resize(styleSpec.trailHistorySize)
        trailHistory.clear()
        rebuildGhosts(Point(smoothedCursorX.toInt(), smoothedCursorY.toInt()))
        styleToast.showStyle(style.title)
    }

    private fun cycleStyle(direction: Int) {
        val styles = TrailStyles.all
        if (styles.isEmpty()) {
            return
        }
        setStyle(styles[(styles.indexOfFirst { it.id == activeStyleId }
                             .let { if (it < 0) 0 else it } + direction).floorMod(styles.size)].id)
    }

    private fun Int.floorMod(mod: Int): Int {
        val result = this % mod
        return if (result < 0) result + mod else result
    }

    private fun shutdown() {
        timer?.stop()
        timer = null
        ghosts.forEach { it.dispose() }
        styleToast.dispose()
        getCurrentKeyboardFocusManager().removeKeyEventDispatcher(styleDispatcher)
        dispose()
        exitProcess(0)
    }

    private fun rebuildGhosts(startPos: Point) {
        ghosts.forEach { it.dispose() }
        ghosts.clear()
        repeat(styleSpec.ghostCount) { idx ->
            val ghost = GhostWindow(idx, styleSpec)
            with(ghost) {
                initAt(startPos.x, startPos.y)
                isVisible = true
                ghosts.add(ghost)
            }
        }
    }
}
