package com.example.ansteducation.repository

import androidx.lifecycle.LiveData
import com.example.ansteducation.dto.Post
import com.example.ansteducation.entity.PostEntity

interface PostRepository {
    fun get(): LiveData<List<Post>>
    fun likeById (id: Long)
    fun shareById (id: Long)
    fun viewById (id: Long)
    fun removeById (id: Long)
    fun save(post: Post)
    fun addVideoPost(post: Post)
}
