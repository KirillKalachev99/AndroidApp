package com.example.ansteducation.repository

import com.example.ansteducation.api.PostApi
import com.example.ansteducation.dto.Post
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PostRepositoryImpl : PostRepository {

    override fun get(): List<Post> {
        return PostApi.service.getAll()
            .execute()
            .body()
            .orEmpty()
    }

    override fun getAsync(callback: PostRepository.GetAllCallback) {
        PostApi.service.getAll()
            .enqueue(object : Callback<List<Post>> {
                override fun onResponse(
                    call: Call<List<Post>>,
                    response: Response<List<Post>>
                ) {
                    if (!response.isSuccessful) {
                        callback.onError(RuntimeException(response.errorBody()?.string()))
                        return
                    }
                    callback.onSuccess(response.body().orEmpty())
                }

                override fun onFailure(call: Call<List<Post>>, throwable: Throwable) {
                    callback.onError(throwable)
                }
            })
    }

    override fun getImgNames(): List<String> {
        val posts = get()
        return posts.map { it.authorAvatar }
    }

    override fun likeById(post: Post): Post? {
        val postId = post.id
        val alreadyLiked = post.likedByMe

        return try {
            val response = if (!alreadyLiked) {
                PostApi.service.likeById(postId).execute()
            } else {
                PostApi.service.dislikeById(postId).execute()
            }

            if (response.isSuccessful) {
                response.body()
            } else {
                throw RuntimeException("HTTP error: ${response.code()} - ${response.message()}")
            }
        } catch (e: Exception) {
            throw e
        }
    }

    override fun likeByIdAsync(post: Post, callback: PostRepository.LikeCallback) {
        val postId = post.id
        val alreadyLiked = post.likedByMe

        val call = if (!alreadyLiked) {
            PostApi.service.likeById(postId)
        } else {
            PostApi.service.dislikeById(postId)
        }

        call.enqueue(object : Callback<Post> {
            override fun onResponse(call: Call<Post>, response: Response<Post>) {
                if (response.isSuccessful) {
                    response.body()?.let { callback.onSuccess(it) }
                        ?: callback.onError(RuntimeException("Empty response body"))
                } else {
                    callback.onError(RuntimeException("HTTP error: ${response.code()}"))
                }
            }

            override fun onFailure(call: Call<Post>, t: Throwable) {
                callback.onError(t as? Exception ?: Exception(t))
            }
        })
    }

    override fun shareById(id: Long) {
        TODO("Not yet implemented")
    }

    override fun removeById(id: Long) {
        PostApi.service.deleteById(id).execute()
    }

    override fun removeByIdAsync(id: Long, callback: PostRepository.RemoveCallback) {
        PostApi.service.deleteById(id).enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (response.isSuccessful) {
                    callback.onSuccess()
                } else {
                    callback.onError(RuntimeException("HTTP error: ${response.code()}"))
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                callback.onError(t as? Exception ?: Exception(t))
            }
        })
    }

    override fun save(post: Post): Post? {
        return try {
            val response = PostApi.service.save(post).execute()
            if (response.isSuccessful) {
                response.body()
            } else {
                throw RuntimeException("HTTP error: ${response.code()} - ${response.message()}")
            }
        } catch (e: Exception) {
            throw e
        }
    }

    override fun saveAsync(post: Post, callback: PostRepository.SaveCallback) {
        PostApi.service.save(post).enqueue(object : Callback<Post> {
            override fun onResponse(call: Call<Post>, response: Response<Post>) {
                if (response.isSuccessful) {
                    response.body()?.let { callback.onSuccess(it) }
                        ?: callback.onError(RuntimeException("Empty response body"))
                } else {
                    callback.onError(RuntimeException("HTTP error: ${response.code()}"))
                }
            }

            override fun onFailure(call: Call<Post>, t: Throwable) {
                callback.onError(t as? Exception ?: Exception(t))
            }
        })
    }

    override fun addVideoPost(post: Post) {
        TODO("Not yet implemented")
    }
}