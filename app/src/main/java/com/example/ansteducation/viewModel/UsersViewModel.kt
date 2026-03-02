package com.example.ansteducation.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ansteducation.api.UserApi
import com.example.ansteducation.dto.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UsersViewModel @Inject constructor(
    private val userApi: UserApi
) : ViewModel() {

    private val _users = MutableLiveData<List<User>>(emptyList())
    val users: LiveData<List<User>> = _users

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    fun loadUsers() {
        if (_loading.value == true) return
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null
                _users.value = userApi.getAll()
            } catch (e: Exception) {
                _error.value = e.message ?: "Не удалось загрузить пользователей"
            } finally {
                _loading.value = false
            }
        }
    }
}

