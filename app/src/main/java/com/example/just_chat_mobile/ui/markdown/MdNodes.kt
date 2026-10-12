package com.example.just_chat_mobile.ui.markdown

// Struktur hasil parse — ditampilkan oleh Compose, bukan HTML.
sealed interface MdBlock {
    data class Paragraph(val inlines: List<MdInline>) : MdBlock
    data class Heading(val level: Int, val inlines: List<MdInline>) : MdBlock
    data class BulletList(val items: List<List<MdInline>>) : MdBlock
    data class NumberedList(val items: List<List<MdInline>>) : MdBlock
    data class CodeBlock(val lang: String, val code: String) : MdBlock
    data class Quote(val inlines: List<MdInline>) : MdBlock
    data object Rule : MdBlock
    data class Table(val head: List<List<MdInline>>, val rows: List<List<List<MdInline>>>) : MdBlock
    data class Math(val tex: String, val display: Boolean) : MdBlock
}

sealed interface MdInline {
    data class Text(val text: String) : MdInline
    data class Bold(val children: List<MdInline>) : MdInline
    data class Code(val text: String) : MdInline
    data class Link(val label: String, val href: String) : MdInline
    data class MathInline(val tex: String) : MdInline
    data class LineBreak(val dummy: Int = 0) : MdInline
}
