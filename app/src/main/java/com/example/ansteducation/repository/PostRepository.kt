package com.example.ansteducation.repository

import androidx.lifecycle.LiveData
import com.example.ansteducation.dto.Post

interface PostRepository {
    fun get(): LiveData<List<Post>>
    fun likeById (id: Long)
    fun repostById (id: Long)
    fun viewById (id: Long)
    fun removeById (id: Long)
    fun save(post: Post)
    fun getPostById(id: Long): List<Post>
}
