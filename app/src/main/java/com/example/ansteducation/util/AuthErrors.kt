package com.example.ansteducation.util

import retrofit2.HttpException

/** HTTP 401/403 or client-side checks that require a signed-in user. */
fun Throwable.requiresSignIn(): Boolean {
    var t: Throwable? = this
    while (t != null) {
        when (t) {
            is HttpException -> {
                val code = t.code()
                if (code == 401 || code == 403) return true
            }
            is IllegalStateException -> {
                val m = t.message.orEmpty()
                if (m.contains("Войдите в аккаунт", ignoreCase = true)) return true
            }
        }
        t = t.cause
    }
    return false
}
