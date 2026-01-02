package com.example.ansteducation.repository.di

import com.example.ansteducation.repository.PostRepository
import com.example.ansteducation.repository.PostRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
interface RepositoryModule {

    @Singleton
    @Binds

    fun bindsPostRepository(impl: PostRepositoryImpl): PostRepository
}