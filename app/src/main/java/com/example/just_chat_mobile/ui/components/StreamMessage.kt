package com.example.just_chat_mobile.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.just_chat_mobile.ui.markdown.MarkdownParser
import com.example.just_chat_mobile.ui.markdown.MdInline
import com.example.just_chat_mobile.ui.theme.Accent
import com.example.just_chat_mobile.ui.theme.Dimens
import com.example.just_chat_mobile.ui.theme.TextPrimary

/**
 * Jawaban AI yang sedang mengalir.
 *
 * Web memberi animasi fade pada kata terakhir tiap kali ada token baru.
 * Di Compose, menempelkan animasi per-kata pada teks panjang akan memicu
 * re-layout terus-menerus dan terasa tersendat. Pendekatannya diganti:
 * seluruh blok masuk dengan fade halus, dan kursor berkedip di akhir teks.
 */
@Composable
fun StreamMessage(
    text: String,
    modifier: Modifier = Modifier,
) {
    val blocks = remember(text) { MarkdownParser.parse(text) }
    val fade = remember { Animatable(0.65f) }

    LaunchedEffect(text) {
        fade.snapTo(0.72f)
        fade.animateTo(1f, animationSpec = tween(durationMillis = 180, easing = LinearEasing))
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Box(modifier = Modifier.alpha(fade.value)) {
            MarkdownBlocks(blocks = blocks, modifier = Modifier.fillMaxWidth())
        }
        Row(verticalAlignment = Alignment.Bottom) {
            StreamingCaret()
        }
    }
}

@Composable
private fun StreamingCaret() {
    val transition = rememberInfiniteTransition(label = "caret")
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "caretAlpha",
    )
    Box(
        modifier = Modifier
            .padding(top = 4.dp)
            .width(8.dp)
            .height(15.dp)
            .alpha(alpha)
            .background(Accent)
    )
}
