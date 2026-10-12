package com.example.just_chat_mobile.ui.components

import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.just_chat_mobile.ui.markdown.MdInline
import com.example.just_chat_mobile.ui.theme.AccentLink
import com.example.just_chat_mobile.ui.theme.White07

private const val MATH_TAG = "m"

/**
 * Render inline markdown jadi AnnotatedString.
 * Rumus inline disisipkan lewat InlineTextContent supaya tetap satu baris.
 */
@Composable
fun InlineText(
    inlines: List<MdInline>,
    baseStyle: TextStyle,
    modifier: Modifier = Modifier,
) {
    val mathSlots = linkedMapOf<String, InlineTextContent>()

    val annotated = buildAnnotatedString {
        fun emit(list: List<MdInline>) {
            for (x in list) {
                when (x) {
                    is MdInline.Text -> append(x.text)
                    is MdInline.Bold -> withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) { emit(x.children) }
                    is MdInline.Code -> withStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = (baseStyle.fontSize.value * 0.86).sp,
                            background = White07,
                        )
                    ) { append(x.text) }
                    is MdInline.Link -> withStyle(SpanStyle(color = AccentLink)) { append(x.label) }
                    is MdInline.LineBreak -> append('\n')
                    is MdInline.MathInline -> {
                        val tex = x.tex.removePrefix("\u0002")
                        val key = "$MATH_TAG${mathSlots.size}"
                        mathSlots[key] = InlineTextContent(
                            Placeholder(
                                width = 1.em,
                                height = 1.em,
                                placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter,
                            )
                        ) {
                            MathView(tex = tex, display = false)
                        }
                        appendInlineContent(key, tex)
                    }
                }
            }
        }
        emit(inlines)
    }

    if (mathSlots.isEmpty()) {
        Text(text = annotated, style = baseStyle, modifier = modifier)
    } else {
        Text(text = annotated, style = baseStyle, inlineContent = mathSlots, modifier = modifier)
    }
}
