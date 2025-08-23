package com.example.ansteducation.dao

import com.example.ansteducation.dto.Post

interface PostDao {
    fun getAll(): List<Post>
    fun save(post: Post): Post
    fun likeById(id: Long)
    fun viewById (id: Long)
    fun shareById(id: Long)
    fun removeById(id: Long)
    fun addPostWithVideo()
}