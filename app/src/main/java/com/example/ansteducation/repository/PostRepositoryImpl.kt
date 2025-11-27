package com.example.ansteducation.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.example.ansteducation.api.PostApi
import com.example.ansteducation.dao.PostDao
import com.example.ansteducation.dto.Post
import com.example.ansteducation.entity.PostEntity
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

        dao.likeById(postId)

        val locallyUpdatedPost = post.copy(
            likedByMe = !alreadyLiked,
            likes = if (!alreadyLiked) post.likes + 1 else post.likes - 1
        )

        try {
            val serverUpdatedPost = if (!alreadyLiked) {
                PostApi.service.likeById(postId)
            } else {
                PostApi.service.dislikeById(postId)
            }
            dao.insert(PostEntity.fromDto(serverUpdatedPost))
            return serverUpdatedPost

        } catch (_: Exception) {
            return locallyUpdatedPost
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

    @OptIn(DelicateCoroutinesApi::class)
    override suspend fun saveAsync(post: Post, update: Boolean): Post {
        val localId = System.currentTimeMillis()
        val localPost = post.copy(id = localId)
        dao.insert(PostEntity.fromDto(localPost))

        if (!update) {
            kotlinx.coroutines.GlobalScope.launch {
                try {
                    val serverPost = PostApi.service.save(post.copy(id = 0))
                    dao.removeById(localId)
                    dao.insert(PostEntity.fromDto(serverPost))

                } catch (e: Exception) {
                    Log.e("PostRepository", "Background sync failed: ${e.message}")
                    val failedPost = localPost.copy(id = -localId)
                    dao.removeById(localId)
                    dao.insert(PostEntity.fromDto(failedPost))
                }
            }
        }
        return localPost
    }


    override suspend fun hasData(): Boolean {
        return try {
            val count = dao.getCount()
            count > 0
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun updatePost(oldId: Long, newPost: Post) {
        dao.removeById(oldId)
        dao.insert(PostEntity.fromDto(newPost))
    }

    //    override suspend fun getImgNames(): List<String> {
//        val posts = getAsync()
//        return posts.map { it.authorAvatar }
//    }
}