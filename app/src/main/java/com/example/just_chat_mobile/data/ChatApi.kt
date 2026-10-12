package com.example.just_chat_mobile.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

// Klien /api/chat dengan streaming SSE.
// Token CodeBuddy tetap di server Vercel; app tidak menyimpan kredensial apa pun.
class ChatApi(
    private val baseUrl: String = DEFAULT_BASE,
) {
    companion object {
        const val DEFAULT_BASE = "https://justchat.closof.site"
        const val MODEL = "deepseek-v4.1-flash"
        private val JSON = "application/json; charset=utf-8".toMediaType()
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS) // streaming: tanpa batas baca
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /** Ubah riwayat chat jadi format OpenAI. */
    fun toHistory(messages: List<ChatMessage>): JSONArray {
        val arr = JSONArray()
        for (m in messages) {
            arr.put(JSONObject().apply {
                put("role", if (m.role == "ai") "assistant" else "user")
                put("content", m.content)
            })
        }
        return arr
    }

    /** Mulai stream. Caller membatalkan dengan membatalkan coroutine scope-nya. */
    fun stream(messages: List<ChatMessage>): Flow<ChatEvent> = callbackFlow {
        val body = JSONObject().apply {
            put("model", MODEL)
            put("messages", toHistory(messages))
        }.toString().toRequestBody(JSON)

        val request = Request.Builder()
            .url("$baseUrl/api/chat")
            .post(body)
            .header("Accept", "text/event-stream")
            .build()

        val call = client.newCall(request)

        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                trySend(ChatEvent.Error(e.message ?: "koneksi gagal"))
                close()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use { res ->
                    if (!res.isSuccessful) {
                        val detail = runCatching { res.body?.string() }.getOrNull() ?: ""
                        val msg = extractError(detail)
                        trySend(ChatEvent.Error("HTTP ${res.code}${if (msg.isNotEmpty()) ": $msg" else ""}"))
                        close()
                        return
                    }
                    val source = res.body?.source()
                    if (source == null) {
                        trySend(ChatEvent.Error("respon kosong"))
                        close()
                        return
                    }

                    var event: String? = null
                    val dataBuf = StringBuilder()

                    fun dispatch() {
                        if (dataBuf.isEmpty()) return
                        val raw = dataBuf.toString().trim()
                        dataBuf.setLength(0)
                        val ev = event
                        event = null
                        if (raw.isEmpty() || raw == "[DONE]") {
                            if (raw == "[DONE]") trySend(ChatEvent.Done)
                            return
                        }
                        trySend(parseEvent(ev, raw))
                    }

                    try {
                        while (true) {
                            val line = source.readUtf8Line() ?: break
                            when {
                                line.isEmpty() -> dispatch()
                                line.startsWith("event:") -> event = line.removePrefix("event:").trim()
                                line.startsWith("data:") -> {
                                    if (dataBuf.isNotEmpty()) dataBuf.append('\n')
                                    dataBuf.append(line.removePrefix("data:").trim())
                                }
                            }
                        }
                        dispatch()
                    } catch (e: IOException) {
                        if (!call.isCanceled()) trySend(ChatEvent.Error(e.message ?: "stream terputus"))
                    } finally {
                        close()
                    }
                }
            }
        })

        awaitClose { call.cancel() }
    }.flowOn(Dispatchers.IO)

    /** Kirim jawaban form pertanyaan ke agent. */
    suspend fun replyQuestion(id: String, answers: List<List<String>>): Boolean =
        withContext(Dispatchers.IO) {
            val payload = JSONObject().apply {
                put("id", id)
                put("answers", JSONArray().apply { answers.forEach { put(JSONArray(it)) } })
            }
            val request = Request.Builder()
                .url("$baseUrl/api/tool/question/reply")
                .post(payload.toString().toRequestBody(JSON))
                .build()
            runCatching {
                client.newCall(request).execute().use { it.isSuccessful }
            }.getOrDefault(false)
        }

    private fun extractError(body: String): String =
        runCatching { JSONObject(body).optString("error") }.getOrNull() ?: ""

    private fun parseEvent(event: String?, raw: String): ChatEvent {
        val json = runCatching { JSONObject(raw) }.getOrNull()
            ?: return ChatEvent.Error("chunk tidak valid")
        return when (event) {
            "token" -> ChatEvent.Token(json.optString("text"))
            "tool" -> ChatEvent.Tool(
                name = json.optString("name"),
                label = json.optString("label"),
            )
            "tool_result" -> ChatEvent.ToolResult(
                id = json.optString("id"),
                name = json.optString("name"),
                display = parseDisplay(json.optJSONObject("display")),
            )
            "question" -> ChatEvent.AskQuestions(
                id = json.optString("id"),
                questions = parseQuestions(json.optJSONArray("questions")),
            )
            "error" -> ChatEvent.Error(json.optString("message"))
            else -> {
                val text = json.optString("text")
                if (text.isNotEmpty()) ChatEvent.Token(text) else ChatEvent.Done
            }
        }
    }

    private fun parseDisplay(obj: JSONObject?): ToolDisplay? {
        if (obj == null) return null
        val results = mutableListOf<SearchResult>()
        obj.optJSONArray("results")?.let { arr ->
            for (i in 0 until arr.length()) {
                val r = arr.optJSONObject(i) ?: continue
                results.add(
                    SearchResult(
                        title = r.optString("title"),
                        url = r.optString("url"),
                        snippet = r.optString("snippet"),
                    )
                )
            }
        }
        return ToolDisplay(
            kind = obj.optString("kind"),
            query = obj.optString("query").ifEmpty { null },
            url = obj.optString("url").ifEmpty { null },
            summary = obj.optString("summary").ifEmpty { null },
            results = results,
            message = obj.optString("message").ifEmpty { null },
        )
    }

    private fun parseQuestions(arr: JSONArray?): List<Question> {
        if (arr == null) return emptyList()
        val list = mutableListOf<Question>()
        for (i in 0 until arr.length()) {
            val q = arr.optJSONObject(i) ?: continue
            val opts = mutableListOf<QuestionOption>()
            q.optJSONArray("options")?.let { oa ->
                for (j in 0 until oa.length()) {
                    val o = oa.optJSONObject(j) ?: continue
                    opts.add(QuestionOption(o.optString("label"), o.optString("description")))
                }
            }
            list.add(
                Question(
                    question = q.optString("question"),
                    header = q.optString("header"),
                    options = opts,
                    multiple = q.optBoolean("multiple", false),
                    custom = q.optBoolean("custom", true),
                )
            )
        }
        return list
    }
}
