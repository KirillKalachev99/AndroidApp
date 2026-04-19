package com.example.ansteducation.api

import com.example.ansteducation.dto.Event
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface EventApi {
    @GET("events")
    suspend fun getAll(): List<Event>

    @GET("events/{id}")
    suspend fun getById(@Path("id") id: Long): Event

    @POST("events")
    suspend fun save(@Body event: Event): Event

    @DELETE("events/{id}")
    suspend fun deleteById(@Path("id") id: Long)

    @POST("events/{id}/likes")
    suspend fun likeById(@Path("id") id: Long): Event

    @DELETE("events/{id}/likes")
    suspend fun dislikeById(@Path("id") id: Long): Event
}

