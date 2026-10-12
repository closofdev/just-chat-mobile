package com.example.just_chat_mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.just_chat_mobile.ui.markdown.MdInline
import com.example.just_chat_mobile.ui.markdown.MarkdownParser
import com.example.just_chat_mobile.ui.theme.TableBg
import com.example.just_chat_mobile.ui.theme.TableHeaderBg
import com.example.just_chat_mobile.ui.theme.TextPrimary
import com.example.just_chat_mobile.ui.theme.White07
import com.example.just_chat_mobile.ui.theme.White08
import com.example.just_chat_mobile.ui.theme.Dimens

/**
 * Tabel markdown: tanpa radius, border tipis, header berlatar, bisa digulir horizontal.
 */
@Composable
fun MarkdownTable(
    head: List<List<MdInline>>,
    rows: List<List<List<MdInline>>>,
    modifier: Modifier = Modifier,
) {
    val scroll = rememberScrollState()
    val columnCount = maxOf(head.size, rows.maxOfOrNull { it.size } ?: 0)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scroll)
            .background(TableBg)
            .border(1.dp, White08)
    ) {
        Row(modifier = Modifier.background(TableHeaderBg)) {
            head.forEach { cell ->
                TableCell(cell, header = true)
            }
        }
        rows.forEachIndexed { rowIndex, row ->
            Row {
                for (c in 0 until columnCount) {
                    val cell = row.getOrNull(c) ?: emptyList()
                    TableCell(cell, header = false, showDivider = rowIndex != rows.lastIndex)
                }
            }
        }
    }
}

@Composable
private fun TableCell(
    inlines: List<MdInline>,
    header: Boolean,
    showDivider: Boolean = false,
) {
    Column {
        InlineText(
            inlines = inlines,
            baseStyle = androidx.compose.ui.text.TextStyle(
                fontSize = Dimens.TableFontSize,
                color = TextPrimary,
                fontWeight = if (header) FontWeight.SemiBold else FontWeight.Normal,
            ),
            modifier = Modifier
                .width(160.dp)
                .padding(horizontal = Dimens.TableCellPadH, vertical = Dimens.TableCellPadV),
        )
        if (showDivider) {
            Column(Modifier.fillMaxWidth().background(White07)) {}
        }
    }
}
