package com.example.ansteducation.repository

import com.example.ansteducation.dto.Post
import kotlinx.coroutines.flow.Flow

interface PostRepository {
    val data: Flow<List<Post>>
    suspend fun getAsync()
    //suspend fun getImgNames(): List<String>
    suspend fun likeByIdAsync(post: Post): Post
    suspend fun shareById(id: Long)
    suspend fun removeByIdAsync(id: Long)
    suspend fun saveAsync(post: Post, update: Boolean = false): Post
    suspend fun hasData(): Boolean
    suspend fun updatePost(oldId: Long, newPost: Post)
}