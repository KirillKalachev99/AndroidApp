package com.example.ansteducation.api

import com.example.ansteducation.dto.User
import retrofit2.http.GET

interface UserApi {
    @GET("users")
    suspend fun getAll(): List<User>
}

