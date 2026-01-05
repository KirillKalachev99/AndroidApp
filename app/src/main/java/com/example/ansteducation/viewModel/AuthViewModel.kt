package com.example.ansteducation.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.ansteducation.api.AuthApi
import com.example.ansteducation.auth.AppAuth
import com.example.ansteducation.dto.Token
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val appAuth: AppAuth,
    private val authApi: AuthApi
) : ViewModel() {
    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    val data: LiveData<Token?> = appAuth
        .authState
        .asLiveData()

    val isAuthorized: Boolean
        get() = data.value != null

    fun authenticate(login: String, password: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                val token = authApi.authenticate(login, password)
                println("Token: $token")
                appAuth.setAuth(token)
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Authentication failed")
            }
        }
    }

    fun clearAuth() {
        appAuth.clear()
    }

    sealed class AuthState {
        object Idle : AuthState()
        object Loading : AuthState()
        object Success : AuthState()
        data class Error(val message: String) : AuthState()
    }
}