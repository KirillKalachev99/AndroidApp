package com.example.ansteducation.repository

import androidx.lifecycle.LiveData
import com.example.ansteducation.dto.Post

interface PostRepository {
    val data: LiveData<List<Post>>
    suspend fun getAsync()
    //suspend fun getImgNames(): List<String>
    suspend fun likeByIdAsync(post: Post): Post
    suspend fun shareById(id: Long)
    suspend fun removeByIdAsync(id: Long)
    suspend fun saveAsync(post: Post, update: Boolean = false): Post
    suspend fun hasData(): Boolean
}