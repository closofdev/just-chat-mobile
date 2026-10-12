package com.example.just_chat_mobile.ui.theme

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Ukuran & spacing — dipetakan dari styles.css (nilai px -> dp 1:1)
object Dimens {
    // layout
    val ScreenMaxWidth = 820.dp
    val ChatPadH = 22.dp
    val ChatPadTop = 26.dp
    val ChatPadBottom = 10.dp
    val MessagesGap = 18.dp

    // bubble user
    val BubblePadH = 16.dp
    val BubblePadV = 11.dp
    val BubbleMaxWidthFraction = 0.75f
    val BubbleMaxWidthFractionMobile = 0.88f
    val TimeMarginTop = 5.dp

    // jawaban AI
    val AiGap = 11.dp
    val AiLineHeight = 22.5.sp   // 15px * 1.5
    val AiFontSize = 15.sp

    // aksi (copy / retry / like)
    val ActionSize = 20.dp
    val ActionSizeMobile = 24.dp
    val ActionIcon = 13.dp
    val RespTimeFont = 10.5.sp

    // code block
    val PrePad = 14.dp
    val PrePadTopWithLang = 30.dp
    val PreFontSize = 13.sp
    val PreLineHeight = 20.sp
    val CopyBtnSize = 26.dp
    val CopyIcon = 13.dp

    // composer
    val ComposerPadOuterH = 20.dp
    val ComposerPadOuterTop = 12.dp
    val ComposerPadOuterBottom = 18.dp
    val ComposerRadius = 22.dp
    val ComposerPadV = 9.dp
    val ComposerPadStart = 5.dp
    val ComposerPadEnd = 9.dp
    val InputFontSize = 15.sp
    val InputMaxHeight = 160.dp
    val PlusBtnSize = 38.dp
    val SendBtnSize = 37.dp

    // popup menu
    val PopMenuMinWidth = 200.dp
    val PopMenuMaxWidth = 260.dp
    val PopMenuRadius = 14.dp

    // tabel
    val TableCellPadH = 12.dp
    val TableCellPadV = 8.dp
    val TableFontSize = 13.5.sp

    // heading
    val H2 = 18.sp
    val H3 = 16.5.sp
    val H4 = 15.sp
    val H2MarginTop = 22.dp
    val H2MarginBottom = 8.dp
    val H3MarginTop = 18.dp
    val H3MarginBottom = 7.dp
    val H4MarginTop = 14.dp
    val H4MarginBottom = 6.dp
}
