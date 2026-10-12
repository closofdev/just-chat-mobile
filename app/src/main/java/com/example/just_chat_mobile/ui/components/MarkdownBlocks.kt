package com.example.just_chat_mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.just_chat_mobile.ui.markdown.MarkdownParser
import com.example.just_chat_mobile.ui.markdown.MdBlock
import com.example.just_chat_mobile.ui.theme.Accent
import com.example.just_chat_mobile.ui.theme.Dimens
import com.example.just_chat_mobile.ui.theme.Line
import com.example.just_chat_mobile.ui.theme.TextPrimary
import com.example.just_chat_mobile.ui.theme.White07

/** Base style untuk isi jawaban AI (15sp / line-height 1.5). */
val AiBaseStyle = TextStyle(
    fontSize = Dimens.AiFontSize,
    color = TextPrimary,
    lineHeight = Dimens.AiLineHeight,
)

/**
 * Render daftar blok markdown. `onMathRendered` dipakai untuk memicu re-layout
 * ketika rumus selesai digambar (tinggi berubah).
 */
@Composable
fun MarkdownBlocks(
    blocks: List<MdBlock>,
    modifier: Modifier = Modifier,
    baseStyle: TextStyle = AiBaseStyle,
) {
    Column(modifier = modifier) {
        blocks.forEach { block -> BlockView(block, baseStyle) }
    }
}

@Composable
private fun BlockView(block: MdBlock, baseStyle: TextStyle) {
    when (block) {
        is MdBlock.Paragraph -> {
            InlineText(block.inlines, baseStyle, Modifier.padding(bottom = 6.dp))
        }

        is MdBlock.Heading -> {
            val spec = when (block.level) {
                1 -> HeadingSpec(Dimens.H2, Dimens.H2MarginTop, Dimens.H2MarginBottom)
                2 -> HeadingSpec(Dimens.H3, Dimens.H3MarginTop, Dimens.H3MarginBottom)
                else -> HeadingSpec(Dimens.H4, Dimens.H4MarginTop, Dimens.H4MarginBottom)
            }
            InlineText(
                block.inlines,
                baseStyle.copy(
                    fontSize = spec.size,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = spec.size * 1.35f,
                ),
                Modifier.padding(top = spec.top, bottom = spec.bottom),
            )
        }

        is MdBlock.BulletList -> {
            Column(Modifier.padding(top = 6.dp, bottom = 6.dp)) {
                block.items.forEach { item ->
                    Row(Modifier.padding(vertical = 3.dp)) {
                        Text("•", color = Accent, fontSize = baseStyle.fontSize, modifier = Modifier.width(18.dp))
                        InlineText(item, baseStyle)
                    }
                }
            }
        }

        is MdBlock.NumberedList -> {
            Column(Modifier.padding(top = 6.dp, bottom = 6.dp)) {
                block.items.forEachIndexed { i, item ->
                    Row(Modifier.padding(vertical = 3.dp)) {
                        Text(
                            "${i + 1}.",
                            color = Accent,
                            fontSize = baseStyle.fontSize,
                            modifier = Modifier.width(22.dp),
                        )
                        InlineText(item, baseStyle)
                    }
                }
            }
        }

        is MdBlock.CodeBlock -> {
            Spacer(Modifier.height(10.dp))
            CodeBlock(lang = block.lang, code = block.code, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(10.dp))
        }

        is MdBlock.Quote -> {
            Row(Modifier.padding(vertical = 10.dp)) {
                Box(Modifier.width(2.dp).height(20.dp).background(Accent))
                Spacer(Modifier.width(12.dp))
                InlineText(block.inlines, baseStyle)
            }
        }

        MdBlock.Rule -> {
            Box(Modifier.fillMaxWidth().height(1.dp).background(Line).padding(vertical = 8.dp))
            Spacer(Modifier.height(16.dp))
        }

        is MdBlock.Table -> {
            Spacer(Modifier.height(10.dp))
            MarkdownTable(head = block.head, rows = block.rows, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(10.dp))
        }

        is MdBlock.Math -> {
            Spacer(Modifier.height(6.dp))
            MathView(tex = block.tex, display = true, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(6.dp))
        }
    }
}

private data class HeadingSpec(
    val size: androidx.compose.ui.unit.TextUnit,
    val top: androidx.compose.ui.unit.Dp,
    val bottom: androidx.compose.ui.unit.Dp,
)
