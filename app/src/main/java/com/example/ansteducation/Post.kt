package com.example.ansteducation

data class Post(
    val id: String,
    val author: String,
    val published: String,
    val content: String,
    var likes: Int = 0,
    var shares: Int = 0,
    var views: Int = 1,
    var sharedByMe: Boolean = false,
    var liked: Boolean = false,

)
