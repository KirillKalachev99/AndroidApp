package com.example.ansteducation.util

import com.example.ansteducation.BuildConfig

/** Аватары и медиа: относительные пути с [BuildConfig.SERVER_ORIGIN], абсолютные URL как есть. */
object ServerUrls {

    private fun isAbsolute(url: String): Boolean =
        url.startsWith("http://", ignoreCase = true) ||
            url.startsWith("https://", ignoreCase = true)

    fun avatar(stored: String?): String? {
        val s = stored?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        if (isAbsolute(s)) return s
        val base = BuildConfig.SERVER_ORIGIN.trimEnd('/')
        return "$base/avatars/${s.trimStart('/')}"
    }

    fun media(path: String): String {
        val s = path.trim()
        if (isAbsolute(s)) return s
        val base = BuildConfig.SERVER_ORIGIN.trimEnd('/')
        return "$base/media/${s.trimStart('/')}"
    }
}
