package com.example.ansteducation.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.ansteducation.dao.PostDao
import com.example.ansteducation.dto.Post

class PostRepositorySQLiteImpl(private val dao: PostDao) : PostRepository {

    private var posts = listOf<Post>()

    private val _data = MutableLiveData(posts)

    init {
        dao.addPostWithVideo()
        posts = dao.getAll()
        _data.value = posts
    }

    override fun get(): LiveData<List<Post>> = _data

    override fun likeById(id: Long) {
        dao.likeById(id)
        posts = posts.map {
            if (it.id != id) it else it.copy(
                likedByMe = !it.likedByMe,
                likes = if (it.likedByMe) it.likes - 1 else it.likes + 1
            )
        }
        posts = dao.getAll()
        _data.value = posts
    }

    override fun shareById(id: Long) {
        val post = posts.filter { it.id == id }
        if (!post.first().sharedByMe) {
            dao.shareById(id)
            posts = posts.map {
                if (it.id != id) it else it.copy(
                    sharedByMe = !it.sharedByMe,
                    shares = it.shares + 1
                )
            }
            _data.value = posts
        }
    }

    override fun viewById(id: Long) {
        dao.viewById(id)
        posts = posts.map {
            if (it.id != id) it else it.copy(
                viewedByMe = !it.viewedByMe,
                views = it.views + 1
            )
        }
        _data.value = posts
    }

    override fun removeById(id: Long) {
        posts = posts.filter { it.id != id }
        _data.value = posts
        dao.removeById(id)
    }

    override fun save(post: Post) {
        val id = post.id
        val saved = dao.save(post)
        posts = if (id == 0L) {
            listOf(saved) + posts
        } else {
            posts.map {
                if (it.id != id) it else saved
            }
        }
        _data.value = posts
    }
}