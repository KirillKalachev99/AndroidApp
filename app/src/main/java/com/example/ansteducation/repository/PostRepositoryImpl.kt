package com.example.ansteducation.repository

import com.example.ansteducation.api.PostApi
import com.example.ansteducation.dao.PostDao
import com.example.ansteducation.dto.Post
import com.example.ansteducation.entity.PostEntity
import com.example.ansteducation.entity.toEntity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PostRepositoryImpl() : PostRepository {


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
        val imgNames = posts.map {
            it.authorAvatar
        }
        return imgNames
    }

    override fun likeById(post: Post): Post? {
        val postId = post.id
        val alreadyLiked = post.likedByMe

        return if (!alreadyLiked) {
            PostApi.service.likeById(postId)
                .execute()
                .body()
        } else {
            PostApi.service.dislikeById(postId)
                .execute()
                .body()
        }
    }

    override fun shareById(id: Long) {
        TODO("Not yet implemented")
    }

    // override fun viewById(id: Long) {
//
//}
    override fun removeById(id: Long) {
        PostApi.service.deleteById(id)
            .execute()
    }

    override fun save(post: Post): Post? {
        return PostApi.service.save(post)
            .execute()
            .body()
    }

    override fun addVideoPost(post: Post) {
        TODO("Not yet implemented")
    }

    /*  override fun get(): LiveData<List<Post>> = dao.getAll().map { listPosts ->
        listPosts.map { entity ->
            entity.toDto()
        }
    }

    override fun likeById(id: Long) {
        dao.likeById(id)
    }

    override fun shareById(id: Long) {
        dao.shareById(id)
    }

    override fun viewById(id: Long) {
        dao.viewById(id)
    }

    override fun removeById(id: Long) {
        dao.removeById(id)
    }

    override fun save(post: Post) {
        dao.save(PostEntity.fromDto(post))
    }

    override fun addVideoPost(post: Post) {
        if (dao.exists(post.id) == 0) {
            dao.insert(post.toEntity())
        }
    } */
}