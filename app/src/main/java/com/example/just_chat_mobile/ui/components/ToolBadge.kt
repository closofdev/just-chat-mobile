package com.example.just_chat_mobile.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.just_chat_mobile.data.ToolInfo
import com.example.just_chat_mobile.ui.theme.Accent
import com.example.just_chat_mobile.ui.theme.Bubble
import com.example.just_chat_mobile.ui.theme.Line
import com.example.just_chat_mobile.ui.theme.Muted
import com.example.just_chat_mobile.ui.theme.TextPrimary

// Badge tool yang sedang dijalankan agent (muncul saat berjalan, hilang setelah selesai).
@Composable
fun ToolBadge(
    tool: ToolInfo?,
    modifier: Modifier = Modifier,
) {
    if (tool == null) return
    val isError = tool.name == "error"

    val transition = rememberInfiniteTransition(label = "tool")
    val spin by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
        ),
        label = "spin",
    )

    Row(
        modifier = modifier
            .background(Bubble, RoundedCornerShape(10.dp))
            .border(1.dp, Line, RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = when (tool.name) {
                "websearch" -> Icons.Filled.Public
                "webfetch" -> Icons.Filled.Description
                "question" -> Icons.Filled.HelpOutline
                "error" -> Icons.Filled.ErrorOutline
                else -> Icons.Filled.Circle
            },
            contentDescription = tool.name,
            tint = if (isError) Accent else Muted,
            modifier = Modifier.size(14.dp).then(if (!isError) Modifier.rotate(spin) else Modifier),
        )
        Text(
            text = tool.label.ifEmpty { tool.name },
            color = if (isError) Accent else TextPrimary,
            fontSize = 12.5.sp,
        )
    }
}
