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
                    val maxKey = postRemoteKeyDao.max()

                    if (maxKey != null) {
                        apiService.getAfter(maxKey, state.config.pageSize)
                    } else {
                        apiService.getLatest(state.config.pageSize)
                    }
                }

                LoadType.APPEND -> {
                    val minKey = postRemoteKeyDao.min()
                    val id = minKey ?: return MediatorResult.Success(endOfPaginationReached = true)
                    apiService.getBefore(id, state.config.pageSize)
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
                        val existingMaxKey = postRemoteKeyDao.max()
                        val newestReceivedPostId = body.first().id

                        val newMaxKey = if (existingMaxKey != null) {
                            maxOf(existingMaxKey, newestReceivedPostId)
                        } else {
                            newestReceivedPostId
                        }

                        val existingMinKey = postRemoteKeyDao.min()
                        val oldestReceivedPostId = body.last().id

                        val newMinKey = if (existingMinKey != null) {
                            minOf(existingMinKey, oldestReceivedPostId)
                        } else {
                            oldestReceivedPostId
                        }

                        postRemoteKeyDao.insert(
                            PostRemoteKeyEntity(
                                type = PostRemoteKeyEntity.KeyType.AFTER,
                                key = newMaxKey
                            )
                        )

                        postRemoteKeyDao.insert(
                            PostRemoteKeyEntity(
                                type = PostRemoteKeyEntity.KeyType.BEFORE,
                                key = newMinKey
                            )
                        )
                    }

                    LoadType.APPEND -> {
                        val existingMinKey = postRemoteKeyDao.min()
                        val oldestReceivedPostId = body.last().id

                        val newMinKey = if (existingMinKey != null) {
                            minOf(existingMinKey, oldestReceivedPostId)
                        } else {
                            oldestReceivedPostId
                        }

                        postRemoteKeyDao.insert(
                            PostRemoteKeyEntity(
                                type = PostRemoteKeyEntity.KeyType.BEFORE,
                                key = newMinKey
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
        val hasKeys = postRemoteKeyDao.max() != null

        return if (hasKeys) {
            InitializeAction.SKIP_INITIAL_REFRESH
        } else {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        }
    }
}