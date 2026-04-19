package com.example.ansteducation.dto

import com.google.gson.annotations.SerializedName

sealed interface FeedItem {
    val id: Long
}

data class Coordinates(
    val lat: Double,
    @SerializedName("long")
    val lon: Double,
)

data class UserPreview(
    val id: Long,
    val name: String,
    val login: String,
)

data class Post(
    override val id: Long,
    val author: String,
    val authorAvatar: String? = "",
    val authorId: Long,
    val published: String,
    val content: String,
    /** Число лайков с сервера (если есть); иначе смотри [likeOwnerIds]. */
    val likes: Int = 0,
    /** Gson может прислать null вместо []. */
    @SerializedName("likeOwnerIds")
    val likeOwnerIds: List<Long>? = null,
    val shares: Int = 0,
    val views: Int = 0,
    val sharedByMe: Boolean = false,
    val likedByMe: Boolean = false,
    var viewedByMe: Boolean = false,
    var video: String? = null,
    val attachment: Attachment? = null,
    val ownedByMe: Boolean = false,
    val link: String? = null,
    val coords: Coordinates? = null,
    @SerializedName("mentions")
    val mentions: List<UserPreview>? = null,
    /** Sent on create/update; omitted in many GET responses. */
    @SerializedName("mentionIds")
    val mentionIds: List<Long>? = null,
    /** Число комментариев в ответе сервера (если поля нет — 0). */
    @SerializedName("commentCount")
    val commentCount: Int = 0,
    /** Место работы автора с API (как в профиле); если нет — блок на экране поста не показываем. */
    val authorJob: String? = null,
) : FeedItem

/** Лайки: на дипломном API приходит [Post.likeOwnerIds], поле [Post.likes] может быть 0. */
val Post.likeDisplayCount: Int
    get() = when {
        !likeOwnerIds.isNullOrEmpty() -> likeOwnerIds.size
        else -> likes
    }

data class Ad(
    override val id: Long,
    /** Имя/путь в media на сервере; null — только локальный ресурс [fallbackDrawable]. */
    val image: String? = null,
    /** `R.drawable.*` для баннера; null — плейсхолдер по умолчанию в адаптере. */
    val fallbackDrawable: Int? = null,
) : FeedItem
