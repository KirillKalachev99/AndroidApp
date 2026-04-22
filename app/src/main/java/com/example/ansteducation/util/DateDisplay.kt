package com.example.ansteducation.util

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

private val outputFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

private val inputFormats = listOf(
    // 2026-04-22T12:34:56.789Z
    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    },
    // 2026-04-22T12:34:56Z
    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    },
    // 2026-04-22T12:34:56
    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    },
)

fun formatApiDateTime(raw: String?): String {
    if (raw.isNullOrBlank()) return ""
    for (format in inputFormats) {
        val parsed = runCatching { format.parse(raw) }.getOrNull()
        if (parsed != null) {
            return outputFormat.format(parsed)
        }
    }
    // Fallback for already formatted or unexpected values.
    return raw
}
