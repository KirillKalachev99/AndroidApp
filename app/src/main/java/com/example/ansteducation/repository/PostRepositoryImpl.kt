package com.example.ansteducation.repository

import com.example.ansteducation.dto.Post
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class PostRepositoryImpl() : PostRepository {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    private val typeToken = TypeToken.getParameterized(List::class.java, Post::class.java).type

    companion object {
        private const val BASE_URL = "http://10.0.2.2:9999/"
        val jsonType = "application/json".toMediaType()
    }

    override fun get(slow: Boolean): List<Post> {
        val endpoint = if (slow) "api/slow/posts" else "api/posts"

        val request = Request.Builder()
            .url("${BASE_URL}$endpoint")
            .build()

        val call = client.newCall(request)
        val response = call.execute()
        val textBody = response.body?.string()

        return gson.fromJson(textBody, typeToken)
    }

    override fun getImgUrl(): String {
        val endpoint = "avatars/"
        val url = "${BASE_URL}$endpoint"
        return url
    }

    override fun getAttachmentUrl(): String {
        val endpoint = "images/"
        val url = "${BASE_URL}$endpoint"
        return url
    }

    override fun likeById(post: Post): Post {
        val postId = post.id.toString()
        val alreadyLiked = post.likedByMe
        val urlLike = "${BASE_URL}api/posts/$postId/likes"

        val request = if (!alreadyLiked) {
            Request.Builder()
                .url(urlLike)
                .post(gson.toJson(post).toRequestBody(jsonType))
                .build()
        } else {
            Request.Builder()
                .url(urlLike)
                .delete(gson.toJson(post).toRequestBody(jsonType))
                .build()
        }

        val call = client.newCall(request)
        val response = call.execute()
        val textBody = response.body?.string()

        return gson.fromJson(textBody, Post::class.java)
    }

    override fun shareById(id: Long) {
        TODO("Not yet implemented")
    }

// override fun viewById(id: Long) {
//
//}

    override fun removeById(id: Long) {
        TODO("Not yet implemented")
    }

    override fun save(post: Post): Post {
        val request = Request.Builder()
            .url("${BASE_URL}api/posts")
            .post(gson.toJson(post).toRequestBody(jsonType))
            .build()

        val call = client.newCall(request)

        val response = call.execute()

        val textBody = response.body?.string()

        return gson.fromJson(textBody, Post::class.java)
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