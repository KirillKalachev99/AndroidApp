package com.example.ansteducation.model

import com.example.ansteducation.dto.Post

data class FeedModel(
    val posts: List<Post> = emptyList(),
    val loading: Boolean = false,
    val error: Boolean = false,
    val empty: Boolean = false,
    var likedByMe: Boolean = false
)
