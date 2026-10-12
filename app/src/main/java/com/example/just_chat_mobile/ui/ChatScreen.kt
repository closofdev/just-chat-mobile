package com.example.just_chat_mobile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.just_chat_mobile.ui.components.AiMessage
import com.example.just_chat_mobile.ui.components.Composer
import com.example.just_chat_mobile.ui.components.QuestionForm
import com.example.just_chat_mobile.ui.components.StreamMessage
import com.example.just_chat_mobile.ui.components.ThinkingDot
import com.example.just_chat_mobile.ui.components.ToolBadge
import com.example.just_chat_mobile.ui.components.UserBubble
import com.example.just_chat_mobile.ui.components.WelcomeState
import com.example.just_chat_mobile.ui.theme.Bg
import com.example.just_chat_mobile.ui.theme.Muted
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
    viewModel: ChatViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    // auto-scroll: ikuti pesan baru, kecuali user sedang membaca ke atas
    LaunchedEffect(state.messages.size, state.streamText) {
        val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
        val total = listState.layoutInfo.totalItemsCount
        val nearBottom = total - lastVisible <= 3
        if (nearBottom) {
            val target = (total - 1).coerceAtLeast(0)
            listState.animateScrollToItem(target)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .systemBarsPadding()
            .imePadding(),
    ) {
        // layar awal saat belum ada pesan sama sekali
        val empty = state.messages.isEmpty() && state.streamText == null && !state.thinking
        if (empty) {
            WelcomeState(
                onPickPrompt = { label -> viewModel.send(label.replace(Regex("^[^\\p{L}\\p{N}]+"), "").trim()) },
                modifier = Modifier.weight(1f),
            )
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 22.dp, end = 22.dp, top = 26.dp, bottom = 10.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                items(state.messages.size) { index ->
                    val msg = state.messages[index]
                    if (msg.role == "user") {
                        UserBubble(msg = msg)
                    } else {
                        AiMessage(
                            msg = msg,
                            onRegenerate = { viewModel.regenerate(index) },
                            onToggleLike = { viewModel.toggleLike(msg.id) },
                        )
                    }
                }

                if (state.streamText != null && state.streamText!!.isNotEmpty()) {
                    item { StreamMessage(text = state.streamText!!) }
                }

                if (state.activeTool != null) {
                    item { ToolBadge(tool = state.activeTool) }
                }

                if (state.thinking) {
                    item { ThinkingDot(modifier = Modifier.padding(vertical = 6.dp)) }
                }

                state.pendingQuestion?.let { pending ->
                    item {
                        QuestionForm(
                            pending = pending,
                            onAnswer = { answers -> viewModel.answerQuestion(pending.id, answers) },
                        )
                    }
                }
            }
        }

        Composer(
            isStreaming = state.isStreaming,
            disabled = state.pendingQuestion != null,
            onSend = viewModel::send,
            onStop = viewModel::stop,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
        )
    }
}
