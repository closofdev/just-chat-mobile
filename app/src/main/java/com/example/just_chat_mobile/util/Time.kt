package com.example.just_chat_mobile.util

import java.util.Locale

// Port dari src/lib/time.js
object Time {

    fun timeStr(t: Long): String {
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = t
        val h = cal.get(java.util.Calendar.HOUR_OF_DAY)
        val m = cal.get(java.util.Calendar.MINUTE)
        return String.format(Locale.US, "%02d:%02d", h, m)
    }

    fun calcTps(text: String, elapsedMs: Long): Int {
        val words = text.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }.size
        val secs = maxOf(0.5, elapsedMs / 1000.0)
        return maxOf(1, Math.round(words * 1.3 / secs).toInt())
    }

    // waktu relatif bahasa Indonesia, detik integer
    fun timeAgo(t: Long): String {
        val s = maxOf(0L, Math.round((System.currentTimeMillis() - t) / 1000.0))
        if (s < 5) return "baru saja"
        if (s < 60) return "$s detik yang lalu"
        val mnt = s / 60
        if (mnt < 60) return "$mnt menit yang lalu"
        return "${mnt / 60} jam yang lalu"
    }

    // label waktu + kecepatan: "49 detik yang lalu | 45 token/s"
    fun fmtResp(time: Long, tps: Int?): String {
        return timeAgo(time) + if (tps != null && tps > 0) " | $tps token/s" else ""
    }
}
