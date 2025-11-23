package com.example.ansteducation.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.example.ansteducation.api.PostApi
import com.example.ansteducation.dao.PostDao
import com.example.ansteducation.dto.Post
import com.example.ansteducation.entity.PostEntity
import com.example.ansteducation.entity.toEntity

class PostRepositoryImpl(private val dao: PostDao) : PostRepository {

    override suspend fun getAsync() {
        try {
            val posts = PostApi.service.getAll()
            dao.insert(posts.map(PostEntity::fromDto))
        } catch (e: Exception) {
            throw e
        }
    }

    override val data: LiveData<List<Post>> = dao.getAll().map {
        it.map { entity -> entity.toDto() }
    }

    override suspend fun likeByIdAsync(post: Post): Post {
        val postId = post.id
        val alreadyLiked = post.likedByMe

        try {
            val updatedPost = if (!alreadyLiked) {
                PostApi.service.likeById(postId)
            } else {
                PostApi.service.dislikeById(postId)
            }

            dao.insert(PostEntity.fromDto(updatedPost))
            return updatedPost

        } catch (e: Exception) {
            dao.likeById(postId)

            return post.copy(
                likedByMe = !alreadyLiked,
                likes = if (!alreadyLiked) post.likes + 1 else post.likes - 1
            )
        }
    }

    override suspend fun shareById(id: Long) {
        TODO("Not yet implemented")
    }


    override suspend fun removeByIdAsync(id: Long) {
        try {
            dao.removeById(id)
            PostApi.service.deleteById(id)
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun saveAsync(post: Post, update: Boolean): Post {
        return try {
            if (update) {
                val updatedPost = PostApi.service.save(post)
                dao.insert(PostEntity.fromDto(updatedPost))
                updatedPost
            } else {
                val newPost = PostApi.service.save(post)
                dao.insert(PostEntity.fromDto(newPost))
                newPost
            }
        } catch (e: Exception) {
            val localPost = if (post.id == 0L) {
                post.copy(id = System.currentTimeMillis())
            } else {
                post
            }
            dao.insert(PostEntity.fromDto(localPost))
            localPost
        }
    }

    override suspend fun hasData(): Boolean {
        return try {
            val count = dao.getCount()
            count > 0
        } catch (e: Exception) {
            false
        }
    }

    //    override suspend fun getImgNames(): List<String> {
//        val posts = getAsync()
//        return posts.map { it.authorAvatar }
//    }
}