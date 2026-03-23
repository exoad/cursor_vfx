# cursor_vfx

A simple desktop app written in Kotlin that adds a trail to your mouse cursor.

> created this as a toy project in 2 hours, so it will not be maintained, feel free to fork and add features.

## tech stack?

1. Kotlin - just better and more fun java :)
2. Java Swing - simple and built into the jvm, though quite heavy and not the best for performance due to software
   rendering (skiko or something would be better)

## controls

<kbd>Shift</kbd><kbd>R</kbd><kbd>Q</kbd> - quit

<kbd>Shift</kbd><kbd>R</kbd><kbd>W</kbd> - previous trail style

<kbd>Shift</kbd><kbd>R</kbd><kbd>E</kbd> - next trail style

> [!WARNING]
>
> Inherently, the app spawns a bunch of windows to represent the trail, so this can not only be quite heavy,
> but at the same time fuck with the mouse cursor as the windows will absorb any interactions.
>
> For custom styles, make the fade out timing super quick or you can just wait for the trail to go away before clicking.

## custom styling

Take a look under [`src/main/kotlin/net.exoad.cursorvfx/styles/`](./src/main/kotlin/net.exoad.cursorvfx/styles/) for
some examples of custom styles, you can create your own by implementing the `TrailStyleSpec` interface.

You will need to implement the following:

```kotlin
// identifier, create an enum in 'TrailStyle'
val id: TrailStyle

// how many particles to show (spawn max)
val ghostCount: Int

// how many frames to skip between each particle spawn
val trailSampleGap: Int

// how many frames to keep in the history for this style (should be at least ghostCount * trailSampleGap)
val trailHistorySize: Int

// whether to use a more smoothed trail (slower) 
val cursorSmoothing: Double

// the distance in pixels the cursor needs to move before spawning a new particle (prevents spawning too many when the cursor is idle)
val moveThresholdPx: Double

// how long to last before fading away when the cursor stops moving
val idleFadeFrames: Int

// self explanatory for the visibility animation    
val fadeInLerp: Double
val fadeOutLerp: Double

// im too tired to explain these, just look at the bundled ones
fun activeLerp(index: Int): Double
fun idleLerp(index: Int): Double
fun ghostSize(index: Int): Int
fun ghostBaseAlpha(index: Int): Float
fun targetFor(index: Int, state: TrailFrameState, history: TrailHistory): GhostTarget
fun paintGhost(g2: Graphics2D, context: GhostPaintContext)
```
