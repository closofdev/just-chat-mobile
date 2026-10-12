package com.example.just_chat_mobile.ui.markdown

// Port dari src/lib/markdown.js — alur & aturan dibuat sama:
//  1. code block diamankan lebih dulu
//  2. rumus blok diamankan
//  3. baris diproses: tabel, heading, hr, quote, list, paragraf
//  4. inline: bold, code, link, rumus
object MarkdownParser {

    // penanda code block di dalam teks
    private const val PH_OPEN = '\u0000'
    private const val PH_CLOSE = '\u0001'

    fun parse(src: String): List<MdBlock> {
        val codeBlocks = mutableListOf<MdBlock.CodeBlock>()

        // 1. code block tertutup
        var text = Regex("```(\\w*)\\n([\\s\\S]*?)```").replace(src) { m ->
            val lang = m.groupValues[1]
            val code = m.groupValues[2].trim('\n')
            codeBlocks.add(MdBlock.CodeBlock(lang, code))
            "$PH_OPEN${codeBlocks.size - 1}$PH_CLOSE"
        }

        // fence yang belum tertutup (saat streaming) -> langsung jadi code block
        var tailCode: MdBlock.CodeBlock? = null
        val fenceIdx = text.indexOf("```")
        if (fenceIdx != -1) {
            val head = text.substring(0, fenceIdx)
            val tailRaw = text.substring(fenceIdx)
            val langMatch = Regex("^```(\\w*)\\n?").find(tailRaw)
            val lang = langMatch?.groupValues?.get(1) ?: ""
            val body = tailRaw.removePrefix(langMatch?.value ?: "```").trimEnd('\n')
            tailCode = MdBlock.CodeBlock(lang, body)
            text = head
        }

        val lines = text.split('\n')
        val out = mutableListOf<MdBlock>()

        var para = mutableListOf<MdInline>()
        var listItems = mutableListOf<List<MdInline>>()
        var listKind = 0 // 0 none, 1 ul, 2 ol
        var listGap = false

        fun flushPara() {
            if (para.isNotEmpty()) { out.add(MdBlock.Paragraph(para.toList())); para = mutableListOf() }
        }
        fun flushList() {
            if (listItems.isNotEmpty()) {
                out.add(if (listKind == 2) MdBlock.NumberedList(listItems.toList()) else MdBlock.BulletList(listItems.toList()))
            }
            listItems = mutableListOf(); listKind = 0; listGap = false
        }
        fun openList(kind: Int) {
            if (listKind == kind) { listGap = false; return }
            flushList()
            listKind = kind; listGap = false
        }

        var i = 0
        while (i < lines.size) {
            val line = lines[i]

            // placeholder code block
            val ph = Regex("^$PH_OPEN(\\d+)$PH_CLOSE$").find(line)
            if (ph != null) {
                flushPara(); flushList()
                out.add(codeBlocks[ph.groupValues[1].toInt()])
                i++; continue
            }

            // angka telanjang: di dalam list = sisa penomoran (dibuang)
            if (Regex("^\\s*\\**\\s*\\d{1,2}\\s*[.)]?\\s*\\**\\s*$").matches(line)) {
                if (listKind != 0) { i++; continue }
                flushPara(); flushList()
                para.addAll(inline(line.trim()))
                i++; continue
            }

            // tabel: baris header + garis pemisah
            if (isTableRow(line) && i + 1 < lines.size && isTableSep(lines[i + 1])) {
                flushPara(); flushList()
                val head = cellsOf(line)
                i += 2
                val rows = mutableListOf<List<List<MdInline>>>()
                while (i < lines.size && isTableRow(lines[i])) {
                    rows.add(cellsOf(lines[i])); i++
                }
                out.add(MdBlock.Table(head, rows))
                continue
            }

            val heading = Regex("^(#{1,4})\\s+(.+)").find(line)
            if (heading != null) {
                flushPara(); flushList()
                out.add(MdBlock.Heading(heading.groupValues[1].length, inline(heading.groupValues[2])))
                i++; continue
            }

            if (Regex("^---+$").matches(line.trim())) {
                flushPara(); flushList(); out.add(MdBlock.Rule); i++; continue
            }

            // rumus blok berdiri di baris sendiri ($$...$$ atau \[...\])
            val mathSegs = MathDetector.splitBlocks(line)
            if (mathSegs.any { it is MathSegment.Math } && mathSegs.size == 1) {
                flushPara(); flushList()
                val seg = mathSegs[0] as MathSegment.Math
                out.add(MdBlock.Math(seg.tex, true))
                i++; continue
            }

            val quote = Regex("^>\\s?(.*)").find(line)
            if (quote != null) {
                flushPara(); flushList()
                out.add(MdBlock.Quote(inline(quote.groupValues[1])))
                i++; continue
            }

            val bullet = Regex("^-\\s+(.+)").find(line)
            if (bullet != null) {
                flushPara(); openList(1)
                listItems.add(inline(bullet.groupValues[1])); i++; continue
            }

            val numbered = Regex("^\\d+[.)]\\s+(.+)").find(line)
            if (numbered != null) {
                flushPara(); openList(2)
                listItems.add(inline(numbered.groupValues[1])); i++; continue
            }

            if (line.isBlank()) {
                flushPara()
                if (listKind != 0) listGap = true else flushList()
                i++; continue
            }

            flushPara(); flushList()
            para.addAll(inline(line))
            if (i < lines.size - 1) para.add(MdInline.LineBreak())
            i++
        }
        flushPara(); flushList()

        // rumus blok berdiri sendiri
        if (tailCode != null) out.add(tailCode)
        return mergeBlockMath(out)
    }

    // rumus blok yang tersisa di dalam paragraf dinaikkan jadi blok sendiri
    // (mis. "$$...$$" yang menyatu dengan teks lain di baris yang sama)
    private fun mergeBlockMath(blocks: List<MdBlock>): List<MdBlock> {
        val out = mutableListOf<MdBlock>()
        for (b in blocks) {
            if (b !is MdBlock.Paragraph) { out.add(b); continue }
            val buf = mutableListOf<MdInline>()
            for (inl in b.inlines) {
                if (inl is MdInline.MathInline && isDisplayMath(inl.tex)) {
                    if (buf.isNotEmpty()) { out.add(MdBlock.Paragraph(buf.toList())); buf.clear() }
                    out.add(MdBlock.Math(inl.tex.removePrefix(DISPLAY_PREFIX), true))
                } else buf.add(inl)
            }
            if (buf.isNotEmpty()) out.add(MdBlock.Paragraph(buf.toList()))
        }
        return out
    }

    // penanda khusus untuk rumus yang HARUS tampil sebagai blok sendiri
    private const val DISPLAY_PREFIX = "\u0002"
    private fun isDisplayMath(tex: String) = tex.startsWith(DISPLAY_PREFIX)

    // ---------- inline ----------

    private fun inline(raw: String): List<MdInline> {
        val out = mutableListOf<MdInline>()
        // rumus inline diamankan dulu
        val segments = MathDetector.splitInline(raw)
        for (seg in segments) {
            when (seg) {
                // jangan beri prefix display: splitInline hanya mengembalikan rumus inline
                is MathSegment.Math -> out.add(MdInline.MathInline(seg.tex))
                is MathSegment.Plain -> out.addAll(inlinePlain(seg.text))
            }
        }
        return out
    }

    private fun inlinePlain(text: String): List<MdInline> {
        val out = mutableListOf<MdInline>()
        var i = 0
        val sb = StringBuilder()

        fun flush() { if (sb.isNotEmpty()) { out.add(MdInline.Text(sb.toString())); sb.clear() } }

        while (i < text.length) {
            // bold **...**
            if (text.startsWith("**", i)) {
                val end = text.indexOf("**", i + 2)
                if (end != -1) {
                    flush()
                    out.add(MdInline.Bold(inlinePlain(text.substring(i + 2, end))))
                    i = end + 2; continue
                }
            }
            // inline code `...`
            if (text[i] == '`') {
                val end = text.indexOf('`', i + 1)
                if (end != -1) {
                    flush()
                    out.add(MdInline.Code(text.substring(i + 1, end)))
                    i = end + 1; continue
                }
            }
            // link [label](href)
            if (text[i] == '[') {
                val closeBracket = text.indexOf(']', i)
                if (closeBracket != -1 && closeBracket + 1 < text.length && text[closeBracket + 1] == '(') {
                    val closeParen = text.indexOf(')', closeBracket + 2)
                    if (closeParen != -1) {
                        val label = text.substring(i + 1, closeBracket)
                        val href = text.substring(closeBracket + 2, closeParen)
                        if (href.startsWith("http")) {
                            flush()
                            out.add(MdInline.Link(label, href))
                            i = closeParen + 1; continue
                        }
                    }
                }
            }
            // URL mentah
            if (text.startsWith("http://", i) || text.startsWith("https://", i)) {
                var end = i
                while (end < text.length && !text[end].isWhitespace() && text[end] != ')' && text[end] != '<') end++
                flush()
                val url = text.substring(i, end)
                out.add(MdInline.Link(url, url))
                i = end; continue
            }
            sb.append(text[i]); i++
        }
        flush()
        return out
    }

    // ---------- tabel ----------

    private fun isTableRow(l: String) = Regex("^\\s*\\|.*\\|\\s*$").matches(l)
    private fun isTableSep(l: String) = Regex("^\\s*\\|[\\s:|-]+\\|\\s*$").matches(l) && l.contains('-')

    private fun cellsOf(l: String): List<List<MdInline>> =
        l.trim().removePrefix("|").removeSuffix("|").split("|").map { inline(it.trim()) }

    /** Teks polos dari inline (untuk salin kode dsb). */
    fun plainText(inlines: List<MdInline>): String = buildString {
        fun walk(list: List<MdInline>) {
            for (x in list) when (x) {
                is MdInline.Text -> append(x.text)
                is MdInline.Code -> append(x.text)
                is MdInline.Link -> append(x.label)
                is MdInline.MathInline -> append(x.tex.removePrefix(DISPLAY_PREFIX))
                is MdInline.Bold -> walk(x.children)
                is MdInline.LineBreak -> append('\n')
            }
        }
        walk(inlines)
    }
}
