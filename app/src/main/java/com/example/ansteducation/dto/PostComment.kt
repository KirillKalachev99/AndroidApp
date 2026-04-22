package com.example.ansteducation.dto

/** Элемент списка GET posts/{id}/comments (дипломный NMedia-сервер). */
data class PostComment(
    val id: Long,
    val postId: Long? = null,
    val authorId: Long = 0L,
    val author: String = "",
    val authorAvatar: String? = null,
    val content: String = "",
    val published: String = "",
)

/** Тело POST posts/{id}/comments */
data class CommentPayload(
    val content: String,
)
