package com.example.ansteducation.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.ansteducation.R
import com.example.ansteducation.dto.Post
import com.example.ansteducation.model.FeedModel
import com.example.ansteducation.repository.PostRepository
import com.example.ansteducation.repository.PostRepositoryImpl

private val empty = Post(
    id = 0,
    author = "",
    content = "",
    published = ""
)

class PostViewModel(application: Application) : AndroidViewModel(application) {

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

        repository.likeByIdAsync(post, object : PostRepository.LikeCallback {
            override fun onSuccess(serverPost: Post) {
                updatePostInList(serverPost)
                _data.postValue(
                    _data.value?.copy(
                        responseError = false,
                        responseErrorText = null
                    )
                )
            }

            override fun onError(throwable: Throwable) {
                updatePostInList(post)
                _data.postValue(
                    _data.value?.copy(
                        responseError = true,
                        responseErrorText = getApplication<Application>().getString(R.string.no_response_like)
                    )
                )
            }
        })
    }

    private fun updatePostInList(updatedPost: Post) {
        val currentState = _data.value
        if (currentState != null) {
            val updatedPosts = currentState.posts.map { post ->
                if (post.id == updatedPost.id) updatedPost else post
            }
            _data.value = currentState.copy(posts = updatedPosts)
        }
    }

    fun repost(id: Long) = repository.shareById(id)

    fun remove(id: Long) {
        val currentState = _data.value
        if (currentState != null) {
            val updatedPosts = currentState.posts.filter { it.id != id }
            _data.value = currentState.copy(posts = updatedPosts)
        }

        repository.removeByIdAsync(id, object : PostRepository.RemoveCallback {
            override fun onSuccess() {
                load()
            }

            override fun onError(throwable: Throwable) {
                _data.postValue(FeedModel(error = true))
            }
        })
    }

    fun save(text: String) {
        edited.value?.let { post ->
            val content = text.trim()
            if (post.content != text) {
                repository.saveAsync(post.copy(content = content, author = "Me"), object : PostRepository.SaveCallback {
                    override fun onSuccess(savedPost: Post) {
                        edited.value = empty
                        load()
                    }

                    override fun onError(throwable: Throwable) {
                        edited.value = empty
                        load()
                    }
                })
            } else {
                edited.value = empty
            }
        } ?: run {
            edited.value = empty
        }
    }

    fun load() {
        _data.value = FeedModel(loading = true)

        repository.getAsync(object : PostRepository.GetAllCallback {
            override fun onSuccess(posts: List<Post>) {
                _data.value = FeedModel(posts = posts, empty = posts.isEmpty())
            }

            override fun onError(throwable: Throwable) {
                _data.value = FeedModel(error = true)
            }
        })
    }

    fun errorShown() {
        _data.postValue(
            _data.value?.copy(
                responseError = false,
                responseErrorText = null
            )
        )
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun clear() {
        edited.value = empty
    }
}