package com.example.ansteducation.dto

import com.google.gson.annotations.SerializedName

/** Тело POST /posts: только поля, которые ожидает сервер (без likes, ownedByMe и т.п.). */
data class PostPayload(
    val id: Long = 0,
    val content: String,
    val authorId: Long,
    val author: String? = null,
    val published: String? = null,
    @SerializedName("mentionIds")
    val mentionIds: List<Long>? = null,
    val coords: Coordinates? = null,
    val link: String? = null,
    val attachment: Attachment? = null,
    val video: String? = null,
)
