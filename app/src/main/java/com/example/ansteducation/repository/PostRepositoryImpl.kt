package com.example.ansteducation.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.insertSeparators
import androidx.paging.map
import com.example.ansteducation.api.PostApi
import com.example.ansteducation.auth.AppAuth
import com.example.ansteducation.dao.PostDao
import com.example.ansteducation.dao.PostRemoteKeyDao
import com.example.ansteducation.db.AppDb
import com.example.ansteducation.dto.Ad
import com.example.ansteducation.dto.Attachment
import com.example.ansteducation.dto.AttachmentType
import com.example.ansteducation.dto.FeedItem
import com.example.ansteducation.dto.Media
import com.example.ansteducation.dto.Post
import com.example.ansteducation.entity.PostEntity
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class PostRepositoryImpl @Inject constructor(
    private val dao: PostDao,
    private val apiService: PostApi,
    private val appAuth: AppAuth,
    postRemoteKeyDao: PostRemoteKeyDao,
    appDb: AppDb,
) : PostRepository {

    private val mutex = Mutex()
    private var cachedNewPosts: List<Post> = emptyList()

    @OptIn(ExperimentalPagingApi::class, ExperimentalCoroutinesApi::class)
    override val data: Flow<PagingData<FeedItem>> = appAuth.authState.flatMapLatest { _ ->
        Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false,
                initialLoadSize = 20
            ),
            pagingSourceFactory = {
                dao.getPagingSource()
            },
            remoteMediator = PostRemoteMediator(
                apiService = apiService,
                postDao = dao,
                postRemoteKeyDao = postRemoteKeyDao,
                appDb = appDb,
            )
        ).flow
    }.map { pagingData ->
        pagingData.map((PostEntity::toDto))
            .insertSeparators { previous, _ ->
                if (previous?.id?.rem(5) == 0L) {
                    Ad(Random.nextLong(), "figma.jpg")
                } else {
                    null
                }
            }
    }

    override suspend fun getAsync() {
        try {
            val posts = apiService.getLatest(10).body()
            dao.insert(posts?.map(PostEntity::fromDto) ?: emptyList())
        } catch (e: Exception) {
            throw e
        }
    }

    override fun getNewer(id: Long): Flow<Int> = flow {
        while (true) {
            delay(10_000)
            try {
                val newPosts = apiService.getNewer(id)
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
                val posts = apiService.getLatest(10).body() ?: emptyList()
                dao.insert(posts.map(PostEntity::fromDto))
            }
        }
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
                apiService.likeById(postId)
            } else {
                apiService.dislikeById(postId)
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
            apiService.deleteById(id)
        } catch (e: Exception) {
            throw e
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override suspend fun saveAsync(post: Post, update: Boolean, image: File?): Post {
        val localId = System.currentTimeMillis()
        val localPost = post.copy(id = localId)

        dao.insert(PostEntity.fromDto(localPost))

        if (!update) {
            supervisorScope {
                launch {
                    try {
                        val media = image?.let { upload(it) }
                        val postWithAttachment = media?.let {
                            post.copy(
                                attachment = Attachment(
                                    it.id,
                                    AttachmentType.IMAGE
                                ),
                                id = 0
                            )
                        } ?: post.copy(id = 0)

                        val serverPost = apiService.save(postWithAttachment)

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

    private suspend fun upload(file: File): Media =
        apiService.upload(
            MultipartBody.Part.createFormData(
                "file",
                file.name,
                file.asRequestBody()
            )
        )

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

//    suspend fun clearCache() {
//        // dao.deleteAll()
//    }
}