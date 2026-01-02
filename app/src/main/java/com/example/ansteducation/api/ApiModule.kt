package com.example.ansteducation.api

import com.example.ansteducation.BuildConfig
import com.example.ansteducation.auth.AppAuth
import com.example.ansteducation.di.AuthOkHttpClient
import com.example.ansteducation.di.AuthRetrofit
import com.example.ansteducation.di.PostOkHttpClient
import com.example.ansteducation.di.PostRetrofit
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class ApiModule {

    companion object {
        private const val BASE_URL = "http://10.0.2.2:9999/api/"
    }

    @Provides
    @Singleton
    @PostOkHttpClient
    fun provideOkhttp(appAuth: AppAuth): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                appAuth.authState.value?.token?.let { token ->
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

    @Provides
    @Singleton
    @AuthOkHttpClient
    fun provideAuthOkhttp(): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    })
                }
            }
            .build()

    @Provides
    @Singleton
    @PostRetrofit
    fun providePostRetrofit(@PostOkHttpClient client: OkHttpClient): Retrofit = Retrofit.Builder()
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(BASE_URL)
        .build()

    @Provides
    @Singleton
    @AuthRetrofit
    fun provideAuthRetrofit(@AuthOkHttpClient client: OkHttpClient): Retrofit = Retrofit.Builder()
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(BASE_URL)
        .build()

    @Provides
    @Singleton
    fun providePostApi(@PostRetrofit retrofit: Retrofit): PostApi = retrofit.create()

    @Provides
    @Singleton
    fun provideAuthApi(@AuthRetrofit retrofit: Retrofit): AuthApi = retrofit.create()
}