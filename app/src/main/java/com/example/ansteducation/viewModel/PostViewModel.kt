package com.example.ansteducation.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.ansteducation.dto.Post
import com.example.ansteducation.model.FeedModel
import com.example.ansteducation.repository.PostRepository
import com.example.ansteducation.repository.PostRepositoryImpl
import kotlin.concurrent.thread

private val empty = Post(
    id = 0,
    author = "",
    content = "",
    published = ""
)


class PostViewModel(application: Application) : AndroidViewModel(application) {

    val postWithVideo = Post(
        id = 5,
        author = "Me",
        published = "1111111",
        content = "Описание поста с видео",
        video = "https://rutube.ru/video/c6cc4d620b1d4338901770a44b3e82f4/"
    )

    private val repository: PostRepository = PostRepositoryImpl()
    private val _data: MutableLiveData<FeedModel> = MutableLiveData(FeedModel())
    val data: LiveData<FeedModel>
        get() = _data
    val edited = MutableLiveData(empty)
    private val _imgNames = MutableLiveData<List<String>>()
    val imgNames: LiveData<List<String>> = _imgNames


    init {
        load(slow = true)
        loadImgNames()
    }

    private fun loadImgNames() {
        thread {
            try {
                val names = repository.getImgNames()
                _imgNames.postValue(names)
            } catch (e: Exception) {
                _imgNames.postValue(emptyList())
            }
        }
    }

    fun like(post: Post) {
        thread {
            try {
                val updatedPost = repository.likeById(post)
                updatePostInList(updatedPost)
            } catch (e: Exception) {
                _data.postValue(FeedModel(error = true))
            }
        }
    }

    private fun updatePostInList(updatedPost: Post) {
        val currentState = _data.value
        if (currentState != null) {
            val updatedPosts = currentState.posts.map { post ->
                if (post.id == updatedPost.id) updatedPost else post
            }
            _data.postValue(currentState.copy(posts = updatedPosts))
        }
    }

    fun repost(id: Long) = repository.shareById(id)
   // fun view(id: Long) = repository.viewById(id)
    fun remove(id: Long) = repository.removeById(id)

    fun save(text: String) {
        thread {
            edited.value?.let {
                val content = text.trim()
                if (it.content != text) {
                    repository.save(it.copy(content = content, author = "Me"))
                }
            }
            edited.postValue(empty)
            load()
        }
    }

    fun load(slow: Boolean = false) {
       _data.value = FeedModel(loading = true)
        repository.getAsync(slow = slow, callback = object : PostRepository.GetAllCallback {
            override fun onSuccess(posts: List<Post>) {
                _data.postValue(FeedModel(posts = posts, empty = posts.isEmpty()))
            }

            override fun onError(e: Exception) {
                _data.postValue(FeedModel(error = true))
            }
        })
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun clear() {
        thread {
            edited.postValue(empty)
        }
    }

    fun addVideoPost(post: Post) {
        repository.addVideoPost(post)
    }

}