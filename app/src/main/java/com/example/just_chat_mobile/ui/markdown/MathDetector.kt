package com.example.just_chat_mobile.ui.markdown

// Port dari src/lib/math.js — deteksi rumus & aturan ketat agar teks biasa
// (mis. "harga $5 dan $10") tidak dianggap rumus.
object MathDetector {

    private val BLOCK_DELIMS = listOf("$$" to "$$", "\\[" to "\\]")
    private val INLINE_DELIMS = listOf("\\(" to "\\)", "$" to "$")

    private val MATH_HINT = Regex(
        "[\\\\^_{}=]|\\d\\s*[+\\-*/]|[+\\-*/]\\s*\\d|" +
            "\\b(frac|sqrt|sum|int|alpha|beta|gamma|theta|pi|lambda|matrix|begin|end|cdot|times|leq|geq|neq|pm|infty)\\b"
    )

    fun looksLikeMath(inner: String): Boolean {
        val s = inner.trim()
        if (s.isEmpty()) return false
        if (s.length > 2000) return false
        return MATH_HINT.containsMatchIn(s)
    }

    private fun isSuspiciousDollar(inner: String): Boolean =
        inner.trimStart().firstOrNull()?.isDigit() == true

    /** Cari rumus blok pada satu baris teks. Mengembalikan daftar segmen. */
    fun splitBlocks(line: String): List<MathSegment> = split(line, BLOCK_DELIMS, false)

    /** Cari rumus inline pada sebuah baris. */
    fun splitInline(line: String): List<MathSegment> = split(line, INLINE_DELIMS, true)

    private fun split(line: String, delims: List<Pair<String, String>>, inline: Boolean): List<MathSegment> {
        val out = mutableListOf<MathSegment>()
        var i = 0
        var plainStart = 0

        while (i < line.length) {
            var matched = false
            for ((open, close) in delims) {
                if (!line.startsWith(open, i)) continue
                val end = line.indexOf(close, i + open.length)
                if (end == -1) continue
                val inner = line.substring(i + open.length, end)
                if (inline && inner.contains('\n')) continue
                if (!looksLikeMath(inner)) continue
                if (open == "$" && isSuspiciousDollar(inner)) continue

                if (i > plainStart) out.add(MathSegment.Plain(line.substring(plainStart, i)))
                out.add(MathSegment.Math(inner.trim(), !inline))
                i = end + close.length
                plainStart = i
                matched = true
                break
            }
            if (!matched) i++
        }
        if (plainStart < line.length) out.add(MathSegment.Plain(line.substring(plainStart)))
        return out
    }
}

sealed interface MathSegment {
    data class Plain(val text: String) : MathSegment
    data class Math(val tex: String, val display: Boolean) : MathSegment
}
