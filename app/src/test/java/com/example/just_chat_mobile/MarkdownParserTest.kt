package com.example.just_chat_mobile

import com.example.just_chat_mobile.ui.markdown.MarkdownParser
import com.example.just_chat_mobile.ui.markdown.MdBlock
import com.example.just_chat_mobile.ui.markdown.MdInline
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

// Membandingkan perilaku parser Kotlin dengan versi JS (src/lib/markdown.js).
class MarkdownParserTest {

    @Test
    fun paragraf_sederhana() {
        val blocks = MarkdownParser.parse("Halo dunia")
        assertEquals(1, blocks.size)
        assertTrue(blocks[0] is MdBlock.Paragraph)
    }

    @Test
    fun bold_terdeteksi() {
        val blocks = MarkdownParser.parse("Ini **tebal** ya")
        val para = blocks[0] as MdBlock.Paragraph
        assertTrue(para.inlines.any { it is MdInline.Bold })
    }

    @Test
    fun code_block_dengan_bahasa() {
        val src = "```python\ndef f():\n    return 1\n```"
        val blocks = MarkdownParser.parse(src)
        val code = blocks.filterIsInstance<MdBlock.CodeBlock>().first()
        assertEquals("python", code.lang)
        assertTrue(code.code.contains("def f()"))
    }

    @Test
    fun fence_terbuka_saat_streaming() {
        val blocks = MarkdownParser.parse("Halo\n\n```cpp\nint main() {")
        assertTrue(blocks.any { it is MdBlock.CodeBlock })
    }

    @Test
    fun list_bullet() {
        val blocks = MarkdownParser.parse("- satu\n- dua\n- tiga")
        val list = blocks.filterIsInstance<MdBlock.BulletList>().first()
        assertEquals(3, list.items.size)
    }

    @Test
    fun list_bernomor_dengan_angka_yatim_tidak_terpecah() {
        // kasus bug di web: ada baris "   3" di tengah list
        val src = "1. Install\n2. Buka file\n   3\n3. Tekan build\n4. Selesai"
        val lists = blocks(src)
        assertEquals("harus satu blok ol", 1, lists.filterIsInstance<MdBlock.NumberedList>().size)
        assertEquals(4, lists.filterIsInstance<MdBlock.NumberedList>().first().items.size)
    }

    @Test
    fun tabel_terdeteksi() {
        val src = "| Variabel | Nilai |\n|---|---|\n| a | 5 |\n| b | 3 |"
        val table = blocks(src).filterIsInstance<MdBlock.Table>().first()
        assertEquals(2, table.head.size)
        assertEquals(2, table.rows.size)
    }

    @Test
    fun heading_terdeteksi() {
        val lists = blocks("## Cara Compile\nisi")
        assertTrue(lists.any { it is MdBlock.Heading && it.level == 2 })
    }

    @Test
    fun rumus_blok_dari_baris_sendiri() {
        val src = "\$\$A = \\\\begin{pmatrix} 2 & -1 \\\\\\\\ 3 & 4 \\\\end{pmatrix}\$\$"
        val math = blocks(src).filterIsInstance<MdBlock.Math>().first()
        assertTrue(math.tex.contains("pmatrix"))
        assertTrue(math.display)
    }

    @Test
    fun rumus_inline_terdeteksi() {
        val lists = blocks("Nilai \$x = 5\$ ya")
        val para = lists[0] as MdBlock.Paragraph
        assertTrue(para.inlines.any { it is MdInline.MathInline })
    }

    @Test
    fun teks_harga_bukan_rumus() {
        val lists = blocks("Harganya \$5 dan \$10 saja")
        val para = lists[0] as MdBlock.Paragraph
        assertTrue("tidak boleh jadi rumus", para.inlines.none { it is MdInline.MathInline })
    }

    @Test
    fun link_terdeteksi() {
        val lists = blocks("Lihat [situs](https://example.com) ini")
        val para = lists[0] as MdBlock.Paragraph
        assertTrue(para.inlines.any { it is MdInline.Link })
    }

    private fun blocks(src: String) = MarkdownParser.parse(src)
}
