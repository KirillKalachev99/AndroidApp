package com.example.ansteducation.api

import com.example.ansteducation.dto.Job
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface JobApi {
    @GET("users/{id}/jobs")
    suspend fun getUserJobs(@Path("id") id: Long): List<Job>

    @GET("my/jobs")
    suspend fun getMyJobs(): List<Job>

    @POST("my/jobs")
    suspend fun save(@Body job: Job): Job

    @DELETE("my/jobs/{id}")
    suspend fun deleteById(@Path("id") id: Long)
}

