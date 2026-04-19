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
import com.example.ansteducation.dto.PostPayload
import com.example.ansteducation.dto.likeDisplayCount
import com.example.ansteducation.entity.PostEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
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
        pagingData.map(PostEntity::toDto)
            .insertSeparators { previous, _ ->
                if (previous?.id?.rem(5) == 0L) {
                    Ad(
                        id = Random.nextLong(),
                        image = "figma.jpg",
                        fallbackDrawable = null,
                    )
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
        val snapshot = post
        val alreadyLiked = post.likedByMe
        val baseCount = post.likeDisplayCount
        val optimistic = post.copy(
            likedByMe = !alreadyLiked,
            likes = if (!alreadyLiked) baseCount + 1 else maxOf(0, baseCount - 1),
            likeOwnerIds = emptyList(),
        )
        dao.insert(PostEntity.fromDto(optimistic))
        return try {
            val serverUpdatedPost = if (!alreadyLiked) {
                apiService.likeById(postId)
            } else {
                apiService.dislikeById(postId)
            }
            dao.insert(PostEntity.fromDto(serverUpdatedPost))
            serverUpdatedPost
        } catch (e: Exception) {
            dao.insert(PostEntity.fromDto(snapshot))
            throw e
        }
    }

    override suspend fun shareById(id: Long) {
        try {
            dao.shareById(id)
        } catch (_: Exception) {
        }
    }

    override suspend fun removeByIdAsync(id: Long) {
        try {
            dao.removeById(id)
            apiService.deleteById(id)
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun saveAsync(post: Post, update: Boolean, image: File?): Post = when {
        post.id < 0 -> retryFailedPost(post, image)
        update && post.id > 0 -> saveServerEdit(post, image)
        else -> saveNewPost(post, image)
    }

    private fun enrichAuthor(post: Post): Post {
        val uid = appAuth.authState.value?.id ?: return post
        return if (post.authorId == 0L) post.copy(authorId = uid) else post
    }

    private fun Post.toSavePayload(): PostPayload {
        val authId = appAuth.authState.value?.id ?: 0L
        val authorIdOut = when {
            authorId != 0L -> authorId
            authId != 0L -> authId
            else -> 0L
        }
        val idOut = when {
            id < 0 -> 0L
            id >= 1_000_000_000_000L -> 0L
            else -> id
        }
        return PostPayload(
            id = idOut,
            content = content,
            authorId = authorIdOut,
            author = author.takeIf { it.isNotBlank() },
            published = published.takeIf { it.isNotBlank() },
            mentionIds = mentionIds?.takeIf { it.isNotEmpty() },
            coords = coords,
            link = link?.takeIf { it.isNotBlank() },
            attachment = attachment,
            video = video?.takeIf { it.isNotBlank() },
        )
    }

    private suspend fun saveNewPost(post: Post, image: File?): Post {
        val localId = System.currentTimeMillis()
        val enriched = enrichAuthor(post)
        val localPost = enriched.copy(id = localId)
        dao.insert(PostEntity.fromDto(localPost))
        return try {
            val media = image?.let { upload(it) }
            val bodyBase = enriched.copy(id = 0)
            val toSend = media?.let {
                bodyBase.copy(
                    attachment = Attachment(it.id, AttachmentType.IMAGE),
                )
            } ?: bodyBase
            val payload = toSend.toSavePayload()
            check(payload.authorId != 0L) { "Войдите в аккаунт, чтобы опубликовать пост" }
            val serverPost = apiService.save(payload)
            updatePost(localId, serverPost)
            serverPost
        } catch (e: Exception) {
            updatePost(localId, localPost.copy(id = -localId))
            throw e
        }
    }

    private suspend fun saveServerEdit(post: Post, image: File?): Post {
        val enriched = enrichAuthor(post)
        val media = image?.let { upload(it) }
        val toSend = if (media != null) {
            enriched.copy(attachment = Attachment(media.id, AttachmentType.IMAGE))
        } else {
            enriched
        }
        val payload = toSend.toSavePayload()
        check(payload.authorId != 0L) { "Войдите в аккаунт, чтобы сохранить пост" }
        val serverPost = apiService.save(payload)
        updatePost(post.id, serverPost)
        return serverPost
    }

    private suspend fun retryFailedPost(post: Post, image: File?): Post {
        val failedId = post.id
        check(failedId < 0) { "Повторная отправка только для поста с ошибкой" }
        val localId = -failedId
        dao.removeById(failedId)
        val enriched = enrichAuthor(post.copy(id = localId))
        val sending = enriched.copy(id = localId)
        dao.insert(PostEntity.fromDto(sending))
        return try {
            val media = image?.let { upload(it) }
            val bodyBase = enriched.copy(id = 0)
            val toSend = media?.let {
                bodyBase.copy(attachment = Attachment(it.id, AttachmentType.IMAGE))
            } ?: bodyBase
            val payload = toSend.toSavePayload()
            check(payload.authorId != 0L) { "Войдите в аккаунт, чтобы опубликовать пост" }
            val serverPost = apiService.save(payload)
            updatePost(localId, serverPost)
            serverPost
        } catch (e: Exception) {
            updatePost(localId, sending.copy(id = -localId))
            throw e
        }
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

    override suspend fun getById(id: Long): Post = apiService.getById(id)

//    suspend fun clearCache() {
//        // dao.deleteAll()
//    }
}