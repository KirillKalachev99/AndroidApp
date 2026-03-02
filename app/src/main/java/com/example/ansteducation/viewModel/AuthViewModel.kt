package com.example.ansteducation.viewModel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.ansteducation.api.AuthApi
import com.example.ansteducation.auth.AppAuth
import com.example.ansteducation.dto.Token
import com.example.ansteducation.model.PhotoModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.HttpException
import java.io.File
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val appAuth: AppAuth,
    private val authApi: AuthApi
) : ViewModel() {
    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    private val _avatar = MutableLiveData<PhotoModel?>(null)
    val avatar: LiveData<PhotoModel?>
        get() = _avatar

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
            } catch (e: HttpException) {
                _authState.value = when (e.code()) {
                    400 -> AuthState.Error("Неправильный логин или пароль")
                    else -> AuthState.Error("Ошибка сервера: ${e.code()}")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Ошибка аутентификации")
            }
        }
    }

    fun register(login: String, name: String, password: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                val avatarPart = _avatar.value?.let { photo ->
                    okhttp3.MultipartBody.Part.createFormData(
                        "avatar",
                        photo.file.name,
                        photo.file.asRequestBody("image/*".toMediaType())
                    )
                }
                val token = authApi.register(login, password, name, avatarPart)
                appAuth.setAuth(token)
                _authState.value = AuthState.Success
            } catch (e: HttpException) {
                _authState.value = when (e.code()) {
                    400 -> AuthState.Error("Пользователь с таким логином уже зарегистрирован")
                    else -> AuthState.Error("Ошибка сервера: ${e.code()}")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Ошибка регистрации")
            }
        }
    }

    fun changeAvatar(uri: Uri, file: File) {
        _avatar.value = PhotoModel(uri, file)
    }

    fun removeAvatar() {
        _avatar.value = null
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