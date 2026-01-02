package com.example.ansteducation.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.ansteducation.dao.PostDao
import com.example.ansteducation.entity.PostEntity

@Database(entities = [PostEntity::class], version = 7)
abstract class AppDb: RoomDatabase(){
    abstract fun postDao(): PostDao
}