package com.example.ansteducation.repository

import com.example.ansteducation.api.PostApi
import com.example.ansteducation.dao.PostDao
import com.example.ansteducation.dto.Post
import com.example.ansteducation.entity.PostEntity
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.collections.map

class PostRepositoryImpl(private val dao: PostDao) : PostRepository {

    private val mutex = Mutex()
    private var cachedNewPosts: List<Post> = emptyList()

    override suspend fun getAsync() {
        try {
            val posts = PostApi.service.getAll()
            dao.insert(posts.map(PostEntity::fromDto))
        } catch (e: Exception) {
            throw e
        }
    }

    override fun getNewer(id: Long): Flow<Int> = flow {
        while (true) {
            delay(10_000)
            try {
                val newPosts = PostApi.service.getNewer(id)
                mutex.withLock {
                    cachedNewPosts = newPosts
                }
                emit(newPosts.size)
                println("DEBUG: Found and cached ${newPosts.size} new posts")
            } catch (_: Exception) {
                emit(0)
            }
        }
    }.catch { _ ->
        emit(0)
    }

    override suspend fun addNewer() {
        mutex.withLock {
            if (cachedNewPosts.isNotEmpty()) {
                dao.insert(cachedNewPosts.map(PostEntity::fromDto))
                cachedNewPosts = emptyList()
            } else {
                val posts = PostApi.service.getAll()
                dao.insert(posts.map(PostEntity::fromDto))
            }
        }
    }

    override val data = dao.getAll().map { it.map { it.toDto() } }

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
            return serverUpdatedPost

        } catch (_: Exception) {
            dao.likeById(postId)
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
            supervisorScope {
                launch {
                    try {
                        val serverPost = PostApi.service.save(post.copy(id = 0))

                        updatePost(localId, serverPost)

                    } catch (_: Exception) {
                        val failedPost = localPost.copy(id = -localId)
                        updatePost(localId, failedPost)
                    }
                }
            }
        }
        return localPost
    }

    override suspend fun hasData(): Boolean {
        return try {
            val count = dao.getCount()
            count > 0
        } catch (_: Exception) {
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