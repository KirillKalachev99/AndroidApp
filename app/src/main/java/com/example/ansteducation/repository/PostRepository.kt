package com.example.ansteducation.repository

import com.example.ansteducation.dto.Post

interface PostRepository {
    fun get(): List<Post>
    fun getAsync(callback: GetAllCallback)
    fun getImgNames(): List<String>
    fun likeById(post: Post): Post?
    fun likeByIdAsync(post: Post, callback: LikeCallback)
    fun shareById(id: Long)
    fun removeById(id: Long)
    fun removeByIdAsync(id: Long, callback: RemoveCallback)
    fun save(post: Post): Post?
    fun saveAsync(post: Post, callback: SaveCallback)
    fun addVideoPost(post: Post)

    interface GetAllCallback {
        fun onSuccess(posts: List<Post>)
        fun onError(throwable: Throwable)
    }

    interface LikeCallback {
        fun onSuccess(post: Post)
        fun onError(throwable: Throwable)
    }

    interface RemoveCallback {
        fun onSuccess()
        fun onError(throwable: Throwable)
    }

    interface SaveCallback {
        fun onSuccess(post: Post)
        fun onError(throwable: Throwable)
    }
}