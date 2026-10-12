package com.example.just_chat_mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.just_chat_mobile.data.ChatMessage
import com.example.just_chat_mobile.ui.theme.Bubble
import com.example.just_chat_mobile.ui.theme.Muted
import com.example.just_chat_mobile.ui.theme.TextPrimary
import com.example.just_chat_mobile.ui.theme.White06
import com.example.just_chat_mobile.util.Time

// Bubble user di kanan, radius 20/20/7/20 sesuai web.
@Composable
fun UserBubble(
    msg: ChatMessage,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.End,
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .background(Bubble, RoundedCornerShape(20.dp, 20.dp, 7.dp, 20.dp))
                .border(1.dp, White06, RoundedCornerShape(20.dp, 20.dp, 7.dp, 20.dp))
                .padding(horizontal = 16.dp, vertical = 11.dp)
        ) {
            Text(
                text = msg.content,
                color = TextPrimary,
                fontSize = 15.sp,
                lineHeight = 22.sp,
            )
        }
        Text(
            text = Time.timeStr(msg.time),
            color = Muted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.padding(top = 5.dp),
        )
    }
}
