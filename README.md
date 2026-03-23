<div align="center">
<h1>cursor_vfx</h1>

<em>A simple desktop app written in Kotlin that adds a trail to your mouse cursor.</em>
<video src="https://github.com/user-attachments/assets/202dd914-8fd7-4eb4-821a-e35657a81153" width=400 />

</div>

> created this as a toy project in 2 hours, so it will not be maintained, feel free to fork and add features.

## tech

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

Take a look under [`src/main/kotlin/net.exoad.cursorvfx/styles/`](./src/main/kotlin/net/exoad/cursorvfx/styles/) for
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


## license

```
              GLWTS(Good Luck With That Shit) Public License
            Copyright (c) Every-fucking-one, except the Author

Everyone is permitted to copy, distribute, modify, merge, sell, publish,
sublicense or whatever the fuck they want with this software but at their
OWN RISK.

                             Preamble

The author has absolutely no fucking clue what the code in this project
does. It might just fucking work or not, there is no third option.


                GOOD LUCK WITH THAT SHIT PUBLIC LICENSE
   TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION, AND MODIFICATION

  0. You just DO WHATEVER THE FUCK YOU WANT TO as long as you NEVER LEAVE
A FUCKING TRACE TO TRACK THE AUTHOR of the original product to blame for
or hold responsible.

IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
DEALINGS IN THE SOFTWARE.

Good luck and Godspeed.
```

OR (if you want something less casual)

```
BSD 2-Clause License

Copyright (c) 2026, Jiaming Meng

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
```
