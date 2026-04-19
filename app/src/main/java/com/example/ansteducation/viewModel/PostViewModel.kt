package com.example.ansteducation.viewModel

import android.content.Context
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
import com.example.ansteducation.dto.FeedItem
import com.example.ansteducation.dto.Post
import com.example.ansteducation.model.FeedModelState
import com.example.ansteducation.model.PhotoModel
import com.example.ansteducation.R
import com.example.ansteducation.repository.PostRepository
import com.example.ansteducation.util.requiresSignIn
import com.example.ansteducation.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    published = "",
)

@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: PostRepository,
    private val appAuth: AppAuth,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val data: Flow<PagingData<FeedItem>> = appAuth.authState.flatMapLatest { token ->
        repository.data
            .map { pagingData ->
                pagingData.map { post ->
                    if (post is Post) {
                        post.copy(ownedByMe = post.authorId == token?.id)
                    } else {
                        post
                    }
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

    private val _mentionIds = MutableLiveData<List<Long>>(emptyList())
    val mentionIds: LiveData<List<Long>> = _mentionIds

    private val _singlePost = MutableLiveData<Post?>(null)
    val singlePost: LiveData<Post?> = _singlePost

    private val _singlePostError = MutableLiveData<String?>(null)
    val singlePostError: LiveData<String?> = _singlePostError

    private val _snackbarMessage = SingleLiveEvent<String>()
    val snackbarMessage: LiveData<String> = _snackbarMessage

    private fun postSnackbarFromException(e: Exception, fallbackRes: Int) {
        _snackbarMessage.value = when {
            e.requiresSignIn() -> appContext.getString(R.string.snackbar_sign_in_required)
            else -> e.message?.takeIf { it.isNotBlank() }
                ?: appContext.getString(fallbackRes)
        }
    }

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
                val updated = repository.likeByIdAsync(post)
                val uid = appAuth.authState.value?.id
                val merged = updated.copy(ownedByMe = updated.authorId == uid)
                if (_singlePost.value?.id == post.id) {
                    _singlePost.value = merged
                }
            } catch (e: Exception) {
                Log.e("PostViewModel", "Like error: ${e.message}", e)
                postSnackbarFromException(e, R.string.no_response_like)
            }
        }
    }

    fun repost(id: Long) {
        viewModelScope.launch {
            try {
                repository.shareById(id)
                if (_singlePost.value?.id == id) {
                    _singlePost.value = repository.getById(id)
                }
            } catch (e: Exception) {
                Log.e("PostViewModel", "Repost error: ${e.message}")
                postSnackbarFromException(e, R.string.no_response_like)
            }
        }
    }

    fun remove(id: Long) {
        viewModelScope.launch {
            try {
                repository.removeByIdAsync(id)
            } catch (e: Exception) {
                Log.e("PostViewModel", "Remove error: ${e.message}")
                postSnackbarFromException(e, R.string.error_title)
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

                val mIds = _mentionIds.value.orEmpty()
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
                        viewedByMe = false,
                        mentions = emptyList(),
                        mentionIds = mIds.takeIf { it.isNotEmpty() },
                    )
                    repository.saveAsync(newPost, update = false, image = _photo.value?.file)
                } else {
                    val updatedPost = currentEdited.copy(
                        content = content,
                        mentions = emptyList(),
                        mentionIds = mIds.takeIf { it.isNotEmpty() },
                    )
                    repository.saveAsync(updatedPost, update = true, image = _photo.value?.file)
                }

                edited.value = empty
                _photo.value = null
                _mentionIds.value = emptyList()
                _postCreated.value = Unit

                _state.value = FeedModelState(refreshing = true)
            } catch (e: Exception) {
                Log.e("PostViewModel", "Save error: ${e.message}")
                postSnackbarFromException(e, R.string.no_response_send_post)
                _state.value = FeedModelState(error = true)
            } finally {
                isSaving = false
            }
        }
    }

    fun retryPost(post: Post) {
        viewModelScope.launch {
            try {
                repository.saveAsync(post, update = false, image = null)
            } catch (e: Exception) {
                Log.e("PostViewModel", "Retry failed: ${e.message}")
                postSnackbarFromException(e, R.string.no_response_send_post)
            }
        }
    }

    fun load() {
        _state.value = FeedModelState(loading = true)
    }

    fun edit(post: Post) {
        edited.value = post
        _mentionIds.value = post.mentions.orEmpty().map { it.id }
    }

    fun setMentionIds(ids: List<Long>) {
        _mentionIds.value = ids.distinct()
    }

    fun clear() {
        edited.value = empty
        _photo.value = null
        _mentionIds.value = emptyList()
    }

    fun changePhoto(uri: Uri, file: File) {
        _photo.value = PhotoModel(uri, file)
    }

    fun removePhoto() {
        _photo.value = null
    }

    fun loadPost(id: Long) {
        viewModelScope.launch {
            if (_singlePost.value?.id == id && _singlePostError.value == null) {
                return@launch
            }
            _singlePost.value = null
            _singlePostError.value = null
            try {
                _singlePost.value = repository.getById(id)
            } catch (e: Exception) {
                _singlePost.value = null
                _singlePostError.value = e.message ?: "Не удалось загрузить пост"
            }
        }
    }
}