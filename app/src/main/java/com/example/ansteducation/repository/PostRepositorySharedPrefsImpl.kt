/* package com.example.ansteducation.repository

import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.ansteducation.dto.Post
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PostRepositorySharedPrefsImpl(context: Context) : PostRepository {

    val prefs = context.getSharedPreferences("repo",Context.MODE_PRIVATE)

    private var posts = listOf<Post>()
        set(value) {
            field = value
            sync()
        }

    private var nextId = posts.maxOfOrNull { it.id }?.plus(1) ?: 1L

    private val _data = MutableLiveData(posts)

    init {
        prefs.getString(KEY_POSTS, null)?.let {
            posts = gson.fromJson(it, type)
            nextId = posts.maxOfOrNull { it.id }?.inc() ?: 1
            _data.value = posts
        }
    }

    private fun sync() {
        prefs.edit {
            putString(KEY_POSTS, gson.toJson(posts))
        }
    }

    override fun get(): LiveData<List<Post>> = _data

    override fun likeById(id: Long) {
        posts = posts.map { post ->
            if (post.id == id) {
                post.copy(
                    likedByMe = !post.likedByMe,
                    likes = if (post.likedByMe) {
                        post.likes - 1
                    } else {
                        post.likes + 1
                    }
                )
            } else post
        }
        _data.value = posts
    }

    override fun shareById(id: Long) {
        posts = posts.map { post ->
            if (post.id == id && !post.sharedByMe) {
                post.copy(
                    sharedByMe = !post.sharedByMe,
                    shares = post.shares + 1
                )
            } else post
        }
        _data.value = posts
    }

    override fun viewById(id: Long) {
        posts = posts.map { post ->
            if (post.id == id && !post.viewedByMe) {
                post.copy(
                    viewedByMe = true,
                    views = post.views + 1
                )
            } else post
        }
        _data.postValue(posts)
    }

    override fun removeById(id: Long) {
        posts = posts.filter { it.id != id }
        _data.value = posts
    }

    override fun save(post: Post) {
        posts = if (post.id == 0L) {
            listOf(post.copy(id = nextId++, author = "Me", published = "now")) + posts
        } else {
            posts.map { if (it.id != post.id) it else it.copy(content = post.content) }
        }
        _data.value = posts
    }

    override fun addVideoPost(post: Post) {
        TODO("Not yet implemented")
    }

    companion object {
        private const val KEY_POSTS = "posts"
        private val gson = Gson()
        private val type = TypeToken.getParameterized(List::class.java, Post::class.java).type
    }
} */