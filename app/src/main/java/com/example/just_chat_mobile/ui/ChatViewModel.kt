package com.example.just_chat_mobile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.just_chat_mobile.data.ChatApi
import com.example.just_chat_mobile.data.ChatEvent
import com.example.just_chat_mobile.data.ChatMessage
import com.example.just_chat_mobile.data.PendingQuestion
import com.example.just_chat_mobile.data.ToolDisplay
import com.example.just_chat_mobile.data.ToolInfo
import com.example.just_chat_mobile.util.Time
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Port dari src/hooks/useChat.js
data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val streamText: String? = null,
    val thinking: Boolean = false,
    val isStreaming: Boolean = false,
    val activeTool: ToolInfo? = null,
    val pendingQuestion: PendingQuestion? = null,
    val lastToolDisplay: ToolDisplay? = null,
)

class ChatViewModel(
    private val api: ChatApi = ChatApi(),
) : ViewModel() {

    private val _state = MutableStateFlow(ChatUiState())
    val state: StateFlow<ChatUiState> = _state.asStateFlow()

    private var streamJob: Job? = null
    private var fullText = ""
    private var startedAt = 0L
    private var nextId = 1L

    fun send(text: String) {
        val content = text.trim()
        if (content.isEmpty() || _state.value.isStreaming) return
        val msg = ChatMessage(id = nextId++, role = "user", content = content, time = System.currentTimeMillis())
        _state.update { it.copy(messages = it.messages + msg) }
        runStream()
    }

    /** Regenerate: kirim ulang prompt user terakhir, jawaban lama tetap ada. */
    fun regenerate(index: Int) {
        if (_state.value.isStreaming) return
        val current = _state.value.messages
        val prompt = (index - 1 downTo 0).firstNotNullOfOrNull { k ->
            current.getOrNull(k)?.takeIf { it.role == "user" }?.content
        } ?: return
        val msg = ChatMessage(id = nextId++, role = "user", content = prompt, time = System.currentTimeMillis())
        _state.update { it.copy(messages = it.messages + msg) }
        runStream()
    }

    fun stop() {
        streamJob?.cancel()
        streamJob = null
        finishStream(keepPartial = true)
    }

    fun toggleLike(id: Long) {
        _state.update { s ->
            s.copy(messages = s.messages.map { if (it.id == id) it.copy(liked = !it.liked) else it })
        }
    }

    fun answerQuestion(id: String, answers: List<List<String>>) {
        viewModelScope.launch {
            api.replyQuestion(id, answers)
            _state.update { it.copy(pendingQuestion = null) }
        }
    }

    fun clearToolDisplay() {
        _state.update { it.copy(lastToolDisplay = null) }
    }

    private fun runStream() {
        fullText = ""
        startedAt = System.currentTimeMillis()
        _state.update {
            it.copy(
                thinking = true, isStreaming = true, streamText = "",
                activeTool = null, pendingQuestion = null,
            )
        }

        val history = _state.value.messages
        streamJob = viewModelScope.launch {
            try {
                api.stream(history).collect { ev ->
                    when (ev) {
                        is ChatEvent.Token -> {
                            fullText += ev.text
                            _state.update { it.copy(thinking = false, streamText = fullText) }
                        }
                        is ChatEvent.Tool -> {
                            _state.update {
                                it.copy(
                                    thinking = false,
                                    activeTool = ToolInfo(ev.name, ev.label),
                                )
                            }
                        }
                        is ChatEvent.ToolResult -> {
                            _state.update { it.copy(activeTool = null, lastToolDisplay = ev.display) }
                        }
                        is ChatEvent.AskQuestions -> {
                            _state.update {
                                it.copy(
                                    thinking = false, activeTool = null,
                                    pendingQuestion = PendingQuestion(ev.id, ev.questions),
                                )
                            }
                        }
                        is ChatEvent.Error -> {
                            if (fullText.isBlank()) {
                                _state.update { s ->
                                    s.copy(messages = s.messages + ChatMessage(
                                        id = nextId++, role = "ai",
                                        content = describeError(ev.message),
                                        time = System.currentTimeMillis(),
                                    ))
                                }
                            }
                        }
                        ChatEvent.Done -> Unit
                    }
                }
            } finally {
                finishStream(keepPartial = true)
            }
        }
    }

    private fun finishStream(keepPartial: Boolean) {
        val text = fullText
        if (keepPartial && text.isNotBlank()) {
            val tps = Time.calcTps(text, System.currentTimeMillis() - startedAt)
            _state.update { s ->
                // hindari duplikat kalau sudah ditambahkan
                if (s.messages.lastOrNull()?.content == text) s
                else s.copy(messages = s.messages + ChatMessage(
                    id = nextId++, role = "ai", content = text,
                    time = System.currentTimeMillis(), tps = tps,
                ))
            }
        }
        fullText = ""
        _state.update {
            it.copy(thinking = false, isStreaming = false, streamText = null, activeTool = null)
        }
    }

    private fun describeError(message: String): String {
        val base = "**Gagal menghubungi AI.** $message"
        return when {
            message.contains("401") ->
                "$base\n\nToken CodeBuddy ditolak. Set `CODEBUDDY_TOKEN` yang baru lalu redeploy."
            message.contains("HTTP 404", true) ->
                "$base\n\nEndpoint `/api/chat` tidak ditemukan."
            message.contains("timeout", true) ->
                "$base\n\nProses melebihi batas waktu server."
            else -> base
        }
    }
}
