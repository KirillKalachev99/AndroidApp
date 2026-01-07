package com.example.ansteducation.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.ansteducation.api.PostApi
import com.example.ansteducation.dao.PostDao
import com.example.ansteducation.dao.PostRemoteKeyDao
import com.example.ansteducation.db.AppDb
import com.example.ansteducation.entity.PostEntity
import com.example.ansteducation.entity.PostRemoteKeyEntity
import retrofit2.HttpException
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class PostRemoteMediator(
    private val apiService: PostApi,
    private val postDao: PostDao,
    private val postRemoteKeyDao: PostRemoteKeyDao,
    private val appDb: AppDb,
) : RemoteMediator<Int, PostEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostEntity>
    ): MediatorResult {
        return try {
            val result = when (loadType) {
                LoadType.REFRESH -> {
                    val firstItem = state.firstItemOrNull()
                    val newestPostId = firstItem?.id

                    if (newestPostId != null) {
                        apiService.getAfter(newestPostId, state.config.pageSize)
                    } else {
                        apiService.getLatest(state.config.pageSize)
                    }
                }

                LoadType.APPEND -> {
                    val beforeKey = postRemoteKeyDao.getByType(PostRemoteKeyEntity.KeyType.BEFORE)
                    val oldestPostId = beforeKey?.key ?: return MediatorResult.Success(endOfPaginationReached = true)
                    apiService.getBefore(oldestPostId, state.config.pageSize)
                }

                LoadType.PREPEND -> {
                    return MediatorResult.Success(endOfPaginationReached = true)
                }
            }

            if (!result.isSuccessful) {
                throw HttpException(result)
            }

            val body = result.body() ?: emptyList()

            if (body.isEmpty()) {
                return MediatorResult.Success(endOfPaginationReached = true)
            }

            appDb.withTransaction {
                when (loadType) {
                    LoadType.REFRESH -> {
                        val newestReceivedPostId = body.first().id

                        val existingAfterKey = postRemoteKeyDao.getByType(PostRemoteKeyEntity.KeyType.AFTER)

                        if (existingAfterKey == null || newestReceivedPostId > existingAfterKey.key) {
                            postRemoteKeyDao.upsert(
                                PostRemoteKeyEntity(
                                    type = PostRemoteKeyEntity.KeyType.AFTER,
                                    key = newestReceivedPostId
                                )
                            )
                        }

                        if (existingAfterKey == null) {
                            postRemoteKeyDao.upsert(
                                PostRemoteKeyEntity(
                                    type = PostRemoteKeyEntity.KeyType.BEFORE,
                                    key = body.last().id
                                )
                            )
                        }
                    }

                    LoadType.APPEND -> {
                        val oldestReceivedPostId = body.last().id
                        postRemoteKeyDao.upsert(
                            PostRemoteKeyEntity(
                                type = PostRemoteKeyEntity.KeyType.BEFORE,
                                key = oldestReceivedPostId
                            )
                        )
                    }

                    LoadType.PREPEND -> {}
                }

                postDao.insert(body.map { PostEntity.fromDto(it) })
            }

            return MediatorResult.Success(
                endOfPaginationReached = body.isEmpty()
            )
        } catch (e: IOException) {
            return MediatorResult.Error(e)
        } catch (e: HttpException) {
            return MediatorResult.Error(e)
        } catch (e: Exception) {
            return MediatorResult.Error(e)
        }
    }

    override suspend fun initialize(): InitializeAction {
        val hasData = postDao.getCount() > 0
        return if (hasData) {
            InitializeAction.SKIP_INITIAL_REFRESH
        } else {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        }
    }
}