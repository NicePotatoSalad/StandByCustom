package com.example.standbycustom

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.example.standbycustom.R
import com.example.standbycustom.ui.theme.StandByDayText
import com.example.standbycustom.ui.theme.StandByNightText
import kotlinx.coroutines.delay
import java.util.Calendar

private const val GRID_COLS = 3
private const val GRID_ROWS = 3
private const val GRID_CELLS = GRID_COLS * GRID_ROWS
private const val TICK_MS = 1000L
private const val JITTER_INTERVAL_MS = 60_000L
private const val MACRO_INTERVAL_MS = 300_000L
private const val CROSS_FADE_DURATION_MS = 1500

/** Inset from screen edges so the clock stays away from borders (for large font). */
private val CLOCK_GRID_MARGIN_DP = 56.dp

/** Clock font weight; selectable in code. Default is [Bold]. */
enum class ClockFontWeight {
    Thin,
    Regular,
    Bold,
    ExtraBold
}

private val InterThin = FontFamily(Font(R.font.inter_thin))
private val InterRegular = FontFamily(Font(R.font.inter_regular))
private val InterBold = FontFamily(Font(R.font.inter_bold))
private val InterExtraBold = FontFamily(Font(R.font.inter_extra_bold))

private fun ClockFontWeight.toFontFamily(): FontFamily = when (this) {
    ClockFontWeight.Thin -> InterThin
    ClockFontWeight.Regular -> InterRegular
    ClockFontWeight.Bold -> InterBold
    ClockFontWeight.ExtraBold -> InterExtraBold
}

@Composable
fun StandByScreen(
    onEnterImmersive: () -> Unit
) {
    onEnterImmersive()

    var clockTime by remember { mutableStateOf(formatTime()) }
    val cellIndexState = remember { mutableIntStateOf(0) }
    var microJitter by remember { mutableStateOf(IntOffset.Zero) }
    var isDayMode by remember { mutableStateOf(true) }
    var clockFontWeight by remember { mutableStateOf(ClockFontWeight.Bold) }
    val fadeAlpha = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        var elapsedForJitter = 0L
        while (true) {
            delay(TICK_MS)
            clockTime = formatTime()
            elapsedForJitter += TICK_MS
            if (elapsedForJitter >= JITTER_INTERVAL_MS) {
                elapsedForJitter = 0
                microJitter = randomJitter()
            }
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(MACRO_INTERVAL_MS)
            fadeAlpha.animateTo(0f, animationSpec = tween(CROSS_FADE_DURATION_MS))
            cellIndexState.intValue = (cellIndexState.intValue + 1) % GRID_CELLS
            microJitter = randomJitter()
            fadeAlpha.animateTo(1f, animationSpec = tween(CROSS_FADE_DURATION_MS))
        }
    }

    val textColor = if (isDayMode) StandByDayText else StandByNightText

    var boxSize by remember { mutableStateOf(IntSize.Zero) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .onSizeChanged { boxSize = it }
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val w = boxSize.width
            val h = boxSize.height
            val density = LocalDensity.current
            if (w > 0 && h > 0) {
                val marginPx = with(density) { CLOCK_GRID_MARGIN_DP.toPx() }.toInt()
                val insetW = (w - 2 * marginPx).coerceAtLeast(1)
                val insetH = (h - 2 * marginPx).coerceAtLeast(1)
                val cellWidthPx = insetW / GRID_COLS
                val cellHeightPx = insetH / GRID_ROWS
                val row = cellIndexState.intValue / GRID_COLS
                val col = cellIndexState.intValue % GRID_COLS
                val cellCenterX = marginPx + col * cellWidthPx + cellWidthPx / 2
                val cellCenterY = marginPx + row * cellHeightPx + cellHeightPx / 2
                val offsetPx = IntOffset(
                    (cellCenterX - w / 2 + microJitter.x),
                    (cellCenterY - h / 2 + microJitter.y)
                )
                StandByClock(
                    time = clockTime,
                    textColor = textColor,
                    clockFontWeight = clockFontWeight,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset { offsetPx }
                        .graphicsLayer(alpha = fadeAlpha.value)
                )
            }
        }

        FloatingActionButton(
            onClick = { isDayMode = !isDayMode },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 30.dp, bottom = 30.dp)
                .size(48.dp),
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            contentColor = if (isDayMode) StandByDayText else StandByNightText
        ) {
            Text(
                text = if (isDayMode) "D" else "N",
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun StandByClock(
    time: String,
    textColor: Color,
    clockFontWeight: ClockFontWeight,
    modifier: Modifier = Modifier
) {
    Text(
        text = time,
        style = MaterialTheme.typography.displayLarge.copy(
            fontFamily = clockFontWeight.toFontFamily()
        ),
        color = textColor,
        modifier = modifier,
        fontSize = 120.sp
    )
}

private fun formatTime(): String {
    val cal = Calendar.getInstance()
    val hour = cal.get(Calendar.HOUR_OF_DAY)
    val minute = cal.get(Calendar.MINUTE)
    return "%02d:%02d".format(hour, minute)
}

private fun randomJitter(): IntOffset {
    val jitterRange = (-2..2).filter { it != 0 }
    return IntOffset(
        jitterRange.random(),
        jitterRange.random()
    )
}
