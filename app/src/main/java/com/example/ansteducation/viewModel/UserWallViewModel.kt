package com.example.ansteducation.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ansteducation.api.PostApi
import com.example.ansteducation.dto.Post
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserWallViewModel @Inject constructor(
    private val postApi: PostApi
) : ViewModel() {

    private val _posts = MutableLiveData<List<Post>>(emptyList())
    val posts: LiveData<List<Post>> = _posts

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    fun loadWall(userId: Long) {
        if (_loading.value == true) return
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null
                _posts.value = postApi.getPostsByAuthor(userId)
            } catch (e: Exception) {
                _error.value = e.message ?: "Не удалось загрузить стену пользователя"
            } finally {
                _loading.value = false
            }
        }
    }
}

