package com.example.ansteducation.model

import com.example.ansteducation.dto.Post

data class FeedModelState(
    val loading: Boolean = false,
    val error: Boolean = false,
    val refreshing: Boolean = false,
)

data class FeedModel(
    val posts: List<Post> = emptyList(),
    val empty: Boolean = false,
)
