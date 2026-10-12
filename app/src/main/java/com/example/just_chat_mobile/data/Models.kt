package com.example.just_chat_mobile.data

// Pesan dalam percakapan.
data class ChatMessage(
    val id: Long,
    val role: String,        // "user" | "ai"
    val content: String,
    val time: Long,
    val tps: Int? = null,
    val liked: Boolean = false,
)

// Tool yang sedang dijalankan agent (badge live).
data class ToolInfo(
    val name: String,
    val label: String,
)

// Hasil tool terstruktur (panel lipat di UI).
data class ToolResult(
    val id: String,
    val name: String,
    val display: ToolDisplay?,
)

data class ToolDisplay(
    val kind: String,                 // websearch | webfetch | question | error
    val query: String? = null,
    val url: String? = null,
    val summary: String? = null,
    val results: List<SearchResult> = emptyList(),
    val message: String? = null,
)

data class SearchResult(
    val title: String,
    val url: String,
    val snippet: String,
)

// Pertanyaan dari agent yang menunggu jawaban user.
data class QuestionOption(
    val label: String,
    val description: String,
)

data class Question(
    val question: String,
    val header: String,
    val options: List<QuestionOption>,
    val multiple: Boolean = false,
    val custom: Boolean = true,
)

data class PendingQuestion(
    val id: String,
    val questions: List<Question>,
)

// Kejadian dari SSE /api/chat
sealed interface ChatEvent {
    data class Token(val text: String) : ChatEvent
    data class Tool(val name: String, val label: String) : ChatEvent
    data class ToolResult(val id: String, val name: String, val display: ToolDisplay?) : ChatEvent
    data class AskQuestions(val id: String, val questions: List<Question>) : ChatEvent
    data class Error(val message: String) : ChatEvent
    data object Done : ChatEvent
}
