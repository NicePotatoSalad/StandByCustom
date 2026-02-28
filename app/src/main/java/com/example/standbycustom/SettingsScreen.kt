package com.example.standbycustom

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Top-left to bottom-right diagonal
private val DarkGradientBackground = Brush.linearGradient(
    colors = listOf(
        Color(0xFF252424),
        Color(0xFF151511)
    ),
    start = Offset.Zero,
    end = Offset(3000f, 3000f)
)

private val LightGradientBackground = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFE8E8E8),
        Color(0xFFF5F5F5),
        Color(0xFFFFFFFF)
    )
)

/** Glassy surface: selected has stronger glow and border. Dark or light variant. */
@Composable
private fun GlassyOptionChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    effectiveDark: Boolean,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, borderColor, textColor) = if (effectiveDark) {
        Triple(
            if (selected) Color.White.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.08f),
            if (selected) Color.White.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.18f),
            if (selected) Color.White else Color.White.copy(alpha = 0.85f)
        )
    } else {
        Triple(
            if (selected) Color.Black.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.06f),
            if (selected) Color.Black.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.18f),
            if (selected) Color(0xFF1C1B1F) else Color(0xFF1C1B1F).copy(alpha = 0.85f)
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 40.dp, vertical = 12.dp)
            .semantics {
                contentDescription = "$label theme option"
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}

/**
 * Theme switcher row: "Theme" on the left, three glassy options (Auto, Light, Dark) aligned right.
 * Uses hoisted themeOption and onThemeChange.
 */
@Composable
private fun ThemeSwitcherRow(
    themeOption: ThemeOption,
    onThemeChange: (ThemeOption) -> Unit,
    effectiveDark: Boolean
) {
    val themeOptions = listOf(
        ThemeOption.Auto to "Auto",
        ThemeOption.Light to "Light",
        ThemeOption.Dark to "Dark"
    )
    val labelColor = if (effectiveDark) Color.White.copy(alpha = 0.9f) else Color(0xFF1C1B1F)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Theme",
            color = labelColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.weight(1f))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            themeOptions.forEach { (option, label) ->
                GlassyOptionChip(
                    label = label,
                    selected = themeOption == option,
                    onClick = { onThemeChange(option) },
                    effectiveDark = effectiveDark
                )
            }
        }
    }
}

/**
 * Settings screen shown as an overlay.
 * Gradient background (dark or light) with glassy theme switcher; back closes.
 */
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    themeOption: ThemeOption,
    effectiveDark: Boolean,
    onThemeChange: (ThemeOption) -> Unit
) {
    BackHandler(onBack = onBack)

    val background = if (effectiveDark) DarkGradientBackground else LightGradientBackground
    val iconTint = if (effectiveDark) Color.White else Color(0xFF1C1B1F)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp),
            colors = if (effectiveDark) {
                IconButtonDefaults.iconButtonColors()
            } else {
                IconButtonDefaults.iconButtonColors(containerColor = Color.Transparent)
            }
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_back),
                contentDescription = "Back",
                modifier = Modifier.size(24.dp),
                tint = iconTint
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 20.dp, top = 56.dp, end = 20.dp)
        ) {
            ThemeSwitcherRow(
                themeOption = themeOption,
                onThemeChange = onThemeChange,
                effectiveDark = effectiveDark
            )
        }
    }
}
