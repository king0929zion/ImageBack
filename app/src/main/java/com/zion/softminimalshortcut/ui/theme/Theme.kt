package com.zion.softminimalshortcut.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Bg = Color(0xFFEAD6CC)
val BgSoft = Color(0xFFF0DED6)
val Card = Color(0xFFFFFDFB)
val CardSoft = Color(0xFFF8F6FA)

val TextPrimary = Color(0xFF151515)
val TextSecondary = Color(0xFF3A3432)
val TextMuted = Color(0xFF9A9292)
val TextPlaceholder = Color(0xFFB8AEAA)

val BlueSoft = Color(0xFFAEB8F3)
val YellowSoft = Color(0xFFEEDD75)
val GreenSoft = Color(0xFFC8E1DD)
val PinkSoft = Color(0xFFEBDCD7)
val OrangeSoft = Color(0xFFE9907D)
val Sage = Color(0xFF8E9B6B)
val PurpleSoft = Color(0xFFA2AADF)

private val LightColors = lightColorScheme(
    background = Bg,
    surface = Card,
    primary = TextPrimary,
    secondary = TextSecondary,
    tertiary = PurpleSoft,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

private val DarkColors = darkColorScheme(
    background = Bg,
    surface = Card,
    primary = TextPrimary,
    secondary = TextSecondary,
    tertiary = PurpleSoft,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun SoftMinimalShortcutTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content
    )
}
