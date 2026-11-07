package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.PlaybackStatus
import com.example.ui.theme.DarkPrimary
import com.example.ui.theme.WaveformAmber
import com.example.ui.theme.WaveformGold
import com.example.ui.theme.WaveformPurple
import com.example.ui.theme.WaveformViolet
import com.example.ui.theme.WaveformYellow
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * Dynamic Audio Visualizer rendered using Jetpack Compose Canvas.
 *
 * Responds to each state of the Media3 ExoPlayer instance:
 *  - [PlaybackStatus.PLAYING]: Rich multi-frequency harmonic waveform animation with peak indicators,
 *    subtle glow floor, energetic bar fluctuations, and phase-shifted frequency modulation.
 *  - [PlaybackStatus.BUFFERING]: Pulsing rhythmic orbital loader wave signaling network buffering.
 *  - [PlaybackStatus.PAUSED]: Smooth damped settle animation, transitioning into a gentle resting wave.
 *  - [PlaybackStatus.IDLE] / [PlaybackStatus.ERROR]: Low-amplitude standby bars with gentle breathing.
 */
@Composable
fun AudioVisualizer(
    status: PlaybackStatus,
    modifier: Modifier = Modifier,
    barCount: Int = 38,
    height: Dp = 56.dp
) {
    val isPlaying = status == PlaybackStatus.PLAYING
    val isBuffering = status == PlaybackStatus.BUFFERING
    val isPaused = status == PlaybackStatus.PAUSED
    val isError = status == PlaybackStatus.ERROR

    // Animated transition between playing intensity and resting state
    val energyMultiplier = remember { Animatable(if (isPlaying) 1f else 0.15f) }
    LaunchedEffect(status) {
        val targetEnergy = when (status) {
            PlaybackStatus.PLAYING -> 1.0f
            PlaybackStatus.BUFFERING -> 0.45f
            PlaybackStatus.PAUSED -> 0.12f
            PlaybackStatus.IDLE -> 0.08f
            PlaybackStatus.ERROR -> 0.05f
        }
        energyMultiplier.animateTo(
            targetValue = targetEnergy,
            animationSpec = tween(
                durationMillis = if (status == PlaybackStatus.PLAYING) 450 else 700,
                easing = FastOutSlowInEasing
            )
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "audio_visualizer_engine")

    // Continuous dynamic wave phase
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when {
                    isPlaying -> 1400
                    isBuffering -> 900
                    else -> 3200
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase_loop"
    )

    // Secondary harmonic phase for multi-layered sound spectrum simulation
    val harmonicPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (4 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when {
                    isPlaying -> 2100
                    isBuffering -> 1400
                    else -> 4800
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "harmonic_phase_loop"
    )

    // Rhythmic pulse for buffering or playing beat modulation
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isBuffering) 450 else 800,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_beat"
    )

    // Particle/sparkle shimmer for peak tops
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer_alpha"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .testTag("audio_visualizer_canvas")
    ) {
        val totalWidth = size.width
        val maxHeight = size.height
        val currentEnergy = energyMultiplier.value

        if (totalWidth <= 0f || maxHeight <= 0f) return@Canvas

        val barWidth = (totalWidth / (barCount * 1.55f)).coerceIn(2.5f, 9.5f)
        val totalBarsWidth = barCount * barWidth
        val spacing = ((totalWidth - totalBarsWidth) / (barCount - 1).coerceAtLeast(1)).coerceAtLeast(1f)

        // Gradient based on brand palette: Electric Violet -> Lavender -> Warm Amber
        val barGradient = Brush.verticalGradient(
            colors = when {
                isError -> listOf(
                    Color(0xFFEF4444),
                    Color(0xFFB91C1C)
                )
                isBuffering -> listOf(
                    WaveformViolet,
                    DarkPrimary,
                    Color(0xFF4C1D95)
                )
                isPlaying -> listOf(
                    Color(0xFFE9D5FF), // Bright lavender highlight at peaks
                    WaveformViolet,
                    DarkPrimary,
                    Color(0xFF3B1E78)
                )
                else -> listOf(
                    WaveformViolet.copy(alpha = 0.6f),
                    DarkPrimary.copy(alpha = 0.3f)
                )
            },
            startY = 0f,
            endY = maxHeight
        )

        // Draw dynamic baseline glowing ribbon when playing
        if (isPlaying) {
            val ribbonPath = Path()
            val centerY = maxHeight / 2f
            for (i in 0 until barCount) {
                val nx = i.toFloat() / (barCount - 1).coerceAtLeast(1)
                val wave = sin(phase * 1.2f + nx * 5f) * (maxHeight * 0.12f * currentEnergy)
                val px = i * (barWidth + spacing) + barWidth / 2f
                val py = centerY + wave
                if (i == 0) ribbonPath.moveTo(px, py) else ribbonPath.lineTo(px, py)
            }
            drawPath(
                path = ribbonPath,
                color = DarkPrimary.copy(alpha = 0.22f * currentEnergy),
                style = Stroke(width = 2.dp.toPx())
            )
        }

        for (i in 0 until barCount) {
            val normalizedIndex = i.toFloat() / barCount.toFloat()

            // Calculate dynamic height tailored to each ExoPlayer playback state
            val barHeight = when {
                isPlaying -> {
                    // Multi-octave harmonic synthesis mimicking live audio frequency spectrum
                    // Lower bars have deep bass resonance, middle has speech/vocal, high has treble
                    val bassWave = sin(phase * 1.1f + normalizedIndex * 2.8f)
                    val midWave = cos(harmonicPhase * 0.9f + normalizedIndex * 7.5f)
                    val trebleWave = sin((phase * 2.3f) + (normalizedIndex * 13f))
                    val beatEnvelope = 0.75f + (0.25f * pulse)

                    // Acoustic bell curve: center frequencies are typically more dominant
                    val bellCurve = sin(normalizedIndex * PI.toFloat()).coerceAtLeast(0.35f)

                    val composite = (bassWave * 0.45f + midWave * 0.35f + trebleWave * 0.20f)
                    val normalizedWave = ((composite + 1f) / 2f).coerceIn(0f, 1f)

                    val dynamicH = normalizedWave * bellCurve * beatEnvelope * maxHeight * currentEnergy
                    dynamicH.coerceIn(maxHeight * 0.10f, maxHeight * 0.96f)
                }
                isBuffering -> {
                    // Rhythmic progressive wave traveling across the canvas
                    val wave = (sin(phase * 2.0f - normalizedIndex * 6f) + 1f) / 2f
                    val sweep = wave * pulse * maxHeight * 0.55f * currentEnergy
                    sweep.coerceIn(maxHeight * 0.08f, maxHeight * 0.65f)
                }
                isPaused -> {
                    // Gentle breathing waveform at rest
                    val gentle = (sin(phase * 0.5f + normalizedIndex * 3f) + 1f) / 2f
                    (maxHeight * 0.08f + gentle * maxHeight * 0.08f * currentEnergy)
                        .coerceIn(maxHeight * 0.06f, maxHeight * 0.22f)
                }
                else -> {
                    // Minimal standby indicator
                    maxHeight * 0.07f
                }
            }

            val x = i * (barWidth + spacing)
            val y = (maxHeight - barHeight) / 2f
            val corner = CornerRadius(barWidth / 2f, barWidth / 2f)

            // Draw primary rounded audio bar
            drawRoundRect(
                brush = barGradient,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = corner
            )

            // Dynamic peak cap dot for playing state
            if (isPlaying && barHeight > maxHeight * 0.35f) {
                val peakY = (y - 3.dp.toPx()).coerceAtLeast(1.dp.toPx())
                val capHeight = 2.dp.toPx()
                drawRoundRect(
                    color = Color.White.copy(alpha = shimmerAlpha * (barHeight / maxHeight)),
                    topLeft = Offset(x, peakY),
                    size = Size(barWidth, capHeight),
                    cornerRadius = CornerRadius(capHeight / 2f, capHeight / 2f)
                )
            }
        }
    }
}
