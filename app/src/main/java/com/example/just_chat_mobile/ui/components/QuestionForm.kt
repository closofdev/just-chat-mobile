package com.example.just_chat_mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.just_chat_mobile.data.PendingQuestion
import com.example.just_chat_mobile.ui.theme.Accent
import com.example.just_chat_mobile.ui.theme.Bubble
import com.example.just_chat_mobile.ui.theme.Card
import com.example.just_chat_mobile.ui.theme.Line
import com.example.just_chat_mobile.ui.theme.Muted
import com.example.just_chat_mobile.ui.theme.TextPrimary
import com.example.just_chat_mobile.ui.theme.White06
import com.example.just_chat_mobile.ui.theme.White07

// Form pertanyaan dari agent (tool "question").
@Composable
fun QuestionForm(
    pending: PendingQuestion,
    onAnswer: (List<List<String>>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selections = remember(pending.id) { mutableStateMapOf<Int, MutableSet<String>>() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Card, RoundedCornerShape(14.dp))
            .border(1.dp, Line, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        pending.questions.forEachIndexed { qi, q ->
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (q.header.isNotEmpty()) {
                    Text(q.header.uppercase(), color = Muted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
                Text(q.question, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)

                q.options.forEach { opt ->
                    val chosen = selections.getOrPut(qi) { mutableSetOf() }.contains(opt.label)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (chosen) Bubble else androidx.compose.ui.graphics.Color.Transparent, RoundedCornerShape(10.dp))
                            .border(1.dp, if (chosen) Accent else Line, RoundedCornerShape(10.dp))
                            .clickable {
                                val set = selections.getOrPut(qi) { mutableSetOf() }
                                if (q.multiple) {
                                    if (chosen) set.remove(opt.label) else set.add(opt.label)
                                } else {
                                    set.clear(); set.add(opt.label)
                                }
                            }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        if (chosen) {
                            Icon(Icons.Filled.Check, contentDescription = null, tint = Accent, modifier = Modifier.padding(end = 2.dp))
                        }
                        Column {
                            Text(opt.label, color = TextPrimary, fontSize = 13.5.sp)
                            if (opt.description.isNotEmpty()) {
                                Text(opt.description, color = Muted, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .background(Accent, RoundedCornerShape(10.dp))
                .clickable {
                    val answers = pending.questions.indices.map { qi ->
                        selections[qi]?.toList() ?: emptyList()
                    }
                    onAnswer(answers)
                }
                .padding(horizontal = 16.dp, vertical = 10.dp),
        ) {
            Text("Kirim jawaban", color = TextPrimary, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
