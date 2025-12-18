package com.example.ansteducation.viewModel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.ansteducation.api.PostApi
import com.example.ansteducation.auth.AppAuth
import com.example.ansteducation.db.AppDb
import com.example.ansteducation.dto.Post
import com.example.ansteducation.model.FeedModel
import com.example.ansteducation.model.FeedModelState
import com.example.ansteducation.model.PhotoModel
import com.example.ansteducation.repository.PostRepository
import com.example.ansteducation.repository.PostRepositoryImpl
import com.example.ansteducation.util.SingleLiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File

private val empty = Post(
    id = 0,
    author = "",
    authorId = 0,
    content = "",
    published = ""
)

class PostViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PostRepository = PostRepositoryImpl(
        AppDb.getInstance(application).postDao()
    )

    val data: LiveData<FeedModel> = AppAuth.getInstance().authState.flatMapLatest { token ->
        repository.data
            .map { posts ->
                FeedModel(posts.map { post ->
                    post.copy(ownedByMe = post.authorId == token?.id)
                }, posts.isEmpty())
            }
            .catch { emit(FeedModel(emptyList(), true)) }
    }
        .asLiveData(Dispatchers.Default)

    private val _state = MutableLiveData(FeedModelState())
    private val _postCreated = SingleLiveEvent<Unit>()

    val state: LiveData<FeedModelState>
        get() = _state
    val edited = MutableLiveData(empty)
    private var isSaving = false

    private val _shouldCheckNewPosts = MutableLiveData(true)
    val newerCount = _shouldCheckNewPosts.switchMap { shouldCheck ->
        if (shouldCheck) {
            data.switchMap {
                repository.getNewer(it.posts.firstOrNull()?.id ?: 0)
                    .asLiveData(Dispatchers.Default)
            }
        } else {
            MutableLiveData(0).apply { value = 0 }
        }
    }
    private val _photo = MutableLiveData<PhotoModel?>(null)
    val photo: LiveData<PhotoModel?>
        get() = _photo


    init {
        load()
    }

    fun addNewer() {
        _shouldCheckNewPosts.value = false
        viewModelScope.launch {
            try {
                repository.addNewer()
                viewModelScope.launch {
                    delay(2000)
                    _shouldCheckNewPosts.value = true
                }
            } catch (e: Exception) {
                Log.e("PostViewModel", "Error adding newer: ${e.message}")
                _shouldCheckNewPosts.value = true
            }
        }
    }

    fun like(post: Post) {
        viewModelScope.launch {
            repository.likeByIdAsync(post)
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
                _postCreated.value = Unit

            } catch (e: Exception) {
                Log.e("PostViewModel", "Save error: ${e.message}")
            } finally {
                isSaving = false
            }
        }
    }

    fun retryPost(post: Post) {
        viewModelScope.launch {
            try {
                val sendingId = System.currentTimeMillis()
                val sendingPost = post.copy(id = sendingId)
                (repository as PostRepositoryImpl).updatePost(post.id, sendingPost)

                val serverPost = PostApi.service.save(post.copy(id = 0))

                repository.updatePost(sendingId, serverPost)

            } catch (e: Exception) {
                Log.e("PostViewModel", "Retry failed: ${e.message}")
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
            } catch (_: Exception) {
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

    fun changePhoto(uri: Uri, file: File) {
        _photo.value = PhotoModel(uri, file)
    }

    fun removePhoto() {
        _photo.value = null
    }

}
