package com.example.ansteducation.repository

import androidx.lifecycle.LiveData
import com.example.ansteducation.dto.Post

interface PostRepository {
    fun get(slow: Boolean = false): List<Post>
    fun likeById (post: Post): Post
    fun shareById (id: Long)
    //fun viewById (id: Long)
    fun removeById (id: Long)
    fun save(post: Post): Post
    fun addVideoPost(post: Post)
}
