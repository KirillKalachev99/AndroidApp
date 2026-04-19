package com.example.ansteducation.dto

data class Event(
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String? = null,
    val content: String,
    val published: String,
    val datetime: String,
    val type: EventType,
    val likedByMe: Boolean = false,
    val likes: Int = 0,
    val participantsIds: List<Long> = emptyList(),
    val speakerIds: List<Long> = emptyList(),
    val attachment: Attachment? = null,
    val link: String? = null,
    val ownedByMe: Boolean = false,
    val coords: Coordinates? = null,
    val authorJob: String? = null,
)

enum class EventType {
    OFFLINE,
    ONLINE,
}

