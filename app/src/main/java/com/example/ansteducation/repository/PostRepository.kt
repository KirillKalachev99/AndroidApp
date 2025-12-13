package com.example.ansteducation.repository

import com.example.ansteducation.dto.Post
import kotlinx.coroutines.flow.Flow
import java.io.File

interface PostRepository {
    val data: Flow<List<Post>>
    suspend fun getAsync()
    //suspend fun getImgNames(): List<String>
    fun getNewer(id: Long): Flow<Int>
    suspend fun addNewer()
    suspend fun likeByIdAsync(post: Post): Post
    suspend fun shareById(id: Long)
    suspend fun removeByIdAsync(id: Long)
    suspend fun saveAsync(post: Post, update: Boolean = false, image: File?): Post
    suspend fun hasData(): Boolean
    suspend fun updatePost(oldId: Long, newPost: Post)

}