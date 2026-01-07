package com.example.ansteducation.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.ansteducation.dao.PostDao
import com.example.ansteducation.dao.PostRemoteKeyDao
import com.example.ansteducation.entity.PostEntity
import com.example.ansteducation.entity.PostRemoteKeyEntity

@Database(entities = [PostEntity::class, PostRemoteKeyEntity::class], version = 7)
abstract class AppDb: RoomDatabase(){
    abstract fun postDao(): PostDao
    abstract fun postRemoteKeyDao(): PostRemoteKeyDao
}