package com.example.ansteducation.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.ansteducation.db.AppDb
import com.example.ansteducation.dto.Post
import com.example.ansteducation.model.FeedModel
import com.example.ansteducation.model.FeedModelState
import com.example.ansteducation.repository.PostRepository
import com.example.ansteducation.repository.PostRepositoryImpl
import com.example.ansteducation.util.SingleLiveEvent
import kotlinx.coroutines.launch
import java.util.Date

private val empty = Post(
    id = 0,
    author = "",
    content = "",
    published = ""
)

class PostViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PostRepository = PostRepositoryImpl(
        AppDb.getInstance(application).postDao()
    )
    val data: LiveData<FeedModel> = repository.data.map {
        FeedModel(
            posts = it,
            empty = it.isEmpty(),
        )
    }

    private val _state = MutableLiveData(FeedModelState())
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    val state: LiveData<FeedModelState>
        get() = _state
    val edited = MutableLiveData(empty)

    init {
        load()
    }

    fun like(post: Post) {
        viewModelScope.launch {
            repository.likeByIdAsync(post)
            // TODO:
        }
    }


    fun repost(id: Long) {
        viewModelScope.launch {
            repository.shareById(id)
            // TODO:
        }
    }

    fun remove(id: Long) {
        viewModelScope.launch {
            repository.removeByIdAsync(id)
            // TODO:
        }
    }


    fun save(content: String) {
        viewModelScope.launch {
            try {
                val currentEdited = edited.value ?: empty

                if (currentEdited.id == 0L) {
                    val newPost = Post(
                        id = 0,
                        author = "Me",
                        content = content,
                        published = Date().toString(),
                        likes = 0,
                        shares = 0,
                        views = 0,
                        likedByMe = false,
                        sharedByMe = false,
                        viewedByMe = false
                    )
                    repository.saveAsync(newPost, update = false)
                } else {
                    val updatedPost = currentEdited.copy(content = content)
                    repository.saveAsync(updatedPost, update = true)
                }

                _postCreated.value = Unit
                edited.value = empty
                load(true)
            } catch (e: Exception) {
                throw e
            }
        }
    }
    fun load(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            if (forceRefresh) {
                _state.value = FeedModelState(refreshing = true, error = false)
            } else {
                _state.value = FeedModelState(refreshing = true, loading = true, error = false)
            }
            try {
                repository.getAsync()
                _state.value = FeedModelState()
            } catch (e: Exception) {
                val hasDataAfterError = repository.hasData()
                if (!hasDataAfterError) {
                    _state.value = FeedModelState(error = true)
                } else {
                    _state.value = FeedModelState()
                }
            }
        }
    }


    fun edit(post: Post) {
        edited.value = post
    }

    fun clear() {
        edited.value = empty
    }

}
