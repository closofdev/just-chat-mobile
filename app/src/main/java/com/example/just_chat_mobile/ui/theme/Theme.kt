package com.example.just_chat_mobile.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

// Web hanya punya tema gelap, jadi app juga dikunci ke tema gelap
// supaya tampilannya identik.
private val DarkScheme = darkColorScheme(
    primary = Accent,
    onPrimary = TextPrimary,
    background = Bg,
    onBackground = TextPrimary,
    surface = Card,
    onSurface = TextPrimary,
    outline = Line,
)

@Composable
fun JustchatmobileTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkScheme,
        typography = Typography,
        content = content,
    )
}
