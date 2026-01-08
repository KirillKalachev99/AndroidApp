package com.example.ansteducation.viewModel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.example.ansteducation.auth.AppAuth
import com.example.ansteducation.dto.Post
import com.example.ansteducation.model.FeedModelState
import com.example.ansteducation.model.PhotoModel
import com.example.ansteducation.repository.PostRepository
import com.example.ansteducation.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

private val empty = Post(
    id = 0,
    author = "",
    authorId = 0,
    content = "",
    published = ""
)

@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: PostRepository,
    private val appAuth: AppAuth
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val data: Flow<PagingData<Post>> = appAuth.authState.flatMapLatest { token ->
        repository.data
            .map { pagingData ->
                pagingData.map { post ->
                    post.copy(ownedByMe = post.authorId == token?.id)
                }
            }
    }
        .flowOn(Dispatchers.Default)
        .cachedIn(viewModelScope)

    private val _state = MutableLiveData(FeedModelState())
    private val _postCreated = SingleLiveEvent<Unit>()

    val state: LiveData<FeedModelState>
        get() = _state
    val edited = MutableLiveData(empty)
    private var isSaving = false

    private val _photo = MutableLiveData<PhotoModel?>(null)
    val photo: LiveData<PhotoModel?>
        get() = _photo

    init {
        viewModelScope.launch {
            val hasData = repository.hasData()
            if (!hasData) {
                _state.value = FeedModelState(loading = true)
            } else {
                _state.value = FeedModelState()
            }
        }
    }

    fun refreshData() {
        _state.value = FeedModelState(refreshing = true)
    }

    fun onDataLoaded() {
        _state.value = FeedModelState()
    }

    fun onDataLoadError() {
        _state.value = FeedModelState(error = true)
    }

    fun addNewer() {
        viewModelScope.launch {
            try {
                _state.value = FeedModelState(refreshing = true)
            } catch (e: Exception) {
                Log.e("PostViewModel", "Error adding newer: ${e.message}")
                _state.value = FeedModelState(error = true)
            }
        }
    }

    fun like(post: Post) {
        viewModelScope.launch {
            try {
                repository.likeByIdAsync(post)
            } catch (e: Exception) {
                Log.e("PostViewModel", "Like error: ${e.message}")
            }
        }
    }

    fun repost(id: Long) {
        viewModelScope.launch {
            try {
                repository.shareById(id)
            } catch (e: Exception) {
                Log.e("PostViewModel", "Repost error: ${e.message}")
            }
        }
    }

    fun remove(id: Long) {
        viewModelScope.launch {
            try {
                repository.removeByIdAsync(id)
            } catch (e: Exception) {
                Log.e("PostViewModel", "Remove error: ${e.message}")
            }
        }
    }

    fun save(content: String) {
        if (isSaving) {
            return
        }
        isSaving = true
        viewModelScope.launch {
            try {
                val currentEdited = edited.value ?: empty

                if (currentEdited.id == 0L) {
                    val newPost = Post(
                        id = 0,
                        author = "Me",
                        authorId = 0,
                        content = content,
                        published = "",
                        likes = 0,
                        shares = 0,
                        views = 0,
                        likedByMe = false,
                        sharedByMe = false,
                        viewedByMe = false
                    )
                    repository.saveAsync(newPost, update = false, image = _photo.value?.file)
                } else {
                    val updatedPost = currentEdited.copy(content = content)
                    repository.saveAsync(updatedPost, update = true, image = _photo.value?.file)
                }

                edited.value = empty
                _photo.value = null
                _postCreated.value = Unit

                _state.value = FeedModelState(refreshing = true)
            } catch (e: Exception) {
                Log.e("PostViewModel", "Save error: ${e.message}")
                _state.value = FeedModelState(error = true)
            } finally {
                isSaving = false
            }
        }
    }

    fun retryPost(post: Post) {
        viewModelScope.launch {
            try {
                repository.saveAsync(post, update = true, image = null)
            } catch (e: Exception) {
                Log.e("PostViewModel", "Retry failed: ${e.message}")
            }
        }
    }

    fun load() {
        _state.value = FeedModelState(loading = true)
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun clear() {
        edited.value = empty
        _photo.value = null
    }

    fun changePhoto(uri: Uri, file: File) {
        _photo.value = PhotoModel(uri, file)
    }

    fun removePhoto() {
        _photo.value = null
    }
}