package com.example.ansteducation.viewModel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ansteducation.R
import com.example.ansteducation.api.PostApi
import com.example.ansteducation.auth.AppAuth
import com.example.ansteducation.dto.CommentPayload
import com.example.ansteducation.dto.PostComment
import com.example.ansteducation.fragment.PostCommentsFragment
import com.example.ansteducation.util.requiresSignIn
import com.example.ansteducation.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class PostCommentsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val postApi: PostApi,
    private val appAuth: AppAuth,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    private val postId: Long =
        savedStateHandle[PostCommentsFragment.ARG_POST_ID] ?: error("postId missing")

    private val _comments = MutableLiveData<List<PostComment>>(emptyList())
    val comments: LiveData<List<PostComment>> = _comments

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _snackbarMessage = SingleLiveEvent<String>()
    val snackbarMessage: LiveData<String> = _snackbarMessage

    init {
        loadComments()
    }

    fun loadComments() {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null
                _comments.value = postApi.getComments(postId)
            } catch (e: HttpException) {
                if (e.code() == 404) {
                    _error.value = appContext.getString(R.string.comments_endpoint_unavailable)
                    _comments.value = emptyList()
                } else {
                    _error.value = e.message()
                }
            } catch (e: Exception) {
                _error.value = e.message ?: appContext.getString(R.string.error_title)
            } finally {
                _loading.value = false
            }
        }
    }

    fun sendComment(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        if (appAuth.authState.value?.id == null) {
            _snackbarMessage.value = appContext.getString(R.string.snackbar_sign_in_required)
            return
        }
        viewModelScope.launch {
            try {
                _loading.value = true
                val created = postApi.addComment(
                    postId,
                    CommentPayload(content = trimmed),
                )
                _comments.value = _comments.value.orEmpty() + created
            } catch (e: Exception) {
                if (e.requiresSignIn()) {
                    _snackbarMessage.value = appContext.getString(R.string.snackbar_sign_in_required)
                } else {
                    _snackbarMessage.value =
                        e.message ?: appContext.getString(R.string.error_title)
                }
            } finally {
                _loading.value = false
            }
        }
    }
}
