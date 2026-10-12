package com.example.just_chat_mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.just_chat_mobile.ui.theme.Accent
import com.example.just_chat_mobile.ui.theme.Bubble
import com.example.just_chat_mobile.ui.theme.Card
import com.example.just_chat_mobile.ui.theme.Dimens
import com.example.just_chat_mobile.ui.theme.Line
import com.example.just_chat_mobile.ui.theme.Muted
import com.example.just_chat_mobile.ui.theme.White07

/**
 * Input bar — menempel di bawah, tidak ikut tergulir.
 * Tombol kiri: lampiran. Tombol kanan: kirim / berhenti.
 */
@Composable
fun Composer(
    isStreaming: Boolean,
    disabled: Boolean,
    onSend: (String) -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var value by remember { mutableStateOf("") }
    var attachName by remember { mutableStateOf<String?>(null) }

    val canSend = isStreaming || value.isNotBlank() || attachName != null

    Column(modifier = modifier.fillMaxWidth()) {
        // chip lampiran
        attachName?.let { name ->
            Row(modifier = Modifier.padding(bottom = 8.dp)) {
                Row(
                    modifier = Modifier
                        .background(Bubble, RoundedCornerShape(10.dp))
                        .border(1.dp, Line, RoundedCornerShape(10.dp))
                        .padding(start = 10.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(name, color = com.example.just_chat_mobile.ui.theme.TextPrimary, fontSize = 12.5.sp)
                    Box(
                        modifier = Modifier.size(24.dp).clickable { attachName = null },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Hapus lampiran", tint = Muted, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Card, RoundedCornerShape(Dimens.ComposerRadius))
                .border(1.dp, White07, RoundedCornerShape(Dimens.ComposerRadius))
                .padding(
                    start = Dimens.ComposerPadStart,
                    end = Dimens.ComposerPadEnd,
                    top = Dimens.ComposerPadV,
                    bottom = Dimens.ComposerPadV,
                ),
            verticalAlignment = Alignment.Bottom,
        ) {
            // tombol lampiran
            Box(
                modifier = Modifier
                    .size(Dimens.PlusBtnSize)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { /* pemilih file ditambahkan menyusul */ },
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Lampirkan", tint = com.example.just_chat_mobile.ui.theme.TextPrimary, modifier = Modifier.size(19.dp))
            }

            // input teks
            Box(modifier = Modifier.weight(1f).padding(horizontal = 4.dp)) {
                if (value.isEmpty()) {
                    Text(
                        text = if (disabled) "Jawab pertanyaan di atas..." else "Tulis pesan...",
                        color = Muted,
                        fontSize = Dimens.InputFontSize,
                        modifier = Modifier.padding(vertical = 9.dp),
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = { value = it },
                    enabled = !disabled,
                    textStyle = TextStyle(
                        color = com.example.just_chat_mobile.ui.theme.TextPrimary,
                        fontSize = Dimens.InputFontSize,
                        lineHeight = 22.sp,
                    ),
                    cursorBrush = SolidColor(Accent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = Dimens.InputMaxHeight)
                        .padding(vertical = 9.dp),
                )
            }

            // tombol kirim / berhenti
            Box(
                modifier = Modifier
                    .size(Dimens.SendBtnSize)
                    .clip(CircleShape)
                    .background(if (canSend) Accent else Accent.copy(alpha = 0.32f))
                    .clickable(enabled = canSend) {
                        if (isStreaming) {
                            onStop()
                        } else {
                            var text = value.trim()
                            if (attachName != null) {
                                text += if (text.isEmpty()) "[lampiran: $attachName]" else "\n\n[lampiran: $attachName]"
                            }
                            if (text.isNotEmpty()) {
                                onSend(text)
                                value = ""
                                attachName = null
                            }
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (isStreaming) Icons.Filled.Stop else Icons.Filled.ArrowUpward,
                    contentDescription = if (isStreaming) "Berhenti" else "Kirim",
                    tint = com.example.just_chat_mobile.ui.theme.TextPrimary,
                    modifier = Modifier.size(if (isStreaming) 14.dp else 18.dp),
                )
            }
        }
    }
}
