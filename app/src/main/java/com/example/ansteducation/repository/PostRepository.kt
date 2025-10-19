package com.example.ansteducation.repository

import androidx.lifecycle.LiveData
import com.example.ansteducation.dto.Post
import okhttp3.Callback
import java.io.InputStream

interface PostRepository {
    fun get(slow: Boolean = false): List<Post>
    fun likeById (post: Post): Post
    fun shareById (id: Long)
    //fun viewById (id: Long)
    fun removeById (id: Long)
    fun save(post: Post): Post
    fun addVideoPost(post: Post)
    fun getAsync(callback: GetAllCallback, slow: Boolean)
    fun getImgNames(): List<String>

    interface GetAllCallback {
        fun onSuccess(posts: List<Post>) {}
        fun onError(e: Exception) {}
    }
    interface GetImageCallback {
        fun onSuccess(byteStream: InputStream) {}
        fun onError(e: Exception) {}
    }
}
