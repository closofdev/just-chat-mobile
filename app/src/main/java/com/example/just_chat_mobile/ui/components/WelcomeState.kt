package com.example.just_chat_mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.just_chat_mobile.ui.theme.Accent
import com.example.just_chat_mobile.ui.theme.Card
import com.example.just_chat_mobile.ui.theme.Line
import com.example.just_chat_mobile.ui.theme.Muted
import com.example.just_chat_mobile.ui.theme.TextPrimary

/**
 * Layar awal saat belum ada pesan — meniru tampilan web:
 * bintang aksen, sapaan, dan chip saran prompt.
 */
@Composable
fun WelcomeState(
    onPickPrompt: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val suggestions = listOf(
        "🎨 Bikin komponen UI ala shadcn/ui",
        "💬 Cara kerja bubble chat kiri-kanan",
        "🧩 Contoh artifact card",
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("✳", color = Accent, fontSize = 34.sp)

        Text(
            text = "Ada yang bisa saya bantu?",
            color = TextPrimary,
            fontSize = 26.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
        )

        Text(
            text = "Mulai dengan mengetik pesan, atau pilih salah satu:",
            color = Muted,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
        )

        suggestions.forEach { label ->
            Text(
                text = label,
                color = TextPrimary,
                fontSize = 13.sp,
                modifier = Modifier
                    .padding(top = 2.dp)
                    .background(Card, RoundedCornerShape(999.dp))
                    .border(1.dp, Line, RoundedCornerShape(999.dp))
                    .clickable { onPickPrompt(label) }
                    .padding(horizontal = 14.dp, vertical = 9.dp),
            )
        }
    }
}
