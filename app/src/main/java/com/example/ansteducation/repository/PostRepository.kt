package com.example.ansteducation.repository

import androidx.lifecycle.LiveData
import com.example.ansteducation.dto.Post

interface PostRepository {
    fun get(): LiveData<Post>
    fun like()
    fun repost()
    fun view()
}