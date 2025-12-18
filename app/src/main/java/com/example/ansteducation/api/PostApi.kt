package com.example.ansteducation.api

import com.example.ansteducation.BuildConfig
import com.example.ansteducation.auth.AppAuth
import com.example.ansteducation.dto.Media
import com.example.ansteducation.dto.Post
import com.example.ansteducation.dto.Token
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

private val client = OkHttpClient.Builder()
    .addInterceptor { chain ->
        AppAuth.getInstance().authState.value?.token?.let { token ->
            chain.proceed(
                chain.request().newBuilder()
                    .addHeader("Authorization", token)
                    .build()
            )
        } ?: chain.proceed(chain.request())
    }
    .connectTimeout(30, TimeUnit.SECONDS)
    .apply {
        if (BuildConfig.DEBUG) {
            addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
        }
    }
    .build()

private const val BASE_URL = "http://10.0.2.2:9999/api/"

private val retrofit = Retrofit.Builder()
    .client(client)
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()

private val authClient = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .apply {
        if (BuildConfig.DEBUG) {
            addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
        }
    }
    .build()

private val authorizedClient = OkHttpClient.Builder()
    .addInterceptor { chain ->
        AppAuth.getInstance().authState.value?.token?.let { token ->
            val newRequest = chain.request().newBuilder()
                .addHeader("Authorization", token)
                .build()
            chain.proceed(newRequest)
        } ?: chain.proceed(chain.request())
    }
    .connectTimeout(30, TimeUnit.SECONDS)
    .apply {
        if (BuildConfig.DEBUG) {
            addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
        }
    }
    .build()

private val authRetrofit = Retrofit.Builder()
    .client(authClient)
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()


private val authorizedRetrofit = Retrofit.Builder()
    .client(authorizedClient)
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()

interface AuthApi {
    @FormUrlEncoded
    @POST("users/authentication")
    suspend fun authenticate(
        @Field("login") login: String,
        @Field("pass") password: String
    ): Token
}

object ApiService {
    val auth: AuthApi by lazy {
        authRetrofit.create()
    }
}


interface PostApi {
    @GET("posts")
    suspend fun getAll(): List<Post>

    @POST("posts")
    suspend fun save(@Body post: Post): Post

    @DELETE("posts/{id}")
    suspend fun deleteById(@Path("id") id: Long)

    @POST("/api/posts/{id}/likes")
    suspend fun likeById(@Path("id") id: Long): Post

    @DELETE("/api/posts/{id}/likes")
    suspend fun dislikeById(@Path("id") id: Long): Post

    @GET("posts/{id}/newer")
    suspend fun getNewer(@Path("id") id: Long): List<Post>

    @Multipart
    @POST("media")
    suspend fun upload(@Part file: MultipartBody.Part): Media

    companion object {
        val service: PostApi by lazy {
            retrofit.create()
        }
    }
}
