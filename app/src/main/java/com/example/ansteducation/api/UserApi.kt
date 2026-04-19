package com.example.ansteducation.api

import com.example.ansteducation.dto.User
import retrofit2.http.GET
import retrofit2.http.Path

interface UserApi {
    @GET("users")
    suspend fun getAll(): List<User>

    @GET("users/{id}")
    suspend fun getById(@Path("id") id: Long): User
}

