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
class UserProfileHeaderViewModel @Inject constructor(
    private val userApi: UserApi,
) : ViewModel() {

    private val _user = MutableLiveData<User?>(null)
    val user: LiveData<User?> = _user

    fun load(userId: Long) {
        viewModelScope.launch {
            _user.value = runCatching { userApi.getById(userId) }.getOrNull()
        }
    }
}
