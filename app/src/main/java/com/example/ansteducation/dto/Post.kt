package com.example.ansteducation.dto

data class Post(
    val id: Long,
    val author: String,
    val published: String,
    val content: String,
    val likes: Int = 0,
    val shares: Int = 0,
    val views: Int = 0,
    val sharedByMe: Boolean = false,
    val liked: Boolean = false,
    var viewedByMe: Boolean = false
)
