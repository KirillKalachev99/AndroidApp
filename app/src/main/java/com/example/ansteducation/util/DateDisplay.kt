package com.example.ansteducation.util

import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val outFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm", Locale.getDefault())

fun formatApiDateTime(raw: String?): String {
    if (raw.isNullOrBlank()) return ""
    return runCatching { OffsetDateTime.parse(raw).format(outFormatter) }.getOrElse {
        runCatching { ZonedDateTime.parse(raw).format(outFormatter) }.getOrElse {
            runCatching { LocalDateTime.parse(raw).format(outFormatter) }.getOrElse {
                raw
            }
        }
    }
}
