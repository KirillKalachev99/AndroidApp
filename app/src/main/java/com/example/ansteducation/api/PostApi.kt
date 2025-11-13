package com.example.ansteducation.api

import com.example.ansteducation.BuildConfig
import com.example.ansteducation.dto.Post
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

private val client = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .apply {
        if (BuildConfig.DEBUG) {
            addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
        }
    }
    .build()

private const val BASE_URL = "http://10.0.2.2:9999/api/slow/"

private val retrofit = Retrofit.Builder()
    .client(client)
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()

interface PostApi {
    @GET("posts")
    fun getAll(): Call<List<Post>>

    @POST("posts")
    fun save(@Body post: Post): Call<Post>

    @DELETE("posts/{id}")
    fun deleteById(@Path("id") id: Long): Call<Unit>

    @POST("/api/posts/{id}/likes")
    fun likeById(@Path("id") id: Long): Call<Post>

    @DELETE("/api/posts/{id}/likes")
    fun dislikeById(@Path("id") id: Long): Call<Post>

    companion object {
        val service: PostApi by lazy {
            retrofit.create()
        }
    }
}