package com.example.just_chat_mobile.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.just_chat_mobile.ui.theme.PreBg
import com.example.just_chat_mobile.ui.theme.TextPrimary

/**
 * Render rumus LaTeX pakai jlatexmath-android.
 *
 * Ditulis defensif: seluruh proses render dibungkus try-catch, termasuk
 * pembuatan bitmap. Kalau rumus gagal dirender (sintaks tidak didukung atau
 * font gagal dimuat), yang tampil adalah teks sumbernya — bukan crash.
 */
@Composable
fun MathView(
    tex: String,
    display: Boolean,
    modifier: Modifier = Modifier,
) {
    val bitmap = remember(tex, display) { safeRender(tex, display) }

    val target = modifier.then(if (display) Modifier.fillMaxWidth() else Modifier)

    if (bitmap == null) {
        Text(
            text = tex,
            color = TextPrimary,
            fontSize = 13.sp,
            modifier = target,
        )
        return
    }

    val img = remember(bitmap) { bitmap.asImageBitmap() }
    val scroll = rememberScrollState()

    Box(
        modifier = target.then(
            if (display) Modifier.horizontalScroll(scroll).padding(vertical = 6.dp)
            else Modifier
        )
    ) {
        Image(bitmap = img, contentDescription = tex)
    }
}

private fun safeRender(tex: String, display: Boolean): Bitmap? {
    if (tex.isBlank()) return null
    return try {
        val drawable = ru.noties.jlatexmath.JLatexMathDrawable.builder(tex)
            .textSize(if (display) 42f else 34f)
            .color(TextPrimary.toArgb())
            .padding(6)
            .build()

        val w = drawable.intrinsicWidth
        val h = drawable.intrinsicHeight
        if (w <= 0 || h <= 0) return null

        val bmp = Bitmap.createBitmap(w.coerceAtMost(4000), h.coerceAtMost(4000), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        drawable.setBounds(0, 0, w, h)
        drawable.draw(canvas)
        bmp
    } catch (t: Throwable) {
        // rumus tidak didukung / font gagal -> biarkan caller tampilkan teks biasa
        null
    }
}
