package com.example.ansteducation.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.example.ansteducation.dao.PostDao
import com.example.ansteducation.dto.Post
import com.example.ansteducation.entity.PostEntity
import com.example.ansteducation.entity.toEntity

class PostRepositoryImpl(private val dao: PostDao) : PostRepository {

    override fun get(): LiveData<List<Post>> = dao.getAll().map { listPosts ->
        listPosts.map { entity ->
            entity.toDto()
        }
    }

    override fun likeById(id: Long) {
        dao.likeById(id)
    }

    override fun shareById(id: Long) {
        dao.shareById(id)
    }

    override fun viewById(id: Long) {
        dao.viewById(id)
    }

    override fun removeById(id: Long) {
        dao.removeById(id)
    }

    override fun save(post: Post) {
        dao.save(PostEntity.fromDto(post))
    }

    override fun addVideoPost(post: Post) {
        if (dao.exists(post.id) == 0) {
            dao.insert(post.toEntity())
        }
    }
}