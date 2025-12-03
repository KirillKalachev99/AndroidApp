package com.example.ansteducation.util

import java.io.IOException
import java.sql.SQLException

sealed class AppError(val code: String): RuntimeException() {
    companion object {
        fun from(e: Throwable) = when (e) {
            is AppError -> e
            is SQLException -> DbError
            is IOException -> NetworkError
            else -> UnknownError
        }
    }
}

class ApiError(val status: Int, code: String) : AppError(code)
object NetworkError : AppError("error_network") {
    private fun readResolve(): Any = NetworkError
}

object DbError : AppError("error_db") {
    private fun readResolve(): Any = DbError
}

object UnknownError : AppError("error_unknown") {
    private fun readResolve(): Any = UnknownError
}