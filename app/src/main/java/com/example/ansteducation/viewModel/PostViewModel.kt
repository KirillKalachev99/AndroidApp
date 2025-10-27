package com.example.ansteducation.viewModel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.ansteducation.R
import com.example.ansteducation.dto.Post
import com.example.ansteducation.model.FeedModel
import com.example.ansteducation.repository.PostRepository
import com.example.ansteducation.repository.PostRepositoryImpl
import com.google.android.material.snackbar.Snackbar
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

    init {
        load()
    }

    fun like(post: Post) {
        val updatedPost = post.copy(
            likedByMe = !post.likedByMe,
            likes = if (!post.likedByMe) post.likes + 1 else post.likes - 1
        )
        updatePostInList(updatedPost)

        thread {
            try {
                val serverPost = repository.likeById(post)
                if (serverPost != null) {
                    updatePostInList(serverPost)
                }
                _data.value?.let { currentState ->
                    _data.postValue(
                        currentState.copy(
                            responseError = false,
                            responseErrorText = null
                        )
                    )
                }
            } catch (e: Exception) {
                updatePostInList(post)

                _data.value?.let { currentState ->
                    _data.postValue(
                        currentState.copy(
                            responseError = true,
                            responseErrorText = getApplication<Application>().getString(R.string.no_response_like)
                        )
                    )
                }
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

    fun remove(id: Long) {
        val currentState = _data.value
        if (currentState != null) {
            val updatedPosts = currentState.posts.filter { it.id != id }
            _data.value = currentState.copy(posts = updatedPosts)
        }
        thread {
            try {
                repository.removeById(id)
                load()
            } catch (e: Exception) {
                _data.postValue(FeedModel(error = true))
            }
        }
    }

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

    fun load() {
        thread {
            _data.postValue(FeedModel(loading = true))
            try {
                val posts = repository.get()
                _data.postValue(FeedModel(posts = posts, empty = posts.isEmpty()))
            } catch (_: Exception) {
                _data.postValue(FeedModel(error = true))
            }
        }
    }

    fun errorShown() {
        // Сбрасываем только флаги ошибки, сохраняя посты
        _data.value?.let { currentState ->
            _data.postValue(
                currentState.copy(
                    responseError = false,
                    responseErrorText = null
                )
            )
        }
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