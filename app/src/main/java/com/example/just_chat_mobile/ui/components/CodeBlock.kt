package com.example.just_chat_mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.just_chat_mobile.ui.theme.CodeBtnBg
import com.example.just_chat_mobile.ui.theme.Muted
import com.example.just_chat_mobile.ui.theme.PreBg
import com.example.just_chat_mobile.ui.theme.TextPrimary
import com.example.just_chat_mobile.ui.theme.White06
import com.example.just_chat_mobile.ui.theme.White08
import kotlinx.coroutines.delay

/**
 * Kotak kode: sudut tajam (tanpa radius) sesuai web,
 * label bahasa di kiri atas, tombol salin di kanan atas.
 */
@Composable
fun CodeBlock(
    lang: String,
    code: String,
    modifier: Modifier = Modifier,
) {
    val clipboard = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    LaunchedEffect(copied) {
        if (copied) { delay(1200); copied = false }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(PreBg)
            .border(1.dp, White06)
            .padding(start = 14.dp, end = 14.dp, top = if (lang.isNotEmpty()) 30.dp else 14.dp, bottom = 14.dp)
    ) {
        if (lang.isNotEmpty()) {
            Text(
                text = lang,
                color = Muted,
                fontSize = 11.sp,
                modifier = Modifier.align(Alignment.TopStart).padding(top = 9.dp),
            )
        }
        Text(
            text = code,
            color = TextPrimary,
            fontSize = 13.sp,
            lineHeight = 20.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.horizontalScroll(rememberScrollState()),
        )
        // tombol salin
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 8.dp)
                .size(26.dp)
                .background(CodeBtnBg)
                .border(1.dp, White08)
                .clickable {
                    clipboard.setText(AnnotatedString(code))
                    copied = true
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (copied) Icons.Filled.Check else Icons.Filled.ContentCopy,
                contentDescription = "Salin kode",
                tint = if (copied) TextPrimary else Muted,
                modifier = Modifier.size(13.dp),
            )
        }
    }
}
