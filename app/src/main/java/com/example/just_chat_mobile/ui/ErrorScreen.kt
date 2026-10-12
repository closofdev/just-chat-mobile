package com.example.just_chat_mobile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.just_chat_mobile.ui.theme.Accent
import com.example.just_chat_mobile.ui.theme.Bg
import com.example.just_chat_mobile.ui.theme.TextPrimary

/** Ditampilkan kalau komposisi utama gagal — memudahkan melihat penyebab. */
@Composable
fun ErrorScreen(title: String, detail: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
    ) {
        Text("Aplikasi gagal dimuat", color = Accent, fontSize = 18.sp)
        Text(
            text = title,
            color = TextPrimary,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 10.dp),
        )
        Text(
            text = detail,
            color = TextPrimary,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(top = 14.dp),
        )
    }
}
