package com.example.just_chat_mobile.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.just_chat_mobile.data.ChatMessage
import com.example.just_chat_mobile.ui.markdown.MarkdownParser
import com.example.just_chat_mobile.ui.theme.Accent
import com.example.just_chat_mobile.ui.theme.Dimens
import com.example.just_chat_mobile.ui.theme.Muted
import com.example.just_chat_mobile.util.Time
import kotlinx.coroutines.delay

// Jawaban AI: teks di kiri (tanpa bubble), aksi di bawahnya.
@Composable
fun AiMessage(
    msg: ChatMessage,
    onRegenerate: () -> Unit,
    onToggleLike: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val blocks = remember(msg.content) { MarkdownParser.parse(msg.content) }

    Column(modifier = modifier.fillMaxWidth()) {
        MarkdownBlocks(blocks = blocks, modifier = Modifier.fillMaxWidth())
        AiActions(
            msg = msg,
            onRegenerate = onRegenerate,
            onToggleLike = onToggleLike,
        )
    }
}

@Composable
private fun AiActions(
    msg: ChatMessage,
    onRegenerate: () -> Unit,
    onToggleLike: () -> Unit,
) {
    val clipboard = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    LaunchedEffect(copied) {
        if (copied) { delay(1200); copied = false }
    }

    Row(
        modifier = Modifier.padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        ActionIcon(
            icon = if (copied) Icons.Filled.Check else Icons.Filled.ContentCopy,
            description = "Salin",
            tint = if (copied) com.example.just_chat_mobile.ui.theme.TextPrimary else Muted,
            onClick = {
                clipboard.setText(AnnotatedString(msg.content))
                copied = true
            },
        )
        ActionIcon(
            icon = Icons.Filled.Refresh,
            description = "Buat ulang",
            tint = Muted,
            onClick = onRegenerate,
        )
        ActionIcon(
            icon = if (msg.liked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
            description = "Suka",
            tint = if (msg.liked) Accent else Muted,
            onClick = onToggleLike,
        )
        Text(
            text = Time.fmtResp(msg.time, msg.tps),
            color = Muted,
            fontSize = Dimens.RespTimeFont,
            modifier = Modifier.padding(start = 2.dp),
        )
    }
}

@Composable
private fun ActionIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    tint: Color,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(Dimens.ActionSize)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = tint,
            modifier = Modifier.size(Dimens.ActionIcon),
        )
    }
}
