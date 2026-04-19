package com.example.ansteducation.api


import com.example.ansteducation.dto.Media
import com.example.ansteducation.dto.Post
import com.example.ansteducation.dto.CommentPayload
import com.example.ansteducation.dto.PostComment
import com.example.ansteducation.dto.PostPayload
import com.example.ansteducation.dto.PushToken
import com.example.ansteducation.dto.Token
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query


interface AuthApi {
    @FormUrlEncoded
    @POST("users/authentication")
    suspend fun authenticate(
        @Field("login") login: String,
        @Field("pass") password: String
    ): Token

    @Multipart
    @POST("users/registration")
    suspend fun register(
        @Part("login") login: String,
        @Part("pass") password: String,
        @Part("name") name: String,
        @Part avatar: MultipartBody.Part?
    ): Token
}


interface PostApi {
    @POST("posts")
    suspend fun save(@Body body: PostPayload): Post

    @DELETE("posts/{id}")
    suspend fun deleteById(@Path("id") id: Long)

    @POST("posts/{id}/likes")
    suspend fun likeById(@Path("id") id: Long): Post

    @DELETE("posts/{id}/likes")
    suspend fun dislikeById(@Path("id") id: Long): Post

    @GET("posts/{id}/newer")
    suspend fun getNewer(@Path("id") id: Long): List<Post>

    @Multipart
    @POST("media")
    suspend fun upload(@Part file: MultipartBody.Part): Media

    @POST("users/push-tokens")
    suspend fun sendPushToken(@Body token: PushToken)

    @GET("posts/latest")
    suspend fun getLatest(@Query("count") count: Int): Response<List<Post>>

    @GET("posts/{id}/before")
    suspend fun getBefore(@Path("id") id: Long, @Query("count") count: Int): Response<List<Post>>

    @GET("posts/{id}/after")
    suspend fun getAfter(@Path("id") id: Long, @Query("count") count: Int): Response<List<Post>>

    /** Дипломный сервер: стена пользователя — список постов по автору. */
    @GET("posts")
    suspend fun getPostsByAuthor(@Query("authorId") authorId: Long): List<Post>

    @GET("posts/{id}")
    suspend fun getById(@Path("id") id: Long): Post

    @GET("posts/{id}/comments")
    suspend fun getComments(@Path("id") postId: Long): List<PostComment>

    @POST("posts/{id}/comments")
    suspend fun addComment(@Path("id") postId: Long, @Body body: CommentPayload): PostComment
}
