package com.example.ansteducation.api

import com.example.ansteducation.dto.Job
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface JobApi {
    /** Дипломный API: список работ пользователя — query, не `users/{id}/jobs` (часто 404). */
    @GET("jobs")
    suspend fun getUserJobsByAuthor(@Query("authorId") authorId: Long): List<Job>

    @GET("jobs")
    suspend fun getUserJobsByUserId(@Query("userId") userId: Long): List<Job>

    /** Старый контракт NMedia (если query не поддерживается). */
    @GET("users/{id}/jobs")
    suspend fun getUserJobsByPath(@Path("id") id: Long): List<Job>

    @GET("my/jobs")
    suspend fun getMyJobs(): List<Job>

    @POST("my/jobs")
    suspend fun save(@Body job: Job): Job

    @DELETE("my/jobs/{id}")
    suspend fun deleteById(@Path("id") id: Long)
}

